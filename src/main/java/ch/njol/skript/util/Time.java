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
import ch.njol.skript.localization.Message;
import ch.njol.util.Math2;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.eclipse.jdt.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public final class Time implements YggdrasilSerializable {

    private static final int TICKS_PER_HOUR = 1000, TICKS_PER_DAY = 24 * TICKS_PER_HOUR;
    private static final double TICKS_PER_MINUTE = 1000. / 60;
    /**
     * 0 ticks == 6:00
     */
    private static final int HOUR_ZERO = 6 * TICKS_PER_HOUR;
    private static final Message m_error_24_hours = new Message("time.errors.24 hours");
    private static final Message m_error_12_hours = new Message("time.errors.12 hours");
    private static final Message m_error_60_minutes = new Message("time.errors.60 minutes");
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d?\\d:\\d\\d");
    private static final Matcher TIME_PATTERN_MATCHER = TIME_PATTERN.matcher("");
    private static final Pattern DETAILED_TIME_PATTERN = Pattern.compile("(\\d?\\d)(:(\\d\\d))? ?(am|pm)", Pattern.CASE_INSENSITIVE);
    private static final Matcher DETAILED_TIME_PATTERN_MATCHER = DETAILED_TIME_PATTERN.matcher("");
    private final int time;

    public Time() {
        time = 0;
    }

    public Time(final int time) {
        this.time = Math2.mod(time, TICKS_PER_DAY);
    }

    public static final String toString(final int ticks) {
        assert 0 <= ticks && ticks < TICKS_PER_DAY;
        final int t = (ticks + HOUR_ZERO) % TICKS_PER_DAY;
        int hours = t / TICKS_PER_HOUR;
        int minutes = (int) Math.round(t % TICKS_PER_HOUR / TICKS_PER_MINUTE);
        if (minutes >= 60) {
            hours = (hours + 1) % 24;
            minutes -= 60;
        }
        return hours + ":" + (minutes < 10 ? "0" : "") + minutes;
    }

    /**
     * @param s The trim()med string to parse
     * @return The parsed time of null if the input was invalid
     */
    @SuppressWarnings("null")
    @Nullable
    public static final Time parse(String s) {
        if (s.isEmpty())
            return null;
        s = s.trim();

//		if (s.matches("\\d+")) {
//			return new Time(Integer.parseInt(s));
//		} else
        if (TIME_PATTERN_MATCHER.reset(s).matches()) {
            int hours = Utils.parseInt(s.split(":")[0]);
            if (hours == 24) { // allows to write 24:00 - 24:59 instead of 0:00-0:59
                hours = 0;
            } else if (hours > 24) {
                Skript.error(m_error_24_hours.toString());
                return null;
            }
            final int minutes = Utils.parseInt(s.split(":")[1]);
            if (minutes >= 60) {
                Skript.error(m_error_60_minutes.toString());
                return null;
            }
            return new Time((int) Math.round(hours * TICKS_PER_HOUR - HOUR_ZERO + minutes * TICKS_PER_MINUTE));
        }
        final Matcher m = DETAILED_TIME_PATTERN_MATCHER.reset(s);
        if (m.matches()) {
            int hours = Utils.parseInt(m.group(1));
            if (hours == 12) {
                hours = 0;
            } else if (hours > 12) {
                Skript.error(m_error_12_hours.toString());
                return null;
            }
            int minutes = 0;
            if (m.group(3) != null)
                minutes = Utils.parseInt(m.group(3));
            if (minutes >= 60) {
                Skript.error(m_error_60_minutes.toString());
                return null;
            }
            if ("pm".equalsIgnoreCase(m.group(4)))
                hours += 12;
            return new Time((int) Math.round(hours * TICKS_PER_HOUR - HOUR_ZERO + minutes * TICKS_PER_MINUTE));
        }
        return null;
    }

    /**
     * @return Ticks in Minecraft time (0 ticks == 6:00)
     */
    public int getTicks() {
        return time;
    }

    /**
     * @return Ticks in day time (0 ticks == 0:00)
     */
    public int getTime() {
        return (time + HOUR_ZERO) % TICKS_PER_DAY;
    }

    @Override
    public String toString() {
        return toString(time);
    }

    @Override
    public int hashCode() {
        return time;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Time))
            return false;
        final Time other = (Time) obj;
        return time == other.time;
    }

}
