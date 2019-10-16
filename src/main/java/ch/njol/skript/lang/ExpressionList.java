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

package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.EmptyArrays;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A list of expressions.
 *
 * @author Peter Güttinger
 */
public class ExpressionList<T> implements Expression<T> {

    protected final Expression<? extends T>[] expressions;
    private final boolean single;
    private final Class<T> returnType;
    @Nullable
    private final ExpressionList<?> source;
    protected boolean and;
    private int time;

    public ExpressionList(final Expression<? extends T>[] expressions, final Class<T> returnType, final boolean and) {
        this(expressions, returnType, and, null);
    }

    protected ExpressionList(final Expression<? extends T>[] expressions, final Class<T> returnType, final boolean and, @Nullable final ExpressionList<?> source) {
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
    public final T getSingle(final Event e) {
        if (!single)
            throw new UnsupportedOperationException();
        for (final int i : CollectionUtils.permutation(expressions.length)) {
            final T t = expressions[i].getSingle(e);
            if (t != null)
                return t;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T[] getArray(final Event e) {
        if (and)
            return getAll(e);
        for (final int i : CollectionUtils.permutation(expressions.length)) {
            final T[] t = expressions[i].getArray(e);
            if (t.length > 0)
                return t;
        }
        return (T[]) Array.newInstance(returnType, 0);
    }

    @SuppressWarnings({"null", "unchecked"})
    @Override
    public final T[] getAll(final Event e) {
        final ArrayList<T> r = new ArrayList<>();
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
        return new ExpressionListIterator<>(expressions, e);
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
    public final boolean check(final Event e, final Checker<? super T> c) {
        for (final Expression<? extends T> expr : expressions) {
            final boolean b = expr.check(e, c);
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
        return new ExpressionList<>(exprs, (Class<R>) Utils.getSuperType(to), and, this);
    }

    @Override
    public final Class<T> getReturnType() {
        return returnType;
    }

    @Override
    public boolean getAnd() {
        return and;
    }

    /**
     * For use in {@link ch.njol.skript.conditions.CondCompare} only.
     *
     * @return The old 'and' value
     */
    public boolean setAnd(final boolean and) {
        this.and = and;
        return and;
    }

    /**
     * For use in {@link ch.njol.skript.conditions.CondCompare} only.
     */
    public final void invertAnd() {
        and = !and;
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(final ChangeMode mode) {
        Class<?>[] l = expressions[0].acceptChange(mode);
        if (l == null)
            return null;
        final ArrayList<Class<?>> r = new ArrayList<>(Arrays.asList(l));
        for (int i = 1; i < expressions.length; i++) {
            l = expressions[i].acceptChange(mode);
            if (l == null)
                return null;
            r.retainAll(Arrays.asList(l));
            if (r.isEmpty())
                return null;
        }
        return r.toArray(EmptyArrays.EMPTY_CLASS_ARRAY);
    }

    @Override
    public void change(final Event e, @Nullable final Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
        for (final Expression<?> expr : expressions) {
            expr.change(e, delta, mode);
        }
    }

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
    public final String toString(@Nullable final Event e, final boolean debug) {
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
        b.append(')');
        if (debug)
            b.append('[').append(returnType).append(']');
        return b.toString();
    }

    @Override
    public final String toString() {
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
            @SuppressWarnings("unchecked") final T[] values = (T[]) Array.newInstance(returnType, expressions.length);
            for (int i = 0; i < values.length; i++)
                values[i] = ((Literal<? extends T>) expressions[i]).getSingle();
            return new SimpleLiteral<>(values, returnType, and);
        }
        if (isLiteralList) {
            return new LiteralList<>(Arrays.copyOf(expressions, expressions.length, Literal[].class), returnType, and);
        }
        return this;
    }

    private static final class ExpressionListIterator<T> implements Iterator<T> {
        private final Expression<? extends T>[] expressions;
        private final Event e;
        private int i;
        @Nullable
        private Iterator<? extends T> current;

        ExpressionListIterator(final Expression<? extends T>[] expressions,
                               final Event e) {
            this.expressions = expressions;
            this.e = e;
        }

        @Override
        public final boolean hasNext() {
            Iterator<? extends T> c = current;
            while (i < expressions.length && (c == null || !c.hasNext()))
                current = c = expressions[i++].iterator(e);
            return c != null && c.hasNext();
        }

        @Override
        public final T next() {
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
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
