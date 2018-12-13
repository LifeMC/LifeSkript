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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Basic class to get text from the language file(s).
 * 
 * @author Peter Güttinger
 */
public class Message {
	
	// this is most likely faster than registering a listener for each Message
	final static Collection<Message> messages = new ArrayList<Message>(50);
	static boolean firstChange = true;
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				for (final Message m : messages) {
					synchronized (m) {
						m.revalidate = true;
					}
					if (firstChange && Skript.testing()) {
						if (!Language.english.containsKey(m.key))
							Language.missingEntryError(m.key);
					}
				}
				firstChange = false;
			}
		});
	}
	
	public final String key;
	@Nullable
	private String value;
	boolean revalidate = true;
	
	public Message(final String key) {
		this.key = "" + key.toLowerCase(Locale.ENGLISH);
		messages.add(this);
		if (Skript.testing() && !Language.english.isEmpty()) {
			if (!Language.english.containsKey(this.key))
				Language.missingEntryError(this.key);
		}
	}
	
	/**
	 * @return The value of this message in the current language
	 */
	@Override
	public String toString() {
		validate();
		return value == null ? key : "" + value;
	}
	
	/**
	 * Gets the text this Message refers to. This method automatically revalidates the value if necessary.
	 * 
	 * @return This message's value or null if it doesn't exist.
	 */
	@Nullable
	protected final String getValue() {
		validate();
		return value;
	}
	
	/**
	 * Checks whether this value is set in the current language or the english default.
	 * 
	 * @return Whether this message will display an actual value instead of its key when used
	 */
	public final boolean isSet() {
		validate();
		return value != null;
	}
	
	/**
	 * Checks whether this message's value has changed and calls {@link #onValueChange()} if neccessary.
	 */
	protected synchronized void validate() {
		if (revalidate) {
			revalidate = false;
			value = Language.get_(key);
			onValueChange();
		}
	}
	
	/**
	 * Called when this Message's value changes. This is not neccessarily called for every language change, but only when the value is actually accessed and the language has
	 * changed since the last call of this method.
	 */
	protected void onValueChange() {}
	
}
