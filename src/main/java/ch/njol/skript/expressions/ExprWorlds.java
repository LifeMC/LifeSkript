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
import ch.njol.skript.events.EvtAtTime;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Worlds")
@Description("All worlds of the server, useful for looping.")
@Examples({"loop all worlds:", "	broadcast \"You're in %loop-world%\" to loop-world"})
@Since("1.0")
public final class ExprWorlds extends SimpleExpression<World> {
	
	static {
		Skript.registerExpression(ExprWorlds.class, World.class, ExpressionType.SIMPLE, "[(the|all)] worlds");
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return Bukkit.getWorlds().size() == 1;
	}
	
	@Override
	public Class<World> getReturnType() {
		return World.class;
	}
	
	@Override
	@Nullable
	protected World[] get(final Event e) {
		return Bukkit.getWorlds().toArray(EvtAtTime.EMPTY_WORLD_ARRAY);
	}
	
	@Override
	@Nullable
	public Iterator<World> iterator(final Event e) {
		return Bukkit.getWorlds().iterator();
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "worlds";
	}
	
}
