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

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * @author Peter Güttinger
 */
public final class IteratorIterable<T> implements Iterable<T> {

    private final Iterator<T> iter;

    public IteratorIterable(final Iterator<T> iter) {
        this.iter = iter;
    }

    @Override
    public final @NotNull Iterator<T> iterator() {
        return iter;
    }

}
