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

package ch.njol.util;

import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Locale;

/**
 * A string, but it is compared ignoring its case.
 *
 * @author Peter Güttinger
 */
public final class CaseInsensitiveString implements Serializable, Comparable<CharSequence>, CharSequence {

    private static final long serialVersionUID = 1205018864604639962L;

    private final String s;
    private final String lc;

    private final Locale locale;

    @SuppressWarnings("null")
    public CaseInsensitiveString(final String s) {
        this.s = s;
        locale = Locale.getDefault();
        lc = "" + s.toLowerCase(locale);
    }

    public CaseInsensitiveString(final String s, final Locale locale) {
        this.s = s;
        this.locale = locale;
        lc = "" + s.toLowerCase(locale);
    }

    @Override
    public int hashCode() {
        return lc.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o instanceof CharSequence)
            return ((CharSequence) o).toString().toLowerCase(locale).equals(lc);
        return false;
    }

    @NotNull
    @Override
    public String toString() {
        return s;
    }

    @Override
    public char charAt(final int i) {
        return s.charAt(i);
    }

    @Override
    public int length() {
        return s.length();
    }

    @Override
    public CaseInsensitiveString subSequence(final int start, final int end) {
        return new CaseInsensitiveString("" + s.substring(start, end), locale);
    }

    @SuppressWarnings("null")
    @Override
    public int compareTo(final CharSequence s) {
        return lc.compareTo(s.toString().toLowerCase(locale));
    }
}
