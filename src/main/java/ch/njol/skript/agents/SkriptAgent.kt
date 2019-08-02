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

package ch.njol.skript.agents

import ch.njol.skript.SkriptAddon
import org.eclipse.jdt.annotation.Nullable

import java.util.*
import java.util.function.Consumer

// Global Static Implementation

/**
 * A list of all the registered agents.
 */
private val agents = ArrayList<SkriptAgent>()

/**
 * Returns the list of all the registered agents.
 *
 * @return The list of all registered agents.
 */
fun getAgents(): Collection<SkriptAgent> {
    return Collections.unmodifiableList(agents)
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
fun registerAgent(addon: SkriptAddon,
                  handler: Consumer<AgentEvent>,
                  vararg events: Class<out AgentEvent>): SkriptAgent {
    @Suppress("SENSELESS_COMPARISON")
    assert(addon != null)

    val agent = SkriptAgent(addon, handler, *events)
    agents.add(agent)

    return agent
}

/**
 * Checks if a skript debugger agent associated with given skript addon exists.
 *
 * @param addon The skript addon to check.
 *
 * @return True if the agent list contains a debugger agent for the given addon.
 */
fun hasAgent(addon: SkriptAddon): Boolean {
    for (agent in agents) {
        if (agent.addon == addon) {
            return true
        }
    }
    return false
}

/**
 * Unregisters a skript debugger agent, with its all listeners.
 *
 * @param agent The agent to clear all listeners of it, and totally unregister.
 */
fun unregisterAgent(agent: SkriptAgent?) {
    assert(agent != null)
    assert(agents.contains(agent))

    agent!!.listeners.clear()
    agents.remove(agent)
}

/**
 * Returns true if at least one skript agent is currently registered.
 *
 * @return True if at least one skript agent is currently registered.
 */
@Suppress("MemberVisibilityCanBePrivate")
val isTrackingEnabled: Boolean
    get() = agents.isNotEmpty()

/**
 * Throws a event to all skript agents that listens for it.
 *
 * You should first check if tracking is enabled, by using
 * [ch.njol.skript.agents.isTrackingEnabled] method.
 *
 * @param event The event for throwing to skript agents that listens for it.
 *
 * @see ch.njol.skript.agents.isTrackingEnabled
 */
fun throwEvent(event: AgentEvent) {
    assert(isTrackingEnabled)
    for (agent in agents)
        if (agent.hasListener(event.javaClass))
            event.execute(agent)
}

// Instance Specific Implementation

/**
 * A debugger agent. Can access various information
 * that normally only accessible by internal use (or reflection).
 *
 * It does not uses bukkit's event system, but still debugging is not
 * performance free.
 *
 * If you're trying to access static methods from Java, just use
 * SkriptAgentKt instead. In Kotlin, you just import ch.njol.skript.agents.*
 *
 * @since 2.2-V13b
 */
class SkriptAgent

/**
 * Internal use only.
 *
 * @param addon The skript addon.
 * @param events The events.
 *
 * @see ch.njol.skript.agents.registerAgent
 */
@SafeVarargs
internal constructor(
        /**
         * The skript addon that this agent is connected.
         */
        @JvmField val addon: SkriptAddon,

        /**
         * The handler that handles all events.
         */
        @JvmField val handler: Consumer<AgentEvent>,

        /**
         * The events that this agent listens.
         */
        @Nullable vararg events: Class<out AgentEvent>
) {

    /**
     * The all agent event listeners for this agent.
     */
    @JvmField
    @JvmSynthetic
    internal val listeners: MutableList<Class<out AgentEvent>>

    init {
        this.listeners = ArrayList(listOf(*events))
    }

    /**
     * Adds an agent event listener to this skript debugger agent.
     *
     * @param event The event to listen.
     *
     * @return Returns true if listener is added.
     */
    fun addListener(@Nullable event: Class<out AgentEvent>): Boolean {
        return addListeners(event)
    }

    /**
     * Adds agent event listeners to this skript debugger agent.
     *
     * @param events The events to listen.
     *
     * @return Returns true if at least one listener is added.
     */
    @SafeVarargs
    fun addListeners(@Nullable vararg events: Class<out AgentEvent>): Boolean {
        @Suppress("SENSELESS_COMPARISON")
        if (events == null || events.isEmpty())
            return false
        var addedOne = false
        for (event in events) {
            if (!listeners.contains(event)) {
                listeners.add(event)
                addedOne = true
            }
        }
        return addedOne
    }

    /**
     * Checks if this agent has listens for the given event.
     *
     * @param event The event to check.
     *
     * @return True if the given event are enabled for this skript agent.
     */
    fun hasListener(@Nullable event: Class<out AgentEvent>): Boolean {
        return hasListeners(event)
    }

    /**
     * Checks if this agent listens for specific events.
     *
     * @param events The events to check.
     *
     * @return True if the all given events are enabled for this skript agent.
     */
    @SafeVarargs
    fun hasListeners(@Nullable vararg events: Class<out AgentEvent>): Boolean {
        for (event in events) {
            if (!listeners.contains(event))
                return false
        }
        return true
    }

    /**
     * Removes an agent event listener from this skript debugger agent.
     *
     * @param event The event to remove listener for.
     *
     * @return Returns true if listener is exists removed.
     */
    fun removeListener(@Nullable event: Class<out AgentEvent>): Boolean {
        return removeListeners(event)
    }

    /**
     * Removes agent event listeners from this skript debugger agent.
     *
     * @param events The events to remove listeners for.
     *
     * @return Returns true if at least one listener is exists removed.
     */
    @SafeVarargs
    fun removeListeners(@Nullable vararg events: Class<out AgentEvent>): Boolean {
        @Suppress("SENSELESS_COMPARISON")
        if (events == null || events.isEmpty())
            return false
        var removedOne = false
        for (event in events) {
            if (listeners.contains(event)) {
                listeners.remove(event)
                removedOne = true
            }
        }
        return removedOne
    }

    /**
     * Gets the all currently registered event listeners.
     *
     * The returned collection is not modifiable; it will throw exception.
     *
     * @return The all currently enabled event listeners.
     */
    fun getListeners(): Collection<Class<out AgentEvent>> {
        return Collections.unmodifiableList(listeners)
    }

    /**
     * Making a data class with a vararg constructor parameter is
     * forbidden in Kotlin. So, we are using our own implementation of
     * equals and hashCode. It is still generated by IDEA.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SkriptAgent

        if (addon != other.addon) return false
        if (handler != other.handler) return false
        if (listeners != other.listeners) return false

        return true
    }

    /**
     * Making a data class with a vararg constructor parameter is
     * forbidden in Kotlin. So, we are using our own implementation of
     * equals and hashCode. It is still generated by IDEA.
     */
    override fun hashCode(): Int {
        var result = addon.hashCode()
        result = 31 * result + handler.hashCode()
        result = 31 * result + listeners.hashCode()
        return result
    }

    /**
     * Making a data class with a vararg constructor parameter is
     * forbidden in Kotlin. So, we are using our own implementation of
     * equals and hashCode. It is still generated by IDEA.
     */
    override fun toString(): String {
        return "SkriptAgent(addon=$addon, handler=$handler, listeners=$listeners)"
    }

}
