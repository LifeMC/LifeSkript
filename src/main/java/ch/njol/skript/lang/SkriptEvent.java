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

import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

/**
 * A SkriptEvent is like a condition. It is called when any of the registered events occurs.
 * An instance of this class should then check whatever the event applies
 * (e.g. the right click event is included in the PlayerInteractEvent which also includes lef clicks, thus the SkriptEvent {@link ch.njol.skript.events.EvtClick} checks whatever it was a rightclick or
 * not).<br/>
 * It is also needed if the event has parameters.
 *
 * @author Peter Güttinger
 * @see ch.njol.skript.Skript#registerEvent(String, Class, Class, String...)
 * @see ch.njol.skript.Skript#registerEvent(String, Class, Class[], String...)
 */
public abstract class SkriptEvent implements SyntaxElement, Debuggable {

    @Override
    public final boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        throw new UnsupportedOperationException();
    }

    /**
     * called just after the constructor
     *
     * @param args
     */
    public abstract boolean init(final Literal<?>[] args, int matchedPattern, ParseResult parseResult);

    /**
     * Checks whatever the given Event applies, e.g. the leftclick event is only part of the PlayerInteractEvent, and this checks whatever the player leftclicked or not. This method
     * will only be called for events this SkriptEvent is registered for.
     *
     * @param e
     * @return true if this is SkriptEvent is represented by the Bukkit Event or false if not
     */
    public abstract boolean check(final Event e);

    @Override
    public final String toString() {
        return toString(null, false);
    }

}
