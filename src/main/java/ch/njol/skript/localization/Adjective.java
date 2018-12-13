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
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.localization;

import ch.njol.skript.Skript;

import java.util.HashMap;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class Adjective extends Message {
	
	// at least in German adjectives behave differently with a definite article. Cases are still not supported though and will likely never be.
	private final static int DEFINITE_ARTICLE = -100;
	private final static String DEFINITE_ARTICLE_TOKEN = "+";
	
	private final HashMap<Integer, String> genders = new HashMap<Integer, String>();
	@Nullable
	String def;
	
	public Adjective(final String key) {
		super(key);
	}
	
	@Override
	protected void onValueChange() {
		genders.clear();
		final String v = getValue();
		def = v;
		if (v == null)
			return;
		final int s = v.indexOf('@');
		if (s == -1)
			return;
		final int e = v.lastIndexOf('@');
		if (s == e) {
			Skript.error("Invalid use of '@' in the adjective '" + key + "' in the " + Language.getName() + " language file: " + v);
			return;
		}
		def = v.substring(0, s) + v.substring(e + 1);
		int c = s;
		do {
			final int c2 = v.indexOf('@', c + 1);
			final int d = v.indexOf(':', c + 1);
			if (d == -1 || d > c2) {
				Skript.error("Missing colon (:) to separate the gender in the adjective '" + key + "' in the " + Language.getName() + " language file at index " + c + ": " + v);
				return;
			}
			final String gender = v.substring(c + 1, d);
			final int g = gender.equals(DEFINITE_ARTICLE_TOKEN) ? DEFINITE_ARTICLE : Noun.getGender(gender, key);
			if (!genders.containsKey(g))
				genders.put(g, v.substring(0, s) + v.substring(d + 1, c2) + v.substring(e + 1));
			c = c2;
		} while (c < e);
	}
	
	@Override
	public String toString() {
		validate();
		if (Skript.testing())
			Skript.warning("Invalid use of Adjective.toString()");
		return "" + def;
	}
	
	public String toString(int gender, final int flags) {
		validate();
		if ((flags & Language.F_DEFINITE_ARTICLE) != 0 && genders.containsKey(DEFINITE_ARTICLE))
			gender = DEFINITE_ARTICLE;
		else if ((flags & Language.F_PLURAL) != 0)
			gender = Noun.PLURAL;
		final String a = genders.get(gender);
		if (a != null)
			return a;
		return "" + def;
	}
	
	public static String toString(final Adjective[] adjectives, final int gender, final int flags, final boolean and) {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < adjectives.length; i++) {
			if (i != 0) {
				if (i == adjectives.length - 1)
					b.append(" ").append(and ? GeneralWords.and : GeneralWords.or).append(" ");
				else
					b.append(", ");
			}
			b.append(adjectives[i].toString(gender, flags));
		}
		return "" + b.toString();
	}
	
	public String toString(final Noun n, final int flags) {
		return n.toString(this, flags);
	}
	
}
