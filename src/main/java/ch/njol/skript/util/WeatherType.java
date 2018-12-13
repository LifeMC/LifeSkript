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

import org.bukkit.World;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherEvent;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public enum WeatherType {
	
	CLEAR, RAIN, THUNDER;
	
	String[] names;
	
	@Nullable
	String adjective;
	
	final static Map<String, WeatherType> byName = new HashMap<String, WeatherType>();
	
	WeatherType(final String... names) {
		this.names = names;
	}
	
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				byName.clear();
				for (final WeatherType t : values()) {
					t.names = Language.getList("weather." + t.name() + ".name");
					t.adjective = Language.get("weather." + t.name() + ".adjective");
					for (final String name : t.names) {
						byName.put(name, t);
					}
				}
			}
		});
	}
	
	@Nullable
	public static WeatherType parse(final String s) {
		return byName.get(s);
	}
	
	public static WeatherType fromWorld(final World world) {
		assert world != null;
		if (world.isThundering())
			return THUNDER;
		if (world.hasStorm())
			return RAIN;
		return CLEAR;
	}
	
	public static WeatherType fromEvent(final WeatherEvent e) {
		if (e instanceof WeatherChangeEvent)
			return fromEvent((WeatherChangeEvent) e);
		if (e instanceof ThunderChangeEvent)
			return fromEvent((ThunderChangeEvent) e);
		assert false;
		return CLEAR;
	}
	
	public static WeatherType fromEvent(final WeatherChangeEvent e) {
		assert e != null;
		if (!e.toWeatherState())
			return CLEAR;
		if (e.getWorld().isThundering())
			return THUNDER;
		return RAIN;
	}
	
	public static WeatherType fromEvent(final ThunderChangeEvent e) {
		assert e != null;
		if (e.toThunderState())
			return THUNDER;
		if (e.getWorld().hasStorm())
			return RAIN;
		return CLEAR;
	}
	
	@SuppressWarnings("null")
	@Override
	public String toString() {
		return names[0];
	}
	
	// REMIND flags?
	@SuppressWarnings("null")
	public String toString(final int flags) {
		return names[0];
	}
	
	@Nullable
	public String adjective() {
		return adjective;
	}
	
	public boolean isWeather(final World w) {
		return isWeather(w.hasStorm(), w.isThundering());
	}
	
	public boolean isWeather(final boolean rain, final boolean thunder) {
		switch (this) {
			case CLEAR:
				return !thunder && !rain;
			case RAIN:
				return !thunder && rain;
			case THUNDER:
				return thunder && rain;
		}
		assert false;
		return false;
	}
	
	public void setWeather(final World w) {
		if (w.isThundering() != (this == THUNDER))
			w.setThundering(this == THUNDER);
		if (w.hasStorm() == (this == CLEAR))
			w.setStorm(this != CLEAR);
	}
	
}
