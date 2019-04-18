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
 *  along with Skript.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.classes;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Used to chain convertes to build a single converter. This is automatically created when a new converter is added.
 *
 * @param <F> same as Converter's <F> (from)
 * @param <M> the middle type, i.e. the type the first converter converts to and the second converter comverts from.
 * @param <T> same as Converter's <T> (to)
 * @author Peter Güttinger
 * @see ch.njol.skript.registrations.Converters#registerConverter(Class, Class, Converter)
 * @see Converter
 */
public final class ChainedConverter<F, M, T> implements Converter<F, T> {

    private final Converter<? super F, ? extends M> first;
    private final Converter<? super M, ? extends T> second;

    public ChainedConverter(final Converter<? super F, ? extends M> first, final Converter<? super M, ? extends T> second) {
        assert first != null;
        assert second != null;
        this.first = first;
        this.second = second;
    }

    @SuppressWarnings("unchecked")
    public static <F, M, T> ChainedConverter<F, M, T> newInstance(final Converter<? super F, ?> first, final Converter<?, ? extends T> second) {
        return new ChainedConverter<>((Converter<? super F, ? extends M>) first, (Converter<? super M, ? extends T>) second);
    }

    @Override
    @Nullable
    public T convert(final F f) {
        final M m = first.convert(f);
        if (m == null)
            return null;
        return second.convert(m);
    }

}
