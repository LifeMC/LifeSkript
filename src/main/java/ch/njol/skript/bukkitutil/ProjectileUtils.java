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

package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("null")
public final class ProjectileUtils {
	
	private ProjectileUtils() {
		throw new UnsupportedOperationException();
	}
	
	private static Method getShooter, setShooter;
	static {
		try {
			getShooter = Projectile.class.getMethod("getShooter");
			try {
				setShooter = Projectile.class.getMethod("setShooter", ProjectileSource.class);
			} catch (final NoClassDefFoundError e) {
				setShooter = Projectile.class.getMethod("setShooter", LivingEntity.class);
			}
		} catch (final NoSuchMethodException e) {
			Skript.outdatedError(e);
		} catch (final SecurityException e) {
			Skript.exception(e, "security manager present");
		}
	}
	
	@Nullable
	public static Object getShooter(final @Nullable Projectile p) {
		if (p == null)
			return null;
		try {
			return getShooter.invoke(p);
		} catch (final IllegalAccessException e) {
			assert false;
			return null;
		} catch (final IllegalArgumentException e) {
			assert false;
			return null;
		} catch (final InvocationTargetException e) {
			Skript.exception(e);
			return null;
		}
	}
	
	public static void setShooter(final Projectile p, final @Nullable Object shooter) {
		try {
			setShooter.invoke(p, shooter);
		} catch (final IllegalAccessException e) {
			assert false;
		} catch (final IllegalArgumentException e) {
			Skript.exception(e, "invalid parameter passed to (" + p + ").setShooter: " + shooter);
		} catch (final InvocationTargetException e) {
			Skript.exception(e);
		}
	}
	
}
