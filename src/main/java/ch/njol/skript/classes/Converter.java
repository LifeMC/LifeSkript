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

package ch.njol.skript.classes;

import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.registrations.Classes;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Used to convert data from one type to another.
 *
 * @param <F> The accepted type of objects to convert <b>f</b>rom
 * @param <T> The type to convert <b>t</b>o
 * @author Peter Güttinger
 * @see ch.njol.skript.registrations.Converters#registerConverter(Class, Class, Converter)
 */
@FunctionalInterface
public interface Converter<F, T> {

    /**
     * Disallow other converters from being chained to this.
     */
    int NO_LEFT_CHAINING = 1;

    /**
     * Disallow chaining this with other converters.
     */
    int NO_RIGHT_CHAINING = 2;

    /**
     * Disallow all chaining.
     */
    int NO_CHAINING = NO_LEFT_CHAINING | NO_RIGHT_CHAINING;

    int NO_COMMAND_ARGUMENTS = 4;

    /**
     * Converts an object from the given to the desired type.
     *
     * @param f The object to convert.
     * @return the converted object
     */
    @Nullable
    T convert(final F f);

    /**
     * Holds information about a converter
     *
     * @param <F> same as in {@link Converter}
     * @param <T> dito
     * @author Peter Güttinger
     */
    @SuppressWarnings("null")
    @NonNullByDefault
    final class ConverterInfo<F, T> implements Debuggable {

        public final Class<F> from;
        public final Class<T> to;
        public final Converter<F, T> converter;
        public final int options;

        /**
         * Chain of types this converter will go through from right to left.
         * For normal converters, contains from at 0 and to at 1. For chained
         * converters, chains of first and second converter are concatenated
         * together.
         */
        private final Class<?>[] chain;

        public ConverterInfo(final Class<F> from, final Class<T> to, final Converter<F, T> converter, final int options) {
            this.from = from;
            this.to = to;

            this.converter = converter;
            this.options = options;

            this.chain = new Class[]{from, to};
        }

        @SuppressWarnings("unchecked")
        public ConverterInfo(final ConverterInfo<?, ?> first, final ConverterInfo<?, ?> second, final Converter<F, T> converter, final int options) {
            this.from = (Class<F>) first.from;
            this.to = (Class<T>) second.to;

            this.converter = converter;
            this.options = options;

            this.chain = new Class[first.chain.length + second.chain.length];

            System.arraycopy(first.chain, 0, chain, 0, first.chain.length);
            System.arraycopy(second.chain, 0, chain, first.chain.length, second.chain.length);
        }

        @Override
        public String toString(final @Nullable Event e, final boolean debug) {
            if (debug) {
                final String str = Arrays.stream(chain).map(Classes::getExactClassName).collect(Collectors.joining(" -> "));

                assert str != null;
                return str;
            }
            return Classes.getExactClassName(from) + " to " + Classes.getExactClassName(to);
        }

    }

    final class ConverterUtils {

        private ConverterUtils() {
            throw new UnsupportedOperationException("Static class");
        }

        public static final <F, T> Converter<?, T> createInstanceofConverter(final ConverterInfo<F, T> conv) {
            return createInstanceofConverter(conv.from, conv.converter);
        }

        public static final <F, T> Converter<?, T> createInstanceofConverter(final Class<F> from, final Converter<F, T> conv) {
            return (Converter<Object, T>) o -> {
                if (!from.isInstance(o))
                    return null;
                return conv.convert((F) o);
            };
        }

        /**
         * Wraps a converter in a filter that will only accept conversion
         * results of given type. All other results are replaced with nulls.
         *
         * @param conv Converter to wrap.
         * @param to   Accepted return type of the converter.
         * @return The wrapped converter.
         */
        public static final <F, T> Converter<F, T> createInstanceofConverter(final Converter<F, ?> conv, final Class<T> to) {
            return f -> {
                final Object o = conv.convert(f);
                if (to.isInstance(o))
                    return (T) o;
                return null;
            };
        }

        public static final <F, T> Converter<?, T> createDoubleInstanceofConverter(final ConverterInfo<F, ?> conv, final Class<T> to) {
            return createDoubleInstanceofConverter(conv.from, conv.converter, to);
        }

        /**
         * Wraps a converter. When values given to the wrapper converter are
         * not of accepted type, it will not be called; instead, a null is
         * returned. When it returns a value that is not of accepted type, the
         * wrapped converter will return null instead.
         *
         * @param from Accepted type of input.
         * @param conv Converter to wrap.
         * @param to   Accepted type of output.
         * @return A wrapped converter.
         */
        public static final <F, T> Converter<?, T> createDoubleInstanceofConverter(final Class<F> from, final Converter<F, ?> conv, final Class<T> to) {
            return (Converter<Object, T>) o -> {
                if (!from.isInstance(o))
                    return null;
                final Object o2 = conv.convert((F) o);
                if (to.isInstance(o2))
                    return (T) o2;
                return null;
            };
        }

    }

}
