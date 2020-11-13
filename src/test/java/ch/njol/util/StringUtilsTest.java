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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("static-method")
final class StringUtilsTest {

    @Test
    void testReplaceLast() {
        //noinspection GraziInspection
        final String s = "hello world world";
        assertEquals("hello world", StringUtils.replaceLast(s, " world", StringUtils.EMPTY));

        final String v = "hello world";
        assertEquals("hello world", StringUtils.replaceLast(v, "world", StringUtils.EMPTY) + "world");

        final StringBuilder b = new StringBuilder(100);

        char l = 0;
        for (int i = 0; i < 100; i++)
            b.append(l = (char) i);

        assertNotNull(StringUtils.replaceLast(b.toString(), Character.toString(l), StringUtils.EMPTY));
    }

}
