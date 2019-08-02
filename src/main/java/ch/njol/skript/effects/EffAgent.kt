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

package ch.njol.skript.effects

import ch.njol.skript.Skript
import ch.njol.skript.agents.TrackerAgent
import ch.njol.skript.agents.defaults.*
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.Literal
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleLiteral
import ch.njol.util.Kleenean
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.Event
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author TheDGOfficial
 */
@Name("Enable/Disable Agent")
@Description("Enable or disable specific tracking agents.")
@Examples("enable agent \"variables\"", "disable agent \"functions\"")
@Since("2.2-V13b")
class EffAgent : Effect() {
    companion object {
        init {
            Skript.registerEffect(
                    EffAgent::class.java,
                    "enable[ the] agent %strings%[ for ][%-commandsenders%]",
                    "disable[ the] agent %strings%[ for ][%-commandsenders%]"
            )
        }
    }

    @JvmField
    var enable: Boolean = false

    @JvmField
    var agent: Expression<String>? = null
    @JvmField
    var type: Array<TrackerType?>? = null

    @JvmField
    var sender: Expression<CommandSender>? = null

    override fun init(exprs: Array<out Expression<*>>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        enable = matchedPattern == 1
        @Suppress("UNCHECKED_CAST")
        agent = exprs[0] as Expression<String>

        @Suppress("UNCHECKED_CAST")
        if (exprs.size > 1)
            sender = exprs[1] as? Expression<CommandSender>?

        if (sender == null)
            sender = SimpleLiteral(Bukkit.getConsoleSender(), false)

        val copy = agent

        if (copy is Literal) {
            // Literal, check at parse time.
            initTypes(copy.all!!)
        }

        return true
    }

    private fun initTypes(arr: Array<String?>): Array<TrackerType?> {
        val trackers: Array<TrackerType?> = arrayOfNulls(arr.size)
        for ((i, str) in arr.withIndex())
            if (str != null) {
                val type = TrackerType.parseFrom(str)

                if (type == null) {
                    Skript.error("Unknown tracker type: $str")
                    continue
                }

                trackers[i] = type
            }
        return trackers
    }

    override fun execute(e: Event?) {
        if (type == null) {
            type = initTypes(agent?.getAll(e)!!)
        }
        val localType = type
        if (localType != null) {
            if (enable) {
                for (trackerType in localType)
                    trackerType?.enable(sender?.getAll(e)!!)
            } else {
                for (trackerType in localType)
                    trackerType?.disable(sender?.getAll(e)!!)
            }
        }
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return if (enable) "enable agent ${agent?.getAll(e)!!.joinToString()}" else "disable agent ${agent?.getAll(e)!!.joinToString()}"
    }

    data class TypedTracker(
            @JvmField val type: TrackerType,
            @JvmField val tracker: TrackerAgent
    )

    class TrackerRegistry {
        companion object {
            @JvmField
            val registryMap: ConcurrentMap<TrackerAgent, CommandSender> =
                    ConcurrentHashMap()

            @JvmField
            val trackerMap: ConcurrentMap<CommandSender, TypedTracker> =
                    ConcurrentHashMap()

            @JvmStatic
            fun register(type: TrackerType, sender: CommandSender) {
                val agent = agentFromType(type, sender)
                if (!registryMap.containsKey(agent)) {
                    registryMap[agent] = sender
                    agent.registerTracker()
                }
            }

            @JvmStatic
            fun unregister(type: TrackerType, sender: CommandSender) {
                val agent = agentFromType(type, sender)
                if (registryMap[agent] == sender) {
                    registryMap.remove(agent)
                    agent.unregisterTracker()
                }
            }

            @JvmStatic
            fun agentFromType(type: TrackerType, out: CommandSender): TrackerAgent {
                val cached = trackerMap[out]

                if (cached != null)
                    return cached.tracker

                val tracker = when (type) {
                    TrackerType.FUNCTIONS -> FunctionTrackerAgent(out)
                    TrackerType.LOOPS -> LoopTrackerAgent(out)
                    TrackerType.RESOLVER -> ResolverTrackerAgent(out)
                    TrackerType.DELAYS -> TaskTrackerAgent(out)
                    TrackerType.VARIABLES -> VariableTrackerAgent(out)
                }

                trackerMap[out] = TypedTracker(type, tracker)

                return tracker
            }
        }
    }

    enum class TrackerType {
        FUNCTIONS, LOOPS, RESOLVER, DELAYS, VARIABLES;

        fun enable(sender: CommandSender) {
            TrackerRegistry.register(this, sender)
        }

        fun disable(sender: CommandSender) {
            TrackerRegistry.unregister(this, sender)
        }

        fun enable(senders: Array<CommandSender>) {
            for (sender in senders)
                enable(sender)
        }

        fun disable(senders: Array<CommandSender>) {
            for (sender in senders)
                disable(sender)
        }

        companion object {
            @JvmStatic
            fun parseFrom(s: String?): TrackerType? {
                for (type in values())
                    if (type.name.equals(s, true) || type.name.equals((s + "s"), true))
                        return type
                return null
            }
        }
    }
}
