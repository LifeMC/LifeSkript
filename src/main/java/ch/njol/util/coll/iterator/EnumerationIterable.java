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
 *   Copyright (C) 2011 Peter Güttinger and contributors
 *
 */

package ch.njol.util.coll.iterator;

import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Güttinger
 */
public final class EnumerationIterable<T> implements Iterable<T> {

    @Nullable
    private final Enumeration<? extends T> e;

    public EnumerationIterable(@Nullable final Enumeration<? extends T> e) {
        this.e = e;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        final Enumeration<? extends T> e = this.e;
        if (e == null)
            return EmptyIterator.get();
        return new EnumerationIterator<>(e);
    }

    private static final class EnumerationIterator<T> implements Iterator<T> {
        private final Enumeration<? extends T> enumeration;

        EnumerationIterator(final Enumeration<? extends T> enumeration) {
            this.enumeration = enumeration;
        }

        @Override
        public final boolean hasNext() {
            return enumeration.hasMoreElements();
        }

        @Override
        @Nullable
        public final T next() throws NoSuchElementException {
            return enumeration.nextElement();
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
