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

public class ImprovedIterator<T> implements Iterator<T> {
	
	private final Iterator<T> iter;
	
	@Nullable
	private T current = null;
	
	public ImprovedIterator(final Iterator<T> iter) {
		this.iter = iter;
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}
	
	@Override
	@Nullable
	public T next() {
		return current = iter.next();
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
	@Nullable
	public T current() {
		return current;
	}
	
}
