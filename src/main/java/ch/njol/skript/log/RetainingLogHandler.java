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

package ch.njol.skript.log;

import ch.njol.skript.Skript;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Level;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public class RetainingLogHandler extends LogHandler {
	
	private final Deque<LogEntry> log = new LinkedList<LogEntry>();
	private int numErrors;
	
	boolean printedErrorOrLog;
	
	@Override
	public LogResult log(final LogEntry entry) {
		log.add(entry);
		if (entry.getLevel().intValue() >= Level.SEVERE.intValue())
			numErrors++;
		printedErrorOrLog = false;
		return LogResult.CACHED;
	}
	
	@Override
	public void onStop() {
		if (!printedErrorOrLog && Skript.testing())
			SkriptLogger.LOGGER.warning("Retaining log wasn't instructed to print anything at " + SkriptLogger.getCaller());
	}
	
	public final boolean printErrors() {
		return printErrors(null);
	}
	
	/**
	 * Prints all retained errors or the given one if no errors were retained.
	 * <p>
	 * This handler is stopped if not already done.
	 * 
	 * @param def Error to print if no errors were logged, can be null to not print any error if there are none
	 * @return Whether there were any errors
	 */
	public final boolean printErrors(final @Nullable String def) {
		return printErrors(def, ErrorQuality.SEMANTIC_ERROR);
	}
	
	public final boolean printErrors(final @Nullable String def, final ErrorQuality quality) {
		assert !printedErrorOrLog;
		printedErrorOrLog = true;
		stop();
		
		boolean hasError = false;
		for (final LogEntry e : log) {
			if (e.getLevel().intValue() >= Level.SEVERE.intValue()) {
				SkriptLogger.log(e);
				hasError = true;
			} else {
				e.discarded("not printed");
			}
		}
		
		if (!hasError && def != null)
			SkriptLogger.log(SkriptLogger.SEVERE, def);
		
		return hasError;
	}
	
	/**
	 * Sends all retained error messages to the given recipient.
	 * <p>
	 * This handler is stopped if not already done.
	 * 
	 * @param recipient
	 * @param def Error to send if no errors were logged, can be null to not print any error if there are none
	 * @return Whether there were any errors to send
	 */
	public final boolean printErrors(final CommandSender recipient, final @Nullable String def) {
		assert !printedErrorOrLog;
		printedErrorOrLog = true;
		stop();
		
		final boolean console = recipient == Bukkit.getConsoleSender(); // log as SEVERE instead of INFO
		
		boolean hasError = false;
		for (final LogEntry e : log) {
			if (e.getLevel().intValue() >= Level.SEVERE.intValue()) {
				if (console)
					SkriptLogger.LOGGER.severe(e.getMessage());
				else
					recipient.sendMessage(e.getMessage());
				e.logged();
				hasError = true;
			} else {
				e.discarded("not printed");
			}
		}
		
		if (!hasError && def != null) {
			if (console)
				SkriptLogger.LOGGER.severe(def);
			else
				recipient.sendMessage(def);
		}
		return hasError;
	}
	
	/**
	 * Prints all retained log messages.
	 * <p>
	 * This handler is stopped if not already done.
	 */
	public final void printLog() {
		assert !printedErrorOrLog;
		printedErrorOrLog = true;
		stop();
		SkriptLogger.logAll(log);
	}
	
	public boolean hasErrors() {
		return numErrors != 0;
	}
	
	@Nullable
	public LogEntry getFirstError() {
		for (final LogEntry e : log) {
			if (e.getLevel().intValue() >= Level.SEVERE.intValue())
				return e;
		}
		return null;
	}
	
	public LogEntry getFirstError(final String def) {
		for (final LogEntry e : log) {
			if (e.getLevel().intValue() >= Level.SEVERE.intValue())
				return e;
		}
		return new LogEntry(SkriptLogger.SEVERE, def);
	}
	
	/**
	 * Clears the list of retained log messages.
	 */
	public void clear() {
		for (final LogEntry e : log)
			e.discarded("cleared");
		log.clear();
		numErrors = 0;
	}
	
	public int size() {
		return log.size();
	}
	
	@SuppressWarnings("null")
	public Collection<LogEntry> getLog() {
		return Collections.unmodifiableCollection(log);
	}
	
	public Collection<LogEntry> getErrors() {
		final Collection<LogEntry> r = new ArrayList<LogEntry>();
		for (final LogEntry e : log) {
			if (e.getLevel().intValue() >= Level.SEVERE.intValue())
				r.add(e);
		}
		return r;
	}
	
	public int getNumErrors() {
		return numErrors;
	}
	
}
