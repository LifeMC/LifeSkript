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
import ch.njol.skript.lang.function.EffFunctionCall;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;

import org.bukkit.event.Event;

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An effect which is unconditionally executed when reached, and execution will usually continue with the next item of the trigger after this effect is executed (the stop effect
 * for example stops the trigger, i.e. nothing else will be executed after it)
 * 
 * @author Peter Güttinger
 * @see Skript#registerEffect(Class, String...)
 */
public abstract class Effect extends Statement {
	
	protected Effect() {}
	
	/**
	 * Executes this effect.
	 * 
	 * @param e
	 */
	protected abstract void execute(Event e);
	
	@Override
	public final boolean run(final Event e) {
		execute(e);
		return true;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked", "null"})
	@Nullable
	public static Effect parse(final String s, final @Nullable String defaultError) {
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final EffFunctionCall f = EffFunctionCall.parse(s);
			if (f != null) {
				log.printLog();
				return f;
			} else if (log.hasError()) {
				log.printError();
				return null;
			} else {
				log.printError();
			}
		} finally {
			log.stop();
		}
		return (Effect) SkriptParser.parse(s, (Iterator) Skript.getEffects().iterator(), defaultError);
	}
	
}
