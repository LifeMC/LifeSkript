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

import org.bukkit.DyeColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

@SuppressWarnings("static-method")
public final class DyeColorTest {

    @SuppressWarnings("deprecation")
    @Test
    public final void testGetData() {
        for (final DyeColor dyeColor : DyeColor.values()) {
            assertSame(dyeColor.getWoolData(), Color.getData(dyeColor));
        }
    }

    @Test
    public final void testSilver() {
        assertSame(DyeColor.SILVER, Color.getSilver());
    }

}
