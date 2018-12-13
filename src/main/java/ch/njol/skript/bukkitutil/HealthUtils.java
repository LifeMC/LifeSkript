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

package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.util.Math2;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Peter Güttinger
 */

@SuppressWarnings("null")
public final class HealthUtils {
	
	private HealthUtils() {
		throw new UnsupportedOperationException();
	}
	
	private final static boolean supportsDoubles = Skript.methodExists(Damageable.class, "setHealth", double.class);
	private static Method getHealth, setHealth, getMaxHealth, setMaxHealth, damage;
	static {
		if (!supportsDoubles) {
			try {
				getHealth = Damageable.class.getDeclaredMethod("getHealth");
				setHealth = Damageable.class.getDeclaredMethod("setHealth", int.class);
				getMaxHealth = Damageable.class.getDeclaredMethod("getMaxHealth");
				setMaxHealth = Damageable.class.getDeclaredMethod("setMaxHealth", int.class);
				damage = Damageable.class.getDeclaredMethod("damage", int.class);
			} catch (final NoSuchMethodException e) {
				Skript.outdatedError(e);
			} catch (final SecurityException e) {
				Skript.exception(e);
			}
		}
	}
	
	/**
	 * @param e
	 * @return The amount of hearts the entity has left
	 */
	public static double getHealth(final LivingEntity e) {
		if (e.isDead())
			return 0;
		if (supportsDoubles)
			return e.getHealth() / 2;
		try {
			return ((Number) getHealth.invoke(e)).doubleValue() / 2;
		} catch (final IllegalAccessException ex) {
			Skript.exception(ex);
		} catch (final IllegalArgumentException ex) {
			Skript.outdatedError(ex);
		} catch (final InvocationTargetException ex) {
			Skript.exception(ex);
		}
		return 0;
	}
	
	/**
	 * @param e
	 * @param health The amount of hearts to set
	 */
	public static void setHealth(final LivingEntity e, final double health) {
		if (supportsDoubles) {
			e.setHealth(Math2.fit(0, health, getMaxHealth(e)) * 2);
			return;
		}
		try {
			setHealth.invoke(e, (int) Math.round(Math2.fit(0, health, getMaxHealth(e)) * 2));
		} catch (final IllegalAccessException ex) {
			Skript.exception(ex);
		} catch (final IllegalArgumentException ex) {
			Skript.outdatedError(ex);
		} catch (final InvocationTargetException ex) {
			Skript.exception(ex);
		}
	}
	
	/**
	 * @param e
	 * @return How many hearts the entity can have at most
	 */
	public static double getMaxHealth(final LivingEntity e) {
		if (supportsDoubles)
			return e.getMaxHealth() / 2;
		try {
			return ((Number) getMaxHealth.invoke(e)).doubleValue() / 2;
		} catch (final IllegalAccessException ex) {
			Skript.exception(ex);
		} catch (final IllegalArgumentException ex) {
			Skript.outdatedError(ex);
		} catch (final InvocationTargetException ex) {
			Skript.exception(ex);
		}
		return 0;
	}
	
	/**
	 * @param e
	 * @param health How many hearts the entity can have at most
	 */
	public static void setMaxHealth(final LivingEntity e, final double health) {
		if (supportsDoubles) {
			e.setMaxHealth(Math.max(Skript.EPSILON / 2, health * 2)); // 0 is not allowed, so just use a small value - smaller than Skript.EPSILON though to compare as 0
			return;
		}
		try {
			setMaxHealth.invoke(e, Math.max(1, (int) Math.round(health * 2)));
		} catch (final IllegalAccessException ex) {
			Skript.exception(ex);
		} catch (final IllegalArgumentException ex) {
			Skript.outdatedError(ex);
		} catch (final InvocationTargetException ex) {
			Skript.exception(ex);
		}
	}
	
	/**
	 * @param e
	 * @param d Amount of hearts to damage
	 */
	public static void damage(final LivingEntity e, final double d) {
		if (d < 0) {
			heal(e, -d);
			return;
		}
		if (supportsDoubles) {
			e.damage(d * 2);
			return;
		}
		try {
			damage.invoke(e, (int) Math.round(d * 2));
		} catch (final IllegalAccessException ex) {
			Skript.exception(ex);
		} catch (final IllegalArgumentException ex) {
			Skript.outdatedError(ex);
		} catch (final InvocationTargetException ex) {
			Skript.exception(ex);
		}
	}
	
	/**
	 * @param e
	 * @param h Amount of hearts to heal
	 */
	public static void heal(final LivingEntity e, final double h) {
		if (h < 0) {
			damage(e, -h);
			return;
		}
		setHealth(e, Math2.fit(0, getHealth(e) + h, getMaxHealth(e)));
	}
	
	private static Method getDamage, setDamage;
	static {
		if (!supportsDoubles) {
			try {
				getDamage = EntityDamageEvent.class.getDeclaredMethod("getDamage");
				setDamage = EntityDamageEvent.class.getDeclaredMethod("setDamage", int.class);
			} catch (final NoSuchMethodException e) {
				Skript.outdatedError(e);
			} catch (final SecurityException e) {
				Skript.exception(e);
			}
		}
	}
	
	public static double getDamage(final EntityDamageEvent e) {
		if (supportsDoubles)
			return e.getDamage() / 2;
		try {
			return ((Number) getDamage.invoke(e)).doubleValue() / 2;
		} catch (final IllegalAccessException ex) {
			Skript.exception(ex);
		} catch (final IllegalArgumentException ex) {
			Skript.outdatedError(ex);
		} catch (final InvocationTargetException ex) {
			Skript.exception(ex);
		}
		return 0;
	}
	
	public static void setDamage(final EntityDamageEvent e, final double damage) {
		if (supportsDoubles) {
			e.setDamage(damage * 2);
			return;
		}
		try {
			setDamage.invoke(e, (int) Math.round(damage * 2));
		} catch (final IllegalAccessException ex) {
			Skript.exception(ex);
		} catch (final IllegalArgumentException ex) {
			Skript.outdatedError(ex);
		} catch (final InvocationTargetException ex) {
			Skript.exception(ex);
		}
	}
	
}
