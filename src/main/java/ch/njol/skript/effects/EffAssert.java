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

package ch.njol.skript.effects;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Assert")
@Description({"Assert that a condition is true.", "Note that this throws a real java script error if the condition is not true.", "So, this should only be used when you are %100 sure the condition should be true."})
@Examples("assert {_someImportantVariable} is set")
@Since("2.2.17")
public final class EffAssert extends Effect {

    static {
        Skript.registerEffect(EffAssert.class, "assert [the] [condition] <.+> [with the] [(with|else|because [of]|reason [of]) [the] %-string%]");
    }

    @Nullable
    private Condition condition;

    @Nullable
    private Expression<String> errorMsg;

    private @Nullable
    String script;
    private int line;

    @SuppressWarnings({"null", "unchecked"})
    @Override
    public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
        final String cond = parseResult.regexes.get(0).group();

        if ("true".equals(cond))
            Skript.warning("Useless assertion; condition is always true");

        // assert false may be used to test unreachable code
        condition = Condition.parse("false".equals(cond) ? cond + " is true" : cond, "Can't understand this condition: " + cond);

        if (condition == null)
            return false;

        if (exprs.length > 0 && exprs[0] != null)
            errorMsg = (Expression<String>) exprs[0];

        if (ScriptLoader.currentScript != null)
            script = ScriptLoader.currentScript.getFileName();
        if (SkriptLogger.getNode() != null)
            line = SkriptLogger.getNode().getLine();

        return true;
    }

    @SuppressWarnings("null")
    @Override
    protected final void execute(final Event e) {
        if (!condition.check(e)) {
            final String msg = errorMsg != null ? errorMsg.getSingle(e) : null;
            throw new EffThrow.ScriptError(script, line, msg != null ? msg : "assertion failed");
        }
    }

    @SuppressWarnings("null")
    @Override
    public final String toString(final @Nullable Event e, final boolean debug) {
        return "assert " + condition.toString(e, debug) + (errorMsg != null ? " else " + errorMsg.toString(e, debug) : "");
    }

}
