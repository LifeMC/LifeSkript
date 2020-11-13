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

package ch.njol.util.coll.stack;

import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("static-method")
final class StackTest {

    @Test
    void testIntStack() {
        try (final IntStack s = new IntStack(5)) {
            assertTrue(s.isEmpty(), () -> Integer.toString(s.getPosition()));

            assertEquals(0, s.getPosition());
            assertEquals(5, s.getSize());

            s.push(1);

            assertTrue(s.isNotEmpty(), () -> Integer.toString(s.getPosition()));

            s.push(2);
            s.push(3);
            s.push(4);
            s.push(5);
            s.push(6);

            assertEquals(6, s.getPosition());
            assertEquals(5 << 1, s.getSize());

            assertEquals(6, s.pop());
            assertEquals(5, s.pop());
            assertEquals(4, s.pop());
            assertEquals(3, s.pop());

            s.push(3);
            s.push(4);
            s.push(5);

            for (int i = 5; s.isNotEmpty(); --i)
                assertEquals(i, s.pop());

            assertThrows(EmptyStackException.class, s::pop);
            assertThrows(EmptyStackException.class, s::peek);

            s.clear();

            final int n = ThreadLocalRandom.current().nextInt();
            s.push(n);

            for (int i = 0; i < 10; i++)
                assertEquals(n, s.peek());

            assertEquals(n, s.pop());

            assertThrows(EmptyStackException.class, s::peek);
            assertThrows(EmptyStackException.class, s::pop);

            s.close();

            assertThrows(IllegalStateException.class, () -> s.push(ThreadLocalRandom.current().nextInt()));

            assertThrows(EmptyStackException.class, s::pop);
            assertThrows(EmptyStackException.class, s::peek);
        }
    }

}
