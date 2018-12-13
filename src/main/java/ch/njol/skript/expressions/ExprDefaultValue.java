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

/**
 * @author Peter Güttinger
 */
@Name("Default Value")
@Description("A shorthand expression for giving things a default value. If the first thing isn't set, the second thing will be returned.")
@Examples({"broadcast {score::%player's uuid%} otherwise \"%player% has no score!\""})
@Since("2.2-Fixes-V10c")
@SuppressWarnings("unchecked")
public class ExprDefaultValue<T> extends SimpleExpression<T> {
	
	static {
		Skript.registerExpression(ExprDefaultValue.class, Object.class, ExpressionType.COMBINED, "%objects% (otherwise|?[?]) %objects%"); // make them two like in C# (a ?? b style) (optional)
	}
	
	private final ExprDefaultValue<?> source;
	private final Class<T> superType;
	@Nullable
	private Expression<Object> first;
	@Nullable
	private Expression<Object> second;
	
	@SuppressWarnings({"unchecked", "null"})
	public ExprDefaultValue() {
		this(null, (Class<? extends T>) Object.class);
	}
	
	@SuppressWarnings({"unchecked", "null"})
	private ExprDefaultValue(final ExprDefaultValue<?> source, final Class<? extends T>... types) {
		this.source = source;
		if (source != null) {
			this.first = source.first;
			this.second = source.second;
		}
		this.superType = (Class<T>) Utils.getSuperType(types);
	}
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		first = LiteralUtils.defendExpression(exprs[0]);
		second = LiteralUtils.defendExpression(exprs[1]);
		return LiteralUtils.canInitSafely(first, second);
	}
	
	@Override
	@SuppressWarnings({"unchecked", "null"})
	protected T[] get(final Event e) {
		final Object[] first = this.first.getArray(e);
		final Object[] values = first.length != 0 ? first : second.getArray(e);
		try {
			return Converters.convertStrictly(values, superType);
		} catch (final ClassCastException e1) {
			return (T[]) Array.newInstance(superType, 0);
		}
	}
	
	@Override
	public <R> Expression<? extends R> getConvertedExpression(final Class<R>... to) {
		return new ExprDefaultValue<R>(this, to);
	}
	
	@Override
	@SuppressWarnings("null")
	public Expression<?> getSource() {
		return source == null ? this : source;
	}
	
	@Override
	public Class<T> getReturnType() {
		return superType;
	}
	
	@Override
	@SuppressWarnings("null")
	public boolean isSingle() {
		return first.isSingle() && second.isSingle();
	}
	
	@Override
	@SuppressWarnings("null")
	public String toString(final Event e, final boolean debug) {
		return first.toString(e, debug) + " or else " + second.toString(e, debug);
	}
	
}
