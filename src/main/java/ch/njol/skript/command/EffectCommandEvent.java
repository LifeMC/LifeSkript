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
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2014 Peter Güttinger
 *
 */

package ch.njol.skript.command;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

/**
 * @author Peter Güttinger
 */
public class EffectCommandEvent extends CommandEvent {

    // Bukkit stuff
    private final static HandlerList handlers = new HandlerList();

    public EffectCommandEvent(final CommandSender sender, final String command) {
        super(sender, command, new String[0]);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
