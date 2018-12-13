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

package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Open/Close Inventory")
@Description({"Opens an inventory to a player. The player can then access and modify the inventory as if it was a chest that he just opened.", "Please note that currently 'show' and 'open' have the same effect, but 'show' will eventually show an unmodifiable view of the inventory in the future."})
@Examples({"show the victim's inventory to the player", "open the player's inventory for the player"})
@Since("2.0, 2.1.1 (closing)")
public final class EffOpenInventory extends Effect {
	static {
		Skript.registerEffect(EffOpenInventory.class, "(0¦open|1¦show) ((crafting [table]|workbench) (view|window|inventory|)|%-inventory%) (to|for) %players%", "close [the] inventory [view] (to|of|for) %players%", "close %players%'[s] inventory [view]");
	}
	
	@Nullable
	private Expression<Inventory> invi;
	
	boolean open;
	
	@SuppressWarnings("null")
	private Expression<Player> players;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		open = matchedPattern == 0;
		invi = open ? (Expression<Inventory>) exprs[0] : null;
		players = (Expression<Player>) exprs[exprs.length - 1];
		if (parseResult.mark == 1 && invi != null) {
			Skript.warning("Using 'show' inventory instead of 'open' is not recommended as it will eventually show an unmodifiable view of the inventory in the future.");
		}
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		if (invi != null) {
			final Inventory i = invi.getSingle(e);
			if (i == null)
				return;
			for (final Player p : players.getArray(e)) {
				p.openInventory(i);
			}
		} else {
			for (final Player p : players.getArray(e)) {
				if (open)
					p.openWorkbench(null, true);
				else
					p.closeInventory();
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (open ? "open " + (invi != null ? invi.toString(e, debug) : "crafting table") + " to " : "close inventory view of ") + players.toString(e, debug);
	}
	
}
