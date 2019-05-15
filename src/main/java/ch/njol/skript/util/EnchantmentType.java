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
import ch.njol.skript.localization.Language;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.bukkit.enchantments.Enchantment;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public final class EnchantmentType implements YggdrasilSerializable {

    static final Map<Enchantment, String> enchantmentNames = new HashMap<>();
    static final Map<String, Enchantment> enchantmentPatterns = new HashMap<>();
    private static final String LANGUAGE_NODE = "enchantments";
    @SuppressWarnings("null")
    private static final Pattern pattern = Pattern.compile(".+ \\d+");

    static {
        Language.addListener(() -> {
            enchantmentNames.clear();
            for (final Enchantment e : Enchantment.values()) {
                final String[] names = Language.getList(LANGUAGE_NODE + ".names." + e.getName());
                enchantmentNames.put(e, names[0]);
                for (final String n : names)
                    enchantmentPatterns.put(n.toLowerCase(Locale.ENGLISH), e);
            }
        });
    }

    private final Enchantment type;
    private final int level;

    /**
     * Used for deserialisation only
     */
    @SuppressWarnings({"unused", "null"})
    private EnchantmentType() {
        type = null;
        level = -1;
    }

    public EnchantmentType(final Enchantment type, final int level) {
        assert type != null;
        this.type = type;
        this.level = level;
    }

    @SuppressWarnings("null")
    public static final String toString(final Enchantment e) {
        return enchantmentNames.get(e);
    }

    // REMIND flags?
    @SuppressWarnings("null")
    public static final String toString(final Enchantment e, final int flags) {
        return enchantmentNames.get(e);
    }

    @SuppressWarnings("null")
    @Nullable
    public static final EnchantmentType parse(final String s) {
        if (pattern.matcher(s).matches()) {
            final Enchantment ench = parseEnchantment(s.substring(0, s.lastIndexOf(' ')));
            if (ench == null)
                return null;
            return new EnchantmentType(ench, Utils.parseInt(s.substring(s.lastIndexOf(' ') + 1)));
        }
        final Enchantment ench = parseEnchantment(s);
        if (ench == null)
            return null;
        return new EnchantmentType(ench, -1);
    }

    @Nullable
    public static final Enchantment parseEnchantment(final String s) {
        return enchantmentPatterns.get(s.toLowerCase(Locale.ENGLISH));
    }

    @SuppressWarnings("null")
    public static final Collection<String> getNames() {
        return enchantmentNames.values();
    }

    /**
     * @return level or 1 if level == -1
     */
    public int getLevel() {
        return level == -1 ? 1 : level;
    }

    /**
     * @return the internal level, can be -1
     */
    public int getInternalLevel() {
        return level;
    }

    @Nullable
    public Enchantment getType() {
        return type;
    }

    public boolean has(final ItemType item) {
        final Map<Enchantment, Integer> enchs = item.getEnchantments();
        if (enchs == null)
            return false;
        final Integer l = enchs.get(type);
        if (l == null)
            return false;
        if (level == -1)
            return true;
        return l == level;
    }

    @Override
    public String toString() {
        return toString(type) + (level == -1 ? "" : " " + level);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + level;
        result = prime * result + type.hashCode();
        return result;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof EnchantmentType))
            return false;
        final EnchantmentType other = (EnchantmentType) obj;
        if (level != other.level)
            return false;
        return type.equals(other.type);
    }

}
