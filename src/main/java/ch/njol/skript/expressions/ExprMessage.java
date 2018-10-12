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

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.events.util.PlayerChatEventHandler;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
@Name("Message")
@Description("The (chat) message of a chat event, the join message of a join event, the quit message of a quit event, or the death message on a death event. This expression is mostly useful for being changed.")
@Examples({"on chat:",
		"	player has permission \"admin\"",
		"	set message to \"<red>%message%\"",
		"",
		"on first join:",
		"	set join message to \"Welcome %player% to our awesome server!\"",
		"on join:",
		"	player has played before",
		"	set join message to \"Welcome back, %player%!\"",
		"",
		"on quit:",
		"	set quit message to \"%player% left this awesome server!\"",
		"",
		"on death:",
		"	set the death message to \"%player% died!\""})
@Since("1.4.6 (chat message), 1.4.9 (join & quit messages), 2.0 (death message)")
@Events({"chat", "join", "quit", "death"})
public class ExprMessage extends SimpleExpression<String> {
	
	@SuppressWarnings("unchecked")
	private static enum MessageType {
		CHAT("chat", "[chat( |-)]message", PlayerChatEventHandler.usesAsyncEvent ? AsyncPlayerChatEvent.class : PlayerChatEvent.class) {
			@Override
			@Nullable
			String get(final Event e) {
				if (PlayerChatEventHandler.usesAsyncEvent)
					return ((AsyncPlayerChatEvent) e).getMessage();
				else
					return ((PlayerChatEvent) e).getMessage();
			}
			
			@Override
			void set(final Event e, final String message) {
				if (PlayerChatEventHandler.usesAsyncEvent)
					((AsyncPlayerChatEvent) e).setMessage(message);
				else
					((PlayerChatEvent) e).setMessage(message);
			}
		},
		JOIN("join", "(join|log[ ]in)( |-)message", PlayerJoinEvent.class) {
			@Override
			@Nullable
			String get(final Event e) {
				return ((PlayerJoinEvent) e).getJoinMessage();
			}
			
			@Override
			void set(final Event e, final String message) {
				((PlayerJoinEvent) e).setJoinMessage(message);
			}
		},
		QUIT("quit", "(quit|leave|log[ ]out|kick)( |-)message", PlayerQuitEvent.class, PlayerKickEvent.class) {
			@Override
			@Nullable
			String get(final Event e) {
				if (e instanceof PlayerKickEvent)
					return ((PlayerKickEvent) e).getLeaveMessage();
				else
					return ((PlayerQuitEvent) e).getQuitMessage();
			}
			
			@Override
			void set(final Event e, final String message) {
				if (e instanceof PlayerKickEvent)
					((PlayerKickEvent) e).setLeaveMessage(message);
				else
					((PlayerQuitEvent) e).setQuitMessage(message);
			}
		},
		DEATH("death", "death( |-)message", EntityDeathEvent.class) {
			@Override
			@Nullable
			String get(final Event e) {
				if (e instanceof PlayerDeathEvent)
					return ((PlayerDeathEvent) e).getDeathMessage();
				return null;
			}
			
			@Override
			void set(final Event e, final String message) {
				if (e instanceof PlayerDeathEvent)
					((PlayerDeathEvent) e).setDeathMessage(message);
			}
		};
		
		final String name;
		private final String pattern;
		final Class<? extends Event>[] events;
		
		MessageType(final String name, final String pattern, final Class<? extends Event>... events) {
			this.name = name;
			this.pattern = "[the] " + pattern;
			this.events = events;
		}
		
		static String[] patterns;
		static {
			patterns = new String[values().length];
			for (int i = 0; i < patterns.length; i++)
				patterns[i] = values()[i].pattern;
		}
		
		@Nullable
		abstract String get(Event e);
		
		abstract void set(Event e, String message);
		
	}
	
	static {
		Skript.registerExpression(ExprMessage.class, String.class, ExpressionType.SIMPLE, MessageType.patterns);
	}
	
	@SuppressWarnings("null")
	private MessageType type;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		type = MessageType.values()[matchedPattern];
		if (!ScriptLoader.isCurrentEvent(type.events)) {
			Skript.error("The " + type.name + " message can only be used in a " + type.name + " event", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}
	
	@Override
	protected String[] get(final Event e) {
		for (final Class<? extends Event> c : type.events) {
			if (c.isInstance(e))
				return new String[] {type.get(e)};
		}
		return new String[0];
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(String.class);
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		assert mode == ChangeMode.SET;
		assert delta != null;
		for (final Class<? extends Event> c : type.events) {
			if (c.isInstance(e))
				type.set(e, "" + delta[0]);
		}
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the " + type.name + " message";
	}
	
}
