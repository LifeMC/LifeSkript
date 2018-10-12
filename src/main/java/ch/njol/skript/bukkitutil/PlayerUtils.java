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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Task;

/**
 * TODO check all updates and find out which ones are not required
 * 
 * @author Peter Güttinger
 */
public abstract class PlayerUtils {
	private PlayerUtils() {}
	
	final static Set<Player> inviUpdate = new HashSet<Player>();
	
	public final static void updateInventory(final @Nullable Player p) {
		if (p != null)
			inviUpdate.add(p);
	}
	
	// created when first used
	final static Task task = new Task(Skript.getInstance(), 1, 1) {
		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			try {
				for (final Player p : inviUpdate)
					p.updateInventory();
			} catch (final NullPointerException e) { // can happen on older CraftBukkit (Tekkit) builds
				if (Skript.debug())
					e.printStackTrace();
			}
			inviUpdate.clear();
		}
	};
	
	private final static boolean hasCollecionGetOnlinePlayers = Skript.methodExists(Bukkit.class, "getOnlinePlayers", new Class[0], Collection.class);
	@Nullable
	private static Method getOnlinePlayers = null;
	
	@SuppressWarnings({"null", "unchecked"})
	public final static Collection<? extends Player> getOnlinePlayers() {
		if (hasCollecionGetOnlinePlayers) {
			return Bukkit.getOnlinePlayers();
		} else {
			if (getOnlinePlayers == null) {
				try {
					getOnlinePlayers = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
				} catch (final NoSuchMethodException e) {
					Skript.outdatedError(e);
				} catch (final SecurityException e) {
					Skript.exception(e);
				}
			}
			try {
				final Object o = getOnlinePlayers.invoke(null);
				if (o instanceof Collection<?>)
					return (Collection<? extends Player>) o;
				else
					return Arrays.asList((Player[]) o);
			} catch (final IllegalAccessException e) {
				Skript.outdatedError(e);
			} catch (final IllegalArgumentException e) {
				Skript.outdatedError(e);
			} catch (final InvocationTargetException e) {
				Skript.exception(e);
			}
			return Collections.emptyList();
		}
	}
	
}
