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

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author TheDGOfficial
 */
@Name("Plugin Version")
@Description("Gets version of a specific plugin.")
@Examples({"on load:\n\tmessage \"The version of Skript is %version of plugin \"Skript\"%"})
@Since("2.2-Fixes-V12")
public final class ExprPluginVersion extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprPluginVersion.class, String.class, ExpressionType.SIMPLE, "version of plugin %string%");
	}
	
	private @Nullable Expression<String> pluginName;
	
	/**
	 * @see ch.njol.skript.lang.Expression#isSingle()
	 */
	public boolean isSingle() {
		return true;
	}

	/**
	 * @see ch.njol.skript.lang.Expression#getReturnType()
	 */
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	/**
	 * @see ch.njol.skript.lang.SyntaxElement#init(ch.njol.skript.lang.Expression[], int, ch.njol.util.Kleenean, ch.njol.skript.lang.SkriptParser.ParseResult)
	 */
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		this.pluginName = (Expression<String>) exprs[0];
		return true;
	}

	/**
	 * @see ch.njol.skript.lang.Debuggable#toString(org.bukkit.event.Event, boolean)
	 */
	@SuppressWarnings("null")
	public String toString(@Nullable final Event e, final boolean debug) {
		return "version of plugin \"" + pluginName.getSingle(e) + "\" (" + (get(e) != null ? get(e) : "".trim()) + ")";
	}

	/**
	 * @see ch.njol.skript.lang.util.SimpleExpression#get(org.bukkit.event.Event)
	 */
	@Override
	@Nullable
	@SuppressWarnings("null")
	protected String[] get(final Event e) {
		String version = null;
		
		final Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName.getSingle(e));
		
		if (plugin != null)
			version = plugin.getDescription().getVersion();
		
		if (version == null)
			return null;
		
		return new String[] { version };
	}
	
}
