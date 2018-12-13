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
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.util.Color;

import org.bukkit.command.CommandSender;

import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.RESET;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public class CommandHelp {
	
	private final static String DEFAULTENTRY = "description";
	
	private final static ArgsMessage m_invalid_argument = new ArgsMessage("commands.invalid argument");
	private final static Message m_usage = new Message("commands.usage");
	
	private String command;
	
	@Nullable
	private Message description;
	
	private final String argsColor;
	
	@Nullable
	private String langNode;
	
	private final LinkedHashMap<String, Object> arguments = new LinkedHashMap<String, Object>();
	
	@Nullable
	private Message wildcardArg;
	
	public CommandHelp(final String command, final Color argsColor, final String langNode) {
		this.command = command;
		this.argsColor = "" + argsColor.getChat();
		this.langNode = langNode;
		description = new Message(langNode + "." + DEFAULTENTRY);
	}
	
	public CommandHelp(final String command, final Color argsColor) {
		this.command = command;
		this.argsColor = "" + argsColor.getChat();
	}
	
	public CommandHelp add(final String argument) {
		if (langNode == null) {
			if (argument.startsWith("<") && argument.endsWith(">")) {
				final String carg = GRAY + "<" + argsColor + argument.substring(1, argument.length() - 1) + GRAY + ">";
				arguments.put(carg, argument);
			} else {
				arguments.put(argument, null);
			}
		} else {
			if (argument.startsWith("<") && argument.endsWith(">")) {
				final String carg = GRAY + "<" + argsColor + argument.substring(1, argument.length() - 1) + GRAY + ">";
				wildcardArg = new Message(langNode + "." + argument);
				arguments.put(carg, wildcardArg);
			} else {
				arguments.put(argument, new Message(langNode + "." + argument));
			}
		}
		return this;
	}
	
	public CommandHelp add(final CommandHelp help) {
		arguments.put(help.command, help);
		help.onAdd(this);
		return this;
	}
	
	protected void onAdd(final CommandHelp parent) {
		langNode = parent.langNode + "." + command;
		description = new Message(langNode + "." + DEFAULTENTRY);
		command = parent.command + " " + parent.argsColor + command;
		for (final Entry<String, Object> e : arguments.entrySet()) {
			if (e.getValue() instanceof CommandHelp) {
				((CommandHelp) e.getValue()).onAdd(this);
			} else {
				if (e.getValue() != null) { // wildcard arg
					wildcardArg = new Message(langNode + "." + e.getValue());
					e.setValue(wildcardArg);
				} else {
					e.setValue(new Message(langNode + "." + e.getKey()));
				}
			}
		}
	}
	
	public boolean test(final CommandSender sender, final String[] args) {
		return test(sender, args, 0);
	}
	
	private boolean test(final CommandSender sender, final String[] args, final int index) {
		if (index >= args.length) {
			showHelp(sender);
			return false;
		}
		final Object help = arguments.get(args[index].toLowerCase());
		if (help == null && wildcardArg == null) {
			showHelp(sender, m_invalid_argument.toString(argsColor + args[index]));
			return false;
		}
		if (help instanceof CommandHelp)
			return ((CommandHelp) help).test(sender, args, index + 1);
		return true;
	}
	
	@SuppressWarnings("null")
	public void showHelp(final CommandSender sender) {
		showHelp(sender, m_usage.toString());
	}
	
	private void showHelp(final CommandSender sender, final String pre) {
		Skript.message(sender, pre + " " + command + " " + argsColor + "...");
		for (final Entry<String, Object> e : arguments.entrySet()) {
			Skript.message(sender, "  " + argsColor + e.getKey() + " " + GRAY + "-" + RESET + " " + e.getValue());
		}
	}
	
	@Override
	public String toString() {
		return "" + description;
	}
	
}
