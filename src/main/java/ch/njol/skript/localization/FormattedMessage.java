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

package ch.njol.skript.localization;

import ch.njol.skript.Skript;

import java.util.IllegalFormatException;
import java.util.concurrent.atomic.AtomicReference;

public final class FormattedMessage extends Message {
	
	private final Object[] args;
	
	/**
	 * @param key
	 * @param args An array of Objects to replace into the format message, e.g. {@link AtomicReference}s.
	 */
	public FormattedMessage(final String key, final Object... args) {
		super(key);
		assert args.length > 0;
		this.args = args;
	}
	
	@Override
	public String toString() {
		try {
			final String val = getValue();
			return val == null ? key : "" + String.format(val, args);
		} catch (final IllegalFormatException e) {
			final String m = "The formatted message '" + key + "' uses an illegal format: " + e.getLocalizedMessage();
			Skript.adminBroadcast("<red>" + m);
			System.err.println("[Skript] " + m);
			e.printStackTrace();
			return "[ERROR]";
		}
	}
	
}
