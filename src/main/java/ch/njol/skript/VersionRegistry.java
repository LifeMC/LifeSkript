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

package ch.njol.skript;

import ch.njol.skript.util.Version;

public final class VersionRegistry {

    public static final Version STABLE_2_2_15 = new Version(2, 2, 15);
    public static final Version STABLE_2_2_16 = new Version(2, 2, 16);
    public static final Version STABLE_2_2_17 = new Version(2, 2, 17);
    public static final Version STABLE_2_2_18 = new Version(2, 2, 18);

    private VersionRegistry() {
        throw new UnsupportedOperationException();
    }

}
