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

package ch.njol.skript.hooks;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.ArgsMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Güttinger
 */
public abstract class Hook<P extends Plugin> {

    public static final ArgsMessage m_hooked = new ArgsMessage("hooks.hooked"),
            m_hook_error = new ArgsMessage("hooks.error");
    private static final Map<String, Hook<? extends Plugin>> hooks = new HashMap<>();
    public final P plugin;

    @SuppressWarnings("null")
    public Hook() throws IOException {
        @SuppressWarnings("unchecked") final P p = (P) Bukkit.getPluginManager().getPlugin(getName());
        plugin = p;
        if (p == null)
            return;
        if (!Bukkit.getPluginManager().isPluginEnabled(p) || !init()) {
            Skript.error(m_hook_error.toString(p.getName()));
            return;
        }
        loadClasses();
        Skript.info(m_hooked.toString(p.getName()));

        hooks.remove(p.getName());
        hooks.put(p.getName(), this);
    }

    public static final boolean isHookEnabled(final Plugin p) {
        return isHookEnabled(p.getName());
    }

    public static final boolean isHookEnabled(final String s) {
        return hooks.containsKey(s);
    }

    public static final <T extends Plugin> Hook<T> getHook(final T p) {
        return (Hook<T>) getHook(p.getName());
    }

    /**
     * Preferably use {@link Hook#getHook(Plugin)} instead. It's more type-safe.
     *
     * @param s The name of the plugin.
     * @return The hook instance for the given plugin, null if it does not exist.
     */
    public static final Hook<? extends Plugin> getHook(final String s) {
        return hooks.get(s);
    }

    public final P getPlugin() {
        return plugin;
    }

    @SuppressWarnings("null")
    protected void loadClasses() throws IOException {
        Skript.getAddonInstance().loadClasses(getClass().getPackage().getName());
    }

    /**
     * @return The hooked plugin's exact name
     */
    public abstract String getName();

    /**
     * Called when the plugin has been successfully hooked
     */
    protected boolean init() {
        return true;
    }

}
