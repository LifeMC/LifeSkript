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

import ch.njol.skript.Skript;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.net.URI;
import java.util.Properties;

public final class PropertiesFile extends File {

    private static final long serialVersionUID = 5226955589070727340L;
    private final Properties properties = new Properties();

    private boolean loaded;
    private boolean saved;

    public PropertiesFile(final String pathname) {
        super(pathname);
    }

    public PropertiesFile(final String parent, final String child) {
        super(parent, child);
    }

    public PropertiesFile(final File parent, final String child) {
        super(parent, child);
    }

    public PropertiesFile(final URI uri) {
        super(uri);
    }

    public final PropertiesFile loadFile() {
        try {
            try (final BufferedInputStream is = new BufferedInputStream(new FileInputStream(this))) {
                properties.load(is);
            }
            loaded = true;
        } catch (final IOException e) {
            throw Skript.sneakyThrow(e);
        }
        return this;
    }

    private final void checkLoad() {
        if (!loaded)
            throw new IllegalStateException("Properties file is not loaded");
    }

    public final PropertiesFile saveFile() {
        try {
            try (final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(this))) {
                properties.store(os, null);
            }
            saved = true;
        } catch (final IOException e) {
            throw Skript.sneakyThrow(e);
        }
        return this;
    }

    @SuppressWarnings("unused")
    private final void checkSave() {
        if (!saved)
            throw new IllegalStateException("Properties file is not saved");
    }

    @Nullable
    public final String get(@Nullable final String key) {
        return get(key, null);
    }

    public final PropertiesFile set(@Nullable final String key,
                                    @Nullable final String value) {
        checkLoad();
        properties.setProperty(key, value);
        return this;
    }

    @Nullable
    public final String get(@Nullable final String key,
                            @Nullable final String defaultValue) {
        checkLoad();
        if (key == null)
            return defaultValue;
        return properties.getProperty(key, defaultValue);
    }

    public final int getInt(@Nullable final String key) {
        return getInt(key, 0);
    }

    public final PropertiesFile setInt(@Nullable final String key,
                                       final int value) {
        set(key, String.valueOf(value));
        return this;
    }

    public final int getInt(@Nullable final String key,
                            final int defaultValue) {
        if (key == null)
            return defaultValue;

        final String value = get(key);
        if (value == null)
            return defaultValue;

        return Integer.parseInt(value);
    }

    public final boolean getBoolean(@Nullable final String key) {
        return getBoolean(key, false);
    }

    public final PropertiesFile setBoolean(@Nullable final String key,
                                           final boolean value) {
        set(key, String.valueOf(value));
        return this;
    }

    public final boolean getBoolean(@Nullable final String key,
                                    final boolean defaultValue) {
        if (key == null)
            return defaultValue;

        final String value = get(key);
        if (value == null)
            return defaultValue;

        return Boolean.parseBoolean(value);
    }

}
