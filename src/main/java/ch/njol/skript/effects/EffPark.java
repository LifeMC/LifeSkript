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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.command.EffectCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Effect used for debugging the {@link ch.njol.skript.bukkitutil.SpikeDetector}
 *
 * @author TheDGOfficial
 * @since 2.2.16
 */
@Name("Park")
@Description({"Parks/locks/freezes/spikes the server for the given amount of time.", "This not something like the Delay effect, this will freeze the server entirely, no other task/code can run in that time. So be careful.", "An announcement will be sent to all operators, and the time is checked for sanity."})
@Examples("park the server for 5 seconds # DANGER: will FREEZE the server entirely for 5 seconds!")
@Since("2.2.16")
public final class EffPark extends Effect {
    private static boolean parkDisabled;

    static {
        Skript.registerEffect(EffPark.class, "(park|lock|freeze|spike) [the ]server for %timespan%");
    }

    @SuppressWarnings("null")
    private Expression<Timespan> duration;

    /**
     * the time to park for
     */
    private long milliSeconds;

    @Nullable
    private String script;

    private int line;

    private static final void announcePark(final Event e, final long milliSeconds, final @Nullable String scriptName, final int line, final boolean isFromEffectCommand, final CommandSender... sender) {
        final String announcementMessage = "Parking the server for " + milliSeconds + " milliseconds as requested by " + scriptName + ", line " + line + " in event " + e + " " + (isFromEffectCommand ? "by using an effect command " : "") + (sender.length == 1 && sender[0] != null ? "from " + sender[0].getName() : "");

        announce(announcementMessage);
    }

    private static final void announce(final String announcementMessage) {
        announce(announcementMessage, Skript::info, Skript::info);
    }

    private static final void announce(final String announcementMessage, final Consumer<String> logger, final BiConsumer<Player, String> handler) {
        logger.accept(announcementMessage);

        for (final Player player : PlayerUtils.getOnlinePlayers())
            if (player.isOp() || player.hasPermission("skript.admin") || player.hasPermission("skript.*") || player.hasPermission("skript.seeParkingAnnouncements"))
                handler.accept(player, announcementMessage);
    }

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final SkriptParser.ParseResult parseResult) {
        if (parkDisabled) { // How did this happen?
            Skript.error("Parking is disabled currently for server safety, sorry");
            return false;
        }

        duration = (Expression<Timespan>) exprs[0];

        if (!duration.isSingle()) { // Ehm, for sanity? or what?
            Skript.error("Multiple durations given");
            return false;
        }

        if (duration.isDefault()) { // Defaults may be danger for an effect like this
            Skript.error("Park effect only accepts explicit durations");
            return false;
        }

        if (!(duration instanceof Literal)) { // Non-literals can't be checked for sanity
            // As such they may contain a duration which is larger than the spigot watchdog timeout, so we must check it.

            Skript.error("Only literal values are allowed here");
            return false;
        }

        final Literal<Timespan> timespan = (Literal<Timespan>) duration;
        milliSeconds = timespan.getSingle().getMilliSeconds();

        if (milliSeconds < 0L) {
            Skript.error("Parking negative amount of times is not possible");
            return false;
        }

        if (milliSeconds == 0L) { // No point sleeping for 0 milliseconds
            Skript.error("Parking for no time is redundant and may cause issues");
            return false;
        }

        if (milliSeconds > 30_000L) { // Spigot watchdog default is 60 but we want to be safe
            Skript.error("Can't park more than 30 seconds, it's too dangerous!");
            return false;
        }

        if (ScriptLoader.currentScript != null)
            script = ScriptLoader.currentScript.getFileName();

        if (SkriptLogger.getNode() != null)
            line = SkriptLogger.getNode().getLine();

        return true;
    }

    @Override
    protected final void execute(final Event e) {
        if (parkDisabled)
            return;

        if (e instanceof CommandEvent) {
            announcePark(e, milliSeconds, script, line, e instanceof EffectCommandEvent, ((CommandEvent) e).getSender());
        } else
            announcePark(e, milliSeconds, script, line, false);

        // It will always freeze the main thread, even if called from another thread.
        Bukkit.getScheduler().runTask(Skript.getInstance(), () -> {
            try {
                Thread.sleep(milliSeconds);
            } catch (final InterruptedException ie) {
                announce("Interrupted while parking using park effect, disabling the park effect!", Skript::error, Skript::error);
                parkDisabled = true;
                Thread.currentThread().interrupt();
            }
        });
    }

    @Override
    public final String toString(final @Nullable Event e, final boolean debug) {
        return "park the server for " + duration.toString(e, debug);
    }

}
