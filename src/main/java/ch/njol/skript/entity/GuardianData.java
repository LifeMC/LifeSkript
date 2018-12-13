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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

import org.bukkit.entity.Guardian;

public class GuardianData extends EntityData<Guardian> {
	
	static {
		if (Skript.classExists("org.bukkit.entity.Guardian")) {
			EntityData.register(GuardianData.class, "guardian", Guardian.class, 1, "normal guardian", "guardian", "elder guardian");
		}
	}
	
	private boolean isElder;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		isElder = matchedPattern == 2;
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean init(final Class<? extends Guardian> c, final Guardian e) {
		if (e != null)
			isElder = e.isElder();
		return true;
	}
	
	@Override
	public void set(final Guardian entity) {
		if (isElder)
			entity.setElder(true);
		
	}
	
	@Override
	protected boolean match(final Guardian entity) {
		return entity.isElder() == isElder;
	}
	
	@Override
	public Class<? extends Guardian> getType() {
		return Guardian.class;
	}
	
	@Override
	public EntityData getSuperType() {
		return new GuardianData();
	}
	
	@Override
	protected int hashCode_i() {
		return isElder ? 1 : 0;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof GuardianData))
			return false;
		final GuardianData other = (GuardianData) obj;
		return other.isElder == isElder;
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof GuardianData)
			return ((GuardianData) e).isElder == isElder;
		return false;
	}
	
}
