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
import ch.njol.skript.events.bukkit.ScheduledEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.EmptyArrays;
import ch.njol.skript.util.Time;
import ch.njol.util.Math2;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author Peter Güttinger
 */
@SuppressFBWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
public final class EvtAtTime extends SelfRegisteringSkriptEvent implements Comparable<EvtAtTime> {
    private static final HashMap<World, EvtAtInfo> triggers = new HashMap<>();
    private static final int CHECKPERIOD = 10;
    private static int taskID = -1;

    static {
        Skript.registerEvent("*At Time", EvtAtTime.class, ScheduledEvent.class, "at %time% [in %worlds%]").description("An event that occurs at a given <a href='../classes/#time'>minecraft time</a> in every world or only in specific worlds.").examples("at 18:00", "at 7am in \"world\"").since("1.3.4");
    }

    private int tick;
    @Nullable
    private Trigger t;
    @SuppressWarnings("null")
    private transient World[] worlds;

    @SuppressWarnings("null")
    private static final void registerListener() {
        if (taskID != -1)
            return;
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), () -> {
            for (final Entry<World, EvtAtInfo> e : triggers.entrySet()) {
                final EvtAtInfo i = e.getValue();
                final int tick = (int) e.getKey().getTime();
                if (i.lastTick == tick) // stupid Bukkit scheduler
                    continue;
                if (i.lastTick + (CHECKPERIOD << 1) < tick || i.lastTick > tick && i.lastTick - 24000 + (CHECKPERIOD << 1) < tick) { // time changed, e.g. by a command or plugin
                    i.lastTick = Math2.mod(tick - CHECKPERIOD, 24000);
                }
                final boolean midnight = i.lastTick > tick; // actually 6:00
                if (midnight)
                    i.lastTick -= 24000;
                final int startIndex = i.currentIndex;
                while (Skript.isSkriptRunning()) {
                    final EvtAtTime next = i.list.get(i.currentIndex);
                    final int nextTick = midnight && next.tick > 12000 ? next.tick - 24000 : next.tick;
                    if (i.lastTick < nextTick && nextTick <= tick) {
                        next.execute(e.getKey());
                        i.currentIndex++;
                        if (i.currentIndex == i.list.size())
                            i.currentIndex = 0;
                        if (i.currentIndex == startIndex) // all events executed at once
                            break;
                    } else {
                        break;
                    }
                }
                i.lastTick = tick;
            }
        }, 0, CHECKPERIOD);
    }

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public final boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
        tick = ((Literal<Time>) args[0]).getSingle().getTicks();
        worlds = args[1] == null ? Bukkit.getWorlds().toArray(EmptyArrays.EMPTY_WORLD_ARRAY) : ((Literal<World>) args[1]).getAll();
        return true;
    }

    private final void execute(final World w) {
        final Trigger t = this.t;
        if (t == null) {
            assert false;
            return;
        }
        final ScheduledEvent e = new ScheduledEvent(w);
        SkriptEventHandler.logEventStart(e);
        SkriptEventHandler.logTriggerEnd(t);
        t.execute(e);
        SkriptEventHandler.logTriggerEnd(t);
        SkriptEventHandler.logEventEnd();
    }

    @Override
    public void register(final Trigger t) {
        this.t = t;
        for (final World w : worlds) {
            EvtAtInfo i = triggers.get(w);
            if (i == null) {
                triggers.put(w, i = new EvtAtInfo());
                i.lastTick = (int) w.getTime() - 1;
            }
            i.list.add(this);
            Collections.sort(i.list);
        }
        registerListener();
    }

    @Override
    public final void unregister(final Trigger t) {
        assert t == this.t;
        this.t = null;
        final Iterator<EvtAtInfo> iter = triggers.values().iterator();
        while (iter.hasNext()) {
            final EvtAtInfo i = iter.next();
            i.list.remove(this);
            if (i.currentIndex >= i.list.size())
                i.currentIndex--;
            if (i.list.isEmpty())
                iter.remove();
        }
        if (triggers.isEmpty())
            unregisterAll();
    }

    @Override
    public final void unregisterAll() {
        if (taskID != -1)
            Bukkit.getScheduler().cancelTask(taskID);
        t = null;
        taskID = -1;
        triggers.clear();
    }

    @Override
    public final String toString(@Nullable final Event e, final boolean debug) {
        return "at " + Time.toString(tick) + " in worlds " + Classes.toString(worlds, true);
    }

    @Override
    public final int compareTo(@Nullable final EvtAtTime e) {
        return e == null ? tick : tick - e.tick;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + tick;
        return result;
    }

    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof EvtAtTime))
            return false;
        final EvtAtTime other = (EvtAtTime) obj;
        return compareTo(other) == 0;
    }

    private static final class EvtAtInfo {
        final ArrayList<EvtAtTime> list = new ArrayList<>();

        int lastTick; // as Bukkit's scheduler is inconsistent this saves the exact tick when the events were last checked
        int currentIndex;

        EvtAtInfo() {
            /* implicit super call */
        }
    }

}
