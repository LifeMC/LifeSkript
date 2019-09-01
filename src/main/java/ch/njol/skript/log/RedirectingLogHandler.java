/*
 *
 *     This file is part of Skript.
 *
 *    Skript is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Skript is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Skript. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 *   Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.log;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.eclipse.jdt.annotation.Nullable;

import java.util.logging.Level;

/**
 * Redirects the log to a {@link CommandSender}.
 *
 * @author Peter Güttinger
 */
public final class RedirectingLogHandler extends LogHandler {

    @Nullable
    private final CommandSender recipient;

    private final String prefix;

    private int numErrors;

    public RedirectingLogHandler(final CommandSender recipient, final @Nullable String prefix) {
        this.recipient = recipient == Bukkit.getConsoleSender() ? null : recipient;
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public LogResult log(final LogEntry entry) {
        if (recipient != null)
            recipient.sendMessage(prefix + entry.toString());
        else
            SkriptLogger.LOGGER.log(entry.getLevel(), prefix + entry.toString());
        if (entry.level.intValue() >= Level.SEVERE.intValue())
            numErrors++;
        return LogResult.DO_NOT_LOG;
    }

    public int numErrors() {
        return numErrors;
    }

}
