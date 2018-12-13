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
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.SingleItemIterator;

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
@Name("Id")
@Description("The id of a specific item. You usually don't need this expression as you can likely do everything with aliases.")
@Examples({"message \"the ID of %type of the clicked block% is %id of the clicked block%.\""})
@Since("1.0")
public class ExprIdOf extends PropertyExpression<ItemType, Integer> {
	static {
		Skript.registerExpression(ExprIdOf.class, Integer.class, ExpressionType.PROPERTY, "[the] id(1¦s|) of %itemtype%", "%itemtype%'[s] id(1¦s|)");
	}
	
	private boolean single;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<ItemType>) vars[0]);
		if (parser.mark != 1) {
			single = true;
			if (!getExpr().isSingle() || getExpr() instanceof Literal && ((Literal<ItemType>) getExpr()).getSingle().getTypes().size() != 1) {
				Skript.warning("'" + getExpr() + "' has multiple ids");
				single = false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected Integer[] get(final Event e, final ItemType[] source) {
		if (single) {
			final ItemType t = getExpr().getSingle(e);
			if (t == null)
				return new Integer[0];
			return new Integer[] {t.getTypes().get(0).getId()};
		}
		final ArrayList<Integer> r = new ArrayList<Integer>();
		for (final ItemType t : source) {
			for (final ItemData d : t) {
				r.add(d.getId());
			}
		}
		return r.toArray(new Integer[0]);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the id" + (single ? "" : "s") + " of " + getExpr().toString(e, debug);
	}
	
	boolean changeItemStack;
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (!getExpr().isSingle())
			return null;
		if (!ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, ItemStack.class, ItemType.class))
			return null;
		changeItemStack = ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, ItemStack.class);
		switch (mode) {
			case ADD:
			case REMOVE:
			case SET:
				return new Class[] {Number.class};
			case RESET:
			case DELETE:
			case REMOVE_ALL:
			default:
				return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert delta != null;
		final ItemType it = getExpr().getSingle(e);
		if (it == null)
			return;
		final ItemStack is = it.getRandom();
		if (is == null)
			return;
		int type = is.getTypeId();
		final int i = ((Number) delta[0]).intValue();
		switch (mode) {
			case ADD:
				type += i;
				break;
			case REMOVE:
				type -= i;
				break;
			case SET:
				type = i;
				break;
			case RESET:
			case DELETE:
			case REMOVE_ALL:
			default:
				assert false;
				return;
		}
		final Material m = Material.getMaterial(type);
		if (m != null) {
			is.setType(m);
			if (changeItemStack)
				getExpr().change(e, new ItemStack[] {is}, ChangeMode.SET);
			else
				getExpr().change(e, new ItemType[] {new ItemType(is)}, ChangeMode.SET);
		}
	}
	
	@Override
	@Nullable
	public Iterator<Integer> iterator(final Event e) {
		if (single) {
			final ItemType t = getExpr().getSingle(e);
			if (t == null)
				return null;
			if (t.numTypes() == 0)
				return null;
			return new SingleItemIterator<Integer>(t.getTypes().get(0).getId());
		}
		final Iterator<? extends ItemType> iter = getExpr().iterator(e);
		if (iter == null || !iter.hasNext())
			return null;
		return new Iterator<Integer>() {
			private Iterator<ItemData> current = iter.next().iterator();
			
			@Override
			public boolean hasNext() {
				while (iter.hasNext() && !current.hasNext()) {
					current = iter.next().iterator();
				}
				return current.hasNext();
			}
			
			@Override
			public Integer next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return current.next().getId();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return "id".equalsIgnoreCase(s);
	}
	
}
