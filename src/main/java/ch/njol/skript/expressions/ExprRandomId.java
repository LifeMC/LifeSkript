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

import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author TheDGOfficial
 */
@Name("Random UUID")
@Description("Gets a random UUID.")
@Examples("set {id} to random id")
@Since("2.2-Fixes-V10c")
public final class ExprRandomId extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprRandomId.class, String.class, ExpressionType.SIMPLE, "[a] [new] random [uu]id");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		return true;
	}
	
	@Override
	protected String[] get(final Event e) {
		return new String[] {UUID.randomUUID().toString()};
	}
	
	@Override
	public Class<String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "random uuid";
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
}
