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
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

@Name("Do If")
@Description("Execute an effect if a condition is true.")
@Examples({"on join:", "\tgive a diamond to the player if the player has permission \"rank.vip\""})
@Since("2.2-Fixes-V10b")
public class EffDoIf extends Effect {
	
	static {
		Skript.registerEffect(EffDoIf.class, "<.+> if <.+>");
	}
	
	@SuppressWarnings("null")
	private Effect effect;
	
	@SuppressWarnings("null")
	private Condition condition;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		final String eff = parseResult.regexes.get(0).group();
		effect = Effect.parse(eff, "Can't understand this effect: " + eff);
		if (effect instanceof EffDoIf) {
			Skript.error("Do if effects may not be nested!");
			return false;
		}
		final String cond = parseResult.regexes.get(1).group();
		condition = Condition.parse(cond, "Can't understand this condition: " + cond);
		return effect != null && condition != null;
	}
	
	@Override
	protected void execute(final Event e) {
		if (condition.check(e))
			effect.run(e);
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return effect.toString(e, debug) + " if " + condition.toString(e, debug);
	}
	
}
