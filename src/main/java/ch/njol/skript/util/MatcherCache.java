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

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NotThreadSafe
public final class MatcherCache {

    @SuppressWarnings("UnstableApiUsage")
    private static final Cache<Pattern, Matcher> matcherCache = CacheBuilder.newBuilder()
            //.softValues()
            .initialCapacity(100)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .expireAfterWrite(1L, TimeUnit.MINUTES)
            .build();

    private MatcherCache() {
        throw new UnsupportedOperationException("Static class");
    }

    /**
     * Matchers are not thread safe, care must be taken if you are calling
     * this method.
     */
    public static final Matcher getMatcher(final Pattern pattern,
                                           final CharSequence input) {
        try {
            return matcherCache.get(pattern, () -> pattern.matcher("")).reset(input);
        } catch (final ExecutionException e) {
            assert false : e;

            throw Skript.sneakyThrow(e);
        }
    }

    public static final void clear() {
        matcherCache.cleanUp();

        matcherCache.invalidateAll();
        matcherCache.cleanUp();
    }

}
