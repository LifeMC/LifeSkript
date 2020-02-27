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

package ch.njol.util;

/**
 * An utility class (currently only consists of constants) about Line Separators.
 * You should generally use {@link LineSeparators#SYSTEM} only and not other hard-coded ones.
 * <p>
 * But they can be used in things like {@link java.util.regex.Pattern#split(CharSequence)} where
 * a hard-coded one is required to make algorithm work same on all machines.
 *
 * @since 2.2.18
 */
public final class LineSeparators {

    /**
     * UNIX style line ending, the "\n"<br />
     * If you need char use {@link LineSeparators#UNIX_CHAR}
     */
    public static final String UNIX = "\n";
    public static final char UNIX_CHAR = '\n';

    /**
     * MAC style line ending, the "\r"<br />
     * If you need char use {@link LineSeparators#MAC_CHAR}
     */
    public static final String MAC = "\r";
    public static final char MAC_CHAR = '\r';

    /**
     * DOS (also Windows) style line ending, the "\r\n" (MAC + UNIX)<br />
     * This does not have a char variant since it is a combine of multiple separators.
     */
    public static final String DOS = /*MAC + UNIX*/"\r\n"; // Not first one to make it constant
    public static final String SYSTEM = System.lineSeparator();

    /**
     * Though not an actual line feed/new line character, this a special
     * meaning of representing combined spaces with tab key
     */
    public static final String TAB = "\t";
    public static final char TAB_CHAR = '\t';

    /**
     * Checks the given function (condition) with the all line endings.
     * Returns true if any of them matches, false otherwise.
     *
     * @param checker The checker function/condition to use.
     * @return True if any of them matches, false otherwise.
     */
    public static final boolean check(final LineSeparatorChecker checker) {
        return checker.check(UNIX) || checker.check(MAC) || checker.check(DOS);
    }

    /**
     * @see LineSeparators#check(LineSeparatorChecker)
     */
    @FunctionalInterface
    public interface LineSeparatorChecker {
        boolean check(final String separator);
    }

    private LineSeparators() {
        throw new UnsupportedOperationException("Static class");
    }

}
