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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.InventorySlot;
import ch.njol.skript.util.Slot;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Items In")
@Description({"All items in an inventory. Useful for looping or storing in a list variable.", "Please note that the positions of the items in the inventory are not saved, only their order is preserved."})
@Examples({"loop all items in the player's inventory:", "	loop-item is enchanted", "	remove loop-item from the player", "set {inventory.%player%} to items in the player's inventory"})
@Since("2.0")
public class ExprItemsIn extends SimpleExpression<Slot> {
	static {
		Skript.registerExpression(ExprItemsIn.class, Slot.class, ExpressionType.PROPERTY, "[all] items (in|of|contained in|out of) (|1¦inventor(y|ies)) %inventories%");
	}
	
	@SuppressWarnings("null")
	private Expression<Inventory> invis;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		invis = (Expression<Inventory>) exprs[0];
		if (invis instanceof Variable && !invis.isSingle() && parseResult.mark != 1)
			Skript.warning("'items in {variable::*}' does not actually represent the items stored in the variable. Use either '{variable::*}' (e.g. 'loop {variable::*}') if the variable contains items, or 'items in inventories {variable::*}' if the variable contains inventories.");
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected Slot[] get(final Event e) {
		final ArrayList<Slot> r = new ArrayList<Slot>();
		for (final Inventory invi : invis.getArray(e)) {
			for (int i = 0; i < invi.getSize(); i++) {
				if (invi.getItem(i) != null)
					r.add(new InventorySlot(invi, i));
			}
		}
		return r.toArray(new Slot[0]);
	}
	
	@Override
	@Nullable
	public Iterator<Slot> iterator(final Event e) {
		final Iterator<? extends Inventory> is = invis.iterator(e);
		if (is == null || !is.hasNext())
			return null;
		return new Iterator<Slot>() {
			@SuppressWarnings("null")
			Inventory current = is.next();
			
			int next;
			
			@SuppressWarnings("null")
			@Override
			public boolean hasNext() {
				while (next < current.getSize() && current.getItem(next) == null)
					next++;
				while (next >= current.getSize() && is.hasNext()) {
					current = is.next();
					next = 0;
					while (next < current.getSize() && current.getItem(next) == null)
						next++;
				}
				return next < current.getSize();
			}
			
			@Override
			public Slot next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return new InventorySlot(current, next++);
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return "item".equalsIgnoreCase(s);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "items in " + invis.toString(e, debug);
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}
	
}
