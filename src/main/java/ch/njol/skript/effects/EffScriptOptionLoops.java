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
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.ScriptOptions;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Script Option: Loops")
@Description("Sets the algorithm used when looping. Using old loops is not recommended.")
@Examples("use old loops # not recommended")
@Since("2.2.15")
public final class EffScriptOptionLoops extends Effect {

    //use (1¦old|2¦new|1¦2.1.2|2¦2.2) loops

    static {
        Skript.registerEffect(EffScriptOptionLoops.class, "use[s] (1¦old|2¦new|1¦2.1.2|2¦2.2) loops");
    }

    @SuppressWarnings("null")
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        if (!ScriptLoader.isCurrentEvent(ScriptEvent.class) || isDelayed == Kleenean.TRUE) {
            Skript.error("Current event is not Script Event or you have a delay before the script option. Defaulting to 2.2 loops.", ErrorQuality.SEMANTIC_ERROR);
            ScriptOptions.getInstance().setUsesNewLoops(ScriptLoader.currentScript.getFile(), true);
            return false;
        }
        ScriptOptions.getInstance().setUsesNewLoops(ScriptLoader.currentScript.getFile(), parseResult.mark == 2);
        return true;
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return "Script Option Loops";
    }

    @Override
    protected void execute(final Event e) {
        /* executed on parse-time */
    }

}
