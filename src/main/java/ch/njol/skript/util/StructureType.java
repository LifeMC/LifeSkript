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

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public enum StructureType {
    TREE(TreeType.TREE, TreeType.BIG_TREE, TreeType.REDWOOD, TreeType.TALL_REDWOOD, TreeType.SMALL_JUNGLE, TreeType.JUNGLE, TreeType.SWAMP),

    REGULAR(TreeType.TREE, TreeType.BIG_TREE), SMALL_REGULAR(TreeType.TREE), BIG_REGULAR(TreeType.BIG_TREE), REDWOOD(TreeType.REDWOOD, TreeType.TALL_REDWOOD), SMALL_REDWOOD(TreeType.REDWOOD), BIG_REDWOOD(TreeType.TALL_REDWOOD), JUNGLE(TreeType.SMALL_JUNGLE, TreeType.JUNGLE), SMALL_JUNGLE(TreeType.SMALL_JUNGLE), BIG_JUNGLE(TreeType.JUNGLE), JUNGLE_BUSH(TreeType.JUNGLE_BUSH), SWAMP(TreeType.SWAMP),

    MUSHROOM(TreeType.RED_MUSHROOM, TreeType.BROWN_MUSHROOM), RED_MUSHROOM(TreeType.RED_MUSHROOM), BROWN_MUSHROOM(TreeType.BROWN_MUSHROOM),

    ;

    /**
     * lazy
     */
    static final Map<Pattern, StructureType> parseMap = new HashMap<>();

    static {
        Language.addListener(parseMap::clear);
    }

    private final TreeType[] types;
    private final Noun name;

    StructureType(final TreeType... types) {
        this.types = types;
        name = new Noun("tree types." + name() + ".name");
    }

    @Nullable
    public static final StructureType fromName(String s) {
        if (s.isEmpty())
            return null;
        if (parseMap.isEmpty()) {
            for (final StructureType t : values()) {
                final String pattern = Language.get("tree types." + t.name() + ".pattern");
                parseMap.put(PatternCache.get(pattern, Pattern.CASE_INSENSITIVE), t);
            }
        }
        s = s.toLowerCase(Locale.ENGLISH);
        for (final Entry<Pattern, StructureType> e : parseMap.entrySet()) {
            if (e.getKey().matcher(s).matches())
                return e.getValue();
        }
        return null;
    }

    public void grow(final Location loc) {
        loc.getWorld().generateTree(loc, CollectionUtils.getRandom(types));
    }

    public void grow(final Block b) {
        b.getWorld().generateTree(b.getLocation(), CollectionUtils.getRandom(types));
    }

    public TreeType[] getTypes() {
        return types;
    }

    @Override
    public String toString() {
        return name.toString();
    }

    public String toString(final int flags) {
        return name.toString(flags);
    }

    public Noun getName() {
        return name;
    }

    public boolean is(final TreeType type) {
        return CollectionUtils.contains(types, type);
    }

}
