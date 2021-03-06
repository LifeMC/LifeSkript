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

package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.events.bukkit.ScheduledEvent;
import ch.njol.skript.events.bukkit.ScheduledNoWorldEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.util.Timespan;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class EvtPeriodical extends SelfRegisteringSkriptEvent {
    static {
        Skript.registerEvent("*Periodical", EvtPeriodical.class, ScheduledNoWorldEvent.class, EvtPeriodical::new, "every %timespan%").description(SkriptEventInfo.NO_DOC);
        Skript.registerEvent("*Periodical", EvtPeriodical.class, ScheduledEvent.class, "every %timespan% in [world[s]] %worlds%").description("An event that is called periodically. The event is used like 'every &lt;<a href='../classes/#timespan'>timespan</a>&gt;', e.g. 'every second' or 'every 5 minutes'.").examples("every second", "every minecraft hour", "every tick # warning: lag!", "every minecraft day in \"world\"").since("1.0");
    }

    @SuppressWarnings("null")
    private Timespan period;

    @Nullable
    private Trigger t;
    @Nullable
    private int[] taskIDs;

    @Nullable
    private transient World[] worlds;

    @SuppressWarnings("unchecked")
    @Override
    public final boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parseResult) {
        period = ((Literal<Timespan>) args[0]).getSingle();
        if (args.length > 1 && args[1] != null) {
            worlds = ((Literal<World>) args[1]).getArray();
        }
        return true;
    }

    private final void execute(@Nullable final World w) {
        final Trigger t = this.t;
        if (t == null) {
            assert false;
            return;
        }
        if (Delay.delayingDisabled)
            return;
        final ScheduledEvent e = w == null ? new ScheduledNoWorldEvent() : new ScheduledEvent(w);
        final boolean flag = Skript.logVeryHigh();
        if (flag) {
            SkriptEventHandler.logEventStart(e);
            SkriptEventHandler.logTriggerStart(t);
        }
        t.execute(e);
        if (flag) {
            SkriptEventHandler.logTriggerEnd(t);
            SkriptEventHandler.logEventEnd();
        }
    }

    @SuppressWarnings("null")
    @Override
    public final void register(final Trigger t) {
        if (Delay.delayingDisabled)
            return;
        this.t = t;
        final int[] taskIDs;
        if (worlds == null) {
            taskIDs = new int[]{Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), () -> execute(null), period.getTicks_i(), period.getTicks_i())};
        } else {
            taskIDs = new int[worlds.length];
            for (int i = 0; i < worlds.length; i++) {
                final World w = worlds[i];
                taskIDs[i] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), () -> execute(w), period.getTicks_i() - w.getFullTime() % period.getTicks_i(), period.getTicks_i());
            }
        }
        this.taskIDs = taskIDs;
    }

    @Override
    public final void unregister(final Trigger t) {
        assert t == this.t;
        this.t = null;
        assert taskIDs != null;
        for (final int taskID : taskIDs)
            Bukkit.getScheduler().cancelTask(taskID);
    }

    @Override
    public final void unregisterAll() {
        t = null;
        assert taskIDs != null;
        for (final int taskID : taskIDs)
            Bukkit.getScheduler().cancelTask(taskID);
    }

    @Override
    public final String toString(@Nullable final Event e, final boolean debug) {
        return "every " + period;
    }

}
