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

package ch.njol.skript.aliases;

import ch.njol.skript.log.BukkitLoggerFilter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Peter Güttinger
 */
final class AliasesTest {
    static {
        BukkitLoggerFilter.addFilter(record -> {
            if (record == null)
                return false;
            return record.getMessage() == null || !record.getMessage().startsWith("[Skript] Missing entry");
        });
    }

    @SuppressWarnings("static-method")
    @Test
    void testNames() {
        final ItemType t = new ItemType();
        t.add(new ItemData(0));

        final HashMap<String, ItemType> var1 = new LinkedHashMap<>(3);
        var1.put("{default}", t);
        var1.put("v1.1", t);
        var1.put("v1.2", t);
        final Aliases.Variations v = new Aliases.Variations();
        v.put("var1", var1);
        final HashMap<String, ItemType> var2 = new LinkedHashMap<>(2);
        var2.put("v2.1 @a", t);
        var2.put("v2.2", t);
        v.put("var2", var2);
        final HashMap<String, ItemType> var3 = new LinkedHashMap<>(2);
        var3.put("v3.1¦¦s¦", t);
        var3.put("v3.2¦a¦b¦", t);
        v.put("var3", var3);
        final HashMap<String, ItemType> varL = new LinkedHashMap<>(3);
        varL.put("{default}", t);
        varL.put("normales ", t);
        varL.put("Birken", t);
        v.put("varL", varL);

        final String[][] tests = {{"a", "a"}, {"a[b]c", "abc", "ac"}, {"a [b] c", "a b c", "a c"}, {"a(b|c)d", "abd", "acd"}, {"a(b|)c", "abc", "ac"}, {"a {var1}", "a", "a v1.1", "a v1.2"}, {"a {var2} @an", "a v2.1@a", "a v2.2 @an", "a @an"}, {"a {var3}", "a v3.1¦¦s¦", "a v3.2¦a¦b¦", "a"}, {"<any> a @an", "aliases.any-skp a @-"}, {"a <item>", "a ¦item¦items¦"}, {"[Holz]Block", "Holzblock", "Block"}, {"{varL}Holz", "Holz", "normales Holz", "Birkenholz"}
        };

        for (final String[] test : tests) {
            final Supplier<String> testFirstIndexSupplier = () -> test[0];
            @SuppressWarnings("null") final Set<String> names = Aliases.getAliases(test[0], t, v).keySet();
            assertEquals(test.length - 1, names.size(), testFirstIndexSupplier);
            int i = 1;
            for (final String name : names)
                assertEquals(test[i++], name, testFirstIndexSupplier);
        }
    }

}
