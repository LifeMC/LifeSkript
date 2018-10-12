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

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class EmptyIterable<T> implements Iterable<T> {
	
	public final static EmptyIterable<Object> instance = new EmptyIterable<Object>();
	
	@SuppressWarnings("unchecked")
	public static <T> EmptyIterable<T> get() {
		return (EmptyIterable<T>) instance;
	}
	
	@Override
	public Iterator<T> iterator() {
		return EmptyIterator.get();
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		return obj instanceof EmptyIterable;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
}
