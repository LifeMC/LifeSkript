/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.agent;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Represents a skript debugger event.
 *
 * It extends bukkit's event, but it's not used.
 *
 * @since 2.2-V13
 */
public abstract class AgentEvent extends Event {

    /**
     * Triggers before executing the event.
     *
     * @param on The debugger that listens this event.
     */
    public void beforeExecuting(final SkriptAgent on) {}

    /**
     * Runs this event on a skript debugger agent.
     *
     * Note: First check if agent has a
     * listener for the event.
     *
     * @param on The skript debugger agent.
     */
    public final void execute(final SkriptAgent on) {
        beforeExecuting(on);

        assert on.hasListener(this.getClass());
        on.handler.accept(this);

        afterExecuting(on);
    }

    /**
     * Triggers after executing the event.
     *
     * @param on The debugger that listens this event.
     */
    public void afterExecuting(final SkriptAgent on) {}

    /**
     * This method is not supported and always throw an exception.
     *
     * @return No return value for you!
     */
    @Override
    @Deprecated
    public final HandlerList getHandlers() {
        throw new UnsupportedOperationException();
    }

}
