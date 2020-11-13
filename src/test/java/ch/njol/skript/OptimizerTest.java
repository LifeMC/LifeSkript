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

package ch.njol.skript;

import ch.njol.util.LineSeparators;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("static-method")
final class OptimizerTest {

    @Test
    void testAndOrOptimizer() {
        final String expected = "if uncolored command or uncolored arguments contains \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\" or \"\":";
        final String[] actual = {"if uncolored command or uncolored arguments contains \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\" or \"\":"};

        final Supplier<String> messageSupplier = () -> "Expected:" + expected + LineSeparators.UNIX + "Actual:" + actual[0] + LineSeparators.UNIX + "Optimized:" + (actual[0] = ScriptLoader.optimizeAndOr(null, actual[0]));

        assertEquals(expected, actual[0] = ScriptLoader.optimizeAndOr(null, actual[0]), messageSupplier);

        for (int i = 0; i < 10; i++)
            assertEquals(expected, actual[0] = ScriptLoader.optimizeAndOr(null, actual[0]), messageSupplier);
    }

}
