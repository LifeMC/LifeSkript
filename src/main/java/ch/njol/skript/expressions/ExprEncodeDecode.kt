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
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.util.Kleenean
import ch.njol.util.coll.CollectionUtils
import org.bukkit.event.Event
import java.util.*

class ExprEncodeDecode : SimpleExpression<String>() {
    companion object {
        init {
            Skript.registerExpression(ExprEncodeDecode::class.java, String::class.java, ExpressionType.SIMPLE, "[base64][ ](0¦encode[d]|1¦decode[d]) %string%")
        }
    }

    @JvmField var str: Expression<String>? = null
    private var encode: Boolean = false

    override fun init(exprs: Array<out Expression<*>>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        @Suppress("UNCHECKED_CAST")
        str = exprs[0] as Expression<String>?
        encode = parseResult.mark == 0
        return true
    }

    override fun getReturnType(): Class<out String> {
        return String::class.java
    }

    override fun get(e: Event?): Array<String>? {
        if (encode)
            return CollectionUtils.array(Base64.getEncoder().encodeToString(str!!.getSingle(e)!!.toByteArray()))
        else
            return CollectionUtils.array(String(Base64.getDecoder().decode(str!!.getSingle(e))))
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return "base64 " + (if (encode) "encoded" else "decoded") + str?.getSingle(e)
    }

    override fun isSingle(): Boolean {
        return true
    }
}
