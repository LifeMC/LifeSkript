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

package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import ch.njol.skript.util.PatternCache;
import org.eclipse.jdt.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Peter Güttinger
 */
public final class RegexMessage extends Message {

    /**
     * A pattern that doesn't match anything
     */
    public static final Pattern nop = Pattern.compile("(?!)");
    public static final Matcher nop_matcher = nop.matcher("");
    private final String prefix, suffix;
    private final int flags;
    @Nullable
    private Pattern pattern;
    @Nullable
    private Matcher matcher;

    public RegexMessage(final String key, @Nullable final String prefix, @Nullable final String suffix, final int flags) {
        super(key);
        this.prefix = prefix == null ? "" : prefix;
        this.suffix = suffix == null ? "" : suffix;
        this.flags = flags;
    }

    public RegexMessage(final String key, final String prefix, final String suffix) {
        this(key, prefix, suffix, 0);
    }

    public RegexMessage(final String key, final int flags) {
        this(key, "", "", flags);
    }

    public RegexMessage(final String key) {
        this(key, "", "", 0);
    }

    @Nullable
    public final Pattern getPattern() {
        validate();
        return pattern;
    }

    @Nullable
    public final Matcher getMatcher() {
        validate();
        return matcher;
    }

    /**
     * Consider using direct {@link RegexMessage#matches(String)} or
     * {@link RegexMessage#find(String)}
     * <p>
     * If you need thread safety, use {@link RegexMessage#newMatcher(CharSequence)}
     */
    public final Matcher matcher(final String s) {
        final Matcher m = getMatcher();
        return (m == null ? nop_matcher : m).reset(s);
    }

    /**
     * Thread safe but creates matchers every time,
     * consider using {@link RegexMessage#matcher(String)} instead.
     */
    public final Matcher newMatcher(final CharSequence s) {
        final Pattern p = getPattern();
        return (p == null ? nop : p).matcher(s);
    }

    public final boolean matches(final String s) {
        final Matcher m = getMatcher();
        return m != null && m.reset(s).matches();
    }

    public final boolean find(final String s) {
        final Matcher m = getMatcher();
        return m != null && m.reset(s).find();
    }

    @Override
    public final String toString() {
        validate();
        return prefix + getValue() + suffix;
    }

    @Override
    protected final void onValueChange() {
        try {
            pattern = PatternCache.get(prefix + getValue() + suffix, flags);
            matcher = pattern.matcher("");
        } catch (final PatternSyntaxException e) {
            Skript.error("Invalid Regex pattern '" + getValue() + "' found at '" + key + "' in the " + Language.getName() + " language file: " + e.getLocalizedMessage());
        }
    }

}
