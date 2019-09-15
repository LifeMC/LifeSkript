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

package ch.njol.skript.hooks.permission.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("All Groups")
@Description("All the groups a player can have. This expression requires Vault and a compatible permissions plugin to be installed.")
@Examples({"command /group <text>:",
        "\ttrigger:",
        "\t\tif argument is \"list\":",
        "\t\t\tsend \"%all groups%\""})
@Since("2.2.14")
@RequiredPlugins({"Vault", "A permission plugin that supports Vault"})
public final class ExprAllGroups extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprAllGroups.class, String.class, ExpressionType.SIMPLE, "all groups");
    }

    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
        if (!VaultHook.permission.hasGroupSupport()) {
            Skript.error(VaultHook.NO_GROUP_SUPPORT);
            return false;
        }
        return true;
    }

    @Override
    @Nullable
    protected String[] get(final Event e) {
        return VaultHook.permission.getGroups();
    }

    @Override
    public Class<String> getReturnType() {
        return String.class;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return "all groups";
    }

}
