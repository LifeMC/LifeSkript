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

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.util.Slot;

import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Item")
@Description("The item involved in an event, e.g. in a drop, dispense, pickup or craft event.")
@Examples({"on dispense:", "	item is a clock", "	set the time to 6:00"/*,"	delete the item"*/})
@Since("")
public class ExprItem extends EventValueExpression<ItemStack> {
	static {
		Skript.registerExpression(ExprItem.class, ItemStack.class, ExpressionType.SIMPLE, "[the] item");
	}
	
	public ExprItem() {
		super(ItemStack.class);
	}
	
	@Nullable
	private EventValueExpression<Item> item;
	@Nullable
	private EventValueExpression<Slot> slot;
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.RESET)
			return null;
		item = new EventValueExpression<Item>(Item.class);
		if (item.init())
			return new Class[] {ItemType.class};
		item = null;
		slot = new EventValueExpression<Slot>(Slot.class);
		if (slot.init())
			return new Class[] {ItemType.class};
		slot = null;
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert mode != ChangeMode.RESET;
		
		final Item i = item != null ? item.getSingle(e) : null;
		final Slot s = slot != null ? slot.getSingle(e) : null;
		if (i == null && s == null)
			return;
		ItemStack is = i != null ? i.getItemStack() : s != null ? s.getItem() : null;
		final ItemType t = delta == null ? null : (ItemType) delta[0];
		switch (mode) {
			case SET:
				assert t != null;
				is = t.getRandom();
				break;
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
				assert t != null;
				if (t.isOfType(is)) {
					if (mode == ChangeMode.ADD)
						is = t.addTo(is);
					else if (mode == ChangeMode.REMOVE)
						is = t.removeFrom(is);
					else
						is = t.removeAll(is);
				}
				break;
			case DELETE:
				is = null;
				if (i != null)
					i.remove();
				break;
			case RESET:
				assert false;
		}
		if (i != null)
			i.setItemStack(is);
		else if (s != null)
			s.setItem(is);
		else
			assert false;
	}
	
}
