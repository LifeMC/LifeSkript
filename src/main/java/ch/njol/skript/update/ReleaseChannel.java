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

package ch.njol.skript.update;

import ch.njol.skript.util.Version;
import ch.njol.util.StringUtils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Represents a {@link ReleaseChannel}.
 */
public enum ReleaseChannel {

    /**
     * Represents a stable release.
     * Stable releases are end-user production ready releases.
     */
    STABLE(0),

    /**
     * Represents a pre/preview-release.
     * <p>
     * Preview releases are used to indicate an update is mostly
     * ready but still not production ready.
     */
    PREVIEW(1, null, "pre"),

    /**
     * Represents a beta release.
     * <p>
     * Beta releases are not finished yet, so it's better
     * to use either {@link ReleaseChannel#PREVIEW} or {@link ReleaseChannel#STABLE}.
     */
    BETA(2, null, "beta"),

    /**
     * Represents an early alpha release.
     * <p>
     * Alpha releases are mostly unstable and contains more
     * unfinished work and bugs than {@link ReleaseChannel#BETA}
     */
    ALPHA(3, null, "alpha"),

    /**
     * Represents a dev/development release.
     * <p>
     * This generally not released to public, only sent people
     * to test certain things.
     */
    DEV(4, "dev", "dev"),

    /**
     * Represents a nightly/self built release.
     * <p>
     * The difference between {@link ReleaseChannel#DEV} and this
     * is, in dev the developer sends a private dev/test version,
     * <p>
     * but in nightly user or anyone can build and publish
     * releases, and these are considered {@link ReleaseChannel#NIGHTLY}
     */
    NIGHTLY(5);

    private static final ReleaseChannel[] values =
            values();

    private static final Map<String, ReleaseChannel> cache =
            new HashMap<>(values.length);

    private static final Set<Map.Entry<String, ReleaseChannel>> cacheSet;

    static {
        for (final ReleaseChannel releaseChannel : values)
            cache.put(releaseChannel.name(), releaseChannel);
        cacheSet = cache.entrySet();
    }

    private final int id;
    @Nullable
    private final String prefix;
    @Nullable
    private final String suffix;

    ReleaseChannel(final int id) {
        this(id, null);
    }

    ReleaseChannel(final int id,
                   @Nullable final String prefix) {
        this(id, prefix, null);
    }

    ReleaseChannel(final int id,
                   @Nullable final String prefix,
                   @Nullable final String suffix) {
        this.id = id;

        this.prefix = prefix;
        this.suffix = suffix;
    }

    public static final ReleaseChannel mostStable() {
        return STABLE;
    }

    public static final ReleaseChannel mostUnstable() {
        return NIGHTLY;
    }

    @Nullable
    public static final ReleaseChannel getByExactName(final String s) {
        return cache.get(s);
    }

    @Nullable
    public static final ReleaseChannel getByStartsWith(final String s) {
        for (final Map.Entry<String, ReleaseChannel> entry : cacheSet) {
            final String name = entry.getKey();

            if (StringUtils.startsWithIgnoreCase(s, name))
                return entry.getValue();
        }

        return null;
    }

    @Nullable
    public static final ReleaseChannel getByEndsWith(final String s) {
        for (final Map.Entry<String, ReleaseChannel> entry : cacheSet) {
            final String name = entry.getKey();

            if (StringUtils.endsWithIgnoreCase(s, name))
                return entry.getValue();
        }

        return null;
    }

    @Nullable
    public static final ReleaseChannel getByContains(final String s) {
        for (final Map.Entry<String, ReleaseChannel> entry : cacheSet) {
            final String name = entry.getKey();

            if (org.apache.commons.lang.StringUtils.containsIgnoreCase(s, name))
                return entry.getValue();
        }

        return null;
    }

    @Nullable
    public static final ReleaseChannel getByPrefix(final String s) {
        for (final Map.Entry<String, ReleaseChannel> entry : cacheSet) {
            final ReleaseChannel releaseChannel = entry.getValue();
            final String prefix = releaseChannel.prefix;

            if (prefix == null)
                continue;

            if (StringUtils.startsWithIgnoreCase(s, prefix))
                return releaseChannel;
        }

        for (final Map.Entry<String, ReleaseChannel> entry : cacheSet) {
            final ReleaseChannel releaseChannel = entry.getValue();
            final String prefix = releaseChannel.prefix;

            if (prefix == null)
                continue;

            if (s.contains(prefix))
                return releaseChannel;
        }

        return null;
    }

    @Nullable
    public static final ReleaseChannel getBySuffix(final String s) {
        for (final Map.Entry<String, ReleaseChannel> entry : cacheSet) {
            final ReleaseChannel releaseChannel = entry.getValue();
            final String suffix = releaseChannel.suffix;

            if (suffix == null)
                continue;

            if (StringUtils.endsWithIgnoreCase(s, suffix))
                return releaseChannel;
        }

        for (final Map.Entry<String, ReleaseChannel> entry : cacheSet) {
            final ReleaseChannel releaseChannel = entry.getValue();
            final String suffix = releaseChannel.suffix;

            if (suffix == null)
                continue;

            if (s.contains(suffix))
                return releaseChannel;
        }

        return null;
    }

    @Nullable
    public static final ReleaseChannel parse(final String s) {
        // Exact uppercase match

        final ReleaseChannel exact = getByExactName(s.toUpperCase(Locale.ENGLISH));

        if (exact != null)
            return exact;

        // Starts with lowercase match

        final ReleaseChannel startsWith = getByStartsWith(s);

        if (startsWith != null)
            return startsWith;

        // Ends with lowercase match

        final ReleaseChannel endsWith = getByEndsWith(s);

        if (endsWith != null)
            return endsWith;

        // Contains lowercase match

        final ReleaseChannel contains = getByContains(s);

        if (contains != null)
            return contains;

        // Prefix lowercase match

        final ReleaseChannel prefix = getByPrefix(s);

        if (prefix != null)
            return prefix;

        // Suffix lowercase match

        final ReleaseChannel suffix = getBySuffix(s);

        if (suffix != null)
            return suffix;

        // Version match

        try {
            return new Version(s, true).isStable() ? STABLE : NIGHTLY;
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    public static final ReleaseChannel parseOrNightly(final String s) {
        final ReleaseChannel parse = parse(s);
        return parse != null ? parse : NIGHTLY;
    }

    public static final ReleaseChannel parseOrStable(final String s) {
        final ReleaseChannel parse = parse(s);
        return parse != null ? parse : STABLE;
    }

    /**
     * Gets the prefix of this update in
     * version strings.<p />
     * <p>
     * <p />Can return null.
     *
     * @return The prefix of this update in
     * version strings.
     */
    @Nullable
    public final String getPrefix() {
        return prefix;
    }

    /**
     * Gets the suffix of this update in
     * version strings.<p />
     * <p>
     * <p />Can return null.
     *
     * @return The suffix of this update in
     * version strings.
     */
    @Nullable
    public final String getSuffix() {
        return suffix;
    }

    /**
     * Gets one release channel up comes
     * before this one.
     * <p>
     * The returned {@link ReleaseChannel}
     * will be more stable.
     * <p>
     * If this the most stable {@link ReleaseChannel}
     * it returns itself.
     *
     * @return The one release channel up
     * comes before this one.
     */
    public final ReleaseChannel up() {
        if (this == mostStable())
            return this;
        return values[id - 1];
    }

    /**
     * Gets one release channel down comes
     * after this one.
     * <p>
     * The returned {@link ReleaseChannel}
     * will be more unstable.
     * <p>
     * If this the most unstable {@link ReleaseChannel},
     * it returns itself.
     *
     * @return The one release channel down
     * comes after this one.
     */
    public final ReleaseChannel down() {
        if (this == mostUnstable())
            return this;
        return values[id + 1];
    }

}
