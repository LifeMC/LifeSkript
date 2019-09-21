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

package ch.njol.skript;

import ch.njol.skript.localization.Language;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Version;
import ch.njol.util.coll.iterator.EnumerationIterable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for Skript addons. Use {@link Skript#registerAddon(JavaPlugin)} to create a SkriptAddon instance for your plugin.
 *
 * @author Peter Güttinger
 */
public final class SkriptAddon {

    public static final Method getFile =
            Skript.methodForName(JavaPlugin.class, "getFile", true);
    private static final Matcher VERSION_PATTERN_MATCHER = Pattern.compile("(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?").matcher("");

    public final JavaPlugin plugin;
    public final Version version;
    private final String name;

    private final AtomicInteger loadedClasses = new AtomicInteger();
    private final AtomicInteger unloadableClasses = new AtomicInteger();
    @Nullable
    private String languageFileDirectory;
    @Nullable
    private File file;

    /**
     * Package-private constructor. Use {@link Skript#registerAddon(JavaPlugin)} to get a SkriptAddon for your plugin.
     *
     * @param p The plugin that this add-on refers to.
     */
    SkriptAddon(final JavaPlugin p) {
        plugin = p;
        name = p.getName();
        Version v;
        try {
            v = new Version(p.getDescription().getVersion());
        } catch (final IllegalArgumentException e) {
            final Matcher m = VERSION_PATTERN_MATCHER.reset(p.getDescription().getVersion());
            if (!m.find())
                throw new IllegalArgumentException("The version of the plugin " + p.getName() + " does not contain any numbers: " + p.getDescription().getVersion());
            v = new Version(Utils.parseInt(m.group(1)), m.group(2) == null ? 0 : Utils.parseInt("" + m.group(2)), m.group(3) == null ? 0 : Utils.parseInt("" + m.group(3)));
            Skript.warning("The plugin " + p.getName() + " uses a non-standard version syntax: '" + p.getDescription().getVersion() + "'. Skript will use " + v + " instead.");
        }
        version = v;
    }

    /**
     * @param p The plugin that this add-on refers to.
     * @deprecated Backwards compatibility. Use {@link SkriptAddon#SkriptAddon(JavaPlugin)}
     */
    @Deprecated
    SkriptAddon(final Plugin p) {
        this((JavaPlugin) p);
    }

    @Override
    public final String toString() {
        return name;
    }

    public final String getName() {
        return name;
    }

    public final int getLoadedClassCount() {
        return loadedClasses.get();
    }

    public final int getUnloadableClassCount() {
        return unloadableClasses.get();
    }

    /**
     * Loads classes of the plugin by package. Useful for registering many syntax elements like Skript does it.
     *
     * @param basePackage The base package to add to all sub packages, e.g. <tt>"ch.njol.skript"</tt>.
     * @param subPackages Which subpackages of the base package should be loaded, e.g. <tt>"expressions", "conditions", "effects"</tt>. Subpackages of these packages will be loaded
     *                    as well. Use an empty array to load all subpackages of the base package.
     * @return This SkriptAddon
     * @throws IOException If some error occurred attempting to read the plugin's jar file.
     */
    public final SkriptAddon loadClasses(String basePackage, final String... subPackages) throws IOException {
        assert subPackages != null;
        //noinspection AssertWithSideEffects
        assert getFile() != null;
        try (final JarFile jar = new JarFile(getFile(), false)) {
            for (int i = 0; i < subPackages.length; i++)
                subPackages[i] = subPackages[i].replace('.', '/') + '/';
            basePackage = basePackage.replace('.', '/') + '/';
            for (final JarEntry e : new EnumerationIterable<>(jar.entries())) {
                if (e.getName().startsWith(basePackage) && e.getName().endsWith(".class")) {
                    boolean load = subPackages.length == 0;
                    for (final String sub : subPackages) {
                        if (e.getName().startsWith(sub, basePackage.length())) {
                            load = true;
                            break;
                        }
                    }
                    if (load) {
                        final String c = e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length());
                        if ((c.toLowerCase(Locale.ENGLISH).contains("guardian") || c.toLowerCase(Locale.ENGLISH).contains("rabbit")) && !Skript.isRunningMinecraft(1, 8)) {
                            // Ooh, we're on 1.7! Skip those classes. We already ignore exceptions when not on the very high verbosity, but anyway.
                            continue;
                        }
                        try {
                            Class.forName(c, true, plugin.getClass().getClassLoader());
                            loadedClasses.incrementAndGet(); // successfully loaded
                        } catch (final NoClassDefFoundError ncdfe) {
                            // not supported or not available on this version, skip it.
                            if (Skript.logHigh()) {
                                if (!plugin.equals(Skript.getInstance())) { // if it is not a Skript class (e.g from an addon)
                                    Skript.exception(ncdfe, "Cannot load class " + c + " from " + this);
                                } else {
                                    // Probably Skript is running ona unsupported or half supported server version,
                                    // if user is not debugging or testing then just ignore that class.
                                    if (Skript.testing() || Skript.debug()) {
                                        Skript.exception(ncdfe, "Cannot load class " + c + " from " + this);
                                    }
                                }
                            }
                            unloadableClasses.incrementAndGet();
                        } catch (final ExceptionInInitializerError err) {
                            Skript.exception(err.getCause(), this + "'s class " + c + " generated an exception while loading");
                            unloadableClasses.incrementAndGet();
                        } catch (final LinkageError le) {
                            if (Skript.testing() || Skript.debug()) {
                                Skript.exception(le, "Cannot load class " + c + " from " + this);
                            }
                            unloadableClasses.incrementAndGet();
                        } catch (final Throwable ex) {
                            // Catch any other exceptions.
                            Skript.exception(ex, "Cannot load class " + c + " from " + this);
                            unloadableClasses.incrementAndGet();
                        }
                    }
                }
            }
        }
        return this;
    }

    @Nullable
    public final String getLanguageFileDirectory() {
        return languageFileDirectory;
    }

    /**
     * Makes Skript load language files from the specified directory, e.g. "lang" or "skript lang" if you have a lang folder yourself. Localised files will be read from the
     * plugin's jar and the plugin's data folder, but the default English file is only taken from the jar and <b>must</b> exist!
     *
     * @param directory Directory name
     * @return This SkriptAddon
     */
    public final SkriptAddon setLanguageFileDirectory(String directory) {
        if (languageFileDirectory != null)
            throw new IllegalStateException();
        directory = directory.replace('\\', '/');
        if (directory.endsWith("/"))
            directory = directory.substring(0, directory.length() - 1);
        languageFileDirectory = directory;
        Language.loadDefault(this);
        return this;
    }

    /**
     * @return The jar file of the plugin. The first invocation of this method uses reflection to invoke the protected method {@link JavaPlugin#getFile()} to get the plugin's jar
     * file. The file is then cached and returned upon subsequent calls to this method to reduce usage of reflection.
     */
    @Nullable
    @SuppressWarnings("JavadocReference")
    public final File getFile() {
        if (file != null)
            return file;
        if (plugin instanceof NonReflectiveAddon)
            return file = ((NonReflectiveAddon) plugin).getFile();
        try {
            file = (File) getFile.invoke(plugin);
            return file;
        } catch (final IllegalArgumentException e) {
            Skript.outdatedError(e);
            return null;
        } catch (final IllegalAccessException | SecurityException e) {
            throw Skript.sneakyThrow(e);
        } catch (final InvocationTargetException e) {
            throw Skript.sneakyThrow(e.getCause());
        }
    }

}
