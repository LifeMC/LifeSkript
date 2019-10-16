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
import ch.njol.skript.bukkitutil.Workarounds;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.LineSeparators;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

/**
 * @author TheDGOfficial
 */
@Name("System Property")
@Description("Gets a system property.")
@Examples("on load:" + LineSeparators.UNIX + LineSeparators.TAB + "message \"The file encoding is %property \"file.encoding\"%")
@Since("2.2-Fixes-V12")
public final class ExprProperty extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprProperty.class, String.class, ExpressionType.SIMPLE, "property( |-)%string%");
    }

    @Nullable
    private
    Expression<String> propertyName;

    /**
     * @see Expression#isSingle()
     */
    @Override
    public boolean isSingle() {
        return true;
    }

    /**
     * @see Expression#getReturnType()
     */
    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    /**
     * @see ch.njol.skript.lang.SyntaxElement#init(Expression[], int, Kleenean, ch.njol.skript.lang.SkriptParser.ParseResult)
     */
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        propertyName = (Expression<String>) exprs[0];
        return true;
    }

    /**
     * @see ch.njol.skript.lang.Debuggable#toString(Event, boolean)
     */
    @Override
    @SuppressWarnings("null")
    public String toString(@Nullable final Event e, final boolean debug) {
        return "property \"" + propertyName.getSingle(e) + "\" (" + Arrays.toString(get(e)) + ')';
    }

    /**
     * @see SimpleExpression#get(Event)
     */
    @Override
    @Nullable
    @SuppressWarnings("null")
    protected String[] get(final Event e) {
        final String key = propertyName.getSingle(e);
        if (key == null || key.isEmpty())
            return null;
        return new String[]{Workarounds.getOriginalProperty(key)};
    }

}
