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

package ch.njol.util.coll.iterator;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A simple iterator to iterate over an array.
 *
 * @author Peter Güttinger
 */
public final class ArrayIterator<T> implements Iterator<T> {

    @Nullable
    private final T[] array;

    private int index;

    public ArrayIterator(@Nullable final T[] array) {
        this.array = array;
    }

    public ArrayIterator(@Nullable final T[] array, final int start) {
        this.array = array;
        index = start;
    }

    @Override
    public final boolean hasNext() {
        final T[] arr = this.array;
        if (arr == null)
            return false;
        return index < arr.length;
    }

    @Override
    @Nullable
    public final T next() {
        final T[] array = this.array;
        if (array == null || index >= array.length)
            throw new NoSuchElementException();
        return array[index++];
    }

    /**
     * Not supported by arrays.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

}
