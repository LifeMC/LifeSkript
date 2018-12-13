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
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.World;
import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Time")
@Description("The <a href='../classes/#time'>time</a> of a world.")
@Examples({"time in world is between 18:00 and 6:00:", "	broadcast \"It's night-time, watch out for monsters!\""})
@Since("1.0")
public final class ExprTime extends PropertyExpression<World, Time> {
	static {
		Skript.registerExpression(ExprTime.class, Time.class, ExpressionType.PROPERTY, "[the] time [(in|of) %worlds%]", "%worlds%'[s] time");
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<World>) exprs[0]);
		return true;
	}
	
	@Override
	protected Time[] get(final Event e, final World[] source) {
		return get(source, new Getter<Time, World>() {
			@Override
			public Time get(final World w) {
				return new Time((int) w.getTime());
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
				return CollectionUtils.array(Timespan.class);
			case SET:
				return CollectionUtils.array(Time.class);
			case DELETE:
			case REMOVE_ALL:
			case RESET:
			default:
				return null;
		}
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		final World[] worlds = getExpr().getArray(e);
		int mod = 1;
		switch (mode) {
			case SET:
				assert delta != null;
				final Time time = (Time) delta[0];
				for (final World w : worlds) {
					w.setTime(time.getTicks());
				}
				break;
			case REMOVE:
				mod = -1;
				//$FALL-THROUGH$
			case ADD:
				assert delta != null;
				final Timespan ts = (Timespan) delta[0];
				for (final World w : worlds) {
					w.setTime(w.getTime() + mod * ts.getTicks_i());
				}
				break;
			case DELETE:
			case REMOVE_ALL:
			case RESET:
				assert false;
		}
	}
	
	@Override
	public Class<Time> getReturnType() {
		return Time.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		if (e == null)
			return "the time in " + getExpr().toString(e, debug);
		return Classes.getDebugMessage(getAll(e));
	}
	
}
