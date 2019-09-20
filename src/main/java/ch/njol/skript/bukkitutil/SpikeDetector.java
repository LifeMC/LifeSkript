/*
 *
 *     This file is part of Skript.
 *
 *    Skript is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Skript is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Skript. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 *   Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.bukkitutil;

import ch.njol.skript.ServerPlatform;
import ch.njol.skript.Skript;
import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;
import org.fusesource.jansi.Ansi;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Paper 1.12+ has a freeze / spike detector
 * that shows the stack trace when a spike occurs.
 * <p>
 * This class backports this to older server versions.
 */
public final class SpikeDetector extends Thread {

    public static final boolean alwaysEnabled = Boolean.getBoolean("skript.spikeDetector.alwaysEnabled");
    @Nullable
    private static final Class<?> watchdogClass =
            Skript.classForName("org.spigotmc.WatchdogThread");
    private static final boolean spigotWatchdog =
            watchdogClass != null;
    @Nullable
    private static final Field instanceField = Skript.fieldForName(watchdogClass, "instance", true);
    private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
    @Nullable
    private static final MethodHandle doStop = findDoStop();
    @Nullable
    private static final MethodHandle doStart = findDoStart();
    private static final long earlyWarningEvery = Long.getLong("skript.earlyWarningEvery", 5000L);
    private static final long earlyWarningDelay = Long.getLong("skript.earlyWarningDelay", 10000L);
    private static volatile boolean hasStarted = alwaysEnabled;
    private static volatile boolean enabled = alwaysEnabled;
    @Nullable
    private static SpikeDetector instance;
    private final Thread serverThread;
    private long lastEarlyWarning;
    private volatile long lastTick;
    private volatile boolean stopping;

    private SpikeDetector(final Thread serverThread) {
        super("Skript spike detector");
        super.setPriority(Thread.MAX_PRIORITY);

        this.serverThread = serverThread;
    }

    @Nullable
    private static final MethodHandle findDoStop() {
        if (spigotWatchdog) {
            try {
                return publicLookup.findStatic(Objects.requireNonNull(watchdogClass), "doStop", MethodType.methodType(void.class));
            } catch (final NoSuchMethodException | IllegalAccessException e) {
                Skript.exception(e);
            }
        }
        return null;
    }

    @Nullable
    private static final MethodHandle findDoStart() {
        if (spigotWatchdog) {
            try {
                return publicLookup.findStatic(Objects.requireNonNull(watchdogClass), "doStart", MethodType.methodType(void.class, int.class, boolean.class));
            } catch (final NoSuchMethodException | IllegalAccessException e) {
                Skript.exception(e);
            }
        }
        return null;
    }

    public static final void testSpike() {
        try {
            Thread.sleep(earlyWarningDelay);
        } catch (final InterruptedException e) {
            Skript.exception(e);
            Thread.currentThread().interrupt();
        }
    }

    private static final long monotonicMillis() {
        return System.nanoTime() / 1000000L;
    }

    /**
     * Does nothing if started already
     */
    public static final void doStart(final Thread serverThread) {
        if (instance == null) {
            instance = new SpikeDetector(serverThread);
            instance.start();
        }
    }

    /**
     * Paper 1.12.x has already this, so check for it
     */
    public static final boolean shouldStart() {
        return alwaysEnabled || (!Skript.isRunningMinecraft(1, 12) || Skript.getServerPlatform() != ServerPlatform.BUKKIT_PAPER && Skript.getServerPlatform() != ServerPlatform.BUKKIT_TACO) && !Boolean.getBoolean("skript.disableSpikeDetector");
    }

    public static final void setEnabled(final boolean enabled) {
        SpikeDetector.hasStarted = enabled;
        SpikeDetector.enabled = enabled;

        if (enabled)
            Skript.debug("Enabled spike detector");
        else
            Skript.info("Spike detector is disabled");
    }

    public static final void tick() {
        if (instance != null) {
            instance.lastTick = monotonicMillis();
        }
    }

    public static final void doStop() {
        if (instance != null) {
            instance.stopping = true;
        }
    }

    /**
     * Stops the spike detector and spigot watchdog temporarily.
     * Useful for too long operations on runtime, like /sk reload all.
     * <p>
     * We know it will complete normally, but it will take long if scripts
     * are too long. Use with caution.
     *
     * @see SpikeDetector#startAgain()
     */
    public static final void stopTemporarily() {
        if (hasStarted && !alwaysEnabled)
            hasStarted = false;

        final MethodHandle stop = doStop;

        if (spigotWatchdog && stop != null) {
            try {
                stop.invokeExact();
            } catch (final Throwable tw) {
                Skript.exception(tw);
            }
        }
    }

    /**
     * Starts the spike detector and spigot watchdog.
     * <p>
     * This method may run two spigot watchdog threads at same time if
     * does not used carefully!
     *
     * @see SpikeDetector#stopTemporarily()
     */
    public static final void startAgain() {
        if (!hasStarted && shouldStart() && enabled) {
            tick(); // Clear spikes
            hasStarted = true;
        }

        final Field instance = instanceField;
        final MethodHandle start = doStart;

        if (spigotWatchdog && instance != null && start != null) {
            try {
                instance.set(null, null);
                start.invokeExact((int) Objects.requireNonNull(SpigotConfig.<Integer>get("timeoutTime")), (boolean) Objects.requireNonNull(SpigotConfig.<Boolean>get("restartOnCrash")));
            } catch (final Throwable tw) {
                Skript.exception(tw);
            }
        }
    }

    private static final void dumpThread(final Thread thread, final State state, final int priority, final boolean suspended, final ThreadInfo threadInfo, final MonitorInfo[] monitorInfo, final StackTraceElement[] stackTrace, final Logger log, final String prefix) {
        log.log(Level.WARNING, prefix + "------------------------------");

        log.log(Level.WARNING, prefix + "Current Thread: " + threadInfo.getThreadName());
        log.log(Level.WARNING, prefix + "\tPID: " + threadInfo.getThreadId() + " | Suspended: " + suspended + " | Native: " + threadInfo
                .isInNative() + " | Daemon: " + thread
                .isDaemon() + " | State: " + state + " | Priority: " + priority);
        if (monitorInfo.length != 0) {
            log.log(Level.WARNING, prefix + "\tThread is waiting on monitor(s):");
            for (final MonitorInfo monitor : monitorInfo) {
                log.log(Level.WARNING, prefix + "\t\tLocked on:" + monitor.getLockedStackFrame());
            }
        }
        log.log(Level.WARNING, prefix + "\tStack:");
        for (final StackTraceElement stack : stackTrace) {
            log.log(Level.WARNING, prefix + "\t\t" + stack);
        }
    }

    @Override
    public final void run() {
        while (!stopping) {
            final Logger log = Skript.minecraftLogger;
            if (log == null) {
                Skript.warning("Can't find logger, disabling spike detector");

                // For sanity
                stopping = true;
                doStop();

                // Actually stops it
                return;
            }

            final long currentTime = monotonicMillis();

            if (lastTick != 0L && currentTime >= lastTick + earlyWarningEvery && !(earlyWarningEvery <= 0L || !hasStarted || !enabled || currentTime <= lastEarlyWarning + earlyWarningEvery /*|| currentTime <= lastTick + earlyWarningDelay*/)) {
                lastEarlyWarning = currentTime;

                // Minimize server thread to get true stack trace
                final int oldPriority = serverThread.getPriority();
                serverThread.setPriority(Thread.MIN_PRIORITY);

                // Get true spike time here
                final long spikeTime = (currentTime - lastTick) / 1000L;

                // Get true stack trace here
                final ThreadInfo threadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(serverThread.getId(), Integer.MAX_VALUE);
                final StackTraceElement[] stackTrace = threadInfo.getStackTrace();

                // Get true monitor info here
                final MonitorInfo[] monitorInfo = threadInfo.getLockedMonitors();
                final State threadState = threadInfo.getThreadState();

                // Get true suspended status here
                final boolean suspended = threadInfo.isSuspended();

                // Print colored to make admins pay attention, if possible
                final String prefix = Skript.hasJLineSupport() && Skript.hasJansi() ?
                        Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString() : "";

                final String suffix = Skript.hasJLineSupport() && Skript.hasJansi() ?
                        Ansi.ansi().a(Ansi.Attribute.RESET).reset().toString() : "";

                log.log(Level.WARNING, prefix + "The server has not responded for " + spikeTime + " seconds! Creating thread dump...");
                log.log(Level.WARNING, prefix + "Bukkit: " + Bukkit.getServer().getVersion() + " | Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + ' ' + System.getProperty("java.vm.version") + ')' + " | OS: " + System.getProperty("os.name") + ' ' + System.getProperty("os.arch") + ' ' + System.getProperty("os.version") + ("64".equalsIgnoreCase(System.getProperty("sun.arch.data.model")) ? " (x64)" : " (x86)") + " | Cores: " + Runtime.getRuntime().availableProcessors() + " | Host: " + Skript.ipAddress);

                log.log(Level.WARNING, prefix + "------------------------------");
                log.log(Level.WARNING, prefix + "Server thread dump:");
                dumpThread(serverThread, threadState, oldPriority, suspended, threadInfo, monitorInfo, stackTrace, log, prefix);
                log.log(Level.WARNING, prefix + "------------------------------" + suffix);

                // Flush to guarantee everything is written
                System.err.flush();
                System.out.flush();

                // Finally restore the priority to not cause issues
                serverThread.setPriority(oldPriority);
            }

            try {
                sleep(100L); // 100L to get truer stack traces
            } catch (final InterruptedException e) {
                interrupt();
                return;
            }
        }
    }

}
