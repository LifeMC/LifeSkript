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

package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Peter Güttinger
 */
public final class EnumParser<E extends Enum<E>> implements Converter<String, E> {

    private final Class<E> enumType;
    private final Map<String, E> cache;

    @Nullable
    private final String allowedValues;
    private final String type;

    public EnumParser(final Class<E> enumType) {
        this(enumType, enumType.getSimpleName().toLowerCase(Locale.ENGLISH));
    }

    public EnumParser(final Class<E> enumType, final String type) {
        assert enumType != null;
        this.enumType = enumType;
        this.type = type;
        if (enumType.getEnumConstants().length <= 12) {
            final StringBuilder b = new StringBuilder(enumType.getEnumConstants()[0].name());
            for (final E e : enumType.getEnumConstants()) {
                if (b.length() != 0)
                    b.append(", ");
                b.append(e.name().toLowerCase(Locale.ENGLISH).replace('_', ' '));
            }
            allowedValues = b.toString();
        } else {
            allowedValues = null;
        }
        this.cache = new HashMap<>(enumType.getEnumConstants().length);
        for (final E enumValue : enumType.getEnumConstants())
            this.cache.put(enumValue.name(), enumValue);
    }

    @Override
    @Nullable
    public E convert(final String s) {
        final String name = s.toUpperCase(Locale.ENGLISH).replace(' ', '_');
        /*
        try {
            return Enum.valueOf(enumType, name);
        } catch (final IllegalArgumentException e) {
            Skript.error('\'' + s + "' is not a valid value for " + type + (allowedValues == null ? "" : ". Allowed values are: " + allowedValues));
            return null;
        }
        */
        final E value = this.cache.get(name);

        if (value == null)
            Skript.error('\'' + s + "' is not a valid value for " + type + (allowedValues == null ? "" : ". Allowed values are: " + allowedValues));

        return value;
    }

    @Override
    public String toString() {
        return "EnumParser[enum=" + enumType + ",allowedValues=" + allowedValues + ",type=" + type + ']';
    }

}
