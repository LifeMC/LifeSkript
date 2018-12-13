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
import ch.njol.skript.classes.ClassInfo;
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
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.event.Event;

import java.lang.reflect.Array;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Random")
@Description("Gets a random item out of a set, e.g. a random player out of all players online.")
@Examples({"give a diamond to a random player out of all players", "give a random item out of all items to the player"})
@Since("1.4.9")
public final class ExprRandom extends SimpleExpression<Object> {
	static {
		Skript.registerExpression(ExprRandom.class, Object.class, ExpressionType.COMBINED, "[a] random %*classinfo% [out] of %objects%");
	}
	
	@SuppressWarnings("null")
	private Expression<?> expr;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		final Expression<?> expr = exprs[1].getConvertedExpression(((Literal<ClassInfo<?>>) exprs[0]).getSingle().getC());
		if (expr == null)
			return false;
		this.expr = expr;
		return true;
	}
	
	@Override
	protected Object[] get(final Event e) {
		final Object[] set = expr.getAll(e);
		if (set.length <= 1)
			return set;
		final Object[] one = (Object[]) Array.newInstance(set.getClass().getComponentType(), 1);
		one[0] = CollectionUtils.getRandom(set);
		return one;
	}
	
	@Override
	public Class<?> getReturnType() {
		return expr.getReturnType();
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "a random element out of " + expr.toString(e, debug);
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}
