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

package ch.njol.skript.variables;

import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * This is used to manage local variable type hints.
 *
 * <ul>
 * <li>EffChange adds them when local variables are set
 * <li>Variable checks them when parser tries to create it
 * <li>ScriptLoader clears hints after each section has been parsed
 * <li>ScriptLoader enters and exists scopes as needed
 * </ul>
 */
public final class TypeHints {

    private static final Deque<Map<String, Class<?>>> typeHints = new ArrayDeque<>(100);

    static {
        clear(); // Initialize type hints
    }

    private TypeHints() {
        throw new UnsupportedOperationException();
    }

    public static final void add(final String variable, final Class<?> hint) {
        if (hint == Object.class) // Ignore useless type hint
            return;

        // Take top of stack, without removing it
        final Map<String, Class<?>> hints = typeHints.getFirst();
        hints.put(variable, hint);
    }

    @Nullable
    public static final Class<?> get(final String variable) {
        // Go through stack of hints for different scopes
        for (final Map<String, Class<?>> hints : typeHints) {
            final Class<?> hint = hints.get(variable);
            if (hint != null) // Found in this scope
                return hint;
        }

        return null; // No type hint available
    }

    public static final void enterScope() {
        typeHints.push(new HashMap<>());
    }

    public static final void exitScope() {
        typeHints.pop();
    }

    public static final void clear() {
        typeHints.clear();
        typeHints.push(new HashMap<>());
    }

}
