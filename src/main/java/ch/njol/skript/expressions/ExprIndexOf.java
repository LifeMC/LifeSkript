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

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Index Of")
@Description("The first or last index of a character (or text) in a text, or -1 if it doesn't occur in the text. Indices range from 1 to the <a href='#ExprIndexOf'>length</a> of the text.")
@Examples({"set {_@} to the first index of \"@\" in the text argument",
		"if {_s} contains \"abc\":",
		"	set {_s} to the first (index of \"abc\" in {_s} + 3) characters of {_s} # removes everything after the first \"abc\" from {_s}"})
@Since("2.1")
public class ExprIndexOf extends SimpleExpression<Integer> {
	static {
		Skript.registerExpression(ExprIndexOf.class, Integer.class, ExpressionType.COMBINED, "[the] (0¦|0¦first|1¦last) index of %string% in %string%");
	}
	
	boolean first;
	
	@SuppressWarnings("null")
	Expression<String> haystack, needle;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		first = parseResult.mark == 0;
		needle = (Expression<String>) exprs[0];
		haystack = (Expression<String>) exprs[1];
		return true;
	}
	
	@Override
	@Nullable
	protected Integer[] get(final Event e) {
		final String h = haystack.getSingle(e), n = needle.getSingle(e);
		if (h == null || n == null)
			return new Integer[0];
		final int i = first ? h.indexOf(n) : h.lastIndexOf(n);
		return new Integer[] {Integer.valueOf(i == -1 ? -1 : i + 1)};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the " + (first ? "first" : "last") + " index of " + needle.toString(e, debug) + " in " + haystack.toString(e, debug);
	}
	
}
