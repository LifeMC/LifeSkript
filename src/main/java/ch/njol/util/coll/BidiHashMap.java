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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public class BidiHashMap<T1, T2> extends HashMap<T1, T2> implements BidiMap<T1, T2> {
	
	private static final long serialVersionUID = -9011678701069901061L;
	
	private final BidiHashMap<T2, T1> other;
	
	private BidiHashMap(final BidiHashMap<T2, T1> other) {
		this.other = other;
	}
	
	public BidiHashMap() {
		other = new BidiHashMap<T2, T1>(this);
	}
	
	public BidiHashMap(final Map<? extends T1, ? extends T2> values) {
		other = new BidiHashMap<T2, T1>(this);
		putAll(values);
	}
	
	@Override
	public BidiHashMap<T2, T1> getReverseView() {
		return other;
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	public T1 getKey(final T2 value) {
		return other.get(value);
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	public T2 getValue(final @Nullable T1 key) {
		return get(key);
	}
	
	@Nullable
	private T2 putDirect(final @Nullable T1 key, final @Nullable T2 value) {
		return super.put(key, value);
	}
	
	@Override
	@Nullable
	public T2 put(final @Nullable T1 key, final @Nullable T2 value) {
		if (key == null || value == null)
			throw new IllegalArgumentException("Can't store null in a BidiHashMap");
		
		removeDirect(key);
		other.removeDirect(value);
		final T2 oldValue = putDirect(key, value);
		other.putDirect(value, key);
		return oldValue;
	}
	
	@SuppressWarnings("null")
	@Override
	public void putAll(final @Nullable Map<? extends T1, ? extends T2> m) {
		for (final Entry<? extends T1, ? extends T2> e : m.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}
	
	@Nullable
	private T2 removeDirect(final @Nullable Object key) {
		return super.remove(key);
	}
	
	@Override
	@Nullable
	public T2 remove(final @Nullable Object key) {
		final T2 oldValue = removeDirect(key);
		if (oldValue != null)
			other.removeDirect(oldValue);
		return oldValue;
	}
	
	private void clearDirect() {
		super.clear();
	}
	
	@Override
	public void clear() {
		this.clearDirect();
		other.clearDirect();
	}
	
	@Override
	public boolean containsValue(final @Nullable Object value) {
		return other.containsKey(value);
	}
	
	// TODO check how changes to the sets affect the map
	
	@SuppressWarnings("null")
	@Override
	public Set<Entry<T1, T2>> entrySet() {
		return Collections.unmodifiableSet(super.entrySet());
	}
	
	@SuppressWarnings("null")
	@Override
	public Set<T1> keySet() {
		return Collections.unmodifiableSet(super.keySet());
	}
	
	@Override
	public Set<T2> values() {
		return valueSet();
	}
	
	@SuppressWarnings("null")
	@Override
	public Set<T2> valueSet() {
		return Collections.unmodifiableSet(other.keySet());
	}
	
	@Override
	public BidiHashMap<T1, T2> clone() {
		return new BidiHashMap<T1, T2>(this);
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@SuppressWarnings("null")
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (this.other == null ? 0 : this.other.hashCode());
		return result;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings({"null", "unused"})
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof BidiHashMap))
			return false;
		final BidiHashMap<?, ?> other = (BidiHashMap<?, ?>) obj;
		if (this.other == null) {
			if (other.other != null)
				return false;
		} else if (!this.other.equals(other.other))
			return false;
		return true;
	}
	
}
