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

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;
import org.fusesource.jansi.Ansi;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Paper 1.12+ has a freeze / spike detector
 * that shows the stack trace when a spike occurs.
 * <p>
 * This class backports this to older server versions.
 */
public final class SpikeDetector extends Thread {

    private static volatile boolean hasStarted;

    @Nullable
    private static SpikeDetector instance;

    private static final long earlyWarningEvery = 5000L;
    private static final long earlyWarningDelay = 10000L;

    private final Thread serverThread;

    private long lastEarlyWarning;
    private volatile long lastTick;

    private volatile boolean stopping;

    private SpikeDetector(final Thread serverThread) {
        super("Skript Watchdog Thread");
        super.setPriority(Thread.MIN_PRIORITY);

        this.serverThread = serverThread;
    }

    public static final void testSpike() {
        try {
            Thread.sleep(earlyWarningDelay);
        } catch (final InterruptedException e) {
            Thread.interrupted();
            Skript.exception(e);
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

    public static final void setEnabled(final boolean enabled) {
        SpikeDetector.hasStarted = enabled;

        if (enabled)
            Skript.info("Enabled spike detector");
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

    private static final void dumpThread(final Thread thread, final State state, final boolean suspended, final ThreadInfo threadInfo, final MonitorInfo[] monitorInfo, final StackTraceElement[] stackTrace, final Logger log, final String prefix) {
        log.log(Level.WARNING, prefix + "------------------------------");

        log.log(Level.WARNING, prefix + "Current Thread: " + threadInfo.getThreadName());
        log.log(Level.WARNING, prefix + "\tPID: " + threadInfo.getThreadId() + " | Suspended: " + suspended + " | Native: " + threadInfo
                .isInNative() + " | Daemon: " + thread
                .isDaemon() + " | State: " + state + " | Priority: " + thread
                .getPriority());
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

            if (lastTick != 0L && currentTime > lastTick + earlyWarningEvery) {
                if (earlyWarningEvery <= 0L || !hasStarted || currentTime < lastEarlyWarning + earlyWarningEvery || currentTime < lastTick + earlyWarningDelay) {
                    continue;
                }
                lastEarlyWarning = currentTime;

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

                Bukkit.getScheduler().runTask(Skript.getInstance(), () -> {
                    // Print colored to make admins pay attention, if possible
                    final String prefix = Skript.hasJLineSupport() && Skript.hasJansi() ?
                            Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString() : "";

                    // Print scheduled because async printing mixes messages
                    log.log(Level.WARNING, prefix + "The server has not responded for " + spikeTime + " seconds! Creating thread dump...");
                    log.log(Level.WARNING, prefix + "Bukkit: " + Bukkit.getServer().getVersion() + " | Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + ")" + " | OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version") + ("64".equalsIgnoreCase(System.getProperty("sun.arch.data.model")) ? " (x64)" : " (x86)") + " | Cores: " + Runtime.getRuntime().availableProcessors() + " | Host: " + Skript.ipAddress);

                    log.log(Level.WARNING, prefix + "------------------------------");
                    log.log(Level.WARNING, prefix + "Server thread dump:");
                    dumpThread(serverThread, threadState, suspended, threadInfo, monitorInfo, stackTrace, log, prefix);
                    log.log(Level.WARNING, prefix + "------------------------------");
                });
            }

            try {
                sleep(1000L);
            } catch (final InterruptedException e) {
                interrupt();
            }
        }
    }

}
