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

import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public final class Timespan implements YggdrasilSerializable, Comparable<Timespan> {

    static final long[] times = {50L, 1000L, 1000L * 60L, 1000L * 60L * 60L, 1000L * 60L * 60L * 24L};
    static final HashMap<String, Long> parseValues = new HashMap<>(100);
    private static final Noun m_tick = new Noun("time.tick");
    private static final Noun m_second = new Noun("time.second");
    private static final Noun m_minute = new Noun("time.minute");
    private static final Noun m_hour = new Noun("time.hour");
    private static final Noun m_day = new Noun("time.day");
    static final Noun[] names = {m_tick, m_second, m_minute, m_hour, m_day};
    @SuppressWarnings("unchecked")
    static final NonNullPair<Noun, Long>[] simpleValues = CollectionUtils.array(new NonNullPair<>(m_day, 1000L * 60 * 60 * 24), new NonNullPair<>(m_hour, 1000L * 60 * 60), new NonNullPair<>(m_minute, 1000L * 60), new NonNullPair<>(m_second, 1000L)
    );
    private static final Pattern TIMESPAN_SPLIT = Pattern.compile("\\s+");
    private static final Pattern TIMESPAN_PATTERN = Pattern.compile("^\\d+:\\d\\d(:\\d\\d)?(\\.\\d{1,4})?$");
    private static final Matcher TIMESPAN_PATTERN_MATCHER = TIMESPAN_PATTERN.matcher("");
    private static final Pattern TIMESPAN_SPLIT_TWO = Pattern.compile("[:.]");
    private static final Pattern TIMESPAN_DOUBLE_PATTERN = Pattern.compile("^\\d+(.\\d+)?$");
    private static final Matcher TIMESPAN_DOUBLE_PATTERN_MATCHER = TIMESPAN_DOUBLE_PATTERN.matcher("");

    static {
        Language.addListener(() -> {
            for (int i = 0; i < names.length; i++) {
                parseValues.put(names[i].getSingular().toLowerCase(Locale.ENGLISH), times[i]);
                parseValues.put(names[i].getPlural().toLowerCase(Locale.ENGLISH), times[i]);
            }
        });
    }

    private final long millis;

    public Timespan() {
        millis = 0;
    }

    public Timespan(final long millis) {
        if (millis < 0)
            throw new IllegalArgumentException("millis must be >= 0");
        this.millis = millis;
    }

    public Timespan(final long time, final TimeUnit unit) {
        this(unit.toMillis(time));
    }

    @Nullable
    public static final Timespan parse(String s) {
        if (s.isEmpty())
            return null;
        s = s.trim();

        long t = 0;
        if (TIMESPAN_PATTERN_MATCHER.reset(s).matches()) { // MM:SS[.ms] or HH:MM:SS[.ms]
            final String[] ss = TIMESPAN_SPLIT_TWO.split(s);
            final long[] times = {1000L * 60L * 60L, 1000L * 60L, 1000L, 1L}; // h, m, s, ms

            final int offset = ss.length == 3 && !s.contains(".") || ss.length == 4 ? 0 : 1;
            for (int i = 0; i < ss.length; i++) {
                t += times[offset + i] * Utils.parseLong(ss[i]);
            }
        } else {
            final String[] subs = TIMESPAN_SPLIT.split(s.toLowerCase(Locale.ENGLISH));
            boolean minecraftTime = false;
            boolean isMinecraftTimeSet = false;
            for (int i = 0; i < subs.length; i++) {
                String sub = subs[i];

                if (sub.equals(GeneralWords.and.toString())) {
                    if (i == 0 || i == subs.length - 1)
                        return null;
                    continue;
                }

                double amount = 1;
                if (Noun.isIndefiniteArticle(sub)) {
                    if (i == subs.length - 1)
                        return null;
                    amount = 1;
                    sub = subs[++i];
                } else if (TIMESPAN_DOUBLE_PATTERN_MATCHER.reset(sub).matches()) {
                    if (i == subs.length - 1)
                        return null;
                    if (!SkriptParser.isIntegerOrDouble(sub))
                        throw new IllegalArgumentException("invalid timespan: " + s);
                    amount = Double.parseDouble(sub);
                    sub = subs[++i];
                }

                if (CollectionUtils.contains(Language.getList("time.real"), sub)) {
                    if (i == subs.length - 1 || isMinecraftTimeSet && minecraftTime)
                        return null;
                    sub = subs[++i];
                } else if (CollectionUtils.contains(Language.getList("time.minecraft"), sub)) {
                    if (i == subs.length - 1 || isMinecraftTimeSet && !minecraftTime)
                        return null;
                    minecraftTime = true;
                    sub = subs[++i];
                }

                if (!sub.isEmpty() && sub.charAt(sub.length() - 1) == ',')
                    sub = sub.substring(0, sub.length() - 1);

                final Long d = parseValues.get(sub.toLowerCase(Locale.ENGLISH));
                if (d == null)
                    return null;

                if (minecraftTime && d != times[0]) // times[0] == tick
                    amount *= 72F;

                t += Math.round(amount * d);

                isMinecraftTimeSet = true;

            }
        }
        return new Timespan(t);
    }

    /**
     * @deprecated Use fromTicks_i(long ticks) instead. Since this method limits timespan to 50 * Integer.MAX_VALUE.
     * I only keep this to allow for older addons to still work. / Mirre
     */
    @Deprecated
    public static final Timespan fromTicks(final int ticks) {
        return new Timespan(ticks * 50L);
    }

    public static final Timespan fromTicks_i(final long ticks) {
        return new Timespan(ticks * 50L);
    }

    public static final String toString(final long millis) {
        return toString(millis, 0);
    }

    @SuppressWarnings("null")
    public static final String toString(final long millis, final int flags) {
        for (int i = 0; i < simpleValues.length - 1; i++) {
            if (millis >= simpleValues[i].getSecond()) {
                final double second = 1. * (millis % simpleValues[i].getSecond()) / simpleValues[i + 1].getSecond();
                if (second != 0) {
                    return toString(Math.floor(1. * millis / simpleValues[i].getSecond()), simpleValues[i], flags) + ' ' + GeneralWords.and + ' ' + toString(second, simpleValues[i + 1], flags);
                }
                return toString(1. * millis / simpleValues[i].getSecond(), simpleValues[i], flags);
            }
        }
        return toString(1. * millis / simpleValues[simpleValues.length - 1].getSecond(), simpleValues[simpleValues.length - 1], flags);
    }

    private static final String toString(final double amount, final NonNullPair<Noun, Long> p, final int flags) {
        return p.getFirst().withAmount(amount, flags);
    }

    public long getMilliSeconds() {
        return millis;
    }

    public long getTicks_i() {
        return Math.round(millis / 50.0);
    }

    /**
     * @addon I only keep this to allow for older addons to still work. / Mirre
     * @Well if need the ticks because of a method that takes a int input it doesn't really matter.
     * @deprecated Use getTicks_i() instead. Since this method limits timespan to Integer.MAX_VALUE.
     */
    @Deprecated
    public int getTicks() {
        return Math.round((millis >= Float.MAX_VALUE ? Float.MAX_VALUE : millis) / 50F);
    }

    @Override
    public String toString() {
        return toString(millis);
    }

    public String toString(final int flags) {
        return toString(millis, flags);
    }

    @Override
    public int compareTo(@Nullable final Timespan o) {
        final long d = o == null ? millis : millis - o.millis;
        return d > 0 ? 1 : d < 0 ? -1 : 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (millis / Integer.MAX_VALUE);
        return result;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Timespan))
            return false;
        final Timespan other = (Timespan) obj;
        return millis == other.millis;
    }

}
