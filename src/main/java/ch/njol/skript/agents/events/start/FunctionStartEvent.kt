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

@file:JvmName("FunctionStartEvent")

package ch.njol.skript.agents.events.start

import ch.njol.skript.agents.AgentEvent
import ch.njol.skript.lang.function.Function

class FunctionStartEvent(
        /**
         * The function that started executing.
         */
        @JvmField val function: Function<*>,

        /**
         * The arguments used to execute function.
         */
        @JvmField val arguments: Array<Array<Any>>
) : AgentEvent()
