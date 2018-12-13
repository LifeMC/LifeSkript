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

package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class EvtPressurePlate extends SkriptEvent {
	static {
		// TODO is EntityInteractEvent similar for entities?
		Skript.registerEvent("Pressure Plate / Trip", EvtPressurePlate.class, PlayerInteractEvent.class, "[step[ping] on] [a] [pressure] plate", "(trip|[step[ping] on] [a] tripwire)").description("Called when a <i>player</i> steps on a pressure plate or tripwire respectively.").examples("").since("1.0 (pressure plate), 1.4.4 (tripwire)");
	}
	
	private boolean tripwire;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		tripwire = matchedPattern == 1;
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean check(final Event e) {
		// TODO !Update with every version [blocks]
		// 'type.getData() == PressurePlate.class' doesn't work for gold and iron pressure plates
		final Block b = ((PlayerInteractEvent) e).getClickedBlock();
		final Material type = b == null ? null : b.getType();
		return type != null && ((PlayerInteractEvent) e).getAction() == Action.PHYSICAL && (tripwire ? type == Material.TRIPWIRE || type == Material.TRIPWIRE_HOOK : type == Material.WOOD_PLATE || type == Material.STONE_PLATE || type.getId() == 147 || type.getId() == 148); // gold and iron pressure plates
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return tripwire ? "trip" : "stepping on a pressure plate";
	}
	
}
