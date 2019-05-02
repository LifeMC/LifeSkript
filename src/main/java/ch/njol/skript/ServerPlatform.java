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

/**
 * Represents all server platforms that Skript runs on. Only some of the
 * platforms are "officially" supported, though.
 */
public enum ServerPlatform {

    /**
     * Unknown Bukkit revision. This is probably a bad thing...
     */
    BUKKIT_UNKNOWN("Bukkit", true, false),

    /**
     * CraftBukkit, but not Spigot or Paper.
     */
    BUKKIT_CRAFTBUKKIT("CraftBukkit", true, true),

    /**
     * Spigot, with its Bukkit API extensions. Officially supported.
     */
    BUKKIT_SPIGOT("Spigot", true, true),

    /**
     * Paper Minecraft server, which is a Spigot fork with additional features.
     * Officially supported.
     */
    BUKKIT_PAPER("Paper", true, true),

    /**
     * Taco Minecraft server, which is a Paper fork with additional performance
     * and configurations. Officially supported.
     */
    BUKKIT_TACO("Taco", true, true),

    /**
     * Glowstone (or similar) fully open source Minecraft server, which
     * supports Spigot API.
     */
    GLOWSTONE("Glowstone", true, true),

    /**
     * Doesn't work at all currently.
     */
    SPONGE("Sponge", false, false),

    /**
     * The LifeSpigot, maintained by LifeMC. It's a fork of Paper.
     */
    LIFE_SPIGOT("LifeSpigot", true, true);

    public String name;
    public boolean works;
    public boolean supported;

    /**
     * Represents a server platform.
     *
     * @param name Display name for platform.
     * @param works If the platform usually works.
     * @param supported If the platform is supported.
     */
    ServerPlatform(final String name, final boolean works, final boolean supported) {
        if (supported && !works)
            throw new IllegalArgumentException();
        this.name = name;
        this.works = works;
        this.supported = supported;
    }

}
