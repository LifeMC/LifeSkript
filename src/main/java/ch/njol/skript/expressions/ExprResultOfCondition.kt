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
import ch.njol.skript.lang.Condition
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.skript.util.toInstanceSupplier
import ch.njol.util.Kleenean
import ch.njol.util.coll.CollectionUtils
import org.bukkit.event.Event

/**
 * @author TheDGOfficial
 */
@Name("Result Of Condition")
@Description("Gets the result of a condition.")
@Examples("send \"%result of condition true%\" # sends true")
@Since("2.2.18")
class ExprResultOfCondition : SimpleExpression<Boolean>() {
    companion object {
        init {
            Skript.registerExpression(ExprResultOfCondition::class.java, Boolean::class.javaObjectType, ExpressionType.SIMPLE, ::ExprResultOfCondition.toInstanceSupplier(),
                    "result of (condition|boolean) %boolean%",
                    "result of (condition|boolean) <.+>[,]")
        }
    }

    private var condition: Condition? = null

    override fun init(exprs: Array<out Expression<*>>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        if (exprs.isNotEmpty()) {
            @Suppress("UNCHECKED_CAST")
            condition = Condition.wrap(exprs[0] as Expression<Boolean>?)

            return true
        } else {
            val cond = parseResult.regexes[0].group()

            condition = Condition.parse(cond.replace("if ", "").replace(":", ""), "Can't understand this condition: $cond")
            return condition != null
        }
    }

    override fun getReturnType(): Class<Boolean> {
        return Boolean::class.javaObjectType
    }

    override fun get(e: Event?): Array<Boolean>? {
        return CollectionUtils.array(condition!!.check(e))
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return "result of condition ${condition!!.toString(e, debug)}"
    }

    override fun isSingle(): Boolean {
        return true
    }
}
