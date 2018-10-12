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

package ch.njol.skript.lang.function;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;

/**
 * @author Peter Güttinger
 */
public abstract class JavaFunction<T> extends Function<T> {
	
	public JavaFunction(final String name, final Parameter<?>[] parameters, final ClassInfo<T> returnType, final boolean single) {
		super(name, parameters, returnType, single);
	}
	
	@Override
	@Nullable
	public abstract T[] execute(FunctionEvent e, Object[][] params);
	
	@Nullable
	private String[] description = null;
	@Nullable
	private String[] examples = null;
	@Nullable
	private String since = null;
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param description
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> description(final String... description) {
		assert this.description == null;
		this.description = description;
		return this;
	}
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param examples
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> examples(final String... examples) {
		assert this.examples == null;
		this.examples = examples;
		return this;
	}
	
	/**
	 * Only used for Skript's documentation.
	 * 
	 * @param since
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> since(final String since) {
		assert this.since == null;
		this.since = since;
		return this;
	}
	
	@Nullable
	public String[] getDescription() {
		return description;
	}
	
	@Nullable
	public String[] getExamples() {
		return examples;
	}
	
	@Nullable
	public String getSince() {
		return since;
	}
	
}
