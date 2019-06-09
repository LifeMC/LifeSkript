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

package ch.njol.skript.hooks.regions.events;

import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.hooks.regions.classes.Region;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Peter Güttinger
 */
@RequiredPlugins("A region plugin")
public final class RegionBorderEvent extends Event implements Cancellable {

    // Bukkit stuff
    private static final HandlerList handlers = new HandlerList();
    final Player player;
    private final Region region;
    private final boolean enter;
    private boolean cancelled;

    @Deprecated
    public RegionBorderEvent(final Region region, final Player player, final boolean enter) {
        this(!Bukkit.isPrimaryThread(), region, player, enter);
    }

    public RegionBorderEvent(final boolean async, final Region region, final Player player, final boolean enter) {
        super(async);
        this.region = region;
        this.player = player;
        this.enter = enter;
    }

    public static final HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isEntering() {
        return enter;
    }

    public Region getRegion() {
        return region;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
