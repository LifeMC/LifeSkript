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

package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Is Holding")
@Description("Checks whatever a player is holdign a specific item. Cannot be used with endermen, use 'entity is [not] an enderman holding &lt;item type&gt;' instead.")
@Examples({"player is holding a stick", "victim isn't holding a sword of sharpness"})
@Since("1.0")
public final class CondItemInHand extends Condition {

    static {
        Skript.registerCondition(CondItemInHand.class, "[%livingentities%] ha(s|ve) %itemtypes% in hand", "[%livingentities%] (is|are) holding %itemtypes%", "[%livingentities%] (ha(s|ve) not|do[es]n't have) %itemtypes% in hand", "[%livingentities%] (is not|isn't) holding %itemtypes%");
    }

    @SuppressWarnings("null")
    private Expression<ItemType> types;
    @SuppressWarnings("null")
    private Expression<LivingEntity> entities;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
        entities = (Expression<LivingEntity>) vars[0];
        types = (Expression<ItemType>) vars[1];
        setNegated(matchedPattern >= 2);
        return true;
    }

    @Override
    public boolean check(final Event e) {
        return entities.check(e, en -> types.check(e, type -> type.isOfType(en.getEquipment().getItemInHand()), isNegated()));
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return entities.toString(e, debug) + ' ' + (entities.isSingle() ? "is" : "are") + " holding " + types.toString(e, debug);
    }

}
