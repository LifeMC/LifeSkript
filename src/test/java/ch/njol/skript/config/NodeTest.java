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

package ch.njol.skript.config;

import ch.njol.util.NonNullPair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("null")
final class NodeTest {

    @SuppressWarnings("static-method")
    @Test
    void testSplitLine() {

        final String[][] data = {{"", "", ""}, {"ab", "ab", ""}, {"ab#", "ab", "#"}, {"ab##", "ab#", ""}, {"ab###", "ab#", "#"}, {"#ab", "", "#ab"}, {"ab#cd", "ab", "#cd"}, {"ab##cd", "ab#cd", ""}, {"ab###cd", "ab#", "#cd"}, {"######", "###", ""}, {"#######", "###", "#"}, {"#### # ####", "## ", "# ####"}, {"##### ####", "##", "# ####"}, {"#### #####", "## ##", "#"}, {"#########", "####", "#"}, {"a##b#c##d#e", "a#b", "#c##d#e"}, {" a ## b # c ## d # e ", " a # b ", "# c ## d # e "},
        };

        for (final String[] d : data) {
            final NonNullPair<String, String> p = Node.splitLine(d[0]);
            assertArrayEquals(new String[]{d[1], d[2]}, new String[]{p.getFirst(), p.getSecond()}, () -> d[0]);
        }

    }

}
