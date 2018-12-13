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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.lang.function;

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

import org.eclipse.jdt.annotation.Nullable;

public final class Parameter<T> {
	
	final String name;
	
	final ClassInfo<T> type;
	
	@Nullable
	final Expression<? extends T> def;
	
	final boolean single;
	final boolean isNone;
	
	@SuppressWarnings("null")
	public Parameter(final String name, final ClassInfo<T> type, final boolean single, final @Nullable Expression<? extends T> def) {
		this.name = name != null ? name.toLowerCase() : null;
		this.type = type;
		this.def = def;
		this.single = single;
		this.isNone = false;
	}
	
	@SuppressWarnings("null")
	public Parameter(final String name, final ClassInfo<T> type, final boolean single, final @Nullable Expression<? extends T> def, final boolean isNone) {
		this.name = name != null ? name.toLowerCase() : null;
		this.type = type;
		this.def = def;
		this.single = single;
		this.isNone = isNone;
	}
	
	public ClassInfo<T> getType() {
		return type;
	}
	
	@Nullable
	public static <T> Parameter<T> newInstance(final String name, final ClassInfo<T> type, final boolean single, final @Nullable String def) {
		if (def != null) {
			final boolean isNone = (def.contains("none") || def.contains("null")) && def.contains("value of");
			return newInstance(name, type, single, def, isNone);
		} else {
			return newInstance(name, type, single, def, false);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> Parameter<T> newInstance(final String name, final ClassInfo<T> type, final boolean single, final @Nullable String def, final boolean isNone) {
		if (!Variable.isValidVariableName(name, false, false)) {
			Skript.error("An argument's name must be a valid variable name, and cannot be a list variable.");
			return null;
		}
		Expression<? extends T> d = null;
		if (def != null) {
//			if (def.startsWith("%") && def.endsWith("%")) {
//				final RetainingLogHandler log = SkriptLogger.startRetainingLog();
//				try {
//					d = new SkriptParser("" + def.substring(1, def.length() - 1), SkriptParser.PARSE_EXPRESSIONS, ParseContext.FUNCTION_DEFAULT).parseExpression(type.getC());
//					if (d == null) {
//						log.printErrors("Can't understand this expression: " + def + "");
//						return null;
//					}
//					log.printLog();
//				} finally {
//					log.stop();
//				}
//			} else {
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
					if (!isNone) {
						log.printErrors("'" + def + "' is not " + type.getName().withIndefiniteArticle());
						return null;
					}
				}
				log.printLog();
			} finally {
				log.stop();
			}
//			}
		}
		return new Parameter<T>(name, type, single, d, isNone);
	}
	
	@Override
	public String toString() {
		return name + ": " + Utils.toEnglishPlural(type.getCodeName(), !single) + (def != null ? " = " + def.toString(null, true) : "");
	}
	
}
