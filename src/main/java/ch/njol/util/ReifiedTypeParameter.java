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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A simple utility class for getting classes from type
 * parameters.<p />
 *
 * <p>
 * Example usage:<p />
 * <pre>
 *     // This will print "class java.lang.String" (without the quotes)
 *     System.out.println(ReifiedTypeParameter.&#60;String&#62getReifiedType());
 * </pre>
 * </p>
 *
 * The algorithm behind this is simple, no deep reflection hacks etc,
 * it just creates a dummy var-args array argument, and then invokes
 * {@link Class#getComponentType()} on it.
 *
 * This returns the type of the inferred var-arg array, hence the
 * actual type parameters class.<p />
 *
 * @since 2.2.17
 */
public final class ReifiedTypeParameter {

    private ReifiedTypeParameter() {
        throw new UnsupportedOperationException("Static class");
    }

    /**
     * Returns the class of passed type parameter. Uses a simple algorithm.
     * The type must be fully known at compile time.<p />
     *
     * <p />
     * Otherwise, a {@link IllegalStateException} will be thrown at runtime.
     * Do not use this frequently; uses empty dummy arrays, may create memory overhead.<p />
     *
     * <p>
     * Example usage:<p />
     * <pre>
     *     // This will print "class java.lang.String" (without the quotes)
     *     System.out.println(ReifiedTypeParameter&#60;String&#62.getReifiedType());
     * </pre>
     * </p>
     *
     * The algorithm behind this is simple, no deep reflection hacks etc,
     * it just creates a dummy var-args array argument, and then invokes
     * {@link Class#getComponentType()} on it.
     *
     * This returns the type of the inferred var-arg array, hence the
     * actual type parameters class.<p />
     *
     * @param <T> The type parameter, can't be inferred by the compiler
     * @return The class of the passed type parameter, non-null
     *
     * @since 2.2.17
     */
    @NonNull
    @SafeVarargs
    public static final <T> Class<T> getReifiedType(final @Nullable T... dummy) {
        if (dummy == null || dummy.length > 0)
            throw new IllegalArgumentException("Dummy argument should be inferred by the compiler");
        final Class<T> reifiedType = (Class<T>) dummy.getClass().getComponentType();
        if (reifiedType == Object.class)
            throw new IllegalStateException("The type parameter must be full known at compile time");
        return reifiedType;
    }

}
