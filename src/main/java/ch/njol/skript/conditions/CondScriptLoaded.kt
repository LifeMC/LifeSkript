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

import ch.njol.skript.ScriptLoader
import ch.njol.skript.Skript
import ch.njol.skript.SkriptCommand
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Condition
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import org.bukkit.event.Event
import org.eclipse.jdt.annotation.Nullable
import java.io.File

@Name("Is Script Loaded")
@Description("Checks if the current script, or another script, is currently loaded.")
@Examples("script is loaded", "script \"example.sk\" is loaded")
@Since("2.2.14")
class CondScriptLoaded : Condition() {
    companion object {
        init {
            Skript.registerCondition(CondScriptLoaded::class.java,
                    "script[s] [%-strings%] (is|are) loaded",
                    "script[s] [%-strings%] (isn't|is not|aren't|are not) loaded")
        }
    }

    @Nullable
    private var scripts: Expression<String>? = null

    @Nullable
    private var currentScriptFile: File? = null

    override fun init(exprs: Array<Expression<*>>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        @Suppress("UNCHECKED_CAST")
        scripts = exprs[0] as Expression<String>
        isNegated = matchedPattern == 1
        assert(ScriptLoader.currentScript != null)
        currentScriptFile = ScriptLoader.currentScript!!.file
        return true
    }

    override fun check(e: Event): Boolean {
        val scripts = this.scripts ?: return ScriptLoader.getLoadedFiles().contains(currentScriptFile)

        return scripts.check(e,
                { scriptName -> ScriptLoader.getLoadedFiles().contains(SkriptCommand.getScriptFromName(scriptName)) },
                isNegated)
    }

    override fun toString(@Nullable e: Event?, debug: Boolean): String {
        val scripts = this.scripts

        val scriptName = if (scripts == null) "script" else if (scripts.isSingle) "script" else "scripts" + " " + scripts.toString(e, debug)
        val isSingle = scripts == null || scripts.isSingle

        return if (isSingle) scriptName + (if (isNegated) " isn't" else " is") + " loaded" else scriptName + (if (isNegated) " aren't" else " are") + " loaded"
    }
}
