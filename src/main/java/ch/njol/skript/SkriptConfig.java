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

import ch.njol.skript.config.*;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.util.FileUtils;
import org.bukkit.event.EventPriority;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unused")
public final class SkriptConfig {

    public static final Option<String> language = new Option<>("language", "english").optional(true).setter(s -> {
        if (!Language.load(s)) {
            Skript.error("No language file found for '" + s + "'!");
        }
    });
    public static final Option<Boolean> enableEffectCommands = new Option<>("enable effect commands", false);
    public static final Option<String> effectCommandToken = new Option<>("effect command token", "!");
    public static final Option<Boolean> allowOpsToUseEffectCommands = new Option<>("allow ops to use effect commands", true);
    // everything handled by Variables
    public static final OptionSection databases = new OptionSection("databases");
    public static final Option<Boolean> usePlayerUUIDsInVariableNames = new Option<>("use player UUIDs in variable names", false);
    public static final Option<Boolean> enablePlayerVariableFix = new Option<>("player variable fix", true);
    public static final Option<EventPriority> defaultEventPriority = new Option<>("plugin priority", EventPriority.NORMAL, s -> {
        try {
            return EventPriority.valueOf(s.toUpperCase(Locale.ENGLISH));
        } catch (final IllegalArgumentException e) {
            Skript.error("The plugin priority has to be one of lowest, low, normal, high, or highest.");
            return EventPriority.NORMAL;
        }
    });
    public static final Option<Boolean> logPlayerCommands = new Option<>("log player commands", true);
    /**
     * Maximum number of digits to display after the period for floats and doubles
     */
    public static final Option<Integer> numberAccuracy = new Option<>("number accuracy", 2);
    public static final Option<Integer> maxTargetBlockDistance = new Option<>("maximum target block distance", 100);
    public static final Option<Boolean> caseSensitive = new Option<>("case sensitive", false);
    public final static Option<Boolean> allowFunctionsBeforeDefs = new Option<>("allow function calls before definitions", true);
    public static final Option<Boolean> disableDocumentationGeneration = new Option<>("disable documentation generation", false);
    public static final Option<Boolean> disableVariableConflictWarnings = new Option<>("disable variable conflict warnings", true);
    public static final Option<Boolean> disableObjectCannotBeSavedWarnings = new Option<>("disable variable will not be saved warnings", true);
    public static final Option<Boolean> disableExpressionAlreadyTextWarnings = new Option<>("disable expression is already a text warnings", false);
    public static final Option<Boolean> disableStartingWithExpressionWarnings = new Option<>("disable variable name starting with expression warnings", false);
    public static final Option<Boolean> disableStartStopEventWarnings = new Option<>("disable start stop event warnings", false);
    public static final Option<Boolean> disableTooLongDelayWarnings = new Option<>("disable too long delay warnings", false);
    public static final Option<Boolean> disableDelaysInFunctionsWarnings = new Option<>("disable delays in functions causes function to return instantly warnings", false);
    public static final Option<Boolean> enableScriptCaching = new Option<>("enable script caching", false).optional(true);
    public static final Option<Boolean> keepConfigsLoaded = new Option<>("keep configs loaded", false).optional(true);
    public static final Option<Boolean> addonSafetyChecks = new Option<>("addon safety checks", true)
            .optional(true);
    static final Collection<Config> configs = new ArrayList<>();
    static final Option<String> version = new Option<>("version", Skript.getVersion().toString()).optional(true);

    //static final Option<Boolean> checkForNewVersion = new Option<>("check for new version", false);
    //static final Option<Timespan> updateCheckInterval = new Option<>("update check interval", new Timespan(0)).setter(t -> {
    //final Task ct = Updater.checkerTask;
    //if (t.getTicks_i() != 0 && ct != null && !ct.isAlive())
    //ct.setNextExecution(t.getTicks_i());
    //});
    //static final Option<Boolean> automaticallyDownloadNewVersion = new Option<>("automatically download new version", false);
    @SuppressWarnings("null")
    private static final DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private static final Option<DateFormat> dateFormat = new Option<>("date format", shortDateFormat, s -> {
        try {
            if ("default".equalsIgnoreCase(s))
                return null;
            return new SimpleDateFormat(s);
        } catch (final IllegalArgumentException e) {
            Skript.error("'" + s + "' is not a valid date format. Please refer to https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html for instructions on the format.");
        }
        return null;
    });
    private static final Option<Verbosity> verbosity = new Option<>("verbosity", Verbosity.NORMAL, new EnumParser<>(Verbosity.class, "verbosity")).setter(SkriptLogger::setVerbosity);
    @Nullable
    static Config mainConfig;

    private SkriptConfig() {
        throw new UnsupportedOperationException();
    }

    public static String formatDate(final long timestamp) {
        final DateFormat format = dateFormat.value();
        assert format != null;

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (format) {
            return "" + format.format(timestamp);
        }
    }

    /**
     * This should only be used in special cases
     */
    @Nullable
    public static Config getConfig() {
        return mainConfig;
    }

    // also used for reloading
    static boolean load() {
        try {
            final File oldConfigFile = new File(Skript.getInstance().getDataFolder(), "config.cfg");
            final File configFile = new File(Skript.getInstance().getDataFolder(), "config.sk");
            if (oldConfigFile.exists()) {
                if (!configFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    oldConfigFile.renameTo(configFile);
                    Skript.info("[1.3] Renamed your 'config.cfg' to 'config.sk' to match the new format");
                } else {
                    Skript.error("Found both a new and an old config, ignoring the old one");
                }
            }
            if (!configFile.exists()) {
                Skript.error("Config file 'config.sk' does not exist!");
                return false;
            }
            if (!configFile.canRead()) {
                Skript.error("Config file 'config.sk' cannot be read!");
                return false;
            }

            Config mc;
            try {
                mc = new Config(configFile, false, false, ":");
            } catch (final IOException e) {
                Skript.error("Could not load the main config: " + e.getLocalizedMessage());
                return false;
            }
            mainConfig = mc;

            if (!Skript.getVersion().toString().equals(mc.get(version.key))) {
                try {
                    final InputStream in = Skript.getInstance().getResource("config.sk");
                    if (in == null) {
                        Skript.error("Your config is outdated, but Skript couldn't find the newest config in its jar. Please download Skript again from the link below:");
                        Skript.printDownloadLink();
                        return false;
                    }
                    final Config newConfig = new Config(in, "Skript.jar/config.sk", false, false, ":");
                    in.close();

                    boolean forceUpdate = false;

                    if (mc.getMainNode().get("database") != null) { // old database layout
                        forceUpdate = true;
                        try {
                            final SectionNode oldDB = (SectionNode) mc.getMainNode().get("database");
                            assert oldDB != null;
                            final SectionNode newDBs = (SectionNode) newConfig.getMainNode().get(databases.key);
                            assert newDBs != null;
                            final SectionNode newDB = (SectionNode) newDBs.get("database 1");
                            assert newDB != null;

                            newDB.setValues(oldDB);

                            // '.db' was dynamically added before
                            final String file = newDB.getValue("file");
                            assert file != null;
                            if (!file.endsWith(".db"))
                                newDB.set("file", file + ".db");

                            final SectionNode def = (SectionNode) newDBs.get("default");
                            assert def != null;
                            def.set("backup interval", "" + mc.get("variables backup interval"));
                        } catch (final Exception e) {
                            Skript.error("An error occurred while trying to update the config's database section.");
                            Skript.error("You'll have to update the config yourself:");
                            Skript.error("Open the new config.sk as well as the created backup, and move the 'database' section from the backup to the start of the 'databases' section");
                            Skript.error("of the new config (i.e. the line 'databases:' should be directly above 'database:'), and add a tab in front of every line that you just copied.");
                            return false;
                        }
                    }

                    if (newConfig.setValues(mc, version.key, databases.key) || forceUpdate) { // new config is different
                        final File bu = FileUtils.backup(configFile);
                        newConfig.getMainNode().set(version.key, Skript.getVersion().toString());
                        if (mc.getMainNode().get(databases.key) != null)
                            newConfig.getMainNode().set(databases.key, mc.getMainNode().get(databases.key));
                        mc = mainConfig = newConfig;
                        mc.save(configFile);
                        Skript.info("Your configuration has been updated to the latest version. A backup of your old config file has been created as " + bu.getName());
                    } else { // only the version changed
                        mc.getMainNode().set(version.key, Skript.getVersion().toString());
                        mc.save(configFile);
                    }
                } catch (final IOException e) {
                    Skript.error("Could not load the new config from the jar file: " + e.getLocalizedMessage());
                }
            }

            mc.load(SkriptConfig.class);

//			if (!keepConfigsLoaded.value())
//				mainConfig = null;
        } catch (final Throwable tw) {
            Skript.exception(tw, "An error occurred while loading the config");
            return false;
        }
        return true;
    }

}
