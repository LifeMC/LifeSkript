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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Cancel Event")
@Description("Cancels the event (e.g. prevent blocks from being placed, or damage being taken).")
@Examples({"on damage:", "	victim is a player", "	victim has the permission \"skript.god\"", "	cancel the event"})
@Since("1.0")
public final class EffCancelEvent extends Effect {
    static {
        Skript.registerEffect(EffCancelEvent.class, "(disallow|cancel) [the] [current] event", "(allow|uncancel) [the] [current] event");
    }

    private boolean cancel;

    @Nullable
    private String script;

    private int line;

    @Override
    @SuppressWarnings("null")
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
        if (isDelayed == Kleenean.TRUE) {
            Skript.error("Can't cancel an event anymore after it has been already passed, remove the wait statements!", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        if (ScriptLoader.isCurrentEvent(PlayerLoginEvent.class)) {
            Skript.error("A connect event cannot be cancelled, but the player may be kicked ('kick player because of \"...\"')", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        cancel = matchedPattern == 0;
        final Class<? extends Event>[] es = ScriptLoader.getCurrentEvents();
        if (es == null || es.length < 1) {
            Skript.error("The cancel event effect only usable in an event", ErrorQuality.SEMANTIC_ERROR);
            return false;
        }
        final Class<? extends Event> e = es[0];
        if (Cancellable.class.isAssignableFrom(e) || InventoryInteractEvent.class.isAssignableFrom(e) || PlayerInteractEvent.class.isAssignableFrom(e) || BlockCanBuildEvent.class.isAssignableFrom(e) || PlayerDropItemEvent.class.isAssignableFrom(e)) {
            if (Skript.logHigh()) {
                script = ScriptLoader.currentScript.getFileName();
                line = SkriptLogger.getNode().getLine();
            }
            return true;
        }
        Skript.error(Utils.A(ScriptLoader.getCurrentEventName()) + " event cannot be cancelled", ErrorQuality.SEMANTIC_ERROR);
        return false;
    }

    @Override
    public void execute(final Event e) {
        boolean cancelled = false;

        if (e instanceof Cancellable) {
            ((Cancellable) e).setCancelled(cancel);
            cancelled = true;
        }

        if (e instanceof InventoryInteractEvent) {
            ((InventoryInteractEvent) e).setResult(cancel ? Result.DENY : Result.DEFAULT);
            cancelled = true;
        }

        if (e instanceof PlayerInteractEvent) {
            ((PlayerInteractEvent) e).setUseItemInHand(cancel ? Result.DENY : Result.DEFAULT);
            ((PlayerInteractEvent) e).setUseInteractedBlock(cancel ? Result.DENY : Result.DEFAULT);
            cancelled = true;
        }

        if (e instanceof BlockCanBuildEvent) {
            ((BlockCanBuildEvent) e).setBuildable(!cancel);
            cancelled = true;
        }

        if (e instanceof PlayerDropItemEvent) {
            PlayerUtils.updateInventory(((PlayerDropItemEvent) e).getPlayer());
            cancelled = true;
        }

        assert cancelled : "The " + e.getClass().getCanonicalName() + " event cannot be cancelled!";

        //noinspection ConstantConditions
        if (cancelled && Skript.logHigh())
            Skript.info(e.getClass().getSimpleName() + " event was cancelled by a script" + (script != null ? " (" + script + ", line " + line + ')' : ""));
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return (cancel ? "" : "un") + "cancel the event";
    }

}
