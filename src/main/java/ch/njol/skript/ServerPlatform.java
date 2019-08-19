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
 * Represents all server platforms that Skript runs on. Only some
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
     * Glowstone, fully open source Minecraft server, which
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

    public final String platformName;

    public final boolean isWorking;
    public final boolean isSupported;

    /**
     * Represents a server platform.
     *
     * @param platformName Display name for platform.
     * @param isWorking    If the platform usually works.
     * @param isSupported  If the platform is supported.
     */
    ServerPlatform(final String platformName, final boolean isWorking, final boolean isSupported) {
        if (isSupported && !isWorking)
            throw new IllegalArgumentException();

        this.platformName = platformName;
        this.isWorking = isWorking;
        this.isSupported = isSupported;
    }

    /**
     * Checks if this server platform contains Spigot API.
     * <p>
     * This includes server platforms like Glowstone and Paper.
     * They both include Spigot, but difference between them is...
     * <p>
     * Paper is a fork of Spigot (meaning it will also include Spigot internals and such),
     * but Glowstone is a completely new server implementation, but it also includes Spigot API.
     *
     * @return True if this server platform also contains
     * the Spigot API.
     */
    public final boolean isSpigot() {
        return this == BUKKIT_SPIGOT || this == BUKKIT_PAPER || this == LIFE_SPIGOT || this == BUKKIT_TACO || this == GLOWSTONE;
    }

    /**
     * Checks if this server platform contains Paper API.
     * <p>
     * This includes server platforms like Glowstone and Taco.
     * They both include Paper, but difference between them is...
     * <p>
     * Taco is a fork of Paper (meaning it will also include Paper internals and such),
     * but Glowstone is a completely new server implementation, but it also includes Paper API.
     *
     * @return True if this server platform also contains
     * the Paper API.
     */
    public final boolean isPaper() {
        return this == BUKKIT_PAPER || this == LIFE_SPIGOT || this == BUKKIT_TACO || this == GLOWSTONE;
    }

}
