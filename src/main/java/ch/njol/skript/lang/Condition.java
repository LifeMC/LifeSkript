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

package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Checker;

import org.bukkit.event.Event;

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A condition which must be fulfilled for the trigger to continue. If the condition is in a section the behaviour depends on the section.
 * 
 * @author Peter Güttinger
 * @see Skript#registerCondition(Class, String...)
 */
public abstract class Condition extends Statement {
	
	private boolean negated;
	
	protected Condition() {}
	
	/**
	 * Checks whether this condition is satisfied with the given event. This should not alter the event or the world in any way, as conditions are only checked until one returns
	 * false. All subsequent conditions of the same trigger will then be omitted.<br/>
	 * <br/>
	 * You might want to use {@link SimpleExpression#check(Event, Checker)}
	 * 
	 * @param e the event to check
	 * @return <code>true</code> if the condition is satisfied, <code>false</code> otherwise or if the condition doesn't apply to this event.
	 */
	public abstract boolean check(final Event e);
	
	@Override
	public final boolean run(final Event e) {
		return check(e);
	}
	
	/**
	 * Sets the negation status of this condition. This will change the behaviour of {@link Expression#check(Event, Checker, boolean)}.
	 * 
	 * @param invert
	 */
	protected final void setNegated(final boolean invert) {
		negated = invert;
	}
	
	/**
	 * @return whether this condition is negated or not.
	 */
	public final boolean isNegated() {
		return negated;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked", "null"})
	@Nullable
	public static Condition parse(String s, final String defaultError) {
		s = s.trim();
		while (s.startsWith("(") && SkriptParser.next(s, 0, ParseContext.DEFAULT) == s.length())
			s = s.substring(1, s.length() - 1);
		return (Condition) SkriptParser.parse(s, (Iterator) Skript.getConditions().iterator(), defaultError);
	}
	
}
