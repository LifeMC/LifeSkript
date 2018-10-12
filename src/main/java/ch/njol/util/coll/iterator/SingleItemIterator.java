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
import java.util.NoSuchElementException;

/**
 * @author Peter Güttinger
 */
public class SingleItemIterator<T> implements Iterator<T> {
	
	private final T item;
	private boolean returned = false;
	
	public SingleItemIterator(final T item) {
		this.item = item;
	}
	
	@Override
	public boolean hasNext() {
		return !returned;
	}
	
	@Override
	public T next() {
		if (returned)
			throw new NoSuchElementException();
		returned = true;
		return item;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
