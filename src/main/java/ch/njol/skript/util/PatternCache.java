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

import ch.njol.skript.Skript;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class PatternCache {

    @SuppressWarnings("UnstableApiUsage")
    private static final Cache<String, Pattern> patternCache = CacheBuilder.newBuilder()
            //.softValues()
            .initialCapacity(100)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .expireAfterWrite(1L, TimeUnit.MINUTES)
            .build();

    private PatternCache() {
        throw new UnsupportedOperationException("Static class");
    }

    public static final Pattern get(final String pattern) {
        return get(pattern, -1);
    }

    public static final Pattern get(final String pattern, final int flags) throws PatternSyntaxException {
        try {
            return patternCache.get(pattern, () -> flags == -1 ? Pattern.compile(pattern) : Pattern.compile(pattern, flags));
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof PatternSyntaxException)
                throw (PatternSyntaxException) e.getCause();

            assert false : e;
            throw Skript.sneakyThrow(e);
        }
    }

    public static final void clear() {
        patternCache.cleanUp();

        patternCache.invalidateAll();
        patternCache.cleanUp();
    }

}
