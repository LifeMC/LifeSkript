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
import org.bukkit.event.Event
import java.util.*

/**
 * @author TheDGOfficial
 */
@Name("Is Script Command")
@Description("Checks whatever a command is a script command.",
        "If the given command is not exists, this returns false.")
@Examples("command is a script command")
@Since("2.2-V13b")
class CondIsScriptCommand : Condition() {
    companion object {
        init {
            Skript.registerCondition(CondIsScriptCommand::class.java, "command %string% is[ a] script[ command]", "[command ]%string% is[ a] script command")
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

        return ScriptCommand.commandMap[name?.toLowerCase(Locale.ENGLISH)] != null
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return "${command!!.getSingle(e)} is a script command"
    }
}
