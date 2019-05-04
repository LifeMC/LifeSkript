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

@file:JvmName("TaskTrackerAgent")
package ch.njol.skript.agents.defaults

import ch.njol.skript.Skript
import ch.njol.skript.Skript.SKRIPT_PREFIX
import ch.njol.skript.agents.*
import ch.njol.skript.agents.events.end.DelayEndEvent
import ch.njol.skript.agents.events.start.DelayStartEvent
import org.bukkit.command.CommandSender
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * A tracker & debugger agent for the [ch.njol.skript.effects.Delay] class.
 *
 * This class is on top of the most timings results, and caused by wait statements,
 * it submits tasks to bukkit scheduler.
 *
 * @since 2.2-V13b
 */
data class TaskTrackerAgent

/**
 * Creates a new TaskTrackerAgent for given out.
 *
 * @param out The out, we report statistics to it.
 * @param limit The minimum limit that as a long time.
 * @param unit The unit of the limit parameter.
 */
@JvmOverloads constructor(
        /**
         * The out, we report statistics to it.
         */
        @JvmField val out: CommandSender,
        /**
         * The minimum limit and its unit that
         * considered as a long time.
         */
        @JvmField val limit: Long, @JvmField val unit: TimeUnit = TimeUnit.SECONDS
) : TrackerAgent {

    /**
     * The agent
     */
    @JvmField var agent: SkriptAgent? = null

    /**
     * Creates a new TaskTrackerAgent for given out.
     *
     * @param out The out, we report statistics to it.
     */
    constructor(out: CommandSender) : this(out, 0L, TimeUnit.NANOSECONDS)

    init {
        // Sanity checks
        @Suppress("SENSELESS_COMPARISON")
        assert(out != null)
        assert(limit >= 0L)
        @Suppress("SENSELESS_COMPARISON")
        assert(unit != null)
    }

    /**
     * Registers this tracker.
     *
     * @return This tracker, useful
     * for chaining.
     */
    override fun registerTracker(): TaskTrackerAgent {
        assert(agent == null)
        agent = registerAgent(Skript.getAddonInstance(), Consumer<AgentEvent> { event ->
            if (event is DelayEndEvent) {
                if (event.endTime - event.startTime > unit.toNanos(limit))
                    out.sendMessage(SKRIPT_PREFIX.replace("Skript", "Skript Tracker") + "Waited for " + event.duration.milliSeconds + " ms, after that ran a task which is completed in " + TimeUnit.NANOSECONDS.toMillis(event.endTime - event.startTime) + " ms.")
            } else if (event is DelayStartEvent) {
                out.sendMessage(SKRIPT_PREFIX.replace("Skript", "Skript Tracker") + "Waiting for " + event.duration.milliSeconds + " milliseconds..")
            } else
                assert(false) { event.javaClass.name }
        }, DelayStartEvent::class.java, DelayEndEvent::class.java)
        return this
    }

    /**
     * Unregisters this tracker.
     *
     * @return This tracker, useful
     * for chaining.
     */
    override fun unregisterTracker(): TaskTrackerAgent {
        assert(agent != null)
        unregisterAgent(agent)
        agent = null
        return this
    }

}
