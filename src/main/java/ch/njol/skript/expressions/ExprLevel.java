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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Level")
@Description("The level of a player.")
@Examples({"reduce the victim's level by 1", "set the player's level to 0"})
@Since("")
@Events("level change")
public class ExprLevel extends SimplePropertyExpression<Player, Integer> {
	static {
		register(ExprLevel.class, Integer.class, "level", "players");
	}
	
	@Override
	protected Integer[] get(final Event e, final Player[] source) {
		return super.get(source, new Converter<Player, Integer>() {
			@Override
			public Integer convert(final Player p) {
				if (e instanceof PlayerLevelChangeEvent && ((PlayerLevelChangeEvent) e).getPlayer() == p && !Delay.isDelayed(e)) {
					return getTime() < 0 ? ((PlayerLevelChangeEvent) e).getOldLevel() : ((PlayerLevelChangeEvent) e).getNewLevel();
				}
				return p.getLevel();
			}
		});
	}
	
	@Override
	@Nullable
	public Integer convert(final Player p) {
		assert false;
		return null;
	}
	
	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		if (ScriptLoader.isCurrentEvent(PlayerRespawnEvent.class) && !ScriptLoader.hasDelayBefore.isTrue()) {
			Skript.error("Cannot change a player's level in a respawn event. Add a delay of 1 tick or change the 'new level' in a death event.");
			return null;
		}
		if (ScriptLoader.isCurrentEvent(PlayerDeathEvent.class) && getTime() == 0 && getExpr().isDefault() && !ScriptLoader.hasDelayBefore.isTrue()) {
			Skript.warning("Changing the player's level in a death event will change the player's level before he dies. " + "Use either 'past level of player' or 'new level of player' to clearly state whether to change the level before or after he dies.");
		}
		if (getTime() == -1 && !ScriptLoader.isCurrentEvent(PlayerDeathEvent.class))
			return null;
		return new Class[] {Number.class};
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert mode != ChangeMode.REMOVE_ALL;
		
		final int l = delta == null ? 0 : ((Number) delta[0]).intValue();
		
		for (final Player p : getExpr().getArray(e)) {
			int level;
			if (getTime() > 0 && e instanceof PlayerDeathEvent && ((PlayerDeathEvent) e).getEntity() == p && !Delay.isDelayed(e)) {
				level = ((PlayerDeathEvent) e).getNewLevel();
			} else {
				level = p.getLevel();
			}
			switch (mode) {
				case SET:
					level = l;
					break;
				case ADD:
					level += l;
					break;
				case REMOVE:
					level -= l;
					break;
				case DELETE:
				case RESET:
					level = 0;
					break;
				case REMOVE_ALL:
					assert false;
					continue;
			}
			if (getTime() > 0 && e instanceof PlayerDeathEvent && ((PlayerDeathEvent) e).getEntity() == p && !Delay.isDelayed(e)) {
				((PlayerDeathEvent) e).setNewLevel(level);
			} else {
				p.setLevel(level);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, getExpr(), PlayerLevelChangeEvent.class, PlayerDeathEvent.class);
	}
	
	@Override
	protected String getPropertyName() {
		return "level";
	}
	
}
