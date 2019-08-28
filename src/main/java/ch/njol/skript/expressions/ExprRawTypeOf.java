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

package ch.njol.skript.expressions;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.registrations.Classes;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author TheDGOfficial
 */
@Name("Raw type of")
@Description({"The raw type of something, i.e variables, expressions etc.", "Returns none if no type is found."})
@Examples("send \"%raw type of the player%\" # will output 'player'")
@Since("2.2.17")
@SuppressWarnings("rawtypes")
public final class ExprRawTypeOf extends SimplePropertyExpression<Object, ClassInfo> {
    static {
        register(ExprRawTypeOf.class, ClassInfo.class, "raw type", "objects");
    }

    @Override
    protected String getPropertyName() {
        return "raw type";
    }

    @Override
    @Nullable
    public ClassInfo<?> convert(final Object o) {
        if (o instanceof ClassInfo<?>)
            return (ClassInfo<?>) o;
        if (o instanceof Class<?>)
            return Classes.getExactClassInfo((Class<?>) o);
        return Classes.getExactClassInfo(o.getClass());
    }

    @Override
    public Class<ClassInfo> getReturnType() {
        return ClassInfo.class;
    }
}
