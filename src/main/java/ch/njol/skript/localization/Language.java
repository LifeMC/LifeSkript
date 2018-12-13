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

package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.Config;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.Version;
import ch.njol.util.StringUtils;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public class Language {
	
	/**
	 * Some flags
	 */
	public final static int F_PLURAL = 1, F_DEFINITE_ARTICLE = 2, F_INDEFINITE_ARTICLE = 4;
	
	/**
	 * masks out article flags - useful if the article has been added already (e.g. by an adjective)
	 */
	public final static int NO_ARTICLE_MASK = ~(F_DEFINITE_ARTICLE | F_INDEFINITE_ARTICLE);
	
	/**
	 * Name of the localised language
	 */
	private static String name = "english";
	
	final static HashMap<String, String> english = new HashMap<String, String>();
	
	/**
	 * May be null.
	 */
	@Nullable
	static HashMap<String, String> localized;
	
	static boolean useLocal;
	
	private static HashMap<Plugin, Version> langVersion = new HashMap<Plugin, Version>();
	
	public static String getName() {
		return useLocal ? name : "english";
	}
	
	@Nullable
	private static String get_i(final String key) {
		if (useLocal && localized != null) {
			final String s = localized.get(key);
			if (s != null)
				return s;
		}
		final String s = english.get(key);
		if (s == null && Skript.testing())
			missingEntryError(key);
		return s;
	}
	
	/**
	 * Gets a string from the language file with the given key, or the english variant if the string is missing from the chosen language's file, or the key itself if the key does
	 * not exist.
	 * 
	 * @param key The message's key (case-insensitive)
	 * @return The requested message if it exists or the key otherwise
	 */
	public static String get(final String key) {
		final String s = get_i("" + key.toLowerCase(Locale.ENGLISH));
		return s == null ? "" + key.toLowerCase(Locale.ENGLISH) : s;
	}
	
	/**
	 * Equal to {@link #get(String)}, but returns null instead of the key if the key cannot be found.
	 * 
	 * @param key The message's key (case-insensitive)
	 * @return The requested message or null if it doesn't exist
	 */
	@Nullable
	public static String get_(final String key) {
		return get_i("" + key.toLowerCase(Locale.ENGLISH));
	}
	
	public static void missingEntryError(final String key) {
		if (!Skript.debug() || !Skript.testing())
			return;
		Skript.warning("Missing entry '" + key.toLowerCase(Locale.ENGLISH) + "' in the default english language file");
	}
	
	/**
	 * Gets a string and uses it as format in {@link String#format(String, Object...)}.
	 * 
	 * @param key
	 * @param args The arguments to pass to {@link String#format(String, Object...)}
	 * @return The formatted string
	 */
	public static String format(String key, final Object... args) {
		key = "" + key.toLowerCase(Locale.ENGLISH);
		final String value = get_i(key);
		if (value == null)
			return key;
		try {
			return "" + String.format(value, args);
		} catch (final Exception e) {
			Skript.error("Invalid format string at '" + key + "' in the " + getName() + " language file: " + value);
			return key;
		}
	}
	
	/**
	 * Gets a localized string surrounded by spaces, or a space if the string is empty
	 * 
	 * @param key
	 * @return The message surrounded by spaces, a space if the entry is empty, or " "+key+" " if the entry is missing.
	 */
	public static String getSpaced(final String key) {
		final String s = get(key);
		if (s.isEmpty())
			return " ";
		return " " + s + " ";
	}
	
	@SuppressWarnings("null")
	private final static Pattern listSplitPattern = Pattern.compile("\\s*,\\s*");
	
	/**
	 * Gets a list of strings.
	 * 
	 * @param key
	 * @return a non-null String array with at least one element
	 */
	public static String[] getList(final String key) {
		final String s = get_i("" + key.toLowerCase(Locale.ENGLISH));
		if (s == null)
			return new String[] {key.toLowerCase(Locale.ENGLISH)};
		final String[] r = listSplitPattern.split(s);
		assert r != null;
		return r;
	}
	
	/**
	 * @param key
	 * @return Whether the given key exists in the <b>english</b> language file.
	 */
	public static boolean keyExists(final String key) {
		return english.containsKey(key.toLowerCase(Locale.ENGLISH));
	}
	
	public static void loadDefault(final SkriptAddon addon) {
		if (addon.getLanguageFileDirectory() == null)
			return;
		final InputStream din = addon.plugin.getResource(addon.getLanguageFileDirectory() + "/english.lang");
		if (din == null)
			throw new IllegalStateException(addon + " is missing the required english.lang file!");
		Map<String, String> en;
		try {
			en = new Config(din, "english.lang", false, false, ":").toMap(".");
		} catch (final Exception e) {
			throw Skript.exception(e, "Could not load " + addon + "'s default language file!");
		} finally {
			try {
				din.close();
			} catch (final IOException ignored) {}
		}
		final String v = en.get("version");
		if (v == null)
			Skript.warning("Missing version in english.lang");
		langVersion.put(addon.plugin, v == null ? Skript.getVersion() : new Version(v));
		en.remove("version");
		english.putAll(en);
		for (final LanguageChangeListener l : listeners)
			l.onLanguageChange();
	}
	
	public static boolean load(String name) {
		name = "" + name.toLowerCase();
		if ("english".equals(name))
			return true;
		localized = new HashMap<String, String>();
		boolean exists = load(Skript.getAddonInstance(), name);
		for (final SkriptAddon addon : Skript.getAddons()) {
			assert addon != null;
			exists |= load(addon, name);
		}
		if (!exists) {
			localized = null;
			Language.name = "english";
			return false;
		}
		Language.name = name;
		validateLocalized();
		if (useLocal) {
			for (final LanguageChangeListener l : listeners)
				l.onLanguageChange();
		}
		return true;
	}
	
	@SuppressWarnings("resource") // closed in line 277.
	private static boolean load(final SkriptAddon addon, final String name) {
		if (addon.getLanguageFileDirectory() == null)
			return false;
		final Map<String, String> l = load(addon.plugin.getResource(addon.getLanguageFileDirectory() + "/" + name + ".lang"), name);
		final File f = new File(addon.plugin.getDataFolder(), addon.getLanguageFileDirectory() + File.separator + name + ".lang");
		try {
			if (f.exists())
				l.putAll(load(new FileInputStream(f), name));
		} catch (final FileNotFoundException e) {
			assert false;
		}
		if (l.isEmpty())
			return false;
		if (!l.containsKey("version")) {
			Skript.error(addon + "'s language file " + name + ".lang does not provide a version number!");
		} else {
			try {
				final Version v = new Version("" + l.get("version"));
				final Version lv = langVersion.get(addon.plugin);
				assert lv != null; // set in loadDefault()
				if (v.isSmallerThan(lv))
					Skript.warning(addon + "'s language file " + name + ".lang is outdated, some messages will be english.");
			} catch (final IllegalArgumentException e) {
				Skript.error("Illegal version syntax in " + addon + "'s language file " + name + ".lang: " + e.getLocalizedMessage());
			}
		}
		l.remove("version");
		final HashMap<String, String> loc = localized;
		if (loc != null)
			loc.putAll(l);
		else
			assert false : addon + "; " + name;
		return true;
	}
	
	private static Map<String, String> load(final @Nullable InputStream in, final String name) {
		if (in == null)
			return new HashMap<String, String>();
		try {
			return new Config(in, name + ".lang", false, false, ":").toMap(".");
		} catch (final IOException e) {
			Skript.exception(e, "Could not load the language file '" + name + ".lang': " + ExceptionUtils.toString(e));
			return new HashMap<String, String>();
		} finally {
			try {
				in.close();
			} catch (final IOException ignored) {}
		}
	}
	
	private static void validateLocalized() {
		final HashMap<String, String> loc = localized;
		if (loc == null) {
			assert false;
			return;
		}
		HashSet<String> s = new HashSet<String>(english.keySet());
		s.removeAll(loc.keySet());
		removeIgnored(s);
		if (!s.isEmpty() && Skript.logNormal())
			Skript.warning("The following messages have not been translated to " + name + ": " + StringUtils.join(s, ", "));
		s = new HashSet<String>(loc.keySet());
		s.removeAll(english.keySet());
		removeIgnored(s);
		if (!s.isEmpty() && Skript.logHigh())
			Skript.warning("The localized language file(s) has/ve superfluous entries: " + StringUtils.join(s, ", "));
	}
	
	private static void removeIgnored(final Set<String> keys) {
		final Iterator<String> i = keys.iterator();
		while (i.hasNext()) {
			if (i.next().startsWith(Noun.GENDERS_SECTION))
				i.remove();
		}
	}
	
	private final static List<LanguageChangeListener> listeners = new ArrayList<LanguageChangeListener>();
	
	public enum LanguageListenerPriority {
		EARLIEST, NORMAL, LATEST
	}
	
	private final static int[] priorityStartIndices = new int[LanguageListenerPriority.values().length];
	
	/**
	 * Registers a listener. The listener will immediately be called if a language has already been loaded.
	 * <p>
	 * The first call to a listener is guaranteed to be (pseudo-*)English even if another language is active, in which case the listener is called twice when registered.
	 * <p>
	 * * Only this class will be English (i.e. no language listeners are notified) if the current language is not English.
	 * 
	 * @param l
	 */
	public static void addListener(final LanguageChangeListener l) {
		addListener(l, LanguageListenerPriority.NORMAL);
	}
	
	public static void addListener(final LanguageChangeListener l, final LanguageListenerPriority priority) {
		assert priority != null;
		listeners.add(priorityStartIndices[priority.ordinal()], l);
		for (int i = priority.ordinal() + 1; i < LanguageListenerPriority.values().length; i++)
			priorityStartIndices[i]++;
		if (!english.isEmpty()) {
			if (localized != null && useLocal) {
				useLocal = false;
				l.onLanguageChange();
				useLocal = true;
			}
			l.onLanguageChange();
		}
	}
	
	/**
	 * Use this preferably like this:
	 * 
	 * <pre>
	 * final boolean wasLocal = Language.setUseLocal(true / false);
	 * try {
	 * 	// whatever
	 * } finally {
	 * 	Language.setUseLocal(wasLocal);
	 * }
	 * </pre>
	 * 
	 * @param b Whether to enable localisation or not
	 * @return Previous state
	 */
	public static boolean setUseLocal(final boolean b) {
		if (useLocal == b)
			return b;
		if (localized == null)
			return false;
		useLocal = b;
		for (final LanguageChangeListener l : listeners) {
			try {
				l.onLanguageChange();
			} catch (final Exception e) {
				Skript.exception(e, "Error while changing the language " + (b ? "from english to" : "to english from") + " " + name, "Listener: " + l);
			}
		}
		return !b;
	}
	
	public static boolean isUsingLocal() {
		return useLocal;
	}
	
}
