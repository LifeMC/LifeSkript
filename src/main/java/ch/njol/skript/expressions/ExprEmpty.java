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

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;

/**
 * @author TheDGOfficial
 */
@Name("Empty")
@Description({"Represents an empty value of a specific type."})
@Examples("empty value of the type strings")
@Since("2.2.17")
public final class ExprEmpty extends SimpleExpression<Object> {
    static {
        Skript.registerExpression(ExprEmpty.class, Object.class, ExpressionType.SIMPLE,
                "[the] empty (value|list|array) of [the] [type] %*classinfo%");
    }

    @SuppressWarnings("null")
    private Literal<ClassInfo<?>> emptyType;

    @SuppressWarnings("ConstantConditions")
    @Override
    public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        emptyType = (Literal<ClassInfo<?>>) exprs[0];
        return true;
    }

    @Override
    protected final Object[] get(final Event e) {
        return (Object[]) Array.newInstance(getReturnType(), 0);
    }

    @Override
    public final boolean isSingle() {
        return true;
    }

    @Override
    public final Class<?> getReturnType() {
        return emptyType.getSingle().getC();
    }

    @Override
    public final String toString(final @Nullable Event e, final boolean debug) {
        return "empty value of the type " + emptyType.toString(e, debug);
    }
}
