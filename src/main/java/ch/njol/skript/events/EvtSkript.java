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

package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.events.bukkit.SkriptStartEvent;
import ch.njol.skript.events.bukkit.SkriptStopEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("unchecked")
public class EvtSkript extends SelfRegisteringSkriptEvent {
	static {
		Skript.registerEvent("Server Start/Stop", EvtSkript.class, CollectionUtils.array(SkriptStartEvent.class, SkriptStopEvent.class), "(0¦server|1¦skript) (start|load|enable)", "(0¦server|1¦skript) (stop|unload|disable)").description("Called when the server starts or stops (actually, when Skript starts or stops, so a /reload will trigger these events as well).").examples("on Skript start", "on server stop").since("2.0");
	}
	
	private boolean isStart;
	
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		isStart = matchedPattern == 0;
		if (parser.mark == 0) {
			if (!SkriptConfig.disableStartStopEventWarnings.value())
				Skript.warning("Server start/stop events are actually called when Skript is started or stopped. It is thus recommended to use 'on Skript start/stop' instead.");
		}
		return true;
	}
	
	private final static Collection<Trigger> start = new ArrayList<Trigger>(), stop = new ArrayList<Trigger>();
	
	public static void onSkriptStart() {
		final Event e = new SkriptStartEvent();
		for (final Trigger t : start)
			t.execute(e);
	}
	
	public static void onSkriptStop() {
		final Event e = new SkriptStopEvent();
		for (final Trigger t : stop)
			t.execute(e);
	}
	
	@Override
	public void register(final Trigger t) {
		if (isStart)
			start.add(t);
		else
			stop.add(t);
	}
	
	@Override
	public void unregister(final Trigger t) {
		if (isStart)
			start.remove(t);
		else
			stop.remove(t);
	}
	
	@Override
	public void unregisterAll() {
		start.clear();
		stop.clear();
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "on server " + (isStart ? "start" : "stop");
	}
	
}
