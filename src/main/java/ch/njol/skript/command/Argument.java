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

package ch.njol.skript.command;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;

import org.bukkit.event.Event;

import java.util.WeakHashMap;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents an argument of a command
 * 
 * @author Peter Güttinger
 */
public final class Argument<T> {
	
	@Nullable
	private final String name;
	
	@Nullable
	private final Expression<? extends T> def;
	
	private final ClassInfo<T> type;
	private final boolean single;
	
	private final int index;
	
	private final boolean optional;
	
	private final transient WeakHashMap<Event, T[]> current = new WeakHashMap<Event, T[]>();
	
	private Argument(@Nullable final String name, final @Nullable Expression<? extends T> def, final ClassInfo<T> type, final boolean single, final int index, final boolean optional) {
		this.name = name;
		this.def = def;
		this.type = type;
		this.single = single;
		this.index = index;
		this.optional = optional;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> Argument<T> newInstance(@Nullable final String name, final ClassInfo<T> type, final @Nullable String def, final int index, final boolean single, final boolean forceOptional) {
		if (name != null && !Variable.isValidVariableName(name, false, false)) {
			Skript.error("An argument's name must be a valid variable name, and cannot be a list variable.");
			return null;
		}
		Expression<? extends T> d = null;
		if (def != null) {
			if (def.startsWith("%") && def.endsWith("%")) {
				final RetainingLogHandler log = SkriptLogger.startRetainingLog();
				try {
					d = new SkriptParser("" + def.substring(1, def.length() - 1), SkriptParser.PARSE_EXPRESSIONS, ParseContext.COMMAND).parseExpression(type.getC());
					if (d == null) {
						log.printErrors("Can't understand this expression: " + def + "");
						return null;
					}
					log.printLog();
				} finally {
					log.stop();
				}
			} else {
				final RetainingLogHandler log = SkriptLogger.startRetainingLog();
				try {
					if (type.getC() == String.class) {
						if (def.startsWith("\"") && def.endsWith("\""))
							d = (Expression<? extends T>) VariableString.newInstance("" + def.substring(1, def.length() - 1));
						else
							d = (Expression<? extends T>) new SimpleLiteral<String>(def, false);
					} else {
						d = new SkriptParser(def, SkriptParser.PARSE_LITERALS, ParseContext.DEFAULT).parseExpression(type.getC());
					}
					if (d == null) {
						log.printErrors("Can't understand this expression: '" + def + "'");
						return null;
					}
					log.printLog();
				} finally {
					log.stop();
				}
			}
		}
		return new Argument<T>(name, d, type, single, index, def != null || forceOptional);
	}
	
	@Override
	public String toString() {
		final Expression<? extends T> def = this.def;
		return "<" + (name != null ? name + ": " : "") + Utils.toEnglishPlural(type.getCodeName(), !single) + (def == null ? "" : " = " + def.toString()) + ">";
	}
	
	public boolean isOptional() {
		return optional;
	}
	
	public void setToDefault(final ScriptCommandEvent event) {
		if (def != null)
			set(event, def.getArray(event));
	}
	
	@SuppressWarnings("unchecked")
	public void set(final ScriptCommandEvent e, final Object[] o) {
		if (!type.getC().isAssignableFrom(o.getClass().getComponentType()))
			throw new IllegalArgumentException();
		current.put(e, (T[]) o);
		final String name = this.name;
		if (name != null) {
			if (single) {
				if (o.length > 0)
					Variables.setVariable(name, o[0], e, true);
			} else {
				for (int i = 0; i < o.length; i++)
					Variables.setVariable(name + "::" + (i + 1), o[i], e, true);
			}
		}
	}
	
	@Nullable
	public T[] getCurrent(final Event e) {
		return current.get(e);
	}
	
	public Class<T> getType() {
		return type.getC();
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isSingle() {
		return single;
	}
	
}
