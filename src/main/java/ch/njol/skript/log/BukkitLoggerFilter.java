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
import ch.njol.util.LoggerFilter;

import java.util.logging.Filter;

/**
 * <ul>
 * <li>The interface Logger and its implementation have the same name
 * <li>In general they duplicate existing code from Java (with the same names), but make it worse
 * <li>You can add filters, but it's impossible to remove them
 * <li>It's a miracle that it somehow even logs messages via Java's default logging system, but usually completely ignores it.
 * <li>Because Level is an enum it's not possible to create your own levels, e.g. DEBUG
 * </ul>
 *
 * @author Peter Güttinger
 */
public final class BukkitLoggerFilter {

    private static final LoggerFilter filter = new LoggerFilter(SkriptLogger.LOGGER);

    static {
        Skript.closeOnDisable(filter);
    }

    private BukkitLoggerFilter() {
        throw new UnsupportedOperationException("Static class");
    }

    /**
     * Adds a filter to Bukkit's log.
     *
     * @param f A filter to filter log messages
     */
    public static final void addFilter(final Filter f) {
        filter.addFilter(f);
    }

    public static final boolean removeFilter(final Filter f) {
        return filter.removeFilter(f);
    }

}
