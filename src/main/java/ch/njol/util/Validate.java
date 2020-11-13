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

import java.util.Collection;

/**
 * @author Peter Güttinger
 */
public final class Validate {

    private Validate() {
        throw new UnsupportedOperationException();
    }

    public static final void notNull(final Object... objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == null)
                throw new IllegalArgumentException("the " + StringUtils.fancyOrderNumber(i + 1) + " parameter must not be null");
        }
    }

    public static final void notNull(@Nullable final Object object, final String name) {
        if (object == null)
            throw new IllegalArgumentException(name + " must not be null");
    }

    public static final void isTrue(final boolean b, final String error) {
        if (!b)
            throw new IllegalArgumentException(error);
    }

    public static final void isFalse(final boolean b, final String error) {
        if (b)
            throw new IllegalArgumentException(error);
    }

    public static final void notNullOrEmpty(@Nullable final String s, final String name) {
        if (s == null || s.isEmpty())
            throw new IllegalArgumentException(name + " must neither be null nor empty");
    }

    public static final void notNullOrEmpty(@Nullable final Object[] array, final String name) {
        if (array == null || array.length == 0)
            throw new IllegalArgumentException(name + " must neither be null nor empty");
    }

    public static final void notNullOrEmpty(@Nullable final Collection<?> collection, final String name) {
        if (collection == null || collection.isEmpty())
            throw new IllegalArgumentException(name + " must neither be null nor empty");
    }

    public static final void notEmpty(@Nullable final String s, final String name) {
        if (s != null && s.isEmpty())
            throw new IllegalArgumentException(name + " must not be empty");
    }

    public static final void notEmpty(final Object[] array, final String name) {
        if (array.length == 0)
            throw new IllegalArgumentException(name + " must not be empty");
    }

    public static final void notEmpty(final int[] nums, final String name) {
        if (nums.length == 0)
            throw new IllegalArgumentException(name + " must not be empty");
    }

}
