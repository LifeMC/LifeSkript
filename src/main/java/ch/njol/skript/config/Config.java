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

package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.validate.NodeValidator;
import ch.njol.skript.config.validate.SectionValidator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Represents a config file.
 *
 * @author Peter Güttinger
 */
public final class Config {

    public static final int GLOBAL_BUFFER_LENGTH = System.getProperty("skript.bufferLength") != null
            ? Integer.parseInt(System.getProperty("skript.bufferLength")) : -1;
    final boolean allowEmptySections;
    private final String defaultSeparator;
    private final SectionNode main;
    private final String fileName;
    boolean simple;
    String separator;
    int level;
    int errors;
    @Nullable
    private
    File file;
    /**
     * One level of the indentation, e.g. a tab or 4 spaces.
     */
    private String indentation = "\t";
    /**
     * The indentation's name, i.e. 'tab' or 'space'.
     */
    private String indentationName = "tab";

    public Config(final BufferedInputStream source, final String fileName, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
        try {
            this.fileName = fileName;
            this.simple = simple;
            this.allowEmptySections = allowEmptySections;
            this.defaultSeparator = defaultSeparator;
            separator = defaultSeparator;

            if (source.available() == 0 && !SkriptConfig.disableEmptyScriptWarnings.value()) {
                main = new SectionNode(this);
                Skript.warning('\'' + this.fileName + "' is empty");
                return;
            }

            if (Skript.logVeryHigh())
                Skript.info("Loading '" + fileName + '\'');

            if (GLOBAL_BUFFER_LENGTH != -1) {
                if (GLOBAL_BUFFER_LENGTH < 8192) // The default is 8192, below will cause performance drop
                    throw new IllegalArgumentException("Please enter a valid buffer length that is higher than 8192, in bytes. (given " + GLOBAL_BUFFER_LENGTH + " bytes)");
                try (final ConfigReader r = new ConfigReader(source, GLOBAL_BUFFER_LENGTH)) {
                    main = SectionNode.load(this, r);
                }
            } else {
                try (final ConfigReader r = new ConfigReader(source)) {
                    main = SectionNode.load(this, r);
                }
            }
        } finally {
            source.close();
        }
    }

    public Config(final InputStream source, final String fileName, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
        this(source instanceof BufferedInputStream ? (BufferedInputStream) source : new BufferedInputStream(source), fileName, simple, allowEmptySections, defaultSeparator);
    }

    public Config(final FileInputStream source, final File file, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
        this(new BufferedInputStream(source), file.getName(), simple, allowEmptySections, defaultSeparator);
        this.file = file;
    }

    /**
     * @deprecated use {@link Config#Config(FileInputStream, File, boolean, boolean, String)} instead
     */
    @SuppressWarnings("resource")
    @Deprecated
    public Config(final File file, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
        this(new FileInputStream(file), file, simple, allowEmptySections, defaultSeparator);
    }

    /**
     * For testing
     *
     * @param s
     * @param fileName
     * @param simple
     * @param allowEmptySections
     * @param defaultSeparator
     * @throws IOException
     */
    public Config(final String s, final String fileName, final boolean simple, final boolean allowEmptySections, final String defaultSeparator) throws IOException {
        this(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), fileName, simple, allowEmptySections, defaultSeparator);
    }

    String getIndentation() {
        return indentation;
    }

    void setIndentation(final String indent) {
        assert indent != null && !indent.isEmpty() : indent;
        indentation = indent;
        indentationName = indent.charAt(0) == ' ' ? "space" : "tab";
    }

    String getIndentationName() {
        return indentationName;
    }

    public SectionNode getMainNode() {
        return main;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Saves the config to a file.
     *
     * @param f The file to save to
     * @throws IOException If the file could not be written to.
     */
    public void save(final File f) throws IOException {
        separator = defaultSeparator;
        try (final PrintWriter w = new PrintWriter(f, "UTF-8")) {
            main.save(w);
            w.flush();
        }
    }

    /**
     * Sets this config's values to those in the given config.
     * <p>
     * Used by Skript to import old settings into the updated config. The return value is used to not modify the config if no new options were added.
     *
     * @param other
     * @return Whatever the configs' keys differ, i.e. false == configs only differ in values, not keys.
     */
    public boolean setValues(final Config other) {
        return main.setValues(other.main);
    }

    public boolean setValues(final Config other, final String... excluded) {
        return main.setValues(other.main, excluded);
    }

    @Nullable
    public File getFile() {
        return file;
    }

    /**
     * @return The most recent separator. Only useful while the file is loading.
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * @return A separator string useful for saving, e.g. ": " or " = ".
     */
    public String getSaveSeparator() {
        if (":".equals(separator))
            return ": ";
        if ("=".equals(separator))
            return " = ";
        return ' ' + separator + ' ';
    }

    /**
     * Splits the given path at the dot character and passes the result to {@link #get(String...)}.
     *
     * @param path
     * @return <tt>get(path.split("\\."))</tt>
     */
    @SuppressWarnings("null")
    @Nullable
    public String getByPath(final String path) {
        return get(path.split("\\."));
    }

    /**
     * Gets an entry node's value at the designated path
     *
     * @param path
     * @return The entry node's value at the location defined by path or null if it either doesn't exist or is not an entry.
     */
    @Nullable
    public String get(final String... path) {
        SectionNode section = main;
        for (int i = 0; i < path.length; i++) {
            final Node n = section.get(path[i]);
            if (n == null)
                return null;
            if (n instanceof SectionNode) {
                if (i == path.length - 1)
                    return null;
                section = (SectionNode) n;
            } else {
                if (n instanceof EntryNode && i == path.length - 1)
                    return ((EntryNode) n).getValue();
                return null;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return main.isEmpty();
    }

    public Map<String, String> toMap(final String separator) {
        return main.toMap("", separator);
    }

    /**
     * Backwards compatibility.
     *
     * @see Config#validate(NodeValidator)
     * @deprecated Use more generic {@link Config#validate(NodeValidator)}
     */
    @Deprecated
    public boolean validate(final SectionValidator validator) {
        return validate((NodeValidator) validator);
    }

    public boolean validate(final NodeValidator validator) {
        return validator.validate(main);
    }

    private final void load(final Class<?> c, @Nullable final Object o, final String path) {
        for (final Field f : c.getDeclaredFields()) {
            if (o != null || Modifier.isStatic(f.getModifiers())) {
                try {
                    f.setAccessible(true);
                    if (OptionSection.class.isAssignableFrom(f.getType())) {
                        final Object p = f.get(o);
                        @NonNull final Class<?> pc = p.getClass();
                        load(pc, p, path + ((OptionSection) p).key + '.');
                    } else if (Option.class.isAssignableFrom(f.getType())) {
                        ((Option<?>) f.get(o)).set(this, path);
                    }
                } catch (final Throwable tw) {
                    if (Skript.testing() || Skript.debug())
                        Skript.exception(tw, "Error when setting field \"" + f.getName() + '"' + " in class \"" + c.getCanonicalName() + "\" (path: \"" + path + "\", object: \"" + (o != null ? o : "null") + "\")");
                }
            }
        }
    }

    /**
     * Sets all {@link Option} fields of the given object to the values from this config
     */
    public void load(final Object o) {
        load(o.getClass(), o, "");
    }

    /**
     * Sets all static {@link Option} fields of the given class to the values from this config
     */
    public void load(final Class<?> c) {
        load(c, null, "");
    }

    @Override
    public final String toString() {
        return "Config{" +
                "defaultSeparator='" + defaultSeparator + '\'' +
                ", allowEmptySections=" + allowEmptySections +
                ", main=" + main +
                ", simple=" + simple +
                ", separator='" + separator + '\'' +
                ", level=" + level +
                ", errors=" + errors +
                ", fileName='" + fileName + '\'' +
                ", file=" + file +
                ", indentation='" + indentation + '\'' +
                ", indentationName='" + indentationName + '\'' +
                '}';
    }

}
