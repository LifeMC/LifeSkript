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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import java.lang.reflect.Array;

import org.eclipse.jdt.annotation.Nullable;

@Name("Ternary")
@Description("A shorthand expression for returning something based on a condition.")
@Examples({"set {points} to 500 if {admin::%player's uuid%} is set else 100"})
@Since("2.2-dev36")
@SuppressWarnings({"null", "unchecked"})
public class ExprTernary<T> extends SimpleExpression<T> {
	
	static {
		Skript.registerExpression(ExprTernary.class, Object.class, ExpressionType.COMBINED, "%objects% if <.+>[,] (otherwise|?[?]|[or ]else) %objects%");
	}
	
	private final ExprTernary<?> source;
	private final Class<T> superType;
	@Nullable
	private Expression<Object> ifTrue;
	@Nullable
	private Condition condition;
	@Nullable
	private Expression<Object> ifFalse;
	
	@SuppressWarnings("unchecked")
	public ExprTernary() {
		this(null, (Class<? extends T>) Object.class);
	}
	
	@SuppressWarnings("unchecked")
	private ExprTernary(final ExprTernary<?> source, final Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.ifTrue = source.ifTrue;
			this.ifFalse = source.ifFalse;
			this.condition = source.condition;
		}
		this.superType = (Class<T>) Utils.getSuperType(types);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		ifTrue = LiteralUtils.defendExpression(exprs[0]);
		ifFalse = LiteralUtils.defendExpression(exprs[1]);
		if (ifFalse instanceof ExprTernary<?> || ifTrue instanceof ExprTernary<?>) {
			Skript.error("Ternary operators may not be nested!");
			return false;
		}
		final String cond = parseResult.regexes.get(0).group();
		condition = Condition.parse(cond, "Can't understand this condition: " + cond);
		return condition != null && LiteralUtils.canInitSafely(ifTrue, ifFalse);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected T[] get(final Event e) {
		final Object[] values = condition.check(e) ? ifTrue.getArray(e) : ifFalse.getArray(e);
		try {
			return Converters.convertStrictly(values, superType);
		} catch (final ClassCastException e1) {
			return (T[]) Array.newInstance(superType, 0);
		}
	}
	
	@Override
	public <R> Expression<? extends R> getConvertedExpression(final Class<R>... to) {
		return new ExprTernary<R>(this, to);
	}
	
	@Override
	public Expression<?> getSource() {
		return source == null ? this : source;
	}
	
	@Override
	public Class<T> getReturnType() {
		return superType;
	}
	
	@Override
	public boolean isSingle() {
		return ifTrue.isSingle() && ifFalse.isSingle();
	}
	
	@Override
	public String toString(final Event e, final boolean debug) {
		return ifTrue.toString(e, debug) + " if " + condition + " otherwise " + ifFalse.toString(e, debug);
	}
	
}
