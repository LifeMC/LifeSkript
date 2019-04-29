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

package ch.njol.skript.agent.defaults;

import ch.njol.skript.Skript;
import ch.njol.skript.agent.SkriptAgent;
import ch.njol.skript.agent.TrackerAgent;
import ch.njol.skript.agent.events.end.DelayEndEvent;
import ch.njol.skript.agent.events.start.DelayStartEvent;
import org.bukkit.command.CommandSender;

import java.util.concurrent.TimeUnit;

import static ch.njol.skript.Skript.SKRIPT_PREFIX;

/**
 * A tracker & debugger agent for the {@link ch.njol.skript.effects.Delay} class.
 *
 * This class is on top of the most timings results, and caused by wait statements,
 * it submits tasks to bukkit scheduler.
 *
 * @since 2.2-V13
 */
public class TaskTrackerAgent implements TrackerAgent {

    /**
     * The agent
     */
    public SkriptAgent agent;

    /**
     * The out, we report statistics to it.
     */
    public final CommandSender out;

    /**
     * The minimum limit and its unit that
     * considered as a long time.
     */
    public final long limit;
    public final TimeUnit unit;

    /**
     * Creates a new TaskTrackerAgent for given out.
     *
     * @param out The out, we report statistics to it.
     */
    public TaskTrackerAgent(final CommandSender out) {
        this(out, 0L, TimeUnit.NANOSECONDS);
    }

    /**
     * Creates a new TaskTrackerAgent for given out.
     *
     * @param out The out, we report statistics to it.
     * @param limit The minimum limit that as a long time.
     */
    public TaskTrackerAgent(final CommandSender out, final long limit) {
        this(out, limit, TimeUnit.SECONDS);
    }

    /**
     * Creates a new TaskTrackerAgent for given out.
     *
     * @param out The out, we report statistics to it.
     * @param limit The minimum limit that as a long time.
     * @param unit The unit of the limit parameter.
     */
    public TaskTrackerAgent(final CommandSender out, final long limit, final TimeUnit unit) {
        // Sanity checks
        assert out != null;
        assert limit >= 0L;
        assert unit != null;

        // Assignments
        this.out = out;
        this.limit = limit;
        this.unit = unit;
    }

    /**
     * Registers this tracker.
     *
     * @return This tracker, useful
     * for chaining.
     */
    @Override
    public TaskTrackerAgent registerTracker() {
        assert agent == null;
        agent = SkriptAgent.registerAgent(Skript.getAddonInstance(), (event) -> {
            assert event instanceof DelayStartEvent || event instanceof DelayEndEvent;
            if (event instanceof DelayEndEvent) {
                final DelayEndEvent e = (DelayEndEvent) event;
                if ((e.endTime - e.startTime) > unit.toNanos(limit))
                    out.sendMessage(SKRIPT_PREFIX.replace("Skript", "Skript Tracker") + "Waited for " + e.duration.getMilliSeconds() + " ms, after that ran a task which is completed in " + TimeUnit.NANOSECONDS.toMillis(e.endTime - e.startTime) + " ms.");
            } else
                out.sendMessage(SKRIPT_PREFIX.replace("Skript", "Skript Tracker") + "Waiting for " + ((DelayStartEvent) event).duration.getMilliSeconds() + " milliseconds..");
        }, DelayStartEvent.class, DelayEndEvent.class);
        return this;
    }

    /**
     * Unregisters this tracker.
     *
     * @return This tracker, useful
     * for chaining.
     */
    @Override
    public TaskTrackerAgent unregisterTracker() {
        assert agent != null;
        SkriptAgent.unregisterAgent(agent);
        agent = null;
        return this;
    }

}
