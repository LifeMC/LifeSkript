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

import org.bukkit.block.Biome;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class BiomeUtils {

    private static final EnumUtils<Biome> util = new EnumUtils<>(Biome.class, "biomes");

    private BiomeUtils() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static final Biome parse(final String s) {
        return util.parse(s);
    }

    public static final String toString(final Biome b, final int flags) {
        return util.toString(b, flags);
    }

    public static final String getAllNames() {
        return util.getAllNames();
    }

}
