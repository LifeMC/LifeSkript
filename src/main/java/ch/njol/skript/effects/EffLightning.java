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
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

import org.bukkit.Location;
import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Lightning")
@Description("Strike lightning at a given location. Can use 'ligning effect' to create a lightning that does not harm entities or start fires.")
@Examples({"strike lightning at the player", "strike lightning effect at the victim"})
@Since("1.4")
public class EffLightning extends Effect {
	
	static {
		Skript.registerEffect(EffLightning.class, "(create|strike) lightning(1¦[ ]effect|) %directions% %locations%");
	}
	
	@SuppressWarnings("null")
	private Expression<Location> locations;
	
	private boolean effectOnly;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		locations = Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]);
		effectOnly = parseResult.mark == 1;
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (final Location l : locations.getArray(e)) {
			if (effectOnly)
				l.getWorld().strikeLightningEffect(l);
			else
				l.getWorld().strikeLightning(l);
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "strike lightning " + (effectOnly ? "effect " : "") + locations.toString(e, debug);
	}
	
}
