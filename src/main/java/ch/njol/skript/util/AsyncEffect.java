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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Effects that extend this class are ran asynchronously. Next trigger item will be ran
 * in main server thread, as if there had been a delay before.
 * <p>
 * Majority of Skript and Minecraft APIs are not thread-safe, so be careful.
 */
public abstract class AsyncEffect extends Effect {
	
	@Override
	@Nullable
	protected TriggerItem walk(final Event e) {
		debug(e, true);
		final TriggerItem next = getNext();
		Delay.addDelayedEvent(e);
		Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), new Runnable() {
			@Override
			@SuppressWarnings("synthetic-access")
			public final void run() {
				execute(e); // Execute this effect
				if (next != null) {
					Bukkit.getScheduler().runTask(Skript.getInstance(), new Runnable() {
						@Override
						public final void run() { // Walk to next item synchronously
							TriggerItem.walk(next, e);
						}
					});
				}
			}
		});
		return null;
	}
	
}
