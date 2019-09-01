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

package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A reflective wrapper for accessing Spigot
 * config data, without manually parsing the spigot.yml.
 */
public final class SpigotConfig {

    @Nullable
    public static final Class<?> configClass =
            Skript.classForName("org.spigotmc.SpigotConfig");
    public static final boolean spigotConfig =
            configClass != null;
    private static final ConcurrentHashMap<String, Field> fieldCache =
            new ConcurrentHashMap<>(100);

    @Deprecated
    private SpigotConfig() {
        throw new UnsupportedOperationException("Static utility class");
    }

    /**
     * Returns value of an option from spigot.yml, returns null
     * when server platform is not Spigot.
     * <p>
     * Throws an exception when the given option name is not found.
     * Please be aware that the names must match field names from the SpigotConfig class.
     *
     * @param key The option, more specifically, field name from the SpigotConfig class.
     * @param <T> The type of the option return value, should be type of the field.
     * @return The value of the field from the SpigotConfig class.
     */
    @Nullable
    public static final <T> T get(final String key) {
        final Class<?> config = configClass;
        if (config == null)
            return null;
        try {
            Field field;

            final Field cached = fieldCache.get(key);

            if (cached != null)
                field = cached;
            else
                field = config.getField(key);

            if (cached == null && field != null) {
                fieldCache.put(key, field);
            }

            if (field == null)
                return null;

            return (T) field.get(null);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw Skript.sneakyThrow(e);
        }
    }

}
