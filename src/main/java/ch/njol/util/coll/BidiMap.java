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

import java.util.Map;
import java.util.Set;

/**
 * @author Peter Güttinger
 */
public interface BidiMap<T1, T2> extends Map<T1, T2> {
	
	public BidiMap<T2, T1> getReverseView();
	
	public T1 getKey(final T2 value);
	
	public T2 getValue(final T1 key);
	
	public Set<T2> valueSet();
	
}
