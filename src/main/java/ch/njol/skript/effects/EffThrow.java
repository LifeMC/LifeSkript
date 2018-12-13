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

package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

@Name("Custom Warn / Error")
@Description("Throws a custom warning / error.")
@Examples({"on load:", "\tset {id} to random uuid", "\tif {id} is not set:", "\t\tthrow new error \"Failed to set ID, please reload!\"", "\t\tstop # Throw does not stops execution, you must add stop!"})
@Since("2.2-Fixes-V10c")
public class EffThrow extends Effect {
	static {
		Skript.registerEffect(EffThrow.class, "throw [a] [new] (0¦warning|1¦error) %string%");
	}
	
	private boolean error;
	
	@SuppressWarnings("null")
	private Expression<?> detail;
	
	@Override
	@SuppressWarnings("null")
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		error = parseResult.mark > 0;
		detail = exprs[0];
		return true;
	}
	
	private String getTypeName() {
		return error ? "error" : "warning";
	}
	
	@Override
	@SuppressWarnings("null")
	protected void execute(final Event e) {
		if (error) {
			Skript.error(String.valueOf(detail.getSingle(e)));
		} else {
			Skript.warning(String.valueOf(detail.getSingle(e)));
		}
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return "throw new " + getTypeName();
	}
}
