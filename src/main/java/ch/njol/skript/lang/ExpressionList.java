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

package ch.njol.skript.lang;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.conditions.CondCompare;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * A list of expressions.
 * 
 * @author Peter Güttinger
 */
public class ExpressionList<T> implements Expression<T> {
	
	protected final Expression<? extends T>[] expressions;
	protected boolean and;
	private final boolean single;
	private final Class<T> returnType;
	@Nullable
	private ExpressionList<?> source;
	
	public ExpressionList(final Expression<? extends T>[] expressions, final Class<T> returnType, final boolean and) {
		this(expressions, returnType, and, null);
	}
	
	protected ExpressionList(final Expression<? extends T>[] expressions, final Class<T> returnType, final boolean and, final @Nullable ExpressionList<?> source) {
		assert expressions != null && expressions.length > 1;
		this.expressions = expressions;
		this.returnType = returnType;
		this.and = and;
		if (and) {
			single = false;
		} else {
			boolean single = true;
			for (final Expression<?> e : expressions) {
				if (!e.isSingle()) {
					single = false;
					break;
				}
			}
			this.single = single;
		}
		this.source = source;
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	@Nullable
	public T getSingle(final Event e) {
		if (!single)
			throw new UnsupportedOperationException();
		for (final int i : CollectionUtils.permutation(expressions.length)) {
			final T t = expressions[i].getSingle(e);
			if (t != null)
				return t;
		}
		return null;
	}
	
	@Override
	public T[] getArray(final Event e) {
		if (and)
			return getAll(e);
		for (final int i : CollectionUtils.permutation(expressions.length)) {
			final T[] t = expressions[i].getArray(e);
			if (t.length > 0)
				return t;
		}
		@SuppressWarnings("unchecked")
		final T[] r = (T[]) Array.newInstance(returnType, 0);
		assert r != null;
		return r;
	}
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public T[] getAll(final Event e) {
		final ArrayList<T> r = new ArrayList<T>();
		for (final Expression<? extends T> expr : expressions)
			r.addAll(Arrays.asList(expr.getAll(e)));
		return r.toArray((T[]) Array.newInstance(returnType, r.size()));
	}
	
	@Override
	@Nullable
	public Iterator<? extends T> iterator(final Event e) {
		if (!and) {
			for (final int i : CollectionUtils.permutation(expressions.length)) {
				final Iterator<? extends T> t = expressions[i].iterator(e);
				if (t != null && t.hasNext())
					return t;
			}
			return null;
		}
		return new Iterator<T>() {
			private int i = 0;
			@Nullable
			private Iterator<? extends T> current = null;
			
			@Override
			public boolean hasNext() {
				Iterator<? extends T> c = current;
				while (i < expressions.length && (c == null || !c.hasNext()))
					current = c = expressions[i++].iterator(e);
				return c != null && c.hasNext();
			}
			
			@Override
			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final Iterator<? extends T> c = current;
				if (c == null)
					throw new NoSuchElementException();
				final T t = c.next();
				assert t != null : current;
				return t;
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public boolean isSingle() {
		return single;
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c, final boolean negated) {
		return negated ^ check(e, c);
	}
	
	@Override
	public boolean check(final Event e, final Checker<? super T> c) {
		for (final Expression<? extends T> expr : expressions) {
			final Boolean b = expr.check(e, c);
			if (and && !b)
				return false;
			if (!and && b)
				return true;
		}
		return and;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <R> Expression<? extends R> getConvertedExpression(final Class<R>... to) {
		final Expression<? extends R>[] exprs = new Expression[expressions.length];
		for (int i = 0; i < exprs.length; i++)
			if ((exprs[i] = expressions[i].getConvertedExpression(to)) == null)
				return null;
		return new ExpressionList<R>(exprs, (Class<R>) Utils.getSuperType(to), and, this);
	}
	
	@Override
	public Class<T> getReturnType() {
		return returnType;
	}
	
	@Override
	public boolean getAnd() {
		return and;
	}
	
	/**
	 * For use in {@link CondCompare} only.
	 * 
	 * @return The old 'and' value
	 */
	public boolean setAnd(final boolean and) {
		final boolean r = and;
		this.and = and;
		return r;
	}
	
	/**
	 * For use in {@link CondCompare} only.
	 */
	public void invertAnd() {
		and = !and;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		Class<?>[] l = expressions[0].acceptChange(mode);
		if (l == null)
			return null;
		final ArrayList<Class<?>> r = new ArrayList<Class<?>>(Arrays.asList(l));
		for (int i = 1; i < expressions.length; i++) {
			l = expressions[i].acceptChange(mode);
			if (l == null)
				return null;
			r.retainAll(Arrays.asList(l));
			if (r.isEmpty())
				return null;
		}
		return r.toArray(new Class[r.size()]);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		for (final Expression<?> expr : expressions) {
			expr.change(e, delta, mode);
		}
	}
	
	private int time = 0;
	
	@Override
	public boolean setTime(final int time) {
		boolean ok = false;
		for (final Expression<?> e : expressions) {
			ok |= e.setTime(time);
		}
		if (ok)
			this.time = time;
		return ok;
	}
	
	@Override
	public int getTime() {
		return time;
	}
	
	@Override
	public boolean isDefault() {
		return false;
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		for (final Expression<?> e : expressions)
			if (e.isLoopOf(s))
				return true;
		return false;
	}
	
	@Override
	public Expression<?> getSource() {
		final ExpressionList<?> s = source;
		return s == null ? this : s;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		final StringBuilder b = new StringBuilder("(");
		for (int i = 0; i < expressions.length; i++) {
			if (i != 0) {
				if (i == expressions.length - 1)
					b.append(and ? " and " : " or ");
				else
					b.append(", ");
			}
			b.append(expressions[i].toString(e, debug));
		}
		b.append(")");
		if (debug)
			b.append("[").append(returnType).append("]");
		return "" + b;
	}
	
	@Override
	public String toString() {
		return toString(null, false);
	}
	
	/**
	 * @return The internal list of expressions. Can be modified with care.
	 */
	public Expression<? extends T>[] getExpressions() {
		return expressions;
	}
	
	@Override
	public Expression<T> simplify() {
		boolean isLiteralList = true;
		boolean isSimpleList = true;
		for (int i = 0; i < expressions.length; i++) {
			expressions[i] = expressions[i].simplify();
			isLiteralList &= expressions[i] instanceof Literal;
			isSimpleList &= expressions[i].isSingle();
		}
		if (isLiteralList && isSimpleList) {
			@SuppressWarnings("unchecked")
			final T[] values = (T[]) Array.newInstance(returnType, expressions.length);
			for (int i = 0; i < values.length; i++)
				values[i] = ((Literal<? extends T>) expressions[i]).getSingle();
			return new SimpleLiteral<T>(values, returnType, and);
		}
		if (isLiteralList) {
			final Literal<? extends T>[] ls = Arrays.copyOf(expressions, expressions.length, Literal[].class);
			assert ls != null;
			return new LiteralList<T>(ls, returnType, and);
		}
		return this;
	}
	
}
