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

import java.util.Locale;

/**
 * @author Peter Güttinger
 */
@Name("Upper/Lower Case Text")
@Description("Copy of given text in upper or lower case.")
@Examples("\"oops!\" in upper case # OOPS!")
@Since("2.2-Fixes-V9c")
public final class ExprStringCase extends SimpleExpression<String> {

    private static final int UPPER = 0, LOWER = 1;

    static {
        Skript.registerExpression(ExprStringCase.class, String.class, ExpressionType.SIMPLE, "%string% in (0¦upper|1¦lower) case", "capitalized %string%");
    }

    @Nullable
    private Expression<String> origin;
    @Nullable
    private String literal;
    private int mode;

    /**
     * Helper function which takes nullable string and
     * uses given mode to it.
     *
     * @param str  Original string.
     * @param mode See above, UPPER or LOWER.
     * @return Changed string.
     */
    @SuppressWarnings("null")
    private static final String changeCase(@Nullable final String str, final int mode) {
        if (str == null)
            return "";
        if (mode == UPPER)
            return str.toUpperCase(Locale.ENGLISH);
        if (mode == LOWER)
            return str.toLowerCase(Locale.ENGLISH);
        return str;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        if (exprs[0] instanceof Literal)
            literal = ((Literal<String>) exprs[0]).getSingle();
        else
            origin = (Expression<String>) exprs[0];
        if (matchedPattern == 1)
            mode = UPPER;
        else
            mode = parseResult.mark;

        return true;
    }

    @Override
    @Nullable
    protected String[] get(final Event e) {
        final String str;
        if (literal != null)
            str = changeCase(literal, mode);
        else if (origin != null)
            str = changeCase(origin.getSingle(e), mode);
        else
            str = "";

        return new String[]{str};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        if (literal != null)
            return changeCase(literal, mode);
        if (origin != null && e != null)
            return changeCase(origin.getSingle(e), mode);

        return "";
    }

}
