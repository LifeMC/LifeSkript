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

package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Message")
@Description("Sends a message to the given player.")
@Examples({"message \"A wild %player% appeared!\"", "message \"This message is a distraction. Mwahaha!\"", "send \"Your kill streak is %{kill streak.%player%}%.\" to player", "if the targeted entity exists:", "	message \"You're currently looking at a %type of the targeted entity%!\""})
@Since("1.0")
public final class EffMessage extends Effect {
	static {
		Skript.registerEffect(EffMessage.class, "(message|msg|tell|send [message]) %strings% [to %commandsenders%]");
	}
	
	@SuppressWarnings("null")
	private Expression<String> messages;
	@SuppressWarnings("null")
	private Expression<CommandSender> recipients;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		messages = (Expression<String>) exprs[0];
		recipients = (Expression<CommandSender>) exprs[1];
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (final String message : messages.getArray(e)) {
//			message = StringUtils.fixCapitalization(message);
			for (final CommandSender s : recipients.getArray(e)) {
				s.sendMessage(message);
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "send " + messages.toString(e, debug) + " to " + recipients.toString(e, debug);
	}
}
