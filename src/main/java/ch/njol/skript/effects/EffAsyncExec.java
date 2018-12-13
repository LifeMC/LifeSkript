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
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Async Execute")
@Description("Execute a effect dynamically and asynchronously in runtime")
@Examples({"command /eval <text>:", "\tdescription: Evaluates the given effect.", "\tusage: /eval <effect>", "\texecutable by: players", "\ttrigger:", "\t\texecute arg-1 async if the player has permission \"skript.eval\""})
@Since("2.2-Fixes-V10c")
public class EffAsyncExec extends AsyncEffect {
	static {
		Skript.registerEffect(EffAsyncExec.class, "(exec[ute]|eval[uate]) %string% async[hronously]");
	}
	
	@SuppressWarnings("null")
	private Expression<String> input;
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		input = (Expression<String>) vars[0];
		return true;
	}
	
	@Override
	@SuppressWarnings("null")
	protected void execute(final Event e) {
		final String s = input.getSingle(e);
		if (s == null)
			return;
		SkriptLogger.startSuppressing();
		final Effect eff = Effect.parse(s, "can't understand this effect: '" + s + "'");
		if (eff instanceof EffExec || eff instanceof EffAsyncExec) {
			Skript.error("Execute effects may not be nested!");
			return;
		}
		final List<LogEntry> entryList = SkriptLogger.stopSuppressing();
		if (eff != null) {
			eff.run(e);
		} else {
			final StringBuilder errorBuilder = new StringBuilder();
			for (final LogEntry entry : entryList) {
				errorBuilder.append(entry.getLevel().getLocalizedName()).append(" ").append(SkriptLogger.format(entry)).append("\n");
			}
			assert errorBuilder != null;
			EffExec.lastExecuteErrors = errorBuilder.toString();
		}
		
		EffExec.lastExecuteState = entryList.isEmpty();
		entryList.clear();
		
		SkriptLogger.cleanSuppressState();
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "exec " + input.toString(e, debug) + " async";
	}
	
}
