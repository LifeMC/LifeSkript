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

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;

import org.bukkit.event.Event;

import java.lang.reflect.Array;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Parse")
@Description({"Parses text as a given type, or as a given pattern.", "This expression can be used in two different ways: One which parses the entire text as a single instance of a type, e.g. as a number, " + "and one that parses the text according to a pattern.", "If the given text could not be parsed, this expression will return nothing and the <a href='#ExprParseError'>parse error</a> will be set if some information is available.", "Some notes about parsing with a pattern:", "- The pattern must be a <a href='../patterns/'>Skript pattern</a>, " + "e.g. percent signs are used to define where to parse which types, e.g. put a %number% or %items% in the pattern if you expect a number or some items there.", "- You <i>have to</i> save the expression's value in a list variable, e.g. <code>set {parsed::*} to message parsed as \"...\"</code>.", "- The list variable will contain the parsed values from all %types% in the pattern in order. If a type was plural, e.g. %items%, the variable's value at the respective index will be a list variable," + " e.g. the values will be stored in {parsed::1::*}, not {parsed::1}."})
@Examples({"set {var} to line 1 parsed as number", "on chat:", "	set {var::*} to message parsed as \"buying %items% for %money%\"", "	if parse error is set:", "		message \"%parse error%\"", "	else if {var::*} is set:", "		cancel event", "		remove {var::2} from the player's balance", "		give {var::1::*} to the player"})
@Since("2.0")
public final class ExprParse extends SimpleExpression<Object> {
	static {
		Skript.registerExpression(ExprParse.class, Object.class, ExpressionType.COMBINED, "%string% parsed as (%-*classinfo%|\"<.*>\")");
	}
	
	@Nullable
	static String lastError;
	
	@SuppressWarnings("null")
	private Expression<String> text;
	
	@Nullable
	private String pattern;
	@Nullable
	private boolean[] plurals;
	
	@Nullable
	private ClassInfo<?> c;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		text = (Expression<String>) exprs[0];
		if (exprs[1] == null) {
			String pattern = "" + parseResult.regexes.get(0).group();
			if (!VariableString.isQuotedCorrectly(pattern, false)) {
				Skript.error("Invalid amount and/or placement of double quotes in '" + pattern + "'", ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
			// escape '¦'
			final StringBuilder b = new StringBuilder(pattern.length());
			for (int i = 0; i < pattern.length(); i++) {
				final char c = pattern.charAt(i);
				if (c == '\\') {
					b.append(c);
					b.append(pattern.charAt(i + 1));
					i++;
				} else if (c == '¦') {
					b.append("\\¦");
				} else {
					b.append(c);
				}
			}
			pattern = "" + b.toString();
			final NonNullPair<String, boolean[]> p = SkriptParser.validatePattern(pattern);
			if (p == null)
				return false;
			this.pattern = p.getFirst();
			plurals = p.getSecond();
		} else {
			c = ((Literal<ClassInfo<?>>) exprs[1]).getSingle();
			if (c.getC() == String.class) {
				Skript.error("Parsing as text is useless as only things that are already text may be parsed");
				return false;
			}
			final Parser<?> p = c.getParser();
			if (p == null || !p.canParse(ParseContext.COMMAND)) { // TODO special parse context?
				Skript.error("Text cannot be parsed as " + c.getName().withIndefiniteArticle(), ErrorQuality.SEMANTIC_ERROR);
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	@Nullable
	protected Object[] get(final Event e) {
		final String t = text.getSingle(e);
		if (t == null)
			return null;
		final ParseLogHandler h = SkriptLogger.startParseLogHandler();
		try {
			if (c != null) {
				final Parser<?> p = c.getParser();
				assert p != null; // checked in init()
				final Object o = p.parse(t, ParseContext.COMMAND);
				if (o != null) {
					final Object[] one = (Object[]) Array.newInstance(c.getC(), 1);
					one[0] = o;
					return one;
				}
			} else {
				assert pattern != null && plurals != null;
				final ParseResult r = SkriptParser.parse(t, pattern);
				if (r != null) {
					assert plurals.length == r.exprs.length;
					int resultCount = 0;
					for (final Expression<?> expr : r.exprs) {
						if (expr != null) // Ignore missing optional parts
							resultCount++;
					}

					final Object[] os = new Object[resultCount];
					for (int i = 0, slot = 0; i < r.exprs.length; i++) {
						if (r.exprs[i] != null)
							os[slot++] = plurals[i] ? r.exprs[i].getArray(null) : r.exprs[i].getSingle(null);
					}
					return os;
				}
			}
			final LogEntry err = h.getError();
			lastError = err != null ? err.getMessage() : null;
			return null;
		} finally {
			h.clear();
			h.printLog();
		}
	}
	
	@Override
	public boolean isSingle() {
		return pattern == null;
	}
	
	@Override
	public Class<?> getReturnType() {
		return c != null ? c.getC() : Object[].class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return text.toString(e, debug) + " parsed as " + (c != null ? c.toString(Language.F_INDEFINITE_ARTICLE) : pattern);
	}
	
}
