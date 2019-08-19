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

package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.AsyncEffect;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;

/**
 * @author Peter Güttinger
 */
@Name("Log")
@Description({"Writes text into a .log file. Skript will write these files to /plugins/Skript/logs.", "NB: Using 'server.log' as the log file will write to the default server log. Omitting the log file altogether will log the message as '[Skript] [&lt;script&gt;.sk] &lt;message&gt;' in the server log."})
@Examples({"on place of TNT:", "	log \"%player% placed TNT in %world% at %location of block%\" to \"tnt/placement.log\""})
@Since("2.0")
public final class EffLog extends AsyncEffect {
    static final HashMap<String, PrintWriter> writers = new HashMap<>();
    private static final boolean flushAllLogsOnShutdownOnly = Boolean.getBoolean("skript.flushAllLogsOnShutdownOnly"); //FIXME test this
    private static final File logsFolder = new File(Skript.getInstance().getDataFolder(), "logs");

    static {
        Skript.closeOnDisable(() -> {
            for (final PrintWriter pw : writers.values()) {
                pw.flush(); // Guarantee everything is printed
                pw.close(); // Close it afterwards
            }
        });

        Skript.registerEffect(EffLog.class, "log %strings% [(to|in) [file[s]] %-strings%]");
    }

    @SuppressWarnings("null")
    private Expression<String> messages;
    @Nullable
    private Expression<String> files;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
        messages = (Expression<String>) exprs[0];
        files = (Expression<String>) exprs[1];
        return true;
    }

    @SuppressWarnings("resource")
    @Override
    protected final void execute(final Event e) {
        PrintWriter w = null;

        for (final String message : messages.getArray(e)) {
            if (files != null) {
                for (String s : files.getArray(e)) {
                    s = s.toLowerCase(Locale.ENGLISH).replace("\\", "/");
                    if (!s.endsWith(".log"))
                        s += ".log";
                    if ("server.log".equals(s)) {
                        SkriptLogger.LOGGER.log(Level.INFO, message);
                        continue;
                    }
                    w = writers.get(s);
                    if (w == null) {
                        final File f = new File(logsFolder, s); // REMIND what if s contains '..'?
                        try {
                            f.getParentFile().mkdirs();
                            w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), StandardCharsets.UTF_8)));
                            writers.put(s, w);
                        } catch (final IOException ex) {
                            Skript.error("Cannot write to log file '" + s + "' (" + f.getPath() + "): " + ExceptionUtils.toString(ex));
                            return;
                        }
                    }
                    w.println("[" + SkriptConfig.formatDate(System.currentTimeMillis()) + "] " + message);
                }
            } else {
                final Trigger t = getTrigger();
                final File script = t == null ? null : t.getScript();
                Skript.info("[" + (script != null ? script.getName() : "---") + "] " + message);
            }
        }

        // Specifying this system property can speedup log write performance, but may cause loss of
        // log files if server shutdowns, so it is not the default behaviour, and it must be used carefully.
        if (!flushAllLogsOnShutdownOnly && w != null)
            w.flush();
    }

    @Override
    public final String toString(final @Nullable Event e, final boolean debug) {
        return "log " + messages.toString(e, debug) + (files != null ? " to " + files.toString(e, debug) : "");
    }
}
