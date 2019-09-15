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
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Condition
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import org.bukkit.event.Event

/**
 * @author TheDGOfficial
 */
@Name("Is Infinity")
@Description("Checks whatever a number represents Infinity")
@Examples("1 is infinity")
@Since("2.2.14")
class CondIsInfinity : Condition() {
    companion object {
        init {
            Skript.registerCondition(CondIsInfinity::class.java, "%number% is infinity")
        }
    }

    private var number: Expression<Number>? = null

    override fun init(exprs: Array<out Expression<*>>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        @Suppress("UNCHECKED_CAST")
        number = exprs[0] as Expression<Number>
        return true
    }

    override fun check(e: Event?): Boolean {
        val double = number!!.getSingle(e)?.toDouble()
        return double == Double.POSITIVE_INFINITY || double == Double.NEGATIVE_INFINITY
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return "${number!!.getSingle(e)?.toString()} is infinity"
    }
}
