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

package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.ArrayIterator;
import ch.njol.util.coll.iterator.EmptyIterator;

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Blocks in Region")
@Description({"All blocks in a <a href='../classes/#region'>region</a>.", "This expression requires a supported regions plugin to be installed."})
@Examples({"loop all blocks in the region {arena.%{faction.%player%}%}:", "	clear the loop-block"})
@Since("2.1")
public final class ExprBlocksInRegion extends SimpleExpression<Block> {
	static {
		Skript.registerExpression(ExprBlocksInRegion.class, Block.class, ExpressionType.COMBINED, "[(all|the)] blocks (in|of) [[the] region[s]] %regions%");
	}
	
	@SuppressWarnings("null")
	private Expression<Region> regions;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		regions = (Expression<Region>) exprs[0];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected Block[] get(final Event e) {
		final Iterator<Block> iter = iterator(e);
		final ArrayList<Block> r = new ArrayList<Block>();
		while (iter.hasNext())
			r.add(iter.next());
		return r.toArray(new Block[0]);
	}
	
	@Override
	@NonNull
	public Iterator<Block> iterator(final Event e) {
		final Region[] rs = regions.getArray(e);
		if (rs.length == 0)
			return EmptyIterator.get();
		return new Iterator<Block>() {
			private Iterator<Block> current = rs[0].getBlocks();
			private final Iterator<Region> iter = new ArrayIterator<Region>(rs, 1);
			
			@Override
			public boolean hasNext() {
				while (!current.hasNext() && iter.hasNext()) {
					final Region r = iter.next();
					if (r != null)
						current = r.getBlocks();
				}
				return current.hasNext();
			}
			
			@SuppressWarnings("null")
			@Override
			public Block next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return current.next();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "all blocks in " + regions.toString(e, debug);
	}
	
}
