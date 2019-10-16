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

package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.config.Config;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.Version;
import ch.njol.util.StringUtils;
import org.bukkit.plugin.Plugin;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

//import ch.njol.skript.util.Date;

/**
 * @author Peter Güttinger
 */
public final class Language {

    /**
     * Some flags
     */
    public static final int F_PLURAL = 1, F_DEFINITE_ARTICLE = 2, F_INDEFINITE_ARTICLE = 4;
    /**
     * masks out article flags - useful if the article has been added already (e.g. by an adjective)
     */
    public static final int NO_ARTICLE_MASK = ~(F_DEFINITE_ARTICLE | F_INDEFINITE_ARTICLE);
    static final HashMap<String, String> english = new HashMap<>();
    private static final HashMap<Plugin, Version> langVersion = new HashMap<>();
    @SuppressWarnings("null")
    private static final Pattern listSplitPattern = Pattern.compile("\\s*,\\s*");
    private static final List<LanguageChangeListener> listeners = new ArrayList<>();
    private static final int[] priorityStartIndices = new int[LanguageListenerPriority.values().length];
    /**
     * May be null.
     */
    @Nullable
    static HashMap<String, String> localized;
    static boolean useLocal;
    /**
     * Name of the localised language
     */
    private static String name = "english";

    private Language() {
        throw new UnsupportedOperationException("Static class");
    }

    public static final String getName() {
        return useLocal ? name : "english";
    }

    @Nullable
    private static final String get_i(final String key) {
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
    public static final String get(final String key) {
        final String s = get_i(key.toLowerCase(Locale.ENGLISH));
        return s == null ? key.toLowerCase(Locale.ENGLISH) : s;
    }

    /**
     * Equal to {@link #get(String)}, but returns null instead of the key if the key cannot be found.
     *
     * @param key The message's key (case-insensitive)
     * @return The requested message or null if it doesn't exist
     */
    @Nullable
    public static final String get_(final String key) {
        return get_i(key.toLowerCase(Locale.ENGLISH));
    }

    public static final void missingEntryError(final String key) {
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
    public static final String format(String key, final Object... args) {
        key = key.toLowerCase(Locale.ENGLISH);
        final String value = get_i(key);
        if (value == null)
            return key;
        try {
            return String.format(value, args);
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
    public static final String getSpaced(final String key) {
        final String s = get(key);
        if (s.isEmpty())
            return " ";
        return ' ' + s + ' ';
    }

    /**
     * Gets a list of strings.
     *
     * @param key
     * @return a non-null String array with at least one element
     */
    public static final String[] getList(final String key) {
        final String s = get_i(key.toLowerCase(Locale.ENGLISH));
        if (s == null)
            return new String[]{key.toLowerCase(Locale.ENGLISH)};
        return listSplitPattern.split(s, 0);
    }

    /**
     * @param key
     * @return Whatever the given key exists in the <b>english</b> language file.
     */
    public static final boolean keyExists(final String key) {
        return english.containsKey(key.toLowerCase(Locale.ENGLISH));
    }

    public static final void loadDefault(final SkriptAddon addon) {
//        final boolean flag = addon.plugin instanceof Skript && Skript.logHigh();
//        Date start = null;

//        if (flag)
//            start = new Date();

        if (addon.getLanguageFileDirectory() == null)
            return;
        final Map<String, String> en;
        try (final InputStream din = addon.plugin.getResource(addon.getLanguageFileDirectory() + "/english.lang")) {
            if (din == null)
                throw new IllegalStateException(addon + " is missing the required english.lang file!");
            try (final BufferedInputStream buf = new BufferedInputStream(din)) {
                en = new Config(buf, new File(addon.getLanguageFileDirectory(), "english.lang").exists() ? "english.lang" : Objects.requireNonNull(addon.getFile()).getName() + '/' + addon.getLanguageFileDirectory() + "/english.lang", false, false, ":").toMap(".");
            }
        } catch (final Throwable tw) {
            throw Skript.exception(tw, "Could not load " + addon + "'s default language file!");
        }
        final String v = en.get("version");
        if (v == null && Skript.testing() && Skript.debug())
            Skript.warning("Missing version in english.lang");
        langVersion.put(addon.plugin, v == null ? Skript.getVersion() : new Version(v));
        en.remove("version");
        english.putAll(en);
        for (final LanguageChangeListener l : listeners)
            l.onLanguageChange();

//        if (flag) {
//            assert start != null : flag;
//
//            Skript.info("Loaded language files in " + start.difference(new Date()));
//        }
    }

    public static final boolean load(String name) {
        name = name.toLowerCase(Locale.ENGLISH);
        if ("english".equals(name))
            return true;
        localized = new HashMap<>();
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

    @SuppressWarnings("resource")
    private static final boolean load(final SkriptAddon addon, final String name) {
        if (addon.getLanguageFileDirectory() == null)
            return false;
        final Map<String, String> l = load(addon.plugin.getResource(addon.getLanguageFileDirectory() + '/' + name + ".lang"), name);
        final File f = new File(addon.plugin.getDataFolder(), addon.getLanguageFileDirectory() + File.separator + name + ".lang");
        try (final InputStream is = new BufferedInputStream(new FileInputStream(f))) {
            if (f.exists())
                l.putAll(load(is, name));
        } catch (final IOException e) {
            Skript.exception(e);
        }
        if (l.isEmpty())
            return false;
        if (!l.containsKey("version")) {
            Skript.error(addon + "'s language file " + name + ".lang does not provide a version number!");
        } else {
            try {
                final Version v = new Version(l.get("version"));
                final Version lv = langVersion.get(addon.plugin);
                assert lv != null; // set in loadDefault()
                if (v.isSmallerThan(lv))
                    Skript.warning(addon + "'s language file " + name + ".lang is outdated, some messages will be english.");
            } catch (final IllegalArgumentException e) {
                Skript.error("Illegal version syntax in " + addon + "'s language file " + name + ".lang: " + e.getLocalizedMessage());
            }
        }
        l.remove("version");
        final Map<String, String> loc = localized;
        if (loc != null)
            loc.putAll(l);
        else
            assert false : addon + "; " + name;
        return true;
    }

    private static final Map<String, String> load(@Nullable final InputStream in, final String name) {
        if (in == null)
            return new HashMap<>();
        BufferedInputStream buf = null;
        try {
            return new Config(in instanceof BufferedInputStream ? in : (buf = new BufferedInputStream(in)), name + ".lang", false, false, ":").toMap(".");
        } catch (final IOException e) {
            Skript.exception(e, "Could not load the language file '" + name + ".lang': " + ExceptionUtils.toString(e));
            return new HashMap<>();
        } finally {
            try {
                in.close();

                if (buf != null)
                    buf.close();
            } catch (final IOException ignored) {
                /* ignored */
            }
        }
    }

    private static final void validateLocalized() {
        final Map<String, String> loc = localized;
        if (loc == null) {
            assert false;
            return;
        }
        Set<String> s = new HashSet<>(english.keySet());
        s.removeAll(loc.keySet());
        removeIgnored(s);
        if (!s.isEmpty() && Skript.logNormal())
            Skript.warning("The following messages have not been translated to " + name + ": " + StringUtils.join(s, ", "));
        s = new HashSet<>(loc.keySet());
        s.removeAll(english.keySet());
        removeIgnored(s);
        if (!s.isEmpty() && Skript.logHigh())
            Skript.warning("The localized language file(s) has/ve superfluous entries: " + StringUtils.join(s, ", "));
    }

    private static final void removeIgnored(final Collection<String> keys) {
        keys.removeIf(s -> s.startsWith(Noun.GENDERS_SECTION));
    }

    /**
     * Registers a listener. The listener will immediately be called if a language has already been loaded.
     * <p>
     * The first call to a listener is guaranteed to be (pseudo-*)English even if another language is active, in which case the listener is called twice when registered.
     * <p>
     * * Only this class will be English (i.e. no language listeners are notified) if the current language is not English.
     *
     * @param l
     */
    public static final void addListener(final LanguageChangeListener l) {
        addListener(l, LanguageListenerPriority.NORMAL);
    }

    public static final void addListener(final LanguageChangeListener l, final LanguageListenerPriority priority) {
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
     * @param b Whatever to enable localisation or not
     * @return Previous state
     */
    public static final boolean setUseLocal(final boolean b) {
        if (useLocal == b)
            return b;
        if (localized == null)
            return false;
        useLocal = b;
        for (final LanguageChangeListener l : listeners) {
            try {
                l.onLanguageChange();
            } catch (final Exception e) {
                Skript.exception(e, "Error while changing the language " + (b ? "from english to" : "to english from") + ' ' + name, "Listener: " + l);
            }
        }
        return !b;
    }

    public static final boolean isUsingLocal() {
        return useLocal;
    }

    public enum LanguageListenerPriority {
        EARLIEST, NORMAL, LATEST
    }

}
