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

import ch.njol.skript.Skript;
import org.eclipse.jdt.annotation.Nullable;

import java.io.Serializable;
import java.util.Map.Entry;

/**
 * For performance-critical, full-immutable pairs use {@link ImmutablePair} instead.
 *
 * @author Peter Güttinger
 */
public class Pair<T1, T2> implements Entry<T1, T2>, Cloneable, Serializable {
    private static final long serialVersionUID = 8296563685697678334L;

    @Nullable
    protected T1 first;
    @Nullable
    protected T2 second;

    public Pair() {
        first = null;
        second = null;
    }

    public Pair(@Nullable final T1 first, @Nullable final T2 second) {
        this.first = first;
        this.second = second;
    }

    public Pair(final Entry<T1, T2> e) {
        this.first = e.getKey();
        this.second = e.getValue();
    }

    @Nullable
    public T1 getFirst() {
        return first;
    }

    public void setFirst(@Nullable final T1 first) {
        this.first = first;
    }

    @Nullable
    public T2 getSecond() {
        return second;
    }

    public void setSecond(@Nullable final T2 second) {
        this.second = second;
    }

    /**
     * @return "first,second"
     */
    @Override
    public final String toString() {
        return first + "," + second;
    }

    /**
     * Checks for equality with Entries to match {@link #hashCode()}
     */
    @Override
    public final boolean equals(@Nullable final Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Entry))
            return false;
        final Entry<?, ?> other = (Entry<?, ?>) obj;
        final T1 first = this.first;
        final T2 second = this.second;
        return (first == null ? other.getKey() == null : other == first || first.equals(other.getKey())) && (second == null ? other.getValue() == null : other == second || second.equals(other.getValue()));
    }

    /**
     * As defined by {@link Entry#hashCode()}
     */
    @Override
    public final int hashCode() {
        final T1 first = this.first;
        final T2 second = this.second;
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }

    @Override
    @Nullable
    public T1 getKey() {
        return first;
    }

    @Override
    @Nullable
    public T2 getValue() {
        return second;
    }

    @Override
    @Nullable
    public T2 setValue(@Nullable final T2 value) {
        final T2 old = second;
        second = value;
        return old;
    }

    /**
     * @return a shallow copy of this pair
     */
    @Override
    public Pair<T1, T2> clone() {
        try {
            return (Pair<T1, T2>) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw Skript.sneakyThrow(e);
        }
    }

}
