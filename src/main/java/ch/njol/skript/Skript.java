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

import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.bukkitutil.Workarounds;
import ch.njol.skript.classes.data.*;
import ch.njol.skript.command.Commands;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.events.EvtSkript;
import ch.njol.skript.expressions.ExprEntities;
import ch.njol.skript.hooks.Hook;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.localization.FormattedMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.*;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.*;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Closeable;
import ch.njol.util.StringUtils;
import ch.njol.util.WebUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.EnumerationIterable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jdt.annotation.Nullable;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

// TODO meaningful error if someone uses an %expression with percent signs% outside of text or a variable

/**
 * <b>Skript</b> - A Bukkit plugin to modify how Minecraft behaves without having to write a single line of code (You'll likely be writing some code though if you're reading this
 * =P)
 * <p>
 * Use this class to extend this plugin's functionality by adding more {@link Condition conditions}, {@link Effect effects}, {@link ch.njol.skript.lang.util.SimpleExpression expressions}, etc.
 * <p>
 * If your plugin.yml contains <tt>'depend: [Skript]'</tt> then your plugin will not start at all if Skript is not present. Add <tt>'softdepend: [Skript]'</tt> to your plugin.yml
 * if you want your plugin to work even if Skript isn't present, but want to make sure that Skript gets loaded before your plugin.
 * <p>
 * If you use 'softdepend' you can test whether Skript is loaded with <tt>'Bukkit.getPluginManager().getPlugin(&quot;Skript&quot;) != null'</tt>
 * <p>
 * Once you made sure that Skript is loaded you can use <code>Skript.getInstance()</code> whenever you need a reference to the plugin, but you likely won't need it since all API
 * methods are static.
 *
 * @author Peter Güttinger
 * @see #registerAddon(JavaPlugin)
 * @see #registerCondition(Class, String...)
 * @see #registerEffect(Class, String...)
 * @see #registerExpression(Class, Class, ExpressionType, String...)
 * @see #registerEvent(String, Class, Class, String...)
 * @see ch.njol.skript.registrations.EventValues#registerEventValue(Class, Class, Getter, int)
 * @see Classes#registerClass(ch.njol.skript.classes.ClassInfo)
 * @see ch.njol.skript.registrations.Comparators#registerComparator(Class, Class, ch.njol.skript.classes.Comparator)
 * @see Converters#registerConverter(Class, Class, ch.njol.skript.classes.Converter)
 */
public final class Skript extends JavaPlugin implements Listener {

    // ================ CONSTANTS ================

    public static final String LATEST_VERSION_DOWNLOAD_LINK = "https://github.com/LifeMC/LifeSkript/releases";
    public static final String ISSUES_LINK = "https://github.com/LifeMC/LifeSkript/issues";

    // ================ PLUGIN ================
    public static final Message m_invalid_reload = new Message("skript.invalid reload"),
            m_finished_loading = new Message("skript.finished loading");
    public static final String SCRIPTSFOLDER = "scripts";
    /**
     * A small value, useful for comparing doubles or floats.
     * <p>
     * E.g. to test whether two floating-point numbers are equal:
     *
     * <pre>
     * Math.abs(a - b) &lt; Skript.EPSILON
     * </pre>
     * <p>
     * or whether a location is within a specific radius of another location:
     *
     * <pre>
     * location.distanceSquared(center) - radius * radius &lt; Skript.EPSILON
     * </pre>
     *
     * @see #EPSILON_MULT
     */
    public static final double EPSILON = 1e-10;
    /**
     * A value a bit larger than 1
     *
     * @see #EPSILON
     */
    public static final double EPSILON_MULT = 1.00001;
    /**
     * The maximum ID a block can have in Minecraft.
     */
    public static final int MAXBLOCKID = 255;
    /**
     * The maximum data value of Minecraft, i.e. Short.MAX_VALUE - Short.MIN_VALUE.
     */
    public static final int MAXDATAVALUE = Short.MAX_VALUE - Short.MIN_VALUE;
    public static final String SKRIPT_PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "Skript" + ChatColor.GRAY + "]" + ChatColor.RESET + " ";
    @SuppressWarnings("null")
    private static final Collection<Closeable> closeOnDisable = Collections.synchronizedCollection(new ArrayList<>());
    private static final HashMap<String, SkriptAddon> addons = new HashMap<>();
    private static final Collection<SyntaxElementInfo<? extends Condition>> conditions = new ArrayList<>(100);
    private static final Collection<SyntaxElementInfo<? extends Effect>> effects = new ArrayList<>(100);
    private static final Collection<SyntaxElementInfo<? extends Statement>> statements = new ArrayList<>(100);
    private static final List<ExpressionInfo<?, ?>> expressions = new ArrayList<>(300);
    private static final int[] expressionTypesStartIndices = new int[ExpressionType.values().length];
    private static final Collection<SkriptEventInfo<?>> events = new ArrayList<>(100);
    private static final String EXCEPTION_PREFIX = "#!#! ";
    /**
     * use {@link Skript#getInstance()} for asserted access
     */
    @Nullable
    public static Skript instance;
    static boolean disabled;
    static boolean updateAvailable;
    static boolean updateChecked;
    @Nullable
    static String latestVersion;
    static Version minecraftVersion = new Version(666);
    private static boolean first;
    private static @Nullable ServerPlatform serverPlatform;
    private static @Nullable Boolean hasJLineSupport = null;
    private static final boolean isUnsupportedTerminal = "jline.UnsupportedTerminal".equals(System.getProperty("jline.terminal")) || "org.bukkit.craftbukkit.libs.jline.UnsupportedTerminal".equals(System.getProperty("org.bukkit.craftbukkit.libs.jline.terminal"));
    @Nullable
    private static Version version;
    public static final @Nullable Class<?> craftbukkitMain = classForName("org.bukkit.craftbukkit.Main");
    public static final String SKRIPT_PREFIX_CONSOLE = hasJLineSupport() ? Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString() + "[" + Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString() + "Skript" + Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString() + "]" + Ansi.ansi().a(Ansi.Attribute.RESET).toString() + " " : "[Skript] ";
    private static final boolean isCraftBukkit = craftbukkitMain != null || classExists("org.bukkit.craftbukkit.CraftServer");
    public static final boolean usingBukkit = classExists("org.bukkit.Bukkit");
    static final boolean runningCraftBukkit = isCraftBukkit;
    public static final FormattedMessage m_update_available = new FormattedMessage("updater.update available", () -> new String[]{Skript.getLatestVersion(), Skript.getVersion().toString()});
    public static final UncaughtExceptionHandler UEH = (t, e) -> Skript.exception(e, "Exception in thread " + (t == null ? null : t.getName()));
    private static boolean acceptRegistrations = true;
    @Nullable
    private static SkriptAddon addon;

    public Skript() throws IllegalStateException {
        super();
        if (instance != null)
            throw new IllegalStateException("Cannot create multiple instances of Skript!");
        instance = this;
    }

    /**
     * Checks if the Bukkit (and server) is loaded, enabled and working
     * correctly. You should use this method when doing Bukkit operations
     * within a method callable from tests, or outside of Bukkit.
     *
     * @return True if the Bukkit (and server) is loaded, enabled and
     * working correctly.
     */
    public static final boolean isBukkitRunning() {
        return usingBukkit && Bukkit.getServer() != null;
    }

    /**
     * Checks if the Skript and Bukkit is running correctly.
     *
     * @see Skript#isBukkitRunning()
     *
     * @return True if the Skript and Bukkit is running correctly.
     */
    public static final boolean isSkriptRunning() {
        return isBukkitRunning() && Skript.instance != null && Skript.instance.isEnabled();
    }

    /**
     * Checks if the current server has Skript add-ons loaded.
     * Add-ons are plugins that adds new expressions, conditions, events etc.
     *
     * While add-ons provide nice & cool new features, add-ons may
     * cause errors because they interact with Skript, and some of them
     * even uses reflection to alter how Skript works.
     *
     * So, in fact, servers with add-ons are not using vanilla Skript.
     *
     * @return True if this server has at least one {@link SkriptAddon}.
     */
    public static final boolean hasAddons() {
        if (addons.isEmpty())
            return false;
        for (final SkriptAddon addon : addons.values())
            // Skript also registers itself as an add-on.
            if (!(addon.plugin instanceof Skript))
                return true;
        return false;
    }

    /**
     * Checks if a CraftBukkit server has JLine support.
     * Calculates once; returns cached value afterwards.
     *
     * @return Returns true if the server has JLine support,
     * and currently enabled.
     */
    @SuppressWarnings("null")
	public static final boolean hasJLineSupport() {
        if (hasJLineSupport != null)
            return hasJLineSupport;
        try {
            if (Skript.testing() && Skript.debug()) {
                if (isUnsupportedTerminal)
                    debug("Unsupported terminal");
                if (craftbukkitMain == null)
                    debug("Null craftbukkit main");
                if (craftbukkitMain != null && !(boolean) craftbukkitMain.getField("useJline").get(null))
                    debug("False useJline");
                if (craftbukkitMain != null && !(boolean) craftbukkitMain.getField("useConsole").get(null))
                    debug("No console");
            }
            return hasJLineSupport = !isUnsupportedTerminal && craftbukkitMain != null && (boolean) craftbukkitMain.getField("useJline").get(null) && (boolean) craftbukkitMain.getField("useConsole").get(null);
        } catch (final ClassCastException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException | NullPointerException e) {
            throw Skript.exception(e);
        }
    }

    /**
     * Gets the {@link ServerPlatform} of the server that Skript currently
     * runs on.
     *
     * It returns {@link ServerPlatform#BUKKIT_UNKNOWN} when we can't detect
     * the server platform / software.
     *
     * It checks all popular platforms and performs some checks, but anyway,
     * the returned value may be incorrect.
     *
     * Also, it returns a cached value after the first call. The first call is
     * probably made by Skript itself.
     *
     * @return The {@link ServerPlatform} of the server that Skript currently runs on.
     */
    public static final ServerPlatform getServerPlatform() {
        if (serverPlatform != null)
            return serverPlatform;
        if (classExists("com.lifespigot.Main")) {
            return serverPlatform = ServerPlatform.LIFE_SPIGOT; // LifeSpigot is a fork of Paper, so check it first.
        } else if (classExists("net.techcable.tacospigot.TacoSpigotConfig")) {
            return serverPlatform = ServerPlatform.BUKKIT_TACO; // TacoSpigot also is a fork of Paper, so check before Paper.
        } else if (classExists("net.glowstone.GlowServer")) {
            return serverPlatform = ServerPlatform.GLOWSTONE; // Glowstone has timings too, so must check for it first
        } else if (classExists("co.aikar.timings.Timings") || classExists("org.github.paperspigot.PaperSpigotConfig") || classExists("com.github.paperspigot.PaperSpigotConfig")) {
            return serverPlatform = ServerPlatform.BUKKIT_PAPER; // Could be Sponge, but it doesn't work at all at the moment
        } else if (classExists("org.spigotmc.SpigotConfig")) {
            return serverPlatform = ServerPlatform.BUKKIT_SPIGOT;
        } else if (isCraftBukkit) {
            // At some point, CraftServer got removed or moved
            return serverPlatform = ServerPlatform.BUKKIT_CRAFTBUKKIT;
        } else { // Probably some ancient Bukkit implementation
            return serverPlatform = ServerPlatform.BUKKIT_UNKNOWN;
        }
    }

    public static final Skript getInstance() {
        final Skript i = instance;
        if (i == null)
            throw new IllegalStateException("Can't get Skript instance because it was null!");
        return i;
    }

    public static final Version getVersion() {
        final Version v = version;
        if (v == null)
            throw new IllegalStateException("Can't get Skript version because it was null!");
        return v;
    }

    public static final String getDownloadLink() {
        return "You can download the latest Skript version here: " + LATEST_VERSION_DOWNLOAD_LINK;
    }

    public static final void printDownloadLink() {
        if (!Skript.getInstance().isEnabled())
            return;
        Bukkit.getScheduler().runTask(getInstance(), () -> info(getDownloadLink()));
    }

    public static final void printIssuesLink() {
        if (!Skript.getInstance().isEnabled())
            return;
        Bukkit.getScheduler().runTask(getInstance(), () -> {
            info("Please report all issues you encounter to the issues page:");
            info(ISSUES_LINK);
        });
    }

    /**
     * Gets the latest version of the Skript. It only
     * returns the latest version of the <b>this implementation of Skript.</b>
     *
     * So, if you forked the project, or using another implementation, this method
     * may return an incorrect value. (it doesn't exist another forks at all)
     *
     * Also, this method does not throw any exceptions and may return null when
     * web server is down or there is no internet connection.
     *
     * This method must NOT be called from main server thread, becuase it makes
     * blocking web connection. If using outside of Bukkit, it don't cares.
     *
     * Currently loading of Skript class outside of Bukkit already causes
     * classpath errors, so be familiar.
     *
     * @return The latest version of the Skript for <b>this implementation of Skript.</b>
     *
     * @see Skript#getLatestVersion(Consumer)
     */
    @Nullable
    public static final String getLatestVersion() {
        return getLatestVersion(null);
    }

    /**
     * Gets the latest version of the Skript. It only
     * returns the latest version of the <b>this implementation of Skript.</b>
     *
     * So, if you forked the project, or using another implementation, this method
     * may return an incorrect value. (it doesn't exist another forks at all)
     *
     * Also, this method does not throw any exceptions and may return null when
     * web server is down or there is no internet connection.
     *
     * This method must NOT be called from main server thread, becuase it makes
     * blocking web connection. If using outside of Bukkit, it don't cares.
     *
     * Currently loading of Skript class outside of Bukkit already causes
     * classpath errors, so be familiar.
     *
     * @param handler The error handler. It's accept method will be called
     *                when an error occurs. Null return value also indicates an error.
     *
     * @return The latest version of the Skript for <b>this implementation of Skript.</b>
     */
    @Nullable
    public static final String getLatestVersion(final @Nullable Consumer<Throwable> handler) {
        return getLatestVersion(handler, true);
    }

    /**
     * Gets the latest version of the Skript. It only
     * returns the latest version of the <b>this implementation of Skript.</b>
     *
     * So, if you forked the project, or using another implementation, this method
     * may return an incorrect value. (it doesn't exist another forks at all)
     *
     * Also, this method does not throw any exceptions and may return null when
     * web server is down or there is no internet connection.
     *
     * This method must NOT be called from main server thread, becuase it makes
     * blocking web connection. If using outside of Bukkit, it don't cares.
     *
     * Currently loading of Skript class outside of Bukkit already causes
     * classpath errors, so be familiar.
     *
     * @param handler The error handler. It's accept method will be called
     *                when an error occurs. Null return value also indicates an error.
     *
     * @param checkThread Pass false to disable checking for Bukkit server
     *                    main thread. It checks by default for preventing freezes.
     *
     * @return The latest version of the Skript for <b>this implementation of Skript.</b>
     */
    @Nullable
    public static final String getLatestVersion(final @Nullable Consumer<Throwable> handler,
                                                final boolean checkThread) {
        if (checkThread && classExists("org.bukkit.Bukkit") && Bukkit.isPrimaryThread())
            throw new SkriptAPIException("This method must be called asynchronously!");
        try {
            return WebUtils.getResponse("https://www.lifemcserver.com/skript-latest.php");
        } catch (final Throwable tw) {
            if (handler != null)
                handler.accept(tw);
            return null;
        }
    }

    /**
     * Returns the version that this server is running, but you don't generally need this method, use {@link Skript#classExists(String)} or
     * {@link Skript#methodExists(Class, String, Class[])} instead for checking certain class / feature is exists.
     *
     * The result produced by {@link Skript#classExists(String)} or {@link Skript#methodExists(Class, String, Class[])}
     * is more accurate because who knows the server is not running a custom implementation which includes
     * certain features in old versions, or does not include a feature in a new version?
     *
     * @return The version that this server is running as a {@link Version} object.
     *
     * @see Skript#isRunningMinecraft(int, int)
     * @see Skript#classExists(String)
     * @see Skript#methodExists(Class, String, Class[])
     */
    public static final Version getMinecraftVersion() {
        return minecraftVersion;
    }

    /**
     * Returns whatever this server is running CraftBukkit or CraftBukkit based server implementation.
     *
     * @return Whatever this server is running CraftBukkit or CraftBukkit based server implementation.
     *
     * @see Skript#getServerPlatform()
     */
    public static final boolean isRunningCraftBukkit() {
        return runningCraftBukkit;
    }

    // ================ CONSTANTS, OPTIONS & OTHER ================

    /**
     * Returns whatever this server is running the given Minecraft <tt>major.minor</tt> <b>or higher</b>
     * version, but you don't generally need this method, use {@link Skript#classExists(String)} or
     * {@link Skript#methodExists(Class, String, Class[])} instead for checking certain class / feature is exists.
     *
     * The result produced by {@link Skript#classExists(String)} or {@link Skript#methodExists(Class, String, Class[])}
     * is more accurate because who knows the server is not running a custom implementation which includes
     * certain features in old versions, or does not include a feature in a new version?
     *
     * @return Whatever this server is running Minecraft <tt>major.minor</tt> <b>or higher</b>
     *
     * @see Skript#isRunningMinecraft(int, int)
     * @see Skript#classExists(String)
     * @see Skript#methodExists(Class, String, Class[])
     */
    public static final boolean isRunningMinecraft(final int major, final int minor) {
        return minecraftVersion.compareTo(major, minor) >= 0;
    }

    /**
     * Returns whatever this server is running the given Minecraft <tt>major.minor.revision</tt> <b>or higher</b>
     * version, but you don't generally need this method, use {@link Skript#classExists(String)} or
     * {@link Skript#methodExists(Class, String, Class[])} instead for checking certain class / feature is exists.
     *
     * The result produced by {@link Skript#classExists(String)} or {@link Skript#methodExists(Class, String, Class[])}
     * is more accurate because who knows the server is not running a custom implementation which includes
     * certain features in old versions, or does not include a feature in a new version?
     *
     * @return Whatever this server is running Minecraft <tt>major.minor.revision</tt> <b>or higher</b>
     *
     * @see Skript#isRunningMinecraft(int, int)
     * @see Skript#classExists(String)
     * @see Skript#methodExists(Class, String, Class[])
     */
    public static final boolean isRunningMinecraft(final int major, final int minor, final int revision) {
        return minecraftVersion.compareTo(major, minor, revision) >= 0;
    }

    /**
     * Returns whatever this server is running the given <b>exact</b> Minecraft version,
     * but you don't generally need this method, use {@link Skript#classExists(String)} or
     * {@link Skript#methodExists(Class, String, Class[])} instead for checking certain class / feature is exists.
     *
     * The result produced by {@link Skript#classExists(String)} or {@link Skript#methodExists(Class, String, Class[])}
     * is more accurate because who knows the server is not running a custom implementation which includes
     * certain features in old versions, or does not include a feature in a new version?
     *
     * @return Whatever this server is running the given <b>exact</b> Minecraft version.
     *
     * @see Skript#isRunningMinecraft(int, int)
     * @see Skript#classExists(String)
     * @see Skript#methodExists(Class, String, Class[])
     */
    public static final boolean isRunningMinecraft(final Version v) {
        return minecraftVersion.compareTo(v) >= 0;
    }

    /**
     * Used to test whether certain Bukkit features are supported.
     *
     * @param className The {@link Class#getCanonicalName() canonical name} of the class
     * @return Whether the given class exists.
     * @deprecated use {@link #classExists(String)}
     */
    @Deprecated
    public static final boolean supports(final String className) {
        return classExists(className);
    }

    /**
     * Tests whether a given class exists in the classpath.
     *
     * Constantly calling this method does not cache values -
     * preferably assign the result to a static final variable.
     *
     * If null name is passed, the result will always be false.
     * No actual checks performed with null name. It just returns false.
     *
     * @param className The {@link Class#getCanonicalName() canonical name} of the class
     * @return Whether the given class exists.
     */
    @SuppressWarnings({"null", "unused"})
    public static final boolean classExists(final @Nullable String className) {
        if (className == null)
            return false;
        try {
            // Don't initialize the class just for checking if it exists.
            ClassLoader classLoader = Skript.class.getClassLoader();
            if (classLoader == null)
                classLoader = ClassLoader.getSystemClassLoader();
            Class.forName(className, /* initialize: */ false, classLoader);
            return true;
        } catch (final ClassNotFoundException e) {
            //if (Skript.testing() && Skript.debug())
                //debug("The class \"" + className + "\" does not exist in classpath.");
            return false;
        }
    }

    /**
     * Gets a specific class if it exists, otherwise returns null.
     * Note this simply catches the exception and returns null on exception.
     * Yo should use {@link Skript#classExists(String)} if you want to actually check if it exists.
     *
     * @param className The {@link Class#getCanonicalName() canonical name} of the class
     * @return The class representing the given class name
     */
    @Nullable
    @SuppressWarnings({"null", "unused"})
    public static final Class<?> classForName(final @Nullable String className) {
        if (className == null)
            return null;
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException ex) {
            //if (Skript.testing() && Skript.debug())
                //debug("The class \"" + className + "\" does not exist in classpath.");
            return null;
        }
    }

    /**
     * Tests whether a method exists in the given class.
     *
     * Save the result to a static final variable for maximum performance.
     *
     * @param c              The class
     * @param methodName     The name of the method
     * @param parameterTypes The parameter types of the method
     * @return Whether the given method exists.
     */
    @SuppressWarnings("null")
    public static final boolean methodExists(final @Nullable Class<?> c, final @Nullable String methodName, final Class<?>... parameterTypes) {
        if (c == null || methodName == null)
            return false;
        try {
            c.getDeclaredMethod(methodName, parameterTypes);
            return true;
        } catch (final NoSuchMethodException | SecurityException e) {
            //if (Skript.testing() && Skript.debug())
                //debug("The method \"" + methodName + "\" does not exist in class \"" + c.getCanonicalName() + "\".");
            return false;
        }
    }

    /**
     * Tests whether a method exists in the given class, and whether the return type matches the expected one.
     * <p>
     * Note that this method doesn't work properly if multiple methods with the same name and parameters exist but have different return types.
     *
     * Save the result to a static final variable for maximum performance.
     *
     * @param c              The class
     * @param methodName     The name of the method
     * @param parameterTypes The parameter types of the method
     * @param returnType     The expected return type
     * @return Whether the given method exists.
     */
    @SuppressWarnings("null")
    public static final boolean methodExists(final @Nullable Class<?> c, final @Nullable String methodName, final Class<?>[] parameterTypes, final Class<?> returnType) {
        if (c == null || methodName == null)
            return false;
        try {
            final Method m = c.getDeclaredMethod(methodName, parameterTypes);
            return m.getReturnType() == returnType;
        } catch (final NoSuchMethodException | SecurityException e) {
            //if (Skript.testing() && Skript.debug())
                //debug("The method \"" + methodName + "\" does not exist in class \"" + c.getCanonicalName() + "\".");
            return false;
        }
    }

    /**
     * Tests whether a field exists in the given class.
     *
     * Save the result to a static final variable for maximum performance.
     *
     * @param c         The class
     * @param fieldName The name of the field
     * @return Whether the given field exists.
     */
    @SuppressWarnings("null")
    public static final boolean fieldExists(final @Nullable Class<?> c, final @Nullable String fieldName) {
        if (c == null || fieldName == null)
            return false;
        try {
            c.getDeclaredField(fieldName);
            return true;
        } catch (final NoSuchFieldException | SecurityException e) {
            //if (Skript.testing() && Skript.debug())
                //debug("The field \"" + fieldName + "\" does not exist in class \"" + c.getCanonicalName() + "\".");
            return false;
        }
    }

    /**
     * Clears triggers, commands, functions and variable names
     */
    static final void disableScripts() {
        VariableString.variableNames.clear();
        SkriptEventHandler.removeAllTriggers();
        Commands.clearCommands();
        Functions.clearFunctions();
    }

    // ================ REGISTRATIONS ================

    /**
     * Prints errors from reloading the config & scripts
     */
    static final void reload() {
        disableScripts();
        reloadMainConfig();
        reloadAliases();
        ScriptLoader.loadScripts();
    }

    /**
     * Prints errors
     */
    static final void reloadScripts() {
        disableScripts();
        ScriptLoader.loadScripts();
    }

    /**
     * Prints errors
     */
    static final void reloadMainConfig() {
        SkriptConfig.load();
    }

    /**
     * Prints errors
     */
    static final void reloadAliases() {
        Aliases.clear();
        Aliases.load();
    }

    // ================ ADDONS ================

    /**
     * Registers a Closeable that should be closed when this plugin is disabled.
     * <p>
     * All registered Closeables will be closed after all scripts have been stopped.
     *
     * @param closeable The closeable to close when disabling the plugin.
     */
    public static final void closeOnDisable(final Closeable closeable) {
        closeOnDisable.add(closeable);
    }

    public static final void outdatedError() {
        error("Skript v" + getInstance().getDescription().getVersion() + " is not fully compatible with Bukkit " + Bukkit.getVersion() + ". Some feature(s) will be broken until you update Skript.");
    }

    public static final void outdatedError(final Throwable tw) {
        outdatedError();
        if (testing() || debug())
            tw.printStackTrace();
    }

    // TODO localise Infinity, -Infinity, NaN (and decimal point?)
    public static final String toString(final double n) {
        return StringUtils.toString(n, SkriptConfig.numberAccuracy.value());
    }

    /**
     * Creates a new Thread and sets its UncaughtExceptionHandler. The Thread is not started automatically.
     */
    public static final Thread newThread(final Runnable r, final String name) {
        final Thread t = new Thread(r, name);
        t.setUncaughtExceptionHandler(UEH);
        if (Skript.debug())
            Skript.debug("Created thread: \"" + name + "\"");
        return t;
    }

    public static final boolean isAcceptRegistrations() {
        return acceptRegistrations;
    }

    public static final void checkAcceptRegistrations() {
        if (!acceptRegistrations)
            throw new SkriptAPIException("Registering is disabled after initialization!");
    }

    // ================ CONDITIONS & EFFECTS ================

    static final void stopAcceptingRegistrations() {
        acceptRegistrations = false;

        Converters.createMissingConverters();
        Classes.onRegistrationsStop();
    }

    /**
     * Registers an addon to Skript. This is currently not required for addons to work, but the returned {@link SkriptAddon} provides useful methods for registering syntax elements
     * and adding new strings to Skript's localization system (e.g. the required "types.[type]" strings for registered classes).
     *
     * @param p The plugin
     */
    public static final SkriptAddon registerAddon(final JavaPlugin p) {
        checkAcceptRegistrations();
        if (addons.containsKey(p.getName()))
            throw new IllegalArgumentException("The addon " + p.getName() + " is already registered!");
        final SkriptAddon addon = new SkriptAddon(p);
        addons.put(p.getName(), addon);
        if (Skript.logVeryHigh())
            Skript.info("The addon " + p.getDescription().getFullName() + " was registered to Skript successfully.");
        return addon;
    }

    @Nullable
    public static final SkriptAddon getAddon(final JavaPlugin p) {
        return addons.get(p.getName());
    }

    @Nullable
    public static final SkriptAddon getAddon(final String name) {
        return addons.get(name);
    }

    @SuppressWarnings("null")
    public static final Collection<SkriptAddon> getAddons() {
        return Collections.unmodifiableCollection(addons.values());
    }

    /**
     * @return A {@link SkriptAddon} representing Skript.
     */
    public static final SkriptAddon getAddonInstance() {
        final SkriptAddon a = addon;
        if (a == null)
            return addon = new SkriptAddon(Skript.getInstance()).setLanguageFileDirectory("lang");
        else
            return a;
    }

    /**
     * registers a {@link Condition}.
     *
     * @param condition The condition's class
     * @param patterns  Skript patterns to match this condition
     */
    public static final <E extends Condition> void registerCondition(final Class<E> condition, final String... patterns) throws IllegalArgumentException {
        checkAcceptRegistrations();
        final SyntaxElementInfo<E> info = new SyntaxElementInfo<>(patterns, condition);
        conditions.add(info);
        statements.add(info);
    }

    /**
     * Registers an {@link Effect}.
     *
     * @param effect   The effect's class
     * @param patterns Skript patterns to match this effect
     */
    public static final <E extends Effect> void registerEffect(final Class<E> effect, final String... patterns) throws IllegalArgumentException {
        checkAcceptRegistrations();
        final SyntaxElementInfo<E> info = new SyntaxElementInfo<>(patterns, effect);
        effects.add(info);
        statements.add(info);
    }

    // ================ EXPRESSIONS ================

    public static final Collection<SyntaxElementInfo<? extends Statement>> getStatements() {
        return statements;
    }

    public static final Collection<SyntaxElementInfo<? extends Condition>> getConditions() {
        return conditions;
    }

    public static final Collection<SyntaxElementInfo<? extends Effect>> getEffects() {
        return effects;
    }

    /**
     * Registers an expression.
     *
     * @param c          The expression's class
     * @param returnType The superclass of all values returned by the expression
     * @param type       The expression's {@link ExpressionType type}. This is used to determine in which order to try to parse expressions.
     * @param patterns   Skript patterns that match this expression
     * @throws IllegalArgumentException if returnType is not a normal class
     */
    public static final <E extends Expression<T>, T> void registerExpression(final Class<E> c, final Class<T> returnType, final ExpressionType type, final String... patterns) throws IllegalArgumentException {
        checkAcceptRegistrations();
        if (returnType.isAnnotation() || returnType.isArray() || returnType.isPrimitive())
            throw new IllegalArgumentException("returnType must be a normal type");
        final ExpressionInfo<E, T> info = new ExpressionInfo<>(patterns, returnType, c);
        for (int i = type.ordinal() + 1; i < ExpressionType.values().length; i++) {
            expressionTypesStartIndices[i]++;
        }
        expressions.add(expressionTypesStartIndices[type.ordinal()], info);
    }

    @SuppressWarnings("null")
    public static final Iterator<ExpressionInfo<?, ?>> getExpressions() {
        return expressions.iterator();
    }

    // ================ EVENTS ================

    public static final Iterator<ExpressionInfo<?, ?>> getExpressions(final Class<?>... returnTypes) {
        return new CheckedIterator<>(getExpressions(), i -> {
            if (i == null || i.returnType == Object.class)
                return true;
            for (final Class<?> returnType : returnTypes) {
                assert returnType != null;
                if (Converters.converterExists(i.returnType, returnType))
                    return true;
            }
            return false;
        });
    }

    /**
     * Registers an event.
     *
     * @param name     Capitalised name of the event without leading "On" which is added automatically (Start the name with an asterisk to prevent this). Used for error messages and
     *                 the documentation.
     * @param c        The event's class
     * @param event    The Bukkit event this event applies to
     * @param patterns Skript patterns to match this event
     * @return A SkriptEventInfo representing the registered event. Used to generate Skript's documentation.
     */
    @SuppressWarnings({"unchecked"})
    public static final <E extends SkriptEvent> SkriptEventInfo<E> registerEvent(final String name, final Class<E> c, final Class<? extends Event> event, final String... patterns) {
        checkAcceptRegistrations();
        final SkriptEventInfo<E> r = new SkriptEventInfo<>(name, patterns, c, CollectionUtils.array(event));
        events.add(r);
        return r;
    }

    /**
     * Registers an event.
     *
     * @param name     The name of the event, used for error messages
     * @param c        The event's class
     * @param events   The Bukkit events this event applies to
     * @param patterns Skript patterns to match this event
     * @return A SkriptEventInfo representing the registered event. Used to generate Skript's documentation.
     */
    public static final <E extends SkriptEvent> SkriptEventInfo<E> registerEvent(final String name, final Class<E> c, final Class<? extends Event>[] events, final String... patterns) {
        checkAcceptRegistrations();
        final SkriptEventInfo<E> r = new SkriptEventInfo<>(name, patterns, c, events);
        Skript.events.add(r);
        return r;
    }

    public static final Collection<SkriptEventInfo<?>> getEvents() {
        return events;
    }

    // ================ COMMANDS ================

    /**
     * Dispatches a command with calling command events
     *
     * @param sender The sender of the command.
     * @param command The command to run.
     * @return Whether the command was run
     */
    public static final boolean dispatchCommand(final CommandSender sender, final String command) {
        try {
            if (sender instanceof Player) {
                final PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent((Player) sender, "/" + command);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled() || e.getMessage() == null || !e.getMessage().startsWith("/"))
                    return false;
                return Bukkit.dispatchCommand(e.getPlayer(), e.getMessage().substring(1));
            } else {
                final ServerCommandEvent e = new ServerCommandEvent(sender, command);
                Bukkit.getPluginManager().callEvent(e);
                if (e.getCommand() == null || e.getCommand().isEmpty())
                    return false;
                return Bukkit.dispatchCommand(e.getSender(), e.getCommand());
            }
        } catch (final Throwable tw) {
            Skript.exception(tw, "Error occurred when executing command " + command);
            return false;
        }
    }

    // ================ LOGGING ================

    public static final boolean logNormal() {
        return logHigh() || SkriptLogger.log(Verbosity.NORMAL);
    }

    public static final boolean logHigh() {
        return logVeryHigh() || SkriptLogger.log(Verbosity.HIGH);
    }

    public static final boolean logVeryHigh() {
        return debug() || SkriptLogger.log(Verbosity.VERY_HIGH);
    }

    private static final boolean debugProperty = System.getProperty("skript.debug") != null
            && Boolean.parseBoolean(System.getProperty("skript.debug"));

    public static final boolean debug() {
        return debugProperty || SkriptLogger.debug();
    }

    public static final boolean testing() {
        return debug() || Skript.class.desiredAssertionStatus();
    }

    public static final boolean log(final Verbosity minVerb) {
        return SkriptLogger.log(minVerb);
    }

    public static final void debug(final String info) {
        if (!debug())
            return;
        SkriptLogger.log(SkriptLogger.DEBUG, info);
    }

    /**
     * @see SkriptLogger#log(Level, String)
     */
    @SuppressWarnings("null")
    public static final void info(final String info) {
        SkriptLogger.log(Level.INFO, info);
    }

    /**
     * @see SkriptLogger#log(Level, String)
     */
    @SuppressWarnings("null")
    public static final void warning(final String warning) {
        SkriptLogger.log(Level.WARNING, warning);
    }

    /**
     * @see SkriptLogger#log(Level, String)
     */
    @SuppressWarnings("null")
    public static final void error(final @Nullable String error) {
        if (error != null)
            SkriptLogger.log(Level.SEVERE, error);
    }

    /**
     * Use this in {@link Expression#init(Expression[], int, ch.njol.util.Kleenean, ch.njol.skript.lang.SkriptParser.ParseResult)} (and other methods that are called during the
     * parsing) to log
     * errors with a specific {@link ErrorQuality}.
     *
     * @param error The error to log.
     * @param quality The {@link ErrorQuality}.
     */
    public static final void error(final String error, final ErrorQuality quality) {
        SkriptLogger.log(new LogEntry(SkriptLogger.SEVERE, quality, error));
    }

    /**
     * Used if something happens that shouldn't happen
     *
     * @param info Description of the error and additional information
     * @return an EmptyStacktraceException to throw if code execution should terminate.
     */
    public static final RuntimeException exception(final String... info) {
        return exception(null, info);
    }

    public static final RuntimeException exception(final @Nullable Throwable cause, final String... info) {
        return exception(cause, null, null, info);
    }

    public static final RuntimeException exception(final @Nullable Throwable cause, final @Nullable Thread thread, final String... info) {
        return exception(cause, thread, null, info);
    }

    public static final RuntimeException exception(final @Nullable Throwable cause, final @Nullable TriggerItem item, final String... info) {
        return exception(cause, null, item, info);
    }

    /**
     * Used if something happens that shouldn't happen
     *
     * @param cause exception that shouldn't occur
     * @param info  Description of the error and additional information
     * @return an EmptyStacktraceException to throw if code execution should terminate.
     */
    public static final RuntimeException exception(@Nullable Throwable cause, final @Nullable Thread thread, final @Nullable TriggerItem item, final @Nullable String... info) {
        try {
            logEx();
            logEx("[Skript] Severe Error:");
            if (info != null)
                logEx(info);
            logEx();
            logEx("Something went horribly wrong with Skript.");
            logEx("This issue is NOT your fault! You probably can't fix it yourself, either.");
            logEx();
            logEx("If you're a server admin please go to " + ISSUES_LINK);
            logEx("and check this issue has already been reported.");
            logEx();
            logEx("If not please create a new issue with a meaningful title, copy & paste this whole error into it,");
            logEx("and describe what you did before it happened and/or what you think caused the error.");
            logEx();
            logEx("If you think that it's a code that's causing the error please post the code as well.");
            logEx("By following this guide fixing the error should be easy and done fast.");
            logEx();
            // Print hint message only if the at least one condition in the hint message is met.
            if (Skript.testing() || Skript.logHigh() || Skript.hasAddons()) {
                logEx("Also removing the -ea java argument, lowering the verbosity or removing the problematic addons may help.");
                logEx();
            }
            logEx("Stack trace:");
            if (cause == null || cause.getStackTrace().length == 0) {
                logEx("  warning: no/empty exception given, dumping current stack trace instead");
                cause = new Throwable(cause);
            }
            boolean first = true;
            while (cause != null) {
                logEx((first ? "" : "Caused by: ") + cause.toString());
                for (final StackTraceElement e : cause.getStackTrace())
                    logEx("    at " + e.toString());
                cause = cause.getCause();
                first = false;
            }
            logEx();
            logEx("Version Information:");
            logEx("  Skript: " + getVersion() + (updateChecked ? updateAvailable ? " (update available)" : " (latest)" : " (not checked)"));
            logEx("  Bukkit: " + Bukkit.getBukkitVersion() + " (" + Bukkit.getVersion() + ")" + (hasJLineSupport() ? " (uses JLine)" : ""));
            logEx("  Minecraft: " + getMinecraftVersion());
            logEx("  Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + ")");
            logEx("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version") + ("64".equalsIgnoreCase(System.getProperty("sun.arch.data.model")) ? " (64-bit)" : " (32-bit)"));
            logEx();
            logEx("Server platform: " + getServerPlatform().platformName + (getServerPlatform().isSupported ? "" : " (unsupported)"));
            if (!getServerPlatform().isWorking) {
                logEx();
                logEx("Your server platform is not tested with Skript. Use at your own risk.");
            }
            logEx();
            final Node node = SkriptLogger.getNode();
            if (node != null)
                logEx("Current node: " + node + " (" + (node.getKey() != null ? node.getKey() : "") + ")");
            logEx("Current item: " + (item == null ? "not available" : item.toString(null, true)));
            if (item != null && item.getTrigger() != null) {
                final Trigger trigger = item.getTrigger();
                //noinspection ConstantConditions
                if (trigger != null) {
                    final File script = trigger.getScript();
                    logEx("Current trigger: " + trigger.toString(null, true) + " (" + (script == null ? "null" : script.getName()) + ", line " + trigger.getLineNumber() + ")");
                } else {
                    logEx("Current trigger: not available");
                }
            } else {
                logEx("Current trigger: no trigger");
            }
            logEx();
            logEx("Thread: " + (thread == null ? Thread.currentThread() : thread).getName());
            logEx();
            logEx("Language: " + Language.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + Language.getName().substring(1) + " (system: " + Workarounds.getOriginalProperty("user.language") + "-" + Workarounds.getOriginalProperty("user.country") + ")");
            logEx("Encoding: " + "file = " + Workarounds.getOriginalProperty("file.encoding") + " , jnu = " + Workarounds.getOriginalProperty("sun.jnu.encoding") + " , stderr = " + Workarounds.getOriginalProperty("sun.stderr.encoding") + " , stdout = " + Workarounds.getOriginalProperty("sun.stdout.encoding"));
            logEx();
            final StringBuilder stringBuilder = new StringBuilder();
            int i = 0;
            for (final SkriptAddon addon : addons.values()) {
                if (addon.plugin instanceof Skript)
                    continue;
                stringBuilder.append(addon.getName());
                if (i < addons.size() - 1)
                    stringBuilder.append(", ");
                i++;
            }
            final String addonList = stringBuilder.toString();
            if (!addonList.isEmpty())
                logEx("Skript Addons: " + addonList);
            logEx();
            logEx("End of Error.");
            logEx();
            return new EmptyStacktraceException();
        } catch (final Throwable tw) {
            // Unexpected horrible error when handling exception.
            tw.printStackTrace();
            if (cause != null)
                cause.printStackTrace();
            if (info != null) {
                for (final String str : info)
                    System.out.println(str);
            }
            // This a real error - don't return an empty error.
            return new RuntimeException();
        }
    }

    static final void logEx() {
        SkriptLogger.LOGGER.severe(EXCEPTION_PREFIX);
    }

    static final void logEx(final @Nullable String... lines) {
        if (lines == null || lines.length < 1)
            return;
        if (lines.length == 1)
            SkriptLogger.LOGGER.severe(EXCEPTION_PREFIX + lines[0]);
        else {
            for (final String line : lines)
                SkriptLogger.LOGGER.severe(EXCEPTION_PREFIX + line);
        }
    }

    public static final void info(final CommandSender sender, final String message) {
        sender.sendMessage((sender instanceof ConsoleCommandSender ? SKRIPT_PREFIX_CONSOLE : SKRIPT_PREFIX) + Utils.replaceEnglishChatStyles(message));
    }

    @SuppressWarnings("null")
	public static final void severe(final String message, final Throwable... errors) {
        error(message);
        if (errors != null)
            for (final Throwable tw : errors)
                exception(tw);
    }

    /**
     * @param message Message to broadcast.
     * @param permission Permission to see the message.
     *
     * @see #adminBroadcast(String)
     */
    public static final void broadcast(final String message, final String permission) {
        Bukkit.broadcast(SKRIPT_PREFIX + Utils.replaceEnglishChatStyles(message), permission);
    }

//	static {
//		Language.addListener(new LanguageChangeListener() {
//			@Override
//			public void onLanguageChange() {
//				final String s = Language.get_("skript.prefix");
//				if (s != null)
//					SKRIPT_PREFIX = Utils.replaceEnglishChatStyles(s) + ChatColor.RESET + " ";
//			}
//		});
//	}

    public static final void adminBroadcast(final String message) {
        Bukkit.broadcast(SKRIPT_PREFIX + Utils.replaceEnglishChatStyles(message), "skript.admin");
    }

    /**
     * Similar to {@link #info(CommandSender, String)} but no [Skript] prefix is added.
     *
     * @param sender Receiver of the message.
     * @param info Message to send the sender.
     */
    public static final void message(final CommandSender sender, final String info) {
        sender.sendMessage(Utils.replaceEnglishChatStyles(info));
    }

    public static final void error(final CommandSender sender, final String error) {
        sender.sendMessage((sender instanceof ConsoleCommandSender ? SKRIPT_PREFIX_CONSOLE : SKRIPT_PREFIX) + ChatColor.DARK_RED + Utils.replaceEnglishChatStyles(error));
    }

    @SuppressWarnings("null")
    public static final File getPluginFile() {
        return getInstance().getFile();
    }

    public static final ClassLoader getBukkitClassLoader() {
        return getBukkitClassLoader(getInstance());
    }

    @SuppressWarnings("null")
    public static final ClassLoader getBukkitClassLoader(final Skript instance) {
        return instance.getClassLoader();
    }

    @SuppressWarnings("null")
	@Override
    public void onEnable() {
        try {

            if (disabled) {
                Skript.error(m_invalid_reload.toString());
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            if (!first) {

                if (System.getProperty("-Dskript.disableAutomaticChanges") != null &&
                        Boolean.parseBoolean(System.getProperty("-Dskript.disableAutomaticChanges"))) {
                    return;
                }

                first = true;
                try {
                    // Get server directory / folder
                    final File serverDirectory = this.getDataFolder().getParentFile().getCanonicalFile().getParentFile().getCanonicalFile();

                    // Flag to track changes and warn the user
                    boolean madeChanges = false;

                    // Find and detect paper file and automatically disable velocity warnings
                    final File paperFile = new File(serverDirectory, "paper.yml");
                    if (paperFile.exists()) {
                        final Path filePath = paperFile.toPath();
                        final String contents = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8).trim();
                        // See: https://github.com/LifeMC/LifeSkript/issues/25
                        final String replacedContents = contents.replace("warnWhenSettingExcessiveVelocity: true", "warnWhenSettingExcessiveVelocity: false").trim();
                        if (!contents.equalsIgnoreCase(replacedContents)) {
                            Files.write(filePath, replacedContents.getBytes(StandardCharsets.UTF_8));
                            madeChanges = true;
                        }
                    }

                    // Find the startup script and automatically add log strip color option
                    final File[] startupScripts = serverDirectory.listFiles((dir, name) ->
                            name.toLowerCase(Locale.ENGLISH).trim()
                                    .endsWith(".bat".toLowerCase(Locale.ENGLISH).trim())
                                    || name.toLowerCase(Locale.ENGLISH).trim()
                                    .endsWith(".batch".toLowerCase(Locale.ENGLISH).trim())
                                    || name.toLowerCase(Locale.ENGLISH).trim()
                                    .endsWith(".cmd".toLowerCase(Locale.ENGLISH).trim())
                                    || name.toLowerCase(Locale.ENGLISH).trim()
                                    .endsWith(".sh".toLowerCase(Locale.ENGLISH).trim())
                                    || name.toLowerCase(Locale.ENGLISH).trim()
                                    .endsWith(".bash".toLowerCase(Locale.ENGLISH).trim())
                    );

                    if (startupScripts != null) {
                        for (final File startupScript : startupScripts) {
                            final Path filePath = startupScript.toPath();
                            final String contents = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8).trim();
                            if (contents.contains("java") && contents.contains("-jar ")) {
                                String afterJar = contents.substring(contents.lastIndexOf("-jar ") + 1).trim();
                                if (afterJar.contains(System.lineSeparator()))
                                    afterJar = afterJar.split(System.lineSeparator())[0];
                                String stripColor = afterJar;
                                // Strip color is required for not showing strange characters on log when using jansi colors
                                if (!afterJar.contains("--log-strip-color")) {
                                    stripColor += " --log-strip-color";
                                }
                                String replacedContents = contents.replace(afterJar, stripColor);
                                if (!contents.equalsIgnoreCase(replacedContents)) {
                                    String beforeJar = replacedContents.substring(0, replacedContents.indexOf("-jar")).trim();
                                    if (beforeJar.contains(System.lineSeparator()))
                                        beforeJar = beforeJar.split(System.lineSeparator())[0];
                                    String fileEncoding = beforeJar;
                                    // File encoding is required on some locales to fix some issues with localization
                                    if (!beforeJar.toLowerCase(Locale.ENGLISH).contains("-Dfile.encoding=UTF-8".toLowerCase(Locale.ENGLISH)))
                                        fileEncoding += " -Dfile.encoding=UTF-8";
                                    replacedContents = replacedContents.replace(beforeJar, fileEncoding);
                                    if (!contents.equalsIgnoreCase(replacedContents)) {
                                        Files.write(filePath, replacedContents.getBytes(StandardCharsets.UTF_8));
                                        madeChanges = true;
                                    }
                                }
                            }
                        }
                    }

                    // Warn the user that server needs a restart
                    if (madeChanges)
                        info("Automatically made some compatibility settings. Restart your server to apply them.");
                } catch (final Throwable tw) {
                    //Skript.exception(tw);
                }
            }

            //System.setOut(new FilterPrintStream(System.out));

            Language.loadDefault(getAddonInstance());

            Workarounds.init();

            version = new Version(getDescription().getVersion());
            //runningCraftBukkit = craftbukkitMain != null; //NOSONAR
            final String bukkitV = Bukkit.getBukkitVersion();
            final Matcher m = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?").matcher(bukkitV);
            if (!m.find()) {
                Skript.error("The Bukkit version '" + bukkitV + "' does not contain a version number which is required for Skript to enable or disable certain features. " + "Skript will still work, but you might get random errors if you use features that are not available in your version of Bukkit.");
                minecraftVersion = new Version(666, 0, 0);
            } else {
                minecraftVersion = new Version(m.group());
            }

            if (!getDataFolder().isDirectory())
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdirs();

            final File scripts = new File(getDataFolder(), SCRIPTSFOLDER);
            if (!scripts.isDirectory() || !Files.exists(Paths.get(getDataFolder() + File.separator + "aliases-english.sk")) || !Files.exists(Paths.get(getDataFolder() + File.separator + "aliases-german.sk"))) {
                ZipFile f = null;
                try {
                    if (!scripts.exists() && !scripts.mkdirs())
                        throw new IOException("Could not create the directory " + scripts);
                    f = new ZipFile(getFile());
                    for (final ZipEntry e : new EnumerationIterable<ZipEntry>(f.entries())) {
                        if (e.isDirectory())
                            continue;
                        File saveTo = null;
                        if (e.getName().startsWith(SCRIPTSFOLDER + "/")) {
                            final String fileName = e.getName().substring(e.getName().lastIndexOf('/') + 1);
                            final File file = new File(scripts, (fileName.startsWith("-") ? "" : "-") + fileName);
                            if (!file.exists())
                                saveTo = file;
                        } else if ("config.sk".equals(e.getName())) {
                            final File cf = new File(getDataFolder(), e.getName());
                            if (!cf.exists())
                                saveTo = cf;
                        } else if (e.getName().startsWith("aliases-") && e.getName().endsWith(".sk") && !e.getName().contains("/")) {
                            final File af = new File(getDataFolder(), e.getName());
                            if (!af.exists())
                                saveTo = af;
                        }
                        if (saveTo != null) {
                            try (InputStream in = f.getInputStream(e)) {
                                assert in != null;
                                FileUtils.save(in, saveTo);
                            }
                        }
                    }
                    info("Successfully generated the config, the example scripts and the aliases files.");
                } catch (final ZipException e) {
                    if (Skript.logVeryHigh())
                        Skript.exception(e);
                } catch (final IOException e) {
                    error("Error generating the default files: " + ExceptionUtils.toString(e));
                } finally {
                    if (f != null) {
                        try {
                            f.close();
                        } catch (final IOException ignored) {
                            /* ignored */
                        }
                    }
                }
            }

            getCommand("skript").setExecutor(new SkriptCommand());

            new JavaClasses(); //NOSONAR
            new BukkitClasses(); //NOSONAR
            new BukkitEventValues(); //NOSONAR
            new SkriptClasses(); //NOSONAR

            new DefaultComparators(); //NOSONAR
            new DefaultConverters(); //NOSONAR
            new DefaultFunctions(); //NOSONAR

            SkriptConfig.load();

            try {
                getAddonInstance().loadClasses("ch.njol.skript", "conditions", "effects", "events", "expressions", "entity");
                if (logHigh()) {
                    if (getAddonInstance().getUnloadableClassCount() > 0) {
                        if (Skript.logVeryHigh()) {
                            info("Total of " + getAddonInstance().getUnloadableClassCount() + " classes are excluded from Skript. This maybe because of your version. Try enabling debug logging for more info.");
                        } else if (!Skript.isRunningMinecraft(1, 7, 10) && !Skript.isRunningMinecraft(1, 8, 8)) {
                            info("Total of " + getAddonInstance().getUnloadableClassCount() + " classes are excluded from Skript. This maybe because of your version. Try enabling debug logging for more info.");
                        }
                    } else {
                        info("Total of " + getAddonInstance().getLoadedClassCount() + " classes loaded from Skript.");
                    }
                }
            } catch (final Throwable tw) {
                exception(tw, "Could not load required .class files: " + tw.getLocalizedMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            if (Bukkit.getOnlineMode() && !SkriptConfig.usePlayerUUIDsInVariableNames.value()) {
                warning("Your server is running with online mode enabled, we recommend you to make \"use player UUIDs in variable names\" setting true in config file.");
            }

            Language.setUseLocal(true);
            Aliases.load();
            Commands.registerListeners();

            // Always print copyright, can't be suppressed by lowering verbosity C:
            info(Language.get("skript.copyright"));

            @SuppressWarnings("unused")
			final long tick = testing() ? Bukkit.getWorlds().get(0).getFullTime() : 0;
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                @SuppressWarnings("synthetic-access")
                public void run() {
                    assert Bukkit.getWorlds().get(0).getFullTime() == tick;

                    // load hooks
                    try {
                        try (JarFile jar = new JarFile(getPluginFile())) {
                            for (final JarEntry e : new EnumerationIterable<>(jar.entries())) {
                                if (e.getName().startsWith("ch/njol/skript/hooks/") && e.getName().endsWith("Hook.class") && StringUtils.count(e.getName(), '/') <= 5) {
                                    final String c = e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length());
                                    try {
                                        final Class<?> hook = Class.forName(c, true, getBukkitClassLoader());
                                        if (hook != null && Hook.class.isAssignableFrom(hook) && !hook.isInterface() && Hook.class != hook) {
                                            hook.getDeclaredConstructor().setAccessible(true);
                                            hook.getDeclaredConstructor().newInstance();
                                        }
                                    } catch (final NoClassDefFoundError ncdffe) {
                                        Skript.exception(ncdffe, "Cannot load class " + c + " because it missing some dependencies");
                                    } catch (final ClassNotFoundException ex) {
                                        Skript.exception(ex, "Cannot load class " + c);
                                    } catch (final ExceptionInInitializerError err) {
                                        Skript.exception(err.getCause(), "Class " + c + " generated an exception while loading");
                                    } catch (final Throwable tw) {
                                        if (Skript.testing() || Skript.debug())
                                            Skript.exception(tw, "Cannot load class " + c);
                                    }
                                }
                            }
                        }
                    } catch (final Throwable tw) {
                        error("Error while loading plugin hooks" + (tw.getLocalizedMessage() == null ? "" : ": " + tw.getLocalizedMessage()));
                        if (testing())
                            tw.printStackTrace();
                    }

                    Language.setUseLocal(false);

                    stopAcceptingRegistrations();

                    if (!SkriptConfig.disableDocumentationGeneration.value())
                        Documentation.generate();

                    if (logNormal())
                        info("Loading variables...");

                    final long vls = System.currentTimeMillis();

                    final LogHandler h = SkriptLogger.startLogHandler(new ErrorDescLogHandler() {
//						private final List<LogEntry> log = new ArrayList<LogEntry>();

                        @Override
                        public LogResult log(final LogEntry entry) {
                            super.log(entry);
                            if (entry.level.intValue() >= Level.SEVERE.intValue()) {
                                logEx(entry.message); // no [Skript] prefix
                                return LogResult.DO_NOT_LOG;
                            } else {
//								log.add(entry);
//								return LogResult.CACHED;
                                return LogResult.LOG;
                            }
                        }

                        @Override
                        protected void beforeErrors() {
                            logEx();
                            logEx("===!!!=== Skript variable load error ===!!!===");
                            logEx("Unable to load (all) variables:");
                        }

                        @Override
                        protected void afterErrors() {
                            logEx();
                            logEx("Skript will work properly, but old variables might not be available at all and new ones may or may not be saved until Skript is able to create a backup of the old file and/or is able to connect to the database (which requires a restart of Skript)!");
                            logEx();
                        }

                        @Override
                        protected void onStop() {
                            super.onStop();
//							SkriptLogger.logAll(log);
                        }
                    });

                    final int oldPriority = Thread.currentThread().getPriority();
                    try {
                        Thread.currentThread().checkAccess();
                        // Set the thread priority for speeding up loading of variables and scripts
                        final int priority = Thread.MAX_PRIORITY;
                        Thread.currentThread().setPriority(priority);
                        if (Skript.debug())
                            Skript.debug("Set thread priority to " + priority);
                    } catch (final SecurityException ignored) {
                        /* ignored */
                    }

                    final CountingLogHandler c = SkriptLogger.startLogHandler(new CountingLogHandler(SkriptLogger.SEVERE));
                    try {
                        if (!Variables.load())
                            if (c.getCount() == 0)
                                error("(no information available)");
                    } finally {
                        c.stop();
                        h.stop();
                    }

                    final long vld = System.currentTimeMillis() - vls;
                    if (logNormal())
                        info("Loaded " + Variables.numVariables() + " variables in " + vld / 100 / 10. + " seconds");

                    ScriptLoader.loadScripts();

                    Skript.info(m_finished_loading.toString());

                    EvtSkript.onSkriptStart();

                    // suppresses the "can't keep up" warning after loading all scripts
                    final Filter f = record -> record != null && record.getMessage() != null && !record.getMessage().toLowerCase(Locale.ENGLISH).startsWith("can't keep up!".toLowerCase(Locale.ENGLISH));
                    BukkitLoggerFilter.addFilter(f);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.this, () -> BukkitLoggerFilter.removeFilter(f), 1);

                    if (Skript.logVeryHigh()) {
                        Bukkit.getScheduler().runTask(instance, () -> info("Skript enabled successfully with " + events.size() + " events, " + expressions.size() + " expressions, " + conditions.size() + " conditions, " + effects.size() + " effects, " + statements.size() + " statements, " + Functions.javaFunctions.size() + " java functions and " + (Functions.functions.size() - Functions.javaFunctions.size()) + " skript functions."));
                    }

                    // Restore the old priority if we changed the priority.
                    if (Thread.currentThread().getPriority() != oldPriority) {
                        Thread.currentThread().setPriority(oldPriority);
                        if (Skript.debug())
                            Skript.debug("Reset thread priority to " + oldPriority);
                    }

                    // No need to add debug code everytime to test the exception handler (:
                    if (System.getProperty("skript.throwTestError") != null
                            && Boolean.parseBoolean(System.getProperty("skript.throwTestError"))) {
                        Skript.exception(new Throwable(), "Test error");
                    }
                }
            });

            if (Skript.testing() && Skript.logHigh() || Skript.logVeryHigh()) {
                Bukkit.getScheduler().runTask(this, () -> {
                    info(Color.getWoolData ? "Using new method for color data." : "Using old method for color data.");
                    info(ExprEntities.getNearbyEntities ? "Using new method for entities expression." : "Using old method for entities expression.");
                });
            }

            if (!isEnabled())
                return;

            // Check server platform and version, ensure everything works fine.

            Bukkit.getScheduler().runTask(this, () -> {
                if (minecraftVersion.compareTo(1, 7, 10) == 0) { // If running on Minecraft 1.7.10
                    if (getServerPlatform() != ServerPlatform.LIFE_SPIGOT && !ExprEntities.getNearbyEntities) { // If not using LifeSpigot and not supports getNearbyEntities
                        warning("You are running on 1.7.10 and not using LifeSpigot, Some features will not be available. Switch to LifeSpigot or upgrade to newer versions. Report this if it is a bug.");
                    }
                } else if (minecraftVersion.compareTo(1, 8, 8) == 0) {
                    if (getServerPlatform() != ServerPlatform.BUKKIT_TACO) {
                        if (getServerPlatform() == ServerPlatform.BUKKIT_UNKNOWN || getServerPlatform() == ServerPlatform.BUKKIT_CRAFTBUKKIT) {
                            // Make user first switch to Spigot, and give warnings (not infos as on taco) because there is no "official" Bukkit or CraftBukkit in 1.8 already.
                            // The Bukkit and CraftBukkit that spigot provides after Minecraft 1.7.10 is unofficial, and it only exists for historical & development related reasons.
                            // So, people should not use Bukkit or CraftBukkit on versions newer than 1.7.10. On 1.7.10, people can use Bukkit because Spigot has protocal patch and breaking changes.
                            warning("We recommend using either Spigot or TacoSpigot with Minecraft 1.8.8, there is no official Bukkit for Minecraft 1.8.8 already. It is compatible with Bukkit plugins.");
                            warning("You can get Spigot 1.8.8 from: https://cdn.getbukkit.org/spigot/spigot-1.8.8-R0.1-SNAPSHOT-latest.jar");
                        } else if (getServerPlatform() != ServerPlatform.BUKKIT_PAPER) {
                            // Just give infos: Only a recommendation. New features and fixes are great, but maybe cause more errors or bugs, and thus maybe unstable.
                            // TODO Make this also appear on normal verbosity after making sure TacoSpigot does NOT break some plugins that work on Spigot.
                            if (Skript.logHigh()) {
                                info("We recommend using TacoSpigot over Spigot on 1.8.8 - because it has fixes, timings v2 and other bunch of stuff. It is compatible with Spigot plugins.");
                                info("You can get TacoSpigot 1.8.8 from: https://ci.techcable.net/job/TacoSpigot-1.8.8/lastSuccessfulBuild/artifact/build/TacoSpigot.jar");
                            }
                        } else {
                            // PaperSpigot on 1.8.8 is just same as TacoSpigot 1.8.8, TacoSpigot only adds extras and more performance.
                            warning("We recommend using TacoSpigot instead of PaperSpigot in 1.8.8 - because it has more fixes, performance and other bunch of stuff. It is compatible with Paper and Spigot plugins.");
                            warning("You can get TacoSpigot 1.8.8 from: https://ci.techcable.net/job/TacoSpigot-1.8.8/lastSuccessfulBuild/artifact/build/TacoSpigot.jar");
                        }
                    }
                }
            });

            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public final void onJoin(final PlayerJoinEvent e) {
                    if (e.getPlayer().hasPermission("skript.seeupdates") || e.getPlayer().hasPermission("skript.admin") || e.getPlayer().hasPermission("skript.*") || e.getPlayer().isOp()) {
                        new Task(Skript.this, 0) {
                            @Override
                            public final void run() {
                                final Player p = e.getPlayer();
                                assert p != null;
                                if (updateAvailable) {
                                    info(p, m_update_available.toString());
                                    info(p, getDownloadLink());
                                }
                            }
                        };
                    }
                }
            }, this);

            latestVersion = this.getDescription().getVersion();

            if (!isEnabled())
                return;

            if (SkriptConfig.checkForNewVersion.value()) {
                final Thread t = newThread(() -> {
                    try {
                        final String current = this.getDescription().getVersion();

                        if (current == null || current.length() < 1)
                            return;

                        final String latest = !debug() ? getLatestVersion() : getLatestVersion(Skript::exception);

                        if (!isEnabled())
                            return;
                        Bukkit.getScheduler().runTask(this, () -> Bukkit.getLogger().info(""));

                        if (latest == null) {
                            Bukkit.getScheduler().runTask(this, () -> warning("Can't check for updates, probably you don't have internet connection, or the web server is down?"));
                            return;
                        }

                        final String latestTrimmed = latest.trim().toLowerCase(Locale.ENGLISH).replaceAll("\\s+", "").trim();
                        final String currentTrimmed = current.trim().toLowerCase(Locale.ENGLISH).replaceAll("\\s+", "").trim();

                        if (!latestTrimmed.equals(currentTrimmed)) {
                            if (!isEnabled())
                                return;
                            Bukkit.getScheduler().runTask(this, () -> warning("A new version of Skript has been found. Skript " + latest + " has been released. It is highly recommended to upgrade latest version. (you are using Skript v" + current + ")"));
                            Bukkit.getScheduler().runTask(this, Skript::printDownloadLink);
                            updateAvailable = true;
                            latestVersion = latest;
                        } else {
                            if (!isEnabled())
                                return;
                            Bukkit.getScheduler().runTask(this, () -> info("You are using the latest version of Skript."));
                            Bukkit.getScheduler().runTask(this, Skript::printIssuesLink);
                        }
                    } catch (final Throwable tw) {
                        if (!isEnabled())
                            return;
                        Bukkit.getScheduler().runTask(this, () -> error("Unable to check updates, make sure you are using the latest version of Skript! (" + tw.getClass().getName() + ": " + tw.getLocalizedMessage() + ")"));
                        if (Skript.logHigh()) {
                            if (!isEnabled())
                                return;
                            Bukkit.getScheduler().runTask(this, () -> severe(SKRIPT_PREFIX_CONSOLE + "Unable to check updates", tw));
                        }
                        Bukkit.getScheduler().runTask(this, Skript::printDownloadLink);
                    }
                    updateChecked = true;
                    Thread.currentThread().interrupt();
                }, "Skript update checker thread");

                // Not slow down the server, it's just an updater!
                // and maybe updater can also be rewrited with proper OOP and Java usage, with checking updates from github
                // instead, just in case the web site is down.
                t.setPriority(Thread.MIN_PRIORITY);
                t.setDaemon(true);
                t.start();
            }

        } catch (final Throwable tw) {
            exception(tw, Thread.currentThread(), (TriggerItem) null, "An error occured when enabling Skript");
        }

    }

    @Override
    public void onDisable() {
        if (disabled)
            return;
        disabled = true;

        if (Skript.logHigh())
            info("Triggering on server stop events - if server freezes here, consider removing such events from skript code.");
        EvtSkript.onSkriptStop();

        if (Skript.logHigh())
            info("Disabling scripts..");
        disableScripts();

        if (Skript.logHigh())
            info("Unregistering tasks and event listeners..");
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll((JavaPlugin) this);

        if (Skript.logHigh())
            info("Freeing up the memory - if server freezes here, open a bug report issue at the github repository.");
        for (final Closeable c : closeOnDisable) {
            try {
                c.close();
            } catch (final Throwable tw) {
                Skript.exception(tw, "An error occurred while shutting down.", "This might or might not cause any issues.");
            }
        }
        if (Skript.logHigh())
            info("Freed up memory - starting cleaning up of fields. If server freezes here, open a bug report issue at the github repository.");

        // unset static fields to prevent memory leaks as Bukkit reloads the classes with a different classloader on reload
        // async to not slow down server reload, delayed to not slow down server shutdown
        final Skript instance = this;
        // we are saving instance because in lambda we can't access it
        // and using getInstance method causes IllegalStateException since
        // we are un setting fields, we also unset the instance field.
        final Thread t = newThread(() -> {
            try {
                Thread.sleep(10000L);
            } catch (final InterruptedException e) {
                return;
            }
            try {
                final Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                try (JarFile jar = new JarFile(getPluginFile())) {
                    for (final JarEntry e : new EnumerationIterable<>(jar.entries())) {
                        if (e.getName().endsWith(".class")) {
                            try {
                                final Class<?> c = Class.forName(e.getName().replace('/', '.').substring(0, e.getName().length() - ".class".length()), false, getBukkitClassLoader(instance));
                                for (final Field f : c.getDeclaredFields()) {
                                    if (Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
                                        if (Modifier.isFinal(f.getModifiers())) {
                                            modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                                        }
                                        f.setAccessible(true);
                                        f.set(null, null);
                                    }
                                }
                            } catch (final Throwable tw) {
                                // If Vault or WorldGuard is not available
                                if (tw instanceof NoClassDefFoundError)
                                    continue;
                                // If we can't set fields (e.g if it's final
                                // and we are on Java 9 or above )
                                if (tw instanceof IllegalAccessException)
                                    continue;
                                // Happens when classes trying to register
                                // expressions etc. when disabling.
                                if (tw instanceof SkriptAPIException)
                                    continue;
                                // Only log in debug otherwise, an interesting
                                // error occured because we already skip general errors
                                if (testing() || debug())
                                    tw.printStackTrace();
                            }
                        }
                    }
                }
            } catch (final Throwable tw) {
                if (testing() || logHigh())
                    tw.printStackTrace();
            }
            Thread.currentThread().interrupt();
        }, "Skript cleanup thread");
        t.setPriority(Thread.MIN_PRIORITY);
        t.setDaemon(true);
        t.start();
    }

}
