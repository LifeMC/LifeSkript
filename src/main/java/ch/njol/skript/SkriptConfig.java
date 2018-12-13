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

package ch.njol.skript;

import ch.njol.skript.classes.Converter;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EnumParser;
import ch.njol.skript.config.Option;
import ch.njol.skript.config.OptionSection;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Task;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Setter;

import org.bukkit.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unused")
public final class SkriptConfig {
	
	private SkriptConfig() {
		throw new UnsupportedOperationException();
	}
	
	@Nullable
	static Config mainConfig;
	static Collection<Config> configs = new ArrayList<Config>();
	
	final static Option<String> version = new Option<String>("version", Skript.getVersion().toString()).optional(true);
	
	public final static Option<String> language = new Option<String>("language", "english").optional(true).setter(new Setter<String>() {
		@Override
		public void set(final String s) {
			if (!Language.load(s)) {
				Skript.error("No language file found for '" + s + "'!");
			}
		}
	});
	
	final static Option<Boolean> checkForNewVersion = new Option<Boolean>("check for new version", false);
	final static Option<Timespan> updateCheckInterval = new Option<Timespan>("update check interval", new Timespan(0)).setter(new Setter<Timespan>() {
		@SuppressWarnings("null")
		@Override
		public void set(final Timespan t) {
			final Task ct = Updater.checkerTask;
			if (t.getTicks_i() != 0 && ct != null && !ct.isAlive())
				ct.setNextExecution(t.getTicks_i());
		}
	});
	final static Option<Boolean> automaticallyDownloadNewVersion = new Option<Boolean>("automatically download new version", false);
	
	public final static Option<Boolean> enableEffectCommands = new Option<Boolean>("enable effect commands", false);
	public final static Option<String> effectCommandToken = new Option<String>("effect command token", "!");
	public final static Option<Boolean> allowOpsToUseEffectCommands = new Option<Boolean>("allow ops to use effect commands", true);
	
	// everything handled by Variables
	public final static OptionSection databases = new OptionSection("databases");
	
	public final static Option<Boolean> usePlayerUUIDsInVariableNames = new Option<Boolean>("use player UUIDs in variable names", false);
	public final static Option<Boolean> enablePlayerVariableFix = new Option<Boolean>("player variable fix", true);
	
	@SuppressWarnings("null")
	private final static DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	private final static Option<DateFormat> dateFormat = new Option<DateFormat>("date format", shortDateFormat, new Converter<String, DateFormat>() {
		@Override
		@Nullable
		public DateFormat convert(final String s) {
			try {
				if ("default".equalsIgnoreCase(s))
					return null;
				return new SimpleDateFormat(s);
			} catch (final IllegalArgumentException e) {
				Skript.error("'" + s + "' is not a valid date format. Please refer to http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html for instructions on the format.");
			}
			return null;
		}
	});
	
	public static String formatDate(final long timestamp) {
		final DateFormat f = dateFormat.value();
		synchronized (f) {
			return "" + f.format(timestamp);
		}
	}
	
	private final static Option<Verbosity> verbosity = new Option<Verbosity>("verbosity", Verbosity.NORMAL, new EnumParser<Verbosity>(Verbosity.class, "verbosity")).setter(new Setter<Verbosity>() {
		@Override
		public void set(final Verbosity v) {
			SkriptLogger.setVerbosity(v);
		}
	});
	
	public final static Option<EventPriority> defaultEventPriority = new Option<EventPriority>("plugin priority", EventPriority.NORMAL, new Converter<String, EventPriority>() {
		@Override
		@Nullable
		public EventPriority convert(final String s) {
			try {
				return EventPriority.valueOf(s.toUpperCase(Locale.ENGLISH));
			} catch (final IllegalArgumentException e) {
				Skript.error("The plugin priority has to be one of lowest, low, normal, high, or highest.");
				return EventPriority.NORMAL;
			}
		}
	});
	
	public final static Option<Boolean> logPlayerCommands = new Option<Boolean>("log player commands", true);
	
	/**
	 * Maximum number of digits to display after the period for floats and doubles
	 */
	public final static Option<Integer> numberAccuracy = new Option<Integer>("number accuracy", 2);
	
	public final static Option<Integer> maxTargetBlockDistance = new Option<Integer>("maximum target block distance", 100);
	
	public final static Option<Boolean> caseSensitive = new Option<Boolean>("case sensitive", false);
	public final static Option<Boolean> disableDocumentationGeneration = new Option<Boolean>("disable documentation generation", false);
	
	// Disable warnings options
	
	public final static Option<Boolean> disableVariableConflictWarnings = new Option<Boolean>("disable variable conflict warnings", true);
	public final static Option<Boolean> disableObjectCannotBeSavedWarnings = new Option<Boolean>("disable variable will not be saved warnings", true);
	
	public final static Option<Boolean> disableExpressionAlreadyTextWarnings = new Option<Boolean>("disable expression is already a text warnings", false);
	
	public final static Option<Boolean> disableStartingWithExpressionWarnings = new Option<Boolean>("disable variable name starting with expression warnings", false);
	
	public final static Option<Boolean> disableStartStopEventWarnings = new Option<Boolean>("disable start stop event warnings", false);
	
	public final static Option<Boolean> disableTooLongDelayWarnings = new Option<Boolean>("disable too long delay warnings", false);
	
	public final static Option<Boolean> disableDelaysInFunctionsWarnings = new Option<Boolean>("disable delays in functions causes function to return instantly warnings", false);
	
	// Disable warnings options
	
	public final static Option<Boolean> enableScriptCaching = new Option<Boolean>("enable script caching", false).optional(true);
	
	public final static Option<Boolean> keepConfigsLoaded = new Option<Boolean>("keep configs loaded", false).optional(true);
	
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
		} catch (final RuntimeException e) {
			Skript.exception(e, "An error occurred while loading the config");
			return false;
		}
		return true;
	}
	
}
