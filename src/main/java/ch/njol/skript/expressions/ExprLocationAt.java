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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * FIXME doesn't parse - update documentation when fixed
 * 
 * @author Peter Güttinger
 */
@Name("Location At")
@Description("Allows to create a <a href='../classes/#location'>location</a> from three coordinates and a world.")
@Examples({"set {_loc} to the location at arg-1, arg-2, arg-3 of the world arg-4", "distance between the player and the location (0, 0, 0) is less than 200"})
@Since("2.0")
public final class ExprLocationAt extends SimpleExpression<Location> {
	static {
		Skript.registerExpression(ExprLocationAt.class, Location.class, ExpressionType.COMBINED, "[the] (location|position) [at] [\\(][x[ ][=[ ]]]%number%, [y[ ][=[ ]]]%number%, [and] [z[ ][=[ ]]]%number%[\\)] [[(in|of) [[the] world]] %world%]");
	}
	
	@SuppressWarnings("null")
	private Expression<World> world;
	@SuppressWarnings("null")
	private Expression<Number> x, y, z;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		x = (Expression<Number>) exprs[0];
		y = (Expression<Number>) exprs[1];
		z = (Expression<Number>) exprs[2];
		world = (Expression<World>) exprs[3];
		return true;
	}
	
	@Override
	@Nullable
	protected Location[] get(final Event e) {
		final World w = world.getSingle(e);
		final Number x = this.x.getSingle(e), y = this.y.getSingle(e), z = this.z.getSingle(e);
		if (w == null || x == null || y == null || z == null)
			return new Location[0];
		return new Location[] {new Location(w, x.doubleValue(), y.doubleValue(), z.doubleValue())};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<Location> getReturnType() {
		return Location.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the location at (" + x.toString(e, debug) + ", " + y.toString(e, debug) + ", " + z.toString(e, debug) + ") in " + world.toString(e, debug);
	}
	
}
