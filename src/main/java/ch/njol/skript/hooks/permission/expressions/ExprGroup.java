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
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Name("Group")
@Description("The primary group or all groups of a player. This expression requires Vault and a compatible permissions plugin to be installed.")
@Examples({"on join:",
        "\tbroadcast \"%group of player%\" # this is the player's primary group",
        "\tbroadcast \"%groups of player%\" # this is all of the player's groups"})
@Since("2.2.14")
@RequiredPlugins({"Vault", "A permission plugin that supports Vault"})
public class ExprGroup extends SimpleExpression<String> {

    static {
        PropertyExpression.register(ExprGroup.class, String.class, "group[(1¦s)]", "offlineplayers");
    }

    private boolean primary;
    @Nullable
    private Expression<OfflinePlayer> players;

    @SuppressWarnings({"unchecked"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
        if (!VaultHook.permission.hasGroupSupport()) {
            Skript.error(VaultHook.NO_GROUP_SUPPORT);
            return false;
        }
        players = (Expression<OfflinePlayer>) exprs[0];
        primary = parseResult.mark == 0;
        return true;
    }

    @SuppressWarnings("null")
    @Override
    protected String[] get(final Event e) {
        final List<String> groups = new ArrayList<>();
        for (final OfflinePlayer player : players.getArray(e)) {
            if (primary)
                groups.add(VaultHook.permission.getPrimaryGroup(null, player));
            else
                Collections.addAll(groups, VaultHook.permission.getPlayerGroups(null, player));
        }
        return groups.toArray(new String[0]);
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(final Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.ADD ||
                mode == Changer.ChangeMode.REMOVE ||
                mode == Changer.ChangeMode.SET ||
                mode == Changer.ChangeMode.DELETE ||
                mode == Changer.ChangeMode.RESET) {
            return new Class<?>[] {String[].class};
        }
        return null;
    }

    @Override
    @SuppressWarnings("null")
    public void change(final Event e, @Nullable final Object[] delta, final Changer.ChangeMode mode) {
        final Permission api = VaultHook.permission;
        for (final OfflinePlayer player : players.getArray(e)) {
            switch (mode) {
                case ADD:
                    for (final Object o : delta)
                        api.playerAddGroup(null, player, (String) o);
                    break;
                case REMOVE:
                    for (final Object o : delta)
                        api.playerRemoveGroup(null, player, (String) o);
                    break;
                case REMOVE_ALL:
                case RESET:
                case DELETE:
                case SET:
                    for (final String group : api.getPlayerGroups(null, player)) {
                        api.playerRemoveGroup(null, player, group);
                    }
                    if (mode == Changer.ChangeMode.SET) {
                        for (final Object o : delta) {
                            api.playerAddGroup(null, player, (String) o);
                        }
                    }
            }
        }
    }

    @SuppressWarnings("null")
    @Override
    public boolean isSingle() {
        return players.isSingle() && primary;
    }

    @SuppressWarnings("null")
    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @SuppressWarnings("null")
    @Override
    public String toString(final Event e, final boolean debug) {
        return "group" + (primary ? "" : "s") + " of " + players.toString(e, debug);
    }

}
