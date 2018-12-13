/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.util.coll;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReversedListViewTest {
	
	@Test
	public void test() {
		
		final ArrayList<Integer> list = new ArrayList<Integer>(Arrays.asList(1, 1, 2, 4, 7));
		final ReversedListView<Integer> reverse = new ReversedListView<Integer>(list);
		
		assertEquals(reverse.get(0), list.get(list.size() - 1));
		assertEquals(list.indexOf(1), list.size() - reverse.lastIndexOf(1) - 1);
		assertEquals(new ReversedListView<Integer>(reverse), list);
		assertEquals(reverse.listIterator(1).next(), list.get(list.size() - 2));
		
	}
	
}
