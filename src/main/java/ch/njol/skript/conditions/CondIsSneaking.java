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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;

import org.bukkit.entity.Player;

/**
 * @author Peter Güttinger
 */
@Name("Is Sneaking")
@Description("Checks whether a player is sneaking")
@Examples({"# prevent mobs from seeing sneaking players if they are at least 4 meters apart", "on target:", "	target is sneaking", "	distance of target and the entity is bigger than 4", "	cancel the event"})
@Since("1.4.4")
public class CondIsSneaking extends PropertyCondition<Player> {
	
	static {
		register(CondIsSneaking.class, "sneaking", "players");
	}
	
	@Override
	public boolean check(final Player p) {
		return p.isSneaking();
	}
	
	@Override
	protected String getPropertyName() {
		return "sneaking";
	}
	
}
