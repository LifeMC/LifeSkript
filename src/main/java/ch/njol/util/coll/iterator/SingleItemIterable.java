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

import java.util.Iterator;

/**
 * @author Peter Güttinger
 */
public final class SingleItemIterable<T> implements Iterable<T> {

    private final T item;

    public SingleItemIterable(final T item) {
        this.item = item;
    }

    @Override
    public Iterator<T> iterator() {
        return new SingleItemIterator<>(item);
    }

}
