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

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

/**
 * @author Peter Güttinger
 */
public final class ScriptCommandEvent extends CommandEvent {

    // Bukkit stuff
    private static final HandlerList handlers = new HandlerList();
    private final ScriptCommand skriptCommand;
    private boolean cooldownCancelled;

    public ScriptCommandEvent(final ScriptCommand command, final CommandSender sender) {
        super(sender, command.getLabel(), null);
        skriptCommand = command;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ScriptCommand getSkriptCommand() {
        return skriptCommand;
    }

    @Override
    public String[] getArgs() {
        throw new UnsupportedOperationException();
    }

    public boolean isCooldownCancelled() {
        return cooldownCancelled;
    }

    public void setCooldownCancelled(final boolean cooldownCancelled) {
        this.cooldownCancelled = cooldownCancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ScriptCommandEvent that = (ScriptCommandEvent) o;

        return Objects.equals(skriptCommand, that.skriptCommand);
    }

    @SuppressWarnings("null")
	@Override
    public int hashCode() {
        return skriptCommand != null ? skriptCommand.hashCode() : 0;
    }
}
