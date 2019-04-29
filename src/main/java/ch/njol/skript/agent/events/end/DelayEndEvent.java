/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.agent.events.end;

import ch.njol.skript.agent.AgentEvent;
import ch.njol.skript.util.Timespan;

/**
 * Occurs when a next trigger of a delayed event is
 * completed.
 *
 * @since 2.2-V13
 */
public class DelayEndEvent extends AgentEvent {

    /**
     * the duration of the delay
     */
    public final Timespan duration;

    /**
     * start time of the next trigger
     *
     * stored in nanoseconds
     * provided by {@link System#nanoTime()}
     */
    public final long startTime;

    /**
     * end time of the next trigger
     *
     * stored in nanoseconds
     * provided by {@link System#nanoTime()}
     */
    public final long endTime;

    /**
     * only for internal use
     */
    public DelayEndEvent(final Timespan duration, final long startTime, final long endTime) {
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}
