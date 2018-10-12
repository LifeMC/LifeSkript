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

package ch.njol.skript.variables;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Peter Güttinger
 */
public class FlatFileStorageTest {
	
	@Test
	public void testHexCoding() {
		final byte[] bytes = {-0x80, -0x50, -0x01, 0x00, 0x01, 0x44, 0x7F};
		final String string = "80B0FF0001447F";
		assertEquals(string, FlatFileStorage.encode(bytes));
		assert Arrays.equals(bytes, FlatFileStorage.decode(string)) : Arrays.toString(bytes) + " != " + Arrays.toString(FlatFileStorage.decode(string));
	}
	
	@SuppressWarnings("null")
	@Test
	public void testCSV() {
		final String[][] vs = {
				{"", ""},
				{",", "", ""},
				{",,", "", "", ""},
				{"a", "a"},
				{"a,", "a", ""},
				{",a", "", "a"},
				{",a,", "", "a", ""},
				{" , a , ", "", "a", ""},
				{"a,b,c", "a", "b", "c"},
				{" a , b , c ", "a", "b", "c"},
				
				{"\"\"", ""},
				{"\",\"", ","},
				{"\"\"\"\"", "\""},
				{"\" \"", " "},
				{"a, \"\"\"\", b, \", c\", d", "a", "\"", "b", ", c", "d"},
				{"a, \"\"\", b, \", c", "a", "\", b, ", "c"},
				
				{"\"\t\0\"", "\t\0"},
		};
		for (final String[] v : vs) {
			assert Arrays.equals(Arrays.copyOfRange(v, 1, v.length), FlatFileStorage.splitCSV(v[0])) : v[0] + ": " + Arrays.toString(Arrays.copyOfRange(v, 1, v.length)) + " != " + Arrays.toString(FlatFileStorage.splitCSV(v[0]));
		}
	}
	
}
