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

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.function.Parameter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;

/**
 * Provides constant empty arrays to use with
 * {@link java.util.Collection#toArray(Object[])}.
 *
 * @since 2.2.15
 */
public final class EmptyArrays {

    // Primitives

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];

    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];

    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

    // Others

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Integer[] EMPTY_INTEGER_ARRAY = new Integer[0];

    public static final Boolean[] EMPTY_WRAPPER_BOOLEAN_ARRAY = new Boolean[0];
    public static final Double[] EMPTY_WRAPPER_DOUBLE_ARRAY = new Double[0];

    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    public static final ItemType[] EMPTY_ITEMTYPE_ARRAY = new ItemType[0];

    public static final Direction[] EMPTY_DIRECTION_ARRAY = new Direction[0];
    public static final Location[] EMPTY_LOCATION_ARRAY = new Location[0];

    public static final World[] EMPTY_WORLD_ARRAY = new World[0];
    public static final File[] EMPTY_FILE_ARRAY = new File[0];

    public static final CommandSender[] EMPTY_COMMANDSENDER_ARRAY = new CommandSender[0];

    public static final Player[] EMPTY_PLAYER_ARRAY = new Player[0];
    public static final Entity[] EMPTY_ENTITY_ARRAY = new Entity[0];

    public static final Block[] EMPTY_BLOCK_ARRAY = new Block[0];
    public static final ItemStack[] EMPTY_ITEMSTACK_ARRAY = new ItemStack[0];

    public static final Experience[] EMPTY_EXPERIENCE_ARRAY = new Experience[0];
    public static final Region[] EMPTY_REGION_ARRAY = new Region[0];

    public static final Expression<?>[] EMPTY_EXPRESSION_ARRAY = new Expression<?>[0];
    public static final Literal<?>[] EMPTY_LITERAL_ARRAY = new Literal<?>[0];

    public static final OfflinePlayer[] EMPTY_OFFLINEPLAYER_ARRAY = new OfflinePlayer[0];
    public static final Parameter<?>[] EMPTY_PARAMETER_ARRAY = new Parameter<?>[0];

    private EmptyArrays() {
        throw new UnsupportedOperationException("Static class");
    }

}
