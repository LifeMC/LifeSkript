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

package ch.njol.skript.expressions;

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

import org.bukkit.event.Event;

import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Alphabetical Sort")
@Description("Sorts given strings in alphabetical order.")
@Examples({"set {list::*} to alphabetically sorted {list::*}"})
@Since("2.2-Fixes-V9c")
public class ExprAlphabetList extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprAlphabetList.class, String.class, ExpressionType.COMBINED, "alphabetically sorted %strings%");
	}
	
	@SuppressWarnings("null")
	private Expression<String> texts;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		texts = (Expression<String>) exprs[0];
		return true;
	}
	
	@Override
	@Nullable
	protected String[] get(final Event e) {
		final String[] sorted = texts.getAll(e).clone(); // Not yet sorted
		Arrays.sort(sorted); // Now sorted
		return sorted;
	}
	
	@Override
	public Class<String> getReturnType() {
		return String.class;
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return "alphabetically sorted strings: " + texts.toString(e, debug);
	}
	
}
