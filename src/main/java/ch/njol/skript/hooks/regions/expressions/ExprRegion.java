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

package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.ExpressionType;

import org.bukkit.event.Event;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Region")
@Description({"The <a href='../classes/#region'>region</a> involved in an event.", "This expression requires a supported regions plugin to be installed."})
@Examples({"on region enter:", "	region is {forbidden region}", "	cancel the event"})
@Since("2.1")
public final class ExprRegion extends EventValueExpression<Region> {
	static {
		Skript.registerExpression(ExprRegion.class, Region.class, ExpressionType.SIMPLE, "[the] [event-]region");
	}
	
	public ExprRegion() {
		super(Region.class);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the region";
	}
	
}
