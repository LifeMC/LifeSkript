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

package ch.njol.skript.lang.function;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.eclipse.jdt.annotation.Nullable;

public final class FunctionEvent<T> extends Event {
    // Bukkit stuff
    private static final HandlerList handlers = new HandlerList();

    @Nullable
    private final Function<? extends T> function;

    /**
     * @see FunctionEvent#FunctionEvent(Function)
     * @deprecated Backwards compatibility.
     */
    @Deprecated
    public FunctionEvent() {
        this(null);
    }

    @Deprecated
    public FunctionEvent(final @Nullable Function<? extends T> function) {
        this(!Bukkit.isPrimaryThread(), function);
    }

    public FunctionEvent(final boolean async, final @Nullable Function<? extends T> function) {
        super(async);
        this.function = function;
    }

    public static final HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Returns null only if the old deprecated
     * constructor is used, or a null function is passed.
     *
     * @return Returns the function.
     */
    @Nullable
    public Function<? extends T> getFunction() {
        return function;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
