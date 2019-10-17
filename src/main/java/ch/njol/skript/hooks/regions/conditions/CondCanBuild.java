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

package ch.njol.skript.hooks.regions.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Can Build")
@Description({"Tests whatever a player is allowed to build at a certain location.", "This condition requires a supported <a href='../classes/#region'>regions</a> plugin to be installed."})
@Examples({"command /setblock <material>:", "	description: set the block at your crosshair to a different type", "	trigger:", "		player cannot build at the targeted block:", "			message \"You do not have permission to change blocks there!\"", "			stop", "		set the targeted block to argument"})
@Since("2.0")
@RequiredPlugins("A region plugin")
public final class CondCanBuild extends Condition {
    static {
        Skript.registerCondition(CondCanBuild.class, CondCanBuild::new, "%players% (can|(is|are) allowed to) build %directions% %locations%", "%players% (can('t|not)|(is|are)(n't| not) allowed to) build %directions% %locations%");
    }

    @SuppressWarnings("null")
    private Expression<Location> locations;
    @SuppressWarnings("null")
    private Expression<Player> players;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        players = (Expression<Player>) exprs[0];
        locations = Direction.combine((Expression<Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(final Event e) {
        return players.check(e, p -> locations.check(e, l -> RegionsPlugin.canBuild(p, l), isNegated()));
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return players.toString(e, debug) + " can build " + locations.toString(e, debug);
    }

}
