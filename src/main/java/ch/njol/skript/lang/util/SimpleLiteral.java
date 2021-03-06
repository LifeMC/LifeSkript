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
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.UnparsedLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.NonNullIterator;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;

/**
 * Represents a literal, i.e. a static value like a number or a string.
 *
 * @author Peter Güttinger
 * @see UnparsedLiteral
 */
public class SimpleLiteral<T> implements Literal<T>, DefaultExpression<T> {

    protected final Class<T> c;

    private final boolean isDefault;
    private final boolean and;
    protected transient T[] data;
    @Nullable
    private UnparsedLiteral source;
    @Nullable
    private ClassInfo<? super T> returnTypeInfo;

    public SimpleLiteral(final T[] data, final Class<T> c, final boolean and) {
        assert data != null && data.length != 0;
        assert c != null;
        this.data = data;
        this.c = c;
        this.and = data.length == 1 || and;
        this.isDefault = false;
    }

    public SimpleLiteral(final T data) {
        this(data, false);
    }

    @SuppressWarnings({"null", "unchecked"})
    public SimpleLiteral(final T data, final boolean isDefault) {
        assert data != null;
        this.data = (T[]) Array.newInstance(data.getClass(), 1);
        this.data[0] = data;
        c = (Class<T>) data.getClass();
        and = true;
        this.isDefault = isDefault;
    }

    public SimpleLiteral(final T[] data, final Class<T> to, final boolean and, @Nullable final UnparsedLiteral source) {
        this(data, to, and);
        this.source = source;
    }

    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public final T[] getArray() {
        return data;
    }

    @Override
    public T[] getArray(final Event e) {
        return data;
    }

    @Override
    public T[] getAll() {
        return data;
    }

    @Override
    public T[] getAll(final Event e) {
        return data;
    }

    @SuppressWarnings("null")
    @Override
    public final T getSingle() {
        return CollectionUtils.getRandom(data);
    }

    @Override
    public T getSingle(final Event e) {
        return getSingle();
    }

    @Override
    public Class<T> getReturnType() {
        return c;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <R> Literal<? extends R> getConvertedExpression(final Class<R>... to) {
        if (CollectionUtils.containsSuperclass(to, c))
            return (Literal<? extends R>) this;
        final R[] parsedData = Converters.convertArray(data, to, (Class<R>) Utils.getSuperType(to));
        if (parsedData.length != data.length)
            return null;
        return new ConvertedLiteral<>(this, parsedData, (Class<R>) Utils.getSuperType(to));
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        if (debug)
            return '[' + Classes.toString(data, and, StringMode.DEBUG) + ']';
        return Classes.toString(data, and);
    }

    @Override
    public final String toString() {
        return toString(null, false);
    }

    @Override
    public boolean isSingle() {
        return !and || data.length == 1;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean check(final Event e, final Checker<? super T> c, final boolean negated) {
        return SimpleExpression.check(data, c, negated, and);
    }

    @Override
    public boolean check(final Event e, final Checker<? super T> c) {
        return SimpleExpression.check(data, c, false, and);
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(final ChangeMode mode) {
        ClassInfo<? super T> rti = returnTypeInfo;
        if (rti == null)
            returnTypeInfo = rti = Classes.getSuperClassInfo(c);
        final Changer<? super T> c = rti.getChanger();
        return c == null ? null : c.acceptChange(mode);
    }

    @Override
    public void change(final Event e, @Nullable final Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
        final ClassInfo<? super T> rti = returnTypeInfo;
        if (rti == null)
            throw new UnsupportedOperationException();
        final Changer<? super T> c = rti.getChanger();
        if (c == null)
            throw new UnsupportedOperationException();
        c.change(getArray(), delta, mode);
    }

    @Override
    public boolean getAnd() {
        return and;
    }

    @Override
    public boolean setTime(final int time) {
        return false;
    }

    @Override
    public int getTime() {
        return 0;
    }

    @Override
    public NonNullIterator<T> iterator(final Event e) {
        return new LiteralIterator<>(data);
    }

    @Override
    public boolean isLoopOf(final String s) {
        return false;
    }

    @Override
    public Expression<?> getSource() {
        final UnparsedLiteral s = source;
        return s == null ? this : s;
    }

    @Override
    public Expression<T> simplify() {
        return this;
    }

    private static final class LiteralIterator<T> extends NonNullIterator<T> {
        private final transient T[] data;
        private int i;

        LiteralIterator(final T[] data) {
            this.data = data;
        }

        @Override
        @Nullable
        protected final T getNext() {
            if (i == data.length)
                return null;
            return data[i++];
        }
    }

}
