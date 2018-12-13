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

package ch.njol.util.coll;

import ch.njol.util.Math2;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A list with fixed size that overrides the oldest elements when new elements are added and no more space is available.
 * 
 * @author Peter Güttinger
 */
public final class CyclicList<E> extends AbstractList<E> {
	
	private final Object[] items;
	private int start;
	
	public CyclicList(final int size) {
		this.items = new Object[size];
	}
	
	public CyclicList(final E[] array) {
		this.items = new Object[array.length];
		System.arraycopy(array, 0, items, 0, array.length);
	}
	
	public CyclicList(final Collection<E> c) {
		final Object[] items = c.toArray();
		if (items == null)
			throw new IllegalArgumentException("" + c);
		this.items = items;
	}
	
	private int toInternalIndex(final int index) {
		return Math2.mod(start + index, items.length);
	}
	
	private int toExternalIndex(final int internal) {
		return Math2.mod(internal - start, items.length);
	}
	
	@Override
	public boolean add(final @Nullable E e) {
		return addLast(e);
	}
	
	public boolean addFirst(final @Nullable E e) {
		start = Math2.mod(start - 1, items.length);
		items[start] = e;
		return true;
	}
	
	public boolean addLast(final @Nullable E e) {
		items[start] = e;
		start = Math2.mod(start + 1, items.length);
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public void add(final int index, final E e) {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean addAll(final Collection<? extends E> c) {
		for (final E e : c)
			add(e);
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
	
	private void rangeCheck(final int index) {
		if (index < 0 || index >= items.length)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + items.length);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public E get(final int index) {
		rangeCheck(index);
		return (E) items[toInternalIndex(index)];
	}
	
	@Override
	public int indexOf(final @Nullable Object o) {
		return toExternalIndex(CollectionUtils.indexOf(items, o));
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public int lastIndexOf(final @Nullable Object o) {
		return toExternalIndex(CollectionUtils.lastIndexOf(items, o));
	}
	
	@Override
	public boolean remove(final @Nullable Object o) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public E remove(final int index) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeAll(final @Nullable Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean retainAll(final @Nullable Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public E set(final int index, final @Nullable E e) {
		rangeCheck(index);
		final int i = toInternalIndex(index);
		final E old = (E) items[i];
		items[i] = e;
		return old;
	}
	
	@Override
	public int size() {
		return items.length;
	}
	
	@Override
	public Object[] toArray() {
		return toArray(new Object[items.length]);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(final @Nullable T[] array) {
		if (array == null)
			return (T[]) toArray();
		if (array.length < items.length)
			return toArray((T[]) Array.newInstance(array.getClass().getComponentType(), items.length));
		System.arraycopy(items, start, array, 0, items.length - start);
		System.arraycopy(items, 0, array, items.length - start, start);
		if (array.length > items.length)
			array[items.length] = null;
		return array;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.deepHashCode(this.items);
		result = prime * result + this.start;
		return result;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof CyclicList))
			return false;
		final CyclicList<?> other = (CyclicList<?>) obj;
		if (!Arrays.deepEquals(this.items, other.items))
			return false;
		if (this.start != other.start)
			return false;
		return true;
	}
	
}
