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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * Represents a general part of the syntax. Implementing classes/interfaces are {@link Expression} and {@link Statement}.
 * <p>
 * TODO find a better name for this
 * 
 * @author Peter Güttinger
 */
public interface SyntaxElement {
	
	/**
	 * Called just after the constructor.
	 * 
	 * @param exprs all %expr%s included in the matching pattern in the order they appear in the pattern. If an optional value was left out it will still be included in this list
	 *            holding the default value of the desired type which usually depends on the event.
	 * @param matchedPattern The index of the pattern which matched
	 * @param isDelayed Whether this expression is used after a delay or not (i.e. if the event has already passed when this expression will be called)
	 * @param parseResult Additional information about the match.
	 * @return Whether this expression was initialised successfully. An error should be printed prior to returning false to specify the cause.
	 * @see ScriptLoader#isCurrentEvent(Class...)
	 */
	boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult);
	
	// TODO [code style] add equals to be able to find out whether two elements are equal (useful for e.g. 'if <a>: ... [else] if not <a>: ...')
	
}
