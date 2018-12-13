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

package ch.njol.skript.lang.util;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A very basic SkriptEvent which returns true for all events (i.e. all registered events).
 * 
 * @author Peter Güttinger
 */
public class SimpleEvent extends SkriptEvent {
	
	public SimpleEvent() {
		super();
	}
	
	@Override
	public boolean check(final Event e) {
		return true;
	}
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		if (args.length != 0)
			throw new SkriptAPIException("Invalid use of SimpleEvent");
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "simple event";
	}
	
}
