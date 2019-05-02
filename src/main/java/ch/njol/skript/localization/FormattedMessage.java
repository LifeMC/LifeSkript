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

package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import org.eclipse.jdt.annotation.Nullable;

import java.util.IllegalFormatException;
import java.util.function.Supplier;

public final class FormattedMessage extends Message {

    private final @Nullable
    Object[] args;

    private final @Nullable
    Supplier<Object[]> supplier;

    /**
     * @param key
     * @param args An array of Objects to replace into the format message, e.g. {@link java.util.concurrent.atomic.AtomicReference}s.
     */
    public FormattedMessage(final String key, final Object... args) {
        super(key);
        assert args.length > 0;
        this.args = args;
        supplier = null;
    }

    /**
     * @param key
     * @param args An array of Objects to replace into the format message, e.g. {@link java.util.concurrent.atomic.AtomicReference}s.
     */
    public FormattedMessage(final String key, final Supplier<Object[]> args) {
        super(key);
        this.args = null;
        supplier = args;
    }

    @SuppressWarnings("null")
	@Override
    public String toString() {
        try {
            final String val = getValue();
            return val == null ? key : "" + String.format(val, args != null ? args : supplier.get());
        } catch (final IllegalFormatException e) {
            final String m = "The formatted message '" + key + "' uses an illegal format: " + e.getLocalizedMessage();
            Skript.adminBroadcast("<red>" + m);
            System.err.println("[Skript] " + m);
            e.printStackTrace();
            return "[ERROR]";
        }
    }

}
