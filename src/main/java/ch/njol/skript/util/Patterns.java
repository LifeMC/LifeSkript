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

package ch.njol.skript.util;

/**
 * A helper class useful when a expression/condition/effect/etc. needs to associate additional data with each pattern.
 *
 * @author Peter Güttinger
 */
public final class Patterns<T> {

    private final String[] patterns;
    private final Object[] ts;

    /**
     * @param info An array which must be like {{String, T}, {String, T}, ...}
     */
    public Patterns(final Object[][] info) {
        patterns = new String[info.length];
        ts = new Object[info.length];
        for (int i = 0; i < info.length; i++) {
            if (info[i].length != 2 || !(info[i][0] instanceof String) || info[i][1] == null)
                throw new IllegalArgumentException("given array is not like {{String, T}, {String, T}, ...}");
            patterns[i] = (String) info[i][0];
            ts[i] = info[i][1];
        }
    }

    public String[] getPatterns() {
        return patterns;
    }

    /**
     * @param matchedPattern The pattern to get the data to as given in {@link ch.njol.skript.lang.SyntaxElement#init(ch.njol.skript.lang.Expression[], int, ch.njol.util.Kleenean, ch.njol.skript.lang.SkriptParser.ParseResult)}
     * @return The info associated with the matched pattern
     * @throws ClassCastException If the item in the source array is not of the requested type
     */
    @SuppressWarnings({"unchecked", "null"})
    public T getInfo(final int matchedPattern) {
        return (T) ts[matchedPattern];
    }

}
