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

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Container;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Güttinger
 */
public class ContainerExpression extends SimpleExpression<Object> {

    private final Expression<? extends Container<?>> expr;
    private final Class<?> c;

    public ContainerExpression(final Expression<? extends Container<?>> expr, final Class<?> c) {
        this.expr = expr;
        this.c = c;
    }

    @Override
    protected Object[] get(final Event e) {
        throw new UnsupportedOperationException("ContanerExpression must only be used by Loops");
    }

    @Override
    @Nullable
    public Iterator<Object> iterator(final Event e) {
        final Iterator<? extends Container<?>> iter = expr.iterator(e);
        if (iter == null)
            return null;
        return new ContainerIterator(iter, expr);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<?> getReturnType() {
        return c;
    }

    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return expr.toString(e, debug);
    }

    private static final class ContainerIterator implements Iterator<Object> {
        private final Iterator<? extends Container<?>> iterator;
        private final Expression<? extends Container<?>> expression;

        @Nullable
        private Iterator<?> current;

        ContainerIterator(final Iterator<? extends Container<?>> iterator,
                          final Expression<? extends Container<?>> expression) {
            this.iterator = iterator;
            this.expression = expression;
        }

        @Override
        public final boolean hasNext() {
            Iterator<?> c = current;
            while (iterator.hasNext() && (c == null || !c.hasNext())) {
                current = c = iterator.next().containerIterator();
            }
            return c != null && c.hasNext();
        }

        @Override
        public final Object next() {
            if (!hasNext())
                throw new NoSuchElementException();
            final Iterator<?> c = current;
            if (c == null)
                throw new NoSuchElementException();
            final Object o = c.next();
            assert o != null : current + "; " + expression;
            return o;
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
