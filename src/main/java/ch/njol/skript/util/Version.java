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

import org.eclipse.jdt.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public final class Version implements Serializable, Comparable<Version> {
    @SuppressWarnings("null")
    public static final Pattern versionPattern = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?\\s*(.*)");
    private static final long serialVersionUID = 8687040355286333293L;
    private final int[] version = new int[3];
    /**
     * Everything after the version, e.g. "alpha", "b", "rc 1", "build 2314", "-SNAPSHOT" etc. or null if nothing.
     */
    @Nullable
    private final String postfix;

    public Version(final int... version) {
        final int[] ver = new int[3];

        for (int i = 0; i < version.length; i++) {
            if (ver.length > i && version.length > i)
                ver[i] = version[i];
        }

        System.arraycopy(ver, 0, this.version, 0, ver.length);
        postfix = null;
    }

    public Version(final int major) {
        this(major, 0);
    }

    public Version(final int major, final int minor) {
        //noinspection ConstantConditions
        this(major, minor, (String[]) null);
    }

    public Version(final int major, final int minor, final @Nullable String... postfix) {
        version[0] = major;
        version[1] = minor;

        final StringBuilder stringBuilder = new StringBuilder(4096);

        if (postfix != null)
            for (final String str : postfix)
                stringBuilder.append(str);

        final String suffix = stringBuilder.toString();
        this.postfix = suffix.isEmpty() ? null : suffix;
    }

    public Version(final String version) {
        this(version, false);
    }

    public Version(String version, final boolean failSafe) {
        Matcher m = versionPattern.matcher(version.trim());
        String postfixStr = "";

        if (!m.matches()) {
            if (!failSafe)
                throw new IllegalArgumentException("'" + version + "' is not a valid version string");
            // Remove any non-digit character to get a "meaningful" version string.
            final StringBuilder stringBuilder = new StringBuilder(4096);
            final StringBuilder postfixBuilder = new StringBuilder(4096);

            int index = 0;

            final char[] characters = version.toCharArray();

            for (final char ch : characters) {
                // Continue if it's not a digit between 0 and 9 (and it's not a dot)
                // We don't use Character#isDigit because it also includes some
                // strange numbers in different locales.
                if ((ch < '0' || ch > '9') && (ch != '.' || index == characters.length))
                    postfixBuilder.append(ch);
                else
                    stringBuilder.append(ch);

                index++;
            }

            version = stringBuilder.toString().trim();

            if (version.endsWith("."))
                version = version.substring(0, version.length() - 1);

            if (version.length() == 1)
                version += ".0";

            m = versionPattern.matcher(version.trim());

            // If it still fails.. Then probably it's a bad argument.
            if (!m.matches())
                throw new IllegalArgumentException("'" + version + "' is not a valid version string");

            postfixStr = postfixBuilder.toString().trim();
        }

        for (int i = 0; i < 3; i++) {
            if (m.group(i + 1) != null)
                this.version[i] = Utils.parseInt(m.group(i + 1));
        }

        if (!postfixStr.isEmpty())
            postfix = postfixStr;
        else
            postfix = m.group(m.groupCount()).isEmpty() ? null : m.group(m.groupCount());
    }

    public static final int compare(final String v1, final String v2) {
        return new Version(v1).compareTo(new Version(v2));
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Version))
            return false;
        return compareTo((Version) obj) == 0;
    }

    @Override
    public int hashCode() {
        final String pf = postfix;
        return Arrays.hashCode(version) * 31 + (pf == null ? 0 : pf.hashCode());
    }

    @Override
    public int compareTo(final @Nullable Version other) {
        if (other == null)
            return 1;
        for (int i = 0; i < version.length; i++) {
            if (version[i] > other.version[i])
                return 1;
            if (version[i] < other.version[i])
                return -1;
        }
        final String pf = postfix;
        if (pf == null)
            return other.postfix == null ? 0 : 1;
        return other.postfix == null ? -1 : pf.compareTo(other.postfix);
    }

    public int compareTo(final int... other) {
        assert other.length >= 2 && other.length <= 3;
        for (int i = 0; i < version.length; i++) {
            if (version[i] > (i >= other.length ? 0 : other[i]))
                return 1;
            if (version[i] < (i >= other.length ? 0 : other[i]))
                return -1;
        }
        return 0;
    }

    public boolean isSmallerThan(final Version other) {
        return compareTo(other) < 0;
    }

    public boolean isLargerThan(final Version other) {
        return compareTo(other) > 0;
    }

    /**
     * @return Whatever this is a stable version, i.e. a simple version number without any additional details (like alpha/beta/etc.)
     */
    public boolean isStable() {
        return postfix == null;
    }

    public int getMajor() {
        return version[0];
    }

    public int getMinor() {
        return version[1];
    }

    public int getRevision() {
        return version.length == 2 ? 0 : version[2];
    }

    @Override
    public String toString() {
        final String pf = postfix;
        return version[0] + "." + version[1] + (version[2] == 0 ? "" : "." + version[2]) + (pf == null ? "" : pf.startsWith("-") ? pf : " " + pf);
    }
}
