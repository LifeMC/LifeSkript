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

package ch.njol.skript.bukkitutil;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * A shorthand abstract class for using both {@link Event} and {@link Cancellable}
 * for an event.<br /><br />
 * <p>
 * Extending this class makes the event cancellable. You don't need to implement
 * {@link Cancellable#setCancelled(boolean)} and {@link Cancellable#isCancelled()} methods.<br /><br />
 * <p>
 * The cancellation status is stored inside a variable named 'cancelled', which
 * comforts to Java Naming Specification, according to getter and setter names in the interface {@link Cancellable},
 * even though {@link Cancellable#setCancelled(boolean)} defines method parameter name as 'cancel'.<br /><br />
 * <p>
 * The cancellation status variable is private, not protected as expected, also {@link CancellableEvent#setCancelled(boolean)}
 * and {@link CancellableEvent#isCancelled()} methods are final, if you want more customization; implement it yourself, this only a short-hand class.
 */
public abstract class CancellableEvent extends Event implements Cancellable {
    private boolean cancelled;

    @Override
    public final boolean isCancelled() {
        return cancelled;
    }

    @Override
    public final void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
