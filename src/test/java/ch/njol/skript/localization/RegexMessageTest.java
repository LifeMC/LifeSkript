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

package ch.njol.skript.localization;

import ch.njol.util.LineSeparators;
import org.junit.jupiter.api.Test;

import static kotlin.test.AssertionsKt.assertTrue;

/**
 * @author Peter Güttinger
 */
final class RegexMessageTest {

    @SuppressWarnings("static-method")
    @Test
    void testRegexMessage() {

        final String[] tests = {"", "!", "a", "()", "^$", "$^", LineSeparators.UNIX, LineSeparators.DOS};

        for (final String test : tests)
            assertTrue(!RegexMessage.nop_matcher.reset(test).find() && !RegexMessage.nop_matcher.reset(test).matches(), test);

    }

}
