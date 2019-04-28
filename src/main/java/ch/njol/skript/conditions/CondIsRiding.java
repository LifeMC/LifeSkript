/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Is Riding")
@Description("Tests whether an entity is riding another or is in a vehicle.")
@Examples({"player is riding a saddled pig"})
@Since("2.0")
public final class CondIsRiding extends Condition {
    static {
        Skript.registerCondition(CondIsRiding.class, "%entities% (is|are) riding [%entitydatas%]", "%entities% (isn't|is not|aren't|are not) riding [%entitydatas%]");
    }

    @SuppressWarnings("null")
    Expression<EntityData<?>> types;
    @SuppressWarnings("null")
    private Expression<Entity> entities;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        entities = (Expression<Entity>) exprs[0];
        types = (Expression<EntityData<?>>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(final Event e) {
        return entities.check(e, en -> types.check(e, d -> d.isInstance(en.getVehicle()), isNegated()));
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {
        return entities.toString(e, debug) + (isNegated() ? " is" : " isn't") + " riding" + types.toString(e, debug);
    }

}
