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

package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

/**
 * @author Peter Güttinger
 */
@Name("Length")
@Description("The length of a text, in number of characters.")
@Examples("set {_l} to length of the string argument")
@Since("2.1")
public class ExprLength extends SimplePropertyExpression<String, Integer> {
	static {
		register(ExprLength.class, Integer.class, "length", "strings");
	}
	
	@SuppressWarnings("null")
	@Override
	public Integer convert(final String s) {
		return s.length();
	}
	
	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "length";
	}
	
}
