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
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;

/**
 * @author Peter Güttinger
 */
@Name("Command Sender")
@Description("The player or the console who sent a command. Mostly useful in <a href='../commands/'>commands</a> and <a href='../events/#command'>command events</a>.")
@Examples({"make the command sender execute \"/say hi!\"",
		"on command:",
		"	log \"%executor% used command /%command% %arguments%\" to \"commands.log\""})
@Since("2.0")
@Events("command")
public class ExprCommandSender extends EventValueExpression<CommandSender> {
	static {
		Skript.registerExpression(ExprCommandSender.class, CommandSender.class, ExpressionType.SIMPLE, "[the] [command['s]] (sender|executor)");
	}
	
	public ExprCommandSender() {
		super(CommandSender.class);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the command sender";
	}
	
}
