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

/**
 * @author TheDGOfficial
 */
@Name("None")
@Description("Represents the none (null) value.")
@Examples({"function send(msg: text, p: player = none value of player):", "if {_p} is set:", "send {_msg} to {_p}", "else:", "broadcast {_msg}"})
@Since("2.2-Fixes-V10c, 2.2.17 (finalization)")
public final class ExprNone extends SimpleExpression<Object> {
    static {
        Skript.registerExpression(ExprNone.class, Object.class, ExpressionType.SIMPLE,
                "[the] (none|null) value of [the] [type] %*classinfo%");
    }

    @SuppressWarnings("null")
    private Literal<ClassInfo<?>> noneType;

    @SuppressWarnings("ConstantConditions")
    @Override
    public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        noneType = (Literal<ClassInfo<?>>) exprs[0];
        return true;
    }

    @Override
    @Nullable
    protected final Object[] get(final Event e) {
        return null;
    }

    @Override
    public final boolean isSingle() {
        return true;
    }

    @Override
    public final Class<?> getReturnType() {
        return noneType.getSingle().getC();
    }

    @Override
    public final String toString(@Nullable final Event e, final boolean debug) {
        return "none value of the type " + noneType.toString(e, debug);
    }
}
