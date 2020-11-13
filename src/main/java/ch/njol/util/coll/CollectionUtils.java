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

import ch.njol.skript.util.EmptyArrays;
import ch.njol.util.Pair;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.*;
import java.util.Map.Entry;

/**
 * Utils for collections and arrays. All methods will not print any errors for <tt>null</tt> collections/arrays, but will return false/-1/etc.
 *
 * @author Peter Güttinger
 */
public final class CollectionUtils {

    private static final Random random = new Random();

    private CollectionUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Finds an object in an array using {@link Object#equals(Object)} (can find null elements).
     *
     * @param array The array to search in
     * @param t     The object to search for
     * @return The index of the first occurrence of the given object or -1 if not found
     */
    public static final <T> int indexOf(@Nullable final T[] array, @Nullable final T t) {
        if (array == null)
            return -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(t))
                return i;
        }
        return -1;
    }

    public static final <T> int lastIndexOf(@Nullable final T[] array, @Nullable final T t) {
        if (array == null)
            return -1;
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i].equals(t))
                return i;
        }
        return -1;
    }

    public static final <T> int indexOf(@Nullable final T[] array, @Nullable final T t, final int start, final int end) {
        if (array == null)
            return -1;
        for (int i = start; i < end; i++) {
            if (array[i].equals(t))
                return i;
        }
        return -1;
    }

    public static final <T> boolean contains(@Nullable final T[] array, @Nullable final T o) {
        return indexOf(array, o) != -1;
    }

    @SafeVarargs
    public static final <T> boolean containsAny(@Nullable final T[] array, @Nullable final T... os) {
        if (array == null || os == null)
            return false;
        for (final T o : os) {
            if (indexOf(array, o) != -1)
                return true;
        }
        return false;
    }

    @SafeVarargs
    public static final <T> boolean containsAll(@Nullable final T[] array, @Nullable final T... os) {
        if (array == null || os == null)
            return false;
        for (final T o : os) {
            if (indexOf(array, o) == -1)
                return false;
        }
        return true;
    }

    public static final int indexOf(@Nullable final int[] array, final int num) {
        if (array == null)
            return -1;
        return indexOf(array, num, 0, array.length);
    }

    public static final int indexOf(@Nullable final int[] array, final int num, final int start) {
        if (array == null)
            return -1;
        return indexOf(array, num, start, array.length);
    }

    public static final int indexOf(@Nullable final int[] array, final int num, final int start, final int end) {
        if (array == null)
            return -1;
        for (int i = start; i < end; i++) {
            if (array[i] == num)
                return i;
        }
        return -1;
    }

    public static final boolean contains(@Nullable final int[] array, final int num) {
        return indexOf(array, num) != -1;
    }

    /**
     * finds a string in an array of strings (ignoring case).
     *
     * @param array the array to search in
     * @param s     the string to search for
     * @return the index of the first occurrence of the given string or -1 if not found
     */
    public static final int indexOfIgnoreCase(@Nullable final String[] array, @Nullable final String s) {
        if (array == null)
            return -1;
        int i = 0;
        for (final String a : array) {
            if (a == null ? s == null : a.equalsIgnoreCase(s))
                return i;
            i++;
        }
        return -1;
    }

    public static final boolean containsIgnoreCase(@Nullable final String[] array, @Nullable final String s) {
        return indexOfIgnoreCase(array, s) != -1;
    }

    /**
     * Finds an object in an iterable using {@link Object#equals(Object)}.
     *
     * @param iter The iterable to search in
     * @param o    The object to search for
     * @return The index of the first occurrence of the given object or -1 if not found
     */
    public static final <T> int indexOf(@Nullable final Iterable<T> iter, @Nullable final T o) {
        if (iter == null)
            return -1;
        int i = 0;
        for (final T a : iter) {
            if (a.equals(o))
                return i;
            i++;
        }
        return -1;
    }

    /**
     * Finds a string in a collection of strings (ignoring case).
     *
     * @param iter The iterable to search in
     * @param s    The string to search for
     * @return The index of the first occurrence of the given string or -1 if not found
     */
    public static final int indexOfIgnoreCase(@Nullable final Iterable<String> iter, @Nullable final String s) {
        if (iter == null)
            return -1;
        int i = 0;
        for (final String a : iter) {
            if (a == null ? s == null : a.equalsIgnoreCase(s))
                return i;
            i++;
        }
        return -1;
    }

    /**
     * @param map
     * @param key
     * @return A new entry object or null if the key is not in the map
     */
    @Nullable
    public static final <T, U> Entry<T, U> containsKey(@Nullable final Map<T, U> map, @Nullable final T key) {
        if (map == null)
            return null;
        if (map.containsKey(key))
            return new Pair<>(key, map.get(key));
        return null;
    }

    @Nullable
    public static final <U> Entry<String, U> containsKeyIgnoreCase(@Nullable final Map<String, U> map, @Nullable final String key) {
        if (key == null)
            return containsKey(map, null);
        if (map == null)
            return null;
        for (final Entry<String, U> e : map.entrySet()) {
            if (key.equalsIgnoreCase(e.getKey()))
                return e;
        }
        return null;
    }

    /**
     * @param classes Array of classes
     * @param c       The class to look for
     * @return Whatever the class or any of its superclasses are contained in the array
     */
    public static final boolean containsSuperclass(@Nullable final Class<?>[] classes, @Nullable final Class<?> c) {
        if (classes == null || c == null)
            return false;
        for (final Class<?> cl : classes) {
            if (cl == null)
                continue;
            if (cl.isAssignableFrom(c))
                return true;
        }
        return false;
    }

    /**
     * @param classes Array of classes
     * @param cs      The classes to look for
     * @return Whatever the classes or any of their superclasses are contained in the array
     */
    public static final boolean containsAnySuperclass(@Nullable final Class<?>[] classes, @Nullable final Class<?>... cs) {
        if (classes == null || cs == null)
            return false;
        for (final Class<?> cl : classes) {
            if (cl == null)
                continue;
            for (final Class<?> c : cs) {
                if (cl.isAssignableFrom(c))
                    return true;
            }
        }
        return false;
    }

    @Nullable
    public static final <T> T getRandom(@Nullable final T[] os) {
        if (os == null || os.length == 0)
            return null;
        return os[random.nextInt(os.length)];
    }

    @Nullable
    public static final <T> T getRandom(@Nullable final T[] os, final int start) {
        if (os == null || os.length == 0)
            return null;
        return os[random.nextInt(os.length - start) + start];
    }

    @Nullable
    public static final <T> T getRandom(@Nullable final List<T> os) {
        if (os == null || os.isEmpty())
            return null;
        return os.get(random.nextInt(os.size()));
    }

    /**
     * @param set The set of elements
     * @param sub The set to test for being a subset of <tt>set</tt>
     * @return Whatever <tt>sub</tt> only contains elements out of <tt>set</tt> or not
     */
    public static final boolean isSubset(@Nullable final Object[] set, @Nullable final Object[] sub) {
        if (set == null || sub == null)
            return false;
        for (final Object s : set) {
            if (!contains(sub, s))
                return false;
        }
        return true;
    }

    /**
     * Gets the intersection of the given sets, i.e. a set that only contains elements that occur in all given sets.
     *
     * @param sets
     * @return
     */
    @SafeVarargs
    @SuppressWarnings("null")
    public static final <E> Set<E> intersection(@Nullable final Set<E>... sets) {
        if (sets == null || sets.length == 0)
            return Collections.emptySet();
        if (sets.length == 1 && sets[0] != null)
            return sets[0];
        final Set<E> l = new HashSet<>(sets[0]);
        for (int i = 1; i < sets.length; i++) {
            if (sets[i] == null)
                continue;
            l.retainAll(sets[i]);
        }
        return l;
    }

    /**
     * Gets the union of the given sets, i.e. a set that contains all elements of the given sets.
     *
     * @param sets
     * @return
     */
    @SafeVarargs
    @SuppressWarnings("null")
    public static final <E> Set<E> union(@Nullable final Set<E>... sets) {
        if (sets == null || sets.length == 0)
            return Collections.emptySet();
        if (sets.length == 1 && sets[0] != null)
            return sets[0];
        final Set<E> l = new HashSet<>(sets[0]);
        for (int i = 1; i < sets.length; i++) {
            if (sets[i] == null)
                continue;
            l.addAll(sets[i]);
        }
        return l;
    }

    /**
     * Creates an array from the given objects. Useful for creating arrays of generic types.
     * <p>
     * The method is annotated {@link NonNull}, but will simply return null if null is passed.
     *
     * @param array Some objects
     * @return The passed array
     */
    @SafeVarargs
    @SuppressWarnings({"null", "ConstantConditions"})
    @NonNull
    @Contract("null->null")
    public static final <T> T[] array(@Nullable final T... array) {
        return array;
    }

    /**
     * Creates a permutation of all integers in the interval [start, end]
     *
     * @param start The lowest number which will be included in the permutation
     * @param end   The highest number which will be included in the permutation
     * @return an array of length end - start + 1, or an empty array if start > end.
     */
    public static final int[] permutation(final int start, final int end) {
        if (start > end)
            return EmptyArrays.EMPTY_INT_ARRAY;
        final int length = end - start + 1;
        final int[] r = new int[length];
        for (int i = 0; i < length; i++)
            r[i] = start + i;
        for (int i = length - 1; i > 0; i--) {
            final int j = random.nextInt(i + 1);
            final int b = r[i];
            r[i] = r[j];
            r[j] = b;
        }
        return r;
    }

    /**
     * Creates a permutation of all bytes in the interval [start, end]
     *
     * @param start The lowest number which will be included in the permutation
     * @param end   The highest number which will be included in the permutation
     * @return an array of length end - start + 1, or an empty array if start > end.
     */
    public static final byte[] permutation(final byte start, final byte end) {
        if (start > end)
            return EmptyArrays.EMPTY_BYTE_ARRAY;
        final int length = end - start + 1;
        final byte[] r = new byte[length];
        for (byte i = 0; i < length; i++)
            r[i] = (byte) (start + i);
        for (int i = length - 1; i > 0; i--) {
            final int j = random.nextInt(i + 1);
            final byte b = r[i];
            r[i] = r[j];
            r[j] = b;
        }
        return r;
    }

    /**
     * Shorthand for <code>{@link CollectionUtils#permutation(int, int) permutation(0, length - 1)}</code>
     */
    public static final int[] permutation(final int length) {
        return permutation(0, length - 1);
    }

    /**
     * Converts a collection of integers into a primitive int array.
     *
     * @param ints The collection
     * @return An int[] containing the elements of the given collection in the order they were returned by the collection's iterator.
     */
    @SuppressWarnings("null")
    public static final int[] toArray(@Nullable final Collection<Integer> ints) {
        if (ints == null)
            return EmptyArrays.EMPTY_INT_ARRAY;
        final int[] r = new int[ints.size()];
        int i = 0;
        for (final Integer n : ints)
            r[i++] = n;
        if (i != r.length) { // shouldn't happen if the collection is valid
            assert false : ints;
            return Arrays.copyOfRange(r, 0, i);
        }
        return r;
    }

    public static final float[] toFloats(@Nullable final double[] doubles) {
        if (doubles == null)
            return EmptyArrays.EMPTY_FLOAT_ARRAY;
        final float[] floats = new float[doubles.length];
        for (int i = 0; i < floats.length; i++)
            floats[i] = (float) doubles[i];
        return floats;
    }

    public static final Double[] wrap(final double... primitive) {
        final Double[] wrapped = new Double[primitive.length];
        for (int i = 0; i < primitive.length; ++i) {
            wrapped[i] = primitive[i]; // Auto boxing
        }
        return wrapped;
    }

    public static final Byte[] wrap(final byte[] primitive) {
        final Byte[] wrapped = new Byte[primitive.length];
        for (int i = 0; i < primitive.length; ++i) {
            wrapped[i] = primitive[i]; // Auto boxing
        }
        return wrapped;
    }

}
