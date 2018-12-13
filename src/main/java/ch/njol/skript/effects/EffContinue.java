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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Loop;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Continue")
@Description("Skips the value currently being looped, moving on to the next value if it exists.")
@Examples("loop all players:\n" + "\tif loop-value does not have permission \"moderator\":\n" + "\t\tcontinue # filter out non moderators\n" + "\tbroadcast \"%loop-player% is a moderator!\" # only moderators get broadcast")
@Since("2.2-Fixes-V10")
public final class EffContinue extends Effect {
	
	static {
		Skript.registerEffect(EffContinue.class, "continue [loop]");
	}
	
	@SuppressWarnings("null")
	private Loop loop;
	
	@Override
	protected void execute(final Event e) {
		throw new UnsupportedOperationException();
	}
	
	@Nullable
	@Override
	protected TriggerItem walk(final Event e) {
		TriggerItem.walk(loop, e);
		return null;
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return "continue";
	}
	
	@Override
	@SuppressWarnings("null")
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		final List<Loop> loops = ScriptLoader.currentLoops;
		if (loops.isEmpty()) {
			Skript.error("Continue may only be used in loops");
			return false;
		}
		loop = loops.get(loops.size() - 1); // the most recent loop
		return true;
	}
	
}
