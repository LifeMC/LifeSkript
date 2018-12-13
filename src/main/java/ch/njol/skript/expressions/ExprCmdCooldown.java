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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

@Name("Cooldown")
@Description({"Only usable in command events. The cooldown of the command"})
@Examples({"send \"The cooldown of this command is: %cooldown%\""})
@Since("2.2-Fixes-V10b")
public class ExprCmdCooldown extends SimpleExpression<Timespan> {
	static {
		Skript.registerExpression(ExprCmdCooldown.class, Timespan.class, ExpressionType.SIMPLE, "[the] cooldown [(of [the] command)]");
	}
	
	@Nullable
	@Override
	protected Timespan[] get(final Event e) {
		if (!(e instanceof ScriptCommandEvent)) {
			return null;
		}
		return new Timespan[] {((ScriptCommandEvent) e).getSkriptCommand().getCooldown()};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<Timespan> getReturnType() {
		return Timespan.class;
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return "the cooldown of the command";
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(ScriptCommandEvent.class)) {
			Skript.error("The expression 'cooldown' can only be used within a command", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}
}
