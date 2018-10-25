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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.compat;

import ch.njol.skript.Skript;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author TheDGOfficial
 */
public abstract class Compatibility {
	
	/**
	 * Gets the most compatible class.
	 * 
	 * @param oldClass - The old, deprecated or moved class.
	 * @param newClass - The new class.
	 * @param superClass - The superclass of the two classes entered.
	 * 
	 * @return The most compatible class as super class.
	 * If you must do operations specific to old or new one,
	 * check via {@link Class#isAssignableFrom(Class)} and cast it.
	 */
	@NonNull @SuppressWarnings("null")
	public final static <Superclass> Class<? extends Superclass> getClass(final String oldClass, final String newClass, final Class<Superclass> superClass) {
		
		if(Skript.classExists(newClass)) {
			
			return (Class<? extends Superclass>) Skript.classForName(newClass);
			
		} else if(Skript.classExists(oldClass)) {
			
			return (Class<? extends Superclass>) Skript.classForName(oldClass);
			
		} else {
			
			// Should be never happen
			if(Skript.logHigh())
				Skript.warning("The class " + newClass + " (also known as " + oldClass + ") is not available on this server.");
			return superClass;
			
		}
		
	}
	
	/**
	 * Gets the most compatible class.
	 * 
	 * @param oldClass - The old, deprecated or moved class.
	 * @param newClass - The new class.
	 * 
	 * @return The most compatible class as super class.
	 * If you must do operations specific to old or new one,
	 * check via {@link Class#isAssignableFrom(Class)} and cast it.
	 */
	@Nullable @SuppressWarnings("null")
	public final static <Superclass> Class<? extends Superclass> getClass(final String oldClass, final String newClass) {
		
		return Compatibility.<Superclass>getClass(oldClass, newClass, null);
		
	}
	
	/**
	 * Gets the most compatible class.
	 * 
	 * @param oldClass - The old, deprecated or moved class.
	 * @param newClass - The new class.
	 * 
	 * @return The most compatible class as generic class.
	 * If you must do operations specific to old or new one,
	 * check via {@link Class#isAssignableFrom(Class)} and cast it.
	 */
	@Nullable @SuppressWarnings("null")
	public final static Class<?> getClassNoSuper(final String oldClass, final String newClass) {
		
		return getClass(oldClass, newClass, null);
		
	}
	
}
