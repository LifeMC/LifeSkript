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
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Speed")
@Description({"A player's walking or flying speed. Both can be changed, but values must be between -1 and 1 (excessive values will be changed to -1 or 1 respectively). Negative values reverse directions.", "Please note that changing a player's speed will change his FOV just like potions do."})
@Examples({"set the player's walk speed to 1", "increase the argument's fly speed by 0.1"})
@Since("")
public class ExprSpeed extends SimplePropertyExpression<Player, Float> {
	static {
		register(ExprSpeed.class, Float.class, "(0¦walk[ing]|1¦fl(y[ing]|ight))[( |-])speed", "players");
	}
	
	private boolean walk;
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (!Skript.isRunningMinecraft(1, 4)) {
			Skript.error("fly and walk speed can only be used in Minecraft 1.4 and newer");
			return false;
		}
		super.init(exprs, matchedPattern, isDelayed, parseResult);
		walk = parseResult.mark == 0;
		return true;
	}
	
	@Override
	public Float convert(final Player p) {
		return walk ? p.getWalkSpeed() : p.getFlySpeed();
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return new Class[] {Number.class};
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		final float d = delta == null ? 0 : Math2.fit(-1, ((Number) delta[0]).floatValue(), 1);
		for (final Player p : getExpr().getArray(e)) {
			if (walk)
				p.setWalkSpeed(delta == null ? 0.2f : d);
			else
				p.setFlySpeed(delta == null ? 0.1f : d);
		}
	}
	
	@Override
	public Class<Float> getReturnType() {
		return Float.class;
	}
	
	@Override
	protected String getPropertyName() {
		return walk ? "walk speed" : "fly speed";
	}
	
}
