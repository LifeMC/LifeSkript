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

package ch.njol.util.coll;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public final class ReversedListViewTest {

    @SuppressWarnings("static-method")
	@Test
    public void testReversedListView() {

        final ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 1, 2, 4, 7));
        final ReversedListView<Integer> reverse = new ReversedListView<>(list);

        assertEquals(reverse.get(0), list.get(list.size() - 1));
        assertEquals(list.indexOf(1), list.size() - reverse.lastIndexOf(1) - 1);
        assertEquals(new ReversedListView<>(reverse), list);
        assertEquals(reverse.listIterator(1).next(), list.get(list.size() - 2));

    }

}
