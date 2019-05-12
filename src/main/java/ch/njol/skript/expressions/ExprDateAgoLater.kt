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

package ch.njol.skript.expressions

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.skript.util.Date
import ch.njol.skript.util.Timespan
import ch.njol.util.Kleenean
import org.bukkit.event.Event
import org.eclipse.jdt.annotation.Nullable

@Name("Ago / Later")
@Description("Gets a timespan representing past or the future from now on")
@Examples("5 hours ago", "1 day later")
@Since("2.2.14")
class ExprDateAgoLater : SimpleExpression<Date>() {
    companion object {
        init {
            Skript.registerExpression(ExprDateAgoLater::class.java, Date::class.java, ExpressionType.COMBINED,
                    "%timespan% (ago|in the past)", "%timespan% (later|from now)")
        }
    }

    private var timespan: Expression<Timespan>? = null
    private var ago: Boolean = false

    override fun init(exprs: Array<Expression<*>>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        @Suppress("UNCHECKED_CAST")
        timespan = exprs[0] as Expression<Timespan>
        ago = matchedPattern == 0
        return true
    }

    @Nullable
    override fun get(e: Event): Array<Date>? {
        val timespan = this.timespan!!.getSingle(e) ?: return null
        val date = Date()
        if (ago) {
            date.subtract(timespan)
        } else {
            date.add(timespan)
        }
        return arrayOf(date)
    }

    override fun isSingle(): Boolean {
        return true
    }

    override fun getReturnType(): Class<Date> {
        return Date::class.java
    }

    override fun toString(@Nullable e: Event?, debug: Boolean): String {
        return timespan!!.toString(e, debug) + " " + if (ago) "ago" else "later"
    }
}
