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

package ch.njol.skript.expressions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Mirreducki, Eugenio GuzmÃ¡n
 */
public class ExprChatRecipients extends SimpleExpression<Player> {
	
	static {
		Skript.registerExpression(ExprChatRecipients.class, Player.class, ExpressionType.SIMPLE, "[chat][( |-)]recipients");
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<Player> getReturnType() {
		return Player.class;
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.ADD || mode == ChangeMode.SET || mode == ChangeMode.DELETE || mode == ChangeMode.REMOVE)
			return CollectionUtils.array(Player.class, Player[].class);
		return null;
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(AsyncPlayerChatEvent.class)) {
			Skript.error("Cannot use chat recipients expression outside of a chat event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return "chat recipients";
	}
	
	@Override
	@Nullable
	protected Player[] get(final Event e) {
		final AsyncPlayerChatEvent ae = (AsyncPlayerChatEvent) e;
		final Set<Player> playerSet = ae.getRecipients();
		return playerSet.toArray(new Player[0]);
	}
	
	@SuppressWarnings({"incomplete-switch", "null"})
	@Override
	public void change(final Event e, @Nullable final Object[] delta, final ChangeMode mode) {
		final Player[] playerArray = (Player[]) delta;
		final AsyncPlayerChatEvent a = (AsyncPlayerChatEvent) e;
		switch (mode) {
			case REMOVE:
				for (final Player p : playerArray)
					a.getRecipients().remove(p);
				break;
			case DELETE:
				a.getRecipients().clear();
				break;
			case ADD:
				for (final Player p : playerArray)
					a.getRecipients().add(p);
				break;
			case SET:
				a.getRecipients().clear();
				for (final Player p : playerArray)
					a.getRecipients().add(p);
				break;
		}
	}
}
