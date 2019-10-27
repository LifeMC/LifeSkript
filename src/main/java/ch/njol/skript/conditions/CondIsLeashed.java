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

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Is Leashed")
@Description("Checks if an entity is currently leashed, i.e to a fence")
@Examples("target entity is leashed")
@Since("2.2.18")
public final class CondIsLeashed extends PropertyCondition<LivingEntity> {

    static {
        register(CondIsLeashed.class, CondIsLeashed::new, "leashed", "livingentities");
    }

    @Override
    public final boolean check(final LivingEntity entity) {
        return entity.isLeashed();
    }

    @Override
    protected final String getPropertyName() {
        return "leashed";
    }

}
