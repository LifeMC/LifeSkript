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
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Is Member/Owner of Region")
@Description({"Checks whatever a player is a member or owner of a particular region.", "This condition requires a supported regions plugin to be installed."})
@Examples({"on region enter:", "	player is the owner of the region", "	message \"Welcome back to %region%!\"", "	send \"%player% just entered %region%!\" to all members of the region"})
@Since("2.1")
@RequiredPlugins("A region plugin")
public final class CondIsMember extends Condition {
    static {
        Skript.registerCondition(CondIsMember.class, CondIsMember::new, "%offlineplayers% (is|are) (0¦[a] member|1¦[(the|an)] owner) of [[the] region] %regions%", "%offlineplayers% (is|are)(n't| not) (0¦[a] member|1¦[(the|an)] owner) of [[the] region] %regions%");
    }

    @SuppressWarnings("null")
    private Expression<Region> regions;
    private boolean owner;
    @SuppressWarnings("null")
    private Expression<OfflinePlayer> players;

    @SuppressWarnings({"null", "unchecked"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        players = (Expression<OfflinePlayer>) exprs[0];
        regions = (Expression<Region>) exprs[1];
        owner = parseResult.mark == 1;
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(final Event e) {
        return players.check(e, p -> regions.check(e, r -> owner ? r.isOwner(p) : r.isMember(p), isNegated()));
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return players.toString(e, debug) + ' ' + (players.isSingle() ? "is" : "are") + (isNegated() ? " not" : "") + ' ' + (owner ? "owner" : "member") + (players.isSingle() ? "" : "s") + " of " + regions.toString(e, debug);
    }
}
