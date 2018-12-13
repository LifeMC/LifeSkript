package ch.njol.skript;

import ch.njol.skript.ScriptLoader.ScriptInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.command.CommandHelp;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.PluralizingArgsMessage;
import ch.njol.skript.log.RedirectingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.util.StringUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

/**
 * @author Peter Güttinger
 */
public final class SkriptCommand implements CommandExecutor {
	private final static String NODE = "skript command";
	
	// TODO /skript scripts show/list - lists all enabled and/or disabled scripts in the scripts folder and/or subfolders (maybe add a pattern [using * and **])
	// TODO document this command on the website
	private final static CommandHelp skriptCommandHelp = new CommandHelp("<gray>/<gold>skript", Color.LIGHT_CYAN, NODE + ".help").add(new CommandHelp("reload", Color.DARK_RED).add("all").add("config").add("aliases").add("scripts").add("<script>")).add(new CommandHelp("enable", Color.DARK_RED).add("all").add("<script>")).add(new CommandHelp("disable", Color.DARK_RED).add("all").add("<script>")).add(new CommandHelp("update", Color.DARK_RED).add("check").add("changes").add("download")
	//			).add(new CommandHelp("variable", "Commands for modifying variables", ChatColor.DARK_RED)
//					.add("set", "Creates a new variable or changes an existing one")
//					.add("delete", "Deletes a variable")
//					.add("find", "Find variables")
	).add("help");
	
	private final static ArgsMessage m_reloading = new ArgsMessage(NODE + ".reload.reloading");
	
	private static void reloading(final CommandSender sender, String what, final Object... args) {
		what = args.length == 0 ? Language.get(NODE + ".reload." + what) : Language.format(NODE + ".reload." + what, args);
		Skript.info(sender, StringUtils.fixCapitalization(m_reloading.toString(what)));
	}
	
	private final static ArgsMessage m_reloaded = new ArgsMessage(NODE + ".reload.reloaded");
	private final static ArgsMessage m_reload_error = new ArgsMessage(NODE + ".reload.error");
	
	@SuppressWarnings("unused")
	private final static ArgsMessage m_changes_title = new ArgsMessage(NODE + ".update.changes.title");
	
	private static void reloaded(final CommandSender sender, final RedirectingLogHandler r, String what, final Object... args) {
		what = args.length == 0 ? Language.get(NODE + ".reload." + what) : PluralizingArgsMessage.format(Language.format(NODE + ".reload." + what, args));
		if (r.numErrors() == 0)
			Skript.info(sender, StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reloaded.toString(what))));
		else
			Skript.error(sender, StringUtils.fixCapitalization(PluralizingArgsMessage.format(m_reload_error.toString(what, r.numErrors()))));
	}
	
	private static void info(final CommandSender sender, String what, final Object... args) {
		what = args.length == 0 ? Language.get(NODE + "." + what) : PluralizingArgsMessage.format(Language.format(NODE + "." + what, args));
		Skript.info(sender, StringUtils.fixCapitalization(what));
	}
	
	@SuppressWarnings("unused")
	private static void message(final CommandSender sender, String what, final Object... args) {
		what = args.length == 0 ? Language.get(NODE + "." + what) : PluralizingArgsMessage.format(Language.format(NODE + "." + what, args));
		Skript.message(sender, StringUtils.fixCapitalization(what));
	}
	
	private static void error(final CommandSender sender, String what, final Object... args) {
		what = args.length == 0 ? Language.get(NODE + "." + what) : PluralizingArgsMessage.format(Language.format(NODE + "." + what, args));
		Skript.error(sender, StringUtils.fixCapitalization(what));
	}
	
	private final static File[] EMPTY_FILE_ARRAY = new File[0];
	
	@SuppressWarnings("null")
	@Override
	@SuppressFBWarnings("REC_CATCH_EXCEPTION")
	public boolean onCommand(final @Nullable CommandSender sender, final @Nullable Command command, final @Nullable String label, final @Nullable String[] args) {
		if (sender == null || command == null || label == null || args == null)
			throw new IllegalArgumentException();
		if (!skriptCommandHelp.test(sender, args))
			return true;
		final RedirectingLogHandler r = SkriptLogger.startLogHandler(new RedirectingLogHandler(sender, ""));
		try {
			if ("reload".equalsIgnoreCase(args[0])) {
				if ("all".equalsIgnoreCase(args[1])) {
					reloading(sender, "config and scripts");
					Skript.reload();
					reloaded(sender, r, "config and scripts");
				} else if ("scripts".equalsIgnoreCase(args[1])) {
					reloading(sender, "scripts");
					Skript.reloadScripts();
					reloaded(sender, r, "scripts");
				} else if ("config".equalsIgnoreCase(args[1])) {
					reloading(sender, "main config");
					Skript.reloadMainConfig();
					reloaded(sender, r, "main config");
				} else if ("aliases".equalsIgnoreCase(args[1])) {
					reloading(sender, "aliases");
					Skript.reloadAliases();
					reloaded(sender, r, "aliases");
				} else {
					final File f = getScriptFromArgs(sender, args, 1);
					if (f == null)
						return true;
					if (!f.isDirectory()) {
						if (f.getName().startsWith("-")) {
							info(sender, "reload.script disabled", f.getName().substring(1));
							return true;
						}
						reloading(sender, "script", f.getName());
						ScriptLoader.unloadScript(f);
						ScriptLoader.loadScripts(new File[] {f});
						reloaded(sender, r, "script", f.getName());
					} else {
						reloading(sender, "scripts in folder", f.getName());
						final int disabled = ScriptLoader.unloadScripts(f).files;
						final int enabled = ScriptLoader.loadScripts(f).files;
						if (Math.max(disabled, enabled) == 0)
							info(sender, "reload.empty folder", f.getName());
						else
							reloaded(sender, r, "x scripts in folder", f.getName(), Math.max(disabled, enabled));
					}
				}
			} else if ("enable".equalsIgnoreCase(args[0])) {
				if ("all".equals(args[1])) {
					try {
						info(sender, "enable.all.enabling");
						final File[] files = toggleScripts(new File(Skript.getInstance().getDataFolder(), Skript.SCRIPTSFOLDER), true).toArray(EMPTY_FILE_ARRAY);
						assert files != null;
						ScriptLoader.loadScripts(files);
						if (r.numErrors() == 0) {
							info(sender, "enable.all.enabled");
						} else {
							error(sender, "enable.all.error", r.numErrors());
						}
					} catch (final IOException e) {
						error(sender, "enable.all.io error", ExceptionUtils.toString(e));
					}
				} else {
					File f = getScriptFromArgs(sender, args, 1);
					if (f == null)
						return true;
					if (!f.isDirectory()) {
						if (!f.getName().startsWith("-")) {
							info(sender, "enable.single.already enabled", f.getName(), StringUtils.join(args, " ", 1, args.length));
							return true;
						}
						
						try {
							f = FileUtils.move(f, new File(f.getParentFile(), f.getName().substring(1)), false);
						} catch (final IOException e) {
							error(sender, "enable.single.io error", f.getName().substring(1), ExceptionUtils.toString(e));
							return true;
						}
						
						info(sender, "enable.single.enabling", f.getName());
						ScriptLoader.loadScripts(new File[] {f});
						if (r.numErrors() == 0) {
							info(sender, "enable.single.enabled", f.getName());
						} else {
							error(sender, "enable.single.error", f.getName(), r.numErrors());
						}
						return true;
					} else {
						final Collection<File> scripts;
						try {
							scripts = toggleScripts(f, true);
						} catch (final IOException e) {
							error(sender, "enable.folder.io error", f.getName(), ExceptionUtils.toString(e));
							return true;
						}
						if (scripts.isEmpty()) {
							info(sender, "enable.folder.empty", f.getName());
							return true;
						}
						info(sender, "enable.folder.enabling", f.getName(), scripts.size());
						final File[] ss = scripts.toArray(new File[0]);
						assert ss != null;
						final ScriptInfo i = ScriptLoader.loadScripts(ss);
						assert i.files == scripts.size();
						if (r.numErrors() == 0) {
							info(sender, "enable.folder.enabled", f.getName(), i.files);
						} else {
							error(sender, "enable.folder.error", f.getName(), r.numErrors());
						}
						return true;
					}
				}
			} else if ("disable".equalsIgnoreCase(args[0])) {
				if ("all".equals(args[1])) {
					Skript.disableScripts();
					try {
						toggleScripts(new File(Skript.getInstance().getDataFolder(), Skript.SCRIPTSFOLDER), false);
						info(sender, "disable.all.disabled");
					} catch (final IOException e) {
						error(sender, "disable.all.io error", ExceptionUtils.toString(e));
					}
				} else {
					final File f = getScriptFromArgs(sender, args, 1);
					if (f == null) // TODO allow disabling deleted/renamed scripts
						return true;
					if (!f.isDirectory()) {
						if (f.getName().startsWith("-")) {
							info(sender, "disable.single.already disabled", f.getName().substring(1));
							return true;
						}
						
						ScriptLoader.unloadScript(f);
						
						try {
							FileUtils.move(f, new File(f.getParentFile(), "-" + f.getName()), false);
						} catch (final IOException e) {
							error(sender, "disable.single.io error", f.getName(), ExceptionUtils.toString(e));
							return true;
						}
						info(sender, "disable.single.disabled", f.getName());
						return true;
					} else {
						final Collection<File> scripts;
						try {
							scripts = toggleScripts(f, false);
						} catch (final IOException e) {
							error(sender, "disable.folder.io error", f.getName(), ExceptionUtils.toString(e));
							return true;
						}
						if (scripts.isEmpty()) {
							info(sender, "disable.folder.empty", f.getName());
							return true;
						}
						
						for (final File script : scripts)
							ScriptLoader.unloadScript(new File(script.getParentFile(), script.getName().substring(1)));
						
						info(sender, "disable.folder.disabled", f.getName(), scripts.size());
						return true;
					}
				}
			} else if ("update".equalsIgnoreCase(args[0])) {
				Updater.stateLock.writeLock().lock();
				try {
					if ("check".equals(args[1])) {
						Skript.info(sender, Skript.updateAvailable ? "New version " + Skript.latestVersion + " is available. Download from here: " + Skript.LATEST_VERSION_DOWNLOAD_LINK : Updater.m_running_latest_version.toString());
					} else if ("changes".equalsIgnoreCase(args[1])) {
						Skript.info(sender, Skript.updateAvailable ? "New version " + Skript.latestVersion + " is available. Download from here: " + Skript.LATEST_VERSION_DOWNLOAD_LINK : Updater.m_running_latest_version.toString());
					} else if ("download".equalsIgnoreCase(args[1])) {
						Skript.info(sender, Skript.updateAvailable ? "New version " + Skript.latestVersion + " is available. Download from here: " + Skript.LATEST_VERSION_DOWNLOAD_LINK : Updater.m_running_latest_version.toString());
					}
				} finally {
					Updater.stateLock.writeLock().unlock();
				}
			} else if ("help".equalsIgnoreCase(args[0])) {
				skriptCommandHelp.showHelp(sender);
			}
		} catch (final Exception e) {
			Skript.exception(e, "Exception occurred in Skript's main command", "Used command: /" + label + " " + StringUtils.join(args, " "));
		} finally {
			r.stop();
		}
		return true;
	}
	
	private final static ArgsMessage m_invalid_script = new ArgsMessage(NODE + ".invalid script");
	private final static ArgsMessage m_invalid_folder = new ArgsMessage(NODE + ".invalid folder");
	
	@Nullable
	private static File getScriptFromArgs(final CommandSender sender, final String[] args, final int start) {
		String script = StringUtils.join(args, " ", start, args.length);
		final boolean isFolder = script.endsWith("/") || script.endsWith("\\");
		if (isFolder) {
			script = script.replace('/', File.separatorChar).replace('\\', File.separatorChar);
		} else if (!StringUtils.endsWithIgnoreCase(script, ".sk")) {
			script = script + ".sk";
		}
		if (script.startsWith("-"))
			script = script.substring(1);
		File f = new File(Skript.getInstance().getDataFolder(), Skript.SCRIPTSFOLDER + File.separator + script);
		if (!f.exists()) {
			f = new File(f.getParentFile(), "-" + f.getName());
			if (!f.exists()) {
				Skript.error(sender, (isFolder ? m_invalid_folder : m_invalid_script).toString(script));
				return null;
			}
		}
		return f;
	}
	
	private static Collection<File> toggleScripts(final File folder, final boolean enable) throws IOException {
		return FileUtils.renameAll(folder, new Converter<String, String>() {
			@Override
			@Nullable
			public String convert(final String name) {
				if (StringUtils.endsWithIgnoreCase(name, ".sk") && name.startsWith("-") == enable)
					return enable ? name.substring(1) : "-" + name;
				return null;
			}
		});
	}
	
}
