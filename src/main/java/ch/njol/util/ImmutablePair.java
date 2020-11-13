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

package ch.njol.util;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;
import java.util.function.IntSupplier;

/**
 * A performance-critical full-immutable version of the class {@link Pair}.
 *
 * @param <T> The type of the first object.
 * @param <E> The type of the second object.
 *
 * @since 2.2.18
 */
public final class ImmutablePair<T, E> implements Immutable {

    private final T first;
    private final E second;

    private final IntSupplier computeHashCode;
    private final int hashCode;

    /**
     * When using this constructor, if the passed objects are immutable, you should mark the classes
     * that T and E represents immutable to get the performance benefit of cached hash code.<p /><p />
     *
     * If you don't control the classes that T and E represents, use {@link ImmutablePair#ImmutablePair(Object, Object, boolean)}
     * with 'true' as third parameter instead to make it cache the result of hash code.<p /><p />
     *
     * Keep in mind that caching the hash code result of mutable objects can result in wrong behaviour, and it is
     * not recommended unless you are sure the objects are never modified (though still mutable).
     *
     * @implNote In future, this constructor will default to 'true' as immutable parameter for the standard JDK
     * immutable classes, e.g {@link String}. For now, you should use the explicit parameter to clarify them.
     */
    public ImmutablePair(final T first, final E second) {
        this(first, second, first instanceof Immutable && second instanceof Immutable);
    }

    /**
     * @param immutable Whatever the passed objects are also immutable.
     */
    @SuppressWarnings("null")
    public ImmutablePair(final T first, final E second, final boolean immutable) {
        this(first, second, immutable, () -> 31 * (first != null ? first.hashCode() : 0) + (second != null ? second.hashCode() : 0));
    }

    /**
     * @param computeHashCode The hash code compute function that provides a hash code.
     *                        Must not return -1 since it is a reserved number reserved by this class.
     * @see Object#hashCode()
     * @throws IllegalArgumentException If the hash code compute function returns a reserved number
     */
    public ImmutablePair(final T first, final E second, final boolean immutable, final IntSupplier computeHashCode) throws IllegalArgumentException {
        this.first = first;
        this.second = second;

        this.computeHashCode = computeHashCode;

        int cachedHashCode = 0;
        this.hashCode = immutable ? (cachedHashCode = this.computeHashCode.getAsInt()) : -1;

        if (cachedHashCode == -1)
            throw new IllegalArgumentException("Hash code compute function must not return reserved numbers");
    }

    public final T getFirst() {
        return first;
    }

    public final E getSecond() {
        return second;
    }

    /**
     * @return "first, second"
     */
    @Override
    public final String toString() {
        return first + ", " + second;
    }

    @Override
    public final boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutablePair<?, ?>)) return false;

        final ImmutablePair<?, ?> that = (ImmutablePair<?, ?>) o;

        if (!Objects.equals(first, that.first)) return false;
        return Objects.equals(second, that.second);
    }

    @Override
    public final int hashCode() {
        if (hashCode == -1)
            return computeHashCode.getAsInt();
        return hashCode;
    }

}
