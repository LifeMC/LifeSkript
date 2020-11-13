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

@file:JvmName("FunctionTrackerAgent")

package ch.njol.skript.agents.defaults

import ch.njol.skript.Skript
import ch.njol.skript.agents.SkriptAgent
import ch.njol.skript.agents.TrackerAgent
import ch.njol.skript.agents.events.end.FunctionEndEvent
import ch.njol.skript.agents.events.start.FunctionStartEvent
import ch.njol.skript.agents.registerAgent
import ch.njol.skript.agents.unregisterAgent
import org.bukkit.command.CommandSender
import java.util.concurrent.TimeUnit

class FunctionTrackerAgent(
        /**
         * The out, we report statistics to it.
         */
        @JvmField val out: CommandSender
) : TrackerAgent {

    /**
     * The skript agent we registered.
     */
    @JvmField
    var agent: SkriptAgent? = null

    init {
        // Sanity checks
        @Suppress("SENSELESS_COMPARISON")
        assert(out != null)
    }

    /**
     * Registers this tracker.
     *
     * @return This tracker, useful
     * for chaining.
     */
    override fun registerTracker(): FunctionTrackerAgent {
        assert(agent == null)
        agent = registerAgent(Skript.getAddonInstance(), { event ->
            when (event) {
                is FunctionEndEvent -> out.sendMessage(Skript.SKRIPT_PREFIX.replace("Skript", "Skript Tracker") + "The function \"" + event.function.name + "\" took " + TimeUnit.NANOSECONDS.toMillis(event.endTime - event.startTime) + " ms to complete.")
                is FunctionStartEvent -> out.sendMessage(Skript.SKRIPT_PREFIX.replace("Skript", "Skript Tracker") + "The function \"" + event.function.name + "\" is running now...")
                else -> assert(false) { event.javaClass.name }
            }
        }, FunctionStartEvent::class.java, FunctionEndEvent::class.java)
        return this
    }

    /**
     * Unregisters this tracker.
     *
     * @return This tracker, useful
     * for chaining.
     */
    override fun unregisterTracker(): FunctionTrackerAgent {
        assert(agent != null)
        unregisterAgent(agent)
        agent = null
        return this
    }
}
