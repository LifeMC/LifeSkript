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

package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.compat.Compatibility;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.StringUtils;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public class MinecartData extends EntityData<Minecart> {
	
	@Nullable
	static final Class<? extends Minecart> storageMinecart = Compatibility.getClass("org.bukkit.entity.StorageMinecart", "org.bukkit.entity.minecart.StorageMinecart");
	
	@Nullable
	static final Class<? extends Minecart> poweredMinecart = Compatibility.getClass("org.bukkit.entity.PoweredMinecart", "org.bukkit.entity.minecart.PoweredMinecart");
	
	@SuppressWarnings("null")
	private enum MinecartType {
		
		ANY(Minecart.class, "minecart"),
		
		NORMAL(Skript.classExists("org.bukkit.entity.minecart.RideableMinecart") ? RideableMinecart.class : Minecart.class, "regular minecart"),
		
		STORAGE((Class<? extends Minecart>) Compatibility.getClass("org.bukkit.entity.StorageMinecart", "org.bukkit.entity.minecart.StorageMinecart"), "storage minecart"), POWERED((Class<? extends Minecart>) Compatibility.getClass("org.bukkit.entity.PoweredMinecart", "org.bukkit.entity.minecart.PoweredMinecart"), "powered minecart"),
		
		HOPPER(Skript.classExists("org.bukkit.entity.minecart.HopperMinecart") ? HopperMinecart.class : null, "hopper minecart"), EXPLOSIVE(Skript.classExists("org.bukkit.entity.minecart.ExplosiveMinecart") ? ExplosiveMinecart.class : null, "explosive minecart"), SPAWNER(Skript.classExists("org.bukkit.entity.minecart.SpawnerMinecart") ? SpawnerMinecart.class : null, "spawner minecart");
		
		@Nullable
		final Class<? extends Minecart> c;
		private final String codeName;
		
		MinecartType(final @Nullable Class<? extends Minecart> c, final String codeName) {
			this.c = c;
			this.codeName = codeName;
		}
		
		@Override
		public String toString() {
			return codeName;
		}
		
		public static String[] codeNames;
		static {
			final ArrayList<String> cn = new ArrayList<String>();
			for (final MinecartType t : values()) {
				if (t.c != null)
					cn.add(t.codeName);
			}
			codeNames = cn.toArray(StringUtils.EMPTY_STRING_ARRAY);
		}
	}
	
	static {
		register(MinecartData.class, "minecart", Minecart.class, 0, MinecartType.codeNames);
		
		Variables.yggdrasil.registerSingleClass(MinecartType.class, "MinecartType");
	}
	
	private MinecartType type = MinecartType.ANY;
	
	public MinecartData() {}
	
	public MinecartData(final MinecartType type) {
		this.type = type;
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		type = MinecartType.values()[matchedPattern];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean init(final @Nullable Class<? extends Minecart> c, final @Nullable Minecart e) {
		final MinecartType[] ts = MinecartType.values();
		for (int i = ts.length - 1; i >= 0; i--) {
			final Class<?> mc = ts[i].c;
			if (mc == null)
				continue;
			if (e == null ? mc.isAssignableFrom(c) : mc.isInstance(e)) {
				type = ts[i];
				return true;
			}
		}
		assert false;
		return false;
	}
	
	@Override
	public void set(final Minecart entity) {}
	
	@Override
	@SuppressWarnings("null")
	public boolean match(final Minecart entity) {
		if (type == MinecartType.NORMAL && type.c == Minecart.class) // pre-1.5
			return !(poweredMinecart.isAssignableFrom(entity.getClass()) || storageMinecart.isAssignableFrom(entity.getClass()));
		return type.c != null && type.c.isInstance(entity);
	}
	
	@Override
	public Class<? extends Minecart> getType() {
		return type.c != null ? type.c : Minecart.class;
	}
	
	@Override
	protected int hashCode_i() {
		return type.hashCode();
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof MinecartData))
			return false;
		final MinecartData other = (MinecartData) obj;
		return type == other.type;
	}
	
//		return type.name();
	@Override
	protected boolean deserialize(final String s) {
		try {
			type = MinecartType.valueOf(s);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof MinecartData)
			return type == MinecartType.ANY || ((MinecartData) e).type == type;
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new MinecartData(type);
	}
	
}
