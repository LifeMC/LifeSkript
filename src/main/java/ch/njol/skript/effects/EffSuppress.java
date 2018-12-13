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
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

@Name("Suppress Warnings / Errors")
@Description({"Suppress warnings or errors.", "Note: Don't forgot the stop suppressing!", "If you forgot, all warnings after effect are suppressed!"})
@Examples({"on load:", "\tstart suppressing warnings", "\tthrow new warning \"never™\"", "\tstop suppressing warnings"})
@Since("2.2-Fixes-V11")
public class EffSuppress extends Effect {
	static {
		Skript.registerEffect(EffSuppress.class, "start [the] (suppressing|hiding|disabling|blocking) [of] [the] (0¦warnings|1¦errors) [because] [of] [due to] [%-strings%]");
		Skript.registerEffect(EffStopSuppress.class, "stop [the] (suppressing|hiding|disabling|blocking) [of] [the] (0¦warnings|1¦errors) [because] [of] [due to] [%-strings%]");
		// the latest optional string part is for the justification, e.g:
		// start suppressing warnings because "It is false positive"
	}
	
	private int mark;
	
	@Override
	@SuppressWarnings("null")
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		mark = parseResult.mark;
		if (mark > 0) {
			SkriptLogger.suppressErrors(true);
		} else {
			SkriptLogger.suppressWarnings(true);
		}
		return true;
	}
	
	private String getTypeName() {
		return mark > 0 ? "errors" : "warnings";
	}
	
	@Override
	@SuppressWarnings("null")
	protected void execute(final Event e) {
		if (mark > 0) {
			SkriptLogger.suppressErrors(true);
		} else {
			SkriptLogger.suppressWarnings(true);
		}
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return "start suppressing " + getTypeName();
	}
	
	@NoDoc
	public static class EffStopSuppress extends Effect {
		
		private int mark;
		
		@Override
		@SuppressWarnings("null")
		public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
			mark = parseResult.mark;
			return true;
		}
		
		private String getTypeName() {
			return mark > 0 ? "errors" : "warnings";
		}
		
		@Override
		@SuppressWarnings("null")
		protected void execute(final Event e) {
			if (mark > 0) {
				SkriptLogger.suppressErrors(false);
			} else {
				SkriptLogger.suppressWarnings(false);
			}
		}
		
		@Override
		public String toString(@Nullable final Event e, final boolean debug) {
			return "stop suppressing " + getTypeName();
		}
		
	}
}
