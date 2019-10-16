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

package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;

/**
 * A condition which must be fulfilled for the trigger to continue. If the condition is in a section the behaviour depends on the section.
 *
 * @author Peter Güttinger
 * @see Skript#registerCondition(Class, String...)
 */
public abstract class Condition extends Statement {

    private boolean negated;

    protected Condition() {
        /* implicit super call */
    }

    @SuppressWarnings({"rawtypes", "unchecked", "null"})
    @Nullable
    public static final Condition parse(String s, final String defaultError) {
        s = s.trim();
        while (!s.isEmpty() && s.charAt(0) == '(' && SkriptParser.next(s, 0, ParseContext.DEFAULT) == s.length())
            s = s.substring(1, s.length() - 1);
        return SkriptParser.<Condition>parse(s, (Iterator/*<? extends SyntaxElementInfo<Condition>>*/) Skript.getConditions().iterator(), defaultError);
    }

    public static final Condition wrap(final Expression<Boolean> bool) {
        return new BooleanCondition(bool);
    }

    /**
     * Not an actual registered condition. (for now?)
     */
    private static final class BooleanCondition extends Condition {
        private final Expression<Boolean> bool;

        BooleanCondition(final Expression<Boolean> bool) {
            this.bool = bool;
        }

        @Override
        public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
            return true;
        }

        @Override
        public final boolean check(final Event e) {
            return bool.check(e, o -> o, isNegated());
        }

        @Override
        public final String toString(@Nullable final Event e, final boolean debug) {
            return bool.toString(e, debug);
        }
    }

    /**
     * Checks whatever this condition is satisfied with the given event. This should not alter the event or the world in any way, as conditions are only checked until one returns
     * false. All subsequent conditions of the same trigger will then be omitted.<br/>
     * <br/>
     * You might want to use {@link ch.njol.skript.lang.util.SimpleExpression#check(Event, ch.njol.util.Checker)}
     *
     * @param e the event to check
     * @return {@code true} if the condition is satisfied, {@code false} otherwise or if the condition doesn't apply to this event.
     */
    public abstract boolean check(final Event e);

    @Override
    public final boolean run(final Event e) {
        return check(e);
    }

    /**
     * @return whatever this condition is negated or not.
     */
    public final boolean isNegated() {
        return negated;
    }

    /**
     * Sets the negation status of this condition. This will change the behaviour of {@link Expression#check(Event, ch.njol.util.Checker, boolean)}.
     *
     * @param invert
     */
    protected final void setNegated(final boolean invert) {
        negated = invert;
    }

}
