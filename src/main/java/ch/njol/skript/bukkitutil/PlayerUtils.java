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

package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.util.EmptyArrays;
import ch.njol.skript.util.Task;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.HashSet;

/**
 * TODO check all updates and find out which ones are not required
 *
 * @author Peter Güttinger
 */
public final class PlayerUtils {

    public static final boolean hasCollecionGetOnlinePlayers = Skript.methodExists(Bukkit.class, "getOnlinePlayers", EmptyArrays.EMPTY_CLASS_ARRAY, Collection.class);
    static final Collection<Player> inviUpdate = new HashSet<>();
    // created when first used
    public static final Task task = new Task(Skript.getInstance(), 1L, 1L) {
        @Override
        public final void run() {
            assert Bukkit.isPrimaryThread() : Thread.currentThread();
            TickUtils.tickAll();
            try {
                for (final Player p : inviUpdate)
                    p.updateInventory();
            } catch (final NullPointerException e) { // can happen on older CraftBukkit (Tekkit) builds
                if (Skript.testing() || Skript.debug())
                    e.printStackTrace();
            }
            inviUpdate.clear();
        }
    };
    @Nullable
    private static Method getOnlinePlayers;
    private static boolean cached;

    private PlayerUtils() {
        throw new UnsupportedOperationException();
    }

    public static final void updateInventory(@Nullable final Player p) {
        if (p != null)
            inviUpdate.add(p);
    }

    @SuppressWarnings({"null", "unchecked"})
    public static final Collection<? extends Player> getOnlinePlayers() {
        if (hasCollecionGetOnlinePlayers) {
            return Bukkit.getOnlinePlayers();
        }
        // Return directly to improve performance and fix some bugs
        // Hope everything goes well, and it works. C:
        if (cached) {
            return Bukkit.getOnlinePlayers();
        }
        // Is running minecraft method checks if version is - at least - >= given version.
        cached = Skript.isRunningMinecraft(1, 7, 10);
        if (cached)
            return Bukkit.getOnlinePlayers();
        // Handle other versions here
        if (getOnlinePlayers == null) {
            try {
                getOnlinePlayers = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
            } catch (final NoSuchMethodException e) {
                Skript.outdatedError(e);
            } catch (final SecurityException e) {
                Skript.exception(e);
            }
        }
        assert getOnlinePlayers != null : Skript.getMinecraftVersion();
        try {
            final Object o = getOnlinePlayers.invoke(null);
            if (o instanceof Collection<?>)
                throw new IllegalStateException("Skript unable to detect collection return type");
            return Arrays.asList((Player[]) o);
        } catch (final IllegalAccessException | IllegalArgumentException e) {
            Skript.outdatedError(e);
        } catch (final InvocationTargetException e) {
            throw Skript.exception(e);
        }
        Skript.warning("Can't get online players");
        throw new EmptyStackException();
    }

}
