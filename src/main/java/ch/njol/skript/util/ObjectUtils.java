/*
 * Copyright (c) 2009, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package ch.njol.skript.util;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is copy of Objects class added in Java 7.
 * Copied from OpenJDK 8 source code for compatibility with Java 6.
 * Note: This class is currently used exactly one location in Skript,
 * but i'm not inlined or copied the methods, i copied the full class
 * and named as "ObjectUtils" for later usage if required.
 * Original description of the class from java.util.Objects:
 * This class consists of {@code static} utility methods for operating
 * on objects. These utilities include {@code null}-safe or {@code
 * null}-tolerant methods for computing the hash code of an object,
 * returning a string for an object, and comparing two objects.
 */
public final class ObjectUtils {
	private ObjectUtils() {
		throw new AssertionError("No ObjectUtils instances for you!");
	}
	
	/**
	 * Returns {@code true} if the arguments are equal to each other
	 * and {@code false} otherwise.
	 * Consequently, if both arguments are {@code null}, {@code true}
	 * is returned and if exactly one argument is {@code null}, {@code
	 * false} is returned. Otherwise, equality is determined by using
	 * the {@link Object#equals equals} method of the first
	 * argument.
	 *
	 * @param a an object
	 * @param b an object to be compared with {@code a} for equality
	 * @return {@code true} if the arguments are equal to each other
	 *         and {@code false} otherwise
	 * @see Object#equals(Object)
	 */
	public static boolean equals(@Nullable final Object a, final Object b) {
		return a == b || a != null && a.equals(b);
	}
	
	/**
	 * Returns the hash code of a non-{@code null} argument and 0 for
	 * a {@code null} argument.
	 *
	 * @param o an object
	 * @return the hash code of a non-{@code null} argument and 0 for
	 *         a {@code null} argument
	 * @see Object#hashCode
	 */
	public static int hashCode(@Nullable final Object o) {
		return o != null ? o.hashCode() : 0;
	}
	
	/**
	 * Returns the result of calling {@code toString} for a non-{@code
	 * null} argument and {@code "null"} for a {@code null} argument.
	 *
	 * @param o an object
	 * @return the result of calling {@code toString} for a non-{@code
	 * null} argument and {@code "null"} for a {@code null} argument
	 * @see Object#toString
	 * @see String#valueOf(Object)
	 */
	@SuppressWarnings("null")
	public static String toString(final Object o) {
		return String.valueOf(o);
	}
	
	/**
	 * Returns the result of calling {@code toString} on the first
	 * argument if the first argument is not {@code null} and returns
	 * the second argument otherwise.
	 *
	 * @param o an object
	 * @param nullDefault string to return if the first argument is
	 *            {@code null}
	 * @return the result of calling {@code toString} on the first
	 *         argument if it is not {@code null} and the second argument
	 *         otherwise.
	 * @see ObjectUtils#toString(Object)
	 */
	@SuppressWarnings("null")
	public static String toString(final Object o, final String nullDefault) {
		return o != null ? o.toString() : nullDefault;
	}
	
	/**
	 * Checks that the specified object reference is not {@code null}. This
	 * method is designed primarily for doing parameter validation in methods
	 * and constructors, as demonstrated below:
	 * <blockquote>
	 * 
	 * <pre>
	 * public Foo(Bar bar) {
	 * 	this.bar = Objects.requireNonNull(bar);
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 *
	 * @param obj the object reference to check for nullity
	 * @param <T> the type of the reference
	 * @return {@code obj} if not {@code null}
	 * @throws NullPointerException if {@code obj} is {@code null}
	 */
	public static <T> T requireNonNull(@Nullable final T obj) {
		if (obj == null)
			throw new NullPointerException();
		return obj;
	}
	
	/**
	 * Checks that the specified object reference is not {@code null} and
	 * throws a customized {@link NullPointerException} if it is. This method
	 * is designed primarily for doing parameter validation in methods and
	 * constructors with multiple parameters, as demonstrated below:
	 * <blockquote>
	 * 
	 * <pre>
	 * public Foo(Bar bar, Baz baz) {
	 * 	this.bar = Objects.requireNonNull(bar, "bar must not be null");
	 * 	this.baz = Objects.requireNonNull(baz, "baz must not be null");
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 *
	 * @param obj the object reference to check for nullity
	 * @param message detail message to be used in the event that a {@code
	 *                NullPointerException} is thrown
	 * @param <T> the type of the reference
	 * @return {@code obj} if not {@code null}
	 * @throws NullPointerException if {@code obj} is {@code null}
	 */
	public static <T> T requireNonNull(@Nullable final T obj, final String message) {
		if (obj == null)
			throw new NullPointerException(message);
		return obj;
	}
	
	/**
	 * Returns {@code true} if the provided reference is {@code null} otherwise
	 * returns {@code false}.
	 *
	 * @apiNote This method exists to be used as a
	 *          {@code filter(Objects::isNull)}
	 * @param obj a reference to be checked against {@code null}
	 * @return {@code true} if the provided reference is {@code null} otherwise
	 *         {@code false}
	 * @since 1.6
	 */
	public static boolean isNull(@Nullable final Object obj) {
		return obj == null;
	}
	
	/**
	 * Returns {@code true} if the provided reference is non-{@code null}
	 * otherwise returns {@code false}.
	 *
	 * @apiNote This method exists to be used as a
	 *          {@code filter(Objects::nonNull)}
	 * @param obj a reference to be checked against {@code null}
	 * @return {@code true} if the provided reference is non-{@code null}
	 *         otherwise {@code false}
	 * @since 1.6
	 */
	public static boolean nonNull(@Nullable final Object obj) {
		return obj != null;
	}
}
