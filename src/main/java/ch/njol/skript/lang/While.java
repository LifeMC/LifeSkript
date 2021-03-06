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

import ch.njol.skript.config.SectionNode;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class While extends TriggerSection {

    private final Condition c;
    @Nullable
    private TriggerItem actualNext;

    public While(final Condition c, final SectionNode n) {
        super(n);
        this.c = c;
        super.setNext(this);
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return "while " + c.toString(e, debug);
    }

    @Override
    @Nullable
    protected final TriggerItem walk(final Event e) {
        if (c.check(e)) {
            return walk(e, true);
        }
        debug(e, false);
        return actualNext;
    }

    @Override
    public While setNext(@Nullable final TriggerItem next) {
        actualNext = next;
        return this;
    }

    @Nullable
    public TriggerItem getActualNext() {
        return actualNext;
    }

}
