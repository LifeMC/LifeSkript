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

package ch.njol.skript.events.bukkit;

import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class ScheduledEvent extends Event {
    // Bukkit stuff
    private static final HandlerList handlers = new HandlerList();

    static {
        EventValues.registerEventValue(ScheduledEvent.class, World.class, new Getter<World, ScheduledEvent>() {
            @Override
            @Nullable
            public final World get(final ScheduledEvent e) {
                return e.getWorld();
            }
        }, 0, "There's no world in a periodic event if no world is given in the event (e.g. like 'every hour in \"world\"')", ScheduledNoWorldEvent.class);
    }

    @Nullable
    private final World world;

    public ScheduledEvent(@Nullable final World world) {
        this.world = world;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Nullable
    public final World getWorld() {
        return world;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
