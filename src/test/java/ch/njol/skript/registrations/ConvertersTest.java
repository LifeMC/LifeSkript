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

package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import ch.njol.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("static-method")
public final class ConvertersTest {

    @SuppressWarnings("unused")
    private static final class SampleConvertableObject {
        static final SampleConvertableTargetObject convert(final SampleConvertableObject o) {
            return new SampleConvertableTargetObject(o.number);
        }

        final int number;

        SampleConvertableObject(final int number) {
            this.number = number;
        }

        final int getNumber() {
            return number;
        }
    }

    @SuppressWarnings("unused")
    private static final class SampleConvertableTargetObject {
        final int number;
        final int n2;

        SampleConvertableTargetObject(final int number) {
            this.number = number;
            n2 = number << 1;
        }

        final int getNumber() {
            return number;
        }

        final int getN2() {
            return n2;
        }
    }

    @SuppressWarnings("null")
    @Test
    public final void testConverters() {
        final Converter<SampleConvertableObject, SampleConvertableTargetObject> converter = SampleConvertableObject::convert;
        Converters.registerConverter(SampleConvertableObject.class, SampleConvertableTargetObject.class, converter);

        final SampleConvertableObject o = new SampleConvertableObject(2);
        final SampleConvertableTargetObject o2 = Converters.convert(o, SampleConvertableTargetObject.class);

        assertNotNull(o2, () -> Converters.getConverters().toString());
        assertSame(o.number, o2.number, () -> o.number + "; " + o2.number);

        final Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> convertersCache;

        try {
            convertersCache = (Map<Pair<Class<?>, Class<?>>, Converter<?, ?>>) Skript.invoke(Converters.class.getDeclaredField("convertersCache"), (Consumer<Field>) field ->
                    field.setAccessible(true)
            ).get(null);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw Assertions.<RuntimeException>fail("Can't get converters cache", e);
        }

        assertNotNull(convertersCache);

        assertFalse(convertersCache.isEmpty());
        assertEquals(1, convertersCache.size());

        for (final Map.Entry<Pair<Class<?>, Class<?>>, Converter<?, ?>> entry : convertersCache.entrySet()) {
            final Pair<Class<?>, Class<?>> key = entry.getKey();
            final Converter<?, ?> value = entry.getValue();

            assertEquals(new Pair<>(SampleConvertableObject.class, SampleConvertableTargetObject.class), key);
            assertEquals(key.hashCode(), new Pair<>(SampleConvertableObject.class, SampleConvertableTargetObject.class).hashCode());

            assertSame(converter, value);
        }
    }

}
