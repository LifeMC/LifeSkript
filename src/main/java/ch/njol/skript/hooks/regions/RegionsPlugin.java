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

import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.hooks.Hook;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.ClassResolver;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * @author Peter Güttinger
 */
// REMIND support more plugins?
@RequiredPlugins("A region plugin")
public abstract class RegionsPlugin<P extends Plugin> extends Hook<P> {

    public static final List<RegionsPlugin<?>> plugins = new ArrayList<>(2);

    static {
        Variables.yggdrasil.registerClassResolver(new RegionsPluginClassResolver());
    }

    private static final class RegionsPluginClassResolver implements ClassResolver {
        RegionsPluginClassResolver() {
            /* implicit super call */
        }

        @Override
        @Nullable
        public final String getID(final Class<?> c) {
            for (final RegionsPlugin<?> p : plugins)
                if (p.getRegionClass() == c)
                    return c.getSimpleName();
            return null;
        }

        @Override
        @Nullable
        public final Class<?> getClass(final String id) {
            for (final RegionsPlugin<?> p : plugins)
                if (id.equals(p.getRegionClass().getSimpleName()))
                    return p.getRegionClass();
            return null;
        }
    }

    protected RegionsPlugin() throws IOException {
    }

    public static final boolean canBuild(final Player p, final Location l) {
        for (final RegionsPlugin<?> pl : plugins) {
            if (!pl.canBuild_i(p, l))
                return false;
        }
        return true;
    }

    public static final Set<? extends Region> getRegionsAt(final Location l) {
        final Set<Region> r = new HashSet<>();
        for (final RegionsPlugin<?> pl : plugins) {
            r.addAll(pl.getRegionsAt_i(l));
        }
        return r;
    }

    @Nullable
    public static final Region getRegion(final World world, final String name) {
        if (!plugins.isEmpty()) {
            return plugins.get(0).getRegion_i(world, name);
        }
        return null;
    }

    public static final boolean hasMultipleOwners() {
        for (final RegionsPlugin<?> pl : plugins) {
            if (pl.hasMultipleOwners_i())
                return true;
        }
        return false;
    }

    @Nullable
    public static final RegionsPlugin<?> getPlugin(final String name) {
        for (final RegionsPlugin<?> pl : plugins) {
            if (pl.getName().equalsIgnoreCase(name))
                return pl;
        }
        return null;
    }

    @Override
    protected boolean init() {
        plugins.add(this);
        return true;
    }

    public abstract boolean canBuild_i(final Player p, final Location l);

    public abstract Collection<? extends Region> getRegionsAt_i(final Location l);

    @Nullable
    public abstract Region getRegion_i(final World world, final String name);

    public abstract boolean hasMultipleOwners_i();

    protected abstract Class<? extends Region> getRegionClass();

}
