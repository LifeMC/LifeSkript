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

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.util.NullableChecker;

/**
 * @author Peter Güttinger
 */
public class StoppableIterator<T> implements Iterator<T> {
	
	private final Iterator<T> iter;
	private final NullableChecker<T> stopper;
	
	private final boolean returnLast;
	@Nullable
	private T current;
	
	private boolean stopped = false;
	private boolean calledNext = false;
	
	/**
	 * @param iter
	 * @param stopper Called for every element. If it returns true the iteration is stopped.
	 * @param returnLast Whether to return the last element, i.e. the element on which the stopper stops.
	 *            This doesn't change anything if the iterator ends before the stopper stops.
	 */
	public StoppableIterator(final Iterator<T> iter, final NullableChecker<T> stopper, final boolean returnLast) {
		assert stopper != null;
		this.iter = iter;
		this.stopper = stopper;
		this.returnLast = returnLast;
		if (!returnLast && iter.hasNext())
			current = iter.next();
	}
	
	@Override
	public boolean hasNext() {
		final boolean cn = calledNext;
		calledNext = false;
		if (stopped || !iter.hasNext())
			return false;
		if (cn && !returnLast) {
			current = iter.next();
			if (stopper.check(current)) {
				stop();
				return false;
			}
		}
		return true;
	}
	
	@Override
	@Nullable
	public T next() {
		if (!hasNext())
			throw new NoSuchElementException();
		calledNext = true;
		if (!returnLast)
			return current;
		final T t = iter.next();
		if (stopper.check(t))
			stop();
		return t;
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
	public void stop() {
		stopped = true;
	}
	
}
