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

package ch.njol.skript.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

public final class ConstructorCache {

    @SuppressWarnings("UnstableApiUsage")
    private static final Cache<Class<?>, Constructor<?>> constructorCache = CacheBuilder.newBuilder()
            //.softValues()
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .expireAfterWrite(1L, TimeUnit.MINUTES)
            .build();

    private ConstructorCache() {
        throw new UnsupportedOperationException("Static class");
    }

    public static final <T> Constructor<T> get(final Class<T> clazz) throws NoSuchMethodException {
        final Constructor<?> cached = constructorCache.getIfPresent(clazz);
        if (cached != null)
            return (Constructor<T>) cached;

        final Constructor<T> computed = clazz.getConstructor();
        constructorCache.put(clazz, computed);

        return computed;
    }

    public static final void clear() {
        constructorCache.cleanUp();

        constructorCache.invalidateAll();
        constructorCache.cleanUp();
    }

}
