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

@file:JvmName("LoopTrackerAgent")

package ch.njol.skript.agents.defaults

import ch.njol.skript.Skript
import ch.njol.skript.agents.SkriptAgent
import ch.njol.skript.agents.TrackerAgent
import ch.njol.skript.agents.events.end.ForLoopEndEvent
import ch.njol.skript.agents.events.start.ForLoopStartEvent
import ch.njol.skript.agents.registerAgent
import ch.njol.skript.agents.unregisterAgent
import org.bukkit.command.CommandSender
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class LoopTrackerAgent(
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
    override fun registerTracker(): LoopTrackerAgent {
        assert(agent == null)
        agent = registerAgent(Skript.getAddonInstance(), Consumer { event ->
            when (event) {
                is ForLoopEndEvent -> out.sendMessage(Skript.SKRIPT_PREFIX.replace("Skript", "Skript Tracker") + "Looping \"" + event.times + "\" times took " + TimeUnit.NANOSECONDS.toMillis(event.endTime - event.startTime) + " ms to complete.")
                is ForLoopStartEvent -> out.sendMessage(Skript.SKRIPT_PREFIX.replace("Skript", "Skript Tracker") + "Looping \"" + event.times + "\" times now...")
                else -> assert(false) { event.javaClass.name }
            }
        }, ForLoopStartEvent::class.java, ForLoopEndEvent::class.java)
        return this
    }

    /**
     * Unregisters this tracker.
     *
     * @return This tracker, useful
     * for chaining.
     */
    override fun unregisterTracker(): LoopTrackerAgent {
        assert(agent != null)
        unregisterAgent(agent)
        agent = null
        return this
    }
}
