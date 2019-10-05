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

package ch.njol.skript.expressions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("UUID")
@Description({"The UUID of a player, entity or a world.", "In the future there will be an option to use a player's UUID instead of the name in variable names (i.e. when %player% is used), but for now this can be used.", "<em>Please note that this expression does not work for offline players if you are under 1.8!</em>"})
// TODO [UUID] update documentation after release. Add note about requiring Bukkit 1.7.(9/10)?
@Examples({"# prevents people from joining the server if they use the name of a player", "# who has played on this server at least once since this script has been added", "on login:", "	{uuids.%name of player%} exists:", "		{uuids.%name of player%} is not UUID of player", "		kick player due to \"Someone with your name has played on this server before\"", "	else:", "		set {uuids.%name of player%} to UUID of player"})
@Since("2.1.2, 2.2 (offline players' UUIDs), 2.2.16 (entity UUIDs)")
public final class ExprUUID extends SimplePropertyExpression<Object, String> {
    static {
        register(ExprUUID.class, String.class, "UUID", (Skript.offlineUUIDSupported ? "offlineplayers" : "players") + "/entities/worlds");
    }

    @Nullable
    private String script;
    private int line;

    @Override
    public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
        final Config currentScript = ScriptLoader.currentScript;
        if (currentScript != null)
            script = currentScript.getFileName();
        final Node currentNode = SkriptLogger.getNode();
        if (currentNode != null)
            line = currentNode.getLine();
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    @Nullable
    public final String convert(final Object o) {
        if (o instanceof OfflinePlayer) {
            if (Skript.offlineUUIDSupported) {
                try {
                    return ((OfflinePlayer) o).getUniqueId().toString();
                } catch (final UnsupportedOperationException e) {
                    // Some plugins (ProtocolLib) try to emulate offline players, but fail miserably
                    // They will throw this exception... and somehow server may freeze when this happens
                    Skript.warning("A script tried to get uuid of an offline player, which was faked by another plugin (probably ProtocolLib). (" + script + ", line " + line + ')');
                    if (Skript.testing() || Skript.debug())
                        Skript.exception(e, "Can't get UUID of a fake player");
                    return null;
                }
            }
            return ((Player) o).getUniqueId().toString();
        }
        if (o instanceof Entity) {
            return ((Entity) o).getUniqueId().toString();
        }
        if (o instanceof World) {
            return ((World) o).getUID().toString();
        }
        return null;
    }

    @Override
    public final Class<String> getReturnType() {
        return String.class;
    }

    @Override
    protected final String getPropertyName() {
        return "UUID";
    }

}
