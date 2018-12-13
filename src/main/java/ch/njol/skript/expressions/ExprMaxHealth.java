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
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Max Health")
@Description("The maximum health of an entity, e.g. 10 for a player")
@Examples({"on join:", "	set the maximum health of the player to 100", "spawn a giant", "set the last spawned entity's max health to 1000"})
@Since("2.0")
@Events({"damage", "death"})
public class ExprMaxHealth extends SimplePropertyExpression<LivingEntity, Double> {
	static {
		register(ExprMaxHealth.class, Double.class, "max[imum] health", "livingentities");
	}
	
	@Override
	public Double convert(final LivingEntity e) {
		return HealthUtils.getMaxHealth(e);
	}
	
	@Override
	public Class<Double> getReturnType() {
		return Double.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "max health";
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (!Skript.methodExists(Damageable.class, "setMaxHealth", double.class)) {
			Skript.error("The max health of an entity can only be changed in Minecraft 1.6 and later");
			return null;
		}
		
		if (mode != ChangeMode.DELETE && mode != ChangeMode.REMOVE_ALL)
			return new Class[] {Number.class};
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		double d = delta == null ? 0 : ((Number) delta[0]).doubleValue();
		for (final LivingEntity en : getExpr().getArray(e)) {
			assert en != null : getExpr();
			switch (mode) {
				case SET:
					HealthUtils.setMaxHealth(en, d);
					break;
				case REMOVE:
					d = -d;
					//$FALL-THROUGH$
				case ADD:
					HealthUtils.setMaxHealth(en, HealthUtils.getMaxHealth(en) + d);
					break;
				case RESET:
					en.resetMaxHealth();
					break;
				case DELETE:
				case REMOVE_ALL:
					assert false;
					
			}
		}
	}
	
}
