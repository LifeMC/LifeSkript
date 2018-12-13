/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.util;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;
import ch.njol.yggdrasil.YggdrasilSerializable;

import org.bukkit.enchantments.Enchantment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public class EnchantmentType implements YggdrasilSerializable {
	
	private final static String LANGUAGE_NODE = "enchantments";
	
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
	
	@SuppressWarnings("null")
	public static String toString(final Enchantment e) {
		return enchantmentNames.get(e);
	}
	
	// REMIND flags?
	@SuppressWarnings("null")
	public static String toString(final Enchantment e, final int flags) {
		return enchantmentNames.get(e);
	}
	
	final static Map<Enchantment, String> enchantmentNames = new HashMap<Enchantment, String>();
	final static Map<String, Enchantment> enchantmentPatterns = new HashMap<String, Enchantment>();
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				enchantmentNames.clear();
				for (final Enchantment e : Enchantment.values()) {
					final String[] names = Language.getList(LANGUAGE_NODE + ".names." + e.getName());
					enchantmentNames.put(e, names[0]);
					for (final String n : names)
						enchantmentPatterns.put(n.toLowerCase(), e);
				}
			}
		});
	}
	
	@SuppressWarnings("null")
	private final static Pattern pattern = Pattern.compile(".+ \\d+");
	
	@SuppressWarnings("null")
	@Nullable
	public static EnchantmentType parse(final String s) {
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
	public static Enchantment parseEnchantment(final String s) {
		return enchantmentPatterns.get(s.toLowerCase());
	}
	
	@SuppressWarnings("null")
	public static Collection<String> getNames() {
		return enchantmentNames.values();
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
		if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
