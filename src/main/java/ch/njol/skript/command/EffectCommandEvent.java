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

package ch.njol.skript.command;

import ch.njol.skript.util.EmptyArrays;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

/**
 * @author Peter Güttinger
 */
public final class EffectCommandEvent extends CommandEvent {

    // Bukkit stuff
    private static final HandlerList handlers = new HandlerList();

    @Deprecated
    public EffectCommandEvent(final CommandSender sender, final String command) {
        this(!Bukkit.isPrimaryThread(), sender, command);
    }

    public EffectCommandEvent(final boolean async, final CommandSender sender, final String command) {
        super(async, sender, command, EmptyArrays.EMPTY_STRING_ARRAY);
    }

    public static final HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
