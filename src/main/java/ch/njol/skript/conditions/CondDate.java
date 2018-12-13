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

package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Time")
@Description("Tests whether a given <a href='../classes/#date'>real time</a> was more or less than some <a href='../classes/#timespan'>time span</a> ago.")
@Examples({"command /command_with_cooldown:", "	trigger:", "		{command.%player%.lastused} was less than a minute ago:", "			message \"Please wait a minute between uses of this command.\"", "			stop", "		set {command.%player%.lastused} to now", "		# ... actual command trigger here ..."})
@Since("2.0")
public final class CondDate extends Condition {
	static {
		Skript.registerCondition(CondDate.class, "%date% (was|were)( more|(n't| not) less) than %timespan% [ago]", "%date% (was|were)((n't| not) more| less) than %timespan% [ago]");
	}
	
	@SuppressWarnings("null")
	private Expression<Date> date;
	@SuppressWarnings("null")
	Expression<Timespan> delta;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		date = (Expression<Date>) exprs[0];
		delta = (Expression<Timespan>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		final long now = System.currentTimeMillis();
		return date.check(e, new Checker<Date>() {
			@Override
			public boolean check(final Date d) {
				return delta.check(e, new Checker<Timespan>() {
					@Override
					public boolean check(final Timespan t) {
						return now - d.getTimestamp() >= t.getMilliSeconds();
					}
				}, isNegated());
			}
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return date.toString(e, debug) + " was " + (isNegated() ? "less" : "more") + " than " + delta.toString(e, debug) + " ago";
	}
	
}
