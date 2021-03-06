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

package ch.njol.skript.effects;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.StringMode;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Command")
@Description("Executes a command. This can be useful to use other plugins in triggers.")
@Examples({"make player execute the command \"/suicide\"", "execute console command \"/say Hello everyone!\""})
@Since("1.0")
public final class EffCommand extends Effect {
    static {
        Skript.registerEffect(EffCommand.class, EffCommand::new, "[execute] [the] command %strings% [by %-commandsenders%]", "[execute] [the] %commandsenders% command %strings%", "(let|make) %commandsenders% execute [[the] command] %strings%");
    }

    @Nullable
    private Expression<CommandSender> senders;
    @SuppressWarnings("null")
    private Expression<String> commands;

    @Nullable
    private String script;

    private int line;

    private boolean flag;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
        if (matchedPattern == 0) {
            commands = (Expression<String>) exprs[0];
            senders = (Expression<CommandSender>) exprs[1];
        } else {
            senders = (Expression<CommandSender>) exprs[0];
            commands = (Expression<String>) exprs[1];
        }
        if (commands instanceof Literal) {
            for (final String command : ((Literal<String>) commands).getAll()) {
                if ("".equalsIgnoreCase(command.trim()) || "/".equalsIgnoreCase(command.trim())) {
                    Skript.error("Executing empty commands is not possible");
                    return false;
                }
                if (!SkriptConfig.disableUseNativeEffectInsteadWarnings.value()) {
                    if (command.contains("eco") && (command.contains("give") ||
                            command.contains("take"))) {
                        Skript.warning("Use the native vault & economy hook instead, e.g: 'add 1000 to the player's balance' or 'remove 1000 from the player's balance'");
                    } else if (command.contains("give ")) {
                        Skript.warning("Use the native 'give' effect instead of executing a console command and depending on other plugins, e.g: 'give 1 diamond to the player'");
                    }
                }
            }
        }
        commands = VariableString.setStringMode(commands, StringMode.COMMAND);
        flag = Skript.debug();
        if (flag) {
            script = ScriptLoader.currentScript.getFileName();
            line = SkriptLogger.getNode().getLine();
        }
        return true;
    }

    @SuppressWarnings("null")
    @Override
    public void execute(final Event e) {
        for (String command : commands.getArray(e)) {
            assert command != null;
            if (!command.isEmpty() && command.charAt(0) == '/')
                command = command.substring(1);
            if (senders != null) {
                for (final CommandSender sender : senders.getArray(e)) {
                    assert sender != null;
                    if (flag)
                        Skript.info("Executing command \"" + command + "\" as " + sender.getName() + (script != null ? " (" + script + ", line " + line + ')' : ""));
                    Skript.dispatchCommand(sender, command);
                }
            } else {
                if (flag)
                    Skript.info("Executing command \"" + command + "\" as console" + (script != null ? " (" + script + ", line " + line + ')' : ""));
                Skript.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return "make " + (senders != null ? senders.toString(e, debug) : "the console") + " execute the command " + commands.toString(e, debug);
    }

}
