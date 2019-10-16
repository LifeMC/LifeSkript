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

import ch.njol.util.misc.Tickable;

import java.util.Collection;
import java.util.HashSet;

/**
 * A utility class about {@link Tickable}s.
 *
 * @since 2.2.18
 */
public final class TickUtils {

    private static final Collection<Tickable> tickables = new HashSet<>(100);

    static {
        PlayerUtils.task.run();
    }

    private TickUtils() {
        throw new UnsupportedOperationException("Static class");
    }

    static final void tickAll() {
        for (final Tickable tickable : tickables)
            tickable.tick();
    }

    /**
     * Schedules a tickable to tick every ticks in minecraft.
     *
     * @param tickable The tickable to ran every ticks in minecraft.
     */
    public static final void everyTick(final Tickable tickable) {
        cancel(tickable);

        tickable.tick(); // First tick
        tickables.add(tickable);
    }

    /**
     * Cancels schedule of a tickable, causing it to not tick
     * anymore unless it has been re-added by invoking {@link TickUtils#everyTick(Tickable)}.
     *
     * @param tickable The tickable to cancel schedule of it.
     */
    public static final void cancel(final Tickable tickable) {
        tickables.remove(tickable);
    }

}
