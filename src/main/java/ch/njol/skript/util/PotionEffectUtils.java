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

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.LanguageChangeListener;

import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public final class PotionEffectUtils {
	
	private PotionEffectUtils() {
		throw new UnsupportedOperationException();
	}
	
	final static Map<String, PotionEffectType> types = new HashMap<String, PotionEffectType>();
	
	final static String[] names = new String[getMaxPotionId() + 1];
	
	// MCPC+ workaround
	private static int getMaxPotionId() {
		int i = 0;
		for (final PotionEffectType t : PotionEffectType.values()) {
			if (t != null && t.getId() > i)
				i = t.getId();
		}
		return i;
	}
	
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				types.clear();
				for (final PotionEffectType t : PotionEffectType.values()) {
					if (t == null)
						continue;
					final String[] ls = Language.getList("potions." + t.getName());
					names[t.getId()] = ls[0];
					for (final String l : ls) {
						types.put(l.toLowerCase(), t);
					}
				}
			}
		});
	}
	
	@Nullable
	public static PotionEffectType parseType(final String s) {
		return types.get(s.toLowerCase());
	}
	
	@SuppressWarnings("null")
	public static String toString(final PotionEffectType t) {
		return names[t.getId()];
	}
	
	// REMIND flags?
	@SuppressWarnings("null")
	public static String toString(final PotionEffectType t, final int flags) {
		return names[t.getId()];
	}
	
	public static String[] getNames() {
		return names;
	}
	
	public static short guessData(final ThrownPotion p) {
		if (p.getEffects().size() == 1) {
			final PotionEffect e = p.getEffects().iterator().next();
			final Potion d = new Potion(PotionType.getByEffect(e.getType())).splash();
			return d.toDamageValue();
		}
		return 0;
	}
}
