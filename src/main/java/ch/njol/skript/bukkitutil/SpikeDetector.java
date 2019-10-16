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
import ch.njol.skript.classes.Parser;
import ch.njol.skript.config.Node;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.misc.Tickable;
import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;
import org.fusesource.jansi.Ansi;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.management.LockInfo;
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
    private static final long earlyWarningDelay = Long.getLong("skript.spikeDetector.earlyWarningDelay", 10000L);
    private static final long earlyWarningEvery = Long.getLong("skript.spikeDetector.earlyWarningEvery", earlyWarningDelay / 2L);
    private static final long sleepMillisDelay = Long.getLong("skript.spikeDetector.sleepMillisDelay", 100L); // 100L to get truer stack traces
    private static final long earlyWarningCooldown = Long.getLong("skript.spikeDetector.earlyWarningCooldown", earlyWarningEvery - sleepMillisDelay);
    private static volatile boolean hasStarted = alwaysEnabled;
    private static volatile boolean enabled = alwaysEnabled;
    @Nullable
    private static SpikeDetector instance;
    private final Thread serverThread;
    private volatile long lastEarlyWarning;
    private volatile long lastTick;
    private volatile boolean stopping;
    private static final Tickable sampleTicker = SpikeDetector::tick;

    private SpikeDetector(final Thread serverThread) {
        super("Skript spike detector");
        setPriority(Thread.MAX_PRIORITY);

        this.serverThread = serverThread;

        assert serverThread != this : this;
        assert !Skript.isBukkitRunning() || Bukkit.isPrimaryThread() : Thread.currentThread();
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
            Thread.sleep(earlyWarningEvery);
        } catch (final InterruptedException e) {
            Skript.exception(e);
            Thread.currentThread().interrupt();
        }
    }

    private static final long monotonicMillis() {
        return System.nanoTime() / 100_00_00L;
    }

    /**
     * Does nothing if started already
     */
    public static final void doStart(final Thread serverThread) {
        if (instance == null) {
            instance = new SpikeDetector(serverThread);
            instance.start();

            TickUtils.everyTick(sampleTicker);
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

    public static final boolean isEnabled() {
        return SpikeDetector.enabled;
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

    public static final void flushOutErr() {
        System.err.flush(); // Errors first
        System.out.flush();
    }

    public static final void dumpMainThreadStack() {
        dumpMainThreadStack(null);
    }

    @SuppressWarnings("null")
    public static final void dumpMainThreadStack(@Nullable final String customMessage) {
        assert Skript.serverThread != null;

        dumpMainThreadStack(Skript.serverThread, Skript.minecraftLogger, false, customMessage, 0, 0);
    }

    public static final void dumpMainThreadStack(final Thread serverThread,
                                                 final Logger log,
                                                 final boolean freeze,
                                                 final long currentTime,
                                                 final long lastTick) {
        dumpMainThreadStack(serverThread, log, freeze, null, currentTime, lastTick);
    }

    @SuppressWarnings("null")
    public static final void dumpMainThreadStack(final Thread serverThread,
                                                 final Logger log,
                                                 final boolean freeze,
                                                 @Nullable final String customMessage,
                                                 final long currentTime,
                                                 final long lastTick) {
        // Minimize server thread to get true stack trace
        final int oldPriority = serverThread.getPriority();
        serverThread.setPriority(Thread.MIN_PRIORITY);

        // Get true spike time here
        final long spikeTime = (currentTime + sleepMillisDelay - lastTick) / 1000L;

        // Get true stack trace here
        final ThreadInfo threadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(serverThread.getId(), Integer.MAX_VALUE);
        final StackTraceElement[] stackTrace = threadInfo.getStackTrace();

        // Get true monitor info here
        final MonitorInfo[] monitorInfo = threadInfo.getLockedMonitors();
        final State threadState = threadInfo.getThreadState();

        // Get true lock info here
        final LockInfo lockInfo = threadInfo.getLockInfo();
        final LockInfo[] lockedSynchronizers = threadInfo.getLockedSynchronizers();

        // Get true alive and suspended status here
        final boolean alive = serverThread.isAlive();
        final boolean suspended = threadInfo.isSuspended();

        // Get true interrupted status here
        final boolean interrupted = serverThread.isInterrupted();

        // Print colored to make admins pay attention, if possible
        final String prefix = Skript.hasJLineSupport() && Skript.hasJansi() ?
                Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString() : "";

        final String suffix = Skript.hasJLineSupport() && Skript.hasJansi() ?
                Ansi.ansi().a(Ansi.Attribute.RESET).reset().toString() : "";

        if (freeze)
            log.log(Level.WARNING, prefix + "The server has not responded for " + spikeTime + " seconds! Creating thread dump...");
        if (customMessage != null)
            log.log(Level.WARNING, prefix + customMessage);
        log.log(Level.WARNING, prefix + "Bukkit: " + Bukkit.getServer().getVersion() + (Skript.version != null ? " | Skript: " + Skript.version : "") + " | Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + ' ' + System.getProperty("java.vm.version") + ')' + " | OS: " + System.getProperty("os.name") + ' ' + System.getProperty("os.arch") + ' ' + System.getProperty("os.version") + ("64".equalsIgnoreCase(System.getProperty("sun.arch.data.model")) ? " (x64)" : " (x86)") + " | Cores: " + Runtime.getRuntime().availableProcessors() + " | Host: " + Skript.ipAddress);

        log.log(Level.WARNING, prefix + "------------------------------");
        log.log(Level.WARNING, prefix + "Server thread dump:");
        dumpThread(serverThread, threadState, oldPriority, alive, suspended, interrupted, threadInfo, lockInfo, lockedSynchronizers, monitorInfo, stackTrace, log, prefix);
        final Node node;
        if ((node = SkriptLogger.getNode()) != null)
            log.log(Level.WARNING, prefix + "\tNode: " + node);
        final Parser<?> lastParser;
        final Class<?> lastParserClass;
        if ((lastParser = Classes.lastCheckedParser) != null && (lastParserClass = lastParser.getClass()) != null) {
            final Class<?> enclosingClass;
            log.log(Level.WARNING, prefix + "\tParser: " + lastParserClass + " (" + ((enclosingClass = Classes.getEnclosingClass(lastParserClass)) != null ? enclosingClass : "no enclosing class") + ')');
        }
        final Class<?> currentlyConstructing;
        if ((currentlyConstructing = Skript.currentlyConstructing) != null) {
            log.log(Level.WARNING, "Thread is currently creating an instance of " + currentlyConstructing + " - constructor or reflection invocation may taken too long!");
        }
        log.log(Level.WARNING, prefix + "------------------------------" + suffix);

        // Flush to guarantee everything is written
        flushOutErr();

        // Finally restore the priority to not cause issues
        serverThread.setPriority(oldPriority);
    }

    private static final void dumpThread(final Thread thread, final State state, final int priority, final boolean alive, final boolean suspended, final boolean interrupted, final ThreadInfo threadInfo, @Nullable final LockInfo lockInfo, final LockInfo[] lockedSynchronizers, final MonitorInfo[] monitorInfo, final StackTraceElement[] stackTrace, final Logger log, final String prefix) {
        log.log(Level.WARNING, prefix + "------------------------------");

        log.log(Level.WARNING, prefix + "Thread Name: " + threadInfo.getThreadName());
        log.log(Level.WARNING, prefix + "\tPID: " + threadInfo.getThreadId() + " | Alive: " + alive + " | Suspended: " + suspended + " | Interrupted: " + interrupted + " | Native: " + threadInfo
                .isInNative() + " | Daemon: " + thread
                .isDaemon() + " | State: " + state + " | Priority: " + priority);
        if (monitorInfo.length > 0) {
            log.log(Level.WARNING, prefix + "\tThread is waiting on monitor(s):");
            for (final MonitorInfo monitor : monitorInfo) {
                log.log(Level.WARNING, prefix + "\t\tLocked on: " + monitor.getLockedStackFrame());
            }
        }
        if (lockInfo != null) {
            log.log(Level.WARNING, prefix + "\tThread is locked on:");
            log.log(Level.WARNING, prefix + "\t\tLocked on: " + lockInfo.getClassName());
        }
        if (lockedSynchronizers.length > 0) {
            log.log(Level.WARNING, prefix + "\tThread is locked on synchronizer(s):");
            for (final LockInfo lock : lockedSynchronizers) {
                log.log(Level.WARNING, prefix + "\t\tLocked on: " + lock.getClassName());
            }
        }
        log.log(Level.WARNING, prefix + "\tStack:");
        for (final StackTraceElement stack : stackTrace) {
            log.log(Level.WARNING, prefix + "\t\t" + stack);
        }
    }

    @SuppressWarnings("null")
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

            if (lastTick != 0L && currentTime + sleepMillisDelay >= lastTick + earlyWarningEvery && !(earlyWarningEvery <= 0L || !hasStarted || !enabled || currentTime < lastEarlyWarning + /*earlyWarningEvery*/ earlyWarningCooldown /*|| currentTime < lastTick + earlyWarningDelay*/)) {
                lastEarlyWarning = currentTime;

                dumpMainThreadStack(serverThread, log, true, currentTime, lastTick);
            }

            try {
                sleep(sleepMillisDelay);
            } catch (final InterruptedException e) {
                interrupt();
                return;
            }
        }
    }

}
