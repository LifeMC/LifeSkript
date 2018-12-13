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

package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

import org.bukkit.World;
import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("PvP")
@Description("Checks the PvP status of a world.")
@Examples({"PvP is enabled", "PvP is disabled in \"world\""})
@Since("1.3.4")
public class CondPvP extends Condition {
	
	static {
		Skript.registerCondition(CondPvP.class, "(is PvP|PvP is) enabled [in %worlds%]", "(is PvP|PvP is) disabled [in %worlds%]");
	}
	
	@SuppressWarnings("null")
	private Expression<World> worlds;
	boolean enabled;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		enabled = matchedPattern == 0;
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "is PvP " + (enabled ? "enabled" : "disabled") + " in " + worlds.toString(e, debug);
	}
	
	@Override
	public boolean check(final Event e) {
		return worlds.check(e, new Checker<World>() {
			@Override
			public boolean check(final World w) {
				return w.getPVP() == enabled;
			}
		}, isNegated());
	}
	
}
