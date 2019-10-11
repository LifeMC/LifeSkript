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
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

/**
 * @author Peter Güttinger
 */
public class ParseLogHandler extends LogHandler {

    private final Collection<LogEntry> log = new ArrayList<>();
    boolean printedErrorOrLog;
    @Nullable
    private LogEntry error;

    @Override
    public final LogResult log(final LogEntry entry) {
        if (entry.getLevel().intValue() >= Level.SEVERE.intValue()) {
            final LogEntry e = error;
            if (e == null || entry.getQuality() > e.getQuality()) {
                error = entry;
                if (e != null)
                    e.discarded("overridden by '" + entry.getMessage() + "' (" + ErrorQuality.get(entry.getQuality()) + " > " + ErrorQuality.get(e.getQuality()) + ')');
            }
        } else {
            synchronized (log) {
                log.add(entry);
            }
        }
        return LogResult.CACHED;
    }

    @Override
    public void onStop() {
        if (!printedErrorOrLog && Skript.testing() && Skript.logVeryHigh())
            SkriptLogger.LOGGER.warning("Parse log wasn't instructed to print anything at " + SkriptLogger.getCaller());
    }

    public void error(final String error, final ErrorQuality quality) {
        log(new LogEntry(SkriptLogger.SEVERE, quality, error));
    }

    /**
     * Clears all log messages except for the error
     */
    public final void clear() {
        synchronized (log) {
            if (!log.isEmpty()) {
                for (final LogEntry e : log) {
                    e.discarded("cleared");
                }
            }
            {
                log.clear();
            }
        }
    }

    /**
     * Prints the retained log, but no errors
     */
    public final void printLog() {
        printedErrorOrLog = true;
        stop();
        synchronized (log) {
            SkriptLogger.logAll((Iterable<LogEntry>) log); // Cast is required to not use deprecated method
        }
        if (error != null)
            error.discarded("not printed");
    }

    public final void printError() {
        printError(null);
    }

    private final void notPrinted() {
        synchronized (log) {
            if (!log.isEmpty()) {
                for (final LogEntry e : log) {
                    e.discarded("not printed");
                }
            }
        }
    }

    /**
     * Prints the best error or the given error if no error has been logged.
     *
     * @param def Error to log if no error has been logged so far, can be null
     */
    public final void printError(@Nullable final String def) {
        printedErrorOrLog = true;
        stop();
        final LogEntry error = this.error;
        if (error != null)
            SkriptLogger.log(error);
        else if (def != null)
            SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, ErrorQuality.SEMANTIC_ERROR, def));
        notPrinted();
    }

    public final void printError(final String def, final ErrorQuality quality) {
        printedErrorOrLog = true;
        stop();
        final LogEntry error = this.error;
        if (error != null && error.quality >= quality.quality())
            SkriptLogger.log(error);
        else
            SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, quality, def));
        notPrinted();
    }

    public int getNumErrors() {
        return error == null ? 0 : 1;
    }

    public final boolean hasError() {
        return error != null;
    }

    @Nullable
    public final LogEntry getError() {
        return error;
    }

}
