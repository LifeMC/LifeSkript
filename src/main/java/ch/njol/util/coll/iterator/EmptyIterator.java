/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2014 Peter Güttinger
 *
 */

package ch.njol.util.coll.iterator;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Güttinger
 */
public final class EmptyIterator<T> implements Iterator<T> {

    public final static EmptyIterator<Object> instance = new EmptyIterator<Object>();

    @SuppressWarnings("unchecked")
    public static <T> EmptyIterator<T> get() {
        return (EmptyIterator<T>) instance;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj instanceof EmptyIterator;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
