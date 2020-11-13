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

import ch.njol.skript.Skript;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("static-method")
final class PairCloneTest {

    @Test
    final void testPairClone() {
        final Pair<Class<String>, Class<Skript>> samplePair = new Pair<>();

        samplePair.setFirst(String.class);
        samplePair.setSecond(Skript.class);

        assertSame(String.class, samplePair.clone().getFirst());
        assertSame(Skript.class, samplePair.clone().getSecond());

        assertNotSame(samplePair.clone(), samplePair.clone());
        assertEquals(samplePair.clone(), samplePair.clone());
    }

}
