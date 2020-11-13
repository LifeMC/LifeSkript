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

import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.RandomAccess;

/**
 * A stack that operates with ints only.
 */
@NotThreadSafe
@SuppressWarnings({"null", "unused", "ConstantConditions", "WeakerAccess"})
public final class IntStack implements Closeable, RandomAccess {

    /**
     * Backing array of this stack.
     */
    @Nullable
    private int[] elements;

    /**
     * Current position in the array.
     */
    private int position;

    /**
     * Creates a new {@link IntStack} with a default initial capacity of 16 {@code int}s.
     */
    public IntStack() {
        this.elements = new int[16];
    }

    /**
     * Creates a new {@link IntStack} with the given initial capacity.
     * @param initialCapacity The initial capacity.
     */
    public IntStack(final int initialCapacity) {
        this.elements = new int[initialCapacity];
    }

    /**
     * Pushes an {@code int} to the stack.
     * @param value The {@code int} to add on top of the stack.
     */
    public final void push(final int value) {
        if (this.elements == null)
            throw new IllegalStateException("Cannot push to a closed stack");

        if (position == elements.length - 1)
            enlargeArray();

        elements[position++] = value;
    }

    /**
     * Gets the top {@code int} in the stack. The {@code int} will be removed from the stack.
     * @return The top {@code int} in the stack.
     */
    public final int pop() {
        if (position == 0)
            throw new EmptyStackException();

        final int value = elements[--position];
        elements[position] = 0;

        return value;
    }

    /**
     * Gets the top {@code int} in the stack without removing it.
     * @return The top {@code int} in the stack.
     */
    public final int peek() {
        if (position == 0)
            throw new EmptyStackException();

        return elements[position - 1];
    }

    /**
     * @return True if the stack is empty.
     */
    public final boolean isEmpty() {
        return position == 0;
    }

    /**
     * @return True if the stack is not empty.
     */
    public final boolean isNotEmpty() {
        return position != 0;
    }

    /**
     * @return The size of the stack.
     */
    public final int getSize() {
        return elements.length;
    }

    /**
     * @return The current position in the stack.
     */
    public final int getPosition() {
        return position;
    }

    /**
     * Growths the size of backing array. If this method
     * shows up when profiling, consider increasing the initial capacity.
     */
    private final void enlargeArray() {
        final int[] newArray = new int[elements.length << 1];
        System.arraycopy(elements, 0, newArray, 0, elements.length);

        this.elements = newArray;
    }

    /**
     * Clears this stack. The stack will still be usable.
     */
    public final void clear() {
        position = 0;

        if (elements != null)
            Arrays.fill(elements, 0);
    }

    /**
     * Closes this stack. The stack can't be used anymore
     * after this method is called.
     */
    @Override
    public final void close() {
        clear();

        this.elements = null;
    }

}
