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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Peter Güttinger
 */
final class Math2Test {

    private static final int RANDOM_NUMBERS = 10000;

    private static final int[] a(final int... is) {
        final Random rand = new SecureRandom();
        final int[] r = Arrays.copyOf(is, is.length + RANDOM_NUMBERS);
        for (int i = is.length; i < r.length; i++)
            r[i] = rand.nextInt();
        return r;
    }

    private static final long[] a(final long... ls) {
        final Random rand = new SecureRandom();
        final long[] r = Arrays.copyOf(ls, ls.length + RANDOM_NUMBERS);
        for (int i = ls.length; i < r.length; i++)
            r[i] = rand.nextLong();
        return r;
    }

    private static final float[] a(final float... fs) {
        final Random rand = new SecureRandom();
        final float[] r = new float[(fs.length << 1) + RANDOM_NUMBERS];
        for (int i = 0; i < fs.length; i++) {
            r[2 * i] = fs[i];
            r[2 * i + 1] = -fs[i];
        }
        for (int i = fs.length << 1; i < r.length; i++)
            r[i] = i % 2 == 0 ? Float.intBitsToFloat(rand.nextInt()) : rand.nextLong();
        return r;
    }

    private static final double[] a(final double... ds) {
        final Random rand = new SecureRandom();
        final double[] r = new double[(ds.length << 1) + RANDOM_NUMBERS];
        for (int i = 0; i < ds.length; i++) {
            r[2 * i] = ds[i];
            r[2 * i + 1] = -ds[i];
        }
        for (int i = ds.length << 1; i < r.length; i++)
            r[i] = i % 2 == 0 ? Double.longBitsToDouble(rand.nextLong()) : rand.nextLong();
        return r;
    }

    @SuppressWarnings({"unused", "static-method"})
    @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
    @Test
    void testMath() {

        a(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, 0, -1, 1);
        a(Long.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, 0, -1, 1);
        final double[] doubles = a(Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN, Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, 0, -0, 1, 0.1, 0x1.fffffffffffffp-2, 0.5, 1.5, 100, 5726579381544559d, 5726579381544559.5d);
        final float[] floats = a(Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE, Float.POSITIVE_INFINITY, Float.NaN, Integer.MAX_VALUE, Integer.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, 0, -0, 1, 0.1f, 0x1.fffffep-2f, 0.5f, 1.5f, 100, 12954701, 12954701.5f);

        final Random rand = new SecureRandom();

        final int[][] modvs = {{5, 4, 1}, {-3, 4, 1}, {4, 4, 0}, {-4, 4, 0}, {-100, 5, 0}, {-50, 100, 50}, {-1000, 4, 0}, {-10, 9, 8}
        };
        for (final int[] v : modvs)
            assertEquals(v[2], Math2.mod(v[0], v[1]), () -> "mod(" + v[0] + ',' + v[1] + ')');

        final int[][] p2vsI = {{1, 1}, {2, 2}, {7, 8}, {8, 8}, {9, 16}, {100, 128}, {-1, -1}, {-2, -2}, {-3, -2}, {-5, -4}, {-100, -64}, {Integer.MAX_VALUE, Integer.MIN_VALUE}, {Integer.MIN_VALUE, Integer.MIN_VALUE}
        };
        for (final int[] v : p2vsI)
            assertEquals(v[1], Math2.nextPowerOfTwo(v[0]), () -> Integer.toString(v[0]));
        final long[][] p2vsL = {{1, 1}, {2, 2}, {7, 8}, {8, 8}, {9, 16}, {100, 128}, {-1, -1}, {-2, -2}, {-3, -2}, {-5, -4}, {-100, -64}, {Integer.MAX_VALUE, (long) Integer.MAX_VALUE + 1}, {Integer.MIN_VALUE, Integer.MIN_VALUE}, {Long.MAX_VALUE, Long.MIN_VALUE}, {Long.MIN_VALUE, Long.MIN_VALUE}
        };
        for (final long[] v : p2vsL)
            assertEquals(v[1], Math2.nextPowerOfTwo(v[0]), () -> Long.toString(v[0]));

        for (int i = -31; i <= 31; i++) {
            final int n = i < 0 ? -1 << -i : 1 << i;
            for (int a = 0; a < RANDOM_NUMBERS; a++) {
                final int b = rand.nextInt(Math.max((n < 0 ? -n : n) / 2, 1));
                assertEquals(n, Math2.nextPowerOfTwo(n - b), () -> Integer.toString(n - b));
            }
        }

        for (final double d : doubles) {
            assertEquals((long) Math.floor(d), Math2.floor(d), () -> Double.toString(d));
            assertEquals((long) Math.ceil(d), Math2.ceil(d), () -> Double.toString(d));
            assertEquals(d + 0.5 == d + 1 ? (long) d : Math.round(d), Math2.round(d), () -> Double.toString(d));
            assertEquals(Math.round(d) > Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.round(d) < Integer.MIN_VALUE ? Integer.MIN_VALUE : Math.round(d), Math2.roundI(d), () -> Double.toString(d));
            assertEquals((int) Math.floor(d), Math2.floorI(d), () -> Double.toString(d));
            assertEquals((int) Math.ceil(d), Math2.ceilI(d), () -> Double.toString(d));

            final double r = Math2.frac(d);
            assertTrue(Double.isNaN(r) || 0 <= r && r < 1, () -> d + "; " + r);
        }

        for (final float f : floats) {
            assertEquals((long) Math.floor(f), Math2.floor(f), () -> Float.toString(f));
            assertEquals((long) Math.ceil(f), Math2.ceil(f), () -> Float.toString(f));
            assertEquals(Math.round((double) f), Math2.round(f), () -> Float.toString(f));
            assertEquals(f + 0.5f == f + 1 ? (int) f : Math.round(f), Math2.roundI(f), () -> Float.toString(f));
            assertEquals((int) Math.floor(f), Math2.floorI(f), () -> Float.toString(f));
            assertEquals((int) Math.ceil(f), Math2.ceilI(f), () -> Float.toString(f));

            final float r = Math2.frac(f);
            assertTrue(Float.isNaN(r) || 0 <= r && r < 1, () -> f + "; " + r);
        }

    }

}
