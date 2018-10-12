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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.util.PlayerChatEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.util.Task;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public class EvtChat extends SelfRegisteringSkriptEvent {
	static {
		Skript.registerEvent("Chat", EvtChat.class, PlayerChatEventHandler.usesAsyncEvent ? AsyncPlayerChatEvent.class : PlayerChatEvent.class, "chat")
				.description("Called whenever a player chats.")
				.examples("")
				.since("1.4.1");
	}
	
	final static Collection<Trigger> triggers = new ArrayList<Trigger>();
	
	private static boolean registeredExecutor = false;
	private final static EventExecutor executor = new EventExecutor() {
		
		final void execute(final Event e) {
			SkriptEventHandler.logEventStart(e);
			for (final Trigger t : triggers) {
				assert t != null : triggers;
				SkriptEventHandler.logTriggerStart(t);
				t.execute(e);
				SkriptEventHandler.logTriggerEnd(t);
			}
			SkriptEventHandler.logEventEnd();
		}
		
		@Override
		public void execute(final @Nullable Listener l, final @Nullable Event e) throws EventException {
			if (e == null)
				return;
			if (!triggers.isEmpty()) {
				if (e instanceof PlayerChatEvent || !e.isAsynchronous()) {
					execute(e);
					return;
				}
				Task.callSync(new Callable<Void>() {
					@Override
					@Nullable
					public Void call() throws Exception {
						execute(e);
						return null;
					}
				});
			}
		}
	};
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "chat";
	}
	
	@Override
	public void register(final Trigger t) {
		triggers.add(t);
		if (!registeredExecutor) {
			PlayerChatEventHandler.registerChatEvent(SkriptConfig.defaultEventPriority.value(), executor, true);
			registeredExecutor = true;
		}
	}
	
	@Override
	public void unregister(final Trigger t) {
		triggers.remove(t);
	}
	
	@Override
	public void unregisterAll() {
		triggers.clear();
	}
	
}
