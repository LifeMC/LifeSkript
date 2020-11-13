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
 *   Copyright (C) 2011 Peter Güttinger and contributors
 *
 */

package ch.njol.util;

import ch.njol.skript.bukkitutil.Workarounds;

import java.util.Locale;

public final class SystemUtils {

    private SystemUtils() {
        throw new UnsupportedOperationException("Static class");
    }

    public enum OperatingSystem {
        LINUX, OSX, WINDOWS, UNKNOWN
    }

    public static final OperatingSystem getOperatingSystem() {
        final String prop = Workarounds.getOriginalProperty("os.name");
        if (prop != null) {
            final String name = prop.toLowerCase(Locale.ENGLISH).trim();
            if (name.startsWith("lin")) {
                return OperatingSystem.LINUX;
            }
            if (name.startsWith("mac")) {
                return OperatingSystem.OSX;
            }
            if (name.startsWith("win")) {
                return OperatingSystem.WINDOWS;
            }
        }
        return OperatingSystem.UNKNOWN;
    }

    public static final int getBitModel() {
        String prop = Workarounds.getOriginalProperty("sun.arch.data.model");
        if (prop == null) {
            prop = Workarounds.getOriginalProperty("com.ibm.vm.bitmode");
        }
        if (prop != null) {
            return Integer.parseInt(prop);
        }
        prop = Workarounds.getOriginalProperty("os.arch");
        if (prop != null) {
            if (prop.endsWith("64")) {
                return 64;
            }
            if (prop.endsWith("86")) {
                return 32;
            }
        }
        return -1; // we don't know...
    }

}
