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
 *  along with Skript.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Workarounds for Minecraft & Bukkit quirks
 *
 * @author Peter Güttinger
 */
public final class Workarounds {

    static {
        // allows to properly remove a player's tool in right click events
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onInteract(final PlayerInteractEvent e) {
                if (e.hasItem() && (e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getType() == Material.AIR || e.getPlayer().getItemInHand().getAmount() == 0))
                    e.setUseItemInHand(Result.DENY);
            }
        }, Skript.getInstance());
    }

    private Workarounds() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        // Exception Catching
        final Thread.UncaughtExceptionHandler handler = (t, e) -> {
            System.err.println("Uncaught exception in thread \"" + t.getName() + "\"");
            e.printStackTrace();
        };

        // Default Handler
        Thread.setDefaultUncaughtExceptionHandler(handler);

        // Current Thread
        Thread.currentThread().setUncaughtExceptionHandler(handler);

        // Server Thread
        Bukkit.getScheduler().runTask(Skript.getInstance(), () -> Thread.currentThread().setUncaughtExceptionHandler(handler));

        /* System properties */

        // UTF-8 Fixes
        System.setProperty("file.encoding", "UTF-8");

        System.setProperty("sun.jnu.encoding", "UTF-8");

        System.setProperty("sun.stderr.encoding", "UTF-8");
        System.setProperty("sun.stdout.encoding", "UTF-8");

        // Language Fix
        System.setProperty("user.language", "EN");

        // Country Fix
        System.setProperty("user.country", "US");

        // Keep Alive Fix
        System.setProperty("paper.playerconnection.keepalive", "120");

        // LifeSkript
        System.setProperty("using.lifeskript", "true");

        /* System properties */
    }

}
