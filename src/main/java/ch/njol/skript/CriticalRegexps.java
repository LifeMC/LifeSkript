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

package ch.njol.skript;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains the regexps that affects most of the load time, thus being 'critical'
 *
 * Note: All fields in this class must be final and does not being used directly,
 * saved to a private static final field instead.
 *
 * So changing the value via reflection does not affect runtime values.
 */
public final class CriticalRegexps {

    private CriticalRegexps() {
        throw new UnsupportedOperationException("Static class");
    }

    /**
     * Affects variable/database load time. Used in {@link ch.njol.skript.variables.FlatFileStorage}
     */
    public static final Pattern CSV = Pattern.compile("(?<=^|,)\\s*?([^\",]*?|\"([^\"]|\"\")*?\")\\s*?(?:,|$)");

    /**
     * Affects config/script load time. Used in {@link ch.njol.skript.config.SectionNode}
     */
    public static final Matcher COMMENT_AND_WHITESPACE = Pattern.compile("(?:[^#]|##)*?#-#(?:\\s.*?)?").matcher("");

}
