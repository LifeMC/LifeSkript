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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.NullableChecker;
import ch.njol.util.coll.iterator.ArrayIterator;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.IteratorIterable;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Items")
@Description("Items or blocks of a specific type, useful for looping.")
@Examples({"loop items of type ore and log:", "	block contains loop-item", "	message \"Theres at least one %loop-item% in this block\"", "drop all blocks at the player # drops one of every block at the player"})
@Since("")
public class ExprItems extends SimpleExpression<ItemStack> {
	
	static {
		Skript.registerExpression(ExprItems.class, ItemStack.class, ExpressionType.COMBINED, "[(all|every)] item(s|[ ]types)", "items of type[s] %itemtypes%", "[(all|every)] block(s|[ ]types)", "blocks of type[s] %itemtypes%");
	}
	
	@Nullable
	Expression<ItemType> types;
	private boolean blocks;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		if (vars.length > 0)
			types = (Expression<ItemType>) vars[0];
		blocks = matchedPattern >= 2;
		if (types instanceof Literal) {
			for (final ItemType t : ((Literal<ItemType>) types).getAll())
				t.setAll(true);
		}
		return true;
	}
	
	@Nullable
	private ItemStack[] buffer;
	
	@SuppressWarnings("null")
	@Override
	protected ItemStack[] get(final Event e) {
		if (buffer != null)
			return buffer;
		final ArrayList<ItemStack> r = new ArrayList<ItemStack>();
		for (final ItemStack is : new IteratorIterable<ItemStack>(iterator(e)))
			r.add(is);
		if (types instanceof Literal)
			return buffer = r.toArray(new ItemStack[0]);
		return r.toArray(new ItemStack[0]);
	}
	
	@Override
	@Nullable
	public Iterator<ItemStack> iterator(final Event e) {
		Iterator<ItemStack> iter;
		if (types == null) {
			iter = new Iterator<ItemStack>() {
				
				private final Iterator<Material> iterator = new ArrayIterator<Material>(Material.values());
				
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}
				
				@Override
				public ItemStack next() {
					return new ItemStack(iterator.next());
				}
				
				@Override
				public void remove() {}
				
			};
		} else {
			@SuppressWarnings("null")
			final Iterator<ItemType> it = new ArrayIterator<ItemType>(types.getArray(e));
			if (!it.hasNext())
				return null;
			iter = new Iterator<ItemStack>() {
				
				@SuppressWarnings("null")
				Iterator<ItemStack> current = it.next().getAll().iterator();
				
				@SuppressWarnings("null")
				@Override
				public boolean hasNext() {
					while (!current.hasNext() && it.hasNext()) {
						current = it.next().getAll().iterator();
					}
					return current.hasNext();
				}
				
				@SuppressWarnings("null")
				@Override
				public ItemStack next() {
					if (!hasNext())
						throw new NoSuchElementException();
					return current.next();
				}
				
				@Override
				public void remove() {}
				
			};
		}
		
		if (!blocks)
			return iter;
		
		return new CheckedIterator<ItemStack>(iter, new NullableChecker<ItemStack>() {
			@SuppressWarnings("deprecation")
			@Override
			public boolean check(final @Nullable ItemStack is) {
				return is != null && is.getTypeId() <= Skript.MAXBLOCKID;
			}
		});
	}
	
	@Override
	public Class<ItemStack> getReturnType() {
		return ItemStack.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final Expression<ItemType> types = this.types;
		return (blocks ? "blocks" : "items") + (types != null ? " of type" + (types.isSingle() ? "" : "s") + " " + types.toString(e, debug) : "");
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return blocks && "block".equalsIgnoreCase(s) || !blocks && "item".equalsIgnoreCase(s);
	}
	
}
