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
import ch.njol.util.NullableChecker;
import ch.njol.util.coll.iterator.CheckedIterator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class BlockSphereIterator extends CheckedIterator<Block> {

    public BlockSphereIterator(final Location center, final double radius) {
        super(new AABB(center, radius + 0.5001, radius + 0.5001, radius + 0.5001).iterator(), new RadiusChecker(radius, center));
    }

    private static final class RadiusChecker implements NullableChecker<Block> {
        private final Location center;
        private final double rSquared;

        RadiusChecker(final double radius, final Location center) {
            this.center = center;
            rSquared = radius * radius * Skript.EPSILON_MULT;
        }

        @Override
        public final boolean check(@Nullable final Block b) {
            return b != null && center.distanceSquared(b.getLocation().add(0.5, 0.5, 0.5)) < rSquared;
        }
    }
}
