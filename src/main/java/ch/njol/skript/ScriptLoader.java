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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.config.*;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.*;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.update.script.ScriptUpdater;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.*;
import ch.njol.skript.variables.TypeHints;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link ScriptLoader} that loads scripts
 * from file system as specified in the configuration.
 *
 * @author Peter Güttinger
 * @see SkriptParser
 */
public final class ScriptLoader {
    public static final boolean COLOR_BASED_ON_LOAD_TIMES = PropertyManager.getBoolean("skript.colorBasedOnLoadTimes");

    public static final List<TriggerSection> currentSections = new ArrayList<>();

    public static final List<Loop> currentLoops = new ArrayList<>();
    /**
     * Filter for enabled scripts & folders.
     */
    public static final FileFilter scriptFilter = f -> f != null && (f.isDirectory() && SkriptConfig.allowScriptsFromSubFolders.value() || StringUtils.endsWithIgnoreCase(f.getName().trim(), ".sk".trim()) && !StringUtils.startsWithIgnoreCase(f.getName().trim(), "-".trim()));
    static final HashMap<String, String> currentOptions = new HashMap<>();
    /**
     * All loaded script files.
     */
    @SuppressWarnings("null")
    static final Set<File> loadedFiles = Collections.synchronizedSet(new HashSet<>(100));
    static final Collection<String> loadedScriptFiles = new ArrayList<>(100);
    private static final Message m_no_errors = new Message("skript.no errors"),
            m_no_scripts = new Message("skript.no scripts");
    private static final PluralizingArgsMessage m_scripts_loaded = new PluralizingArgsMessage("skript.scripts loaded");
    private static final Map<String, ItemType> currentAliases = new HashMap<>();
    private static final Map<String, Version> sourceRevisionMap = new HashMap<>(100);
    private static final Map<String, ScriptInfo> skipFiles = new HashMap<>();
    /**
     * must be synchronized
     */
    private static final ScriptInfo loadedScripts = new ScriptInfo();
    private static final Matcher FUNC_PATTERN_MATCHER = Pattern.compile("func").matcher("");
    private static final Matcher FUN_PATTERN_MATCHER = Pattern.compile("fun").matcher("");
    private static final Matcher OR_PATTERN_MATCHER = Pattern.compile("\" or \"", Pattern.LITERAL).matcher("");
    private static final String commaSpace = Matcher.quoteReplacement("\", \"");
    private static final Pattern PROJECT_VERSION_PATTERN = Pattern.compile("${project.version}", Pattern.LITERAL);
    @Nullable
    public static Config currentScript;
    public static Kleenean hasDelayBefore = Kleenean.FALSE;
    @Nullable
    private static
    Version currentScriptVersion;
    @Nullable
    private static
    Version cachedDefaultScriptVersion;
    // We don't use static initializer because Skript may not be initialized in that time,
    // when this occurs, Skript#getVersion can throw errors, so we lazy init the variable.
    public static final Supplier<Version> defaultScriptVersion = () -> {
        if (cachedDefaultScriptVersion == null) {
            final String defaultSourceVersion = SkriptConfig.defaultSourceVersion.value();

            switch (defaultSourceVersion) {
                case "current":
                    return cachedDefaultScriptVersion = Skript.getVersion();
                case "latest":
                    try {
                        if (Skript.latestVersion != null)
                            return cachedDefaultScriptVersion = new Version(Skript.latestVersion);
                    } catch (final IllegalArgumentException ignored) {
                        // Fall back to current version
                    }
                    return cachedDefaultScriptVersion = Skript.getVersion();
                case "default":
                    return cachedDefaultScriptVersion = getSourceVersionFrom(Skript.getVersion());
                default:
                    return cachedDefaultScriptVersion = new Version(defaultSourceVersion);
            }
        }
        return cachedDefaultScriptVersion;
    };
    /**
     * use {@link #setCurrentEvent(String, Class...)}
     */
    @Nullable
    private static String currentEventName;
    /**
     * use {@link #setCurrentEvent(String, Class...)}
     */
    @Nullable
    private static Class<? extends Event>[] currentEvents;
    private static String indentation = "";

    private static volatile boolean loadingScripts;

    @Nullable
    private static Thread loadingLoggerThread;

    @Nullable
    private static Date loadStart;

    private ScriptLoader() {
        throw new UnsupportedOperationException();
    }

    public static final void startTracker() {
        loadingScripts = true;
        loadStart = new Date();

        // reports once per second how many scripts were loaded. Useful to make clear that Skript is still doing something if it's loading many scripts
        final Thread loggerThread = Skript.newThread(() -> {
            while (loadingScripts) {
                try {
                    Thread.sleep(Skript.logVeryHigh() ? 3000L : Skript.logHigh() ? 5000L : Skript.logNormal() ? 8000L : 10000L); // low verbosity won't disable these messages, but makes them more rare
                } catch (final InterruptedException e) {
                    if (loadingScripts)
                        Skript.exception(e);
                    Thread.currentThread().interrupt();

                    return;
                }
                synchronized (loadedFiles) {
                    if (loadingScripts)
                        Skript.info("Loaded " + loadedFiles.size() + " scripts" + " so far...");
                }
            }
            Thread.currentThread().interrupt();
        }, "Skript load tracker thread");

        loadingLoggerThread = loggerThread;

        loggerThread.setPriority(Thread.MIN_PRIORITY);
        loggerThread.setDaemon(true);

        // actually starts the tracker
        loggerThread.start();
    }

    public static final void endTracker() {
        final Date start = loadStart;
        final Thread loggerThread = loadingLoggerThread;

        if (loggerThread == null || !loadingScripts || start == null)
            return;
        loadingScripts = false;
        loggerThread.interrupt(); // In case if not interrupted

        loadingLoggerThread = null;
    }

    public static final boolean isErrorAllowed(final Version versionAdded) {
        return isErrorAllowed(versionAdded, ScriptLoader.getCurrentScriptVersion());
    }

    public static final boolean isErrorAllowed(final Version versionAdded, final Version scriptVersion) {
        return isWarningAllowed(versionAdded, scriptVersion, true);
    }

    public static final boolean isWarningAllowed(final Version versionAdded) {
        return isWarningAllowed(versionAdded, ScriptLoader.getCurrentScriptVersion(), false);
    }

    public static final boolean isWarningAllowed(final Version versionAdded, final Version scriptVersion) {
        return isWarningAllowed(versionAdded, scriptVersion, false);
    }

    public static final boolean isWarningAllowed(final Version versionAdded, final Version scriptVersion, final boolean strict) {
        if (scriptVersion.isSmallerThan(versionAdded) || strict && getSourceVersionFrom(scriptVersion).isSmallerThan(getSourceVersionFrom(versionAdded)))
            return false;

        return getSourceVersionFrom(scriptVersion).isLargerThan(getSourceVersionFrom(versionAdded));
    }

    public static final Version getSourceVersionFrom(final Version normalVersion) {
        final int sourceRevision = (int) Math.floor(normalVersion.getRevision() / 10D);

        final int major = normalVersion.getMajor();
        final int minor = normalVersion.getMinor();

        //noinspection UnnecessaryBoxing,UnnecessaryCallToStringValueOf
        final String sourceRevisionStr = Integer.toString(major) + Integer.toString(minor) + Integer.toString(sourceRevision);
        Version sourceVersion = sourceRevisionMap.get(sourceRevisionStr);

        if (sourceVersion == null) {
            sourceRevisionMap.put(sourceRevisionStr, sourceVersion = new Version(normalVersion.getMajor(), normalVersion.getMinor(), sourceRevision));
        }

        return sourceVersion;
    }

    @Nullable
    public static final String getCurrentEventName() {
        return currentEventName;
    }

    /**
     * Call {@link #deleteCurrentEvent()} after parsing
     *
     * @param name   The name of the current event.
     * @param events The current events.
     */
    @SafeVarargs
    public static final void setCurrentEvent(final String name, @Nullable final Class<? extends Event>... events) {
        currentEventName = name;
        currentEvents = events;
        hasDelayBefore = Kleenean.FALSE;
    }

//	private static final class SerializedScript {
//		public SerializedScript() {}
//
//		public final List<Trigger> triggers = new ArrayList<Trigger>();
//		public final List<ScriptCommand> commands = new ArrayList<ScriptCommand>();
//	}

    public static final void deleteCurrentEvent() {
        currentEventName = null;
        currentEvents = null;
        hasDelayBefore = Kleenean.FALSE;
        TypeHints.clear(); // Local variables are local to event
    }

    public static final Map<String, ItemType> getScriptAliases() {
        return currentAliases;
    }

    public static final File getScriptsFolder() {
        return new File(Skript.getInstance().getDataFolder(), Skript.SCRIPTSFOLDER + File.separator);
    }

    static final ScriptInfo loadScripts() {
        startTracker();

        final File scriptsFolder = getScriptsFolder();
        if (!scriptsFolder.isDirectory())
            //noinspection ResultOfMethodCallIgnored
            scriptsFolder.mkdirs();

        final Date start = new Date();

        final ScriptInfo i;

        final ErrorDescLogHandler h = SkriptLogger.startLogHandler(new ErrorDescLogHandler(null, null, m_no_errors.toString()));
        try {
            Language.setUseLocal(false);

            i = loadScripts(scriptsFolder);

            synchronized (loadedScripts) {
                loadedScripts.add(i);
            }
        } finally {
            Language.setUseLocal(true);
            h.stop();

            endTracker();
        }

        if (i.files == 0)
            Skript.warning(m_no_scripts.toString());
        if (Skript.logNormal() && i.files > 0)
            Skript.info(m_scripts_loaded.toString(i.files, i.triggers, i.commands, start.difference(new Date())));

        SkriptEventHandler.registerBukkitEvents();
        Functions.postCheck(); // Check that all functions which are called exist.
        return i;
    }

    @SuppressWarnings("null") // Collections methods don't return nulls, ever
    public static final Collection<File> getLoadedFiles() {
        return Collections.unmodifiableCollection(loadedFiles);
    }

    /**
     * Loads enabled scripts from the specified directory and it's subdirectories.
     *
     * @param directory The directory to load scripts from
     * @return Info on the loaded scripts
     */
    public static final ScriptInfo loadScripts(final File directory) {
        final ScriptInfo i = new ScriptInfo();
        final boolean wasLocal = Language.setUseLocal(false);
        try {
            final File[] files = directory.listFiles(scriptFilter);
            assert files != null;
            Arrays.sort(files);
            for (final File f : files) {
                if (skipFiles.containsKey(f.getName())) {
                    i.add(skipFiles.get(f.getName()));

                    continue;
                }
                if (f.isDirectory()) {
                    i.add(loadScripts(f));
                } else {
                    i.add(loadScript(f));
                }
            }
        } finally {
            if (wasLocal)
                Language.setUseLocal(true);
        }
        Functions.postCheck(); // Check that all functions which are called exist.
        skipFiles.clear();
        return i;
    }

    /**
     * Loads the specified scripts.
     *
     * @param files The script files to load
     * @return Info on the loaded scripts
     */
    public static final ScriptInfo loadScripts(final File[] files) {
        Arrays.sort(files);
        final ScriptInfo i = new ScriptInfo();
        final boolean wasLocal = Language.setUseLocal(false);
        try {
            for (final File f : files) {
                assert f != null : Arrays.toString(files);
                i.add(loadScript(f));
            }
        } finally {
            if (wasLocal)
                Language.setUseLocal(true);
        }

        synchronized (loadedScripts) {
            loadedScripts.add(i);
        }

        SkriptEventHandler.registerBukkitEvents();
        Functions.postCheck(); // Check that all functions which are called exist.
        return i;
    }

    /**
     * This is an internal method, if you want to load scripts,
     * use either {@link ScriptLoader#loadScripts(File[])} or {@link ScriptLoader#loadScripts(File)}.<br /><br />
     * <p>
     * If you need to load a single script file, use {@link ScriptLoader#loadScripts(File[])} with a single
     * element array that contains your file.<br /><br />
     * <p>
     * For example, use it like this<br />
     * {@code
     * ScriptLoader.loadScripts(new File[] {new File("hello world.sk")})
     * }
     *
     * @see ScriptLoader#loadScripts(File[])
     * @see ScriptLoader#loadScripts(File)
     */
    @SuppressWarnings("null")
    private static final ScriptInfo loadScript(final File f) {
        assert f != null;

        // FIXME Fix assertion errors when using /sk reload

        //assert !loadedFiles.contains(f);
        //assert !loadedScriptFiles.contains(f.getName());

        assert currentScript == null : "Current script should be null for script \"" + f.getName() + "\" (" + currentScript + ')';

//		File cache = null;
//		if (SkriptConfig.enableScriptCaching.value()) {
//			cache = new File(f.getParentFile(), "cache" + File.separator + f.getName() + "c");
//			if (cache.exists()) {
//				final RetainingLogHandler log = SkriptLogger.startRetainingLog();
//				ObjectInputStream in = null;
//				try {
//					in = new ObjectInputStream(new FileInputStream(cache));
//					final long lastModified = in.readLong();
//					if (lastModified == f.lastModified()) {
//						final SerializedScript script = (SerializedScript) in.readObject();
//						triggersLoop: for (final Trigger t : script.triggers) {
//							if (t.getEvent() instanceof SelfRegisteringSkriptEvent) {
//								((SelfRegisteringSkriptEvent) t.getEvent()).register(t);
//								SkriptEventHandler.addSelfRegisteringTrigger(t);
//							} else {
//								for (final SkriptEventInfo<?> e : Skript.getEvents()) {
//									if (e.c == t.getEvent().getClass()) {
//										SkriptEventHandler.addTrigger(e.events, t);
//										continue triggersLoop;
//									}
//								}
//								throw new EmptyStackException();
//							}
//						}
//						for (final ScriptCommand c : script.commands) {
//							Commands.registerCommand(c);
//						}
//						log.printLog();
//						return new ScriptInfo(1, script.triggers.size(), script.commands.size());
//					} else {
//						cache.delete();
//					}
//				} catch (final Exception e) {
//					if (Skript.testing()) {
//						System.err.println("[debug] Error loading cached script '" + f.getName() + "':");
//						e.printStackTrace();
//					}
//					unloadScript(f);
//					if (in != null) {
//						try {
//							in.close();
//						} catch (final IOException e1) {}
//					}
//					cache.delete();
//				} finally {
//					log.stop();
//					if (in != null) {
//						try {
//							in.close();
//						} catch (final IOException e) {}
//					}
//				}
//			}
//		}

        @Nullable
        Date startDate = null;

        if (Skript.logHigh())
            startDate = new Date();

        try {

            final Config config;

            try (final FileInputStream is = new FileInputStream(f)) {
                config = new Config(is, f, true, false, ":");
            }

            if (SkriptConfig.keepConfigsLoaded.value()) {
                SkriptConfig.configs.remove(config);
                SkriptConfig.configs.add(config);
            }

            Version scriptVersion = defaultScriptVersion.get();

            currentAliases.clear();
            currentOptions.clear();

            currentScript = config;

//			final SerializedScript script = new SerializedScript();

            final CountingLogHandler numErrors = SkriptLogger.startLogHandler(new CountingLogHandler(SkriptLogger.SEVERE));

            int numFunctions = 0;
            int numCommands = 0;
            int numTriggers = 0;
            try {
                int index = 0;
                boolean hasConfiguraton = false;
                for (final Node cnode : config.getMainNode()) {
                    index++;

                    if (!(cnode instanceof SectionNode)) {
                        Skript.error("invalid line - all code has to be put into triggers");
                        continue;
                    }

                    final SectionNode node = (SectionNode) cnode;
                    String event = node.getKey();
                    if (event == null)
                        continue;

                    if (ScriptConfig.isConfig(event)) {
                        if (hasConfiguraton) {
                            Skript.error("duplicate configuration section");
                            continue;
                        }
                        if (index != 1) {
                            Skript.error("configuration should be on top of the script");
                            continue;
                        }
                        hasConfiguraton = true;
                        node.convertToEntries(0);
                        ScriptUpdater.Parser.clearValues();
                        final List<String> duplicateCheckList = new ArrayList<>();
                        for (final Node n : node) {
                            if (!(n instanceof EntryNode)) {
                                Skript.error("invalid line in the configuration");
                                continue;
                            }
                            final String key = n.getKey();
                            final String value = PROJECT_VERSION_PATTERN.matcher(Node.splitLine(((EntryNode) n).getValue()).getFirst()).replaceAll(Matcher.quoteReplacement(Skript.getVersion().toString()));

                            try {
                                final AtomicReference<Version> _scriptVersion = new AtomicReference<>(scriptVersion);
                                final AtomicReference<Version> _currentScriptVersion = new AtomicReference<>(currentScriptVersion);

                                final ScriptConfig.ConfigParseResult configParseResult = ScriptConfig.tryParse(f, duplicateCheckList, key, value,
                                        _scriptVersion, _currentScriptVersion);

                                scriptVersion = _scriptVersion.get();
                                currentScriptVersion = _currentScriptVersion.get();

                                switch (configParseResult) {
                                    case OK:
                                        break;
                                    case ABORT_PARSING:
                                        currentScript = null;
                                        return new ScriptInfo(); // we return empty script info to abort parsing
                                    case CONTINUE:
                                        break;
                                    default: // Special handling or such
                                        break;
                                }

                                if ("load after".equalsIgnoreCase(key)) { // This also can be duplicate-able to require more than one script
                                    // This can be used to require a script (not generally), or defer loading of this script after a specific script is loaded.
                                    // it also can be used for functions, etc., when not using 'allow function calls before definitions'
                                    final File file = new File(getScriptsFolder(), value.endsWith(".sk") ? value : value + ".sk"); // .sk suffix can be omitted

                                    if (!file.exists()) {
                                        // This generally should not be used to require a script because user may change names of the scripts
                                        // so we are not using something like "This script requires script ..." as the message
                                        Skript.error("Can't find required script " + value);

                                        currentScript = null;
                                        return new ScriptInfo(); // we return empty script info to abort parsing
                                    }

                                    if (file.getPath().equals(f.getPath())) {
                                        // The script tries to load itself after itself, which it should be permitted
                                        Skript.error("Loading the current script after current script is not possible");

                                        currentScript = null;
                                        return new ScriptInfo(); // we return empty script info to abort parsing
                                    }

                                    if (!loadedScriptFiles.contains(file.getName())) { // If the script is not already loaded
                                        if (Skript.logHigh())
                                            Skript.info("Loading script '" + file.getName() + "' because the script '" + f.getName() + "' requires it");

                                        // Set to null, method call re-sets it
                                        currentScript = null;

                                        // Backup the aliases and the options
                                        final Map<String, ItemType> aliases = new HashMap<>(currentAliases);
                                        final Map<String, String> options = new HashMap<>(currentOptions);

                                        final ScriptInfo scriptInfo = loadScript(file); // Load the required script before continuing to parse this script

                                        skipFiles.remove(file.getName()); // Remove to re-add it
                                        skipFiles.put(file.getName(), scriptInfo); // Required to skip this script in iteration

                                        currentScript = config; // Re-set the current script to this script

                                        // Re-set the aliases and the options
                                        currentAliases.clear();
                                        currentAliases.putAll(aliases);

                                        currentOptions.clear();
                                        currentOptions.putAll(options);
                                    }
                                }
                            } catch (final IllegalArgumentException e) {
                                // Probably an illegal version string is passed
                                Skript.error(e.getLocalizedMessage());

                                currentScript = null;
                                return new ScriptInfo(); // we return empty script info to abort parsing
                            }
                        }
                        ScriptUpdater.Parser.clearValues();
                        duplicateCheckList.clear();
                        continue;
                    }
                    if ("aliases".equalsIgnoreCase(event)) {
                        node.convertToEntries(0, "=");
                        for (final Node n : node) {
                            if (!(n instanceof EntryNode)) {
                                Skript.error("invalid line in aliases section");
                                continue;
                            }
                            final ItemType t = Aliases.parseAlias(((EntryNode) n).getValue());
                            if (t == null)
                                continue;
                            currentAliases.put(n.getKey().toLowerCase(Locale.ENGLISH), t);
                        }
                        continue;
                    }
                    if ("options".equalsIgnoreCase(event)) {
                        node.convertToEntries(0);
                        for (final Node n : node) {
                            if (!(n instanceof EntryNode)) {
                                Skript.error("invalid line in options");
                                continue;
                            }
                            currentOptions.put(n.getKey(), ((EntryNode) n).getValue());
                        }
                        continue;
                    }
                    if ("variables".equalsIgnoreCase(event)) {
                        node.convertToEntries(0, "=");
                        for (final Node n : node) {
                            if (!(n instanceof EntryNode)) {
                                Skript.error("Invalid line in variables section");
                                continue;
                            }
                            @SuppressWarnings("null")
                            String name = n.getKey().toLowerCase(Locale.ENGLISH);
                            if (name.startsWith("{") && name.endsWith("}"))
                                name = name.substring(1, name.length() - 1);
                            final String var = name;
                            name = StringUtils.replaceAll(name, "%(.+)?%", m -> {
                                if (m.group(1).contains("{") || m.group(1).contains("}") || m.group(1).contains("%")) {
                                    Skript.error('\'' + var + "' is not a valid name for a default variable");
                                    return null;
                                }
                                final ClassInfo<?> ci = Classes.getClassInfoFromUserInput(m.group(1));
                                if (ci == null) {
                                    Skript.error("Can't understand the type '" + m.group(1) + '\'');
                                    return null;
                                }
                                return '<' + ci.getCodeName() + '>';
                            });
                            if (name == null) {
                                continue;
                            }
                            if (name.contains("%")) {
                                Skript.error("Invalid use of percent signs in variable name");
                                continue;
                            }
                            // Variables feature is for setting default variables,
                            // Not a shortcut for setting variables at startup.
                            // So if variable already exists, do nothing and continue.
                            if (Variables.getVariable(name, null, false) != null)
                                continue;
                            Object o;
                            final ParseLogHandler log = SkriptLogger.startParseLogHandler();
                            try {
                                o = Classes.parseSimple(((EntryNode) n).getValue(), Object.class, ParseContext.SCRIPT);
                                if (o == null) {
                                    log.printError("Can't understand the value '" + ((EntryNode) n).getValue() + '\'');
                                    continue;
                                }
                                log.printLog();
                            } finally {
                                log.stop();
                            }
                            final ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
                            if (ci == null || ci.getSerializer() == null) {
                                Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
                                continue;
                            }
                            if (ci.getSerializeAs() != null) {
                                final ClassInfo<?> as = Classes.getExactClassInfo(ci.getSerializeAs());
                                if (as == null) {
                                    assert false : ci;
                                    continue;
                                }
                                o = Converters.convert(o, as.getC());
                                if (o == null) {
                                    Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
                                    continue;
                                }
                            }
                            Variables.setVariable(name, o, null, false);
                        }
                        continue;
                    }
                    currentScriptVersion = scriptVersion;

                    if (!SkriptParser.validateLine(event))
                        continue;

                    if (event.toLowerCase(Locale.ENGLISH).startsWith("command ")) {

                        setCurrentEvent("command", CommandEvent.class);

                        final ScriptCommand c = Commands.loadCommand(node);
                        if (c != null) {
                            numCommands++;
//							script.commands.add(c);
                        }

                        deleteCurrentEvent();

                        continue;
                    }
                    if (event.toLowerCase(Locale.ENGLISH).startsWith("function ") || event.startsWith("func ") || event.startsWith("fun ")) { // Allow kotlin and javascript style function prefixes

                        setCurrentEvent("function", FunctionEvent.class);

                        // Allows to define functions with kotlin style:
                        // fun myFunction():
                        //     broadcast "Yey!"
                        if (!event.toLowerCase(Locale.ENGLISH).startsWith("function ")) {
                            if (event.startsWith("func ")) {
                                node.setKey(FUNC_PATTERN_MATCHER.reset(event).replaceFirst("function"));
                            } else {
                                node.setKey(FUN_PATTERN_MATCHER.reset(event).replaceFirst("function"));
                            }
                        }

                        final Function<?> func = Functions.loadFunction(node);
                        if (func != null) {
                            numFunctions++;
                        }

                        deleteCurrentEvent();

                        continue;
                    }

                    if (Skript.logVeryHigh() && !Skript.debug())
                        Skript.info("loading trigger '" + event + '\'');

                    if (StringUtils.startsWithIgnoreCase(event, "on "))
                        event = event.substring("on ".length());

                    event = replaceOptions(event);

                    final NonNullPair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(event, "can't understand this event: '" + node.getKey() + '\'');
                    if (parsedEvent == null)
                        continue;

                    if (Skript.debug() || node.debug())
                        Skript.debug(event + " (" + parsedEvent.getSecond().toString(null, true) + "):");

                    setCurrentEvent(parsedEvent.getFirst().getName().toLowerCase(Locale.ENGLISH), parsedEvent.getFirst().events);
                    final Trigger trigger;
                    try {
                        {
                            trigger = new Trigger(config.getFile(), event, parsedEvent.getSecond(), loadItems(node));
                        }
                        trigger.setLineNumber(node.getLine());
                        trigger.setDebugLabel(config.getFileName() + ": line " + node.getLine());
                    } finally {
                        deleteCurrentEvent();
                    }

                    if (parsedEvent.getSecond() instanceof SelfRegisteringSkriptEvent) {
                        ((SelfRegisteringSkriptEvent) parsedEvent.getSecond()).register(trigger);
                        SkriptEventHandler.addSelfRegisteringTrigger(trigger);
                    } else {
                        SkriptEventHandler.addTrigger(parsedEvent.getFirst().events, trigger);
                    }

//					script.triggers.add(trigger);

                    numTriggers++;
                }

                if (Skript.logHigh() && startDate != null) {
                    String prefix = "";
                    String suffix = "";

                    final Timespan difference = startDate.difference(new Date());
                    final long differenceInSeconds = TimeUnit.MILLISECONDS.toSeconds(difference.getMilliSeconds());

                    if (Skript.hasJLineSupport() && Skript.hasJansi() && COLOR_BASED_ON_LOAD_TIMES) {
                        if (differenceInSeconds > 5L) // Script take longer than 5 seconds to load
                            prefix += Ansi.ansi().a(Ansi.Attribute.RESET).reset().fg(Ansi.Color.RED).bold().toString();
                        else if (differenceInSeconds > 3L) // Script take longer than 3 seconds to load
                            prefix += Ansi.ansi().a(Ansi.Attribute.RESET).reset().fg(Ansi.Color.YELLOW).bold().toString();
                        suffix += Ansi.ansi().a(Ansi.Attribute.RESET).reset().toString();
                    }

                    Skript.info(prefix + "Loaded " + numTriggers + " trigger" + (numTriggers == 1 ? "" : "s") + ", " + numCommands + " command" + (numCommands == 1 ? "" : "s") + " and " + numFunctions + " function" + (numFunctions == 1 ? "" : "s") + " from '" + config.getFileName() + "' " + (Skript.logVeryHigh() ? "with source version " + scriptVersion + ' ' : "") + "in " + difference + suffix);
                }

                currentScript = null;

            } finally {
                numErrors.stop();
            }

//			if (SkriptConfig.enableScriptCaching.value() && cache != null) {
//				if (numErrors.getCount() > 0) {
//					ObjectOutputStream out = null;
//					try {
//						cache.getParentFile().mkdirs();
//						out = new ObjectOutputStream(new FileOutputStream(cache));
//						out.writeLong(f.lastModified());
//						out.writeObject(script);
//					} catch (final NotSerializableException e) {
//						Skript.exception(e, "Cannot cache " + f.getName());
//						if (out != null)
//							out.close();
//						cache.delete();
//					} catch (final IOException e) {
//						Skript.warning("Cannot cache " + f.getName() + ": " + e.getLocalizedMessage());
//						if (out != null)
//							out.close();
//						cache.delete();
//					} finally {
//						if (out != null)
//							out.close();
//					}
//				}
//			}

            loadedFiles.remove(f);
            loadedFiles.add(f);

            loadedScriptFiles.remove(f.getName());
            loadedScriptFiles.add(f.getName());

            return new ScriptInfo(1, numTriggers, numCommands, numFunctions, scriptVersion);
        } catch (final IOException e) {
            Skript.error("Could not load " + f.getName() + ": " + ExceptionUtils.toString(e));
        } catch (final Throwable tw) {
            Skript.exception(tw, "Could not load " + f.getName());
        } finally {
            SkriptLogger.setNode(null);
        }
        if (Skript.testing() || Skript.debug())
            Skript.warning("Returning empty script info after loading \"" + f.getName() + '"');
        currentScript = null;
        return new ScriptInfo();
    }

    /**
     * Unloads enabled scripts from the specified directory and its subdirectories.
     *
     * @param folder The folder to unload scripts from
     * @return Info on the unloaded scripts
     */
    static final ScriptInfo unloadScripts(final File folder) {
        final ScriptInfo r = unloadScripts_(folder);
        Functions.validateFunctions();
        return r;
    }

    private static final ScriptInfo unloadScripts_(final File folder) {
        final ScriptInfo info = new ScriptInfo();
        final File[] files = folder.listFiles(scriptFilter);
        assert files != null;
        for (final File f : files) {
            if (f.isDirectory()) {
                info.add(unloadScripts_(f));
            } else if (f.getName().endsWith(".sk")) {
                info.add(unloadScript_(f));
            }
        }
        return info;
    }

    /**
     * Unloads the specified script.
     *
     * @param script The script file to unload
     * @return Info on the unloaded script
     */
    static final ScriptInfo unloadScript(final File script) {
        final ScriptInfo r = unloadScript_(script);
        Functions.validateFunctions();
        return r;
    }

    private static final ScriptInfo unloadScript_(final File script) {
        final ScriptInfo info = SkriptEventHandler.removeTriggers(script);
        synchronized (loadedScripts) {
            loadedScripts.subtract(info);
        }
        loadedFiles.remove(script);
        loadedScriptFiles.remove(script.getName());
        return info;
    }

    /**
     * Replaces options in a string. May return null, but only if the input is null.
     *
     * @param s The string to replace options.
     * @return The replaced string. May return null, but only if the input is null.
     */
    public static final String replaceOptions(final String s) {
        final String r = StringUtils.replaceAll(s, "\\{@(.+?)\\}", m -> {
            final String option = currentOptions.get(m.group(1));
            if (option == null) {
                Skript.error("undefined option " + m.group());
                return m.group();
            }
            return Matcher.quoteReplacement(option);
        });
        assert r != null;
        return r;
    }

    @SuppressWarnings({"unchecked", "null"})
    public static final ArrayList<TriggerItem> loadItems(final SectionNode node) {
        if (Skript.debug())
            indentation += "    ";

        final ArrayList<TriggerItem> items = new ArrayList<>(100);

        Kleenean hadDelayBeforeLastIf = Kleenean.FALSE;

        for (final Node n : node) {
            SkriptLogger.setNode(n);
            if (n instanceof SimpleNode) {
                final SimpleNode e = (SimpleNode) n;
                @SuppressWarnings("null") final String s = optimizeAndOr(n, replaceOptions(e.getKey()));
                if (!SkriptParser.validateLine(s))
                    continue;
                final Statement stmt = Statement.parse(s, "Can't understand this condition/effect: " + s);
                if (stmt == null)
                    continue;
                if (Skript.debug() || n.debug())
                    Skript.debug(indentation + stmt.toString(null, true));
                items.add(stmt);
                if (stmt instanceof Delay)
                    hasDelayBefore = Kleenean.TRUE;
            } else if (n instanceof SectionNode) {
                @SuppressWarnings("null")
                String name = replaceOptions(n.getKey());
                if (!SkriptParser.validateLine(name))
                    continue;
                TypeHints.enterScope(); // Begin conditional type hints

                if (StringUtils.startsWithIgnoreCase(name, "loop ")) {
                    final String l = optimizeAndOr(n, name.substring("loop ".length()));
                    final RetainingLogHandler h = SkriptLogger.startRetainingLog();
                    Expression<?> loopedExpr;
                    try {
                        loopedExpr = new SkriptParser(l).parseExpression(new Class<?>[]{Object.class});
                        if (loopedExpr != null)
                            loopedExpr = loopedExpr.getConvertedExpression(Object.class);
                        if (loopedExpr == null) {
                            h.printErrors("Can't understand this loop: '" + name + '\'');
                            continue;
                        }
                        h.printLog();
                    } finally {
                        h.stop();
                    }
                    //noinspection ConstantConditions
                    assert loopedExpr != null;
                    if (loopedExpr.isSingle()) {
                        Skript.error("Can't loop " + loopedExpr + " because it's only a single value");
                        continue;
                    }
                    if (Skript.debug() || n.debug())
                        Skript.debug(indentation + "loop " + loopedExpr.toString(null, true) + ':');
                    final Kleenean hadDelayBefore = hasDelayBefore;
                    items.add(new Loop(loopedExpr, (SectionNode) n));
                    if (hadDelayBefore != Kleenean.TRUE && hasDelayBefore != Kleenean.FALSE)
                        hasDelayBefore = Kleenean.UNKNOWN;
                } else if (StringUtils.startsWithIgnoreCase(name, "while ")) {
                    final String l = optimizeAndOr(n, name.substring("while ".length()));
                    final Condition c = Condition.parse(l, "Can't understand this condition: " + l);
                    if (c == null)
                        continue;
                    if (Skript.debug() || n.debug())
                        Skript.debug(indentation + "while " + c.toString(null, true) + ':');
                    final Kleenean hadDelayBefore = hasDelayBefore;
                    items.add(new While(c, (SectionNode) n));
                    if (hadDelayBefore != Kleenean.TRUE && hasDelayBefore != Kleenean.FALSE)
                        hasDelayBefore = Kleenean.UNKNOWN;
                } else if ("else".equalsIgnoreCase(name)) {
                    if (items.isEmpty() || !(items.get(items.size() - 1) instanceof Conditional) || ((Conditional) items.get(items.size() - 1)).hasElseClause()) {
                        Skript.error("'else' has to be placed just after an 'if' or 'else if' section");
                        continue;
                    }
                    if (Skript.debug() || n.debug())
                        Skript.debug(indentation + "else:");
                    final Kleenean hadDelayAfterLastIf = hasDelayBefore;
                    hasDelayBefore = hadDelayBeforeLastIf;
                    ((Conditional) items.get(items.size() - 1)).loadElseClause((SectionNode) n);
                    hasDelayBefore = hadDelayBeforeLastIf.or(hadDelayAfterLastIf.and(hasDelayBefore));
                } else if (StringUtils.startsWithIgnoreCase(name, "else if ")) {
                    if (items.isEmpty() || !(items.get(items.size() - 1) instanceof Conditional) || ((Conditional) items.get(items.size() - 1)).hasElseClause()) {
                        Skript.error("'else if' has to be placed just after another 'if' or 'else if' section");
                        continue;
                    }
                    name = name.substring("else if ".length());
                    name = optimizeAndOr(n, name);
                    final Condition cond = Condition.parse(name, "can't understand this condition: '" + name + '\'');
                    if (cond == null)
                        continue;
                    if (Skript.debug() || n.debug())
                        Skript.debug(indentation + "else if " + cond.toString(null, true));
                    final Kleenean hadDelayAfterLastIf = hasDelayBefore;
                    hasDelayBefore = hadDelayBeforeLastIf;
                    ((Conditional) items.get(items.size() - 1)).loadElseIf(cond, (SectionNode) n);
                    hasDelayBefore = hadDelayBeforeLastIf.or(hadDelayAfterLastIf.and(hasDelayBefore.and(Kleenean.UNKNOWN)));
                } else {
                    if (StringUtils.startsWithIgnoreCase(name, "if "))
                        name = name.substring(3);
                    name = optimizeAndOr(n, name);
                    final Condition cond = Condition.parse(name, "can't understand this condition: '" + name + '\'');
                    if (cond == null)
                        continue;
                    if (Skript.debug() || n.debug())
                        Skript.debug(indentation + cond.toString(null, true) + ':');
                    final Kleenean hadDelayBefore = hasDelayBefore;
                    hadDelayBeforeLastIf = hadDelayBefore;
                    final Conditional conditional = new Conditional(cond);
                    items.add(conditional);
                    ScriptLoader.currentSections.add(conditional);
                    try {
                        conditional.setTriggerItems0(ScriptLoader.loadItems((SectionNode) n));
                    } finally {
                        ScriptLoader.currentSections.remove(ScriptLoader.currentSections.size() - 1);
                    }
                    hasDelayBefore = hadDelayBefore.or(hasDelayBefore.and(Kleenean.UNKNOWN));
                }

                // Destroy these conditional type hints
                TypeHints.exitScope();
            }
        }

        for (int i = 0; i < items.size() - 1; i++)
            items.get(i).setNext(items.get(i + 1));

        optimizeAndOr(node);
        SkriptLogger.setNode(node);

        if (Skript.debug())
            indentation = indentation.substring(0, indentation.length() - 4);

        return items;
    }

    public static final void optimizeAndOr(@Nullable final Node n) {
        optimizeAndOr(n, null);
    }

    @SuppressWarnings({"ConstantConditions", "null"})
    public static final String optimizeAndOr(@Nullable final Node n, @Nullable final String s) {
        if (n == null && s == null)
            throw new IllegalArgumentException();
        String optimized = s != null ? s : n != null ? n.getKey() : null;
        if (optimized != null && (!Skript.isConfigLoaded() || SkriptConfig.optimizeAndOrLists.value()) && !optimized.contains(", ") && optimized.contains(" or ")) {
            optimized = StringUtils.replaceLast(OR_PATTERN_MATCHER.reset(optimized).replaceAll(commaSpace),
                    "\", \"", "\" or \"");
        }
        if (n != null && optimized != null)
            n.setKey(optimized);
        return optimized;
    }

    /**
     * For unit testing
     *
     * @param node The node to load trigger from
     * @return The loaded Trigger
     */
    @SuppressWarnings("null")
    static final Trigger loadTrigger(final SectionNode node) {
        String event = node.getKey();
        if (event == null) {
            assert false : node;
            return null;
        }
        if (event.toLowerCase(Locale.ENGLISH).startsWith("on "))
            event = event.substring("on ".length());

        final NonNullPair<SkriptEventInfo<?>, SkriptEvent> parsedEvent = SkriptParser.parseEvent(event, "can't understand this event: '" + node.getKey() + '\'');
        if (parsedEvent == null) {
            assert false;
            return null;
        }

        assert parsedEvent.getFirst() != null;
        setCurrentEvent("unit test", parsedEvent.getFirst().events);
        try {
            return new Trigger(null, event, parsedEvent.getSecond(), loadItems(node));
        } finally {
            deleteCurrentEvent();
        }
    }

    public static final int loadedScripts() {
        synchronized (loadedScripts) {
            return loadedScripts.files;
        }
    }

    public static final int loadedCommands() {
        synchronized (loadedScripts) {
            return loadedScripts.commands;
        }
    }

    public static final int loadedFunctions() {
        synchronized (loadedScripts) {
            return loadedScripts.functions;
        }
    }

    public static final int loadedTriggers() {
        synchronized (loadedScripts) {
            return loadedScripts.triggers;
        }
    }

    public static final boolean isCurrentEvent(@Nullable final Class<? extends Event> event) {
        return CollectionUtils.containsSuperclass(currentEvents, event);
    }

    @SafeVarargs
    public static final boolean isCurrentEvent(final Class<? extends Event>... events) {
        return CollectionUtils.containsAnySuperclass(currentEvents, events);
    }

    /**
     * Use this sparingly; {@link #isCurrentEvent(Class)} or {@link #isCurrentEvent(Class...)} should be used in most cases.
     */
    @Nullable
    public static final Class<? extends Event>[] getCurrentEvents() {
        return currentEvents;
    }

    public static final Version getCurrentScriptVersion() {
        final Version localCurrentScriptVersion = currentScriptVersion;
        if (localCurrentScriptVersion != null)
            return localCurrentScriptVersion;
        return defaultScriptVersion.get();
    }

    public static final class ScriptInfo {
        /**
         * The Skript version that this script is written.
         * This not used currently; but maybe used in future.
         */
        @Nullable
        private final
        Version scriptVersion;
        public int files, triggers, commands, functions;

        public ScriptInfo() {
            this(0, 0, 0, 0);
        }

        public ScriptInfo(final int numFiles, final int numTriggers, final int numCommands, final int numFunctions) {
            this(numFiles, numTriggers, numCommands, numFunctions, null);
        }

        public ScriptInfo(final int numFiles, final int numTriggers, final int numCommands, final int numFunctions, @Nullable final Version scriptVersion) {
            files = numFiles;
            triggers = numTriggers;
            commands = numCommands;
            functions = numFunctions;
            this.scriptVersion = scriptVersion;
        }

        public void add(final ScriptInfo other) {
            files += other.files;
            triggers += other.triggers;
            commands += other.commands;
            functions += other.functions;
        }

        public void subtract(final ScriptInfo other) {
            files -= other.files;
            triggers -= other.triggers;
            commands -= other.commands;
            functions -= other.functions;
        }

        /**
         * Gets the source version of this script.
         *
         * @return The source version of this script.
         */
        public Version getScriptVersion() {
            @Nullable final Version localScriptVersion = scriptVersion;
            if (localScriptVersion == null)
                return defaultScriptVersion.get();
            return localScriptVersion;
        }
    }

}
