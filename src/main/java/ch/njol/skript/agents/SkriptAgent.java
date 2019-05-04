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

package ch.njol.skript.agents;

import ch.njol.skript.SkriptAddon;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * A debugger agent. Can access various information
 * that normally only accessible by internal use (or reflection).
 *
 * It does not uses bukkit's event system, but still debugging is not
 * performance free.
 *
 * @since 2.2-V13
 */
public final class SkriptAgent {

    // Global Static Implementation

    /**
     * A list of all the registered agents.
     */
    private static final List<SkriptAgent> agents =
        new ArrayList<>();

    /**
     * Returns the list of all the registered agents.
     *
     * @return The list of all registered agents.
     */
    public static final Collection<SkriptAgent> getAgents() {
        return Collections.unmodifiableList(SkriptAgent.agents);
    }

    /**
     * Registers a skript debugger agent, with optional pre-defined events.
     *
     * @param addon The addon that debugger uses or connected to.
     * @param events The optional pre-defined events.
     *
     * @return The created skript debugger agent.
     */
    @SafeVarargs
    public static final SkriptAgent registerAgent(final SkriptAddon addon,
                                                  final Consumer<AgentEvent> handler,
                                                  final Class<? extends AgentEvent>... events) {
        assert addon != null;

        final SkriptAgent agent = new SkriptAgent(addon, handler, events);
        SkriptAgent.agents.add(agent);

        return agent;
    }

    /**
     * Checks if a skript debugger agent associated with given skript addon exists.
     *
     * @param addon The skript addon to check.
     *
     * @return True if the agent list contains a debugger agent for the given addon.
     */
    public static final boolean hasAgent(final SkriptAddon addon) {
        for (final SkriptAgent agent : agents) {
            if (agent.addon == addon || agent.addon.equals(addon)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Unregisters a skript debugger agent, with its all listeners.
     *
     * @param agent The agent to clear all listeners of it, and totally unregister.
     */
    public static final void unregisterAgent(final SkriptAgent agent) {
        assert agent != null;
        assert agents.contains(agent);

        agent.listeners.clear();
        agents.remove(agent);
    }

    /**
     * Returns true if at least one skript agent is currently registered.
     *
     * @return True if at least one skript agent is currently registered.
     */
    public static final boolean isTrackingEnabled() {
        return !agents.isEmpty();
    }

    /**
     * Throws a event to all skript agents that listens for it.
     *
     * You should first check if tracking is enabled, by using
     * {@link SkriptAgent#isTrackingEnabled()} method.
     *
     * @param event The event for throwing to skript agents that listens for it.
     *
     * @see SkriptAgent#isTrackingEnabled()
     */
    public static final void throwEvent(final AgentEvent event) {
        assert isTrackingEnabled();
        for (final SkriptAgent agent : agents)
            if (agent.hasListener(event.getClass()))
                event.execute(agent);
    }

    // Instance Specific Implementation

    /**
     * The skript addon that this agent is connected.
     */
    public final SkriptAddon addon;

    /**
     * The handler that handles all events.
     */
    public final Consumer<AgentEvent> handler;

    /**
     * The all agent event listeners for this agent.
     */
    private final List<Class<? extends AgentEvent>> listeners;

    /**
     * Internal use only.
     *
     * @param addon The skript addon.
     * @param events The events.
     *
     * @see SkriptAgent#registerAgent(SkriptAddon, Consumer, Class[])
     */
    @SafeVarargs
    SkriptAgent(final SkriptAddon addon, final Consumer<AgentEvent> handler, final @Nullable Class<? extends AgentEvent>... events) {
        this.addon = addon;
        this.handler = handler;
        this.listeners = new ArrayList<>(Arrays.asList(events));
    }

    /**
     * Adds a agent event listener to this skript debugger agent.
     *
     * @param event The event to listen.
     *
     * @return Returns true if listener is added.
     */
    public final boolean addListener(final @Nullable Class<? extends AgentEvent> event) {
        return addListeners(event);
    }

    /**
     * Adds agent event listeners to this skript debugger agent.
     *
     * @param events The events to listen.
     *
     * @return Returns true if at least one listener is added.
     */
    @SafeVarargs
    public final boolean addListeners(final @Nullable Class<? extends AgentEvent>... events) {
        if (events == null || events.length < 1)
            return false;
        boolean addedOne = false;
        for (final Class<? extends AgentEvent> event : events) {
            if (!listeners.contains(event)) {
                listeners.add(event);
                addedOne = true;
            }
        }
        return addedOne;
    }

    /**
     * Checks if this agent has listens for the given event.
     *
     * @param event The event to check.
     *
     * @return True if the given event are enabled for this skript agent.
     */
    public final boolean hasListener(final @Nullable Class<? extends AgentEvent> event) {
        return hasListeners(event);
    }

    /**
     * Checks if this agent listens for specific events.
     *
     * @param events The events to check.
     *
     * @return True if the all given events are enabled for this skript agent.
     */
    @SafeVarargs
    public final boolean hasListeners(final @Nullable Class<? extends AgentEvent>... events) {
        for (final Class<? extends AgentEvent> event : events) {
            if (!listeners.contains(event))
                return false;
        }
        return true;
    }

    /**
     * Removes a agent event listener from this skript debugger agent.
     *
     * @param event The event to remove listener for.
     *
     * @return Returns true if listener is exists removed.
     */
    public final boolean removeListener(final @Nullable Class<? extends AgentEvent> event) {
        return removeListeners(event);
    }

    /**
     * Removes agent event listeners from this skript debugger agent.
     *
     * @param events The events to remove listeners for.
     *
     * @return Returns true if at least one listener is exists removed.
     */
    @SafeVarargs
    public final boolean removeListeners(final @Nullable Class<? extends AgentEvent>... events) {
        if (events == null || events.length < 1)
            return false;
        boolean removedOne = false;
        for (final Class<? extends AgentEvent> event : events) {
            if (listeners.contains(event)) {
                listeners.remove(event);
                removedOne = true;
            }
        }
        return removedOne;
    }

    /**
     * Gets the all currently registered event listeners.
     *
     * The returned collection is not modifiable; it will throw exception.
     *
     * @return The all currently enabled event listeners.
     */
    public final Collection<Class<? extends AgentEvent>> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

}
