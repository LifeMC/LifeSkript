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

package ch.njol.skript.conditions

import ch.njol.skript.Skript
import ch.njol.skript.command.ScriptCommand
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Condition
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import org.bukkit.event.Event
import org.bukkit.plugin.SimplePluginManager

/**
 * @author TheDGOfficial
 */
@Name("Command Exists")
@Description("Checks whatever the given command is exists or not.")
@Examples("if command \"help\" exists")
@Since("2.2.16")
class CondCommandExists : Condition() {
    companion object {
        @JvmField
        val commandMap: CommandMap? = getCommandMap()

        init {
            Skript.registerCondition(CondCommandExists::class.java, "command %string% [is[ a] ](valid|existing|exists)[ command]", "[command ]%string% [is[ a] ](valid|existing|exists) command")
        }

        private fun getCommandMap(): CommandMap? {
            val pluginManager = Bukkit.getPluginManager()

            if (pluginManager is SimplePluginManager) {
                val commandMap = pluginManager.javaClass.getDeclaredField("commandMap")

                if (!commandMap.isAccessible)
                    commandMap.isAccessible = true

                return commandMap.get(pluginManager) as? CommandMap?
            }

            return null
        }
    }

    @JvmField
    var command: Expression<String>? = null

    override fun init(exprs: Array<out Expression<*>>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        @Suppress("UNCHECKED_CAST")
        command = exprs[0] as Expression<String>?
        return true
    }

    override fun check(e: Event?): Boolean {
        var name: String? = command!!.getSingle(e)

        if (name?.startsWith("/") == true)
            name = name.substring(1)

        return ScriptCommand.commandMap[name] != null || Bukkit.getPluginCommand(name) != null || Bukkit.getServer().helpMap.getHelpTopic(name) != null
                || commandMap?.getCommand(name) != null
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return "command ${command!!.getSingle(e)} is exists"
    }
}
