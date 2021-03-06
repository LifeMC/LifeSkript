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

package ch.njol.skript;

import ch.njol.skript.ScriptLoader.ScriptInfo;
import ch.njol.skript.command.Commands;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.timings.SkriptTimings;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.EventExecutor;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.util.*;

/**
 * @author Peter Güttinger
 */
public final class SkriptEventHandler {

    public static final long moveEventCooldown = Long.getLong("skript.moveEventCooldown", /*100L*/0L);
    public static final EventExecutor ee = new SkriptEventExecutor();
    static final Map<Class<? extends Event>, List<Trigger>> triggers = new HashMap<>(100);
    static final long eventCooldown = Long.getLong("skript.eventCooldown", /*100L*/0L);
    private static final Listener listener = new EmptyListener();
    private static final List<Trigger> selfRegisteredTriggers = new ArrayList<>(100);
    /**
     * Stores which events are currently registered with Bukkit
     */
    private static final Set<Class<? extends Event>> registeredEvents = new HashSet<>(100);
    @Nullable
    public static Event last;
    static long startTrigger;
    static long lastCall;
    private static long startEvent;

    private SkriptEventHandler() {
        throw new UnsupportedOperationException();
    }

    private static final Iterator<Trigger> getTriggers(final Class<? extends Event> event) {
        return new Iterator<Trigger>() {
            @Nullable
            private Class<?> e = event;

            @Nullable
            private Iterator<Trigger> current;

            @Override
            public boolean hasNext() {
                Iterator<Trigger> current = this.current;
                Class<?> e = this.e;
                while (current == null || !current.hasNext()) {
                    if (e == null || !Event.class.isAssignableFrom(e))
                        return false;
                    @SuppressWarnings("unlikely-arg-type") final List<Trigger> l = triggers.get(e);
                    this.current = current = l == null ? null : l.iterator();
                    this.e = e = e.getSuperclass();
                }
                return true;
            }

            @Override
            public Trigger next() {
                final Iterator<Trigger> current = this.current;
                if (current == null || !hasNext())
                    throw new NoSuchElementException();
                final Trigger next = current.next();
                assert next != null;
                return next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    static final void check(final Event e) {
        Iterator<Trigger> ts = getTriggers(e.getClass());
        if (!ts.hasNext())
            return;

        final boolean logVeryHigh = Skript.logVeryHigh();

        if (logVeryHigh) {
            boolean hasTrigger = false;
            while (ts.hasNext()) {
                if (ts.next().getEvent().check(e)) {
                    hasTrigger = true;
                    break;
                }
            }
            if (!hasTrigger)
                return;
            final Class<? extends Event> c = e.getClass();
            assert c != null;
            ts = getTriggers(c);

            logEventStart(e);
        }

        if (e instanceof Cancellable && ((Cancellable) e).isCancelled() && !(e instanceof PlayerInteractEvent && (((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_AIR) && ((PlayerInteractEvent) e).useItemInHand() != Result.DENY) || e instanceof ServerCommandEvent && (((ServerCommandEvent) e).getCommand() == null || ((ServerCommandEvent) e).getCommand().isEmpty())) {
            if (Skript.logHigh()) {
                final String eventName = Skript.testing() && Skript.debug() ? e.getClass().getCanonicalName() : e.getClass().getSimpleName();

                boolean skript = false;

                if (Commands.cancelledEvent.get()) {
                    skript = true;
                    Commands.cancelledEvent.set(false);
                }

                Skript.info(eventName + " was cancelled by " + (skript ? "Skript" : "a plugin"));
            }
            return;
        }

        while (ts.hasNext()) {
            final Trigger t = ts.next();
            if (!t.getEvent().check(e))
                continue;
            if (logVeryHigh)
                logTriggerStart(t);
            final Object timing = SkriptTimings.start(t.getDebugLabel());
            t.execute(e);
            if (timing != null)
                SkriptTimings.stop(timing);
            if (logVeryHigh)
                logTriggerEnd(t);
        }

        if (logVeryHigh)
            logEventEnd();
    }

    public static final void logEventStart(final Event e) {
        if (!Skript.logVeryHigh())
            return;
        startEvent = System.nanoTime();
        Skript.info("");
        Skript.info("== " + e.getClass().getName() + " ==");
    }

    public static final void logEventEnd() {
        if (!Skript.logVeryHigh())
            return;
        Skript.info("== took " + 1. * (System.nanoTime() - startEvent) / 1000000. + " milliseconds ==");
    }

    public static final void logTriggerStart(final Trigger t) {
        if (!Skript.logVeryHigh())
            return;
        Skript.info("# " + t.getName());
        startTrigger = System.nanoTime();
    }

    public static final void logTriggerEnd(final Trigger t) {
        if (!Skript.logVeryHigh())
            return;
        Skript.info("# " + t.getName() + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
    }

    static final void addTrigger(final Class<? extends Event>[] events, final Trigger trigger) {
        for (final Class<? extends Event> e : events) {
            final List<Trigger> ts = triggers.computeIfAbsent(e, k -> new ArrayList<>());
            ts.add(trigger);
        }
    }

    /**
     * Stores a self registered trigger to allow for it to be unloaded later on.
     *
     * @param t Trigger that has already been registered to its event
     */
    public static final void addSelfRegisteringTrigger(final Trigger t) {
        assert t.getEvent() instanceof SelfRegisteringSkriptEvent;
        selfRegisteredTriggers.add(t);
    }

    static final ScriptInfo removeTriggers(final File script) {
        final ScriptInfo info = new ScriptInfo();
        info.files = 1;

        final Iterator<List<Trigger>> triggersIter = SkriptEventHandler.triggers.values().iterator();
        while (triggersIter.hasNext()) {
            final List<Trigger> ts = triggersIter.next();
            for (int i = 0; i < ts.size(); i++) {
                if (script.equals(ts.get(i).getScript())) {
                    info.triggers++;
                    ts.remove(i);
                    i--;
                    if (ts.isEmpty())
                        triggersIter.remove();
                }
            }
        }

        for (int i = 0; i < selfRegisteredTriggers.size(); i++) {
            final Trigger t = selfRegisteredTriggers.get(i);
            if (script.equals(t.getScript())) {
                info.triggers++;
                ((SelfRegisteringSkriptEvent) t.getEvent()).unregister(t);
                selfRegisteredTriggers.remove(i);
                i--;
            }
        }

        info.commands = Commands.unregisterCommands(script);

        info.functions = Functions.clearFunctions(script);

        return info;
    }

    static final void removeAllTriggers() {
        triggers.clear();
        for (final Trigger t : selfRegisteredTriggers)
            ((SelfRegisteringSkriptEvent) t.getEvent()).unregisterAll();
        selfRegisteredTriggers.clear();
//		unregisterEvents();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static final void registerBukkitEvents() {
        for (final Class<? extends Event> e : triggers.keySet()) {
            assert e != null;
            if (!containsSuperclass(registeredEvents, e)) { // I just love Java's generics
                Bukkit.getPluginManager().registerEvent(e, listener, SkriptConfig.defaultEventPriority.value(), ee, Skript.getInstance());
                registeredEvents.add(e);
//				for (final Iterator<Class<? extends Event>> i = registeredEvents.iterator(); i.hasNext();) {
//					final Class<? extends Event> ev = i.next();
//					if (e.isAssignableFrom(ev)) {
//						if (unregisterEvent(ev))
//							i.remove();
//					}
//				}
            }
        }
    }

    private static final boolean containsSuperclass(final Collection<Class<? extends Event>> classes, final Class<? extends Event> c) {
        if (classes.contains(c))
            return true;
        for (final Class<? extends Event> cl : classes) {
            if (cl.isAssignableFrom(c))
                return true;
        }
        return false;
    }

    private static final class EmptyListener implements Listener {
        EmptyListener() {
            /* implicit super call */
        }
    }

    private static final class SkriptEventExecutor implements EventExecutor {
        SkriptEventExecutor() {
            /* implicit super call */
        }

        @Override
        public final void execute(@Nullable final Listener l, @Nullable final Event e) {
            if (e == null)
                return;

            if (last == e) // an event is received multiple times if multiple superclasses of it are registered
                return;

            last = e;

            // Event is asynchronous, but it ran from main thread
            assert !e.isAsynchronous() || !Bukkit.isPrimaryThread() : e.getClass().getCanonicalName() + " is asynchronous, but it ran from main thread";

            // Event is synchronous, but it ran from a different thread
            assert e.isAsynchronous() || Bukkit.isPrimaryThread() : e.getClass().getCanonicalName() + " is synchronous, but it ran from a different thread";

            // Skip the event if it's a frequently called event
            // Note: Making anti-cheats with Skript is already a bad idea, I'm not responsible if it breaks them
            if (e instanceof PlayerMoveEvent && System.currentTimeMillis() - lastCall < moveEventCooldown)
                return;

            if ((e instanceof BlockPhysicsEvent || e instanceof InventoryMoveItemEvent) && System.currentTimeMillis() - lastCall < eventCooldown)
                return;

            lastCall = System.currentTimeMillis();

            check(e);
        }
    }

//	private static final void unregisterEvents() {
//		for (final Iterator<Class<? extends Event>> i = registeredEvents.iterator(); i.hasNext();) {
//			if (unregisterEvent(i.next()))
//				i.remove();
//		}
//	}
//
//	private static final boolean unregisterEvent(Class<? extends Event> event) {
//		try {
//			Method m = null;
//			while (m == null) {
//				try {
//					m = event.getDeclaredMethod("getHandlerList");
//				} catch (final NoSuchMethodException e) {
//					event = (Class<? extends Event>) event.getSuperclass();
//					if (event == Event.class) {
//						assert false;
//						return false;
//					}
//				}
//			}
//			m.setAccessible(true);
//			final HandlerList l = (HandlerList) m.invoke(null);
//			l.unregister(listener);
//			return true;
//		} catch (final Exception e) {
//			if (Skript.testing())
//				e.printStackTrace();
//		}
//		return false;
//	}

}
