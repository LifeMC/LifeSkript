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

import java.io.File;

/**
 * Represents a non-reflective {@link SkriptAddon}.
 * <p>
 * Add-ons implementing this interface has
 * public get file method. So Skript does not
 * use reflection to call the get file method.
 * <p>
 * The result of the get file method is cached anyway,
 * but this can speed up the first call.
 * <p>
 * The binary compatibility of this interface is
 * not guaranteed, and it does not exist in other skript forks.
 * <p>
 * I made it specially for my add-ons and the vanilla Skript,
 * so implementing this in a global add-on is probably a bad idea.
 *
 * @since 2.2.16
 */
@FunctionalInterface
public interface NonReflectiveAddon {
    /**
     * This method is originally protected in Bukkit's {@link org.bukkit.plugin.java.JavaPlugin} class,
     * But plugins implementing this interface has public get file methods.
     *
     * @return The plugin's JAR file if this is a plugin. You should check if it is a plugin before doing so,
     * because everyone can implement this interface.
     * @since 2.2.16
     */
    /*public */File getFile();
}
