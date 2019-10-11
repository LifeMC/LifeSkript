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
import ch.njol.skript.bukkitutil.PlayerUtils
import ch.njol.skript.doc.Description
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import org.bukkit.entity.Player
import org.bukkit.event.Event

@Name("Update Inventory")
@Description("Updates the inventory of a player. This maybe used to fix some bugs, but please keep in mind that it may create more bugs.")
@Examples("on inventory close:\n\tupdate inventory of player # to fix the skquery item stole bug")
@Since("2.2.15")
class EffUpdateInventory : Effect() {
    companion object {
        init {
            Skript.registerEffect(
                    EffUpdateInventory::class.java,
                    "update[d][ the] inventory of[ the] %players%"
            )
        }
    }

    private var players: Expression<Player>? = null

    override fun init(exprs: Array<out Expression<*>>, matchedPattern: Int, isDelayed: Kleenean, parseResult: SkriptParser.ParseResult): Boolean {
        @Suppress("UNCHECKED_CAST")
        players = exprs[0] as Expression<Player>
        return true
    }

    override fun execute(e: Event?) {
        for (player in players!!.getAll(e))
            PlayerUtils.updateInventory(player)
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return "update inventory of ${players?.toString(e, debug)}"
    }
}
