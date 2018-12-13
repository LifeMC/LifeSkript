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

package ch.njol.skript.command;

import ch.njol.skript.Skript;
import ch.njol.skript.command.Commands.CommandAliasHelpTopic;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.log.Verbosity;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.EmptyStacktraceException;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import ch.njol.util.StringUtils;
import ch.njol.util.Validate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is used for user-defined commands.
 * 
 * @author Peter Güttinger
 */
public class ScriptCommand implements CommandExecutor {
	public final static Message m_executable_by_players = new Message("commands.executable by players");
	public final static Message m_executable_by_console = new Message("commands.executable by console");
	
	final String name;
	private final String label;
	private final List<String> aliases;
	private List<String> activeAliases;
	private final String permission;
 	private final Expression<String> permissionMessage;
	private final String description;
	@Nullable
	private final Timespan cooldown;
	private final Expression<String> cooldownMessage;
	private final String cooldownBypass;
	@Nullable
	private final Expression<String> cooldownStorage;
	final String usage;
	
	final Trigger trigger;
	
	private final String pattern;
	private final List<Argument<?>> arguments;
	
	public final static int PLAYERS = 0x1, CONSOLE = 0x2, BOTH = PLAYERS | CONSOLE;
	final int executableBy;
	
	private transient PluginCommand bukkitCommand;
	
	private final Map<UUID,Date> lastUsageMap = new HashMap<UUID, Date>();
	
	/**
	 * Creates a new SkriptCommand.
	 * 
	 * @param name /name
	 * @param pattern
	 * @param arguments the list of Arguments this command takes
	 * @param description description to display in /help
	 * @param usage message to display if the command was used incorrectly
	 * @param aliases /alias1, /alias2, ...
	 * @param permission permission or null if none
	 * @param permissionMessage message to display if the player doesn't have the given permission
	 * @param items trigger to execute
	 */
	public ScriptCommand(final File script, final String name, final String pattern, final List<Argument<?>> arguments, final String description, final String usage, final List<String> aliases, final String permission, final @Nullable Expression<String> permissionMessage, @Nullable final Timespan cooldown,
			 @Nullable final VariableString cooldownMessage, final String cooldownBypass,
			 @Nullable final VariableString cooldownStorage, final int executableBy, final List<TriggerItem> items) {
		Validate.notNull(name, pattern, arguments, description, usage, aliases, items);
		this.name = name;
		label = "" + name.toLowerCase();
		this.permission = permission;
		this.permissionMessage = permissionMessage == null ?
				new SimpleLiteral<String>(Language.get("commands.no permission message"), false)
 				: permissionMessage;
				
		this.cooldown = cooldown;
		this.cooldownMessage = cooldownMessage == null
				? new SimpleLiteral<String>(Language.get("commands.cooldown message"),false)
				: cooldownMessage;
		this.cooldownBypass = cooldownBypass;
		this.cooldownStorage = cooldownStorage;
		
		final Iterator<String> as = aliases.iterator();
		while (as.hasNext()) { // remove aliases that are the same as the command
			if (as.next().equalsIgnoreCase(label))
				as.remove();
		}
		this.aliases = aliases;
		activeAliases = new ArrayList<String>(aliases);
		
		this.description = Utils.replaceEnglishChatStyles(description);
		this.usage = Utils.replaceEnglishChatStyles(usage);
		
		this.executableBy = executableBy;
		
		this.pattern = pattern;
		this.arguments = arguments;
		
		trigger = new Trigger(script, "command /" + name, new SimpleEvent(), items);
		
		bukkitCommand = setupBukkitCommand();
	}
	
	private PluginCommand setupBukkitCommand() {
		try {
			final Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
			final PluginCommand bukkitCommand = c.newInstance(name, Skript.getInstance());
			bukkitCommand.setAliases(aliases);
			bukkitCommand.setDescription(description);
			bukkitCommand.setLabel(label);
			bukkitCommand.setPermission(permission);
			// We can only set the message if it's available at parse time (aka a literal)
			if (permissionMessage instanceof Literal)
 				bukkitCommand.setPermissionMessage(((Literal<String>) permissionMessage).getSingle());
			bukkitCommand.setUsage(usage);
			bukkitCommand.setExecutor(this);
			return bukkitCommand;
		} catch (final Exception e) {
			Skript.outdatedError(e);
			throw new EmptyStacktraceException();
		}
	}
	
	@Override
	public boolean onCommand(final @Nullable CommandSender sender, final @Nullable Command command, final @Nullable String label, final @Nullable String[] args) {
		if (sender == null || label == null || args == null)
			return false;
		execute(sender, label, StringUtils.join(args, " "));
		return true;
	}
	
	@SuppressWarnings("null")
	public boolean execute(final CommandSender sender, final String commandLabel, final String rest) {
		if (sender instanceof Player) {
			if ((executableBy & PLAYERS) == 0) {
				sender.sendMessage("" + m_executable_by_console);
				return false;
			}
		} else {
			if ((executableBy & CONSOLE) == 0) {
				sender.sendMessage("" + m_executable_by_players);
				return false;
			}
		}
		
		final ScriptCommandEvent event = new ScriptCommandEvent(ScriptCommand.this, sender);
		
		if (!permission.isEmpty() && !sender.hasPermission(permission)) {
			sender.sendMessage(permissionMessage.getSingle(event));
			return false;
		}
		
		cooldownCheck : {
			if (sender instanceof Player && cooldown != null) {
				final Player player = (Player) sender;
				final UUID uuid = player.getUniqueId();

				// Cooldown bypass
				if (!cooldownBypass.isEmpty() && player.hasPermission(cooldownBypass)) {
					setLastUsage(uuid, event, null);
					break cooldownCheck;
				}

				if (getLastUsage(uuid, event) != null) {
					if (getRemainingMilliseconds(uuid, event) <= 0) {
                    	setLastUsage(uuid, event, null);
					} else {
						sender.sendMessage(cooldownMessage.getSingle(event));
						return false;
					}
				}
			}
		}
		
		if (Bukkit.isPrimaryThread()) {
			execute2(event, sender, commandLabel, rest);
			if (sender instanceof Player && !event.isCooldownCancelled()) {
				setLastUsage(((Player) sender).getUniqueId(), event, new Date());
 			}
		} else {
			// must not wait for the command to complete as some plugins call commands in such a way that the server will deadlock
			Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), new Runnable() {
				@Override
				public void run() {
					execute2(event, sender, commandLabel, rest);
					if (sender instanceof Player && !event.isCooldownCancelled()) {
						setLastUsage(((Player) sender).getUniqueId(), event, new Date());
 					}
				}
			});
		}
		return true; // Skript prints its own error message anyway
	}
	
	boolean execute2(final ScriptCommandEvent event, final CommandSender sender, final String commandLabel, final String rest) {
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final boolean ok = SkriptParser.parseArguments(rest, ScriptCommand.this, event);
			if (!ok) {
				final LogEntry e = log.getError();
				if (e != null)
					sender.sendMessage(ChatColor.DARK_RED + e.getMessage());
				sender.sendMessage(Commands.m_correct_usage + " " + usage);
				log.clear();
				log.printLog();
				return false;
			}
			log.clear();
			log.printLog();
		} finally {
			log.stop();
		}
		
		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# /" + name + " " + rest);
		final long startTrigger = System.nanoTime();
		
		if (!trigger.execute(event))
			sender.sendMessage(Commands.m_internal_error.toString());
		
		if (Skript.log(Verbosity.VERY_HIGH))
			Skript.info("# " + name + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
		return true;
	}
	
	public void sendHelp(final CommandSender sender) {
		if (!description.isEmpty())
			sender.sendMessage(description);
		sender.sendMessage(ChatColor.GOLD + "Usage" + ChatColor.RESET + ": " + usage);
	}
	
	/**
	 * Gets the arguments this command takes.
	 * 
	 * @return The internal list of arguments. Do not modify it!
	 */
	public List<Argument<?>> getArguments() {
		return arguments;
	}
	
	public String getPattern() {
		return pattern;
	}
	
	@Nullable
	private transient Command overridden;
	
	private transient Map<String, Command> overriddenAliases = new HashMap<String, Command>();
	
	public void register(final SimpleCommandMap commandMap, final Map<String, Command> knownCommands, final @Nullable Set<String> aliases) {
		synchronized (commandMap) {
			overriddenAliases.clear();
			overridden = knownCommands.put(label, bukkitCommand);
			if (aliases != null)
				aliases.remove(label);
			final Iterator<String> as = activeAliases.iterator();
			while (as.hasNext()) {
				final String lowerAlias = as.next().toLowerCase();
				if (knownCommands.containsKey(lowerAlias) && (aliases == null || !aliases.contains(lowerAlias))) {
					as.remove();
					continue;
				}
				overriddenAliases.put(lowerAlias, knownCommands.put(lowerAlias, bukkitCommand));
				if (aliases != null)
					aliases.add(lowerAlias);
			}
			bukkitCommand.setAliases(activeAliases);
			bukkitCommand.register(commandMap);
		}
	}
	
	public void unregister(final SimpleCommandMap commandMap, final Map<String, Command> knownCommands, final @Nullable Set<String> aliases) {
		synchronized (commandMap) {
			knownCommands.remove(label);
			if (aliases != null)
				aliases.removeAll(activeAliases);
			for (final String alias : activeAliases)
				knownCommands.remove(alias);
			activeAliases = new ArrayList<String>(this.aliases);
			bukkitCommand.unregister(commandMap);
			bukkitCommand.setAliases(this.aliases);
			if (overridden != null) {
				knownCommands.put(label, overridden);
				overridden = null;
			}
			for (final Entry<String, Command> e : overriddenAliases.entrySet()) {
				if (e.getValue() == null)
					continue;
				knownCommands.put(e.getKey(), e.getValue());
				if (aliases != null)
					aliases.add(e.getKey());
			}
			overriddenAliases.clear();
		}
	}
	
	private transient Collection<HelpTopic> helps = new ArrayList<HelpTopic>();
	
	public void registerHelp() {
		helps.clear();
		final HelpMap help = Bukkit.getHelpMap();
		final HelpTopic t = new GenericCommandHelpTopic(bukkitCommand);
		help.addTopic(t);
		helps.add(t);
		final HelpTopic aliases = help.getHelpTopic("Aliases");
		if (aliases instanceof IndexHelpTopic) {
			aliases.getFullText(Bukkit.getConsoleSender()); // CraftBukkit has a lazy IndexHelpTopic class (org.bukkit.craftbukkit.help.CustomIndexHelpTopic) - maybe its used for aliases as well
			try {
				final Field topics = IndexHelpTopic.class.getDeclaredField("allTopics");
				topics.setAccessible(true);
				@SuppressWarnings("unchecked")
				final ArrayList<HelpTopic> as = new ArrayList<HelpTopic>((Collection<HelpTopic>) topics.get(aliases));
				for (final String alias : activeAliases) {
					final HelpTopic at = new CommandAliasHelpTopic("/" + alias, "/" + getLabel(), help);
					as.add(at);
					helps.add(at);
				}
				Collections.sort(as, HelpTopicComparator.helpTopicComparatorInstance());
				topics.set(aliases, as);
			} catch (final Exception e) {
				Skript.outdatedError(e);//, "error registering aliases for /" + getName());
			}
		}
	}
	
	public void unregisterHelp() {
		Bukkit.getHelpMap().getHelpTopics().removeAll(helps);
		final HelpTopic aliases = Bukkit.getHelpMap().getHelpTopic("Aliases");
		if (aliases instanceof IndexHelpTopic) {
			try {
				final Field topics = IndexHelpTopic.class.getDeclaredField("allTopics");
				topics.setAccessible(true);
				@SuppressWarnings("unchecked")
				final ArrayList<HelpTopic> as = new ArrayList<HelpTopic>((Collection<HelpTopic>) topics.get(aliases));
				as.removeAll(helps);
				topics.set(aliases, as);
			} catch (final Exception e) {
				Skript.outdatedError(e);//, "error unregistering aliases for /" + getName());
			}
		}
		helps.clear();
	}
	
	public String getName() {
		return name;
	}
	
	public String getLabel() {
		return label;
	}
	
	@Nullable
	public Timespan getCooldown() {
		return cooldown;
	}

	@Nullable
	private String getStorageVariableName(final Event event) {
		assert cooldownStorage != null;
		String variableString = cooldownStorage.getSingle(event);
		if (variableString == null) {
			return null;
		}
		if (variableString.startsWith("{")) {
			variableString = variableString.substring(1);
		}
		if (variableString.endsWith("}")) {
			variableString = variableString.substring(0, variableString.length() - 1);
		}
		return variableString;
	}

	@Nullable
	public Date getLastUsage(final UUID uuid, final Event event) {
		if (cooldownStorage == null) {
			return lastUsageMap.get(uuid);
		} else {
			final String name = getStorageVariableName(event);
			assert name != null;
			return (Date) Variables.getVariable(name, null, false);
		}
	}

	public void setLastUsage(final UUID uuid, final Event event, @Nullable final Date date) {
		if (cooldownStorage != null) {
			// Using a variable
			final String name = getStorageVariableName(event);
			assert name != null;
			Variables.setVariable(name, date, null, false);
		} else {
			// Use the map
			if (date == null) {
                lastUsageMap.remove(uuid);
            } else {
                lastUsageMap.put(uuid, date);
            }
		}
	}

	public long getRemainingMilliseconds(final UUID uuid, final Event event) {
		final Date lastUsage = getLastUsage(uuid, event);
		if (lastUsage == null) {
			return 0;
		}
		final Timespan cooldown = this.cooldown;
		assert cooldown != null;
		long remaining = cooldown.getMilliSeconds() - getElapsedMilliseconds(uuid, event);
		if (remaining < 0) {
			remaining = 0;
		}
		return remaining;
	}

	public void setRemainingMilliseconds(final UUID uuid, final Event event, final long milliseconds) {
		final Timespan cooldown = this.cooldown;
		assert cooldown != null;
		final long cooldownMs = cooldown.getMilliSeconds();
		if (milliseconds > cooldownMs) {
			throw new IllegalArgumentException("Remaining time may not be longer than the cooldown");
		}
		setElapsedMilliSeconds(uuid, event, cooldownMs - milliseconds);
	}

	public long getElapsedMilliseconds(final UUID uuid, final Event event) {
		final Date lastUsage = getLastUsage(uuid, event);
		return lastUsage == null ? 0 : new Date().getTimestamp() - lastUsage.getTimestamp();
	}

	public void setElapsedMilliSeconds(final UUID uuid, final Event event, final long milliseconds) {
		final Date date = new Date();
		date.subtract(new Timespan(milliseconds));
		setLastUsage(uuid, event, date);
	}

	public String getCooldownBypass() {
		return cooldownBypass;
	}
	
	public List<String> getAliases() {
		return aliases;
	}
	
	public List<String> getActiveAliases() {
		return activeAliases;
	}
	
	public PluginCommand getBukkitCommand() {
		return bukkitCommand;
	}
	
	@Nullable
	public File getScript() {
		return trigger.getScript();
	}
	
}
