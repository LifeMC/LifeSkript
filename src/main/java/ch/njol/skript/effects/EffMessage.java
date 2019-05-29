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
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerCommandEvent;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Send / Message")
@Description("Sends a message to the given player or console.")
@Examples({"send \"A wild %player% appeared!\"", "send \"This message is a distraction. Mwahaha!\"", "send \"Your kill streak is %{kill streak.%player%}%.\" to player", "if the targeted entity exists:", "	send \"You're currently looking at a %type of the targeted entity%!\""})
@Since("1.0")
public final class EffMessage extends Effect {
    static {
        Skript.registerEffect(EffMessage.class, "(message|msg|tell|send [message]) %strings% [to %commandsenders%]");
    }

    @SuppressWarnings("null")
    private Expression<String> messages;
    @SuppressWarnings("null")
    private Expression<CommandSender> recipients;

    @Nullable
    private String script;

    private int line;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
        messages = (Expression<String>) exprs[0];
        recipients = (Expression<CommandSender>) exprs[1];

        if (SkriptConfig.enableExplicitPlayerUseWarnings.value()) {
            script = ScriptLoader.currentScript.getFileName();
            line = SkriptLogger.getNode().getLine();
        }

        return true;
    }

    @Override
    protected void execute(final Event e) {
        for (final String message : messages.getArray(e)) {
//			message = StringUtils.fixCapitalization(message);
            final CommandSender[] recipientsArray = recipients.getArray(e);
            if (SkriptConfig.enableExplicitPlayerUseWarnings.value() && recipientsArray.length == 1 && !(recipientsArray[0] instanceof ConsoleCommandSender) && e instanceof ServerCommandEvent) {
                Skript.warning("Command used from console, but send message uses the form of explicit \"to player\". For clarification, limit the command to the players, or remove the \"to player\" part." + (script != null ? " (" + script + ", line " + line + ")" : ""));
            }
            for (final CommandSender s : recipientsArray) {
                s.sendMessage(message);
            }
        }
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {
        return "send " + messages.toString(e, debug) + " to " + recipients.toString(e, debug);
    }
}
