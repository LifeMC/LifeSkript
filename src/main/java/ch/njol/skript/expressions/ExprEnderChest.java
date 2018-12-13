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

package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Ender Chest")
@Description("The ender chest of a player")
@Examples("open the player's ender chest to the player")
@Since("2.0")
public class ExprEnderChest extends SimplePropertyExpression<Player, Inventory> {
	static {
		register(ExprEnderChest.class, Inventory.class, "ender[ ]chest[s]", "players");
	}
	
	@Override
	@Nullable
	public Inventory convert(final Player p) {
		return p.getEnderChest();
	}
	
	@Override
	public Class<Inventory> getReturnType() {
		return Inventory.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "ender chest";
	}
	
}
