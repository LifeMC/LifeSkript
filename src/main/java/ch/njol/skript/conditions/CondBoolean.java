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

package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Boolean")
@Description("Checks a boolean value.")
@Examples("if true: # always true")
@Since("2.2-Fixes-V11")
/**
 * @author Peter Güttinger
 */
public class CondBoolean extends Condition {
	
	static {
		Skript.registerCondition(CondBoolean.class, "%booleans%");
	}
	
	@SuppressWarnings("null")
	private Expression<Boolean> condition;
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		condition = (Expression<Boolean>) exprs[0];
		return true;
	}
	
	@Override
	public boolean check(final Event event) {
		return condition.check(event, new Checker<Boolean>() {
			@Override
			public boolean check(final Boolean object) {
				return object;
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		return "boolean condition";
	}
	
}
