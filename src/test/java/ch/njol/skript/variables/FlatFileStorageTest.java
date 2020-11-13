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

package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

import static kotlin.test.AssertionsKt.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Güttinger
 */
final class FlatFileStorageTest {

    private static final String encode(final byte[] data) {
        try {
            return (String) Skript.invoke(FlatFileStorage.class.getDeclaredMethod("encode", byte[].class), (Function<Method, Method>) Skript::setAccessible).invoke(null, new Object[]{data});
        } catch (final InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            return fail(e);
        }
    }

    private static final byte[] decode(final CharSequence hex) {
        try {
            return (byte[]) Skript.invoke(FlatFileStorage.class.getDeclaredMethod("decode", CharSequence.class), (Function<Method, Method>) Skript::setAccessible).invoke(null, hex);
        } catch (final InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            return fail(e);
        }
    }

    @Nullable
    private static final String[] splitCSV(final CharSequence line) {
        try {
            return (String[]) Skript.invoke(FlatFileStorage.class.getDeclaredMethod("splitCSV", CharSequence.class), (Function<Method, Method>) Skript::setAccessible).invoke(null, line);
        } catch (final InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            return fail(e);
        }
    }

    @SuppressWarnings("static-method")
    @Test
    void testHexCoding() {
        final byte[] bytes = {-0x80, -0x50, -0x01, 0x00, 0x01, 0x44, 0x7F};
        final String string = "80B0FF0001447F";
        assertEquals(string, encode(bytes));
        assert Arrays.equals(bytes, decode(string)) : Arrays.toString(bytes) + " != " + Arrays.toString(decode(string));
    }

    @SuppressWarnings({"null", "static-method"})
    @Test
    void testCSV() {
        final String[][] vs = {{"", ""}, {",", "", ""}, {",,", "", "", ""}, {"a", "a"}, {"a,", "a", ""}, {",a", "", "a"}, {",a,", "", "a", ""}, {" , a , ", "", "a", ""}, {"a,b,c", "a", "b", "c"}, {" a , b , c ", "a", "b", "c"},

                {"\"\"", ""}, {"\",\"", ","}, {"\"\"\"\"", "\""}, {"\" \"", " "}, {"a, \"\"\"\", b, \", c\", d", "a", "\"", "b", ", c", "d"}, {"a, \"\"\", b, \", c", "a", "\", b, ", "c"},

                {"\"\t\0\"", "\t\0"},
        };
        for (final String[] v : vs) {
            assertTrue(Arrays.equals(Arrays.copyOfRange(v, 1, v.length), splitCSV(v[0])), v[0] + ": " + Arrays.toString(Arrays.copyOfRange(v, 1, v.length)) + " != " + Arrays.toString(splitCSV(v[0])));
        }
    }

}
