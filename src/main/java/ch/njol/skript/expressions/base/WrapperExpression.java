/*
 *
 *     This file is part of Skript.
 *
 *    Skript is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Skript is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Skript. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 *   Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.expressions.base;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Converters;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.function.BiFunction;

/**
 * Represents an expression which is a wrapper of another one. Remember to set the wrapped expression in the constructor ({@link #WrapperExpression(SimpleExpression)})
 * or with {@link #setExpr(Expression)} in {@link ch.njol.skript.lang.SyntaxElement#init(Expression[], int, ch.njol.util.Kleenean, ch.njol.skript.lang.SkriptParser.ParseResult) init()}.<br/>
 * If you override {@link #get(Event)} you must override {@link #iterator(Event)} as well.
 *
 * @author Peter Güttinger
 */
public abstract class WrapperExpression<T> extends SimpleExpression<T> {

    private Expression<? extends T> expr;

    @SuppressWarnings("null")
    protected WrapperExpression() {
    }

    protected WrapperExpression(final SimpleExpression<? extends T> expr) {
        this((Expression<? extends T>) expr); // Backwards compatibility
    }

    protected WrapperExpression(final Expression<? extends T> expr) {
        this.expr = expr;
    }

    public final Expression<?> getExpr() {
        return expr;
    }

    protected final void setExpr(final Expression<? extends T> expr) {
        this.expr = expr;
    }

    @SafeVarargs
    @Override
    @Nullable
    protected final <R> ConvertedExpression<T, ? extends R> getConvertedExpr(final Class<R>... to) {
        for (final Class<R> c : to) {
            assert c != null;
            @SuppressWarnings("unchecked") final Converter<? super T, ? extends R> conv = (Converter<? super T, ? extends R>) Converters.getConverter(getReturnType(), c);
            if (conv == null)
                continue;
            return new ConvertedWrapperExpression<>(expr, c, conv, this::toString);
        }
        return null;
    }

    @Override
    protected T[] get(final Event e) {
        return expr.getArray(e);
    }

    @Override
    @Nullable
    public Iterator<? extends T> iterator(final Event e) {
        return expr.iterator(e);
    }

    @Override
    public boolean isSingle() {
        return expr.isSingle();
    }

    @Override
    public boolean getAnd() {
        return expr.getAnd();
    }

    @Override
    public final Class<? extends T> getReturnType() {
        return expr.getReturnType();
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(final ChangeMode mode) {
        return expr.acceptChange(mode);
    }

    @Override
    public void change(final Event e, @Nullable final Object[] delta, final ChangeMode mode) {
        expr.change(e, delta, mode);
    }

    @Override
    public boolean setTime(final int time) {
        return expr.setTime(time);
    }

    @Override
    public final int getTime() {
        return expr.getTime();
    }

    @Override
    public boolean isDefault() {
        return expr.isDefault();
    }

    @Override
    public Expression<? extends T> simplify() {
        return expr;
    }

    private static final class ConvertedWrapperExpression<T, R> extends ConvertedExpression<T, R> {
        private final BiFunction<Event, Boolean, String> toStringFunction;

        ConvertedWrapperExpression(final Expression<? extends T> expr,
                                   final Class<R> c,
                                   final Converter<? super T, ? extends R> converter,
                                   final BiFunction<Event, Boolean, String> toStringFunction) {
            super(expr, c, converter);

            this.toStringFunction = toStringFunction;
        }

        @Override
        public final String toString(@Nullable final Event e, final boolean debug) {
            if (debug && e == null)
                return '(' + toStringFunction.apply(null, true) + ")->" + to.getName();
            return toStringFunction.apply(e, debug);
        }
    }

}
