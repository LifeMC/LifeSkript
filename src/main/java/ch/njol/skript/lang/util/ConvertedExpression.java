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

package ch.njol.skript.lang.util;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a expression converted to another type. This, and not Expression, is the required return type of {@link SimpleExpression#getConvertedExpr(Class...)} because this
 * class
 * <ol>
 * <li>automatically lets the source expression handle everything apart from the get() methods</li>
 * <li>will never convert itself to another type, but rather request a new converted expression from the source expression.</li>
 * </ol>
 *
 * @author Peter Güttinger
 */
public class ConvertedExpression<F, T> implements Expression<T> {

    private final Converter<? super F, ? extends T> conv;
    protected Expression<? extends F> source;
    protected Class<T> to;
    @Nullable
    private ClassInfo<? super T> returnTypeInfo;

    public ConvertedExpression(final Expression<? extends F> source, final Class<T> to, final Converter<? super F, ? extends T> conv) {
        assert source != null;
        assert to != null;
        assert conv != null;

        this.source = source;
        this.to = to;
        this.conv = conv;
    }

    @SafeVarargs
    @Nullable
    public static final <F, T> ConvertedExpression<F, T> newInstance(final Expression<F> v, final Class<T>... to) {
        assert !CollectionUtils.containsSuperclass(to, v.getReturnType());
        for (final Class<T> c : to) { // REMIND try more converters? -> also change WrapperExpression (and maybe ExprLoopValue)
            assert c != null;
            // casting <? super ? extends F> to <? super F> is wrong, but since the converter is only used for values returned by the expression
            // (which are instances of "<? extends F>") this won't result in any ClassCastExceptions.
            @SuppressWarnings("unchecked") final Converter<? super F, ? extends T> conv = (Converter<? super F, ? extends T>) Converters.getConverter(v.getReturnType(), c);
            if (conv == null)
                continue;
            return new ConvertedExpression<>(v, c, conv);
        }
        return null;
    }

    @Override
    public final boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult matcher) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        if (debug && e == null)
            return '(' + source.toString(null, true) + " >> " + conv + ": " + source.getReturnType().getName() + "->" + to.getName() + ')';
        return source.toString(e, debug);
    }

    @Override
    public final String toString() {
        return toString(null, false);
    }

    @Override
    public Class<T> getReturnType() {
        return to;
    }

    @Override
    public boolean isSingle() {
        return source.isSingle();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <R> Expression<? extends R> getConvertedExpression(final Class<R>... to) {
        if (CollectionUtils.containsSuperclass(to, this.to))
            return (Expression<? extends R>) this;
        return source.getConvertedExpression(to);
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(final ChangeMode mode) {
        final Class<?>[] r = source.acceptChange(mode);
        if (r == null) {
            final ClassInfo<? super T> rti;
            returnTypeInfo = rti = Classes.getSuperClassInfo(to);
            final Changer<?> c = rti.getChanger();
            return c == null ? null : c.acceptChange(mode);
        }
        return r;
    }

    @Override
    public void change(final Event e, @Nullable final Object[] delta, final ChangeMode mode) {
        final ClassInfo<? super T> rti = returnTypeInfo;
        if (rti != null) {
            final Changer<? super T> c = rti.getChanger();
            if (c != null)
                c.change(getArray(e), delta, mode);
        } else {
            source.change(e, delta, mode);
        }
    }

    @Override
    @Nullable
    public T getSingle(final Event e) {
        final F f = source.getSingle(e);
        if (f == null)
            return null;
        return conv.convert(f);
    }

    @Override
    public T[] getArray(final Event e) {
        return Converters.convert(source.getArray(e), to, conv);
    }

    @Override
    public T[] getAll(final Event e) {
        return Converters.convert(source.getAll(e), to, conv);
    }

    @Override
    public boolean check(final Event e, final Checker<? super T> c, final boolean negated) {
        return negated ^ check(e, c);
    }

    @Override
    public boolean check(final Event e, final Checker<? super T> c) {
        return source.check(e, (Checker<F>) f -> {
            final T t = conv.convert(f);
            if (t == null)
                return false;
            return c.check(t);
        });
    }

    @Override
    public final boolean getAnd() {
        return source.getAnd();
    }

    @Override
    public boolean setTime(final int time) {
        return source.setTime(time);
    }

    @Override
    public int getTime() {
        return source.getTime();
    }

    @Override
    public boolean isDefault() {
        return source.isDefault();
    }

    @Override
    public boolean isLoopOf(final String s) {
        return false;// A loop does not convert the expression to loop
    }

    @Override
    @Nullable
    public Iterator<T> iterator(final Event e) {
        final Iterator<? extends F> iter = source.iterator(e);
        if (iter == null)
            return null;
        return new ConvertedIterator<T, F>(iter, conv);
    }

    @Override
    public Expression<?> getSource() {
        return source;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Expression<? extends T> simplify() {
        final Expression<? extends T> c = source.simplify().getConvertedExpression(to);
        if (c != null)
            return c;
        return this;
    }

    private static final class ConvertedIterator<T, F> implements Iterator<T> {
        private final Iterator<? extends F> iter;
        private final Converter<? super F, ? extends T> conv;
        @Nullable
        private T next;

        ConvertedIterator(final Iterator<? extends F> iter, final Converter<? super F, ? extends T> conv) {
            this.iter = iter;
            this.conv = conv;
        }

        @Override
        public final boolean hasNext() {
            if (next != null)
                return true;
            while (next == null && iter.hasNext()) {
                final F f = iter.next();
                next = f == null ? null : conv.convert(f);
            }
            return next != null;
        }

        @Override
        public final T next() {
            if (!hasNext())
                throw new NoSuchElementException();
            final T n = next;
            next = null;
            assert n != null;
            return n;
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
