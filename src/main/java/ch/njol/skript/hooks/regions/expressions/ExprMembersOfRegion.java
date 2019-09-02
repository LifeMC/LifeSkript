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

package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EmptyArrays;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;

/**
 * @author Peter Güttinger
 */
@Name("Region Members & Owners")
@Description({"A list of members or owners of a <a href='../classes/#region'>region</a>.", "This expression requires a supported regions plugin to be installed."})
@Examples({"on entering of a region:", "	message \"You're entering %region% whose owners are %owners of region%\"."})
@Since("2.1")
@RequiredPlugins("A region plugin")
public final class ExprMembersOfRegion extends SimpleExpression<OfflinePlayer> {
    static {
        Skript.registerExpression(ExprMembersOfRegion.class, OfflinePlayer.class, ExpressionType.PROPERTY, "(all|the|) (0¦members|1¦owner[s]) of [[the] region[s]] %regions%", "[[the] region[s]] %regions%'[s] (0¦members|1¦owner[s])");
    }

    private boolean owners;
    @SuppressWarnings("null")
    private Expression<Region> regions;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        regions = (Expression<Region>) exprs[0];
        owners = parseResult.mark == 1;
        return true;
    }

    @SuppressWarnings("null")
    @Override
    protected OfflinePlayer[] get(final Event e) {
        final ArrayList<OfflinePlayer> r = new ArrayList<>();
        for (final Region region : regions.getArray(e)) {
            r.addAll(owners ? region.getOwners() : region.getMembers());
        }
        return r.toArray(EmptyArrays.EMPTY_OFFLINEPLAYER_ARRAY);
    }

    @Override
    public boolean isSingle() {
        return owners && regions.isSingle() && !RegionsPlugin.hasMultipleOwners();
    }

    @Override
    public Class<? extends OfflinePlayer> getReturnType() {
        return OfflinePlayer.class;
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {
        return "the " + (owners ? "owner" + (isSingle() ? "" : "s") : "members") + " of " + regions.toString(e, debug);
    }

}
