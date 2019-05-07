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

@file:JvmName("FunctionEndEvent")
package ch.njol.skript.agents.events.end

import ch.njol.skript.agents.AgentEvent
import ch.njol.skript.lang.function.Function

data class FunctionEndEvent(
    /**
     * The function ended executing.
     */
    @JvmField val function: Function<*>,

    /**
     * The arguments used to execute function.
     */
    @JvmField val arguments: Array<Array<Any>>,

    /**
     * The start time of the function.
     * Determined by nanoseconds, provided by
     * [java.lang.System.nanoTime].
     */
    @JvmField val startTime: Long,

    /**
     * The end time of the function.
     * Determined by nanoseconds, provided by
     * [java.lang.System.nanoTime].
     */
    @JvmField val endTime: Long
) : AgentEvent() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FunctionEndEvent

        if (function != other.function) return false
        if (!arguments.contentDeepEquals(other.arguments)) return false
        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = function.hashCode()
        result = 31 * result + arguments.contentDeepHashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        return result
    }
}
