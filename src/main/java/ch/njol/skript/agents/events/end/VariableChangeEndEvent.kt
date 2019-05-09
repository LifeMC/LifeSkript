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

@file:JvmName("VariableChangeEndEvent")
package ch.njol.skript.agents.events.end

import ch.njol.skript.agents.AgentEvent
import ch.njol.skript.lang.Variable

/**
 * Occurs when a variable change effect is
 * ended, and thus variable is changed.
 *
 * @since 2.2-V13b
 */
data class VariableChangeEndEvent(
    /**
     * The variable being changed.
     */
    @JvmField val variable: Variable<*>,
	/**
	 * The new value of the variable.
	 */
	@JvmField val newValue: Array<Any?>?
) : AgentEvent() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VariableChangeEndEvent

        if (variable != other.variable) return false
        if (newValue != null) {
            if (other.newValue == null) return false
            if (!newValue.contentEquals(other.newValue)) return false
        } else if (other.newValue != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = variable.hashCode()
        result = 31 * result + (newValue?.contentHashCode() ?: 0)
        return result
    }
}
