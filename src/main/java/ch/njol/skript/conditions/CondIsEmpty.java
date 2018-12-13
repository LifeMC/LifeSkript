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
import ch.njol.skript.util.Slot;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Peter Güttinger
 */
@Name("Is Empty")
@Description("Checks whether an inventory, an inventory slot, or a text is empty.")
@Examples("player's inventory is empty")
@Since("1.0")
public final class CondIsEmpty extends PropertyCondition<Object> {
	static {
		register(CondIsEmpty.class, "empty", "inventories/slots/strings");
	}
	
	@Override
	public boolean check(final Object o) {
		if (o instanceof String)
			return ((String) o).isEmpty();
		if (o instanceof Inventory)
			return !((Inventory) o).iterator().hasNext();
		if (o instanceof Slot) {
			final Slot s = (Slot) o;
			final ItemStack i = s.getItem();
			return i == null || i.getType() == Material.AIR;
		}
		return false;
	}
	
	@Override
	protected String getPropertyName() {
		return "empty";
	}
	
}
