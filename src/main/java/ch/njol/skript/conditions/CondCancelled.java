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
import ch.njol.util.Kleenean;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Event Cancelled")
@Description("Checks whether or not the event is cancelled")
@Examples({"on click:", "\tif event is cancelled:", "\t\tbroadcast \"no clicks allowed!\""
})
@Since("2.2-Fixes-V9c")
public final class CondCancelled extends Condition {
	
	static {
		Skript.registerCondition(CondCancelled.class, "[the] event is cancel[l]ed", "[the] event (is not|isn't) cancel[l]ed");
	}
	
	@Override
	public boolean check(final Event e) {
		return (e instanceof Cancellable && ((Cancellable) e).isCancelled()) ^ isNegated();
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return isNegated() ? "event is not cancelled" : "event is cancelled";
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		setNegated(matchedPattern == 1);
		return true;
	}
	
}
