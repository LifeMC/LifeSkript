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
import ch.njol.util.Math2;
import ch.njol.util.NullableChecker;
import ch.njol.util.coll.iterator.StoppableIterator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class BlockLineIterator extends StoppableIterator<Block> {

    /**
     * @param start
     * @param end
     * @throws IllegalStateException randomly (Bukkit bug)
     */
    @SuppressWarnings("null")
    public BlockLineIterator(final Block start, final Block end) throws IllegalStateException {
        super(new BlockIterator(start.getWorld(), fitInWorld(start.getLocation().add(0.5, 0.5, 0.5), end.getLocation().subtract(start.getLocation()).toVector()), end.equals(start) ? new Vector(1, 0, 0) : end.getLocation().subtract(start.getLocation()).toVector(), 0, 0), // should prevent an error if start = end
                new MissingEndBlockChecker(start, end), true);
    }

    /**
     * @param start
     * @param dir
     * @param dist
     * @throws IllegalStateException randomly (Bukkit bug)
     */
    public BlockLineIterator(final Location start, final Vector dir, final double dist) throws IllegalStateException {
        super(new BlockIterator(start.getWorld(), fitInWorld(start, dir), dir, 0, 0), new BlockNullableChecker(dist, start), false);
    }

    /**
     * @param start
     * @param dir
     * @param dist
     * @throws IllegalStateException randomly (Bukkit bug)
     */
    @SuppressWarnings("null")
    public BlockLineIterator(final Block start, final Vector dir, final double dist) throws IllegalStateException {
        this(start.getLocation().add(0.5, 0.5, 0.5), dir, dist);
    }

    @SuppressWarnings("null")
    private static final Vector fitInWorld(final Location l, final Vector dir) {
        if (0 <= l.getBlockY() && l.getBlockY() < l.getWorld().getMaxHeight())
            return l.toVector();
        final double y = Math2.fit(0, l.getY(), l.getWorld().getMaxHeight());
        if (Math.abs(dir.getY()) < Skript.EPSILON)
            return new Vector(l.getX(), y, l.getZ());
        final double dy = y - l.getY();
        final double n = dy / dir.getY();
        return l.toVector().add(dir.clone().multiply(n));
    }

    private static final class MissingEndBlockChecker implements NullableChecker<Block> {
        private final double overshotSq;

        private final Block start;
        private final Block end;

        MissingEndBlockChecker(final Block start, final Block end) {
            this.start = start;
            this.end = end;
            overshotSq = Math.pow(start.getLocation().distance(end.getLocation()) + 2, 2);
        }

        @Override
        public final boolean check(@Nullable final Block b) {
            assert b != null;
            if (b.getLocation().distanceSquared(start.getLocation()) > overshotSq)
                throw new IllegalStateException("BlockLineIterator missed the end block!");
            return b.equals(end);
        }
    }

    private static final class BlockNullableChecker implements NullableChecker<Block> {
        private final Location start;
        private final double distSq;

        BlockNullableChecker(final double dist, final Location start) {
            this.start = start;
            distSq = dist * dist;
        }

        @Override
        public final boolean check(@Nullable final Block b) {
            return b != null && b.getLocation().add(0.5, 0.5, 0.5).distanceSquared(start) >= distSq;
        }
    }
}
