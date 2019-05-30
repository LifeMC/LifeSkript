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

/**
 * @author Peter Güttinger
 */
public abstract class LogHandler/* implements AutoCloseable */{

    /**
     * @param entry
     * @return Whatever to print the specified entry or not.
     */
    public abstract LogResult log(LogEntry entry);

    /**
     * Called just after the handler is removed from the active handlers stack.
     */
    protected void onStop() {
        /* empty, override-able */
    }

    public final void stop() {
        SkriptLogger.removeHandler(this);
        onStop();
    }

    public final boolean isStopped() {
        return SkriptLogger.isStopped(this);
    }

    public enum LogResult {
        LOG, CACHED, DO_NOT_LOG
    }

    /**
     * Stops the log handler.
     *
     * Useful on Java 7 and above.
     *
     * @see LogHandler#stop()
     */
//    @Override
//    public final void close() {
//        stop();
//    }

}
