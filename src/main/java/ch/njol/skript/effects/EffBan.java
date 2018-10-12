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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.effects;

import java.util.Date;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Ban")
@Description({"Bans/unbans a player or IP.",
		"Starting with Skript 2.1.1 and Bukkit 1.7.2 R0.4, one can also ban players with a reason."})
@Examples({"unban player",
		"ban \"127.0.0.1\"",
		"IP-ban the player because \"he is an idiot\""})
@Since("1.4, 2.1.1 (ban reason)")
public class EffBan extends Effect {
	
	public final static boolean hasBanList = Skript.classExists("org.bukkit.BanList");
	
	static {
		Skript.registerEffect(EffBan.class,
				"ban %strings/offlineplayers% [(by reason of|because [of]|on account of|due to) %-string%]", "unban %strings/offlineplayers%",
				"ban %players% by IP [(by reason of|because [of]|on account of|due to) %-string%]", "unban %players% by IP",
				"IP(-| )ban %players% [(by reason of|because [of]|on account of|due to) %-string%]", "(IP(-| )unban|un[-]IP[-]ban) %players%");
	}
	
	@SuppressWarnings("null")
	private Expression<?> players;
	@Nullable
	private Expression<String> reason;
	
	private boolean ban;
	private boolean ipBan;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		players = exprs[0];
		reason = exprs.length > 1 ? (Expression<String>) exprs[1] : null;
		if (!hasBanList && reason != null) {
			Skript.error("Bukkit 1.7.2 R0.4 or later is required to ban players with a reason.");
			return false;
		}
		ban = matchedPattern % 2 == 0;
		ipBan = matchedPattern >= 2;
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void execute(final Event e) {
		final String reason = this.reason != null ? this.reason.getSingle(e) : null; // don't check for null, just ignore an invalid reason
		final Date expires = null;
		final String source = "Skript ban effect";
		for (final Object o : players.getArray(e)) {
			if (o instanceof Player) {
				if (ipBan) {
					final String ip = ((Player) o).getAddress().getAddress().getHostAddress();
					if (hasBanList) {
						if (ban)
							Bukkit.getBanList(BanList.Type.IP).addBan(ip, reason, expires, source);
						else
							Bukkit.getBanList(BanList.Type.IP).pardon(ip);
					} else {
						if (ban)
							Bukkit.banIP(ip);
						else
							Bukkit.unbanIP(ip);
					}
				} else {
					if (hasBanList) {
						if (ban)
							Bukkit.getBanList(BanList.Type.NAME).addBan(((Player) o).getName(), reason, expires, source); // FIXME [UUID] ban UUID
						else
							Bukkit.getBanList(BanList.Type.NAME).pardon(((Player) o).getName());
					} else {
						((Player) o).setBanned(ban);
					}
				}
			} else if (o instanceof OfflinePlayer) {
				if (hasBanList) {
					if (ban)
						Bukkit.getBanList(BanList.Type.NAME).addBan(((OfflinePlayer) o).getName(), reason, expires, source);
					else
						Bukkit.getBanList(BanList.Type.NAME).pardon(((OfflinePlayer) o).getName());
				} else {
					((OfflinePlayer) o).setBanned(ban);
				}
			} else if (o instanceof String) {
				final String s = (String) o;
				if (hasBanList) {
					if (ban) {
						Bukkit.getBanList(BanList.Type.IP).addBan(s, reason, expires, source);
						Bukkit.getBanList(BanList.Type.NAME).addBan(s, reason, expires, source);
					} else {
						Bukkit.getBanList(BanList.Type.IP).pardon(s);
						Bukkit.getBanList(BanList.Type.NAME).pardon(s);
					}
				} else {
					if (ban)
						Bukkit.banIP(s);
					else
						Bukkit.unbanIP(s);
					Bukkit.getOfflinePlayer(s).setBanned(ban);
				}
			} else {
				assert false;
			}
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (ipBan ? "IP-" : "") + (ban ? "" : "un") + "ban " + players.toString(e, debug) + (reason != null ? " on account of " + reason.toString(e, debug) : "");
	}
	
}
