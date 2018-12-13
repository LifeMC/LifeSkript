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

package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Setter;

import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public class Option<T> {
	
	public final String key;
	private boolean optional;
	
	@Nullable
	private String value;
	
	private final Converter<String, ? extends T> parser;
	private final T defaultValue;
	private T parsedValue;
	
	@Nullable
	private Setter<? super T> setter;
	
	public Option(final String key, final T defaultValue) {
		this.key = "" + key.toLowerCase(Locale.ENGLISH);
		this.defaultValue = defaultValue;
		parsedValue = defaultValue;
		@SuppressWarnings("unchecked")
		final Class<T> c = (Class<T>) defaultValue.getClass();
		if (c == String.class) {
			parser = new Converter<String, T>() {
				@SuppressWarnings("unchecked")
				@Override
				public T convert(final String s) {
					return (T) s;
				}
			};
		} else {
			final ClassInfo<T> ci = Classes.getExactClassInfo(c);
			final Parser<? extends T> p;
			if (ci == null || (p = ci.getParser()) == null)
				throw new IllegalArgumentException(c.getName());
			this.parser = new Converter<String, T>() {
				@Override
				@Nullable
				public T convert(final String s) {
					final T t = p.parse(s, ParseContext.CONFIG);
					if (t != null)
						return t;
					Skript.error("'" + s + "' is not " + ci.getName().withIndefiniteArticle());
					return null;
				}
			};
		}
	}
	
	public Option(final String key, final T defaultValue, final Converter<String, ? extends T> parser) {
		this.key = "" + key.toLowerCase(Locale.ENGLISH);
		this.defaultValue = defaultValue;
		parsedValue = defaultValue;
		this.parser = parser;
	}
	
	public final Option<T> setter(final Setter<? super T> setter) {
		this.setter = setter;
		return this;
	}
	
	public final Option<T> optional(final boolean optional) {
		this.optional = optional;
		return this;
	}
	
	public final void set(final Config config, final String path) {
		final String oldValue = value;
		value = config.getByPath(path + key);
		if (value == null && !optional)
			Skript.error("Required entry '" + path + key + "' is missing in " + config.getFileName() + ". Please make sure that you have the latest version of the config.");
		if (value == null ^ oldValue == null || value != null && !value.equals(oldValue)) {
			T parsedValue = value != null ? parser.convert(value) : defaultValue;
			if (parsedValue == null)
				parsedValue = defaultValue;
			this.parsedValue = parsedValue;
			onValueChange();
		}
	}
	
	protected void onValueChange() {
		if (setter != null)
			setter.set(parsedValue);
	}
	
	public final T value() {
		return parsedValue;
	}
	
	public final boolean isOptional() {
		return optional;
	}
	
}
