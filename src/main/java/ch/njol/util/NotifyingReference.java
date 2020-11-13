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
 *   Copyright (C) 2011 Peter Güttinger and contributors
 *
 */

package ch.njol.util;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class NotifyingReference<V> {
    private final boolean notifyAll;
    @Nullable
    private volatile V value;

    public NotifyingReference(@Nullable final V value, final boolean notifyAll) {
        this.value = value;
        this.notifyAll = notifyAll;
    }

    public NotifyingReference(@Nullable final V value) {
        this.value = value;
        this.notifyAll = true;
    }

    @SuppressWarnings("null")
    public NotifyingReference() {
        this.value = null;
        this.notifyAll = true;
    }

    @Nullable
    public final V get() {
        synchronized (this) {
            return this.value;
        }
    }

    public final void set(@Nullable final V newValue) {
        synchronized (this) {
            this.value = newValue;
            if (this.notifyAll) {
                notifyAll();
            } else {
                notify();
            }
        }
    }
}
