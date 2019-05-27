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

package ch.njol.skript.log;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.log.LogHandler.LogResult;
import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ch.njol.skript.Skript.SKRIPT_PREFIX_CONSOLE;

/**
 * @author Peter Güttinger
 */
public final class SkriptLogger {

    @SuppressWarnings("null")
    public static final Level SEVERE = Level.SEVERE;
    @SuppressWarnings("null")
    public static final Level DEBUG = Level.INFO;
    @SuppressWarnings("null")
    public static final Logger LOGGER = Bukkit.getServer() != null ? Bukkit.getLogger() : Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // cannot use Bukkit in tests
    private static final HandlerList handlers = new HandlerList();
    static boolean debug;
    @Nullable
    private static Node node;
    private static Verbosity verbosity = Verbosity.NORMAL;
    private static final List<LogEntry> suppressed = new ArrayList<>();
    private static boolean suppressing;
    private static boolean suppressWarnings;
    private static boolean suppressErrors;

    /**
     * Shorthand for <tt>{@link #startLogHandler(LogHandler) startLogHandler}(new {@link RetainingLogHandler}());</tt>
     *
     * @return A newly created RetainingLogHandler
     */
    public static final RetainingLogHandler startRetainingLog() {
        return startLogHandler(new RetainingLogHandler());
    }

    /**
     * Shorthand for <tt>{@link #startLogHandler(LogHandler) startLogHandler}(new {@link ParseLogHandler}());</tt>
     *
     * @return A newly created ParseLogHandler
     */
    public static final ParseLogHandler startParseLogHandler() {
        return startLogHandler(new ParseLogHandler());
    }

    /**
     * Starts a log handler.
     * <p>
     * This should be used like this:
     *
     * <pre>
     * LogHandler log = SkriptLogger.startLogHandler(new ...LogHandler());
     * try {
     * 	doSomethingThatLogsMessages();
     * 	// do something with the logged messages
     * } finally {
     * 	log.stop();
     * }
     * </pre>
     *
     * @return The passed LogHandler
     * @see #startParseLogHandler()
     * @see #startRetainingLog()
     * @see BlockingLogHandler
     * @see CountingLogHandler
     * @see ErrorDescLogHandler
     * @see FilteringLogHandler
     * @see RedirectingLogHandler
     */
    public static final <T extends LogHandler> T startLogHandler(final T h) {
        handlers.add(h);
        return h;
    }

    static final void removeHandler(final LogHandler h) {
        if (!handlers.contains(h))
            return;
        if (!h.equals(handlers.remove())) {
            int i = 1;
            while (!h.equals(handlers.remove()))
                i++;
            LOGGER.severe("[Skript] " + i + " log handler" + (i == 1 ? " was" : "s were") + " not stopped properly! (at " + getCaller() + ") [if you're a server admin and you see this message please create a bug report at " + Skript.ISSUES_LINK + " if there is not already one]");
        }
    }

    static final boolean isStopped(final LogHandler h) {
        return !handlers.contains(h);
    }

    @Nullable
    static final StackTraceElement getCaller() {
        for (final StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (!e.getClassName().startsWith(SkriptLogger.class.getPackage().getName()) && !e.toString().contains("java.lang.Thread.getStackTrace") && !e.toString().contains("java.lang.Thread.currentThread"))
                return e;
        }
        return null;
    }

    public static final void setVerbosity(final Verbosity v) {
        verbosity = v;
        if (v.compareTo(Verbosity.DEBUG) >= 0)
            debug = true;
    }

    @Nullable
    public static final Node getNode() {
        return node;
    }

    public static final void setNode(final @Nullable Node node) {
        SkriptLogger.node = node == null || node.getParent() == null ? null : node;
    }

    /**
     * Logging should be done like this:
     *
     * <pre>
     * if (Skript.logNormal())
     * 	Skript.info(&quot;this information is displayed on verbosity normal or higher&quot;);
     * </pre>
     *
     * @param level
     * @param message
     * @see Skript#info(String)
     * @see Skript#warning(String)
     * @see Skript#error(String)
     * @see Skript#logNormal()
     * @see Skript#logHigh()
     * @see Skript#logVeryHigh()
     * @see Skript#debug()
     */
    public static final void log(final Level level, final String message) {
        log(new LogEntry(level, message, node));
    }

    public static final void log(final @Nullable LogEntry entry) {
        if (entry == null)
            return;
        if (Skript.testing() && node != null && node.debug())
            System.out.print("---> " + entry.level + "/" + ErrorQuality.get(entry.quality) + ": " + entry.getMessage() + " ::" + LogEntry.findCaller());
        for (final LogHandler h : handlers) {
            final LogResult r = h.log(entry);
            switch (r) {
                case CACHED:
                    return;
                case DO_NOT_LOG:
                    entry.discarded("denied by " + h);
                    return;
                case LOG:
            }
        }
        if (suppressing) {
            suppressed.add(entry);
            return;
        }
        if (suppressWarnings && entry.getLevel() == Level.WARNING) {
            return;
        }
        if (suppressErrors && entry.getLevel() == Level.SEVERE) {
            return;
        }
        entry.logged();
        LOGGER.log(entry.getLevel(), format(entry));
    }

    public static final void suppressWarnings(final boolean suppressWarnings) {
        SkriptLogger.suppressWarnings = suppressWarnings;
    }

    public static final void suppressErrors(final boolean suppressErrors) {
        SkriptLogger.suppressErrors = suppressErrors;
    }

    public static final void startSuppressing() {
        suppressed.clear();
        suppressing = true;
    }

    @SuppressWarnings("null")
    public static final List<LogEntry> stopSuppressing() {
        return suppressed;
    }

    public static final void cleanSuppressState() {
        suppressed.clear();
    }

    public static final String format(final LogEntry entry) {
        return SKRIPT_PREFIX_CONSOLE + entry.getMessage();
    }

    public static final void logAll(final Iterable<LogEntry> entries) {
        for (final LogEntry entry : entries) {
            if (entry == null)
                continue;
            log(entry);
        }
    }

    public static final void logTracked(final Level level, final String message, final ErrorQuality quality) {
        log(new LogEntry(level, quality.quality(), message, node, true));
    }

    /**
     * Checks whatever messages should be logged for the given verbosity.
     *
     * @param minVerb minimal verbosity
     * @return Whatever messages should be logged for the given verbosity.
     */
    public static final boolean log(final Verbosity minVerb) {
        return minVerb.compareTo(verbosity) <= 0;
    }

    public static final boolean debug() {
        return debug;
    }

}
