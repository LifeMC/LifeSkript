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

package ch.njol.skript.hooks.regions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.*;

/**
 * @author Peter Güttinger
 */
@RequiredPlugins("WorldGuard")
public final class WorldGuardHook extends RegionsPlugin<WorldGuardPlugin> {

    static {
        Variables.yggdrasil.registerSingleClass(WorldGuardRegion.class);
    }

    boolean supportsUUIDs;

    public WorldGuardHook() throws IOException {
    }

    @Override
    protected boolean init() {
        supportsUUIDs = Skript.methodExists(DefaultDomain.class, "getUniqueIds");
        return super.init();
    }

    @Override
    public String getName() {
        return "WorldGuard";
    }

    @Override
    public boolean canBuild_i(final Player p, final Location l) {
        return plugin.canBuild(p, l);
    }

    @SuppressWarnings({"null", "unused"})
    @Override
    public Collection<? extends Region> getRegionsAt_i(@Nullable final Location l) {
        if (l == null)
            return Collections.emptyList();
        if (l.getWorld() == null)
            return Collections.emptyList();
        final RegionManager manager = plugin.getRegionManager(l.getWorld());
        final List<Region> r = new ArrayList<>();
        if (manager == null)
            return r;
        final ApplicableRegionSet applicable = manager.getApplicableRegions(l);
        if (applicable == null)
            return r;
        for (final ProtectedRegion protectedRegion : applicable)
            r.add(new WorldGuardRegion(l.getWorld(), protectedRegion));
        return r;
    }

    @Override
    @Nullable
    public Region getRegion_i(final World world, final String name) {
        final ProtectedRegion r = plugin.getRegionManager(world).getRegion(name);
        if (r != null)
            return new WorldGuardRegion(world, r);
        return null;
    }

    @Override
    public boolean hasMultipleOwners_i() {
        return true;
    }

    @Override
    protected Class<? extends Region> getRegionClass() {
        return WorldGuardRegion.class;
    }

    @YggdrasilID("WorldGuardRegion")
    public final class WorldGuardRegion extends Region {

        final World world;
        private transient ProtectedRegion region;

        @SuppressWarnings({"null", "unused"})
        private WorldGuardRegion() {
            world = null;
        }

        public WorldGuardRegion(final World w, final ProtectedRegion r) {
            world = w;
            region = r;
        }

        @Override
        public boolean contains(final Location l) {
            return l.getWorld().equals(world) && region.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean isMember(final OfflinePlayer p) {
            if (supportsUUIDs)
                return region.isMember(plugin.wrapOfflinePlayer(p));
            return region.isMember(p.getName());
        }

        @SuppressWarnings("deprecation")
        @Override
        public Collection<OfflinePlayer> getMembers() {
            if (supportsUUIDs) {
                final Collection<UUID> ids = region.getMembers().getUniqueIds();
                final Collection<OfflinePlayer> r = new ArrayList<>(ids.size());
                for (final UUID id : ids)
                    r.add(Bukkit.getOfflinePlayer(id));
                return r;
            }
            final Collection<String> ps = region.getMembers().getPlayers();
            final Collection<OfflinePlayer> r = new ArrayList<>(ps.size());
            for (final String p : ps)
                r.add(Bukkit.getOfflinePlayer(p));
            return r;
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean isOwner(final OfflinePlayer p) {
            if (supportsUUIDs)
                return region.isOwner(plugin.wrapOfflinePlayer(p));
            return region.isOwner(p.getName());
        }

        @SuppressWarnings("deprecation")
        @Override
        public Collection<OfflinePlayer> getOwners() {
            if (supportsUUIDs) {
                final Collection<UUID> ids = region.getOwners().getUniqueIds();
                final Collection<OfflinePlayer> r = new ArrayList<>(ids.size());
                for (final UUID id : ids)
                    r.add(Bukkit.getOfflinePlayer(id));
                return r;
            }
            final Collection<String> ps = region.getOwners().getPlayers();
            final Collection<OfflinePlayer> r = new ArrayList<>(ps.size());
            for (final String p : ps)
                r.add(Bukkit.getOfflinePlayer(p));
            return r;
        }

        @Override
        public Iterator<Block> getBlocks() {
            final BlockVector min = region.getMinimumPoint(), max = region.getMaximumPoint();
            return new AABB(world, new Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ()), new Vector(max.getBlockX() + 1, max.getBlockY() + 1, max.getBlockZ() + 1)).iterator();
//			final Iterator<BlockVector2D> iter = region.getPoints().iterator();
//			if (!iter.hasNext())
//				return EmptyIterator.get();
//			return new Iterator<Block>() {
//				@SuppressWarnings("null")
//				BlockVector2D current = iter.next();
//				int height = 0;
//				final int maxHeight = world.getMaxHeight();
//
//				@SuppressWarnings("null")
//				@Override
//				public boolean hasNext() {
//					if (height >= maxHeight && iter.hasNext()) {
//						height = 0;
//						current = iter.next();
//					}
//					return height < maxHeight;
//				}
//
//				@SuppressWarnings("null")
//				@Override
//				public Block next() {
//					if (!hasNext())
//						throw new NoSuchElementException();
//					return world.getBlockAt(current.getBlockX(), height++, current.getBlockZ());
//				}
//
//				@Override
//				public void remove() {
//					throw new UnsupportedOperationException();
//				}
//			};
        }

        @Override
        public Fields serialize() throws NotSerializableException {
            final Fields f = new Fields(this);
            f.putObject("region", region.getId());
            return f;
        }

        @Override
        public void deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
            final String r = fields.getAndRemoveObject("region", String.class);
            fields.setFields(this);
            final ProtectedRegion region = plugin.getRegionManager(world).getRegion(r);
            if (region == null)
                throw new StreamCorruptedException("Invalid region " + r + " in world " + world);
            this.region = region;
        }

        @Override
        public String toString() {
            return region.getId() + " in world " + world.getName();
        }

        @Override
        public RegionsPlugin<?> getPlugin() {
            return WorldGuardHook.this;
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (o == this)
                return true;
            if (o == null)
                return false;
            if (!(o instanceof WorldGuardRegion))
                return false;
            return world.equals(((WorldGuardRegion) o).world) && region.equals(((WorldGuardRegion) o).region);
        }

        @Override
        public int hashCode() {
            return world.hashCode() * 31 + region.hashCode();
        }

    }

}
