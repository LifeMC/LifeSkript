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

package ch.njol.skript.command;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.bukkitutil.SpikeDetector;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.BukkitLoggerFilter;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.SimplePluginManager;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.njol.skript.Skript.*;

//TODO option to disable replacement of <color>s in command arguments?

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public final class Commands {

    public static final ArgsMessage m_too_many_arguments = new ArgsMessage("commands.too many arguments");
    public static final Message m_internal_error = new Message("commands.internal error");
    public static final boolean cancellableServerCommand = Skript.methodExists(ServerCommandEvent.class, "setCancelled", boolean.class);
    public static final AtomicBoolean cancelledEvent =
            new AtomicBoolean();
    private static final Map<String, ScriptCommand> commands = new HashMap<>();
    private static final SectionValidator commandStructure = new SectionValidator().addEntry("usage", true).addEntry("description", true).addEntry("permission", true).addEntry("permission message", true).addEntry("cooldown", true).addEntry("cooldown message", true).addEntry("cooldown bypass", true).addEntry("cooldown storage", true).addEntry("aliases", true).addEntry("executable by", true).addSection("trigger", false);
    @SuppressWarnings("null")
    private static final Pattern escape = Pattern.compile("[" + Pattern.quote("(|)<>%\\") + "]");
    @SuppressWarnings("null")
    private static final Pattern unescape = Pattern.compile("\\\\[" + Pattern.quote("(|)<>%\\") + "]");
    @SuppressWarnings("null")
    private static final Pattern commandPattern = Pattern.compile("(?i)^command /?(\\S+)\\s*(\\s+(.+))?$"),
            argumentPattern = Pattern.compile("<\\s*(?:(.+?)\\s*:\\s*)?(.+?)\\s*(?:=\\s*(" + SkriptParser.wildcard + "))?\\s*>");
    @Nullable
    public static List<Argument<?>> currentArguments;
    static boolean suppressUnknownCommandMessage;
    @Nullable
    private static SimpleCommandMap commandMap;
    @Nullable
    private static Map<String, Command> cmKnownCommands;
    @Nullable
    private static Set<String> cmAliases;
    private static boolean registeredListeners;

    static {
        init(); // separate method for the annotation
    }

    static {
        BukkitLoggerFilter.addFilter((final @Nullable LogRecord record) -> {
            if (record == null)
                return false;
            if (suppressUnknownCommandMessage && record.getMessage() != null && record.getMessage().startsWith("Unknown command. Type")) {
                suppressUnknownCommandMessage = false;
                return false;
            }
            return true;
        });
    }

    private Commands() {
        throw new UnsupportedOperationException("Static class");
    }

    @SuppressWarnings("unchecked")
    private static final void init() {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                final Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());

                final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                cmKnownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

                try {
                    // Aliases field is removed in new versions. (probably 1.7+)
                    if (!Skript.isRunningMinecraft(1, 7, 10)) {
                        //noinspection JavaReflectionMemberAccess
                        final Field aliasesField = SimpleCommandMap.class.getDeclaredField("aliases");
                        aliasesField.setAccessible(true);
                        cmAliases = (Set<String>) aliasesField.get(commandMap);
                    }
                } catch (final NoSuchFieldException ignored) {
                    /* ignored */
                }
            } else if (Skript.logHigh())
                Skript.warning("Your server is using an unsupported plugin manager. Commands will NOT be registered.");
        } catch (final SecurityException e) {
            Skript.error("Please disable the security manager");
            commandMap = null;
        } catch (final Exception e) {
            Skript.outdatedError(e);
            commandMap = null;
        }
    }

    private static final String escape(final String s) {
        return escape.matcher(s).replaceAll("\\\\$0");
    }

    private static final String unescape(final String s) {
        return unescape.matcher(s).replaceAll("$0");
    }

    public static final void checkTimings(final String command) {
        if ("timings on".equalsIgnoreCase(command) && !SkriptTimings.timingsEnabled) {
            SkriptTimings.timingsEnabled = true;
            if ("default".equalsIgnoreCase(SkriptConfig.enableSpikeDetector.value()))
                SpikeDetector.setEnabled(true);
            Skript.info("Timings mode enabled");
        } else if ("timings off".equalsIgnoreCase(command) && SkriptTimings.timingsEnabled) {
            SkriptTimings.timingsEnabled = false;
            if ("default".equalsIgnoreCase(SkriptConfig.enableSpikeDetector.value()))
                SpikeDetector.setEnabled(false);
            Skript.info("Timings mode disabled");
        }
    }

    @SuppressWarnings("null")
    public static final void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
        final String command = e.getMessage().substring(1);
        checkTimings(command);

        if (handleCommand(e.getPlayer(), command)) {
            e.setCancelled(true);
            cancelledEvent.set(true);
            if (!SkriptConfig.throwOnCommandOnlyForPluginCommands.value()) {
                try {
                    // We cancelled the current event, execute this one instead.
                    SkriptEventHandler.last = null;
                    SkriptEventHandler.ee.execute(null, new PlayerCommandPreprocessEvent(e.getPlayer(), e.getMessage()));
                } catch (final EventException ex) {
                    Skript.exception(ex, "Error when handling player command \"" + e.getMessage() + "\"");
                }
            }
        }
    }

    @SuppressWarnings("null")
    public static final void onServerCommand(final ServerCommandEvent e) {
        if (e.getCommand() == null || e.getCommand().isEmpty() || cancellableServerCommand && e.isCancelled())
            return;
        checkTimings(e.getCommand());
        boolean effectCommand = false;
        if (SkriptConfig.enableEffectCommands.value() && e.getCommand().startsWith(SkriptConfig.effectCommandToken.value())) {
            if (!handleEffectCommand(e.getSender(), e.getCommand())) {
                return;
            }
            effectCommand = true;
        }
        if (!effectCommand && !handleCommand(e.getSender(), e.getCommand())) {
            return;
        }
        final String command = e.getCommand();
        if (cancellableServerCommand)
            e.setCancelled(true);
        e.setCommand("");
        cancelledEvent.set(true);
        suppressUnknownCommandMessage = true;
        if (!SkriptConfig.throwOnCommandOnlyForPluginCommands.value()) {
            try {
                // We cancelled the current event, execute this one instead.
                SkriptEventHandler.last = null;
                SkriptEventHandler.ee.execute(null, new ServerCommandEvent(e.getSender(), command));
            } catch (final EventException ex) {
                Skript.exception(ex, "Error when handling player command \"" + command + "\"");
            }
        }
    }

    @SuppressWarnings("null")
    public static final void onAsyncPlayerChat(final AsyncPlayerChatEvent e) {
        if (!SkriptConfig.enableEffectCommands.value() || !e.getMessage().startsWith(SkriptConfig.effectCommandToken.value()))
            return;
        if (!e.isAsynchronous()) {
            if (handleEffectCommand(e.getPlayer(), e.getMessage())) {
                e.setCancelled(true);
                cancelledEvent.set(true);
            }
        } else {
            final Future<Boolean> f = Bukkit.getScheduler().callSyncMethod(Skript.getInstance(), () -> handleEffectCommand(e.getPlayer(), e.getMessage()));
            try {
                try {
                    if (f.get())
                        e.setCancelled(true);
                } catch (final InterruptedException ie) {
                    Skript.exception(ie);
                    Thread.currentThread().interrupt();
                }
            } catch (final ExecutionException ee) {
                Skript.exception(ee);
            }
        }
    }

    /**
     * @deprecated {@link Commands#onAsyncPlayerChat(AsyncPlayerChatEvent)}
     */
    @Deprecated
    @SuppressWarnings("null")
    public static final void onPlayerChat(final PlayerChatEvent e) {
        if (!SkriptConfig.enableEffectCommands.value() || !e.getMessage().startsWith(SkriptConfig.effectCommandToken.value()))
            return;
        if (handleEffectCommand(e.getPlayer(), e.getMessage())) {
            e.setCancelled(true);
            cancelledEvent.set(true);
        }
    }

    /**
     * @param sender  the sender of the command
     * @param command full command string without the slash
     * @return whatever to cancel the event
     */
    public static final boolean handleCommand(final CommandSender sender, final String command) {
        final String[] cmd = command.split("\\s+", 2);
        cmd[0] = cmd[0].toLowerCase(Locale.ENGLISH);
        if (cmd[0].endsWith("?")) {
            final ScriptCommand c = commands.get(cmd[0].substring(0, cmd[0].length() - 1));
            if (c != null) {
                c.sendHelp(sender);
                return true;
            }
        }
        final ScriptCommand c = commands.get(cmd[0]);
        if (c != null) {
//            if (cmd.length == 2 && cmd[1].equals("?")) {
//                c.sendHelp(sender);
//                return true;
//            }
            if (Skript.logHigh())
                Skript.info("Executing script command \"/" + command + "\" as " + (sender instanceof Player ? sender.getName() : "console"));
            else if (SkriptConfig.logPlayerCommands.value() && sender instanceof Player)
                SkriptLogger.LOGGER.info(sender.getName() + " [" + ((Player) sender).getUniqueId() + "]: /" + command);
            c.execute(sender, cmd[0], cmd.length == 1 ? "" : cmd[1]);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static final boolean handleEffectCommand(final CommandSender sender, String command) {
        if (!(sender instanceof ConsoleCommandSender || sender.hasPermission("skript.effectcommands") || SkriptConfig.allowOpsToUseEffectCommands.value() && sender.isOp()))
            return false;
        final boolean wasLocal = Language.setUseLocal(false);
        try {
            command = command.substring(SkriptConfig.effectCommandToken.value().length()).trim();
            if (command.isEmpty()) {
                info(sender, "Please enter an effect, expression or condition");
                return true;
            }
            final RetainingLogHandler log = SkriptLogger.startRetainingLog();
            try {
                ScriptLoader.setCurrentEvent("effect command", EffectCommandEvent.class);
                Effect e = Effect.parse(command, null);
                if (e == null) {
                    // Send return value of the expression or condition
                    log.clear(); // Clear the first error
                    e = Effect.parse("send \"%" + command.replace("\"", "\"\"") + "%\" to me", null);
                }
                ScriptLoader.deleteCurrentEvent();

                if (e != null) {
                    log.clear(); // Ignore warnings and stuff
                    log.printLog();

                    sender.sendMessage(ChatColor.GRAY + "executing '" + ChatColor.stripColor(command) + "'");
                    if (SkriptConfig.logPlayerCommands.value() && !(sender instanceof ConsoleCommandSender))
                        info(sender.getName() + " issued effect command: " + command);
                    TriggerItem.walk(e, new EffectCommandEvent(!Bukkit.isPrimaryThread(), sender, command));
                } else {
                    if (sender == Bukkit.getConsoleSender()) // log as SEVERE instead of INFO like printErrors below
                        error("Error in: " + ChatColor.stripColor(command));
                    else
                        info(sender, ChatColor.RED + "Error in: " + ChatColor.GRAY + ChatColor.stripColor(command));
                    if (log.getFirstError("").message.contains("Can't understand this expression"))
                        log.clear();
                    for (final LogEntry error : log.getErrors()) {
                        if (error.getMessage().trim().isEmpty())
                            continue;
                        error.setMessage((sender instanceof ConsoleCommandSender ? SKRIPT_PREFIX_CONSOLE : SKRIPT_PREFIX) + Utils.replaceEnglishChatStyles(error.getMessage()));
                    }
                    log.printErrors(sender, (sender instanceof ConsoleCommandSender ? SKRIPT_PREFIX_CONSOLE : SKRIPT_PREFIX) + Utils.replaceEnglishChatStyles("(No specific information is available)"));
                }
            } finally {
                log.stop();
            }
            return true;
        } catch (final Exception e) {
            Skript.exception(e, "Unexpected error while executing effect command '" + command + "' by '" + sender.getName() + "'");
            sender.sendMessage(ChatColor.RED + "An internal error occurred while executing this effect. Please refer to the server log for details.");
            return true;
        } finally {
            Language.setUseLocal(wasLocal);
        }
    }

//    public static final boolean skriptCommandExists(final String command) {
//        final ScriptCommand c = commands.get(command);
//        return c != null && c.getName().equals(command);
//    }

    @SuppressWarnings("null")
    @Nullable
    public static final ScriptCommand loadCommand(final SectionNode node) {
        final String key = node.getKey();
        if (key == null)
            return null;
        final String s = ScriptLoader.replaceOptions(key);

        int level = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '[') {
                level++;
            } else if (s.charAt(i) == ']') {
                if (level == 0) {
                    Skript.error("Invalid placement of [optional brackets]");
                    return null;
                }
                level--;
            }
        }
        if (level > 0) {
            Skript.error("Invalid amount of [optional brackets]");
            return null;
        }

        Matcher m = commandPattern.matcher(s);
        final boolean a = m.matches();
        assert a;

        final String command = m.group(1).toLowerCase(Locale.ENGLISH);
        final ScriptCommand existingCommand = commands.get(command);
        if (existingCommand != null && existingCommand.getLabel().equals(command)) {
            final File f = existingCommand.getScript();
            Skript.error("A command with the name /" + existingCommand.getName() + " is already defined" + (f == null ? "" : " in " + f.getName()));
            return null;
        }

        final String arguments = m.group(3) == null ? "" : m.group(3);
        final StringBuilder pattern = new StringBuilder(4096);

        final List<Argument<?>> currentArguments = Commands.currentArguments = new ArrayList<>(); //Mirre
        m = argumentPattern.matcher(arguments);
        int lastEnd = 0;
        int optionals = 0;
        for (int i = 0; m.find(); i++) {
            pattern.append(escape(arguments.substring(lastEnd, m.start())));
            optionals += StringUtils.count(arguments, '[', lastEnd, m.start());
            optionals -= StringUtils.count(arguments, ']', lastEnd, m.start());

            lastEnd = m.end();

            ClassInfo<?> c = Classes.getClassInfoFromUserInput(m.group(2));
            final NonNullPair<String, Boolean> p = Utils.getEnglishPlural(m.group(2));
            if (c == null)
                c = Classes.getClassInfoFromUserInput(p.getFirst());
            if (c == null) {
                Skript.error("Unknown type '" + m.group(2) + "'");
                return null;
            }
            final Parser<?> parser = c.getParser();
            if (parser == null || !parser.canParse(ParseContext.COMMAND)) {
                Skript.error("Can't use " + c + " as argument of a command");
                return null;
            }

            final Argument<?> arg = Argument.newInstance(m.group(1), c, m.group(3), i, !p.getSecond(), optionals > 0);
            if (arg == null)
                return null;
            currentArguments.add(arg);

            if (arg.isOptional() && optionals == 0) {
                pattern.append('[');
                optionals++;
            }
            pattern.append("%").append(arg.isOptional() ? "-" : "").append(Utils.toEnglishPlural(c.getCodeName(), p.getSecond())).append("%");
        }

        pattern.append(escape(arguments.substring(lastEnd)));
        optionals += StringUtils.count(arguments, '[', lastEnd);
        optionals -= StringUtils.count(arguments, ']', lastEnd);
        for (int i = 0; i < optionals; i++)
            pattern.append(']');

        String desc = "/" + command + " ";
        final boolean wasLocal = Language.setUseLocal(true); // use localised class names in description
        try {
            desc += StringUtils.replaceAll(pattern, "(?<!\\\\)%-?(.+?)%", m1 -> {
                assert m1 != null;
                final NonNullPair<String, Boolean> p = Utils.getEnglishPlural(m1.group(1));
                final String s1 = p.getFirst();
                return "<" + Classes.getClassInfo(s1).getName().toString(p.getSecond()) + ">";
            });
        } finally {
            Language.setUseLocal(wasLocal);
        }
        desc = unescape(desc);
        desc = desc.trim();

        node.convertToEntries(0);
        commandStructure.validate(node);
        if (!(node.get("trigger") instanceof SectionNode))
            return null;

        ArrayList<String> aliases = new ArrayList<>(Arrays.asList(ScriptLoader.replaceOptions(node.get("aliases", "")).split("\\s*,\\s*/?")));
        if (aliases.get(0).startsWith("/"))
            aliases.set(0, aliases.get(0).substring(1));
        else if (aliases.get(0).isEmpty())
            aliases = new ArrayList<>(0);

        final String rawPermissionMessage = ScriptLoader.replaceOptions(node.get("permission message", ""));
        Expression<String> permissionMessage = rawPermissionMessage.isEmpty() ? null : VariableString.newInstance(rawPermissionMessage);
        if (permissionMessage != null && ((VariableString) permissionMessage).isSimple()) {
            permissionMessage = new SimpleLiteral<>(rawPermissionMessage, false);
        }

        final SectionNode trigger = (SectionNode) node.get("trigger");
        if (trigger == null)
            return null;
        final String[] by = ScriptLoader.replaceOptions(node.get("executable by", "console,players")).split("\\s*,\\s*|\\s+(and|or)\\s+");
        int executableBy = 0;
        for (final String b : by) {
            if ("console".equalsIgnoreCase(b) || "the console".equalsIgnoreCase(b)) {
                executableBy |= ScriptCommand.CONSOLE;
            } else if ("players".equalsIgnoreCase(b) || "player".equalsIgnoreCase(b)) {
                executableBy |= ScriptCommand.PLAYERS;
            } else {
                Skript.warning("'executable by' should be either be 'players', 'console', or both, but found '" + b + "'");
            }
        }

        final String cooldownString = ScriptLoader.replaceOptions(node.get("cooldown", ""));
        Timespan cooldown = null;
        if (!cooldownString.isEmpty()) {
            // ParseContext doesn't matter for Timespan's parser
            cooldown = Classes.parse(cooldownString, Timespan.class, ParseContext.DEFAULT);
            if (cooldown == null) {
                Skript.warning("'" + cooldownString + "' is an invalid timespan for the cooldown");
            }
        }

        final String cooldownMessageString = ScriptLoader.replaceOptions(node.get("cooldown message", ""));
        final boolean usingCooldownMessage = !cooldownMessageString.isEmpty();
        VariableString cooldownMessage = null;
        if (usingCooldownMessage) {
            cooldownMessage = VariableString.newInstance(cooldownMessageString);
        }

        if (usingCooldownMessage && cooldownString.isEmpty()) {
            Skript.warning("command /" + command + " has a cooldown message set, but not a cooldown");
        }

        final String cooldownStorageString = ScriptLoader.replaceOptions(node.get("cooldown storage", ""));
        VariableString cooldownStorage = null;
        if (!cooldownStorageString.isEmpty()) {
            cooldownStorage = VariableString.newInstance(cooldownStorageString, StringMode.VARIABLE_NAME);
        }

        final String permission = ScriptLoader.replaceOptions(node.get("permission", ""));
        if (permissionMessage != null && permission.isEmpty()) {
            Skript.warning("command /" + command + " has a permission message set, but not permission");
        }

        if (Skript.debug() || node.debug())
            Skript.debug("command " + desc + ":");

        final File config = node.getConfig().getFile();
        if (config == null) {
            assert false;
            return null;
        }

        final String cooldownBypass = ScriptLoader.replaceOptions(node.get("cooldown bypass", ""));
        final String description = ScriptLoader.replaceOptions(node.get("description", ""));
        final String usage = ScriptLoader.replaceOptions(node.get("usage", desc));

        Commands.currentArguments = currentArguments;
        final ScriptCommand c;

        try {
            c = new ScriptCommand(config, command, pattern.toString(), currentArguments, description, usage, aliases, permission, permissionMessage, cooldown, cooldownMessage, cooldownBypass, cooldownStorage, executableBy, ScriptLoader.loadItems(trigger));
        } finally {
            Commands.currentArguments = null;
        }
        registerCommand(c);

        if (Skript.logVeryHigh() && !Skript.debug())
            info("registered command " + desc);
        return c;
    }

    public static final void registerCommand(final ScriptCommand command) {
        final ScriptCommand existingCommand = commands.get(command.getLabel());
        if (existingCommand != null && existingCommand.getLabel().equals(command.getLabel())) {
            final File f = existingCommand.getScript();
            Skript.error("A command with the name /" + existingCommand.getName() + " is already defined" + (f == null ? "" : " in " + f.getName()));
            return;
        }
        if (commandMap != null) {
            assert cmKnownCommands != null;// && cmAliases != null;
            command.register(commandMap, cmKnownCommands, cmAliases);
        }
        commands.put(command.getLabel(), command);
        for (final String alias : command.getActiveAliases()) {
            commands.put(alias.toLowerCase(Locale.ENGLISH), command);
        }
        command.registerHelp();
    }

    public static final int unregisterCommands(final File script) {
        int numCommands = 0;
        final Iterator<ScriptCommand> commandsIter = commands.values().iterator();
        while (commandsIter.hasNext()) {
            final ScriptCommand c = commandsIter.next();
            if (script.equals(c.getScript())) {
                numCommands++;
                c.unregisterHelp();
                if (commandMap != null) {
                    assert cmKnownCommands != null;// && cmAliases != null;
                    c.unregister(commandMap, cmKnownCommands, cmAliases);
                }
                commandsIter.remove();
            }
        }
        return numCommands;
    }

    public static final void registerListeners() {
        if (!registeredListeners) {
            final EventPriority commandPriority = SkriptConfig.commandPriority.value();

            if (Skript.debug())
                Skript.debug("Using command priority " + commandPriority.name().toLowerCase(Locale.ENGLISH));

            Bukkit.getPluginManager().registerEvent(PlayerCommandPreprocessEvent.class, new Listener() {
                /* ignored */
            }, commandPriority, (listener, event) -> {
                if (event instanceof PlayerCommandPreprocessEvent)
                    onPlayerCommand((PlayerCommandPreprocessEvent) event);
            }, Skript.getInstance(), true);

            Bukkit.getPluginManager().registerEvent(ServerCommandEvent.class, new Listener() {
                /* ignored */
            }, EventPriority.MONITOR, (listener, event) -> {
                if (event instanceof ServerCommandEvent)
                    onServerCommand((ServerCommandEvent) event);
            }, Skript.getInstance(), true);

            if (Skript.classExists("org.bukkit.event.player.AsyncPlayerChatEvent")) {
                Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, new Listener() {
                    /* ignored */
                }, EventPriority.MONITOR, (listener, event) -> {
                    if (event instanceof AsyncPlayerChatEvent)
                        onAsyncPlayerChat((AsyncPlayerChatEvent) event);
                }, Skript.getInstance(), true);
            } else if (Skript.classExists("org.bukkit.event.player.PlayerChatEvent")) {
                if (Skript.logHigh())
                    Skript.warning("Using non-async chat event, performance may drop!");
                Bukkit.getPluginManager().registerEvent(PlayerChatEvent.class, new Listener() {
                    /* ignored */
                }, EventPriority.MONITOR, (listener, event) -> {
                    if (event instanceof PlayerChatEvent)
                        onPlayerChat((PlayerChatEvent) event);
                }, Skript.getInstance(), true);
            } else
                Skript.outdatedError(new UnsupportedOperationException("Can't find chat event"));

            // If we use these methods, Bukkit will use reflection to access @EventHandler methods
            // so it's super slow, and we are handling the events manually to fix performance impact.

            //Bukkit.getPluginManager().registerEvents(commandListener, Skript.getInstance());
            //Bukkit.getPluginManager().registerEvents(post1_3chatListener != null ? post1_3chatListener : pre1_3chatListener, Skript.getInstance());

            registeredListeners = true;
        }
    }

    public static final void clearCommands() {
        final SimpleCommandMap commandMap = Commands.commandMap;
        if (commandMap != null) {
            final Map<String, Command> cmKnownCommands = Commands.cmKnownCommands;
            final Set<String> cmAliases = Commands.cmAliases;
            assert cmKnownCommands != null;// && cmAliases != null;
            for (final ScriptCommand c : commands.values())
                c.unregister(commandMap, cmKnownCommands, cmAliases);
        }
        for (final ScriptCommand c : commands.values()) {
            c.unregisterHelp();
        }
        commands.clear();
    }

    /**
     * copied from CraftBukkit (org.bukkit.craftbukkit.help.CommandAliasHelpTopic)
     */
    public static final class CommandAliasHelpTopic extends HelpTopic {

        private final String aliasFor;
        private final HelpMap helpMap;

        public CommandAliasHelpTopic(final String alias, final String aliasFor, final HelpMap helpMap) {
            this.aliasFor = aliasFor.startsWith("/") ? aliasFor : "/" + aliasFor;
            this.helpMap = helpMap;
            name = alias.startsWith("/") ? alias : "/" + alias;
            Validate.isTrue(!name.equals(this.aliasFor), "Command " + name + " cannot be alias for itself");
            shortText = ChatColor.YELLOW + "Alias for " + ChatColor.WHITE + this.aliasFor;
        }

        @Override
        public String getFullText(final @Nullable CommandSender forWho) {
            final StringBuilder sb = new StringBuilder(shortText);
            final HelpTopic aliasForTopic = helpMap.getHelpTopic(aliasFor);
            if (aliasForTopic != null) {
                sb.append("\n");
                sb.append(aliasForTopic.getFullText(forWho));
            }
            return sb.toString();
        }

        @Override
        public boolean canSee(final @Nullable CommandSender commandSender) {
            if (amendedPermission == null) {
                final HelpTopic aliasForTopic = helpMap.getHelpTopic(aliasFor);
                if (aliasForTopic != null) {
                    return aliasForTopic.canSee(commandSender);
                }
                return false;
            }
            return commandSender != null && commandSender.hasPermission(amendedPermission);
        }
    }

}
