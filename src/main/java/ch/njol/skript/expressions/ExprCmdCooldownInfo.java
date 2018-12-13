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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.expressions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

@Name("Cooldown Time/Remaining Time/Elapsed Time/Last Usage Date/Cooldown Bypass Permission")
@Description({"Only usable in command events. Represents the cooldown time, the remaining time, or the elapsed time, or the last usage date, or the cooldown bypass permission."})
@Examples({"command /home:", "\tcooldown: 10 seconds", "\tcooldown message: You last teleported home %elapsed time% ago, you may teleport home again in %remaining time%", "\ttrigger:", "\t\tteleport player to {home::%player%}"
})
@Since("2.2-Fixes-V10b")
public class ExprCmdCooldownInfo extends SimpleExpression<Object> {
	
	static {
		Skript.registerExpression(ExprCmdCooldownInfo.class, Object.class, ExpressionType.SIMPLE, "[the] (0¦remaining [time]|1¦elapsed [time]|2¦cooldown [time] [length]|3¦last usage [date]|4¦cooldown bypass perm[ission]) [of] [the] [(cooldown|wait)] [((of|for)[the] [current] command)]");
	}
	
	private int mark;
	
	@Override
	@Nullable
	@SuppressWarnings("null")
	protected Object[] get(final Event e) {
		if (!(e instanceof ScriptCommandEvent)) {
			return null;
		}
		final ScriptCommandEvent event = (ScriptCommandEvent) e;
		final ScriptCommand scriptCommand = event.getSkriptCommand();
		
		final CommandSender sender = event.getSender();
		if (scriptCommand.getCooldown() == null || !(sender instanceof Player)) {
			return null;
		}
		final Player player = (Player) event.getSender();
		final UUID uuid = player.getUniqueId();
		
		switch (mark) {
			case 0:
			case 1:
				final long ms = mark != 1 ? scriptCommand.getRemainingMilliseconds(uuid, event) : scriptCommand.getElapsedMilliseconds(uuid, event);
				return new Timespan[] {new Timespan(ms)};
			case 2:
				return new Timespan[] {scriptCommand.getCooldown()};
			case 3:
				return new Date[] {scriptCommand.getLastUsage(uuid, event)};
			case 4:
				return new String[] {scriptCommand.getCooldownBypass()};
		}
		
		return null;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<?> getReturnType() {
		return mark <= 2 ? Timespan.class : String.class;
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return "the " + getExpressionName();
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		mark = parseResult.mark;
		if (!ScriptLoader.isCurrentEvent(ScriptCommandEvent.class)) {
			Skript.error("The expression '" + getExpressionName() + " time' can only be used within a command", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		return true;
	}
	
	@Nullable
	private String getExpressionName() {
		switch (mark) {
			case 0:
				return "remaining time";
			case 1:
				return "elapsed time";
			case 2:
				return "cooldown time";
			case 3:
				return "last usage date";
			case 4:
				return "cooldown bypass permission";
		}
		return null;
	}
	
	@Nullable
	@Override
	@SuppressWarnings("incomplete-switch")
	public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
		switch (mode) {
			case ADD:
			case REMOVE:
				if (mark <= 1) {
					// remaining or elapsed time
					return new Class<?>[] {Timespan.class};
				}
				//$FALL-THROUGH$
			case REMOVE_ALL:
			case RESET:
			case SET:
				if (mark <= 1) {
					// remaining or elapsed time
					return new Class<?>[] {Timespan.class};
				} else if (mark == 3) {
					// last usage dtae
					return new Class<?>[] {Date.class};
				}
		}
		return null;
	}
	
	@Override
	@SuppressWarnings({"incomplete-switch", "null"})
	public void change(final Event e, @Nullable final Object[] delta, final Changer.ChangeMode mode) {
		if (!(e instanceof ScriptCommandEvent)) {
			return;
		}
		final ScriptCommandEvent commandEvent = (ScriptCommandEvent) e;
		final ScriptCommand command = commandEvent.getSkriptCommand();
		final Timespan cooldown = command.getCooldown();
		final CommandSender sender = commandEvent.getSender();
		if (cooldown == null || !(sender instanceof Player)) {
			return;
		}
		final long cooldownMs = cooldown.getMilliSeconds();
		final UUID uuid = ((Player) sender).getUniqueId();
		
		if (mark <= 1) {
			final Timespan timespan = delta == null ? new Timespan(0) : (Timespan) delta[0];
			switch (mode) {
				case ADD:
				case REMOVE:
					final long change = (mode == Changer.ChangeMode.ADD ? 1 : -1) * timespan.getMilliSeconds();
					if (mark == 0) {
						// Remaining time
						final long remaining = command.getRemainingMilliseconds(uuid, commandEvent);
						long changed = remaining + change;
						if (changed < 0) {
							changed = 0;
						}
						command.setRemainingMilliseconds(uuid, commandEvent, changed);
					} else {
						// Elapsed time
						final long elapsed = command.getElapsedMilliseconds(uuid, commandEvent);
						long changed = elapsed + change;
						if (changed > cooldownMs) {
							changed = cooldownMs;
						}
						command.setElapsedMilliSeconds(uuid, commandEvent, changed);
					}
					break;
				case REMOVE_ALL:
				case RESET:
					if (mark == 0) {
						// Remaining time
						command.setRemainingMilliseconds(uuid, commandEvent, cooldownMs);
					} else {
						// Elapsed time
						command.setElapsedMilliSeconds(uuid, commandEvent, 0);
					}
					break;
				case SET:
					if (mark == 0) {
						// Remaining time
						command.setRemainingMilliseconds(uuid, commandEvent, timespan.getMilliSeconds());
					} else {
						// Elapsed time
						command.setElapsedMilliSeconds(uuid, commandEvent, timespan.getMilliSeconds());
					}
					break;
			}
		} else if (mark == 3) {
			switch (mode) {
				case REMOVE_ALL:
				case RESET:
					command.setLastUsage(uuid, commandEvent, null);
					break;
				case SET:
					final Date date = delta == null ? null : (Date) delta[0];
					command.setLastUsage(uuid, commandEvent, date);
					break;
			}
		}
	}
}
