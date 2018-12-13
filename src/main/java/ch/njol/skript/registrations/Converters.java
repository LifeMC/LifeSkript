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

package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ChainedConverter;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterInfo;
import ch.njol.skript.classes.Converter.ConverterUtils;
import ch.njol.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class Converters {
	
	private Converters() {
		throw new UnsupportedOperationException();
	}
	
	private static List<ConverterInfo<?, ?>> converters = new ArrayList<ConverterInfo<?, ?>>(50);
	
	@SuppressWarnings("null")
	public static List<ConverterInfo<?, ?>> getConverters() {
		return Collections.unmodifiableList(converters);
	}
	
	/**
	 * Registers a converter.
	 * 
	 * @param from
	 * @param to
	 * @param converter
	 */
	public static <F, T> void registerConverter(final Class<F> from, final Class<T> to, final Converter<F, T> converter) {
		registerConverter(from, to, converter, 0);
	}
	
	public static <F, T> void registerConverter(final Class<F> from, final Class<T> to, final Converter<F, T> converter, final int options) {
		Skript.checkAcceptRegistrations();
		final ConverterInfo<F, T> info = new ConverterInfo<F, T>(from, to, converter, options);
		for (int i = 0; i < converters.size(); i++) {
			final ConverterInfo<?, ?> info2 = converters.get(i);
			if (info2.from.isAssignableFrom(from) && to.isAssignableFrom(info2.to)) {
				converters.add(i, info);
				return;
			}
		}
		converters.add(info);
	}
	
	// REMIND how to manage overriding of converters? - shouldn't actually matter
	public static void createMissingConverters() {
		for (int i = 0; i < converters.size(); i++) {
			final ConverterInfo<?, ?> info = converters.get(i);
			for (int j = 0; j < converters.size(); j++) {// not from j = i+1 since new converters get added during the loops
				final ConverterInfo<?, ?> info2 = converters.get(j);
				if ((info.options & Converter.NO_RIGHT_CHAINING) == 0 && (info2.options & Converter.NO_LEFT_CHAINING) == 0 && info2.from.isAssignableFrom(info.to) && !converterExistsSlow(info.from, info2.to)) {
					converters.add(createChainedConverter(info, info2));
				} else if ((info.options & Converter.NO_LEFT_CHAINING) == 0 && (info2.options & Converter.NO_RIGHT_CHAINING) == 0 && info.from.isAssignableFrom(info2.to) && !converterExistsSlow(info2.from, info.to)) {
					converters.add(createChainedConverter(info2, info));
				}
			}
		}
	}
	
	private static boolean converterExistsSlow(final Class<?> from, final Class<?> to) {
		for (final ConverterInfo<?, ?> i : converters) {
			if ((i.from.isAssignableFrom(from) || from.isAssignableFrom(i.from)) && (i.to.isAssignableFrom(to) || to.isAssignableFrom(i.to))) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private static <F, M, T> ConverterInfo<F, T> createChainedConverter(final ConverterInfo<?, ?> first, final ConverterInfo<?, ?> second) {
		return new ConverterInfo<F, T>((Class<F>) first.from, (Class<T>) second.to, new ChainedConverter<F, M, T>((Converter<F, M>) first.converter, (Converter<M, T>) second.converter), first.options | second.options);
	}
	
	/**
	 * Converts the given value to the desired type. If you want to convert multiple values of the same type you should use {@link #getConverter(Class, Class)} to get a
	 * converter to convert the values.
	 * 
	 * @param o
	 * @param to
	 * @return The converted value or null if no converter exists or the converter returned null for the given value.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <F, T> T convert(final @Nullable F o, final Class<T> to) {
		if (o == null)
			return null;
		if (to.isInstance(o))
			return (T) o;
		final Converter<? super F, ? extends T> conv = getConverter((Class<F>) o.getClass(), to);
		if (conv == null)
			return null;
		return conv.convert(o);
	}
	
	/**
	 * Converts an object into one of the given types.
	 * <p>
	 * This method does not convert the object if it is already an instance of any of the given classes.
	 * 
	 * @param o
	 * @param to
	 * @return The converted object
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <F, T> T convert(final @Nullable F o, final Class<? extends T>[] to) {
		if (o == null)
			return null;
		for (final Class<? extends T> t : to)
			if (t.isInstance(o))
				return (T) o;
		final Class<F> c = (Class<F>) o.getClass();
		for (final Class<? extends T> t : to) {
			@SuppressWarnings("null")
			final Converter<? super F, ? extends T> conv = getConverter(c, t);
			if (conv != null)
				return conv.convert(o);
		}
		return null;
	}
	
	/**
	 * Converts all entries in the given array to the desired type, using {@link #convert(Object, Class)} to convert every single value. If you want to convert an array of values
	 * of a known type, consider using {@link #convert(Object[], Class, Converter)} for much better performance.
	 * 
	 * @param o
	 * @param to
	 * @return A T[] array without null elements
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> T[] convertArray(final @Nullable Object[] o, final Class<T> to) {
		assert to != null;
		if (o == null)
			return null;
		if (to.isAssignableFrom(o.getClass().getComponentType()))
			return (T[]) o;
		final List<T> l = new ArrayList<T>(o.length);
		for (final Object e : o) {
			final T c = convert(e, to);
			if (c != null)
				l.add(c);
		}
		return l.toArray((T[]) Array.newInstance(to, l.size()));
	}
	
	/**
	 * Converts multiple objects into any of the given classes.
	 * 
	 * @param o
	 * @param to
	 * @param superType The component type of the returned array
	 * @return The converted array
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] convertArray(final @Nullable Object[] o, final Class<? extends T>[] to, final Class<T> superType) {
		if (o == null) {
			final T[] r = (T[]) Array.newInstance(superType, 0);
			assert r != null;
			return r;
		}
		for (final Class<? extends T> t : to)
			if (t.isAssignableFrom(o.getClass().getComponentType()))
				return (T[]) o;
		final List<T> l = new ArrayList<T>(o.length);
		for (final Object e : o) {
			final T c = convert(e, to);
			if (c != null)
				l.add(c);
		}
		final T[] r = l.toArray((T[]) Array.newInstance(superType, l.size()));
		assert r != null;
		return r;
	}
	
	/**
	 * Strictly converts an array to a non-null array of the specified class.
	 * Uses registered {@link ch.njol.skript.registrations.Converters} to convert.
	 *
	 * @param original The array to convert
	 * @param to What to convert {@code original} to
	 * @return {@code original} converted to an array of {@code to}
	 * @throws ClassCastException if one of {@code original}'s
	 *             elements cannot be converted to a {@code to}
	 */
	@SuppressWarnings({"unchecked", "null"})
	public static <T> T[] convertStrictly(final Object[] original, final Class<T> to) throws ClassCastException {
		final T[] end = (T[]) Array.newInstance(to, original.length);
		for (int i = 0; i < original.length; i++) {
			final T converted = Converters.convert(original[i], to);
			if (converted != null)
				end[i] = converted;
			else
				throw new ClassCastException();
		}
		return end;
	}
	
	/**
	 * Strictly converts an object to the specified class
	 *
	 * @param original The object to convert
	 * @param to What to convert {@code original} to
	 * @return {@code original} converted to a {@code to}
	 * @throws ClassCastException if {@code original} could not be converted to a {@code to}
	 */
	public static <T> T convertStrictly(final Object original, final Class<T> to) throws ClassCastException {
		final T converted = convert(original, to);
		if (converted != null)
			return converted;
		else
			throw new ClassCastException();
	}
	
	private final static Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> convertersCache = new HashMap<Pair<Class<?>, Class<?>>, Converter<?, ?>>();
	
	/**
	 * Tests whether a converter between the given classes exists.
	 * 
	 * @param from
	 * @param to
	 * @return Whether a converter exists
	 */
	public static boolean converterExists(final Class<?> from, final Class<?> to) {
		if (to.isAssignableFrom(from) || from.isAssignableFrom(to))
			return true;
		return getConverter(from, to) != null;
	}
	
	public static boolean converterExists(final Class<?> from, final Class<?>... to) {
		for (final Class<?> t : to) {
			assert t != null;
			if (converterExists(from, t))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets a converter
	 * 
	 * @param from
	 * @param to
	 * @return the converter or null if none exist
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <F, T> Converter<? super F, ? extends T> getConverter(final Class<F> from, final Class<T> to) {
		final Pair<Class<?>, Class<?>> p = new Pair<Class<?>, Class<?>>(from, to);
		if (convertersCache.containsKey(p)) // can contain null to denote nonexistence of a converter
			return (Converter<? super F, ? extends T>) convertersCache.get(p);
		final Converter<? super F, ? extends T> c = getConverter_i(from, to);
		convertersCache.put(p, c);
		return c;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	private static <F, T> Converter<? super F, ? extends T> getConverter_i(final Class<F> from, final Class<T> to) {
		for (final ConverterInfo<?, ?> conv : converters) {
			if (conv.from.isAssignableFrom(from) && to.isAssignableFrom(conv.to))
				return (Converter<? super F, ? extends T>) conv.converter;
		}
		for (final ConverterInfo<?, ?> conv : converters) {
			if (conv.from.isAssignableFrom(from) && conv.to.isAssignableFrom(to)) {
				return (Converter<? super F, ? extends T>) ConverterUtils.createInstanceofConverter(conv.converter, to);
			} else if (from.isAssignableFrom(conv.from) && to.isAssignableFrom(conv.to)) {
				return (Converter<? super F, ? extends T>) ConverterUtils.createInstanceofConverter(conv);
			}
		}
		for (final ConverterInfo<?, ?> conv : converters) {
			if (from.isAssignableFrom(conv.from) && conv.to.isAssignableFrom(to)) {
				return (Converter<? super F, ? extends T>) ConverterUtils.createDoubleInstanceofConverter(conv, to);
			}
		}
		return null;
	}
	
	/**
	 * @param from
	 * @param to
	 * @param conv
	 * @return The converted array
	 * @throws ArrayStoreException if the given class is not a superclass of all objects returned by the converter
	 */
	@SuppressWarnings("unchecked")
	public static <F, T> T[] convertUnsafe(final F[] from, final Class<?> to, final Converter<? super F, ? extends T> conv) {
		return convert(from, (Class<T>) to, conv);
	}
	
	public static <F, T> T[] convert(final F[] from, final Class<T> to, final Converter<? super F, ? extends T> conv) {
		@SuppressWarnings("unchecked")
		T[] ts = (T[]) Array.newInstance(to, from.length);
		int j = 0;
		for (final F f : from) {
			final T t = f == null ? null : conv.convert(f);
			if (t != null)
				ts[j++] = t;
		}
		if (j != ts.length)
			ts = Arrays.copyOf(ts, j);
		assert ts != null;
		return ts;
	}
	
}
