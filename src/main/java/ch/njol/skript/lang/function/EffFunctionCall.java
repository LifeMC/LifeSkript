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

package ch.njol.skript.lang.function;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class EffFunctionCall extends Effect {
	
	private final FunctionReference<?> function;
	
	public EffFunctionCall(final FunctionReference<?> function) {
		this.function = function;
	}
	
	@Nullable
	public static EffFunctionCall parse(final String line) {
		final FunctionReference<?> function = new SkriptParser(line, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseFunction((Class<?>[]) null);
		if (function != null)
			return new EffFunctionCall(function);
		return null;
	}
	
	@Override
	protected void execute(final Event e) {
		function.execute(e);
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return function.toString(e, debug);
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		assert false;
		return false;
	}
	
}
