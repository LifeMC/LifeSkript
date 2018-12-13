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
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Level Progress")
@Description({"The progress a player has made until the next level. Remember that this value is between 0 and 1, not 0 and 100!", "Changing this value can cause a player's level to change if the resulting level progess is negative or larger than 1, e.g." + "<code>increase the player's level progress by 0.5</code> will make the player gain a level if his progress was more than 50%."})
@Examples({"# use the exp bar as mana", "on rightclick with a blaze rod:", "	player's level progress is larger than 0.2", "	shoot a fireball from the player", "	reduce the player's level progress by 0.2", "every 2 seconds:", "	loop all players:", "		level progress of loop-player is smaller than 0.9:", "			increase level progress of the loop-player by 0.1", "		else:", "			set level progress of the loop-player to 0.99", "on xp spawn:", "	cancel event"})
@Since("2.0")
@Events("level change")
public class ExprLevelProgress extends SimplePropertyExpression<Player, Float> {
	static {
		register(ExprLevelProgress.class, Float.class, "level progress", "players");
	}
	
	@Override
	public Float convert(final Player p) {
		return p.getExp();
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return new Class[] {Number.class};
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert mode != ChangeMode.REMOVE_ALL;
		
		final float d = delta == null ? 0 : ((Number) delta[0]).floatValue();
		for (final Player p : getExpr().getArray(e)) {
			final float c;
			switch (mode) {
				case SET:
					c = d;
					break;
				case ADD:
					c = p.getExp() + d;
					break;
				case REMOVE:
					c = p.getExp() - d;
					break;
				case DELETE:
				case RESET:
					c = 0;
					break;
				case REMOVE_ALL:
				default:
					assert false;
					return;
			}
			p.setLevel(Math.max(0, p.getLevel() + (int) Math.floor(c)));
			p.setExp(Math2.mod(c, 1));
		}
	}
	
	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "level progress";
	}
	
}
