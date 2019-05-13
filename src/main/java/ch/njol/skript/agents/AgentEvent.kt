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

@file:JvmName("AgentEvent")

package ch.njol.skript.agents

/**
 * Represents a skript debugger event.
 *
 * It extends bukkit's event, but it's not used.
 *
 * @since 2.2-V13b
 */
abstract class AgentEvent {

    /**
     * Triggers before executing the event.
     *
     * @param on The debugger that listens this event.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun beforeExecuting(@Suppress("UNUSED_PARAMETER") on: SkriptAgent) {
        /* empty, overrideable */
    }

    /**
     * Runs this event on a skript debugger agent.
     *
     * Note: First check if agent has a
     * listener for the event.
     *
     * @param on The skript debugger agent.
     */
    fun execute(on: SkriptAgent) {
        beforeExecuting(on)

        assert(on.hasListener(this.javaClass))
        on.handler.accept(this)

        afterExecuting(on)
    }

    /**
     * Triggers after executing the event.
     *
     * @param on The debugger that listens this event.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun afterExecuting(@Suppress("UNUSED_PARAMETER") on: SkriptAgent) {
        /* empty, overrideable */
    }

}
