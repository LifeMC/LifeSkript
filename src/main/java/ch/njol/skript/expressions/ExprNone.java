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

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author TheDGOfficial
 */
@Name("None")
@Description({"Represents the none (null) value."})
@Examples({"function send(msg: text, p: player = none value of player):", "if {_p} is set:", "send {_msg} to {_p}", "else:", "broadcast {_msg}"})
@Since("2.2-Fixes-V10c")
public final class ExprNone extends SimpleExpression<Object> {
	static {
		Skript.registerExpression(ExprNone.class, Object.class, ExpressionType.SIMPLE, "[the] (none|null) value of [the] [type] %*classinfo%");
	}
	
	@Nullable
	private Expression<?> noneType;
	
	@Override
	@SuppressWarnings("null")
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		noneType = exprs[0];
		return true;
	}
	
	@Override
	@Nullable
	protected Object[] get(final Event e) {
		return null;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	@SuppressWarnings("null")
	public Class<?> getReturnType() {
		return noneType.getReturnType();
	}
	
	@SuppressWarnings("null")
	public String toString(final @Nullable Event e, final boolean debug) {
		return "none value of " + getReturnType().getSimpleName().toLowerCase();
	}
}
