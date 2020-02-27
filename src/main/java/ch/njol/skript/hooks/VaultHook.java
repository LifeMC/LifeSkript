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

package ch.njol.skript.hooks;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.RequiredPlugins;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.IOException;

/**
 * @author Peter Güttinger
 */
@RequiredPlugins("Vault")
public final class VaultHook extends Hook<Vault> {

    public static final String NO_GROUP_SUPPORT = "The permissions plugin you are using does not support groups.";
    private static final boolean oldVault = !Skript.methodExists(Economy.class, "getBalance", OfflinePlayer.class) ||
            !Skript.methodExists(Economy.class, "withdrawPlayer", OfflinePlayer.class, double.class) ||
            !Skript.methodExists(Economy.class, "depositPlayer", OfflinePlayer.class, double.class);
    @SuppressWarnings("null")
    public static Economy economy;
    @SuppressWarnings("null")
    public static Chat chat;
    @SuppressWarnings("null")
    public static Permission permission;

    static {
        if (oldVault)
            Skript.warning("You are using an old version of Vault, economy operations may act slow.");
    }

    public VaultHook() throws IOException {
	}

    @SuppressWarnings("deprecation")
    public static final double getBalance(final OfflinePlayer player) {
        if (oldVault)
            return economy.getBalance(player.getName());

        return economy.getBalance(player);
    }

    @SuppressWarnings("deprecation")
    public static final void remove(final OfflinePlayer player,
                                    final double amount) {
        if (oldVault) {
            economy.withdrawPlayer(player.getName(), amount);
            return;
        }

        economy.withdrawPlayer(player, amount);
    }

    @SuppressWarnings("deprecation")
    public static final void add(final OfflinePlayer player,
                                 final double amount) {
        if (oldVault) {
            economy.depositPlayer(player.getName(), amount);
            return;
        }

        economy.depositPlayer(player, amount);
    }

    @SuppressWarnings("null")
    @Override
    protected boolean init() {
        economy = Bukkit.getServicesManager().getRegistration(Economy.class) == null ? null : Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        chat = Bukkit.getServicesManager().getRegistration(Chat.class) == null ? null : Bukkit.getServicesManager().getRegistration(Chat.class).getProvider();
        permission = Bukkit.getServicesManager().getRegistration(Permission.class) == null ? null : Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
        return economy != null || chat != null || permission != null;
    }

    @SuppressWarnings("null")
    @Override
    protected void loadClasses() throws IOException {
        if (economy != null)
            Skript.getAddonInstance().loadClasses(getClass().getPackage().getName() + ".economy");
        if (chat != null)
            Skript.getAddonInstance().loadClasses(getClass().getPackage().getName() + ".chat");
        if (permission != null)
            Skript.getAddonInstance().loadClasses(getClass().getPackage().getName() + ".permission");
    }

    @Override
    public String getName() {
        return "Vault";
    }

}
