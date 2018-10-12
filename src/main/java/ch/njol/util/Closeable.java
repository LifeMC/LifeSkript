/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.util;

/**
 * Like {@link java.io.Closeable}, but not used for resources, thus it neither throws checked exceptions nor causes resource leak warnings.
 * 
 * @author Peter Güttinger
 */
public interface Closeable {
	
	/**
	 * Closes this object. This method may be called multiple times and may or may not have an effect on subsequent calls (e.g. a task might be stopped, but resumed later and
	 * stopped again).
	 */
	public void close();
	
}
