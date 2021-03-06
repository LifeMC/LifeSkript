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
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Region Contains")
@Description({"Checks whatever a location is contained in a particular <a href='../classes/#region'>region</a>.", "This condition requires a supported regions plugin to be installed."})
@Examples({"player is in the region {regions::3}", "on region enter:", "	region contains {flags.%world%.red}", "	message \"The red flag is near!\""})
@Since("2.1")
@RequiredPlugins("A region plugin")
public final class CondRegionContains extends Condition {
    static {
        Skript.registerCondition(CondRegionContains.class, CondRegionContains::new, "[[the] region] %regions% contain[s] %directions% %locations%", "%locations% (is|are) ([contained] in|part of) [[the] region] %regions%", "[[the] region] %regions% (do|does)(n't| not) contain %directions% %locations%", "%locations% (is|are)(n't| not) (contained in|part of) [[the] region] %regions%");
    }

    @SuppressWarnings("null")
    private Expression<Location> locations;
    @SuppressWarnings("null")
    private Expression<Region> regions;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        if (exprs.length == 3) {
            regions = (Expression<Region>) exprs[0];
            locations = Direction.combine((Expression<Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
        } else {
            regions = (Expression<Region>) exprs[1];
            locations = (Expression<Location>) exprs[0];
        }
        setNegated(matchedPattern >= 2);
        return true;
    }

    @Override
    public boolean check(final Event e) {
        return regions.check(e, r -> locations.check(e, r::contains, isNegated()));
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return regions.toString(e, debug) + " contain" + (regions.isSingle() ? "s" : "") + ' ' + locations.toString(e, debug);
    }

}
