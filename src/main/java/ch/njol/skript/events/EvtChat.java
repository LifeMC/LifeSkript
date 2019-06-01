/*
 *
 *     This file is part of Skript.
 *
 *    Skript is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Skript is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Skript. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 *   Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.util.PlayerChatEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.util.Task;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.EventExecutor;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public final class EvtChat extends SelfRegisteringSkriptEvent {
    static final Collection<Trigger> triggers = new ArrayList<>();
    private static final EventExecutor executor = new EventExecutor() {

        final void execute(final Event e) {
            final boolean flag = Skript.logVeryHigh();
            if (flag)
                SkriptEventHandler.logEventStart(e);
            for (final Trigger t : triggers) {
                assert t != null : triggers;
                if (flag)
                    SkriptEventHandler.logTriggerStart(t);
                t.execute(e);
                if (flag)
                    SkriptEventHandler.logTriggerEnd(t);
            }
            if (flag)
                SkriptEventHandler.logEventEnd();
        }

        @Override
        public final void execute(final @Nullable Listener l, final @Nullable Event e) throws EventException {
            if (e == null)
                return;
            if (!triggers.isEmpty()) {
                if (e instanceof PlayerChatEvent || !e.isAsynchronous()) {
                    execute(e);
                    return;
                }
                Task.callSync((Callable<Void>) () -> {
                    execute(e);
                    return null;
                });
            }
        }
    };
    private static boolean registeredExecutor;

    static {
        Skript.registerEvent("Chat", EvtChat.class, PlayerChatEventHandler.usesAsyncEvent ? AsyncPlayerChatEvent.class : PlayerChatEvent.class, "chat").description("Called whenever a player chats.").examples("").since("1.4.1");
    }

    @Override
    public final boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
        return true;
    }

    @Override
    public final String toString(final @Nullable Event e, final boolean debug) {
        return "chat";
    }

    @Override
    public final void register(final Trigger t) {
        triggers.add(t);
        if (!registeredExecutor) {
            PlayerChatEventHandler.registerChatEvent(SkriptConfig.defaultEventPriority.value(), executor, true);
            registeredExecutor = true;
        }
    }

    @Override
    public final void unregister(final Trigger t) {
        triggers.remove(t);
    }

    @Override
    public final void unregisterAll() {
        triggers.clear();
    }

}
