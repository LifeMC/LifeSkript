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

package ch.njol.skript.effects;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

@Name("Cancel Command Cooldown")
@Description({"Only usable in command events. Makes it so the current command usage isn't counted towards the cooldown."})
@Examples({"command /nick <text>:", "\texecutable by: players", "\tcooldown: 10 seconds", "\ttrigger:", "\t\tif length of arg-1 is more than 16:", "\t\t\t# Makes it so that invalid arguments don't make you wait for the cooldown again", "\t\t\tcancel the cooldown", "\t\t\tsend \"Your nickname may be at most 16 characters.\"", "\t\t\tstop", "\t\tset the player's display name to arg-1"
})
@Since("2.2-Fixes-V10b")
public class EffCancelCooldown extends Effect {
	static {
		Skript.registerEffect(EffCancelCooldown.class, "(cancel|ignore) [the] [current] [command] cooldown", "un(cancel|ignore) [the] [current] [command] cooldown");
	}
	
	private boolean cancel;
	
	@Override
	protected void execute(final Event e) {
		if (!(e instanceof ScriptCommandEvent)) {
			return;
		}
		((ScriptCommandEvent) e).setCooldownCancelled(cancel);
	}
	
	@Override
	public String toString(@Nullable final Event e, final boolean debug) {
		return (cancel ? "" : "un") + "cancel the command cooldown";
	}
	
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
		if (!ScriptLoader.isCurrentEvent(ScriptCommandEvent.class)) {
			Skript.error("The cancel cooldown effect may only be used in a command.", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		cancel = matchedPattern == 0;
		return true;
	}
}
