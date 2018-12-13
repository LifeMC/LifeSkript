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
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Damage Cause")
@Description("Tests what kind of damage caused a <a href='../events/#damage'>damage event</a>. Refer to the <a href='../classes/#damagecause'>Damage Cause</a> type for a list of all possible causes.")
@Examples({"# make players use their potions of fire resistance whenever they take any kind of fire damage", "on damage:", "	damage was caused by lava, fire or burning", "	victim is a player", "	victim has a potion of fire resistance", "	cancel event", "	apply fire resistance to the victim for 30 seconds", "	remove 1 potion of fire resistance from the victim", "# prevent mobs from dropping items under certain circumstances", "on death;", "	entity is not a player", "	damage wasn't caused by a block explosion, an attack, a projectile, a potion, fire, burning, thorns or poison", "	clear drops"})
@Since("2.0")
public final class CondDamageCause extends Condition {
	static {
		Skript.registerCondition(CondDamageCause.class, "[the] damage (was|is|has)(0¦|1¦n('|o)t) [been] (caused|done|made) by %damagecause%");
	}
	
	@SuppressWarnings("null")
	private Expression<DamageCause> cause, expected;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		cause = new EventValueExpression<DamageCause>(DamageCause.class);
		expected = (Expression<DamageCause>) exprs[0];
		setNegated(parseResult.mark == 1);
		return ((EventValueExpression<DamageCause>) cause).init();
	}
	
	@Override
	public boolean check(final Event e) {
		final DamageCause c = cause.getSingle(e);
		if (c == null)
			return false;
		return expected.check(e, new Checker<DamageCause>() {
			@Override
			public boolean check(final DamageCause o) {
				return c == o;
			}
		}, isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "damage was" + (isNegated() ? " not" : "") + " caused by " + expected.toString(e, debug);
	}
	
}
