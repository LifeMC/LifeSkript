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
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.hooks.regions.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class Region implements YggdrasilExtendedSerializable {
	static {
		Classes.registerClass(new ClassInfo<Region>(Region.class, "region").name("Region").description("A region of a regions plugin. Skript currently supports WorldGuard, Factions, GriefPrevention and PreciousStones.", "Please note that some regions plugins do not have named regions, some use numerical ids to identify regions, and some may have regions with the same name in different worlds, " + "thus using regions like \"region name\" in scripts may or may not work.").usage("\"region name\"").examples("").after("string", "world", "offlineplayer", "player").since("2.1").user("regions?").parser(new Parser<Region>() {
			@Override
			@Nullable
			public Region parse(String s, final ParseContext context) {
				final boolean quoted;
				switch (context) {
					case DEFAULT:
					case EVENT:
					case SCRIPT:
						quoted = true;
						break;
					case COMMAND:
					case CONFIG:
						quoted = false;
						break;
					default:
						assert false;
						return null;
				}
				if (!VariableString.isQuotedCorrectly(s, quoted))
					return null;
				s = VariableString.unquote(s, quoted);
				Region r = null;
				for (final World w : Bukkit.getWorlds()) {
					@SuppressWarnings("null")
					final Region r2 = RegionsPlugin.getRegion(w, s);
					if (r2 == null)
						continue;
					if (r != null) {
						Skript.error("Multiple regions with the name '" + s + "' exist.");
						return null;
					}
					r = r2;
				}
				return r;
			}
			
			@Override
			public String toString(final Region r, final int flags) {
				return r.toString();
			}
			
			@Override
			public String toVariableNameString(final Region r) {
				return r.toString();
			}
			
			@Override
			public String getVariableNamePattern() {
				return ".*";
			}
		}).serializer(new YggdrasilSerializer<Region>() {
			@Override
			public boolean mustSyncDeserialization() {
				return true;
			}
		}));
	}
	
	public abstract boolean contains(Location l);
	
	public abstract boolean isMember(OfflinePlayer p);
	
	public abstract Collection<OfflinePlayer> getMembers();
	
	public abstract boolean isOwner(OfflinePlayer p);
	
	public abstract Collection<OfflinePlayer> getOwners();
	
	public abstract Iterator<Block> getBlocks();
	
	@Override
	public abstract String toString();
	
	public abstract RegionsPlugin<?> getPlugin();
	
	@Override
	public abstract boolean equals(@Nullable Object o);
	
	@Override
	public abstract int hashCode();
	
}
