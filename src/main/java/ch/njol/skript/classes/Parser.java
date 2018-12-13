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

package ch.njol.skript.classes;

import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A parser used to parse data from a string or turn data into a string.
 * 
 * @author Peter Güttinger
 * @param <T> the type of this parser
 * @see Classes#registerClass(ClassInfo)
 * @see ClassInfo
 * @see Classes#toString(Object)
 */
public abstract class Parser<T> {
	
	/**
	 * Parses the input. This method may print an error prior to returning null if the input couldn't be parsed.
	 * <p>
	 * Remember to override {@link #canParse(ParseContext)} if this parser doesn't parse at all (i.e. you only use it's toString methods) or only parses for certain contexts.
	 * 
	 * @param s The String to parse. This string is already trim()med.
	 * @param context Context of parsing, may not be null
	 * @return The parsed input or null if the input is invalid for this parser.
	 */
	@Nullable
	public abstract T parse(String s, ParseContext context);
	
	/**
	 * @return Whether {@link #parse(String, ParseContext)} can actually return something other that null for the given context
	 */
	public boolean canParse(final ParseContext context) {
		return true;
	}
	
	/**
	 * Returns a string representation of the given object to be used in messages.
	 * 
	 * @param o The object. This will never be <code>null</code>.
	 * @return The String representation of the object.
	 * @see #getDebugMessage(Object)
	 */
	public abstract String toString(T o, int flags);
	
	/**
	 * Gets a string representation of this object for the given mode
	 * 
	 * @param o
	 * @param mode
	 * @return A string representation of the given object.
	 */
	public final String toString(final T o, final StringMode mode) {
		switch (mode) {
			case MESSAGE:
				return toString(o, 0);
			case DEBUG:
				return getDebugMessage(o);
			case VARIABLE_NAME:
				return toVariableNameString(o);
			case COMMAND:
				return toCommandString(o);
		}
		assert false;
		return "";
	}
	
	// not used anymore
	public String toCommandString(final T o) {
		return toString(o, 0);
	}
	
	/**
	 * Returns an object's string representation in a variable name.
	 * 
	 * @param o
	 * @return The given object's representation in a variable name.
	 */
	public abstract String toVariableNameString(final T o);
	
	/**
	 * Returns a pattern that matches all possible outputs of {@link #toVariableNameString(Object)}. This is used to test for variable conflicts.
	 * <p>
	 * This pattern is inserted directly into another pattern, i.e. without any surrounding parantheses, and the pattern is compiled without any checks, thus an invalid pattern
	 * will crash Skript.
	 * 
	 * @return A valid Regex pattern string matching all possible return values of {@link #toVariableNameString(Object)}
	 */
	public abstract String getVariableNamePattern();
	
	/**
	 * Returns a string representation of the given object to be used for debugging.<br>
	 * The Parser of 'Block' for example returns the block's type in toString, while this method also returns the coordinates of the block.<br>
	 * The default implementation of this method returns {@link #toString(Object, int) toString}(o, 0).
	 * 
	 * @param o
	 * @return A message containing debug information about the given object
	 */
	public String getDebugMessage(final T o) {
		return toString(o, 0);
	}
	
}
