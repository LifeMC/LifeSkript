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

package ch.njol.skript.events.util;

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.EventExecutor;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public final class PlayerChatEventHandler {

    public static final boolean usesAsyncEvent = Skript.classExists("org.bukkit.event.player.AsyncPlayerChatEvent");

    private PlayerChatEventHandler() {
        throw new UnsupportedOperationException();
    }

    public static final void registerChatEvent(final EventPriority priority, final EventExecutor executor, final boolean ignoreCancelled) {
        if (Skript.classExists("org.bukkit.event.player.AsyncPlayerChatEvent"))
            Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, new Listener() {
            	/* empty */
            }, priority, executor, Skript.getInstance(), ignoreCancelled);
        else
            Bukkit.getPluginManager().registerEvent(PlayerChatEvent.class, new Listener() {
            	/* empty */
            }, priority, executor, Skript.getInstance(), ignoreCancelled);
    }

}
