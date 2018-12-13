/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.util.Math2;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;

/**
 * AABB = Axis-Aligned Bounding Box
 * 
 * @author Peter Güttinger
 */
public class AABB implements Iterable<Block> {
	
	final World world;
	final Vector lowerBound, upperBound;
	
	//	private final static Vector EPSILON = new Vector(Skript.EPSILON, Skript.EPSILON, Skript.EPSILON);
	
	@SuppressWarnings("null")
	public AABB(final Location l1, final Location l2) {
		if (l1.getWorld() != l2.getWorld())
			throw new IllegalArgumentException("Locations must be in the same world");
		world = l1.getWorld();
		lowerBound = new Vector(Math.min(l1.getX(), l2.getX()), Math.min(l1.getY(), l2.getY()), Math.min(l1.getZ(), l2.getZ()));
		upperBound = new Vector(Math.max(l1.getX(), l2.getX()), Math.max(l1.getY(), l2.getY()), Math.max(l1.getZ(), l2.getZ()));
	}
	
	@SuppressWarnings("null")
	public AABB(final Block b1, final Block b2) {
		if (b1.getWorld() != b2.getWorld())
			throw new IllegalArgumentException("Blocks must be in the same world");
		world = b1.getWorld();
		lowerBound = new Vector(Math.min(b1.getX(), b2.getX()), Math.min(b1.getY(), b2.getY()), Math.min(b1.getZ(), b2.getZ()));
		upperBound = new Vector(Math.max(b1.getX(), b2.getX()) + 1, Math.max(b1.getY(), b2.getY()) + 1, Math.max(b1.getZ(), b2.getZ()) + 1);
	}
	
	@SuppressWarnings("null")
	public AABB(final Location center, final double rX, final double rY, final double rZ) {
		assert rX >= 0 && rY >= 0 && rZ >= 0 : rX + "," + rY + "," + rY;
		world = center.getWorld();
		lowerBound = new Vector(center.getX() - rX, Math.max(center.getY() - rY, 0), center.getZ() - rZ);
		upperBound = new Vector(center.getX() + rX, Math.min(center.getY() + rY, world.getMaxHeight()), center.getZ() + rZ);
	}
	
	public AABB(final World w, final Vector v1, final Vector v2) {
		world = w;
		lowerBound = new Vector(Math.min(v1.getX(), v2.getX()), Math.min(v1.getY(), v2.getY()), Math.min(v1.getZ(), v2.getZ()));
		upperBound = new Vector(Math.max(v1.getX(), v2.getX()) + 1, Math.max(v1.getY(), v2.getY()) + 1, Math.max(v1.getZ(), v2.getZ()) + 1);
	}
	
	@SuppressWarnings("null")
	public AABB(final Chunk c) {
		world = c.getWorld();
		lowerBound = c.getBlock(0, 0, 0).getLocation().toVector();
		upperBound = lowerBound.clone().add(new Vector(16, world.getMaxHeight(), 16));
	}
	
	public boolean contains(final Location l) {
		if (l.getWorld() != world)
			return false;
		return lowerBound.getX() - Skript.EPSILON < l.getX() && l.getX() < upperBound.getX() + Skript.EPSILON && lowerBound.getY() - Skript.EPSILON < l.getY() && l.getY() < upperBound.getY() + Skript.EPSILON && lowerBound.getZ() - Skript.EPSILON < l.getZ() && l.getZ() < upperBound.getZ() + Skript.EPSILON;
	}
	
	@SuppressWarnings("null")
	public boolean contains(final Block b) {
		return contains(b.getLocation()) && contains(b.getLocation().add(1, 1, 1));
	}
	
	@SuppressWarnings("null")
	public Vector getDimensions() {
		return upperBound.clone().subtract(lowerBound);
	}
	
	public World getWorld() {
		return world;
	}
	
	/**
	 * Returns an iterator which iterates over all blocks that are in this AABB
	 */
	@Override
	public Iterator<Block> iterator() {
		return new Iterator<Block>() {
			private final int minX = Math2.ceilI(lowerBound.getX() - Skript.EPSILON),
					minY = Math2.ceilI(lowerBound.getY() - Skript.EPSILON),
					minZ = Math2.ceilI(lowerBound.getZ() - Skript.EPSILON);
			private final int maxX = Math2.floorI(upperBound.getX() + Skript.EPSILON) - 1,
					maxY = Math2.floorI(upperBound.getY() + Skript.EPSILON) - 1,
					maxZ = Math2.floorI(upperBound.getZ() + Skript.EPSILON) - 1;
			
			private int x = minX - 1,// next() increases x by one immediately
					y = minY,
					z = minZ;
			
			@Override
			public boolean hasNext() {
				return y <= maxY && (x != maxX || y != maxY || z != maxZ);
			}
			
			@SuppressWarnings("null")
			@Override
			public Block next() {
				if (!hasNext())
					throw new NoSuchElementException();
				x++;
				if (x > maxX) {
					x = minX;
					z++;
					if (z > maxZ) {
						z = minZ;
						y++;
					}
				}
				if (y > maxY)
					throw new NoSuchElementException();
				return world.getBlockAt(x, y, z);
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lowerBound.hashCode();
		result = prime * result + upperBound.hashCode();
		result = prime * result + world.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AABB))
			return false;
		final AABB other = (AABB) obj;
		if (!lowerBound.equals(other.lowerBound))
			return false;
		if (!upperBound.equals(other.upperBound))
			return false;
		if (!world.equals(other.world)) //NOSONAR
			return false;
		return true;
	}
	
}
