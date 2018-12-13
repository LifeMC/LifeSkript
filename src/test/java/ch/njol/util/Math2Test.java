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

package ch.njol.util;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Peter Güttinger
 */
public class Math2Test {
	
	private final static int RANDOM_NUMBERS = 10000;
	
	@SuppressWarnings("unused")
	@SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
	@Test
	public void test() {
		
		final int[] ints = a(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, 0, -1, 1);
		final long[] longs = a(Long.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, 0, -1, 1);
		final double[] doubles = a(Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN, Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, 0, -0, 1, 0.1, 0x1.fffffffffffffp-2, 0.5, 1.5, 100, 5726579381544559d, 5726579381544559.5d);
		final float[] floats = a(Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE, Float.POSITIVE_INFINITY, Float.NaN, Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, 0, -0, 1, 0.1f, 0x1.fffffep-2f, 0.5f, 1.5f, 100, 12954701, 12954701.5f);
		
		final Random rand = new Random();
		
		final int[][] modvs = {{5, 4, 1}, {-3, 4, 1}, {4, 4, 0}, {-4, 4, 0}, {-100, 5, 0}, {-50, 100, 50}, {-1000, 4, 0}, {-10, 9, 8}
		};
		for (final int[] v : modvs)
			assertEquals("mod(" + v[0] + "," + v[1] + ")", v[2], Math2.mod(v[0], v[1]));
		
		final int[][] p2vsI = {{1, 1}, {2, 2}, {7, 8}, {8, 8}, {9, 16}, {100, 128}, {-1, -1}, {-2, -2}, {-3, -2}, {-5, -4}, {-100, -64}, {Integer.MAX_VALUE, Integer.MIN_VALUE}, {Integer.MIN_VALUE, Integer.MIN_VALUE}
		};
		final long[][] p2vsL = {{1, 1}, {2, 2}, {7, 8}, {8, 8}, {9, 16}, {100, 128}, {-1, -1}, {-2, -2}, {-3, -2}, {-5, -4}, {-100, -64}, {Integer.MAX_VALUE, (long) Integer.MAX_VALUE + 1}, {Integer.MIN_VALUE, Integer.MIN_VALUE}, {Long.MAX_VALUE, Long.MIN_VALUE}, {Long.MIN_VALUE, Long.MIN_VALUE}
		};
		for (final int[] v : p2vsI)
			assertEquals("" + v[0], v[1], Math2.nextPowerOfTwo(v[0]));
		for (final long[] v : p2vsL)
			assertEquals("" + v[0], v[1], Math2.nextPowerOfTwo(v[0]));
		
		for (int i = -31; i <= 31; i++) {
			final int n = i < 0 ? -1 << -i : 1 << i;
			for (int a = 0; a < RANDOM_NUMBERS; a++) {
				final int b = n < 0 ? rand.nextInt(Math.max(-n / 2, 1)) : rand.nextInt(Math.max(n / 2, 1));
				assertEquals("" + (n - b), n, Math2.nextPowerOfTwo(n - b));
			}
		}
		
		for (final double d : doubles) {
			assertEquals("" + d, (long) Math.floor(d), Math2.floor(d));
			assertEquals("" + d, (long) Math.ceil(d), Math2.ceil(d));
			assertEquals("" + d, d + 0.5 == d + 1 ? (long) d : Math.round(d), Math2.round(d));
			assertEquals("" + d, Math.round(d) > Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.round(d) < Integer.MIN_VALUE ? Integer.MIN_VALUE : Math.round(d), Math2.roundI(d));
			assertEquals("" + d, (int) Math.floor(d), Math2.floorI(d));
			assertEquals("" + d, (int) Math.ceil(d), Math2.ceilI(d));
			
			final double r = Math2.frac(d);
			assertTrue("" + d + "; " + r, Double.isNaN(r) || 0 <= r && r < 1);
		}
		
		for (final float f : floats) {
			assertEquals("" + f, (long) Math.floor(f), Math2.floor(f));
			assertEquals("" + f, (long) Math.ceil(f), Math2.ceil(f));
			assertEquals("" + f, Math.round((double) f), Math2.round(f));
			assertEquals("" + f, f + 0.5f == f + 1 ? (int) f : Math.round(f), Math2.roundI(f));
			assertEquals("" + f, (int) Math.floor(f), Math2.floorI(f));
			assertEquals("" + f, (int) Math.ceil(f), Math2.ceilI(f));
			
			final float r = Math2.frac(f);
			assertTrue("" + f + "; " + r, Float.isNaN(r) || 0 <= r && r < 1);
		}
		
	}
	
	private static int[] a(final int... is) {
		final Random rand = new Random();
		final int[] r = Arrays.copyOf(is, is.length + RANDOM_NUMBERS);
		for (int i = is.length; i < r.length; i++)
			r[i] = rand.nextInt();
		return r;
	}
	
	private static long[] a(final long... ls) {
		final Random rand = new Random();
		final long[] r = Arrays.copyOf(ls, ls.length + RANDOM_NUMBERS);
		for (int i = ls.length; i < r.length; i++)
			r[i] = rand.nextLong();
		return r;
	}
	
	private static float[] a(final float... fs) {
		final Random rand = new Random();
		final float[] r = new float[fs.length * 2 + RANDOM_NUMBERS];
		for (int i = 0; i < fs.length; i++) {
			r[2 * i] = fs[i];
			r[2 * i + 1] = -fs[i];
		}
		for (int i = fs.length * 2; i < r.length; i++)
			r[i] = i % 2 == 0 ? Float.intBitsToFloat(rand.nextInt()) : rand.nextLong();
		return r;
	}
	
	private static double[] a(final double... ds) {
		final Random rand = new Random();
		final double[] r = new double[ds.length * 2 + RANDOM_NUMBERS];
		for (int i = 0; i < ds.length; i++) {
			r[2 * i] = ds[i];
			r[2 * i + 1] = -ds[i];
		}
		for (int i = ds.length * 2; i < r.length; i++)
			r[i] = i % 2 == 0 ? Double.longBitsToDouble(rand.nextLong()) : rand.nextLong();
		return r;
	}
	
}
