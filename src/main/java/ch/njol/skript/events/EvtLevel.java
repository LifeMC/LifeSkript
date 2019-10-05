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

package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.eclipse.jdt.annotation.Nullable;

public final class EvtLevel extends SkriptEvent {

    static {
        Skript.registerEvent("Level Change", EvtLevel.class, PlayerLevelChangeEvent.class, "[player] level (change|1¦up|-1¦down)")
                .description("Called when a player's <a href='../expressions/#ExprLevel'>level</a> changes, e.g. by gathering experience or by enchanting something.")
                .examples("on level change:")
                .since("1.0, 2.2.18 (level up/down)");
    }

    @SuppressWarnings("null")
    private Kleenean leveling;

    @Override
    public final boolean init(final Literal<?>[] args, final int matchedPattern, final SkriptParser.ParseResult parseResult) {
        leveling = Kleenean.get(parseResult.mark);
        return true;
    }

    @Override
    public final boolean check(final Event e) {
        final PlayerLevelChangeEvent event = (PlayerLevelChangeEvent) e;
        if (leveling.isTrue())
            return event.getNewLevel() > event.getOldLevel();
        if (leveling.isFalse())
            return event.getNewLevel() < event.getOldLevel();
        return true;
    }

    @Override
    public final String toString(@Nullable final Event e, final boolean debug) {
        return "level " + (leveling.isTrue() ? "up" : leveling.isFalse() ? "down" : "change");
    }

}
