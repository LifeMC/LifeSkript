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

package ch.njol.skript.util;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.util.StringUtils;

import java.util.HashMap;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class EnumUtils<E extends Enum<E>> {
	
	private final Class<E> c;
	private final String languageNode;
	
	private String[] names;
	private final HashMap<String, E> parseMap = new HashMap<String, E>();
	
	public EnumUtils(final Class<E> c, final String languageNode) {
		assert c != null && c.isEnum() : c;
		assert languageNode != null && !languageNode.isEmpty() && !languageNode.endsWith(".") : languageNode;
		
		this.c = c;
		this.languageNode = languageNode;
		
		names = new String[c.getEnumConstants().length];
		
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				validate(true);
			}
		});
	}
	
	/**
	 * Updates the names if the language has changed or the enum was modified (using reflection).
	 */
	void validate(final boolean force) {
		boolean update = force;
		
		final int newL = c.getEnumConstants().length;
		if (newL > names.length) {
			names = new String[newL];
			update = true;
		}
		
		if (update) {
			parseMap.clear();
			for (final E e : c.getEnumConstants()) {
				final String[] ls = Language.getList(languageNode + "." + e.name());
				names[e.ordinal()] = ls[0];
				for (final String l : ls)
					parseMap.put(l.toLowerCase(), e);
			}
		}
	}
	
	@Nullable
	public E parse(final String s) {
		validate(false);
		return parseMap.get(s.toLowerCase());
	}
	
	@SuppressWarnings("null")
	public String toString(final E e, final int flags) {
		validate(false);
		return names[e.ordinal()];
	}
	
	public String getAllNames() {
		validate(false);
		return StringUtils.join(names, ", ");
	}
	
}
