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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffExec;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import java.io.Serializable;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author TheDGOfficial
 */
@Name("Last Execute State/Last Execute Errors")
@Description({"Represents the last execute state or errors."})
@Examples({"command /eval <text>:", "\tdescription: Evaluates the given effect.", "\tusage: /eval <effect>", "\texecutable by: players", "\ttrigger:", "\t\texecute arg-1 if the player has permission \"skript.eval\"", "\t\tsend last execute errors to player if last execute state is false"})
@Since("2.2-Fixes-V10c")
public class ExprLastExecuteInfo extends SimpleExpression<Object> {
	static {
		Skript.registerExpression(ExprLastExecuteInfo.class, Object.class, ExpressionType.SIMPLE, "[the] last (0¦execute state|1¦execute errors)");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		mark = parseResult.mark;
		return true;
	}
	
	@Override
	@Nullable
	protected Object[] get(final Event e) {
		switch (mark) {
			case 0:
				return new Boolean[] {EffExec.lastExecuteState};
			case 1:
				return new String[] {EffExec.lastExecuteErrors};
		}
		return null;
	}
	
	private int mark;
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Nullable
	private String getExpressionName() {
		switch (mark) {
			case 0:
				return "last execute state";
			case 1:
				return "last execute errors";
		}
		return null;
	}
	
	@Override
	public Class<? extends Serializable> getReturnType() {
		return mark == 0 ? Boolean.class : String.class;
	}
	
	public String toString(@Nullable final Event e, final boolean debug) {
		return "the " + getExpressionName();
	}
}
