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

package ch.njol.skript.hooks.regions.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.EventExecutor;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @author Peter Güttinger
 */
@RequiredPlugins("A region plugin")
public final class EvtRegionBorder extends SelfRegisteringSkriptEvent {
    private static final Collection<Trigger> triggers = new ArrayList<>();
    // even WorldGuard doesn't have events, and this way all region plugins are supported for sure.
    private static final EventExecutor ee = new EventExecutor() {
        @Nullable
        Event last;

        @SuppressWarnings({"null", "unlikely-arg-type"})
        @Override
        public void execute(final @Nullable Listener listener, final Event event) throws EventException {
            if (event == last)
                return;
            last = event;
            final PlayerMoveEvent e = (PlayerMoveEvent) event;
            final Location to = e.getTo(), from = e.getFrom();
            if (to != null && to.equals(from))
                return;
            if (to != null && from != null && to.getWorld().equals(from.getWorld()) && to.distanceSquared(from) < 2)
                return;
            final Set<? extends Region> oldRs = RegionsPlugin.getRegionsAt(from), newRs = RegionsPlugin.getRegionsAt(to);
            for (final Region r : oldRs) {
                if (!newRs.contains(r))
                    callEvent(r, e, false);
            }
            for (final Region r : newRs) {
                if (!oldRs.contains(r))
                    callEvent(r, e, true);
            }
        }
    };
    private static boolean registered;

    static {
        Skript.registerEvent("Region Enter/Leave", EvtRegionBorder.class, RegionBorderEvent.class, "(0¦enter[ing]|1¦leav(e|ing)|1¦exit[ing]) [of] ([a] region|[[the] region] %-regions%)", "region (0¦enter[ing]|1¦leav(e|ing)|1¦exit[ing])").description("Called when a player enters or leaves a <a href='../classes/#region'>region</a>.", "This event requires a supported regions plugin to be installed.").examples("on region exit:", "	message \"Leaving %region%.\"").since("2.1");
        EventValues.registerEventValue(RegionBorderEvent.class, Region.class, new Getter<Region, RegionBorderEvent>() {
            @Override
            public Region get(final RegionBorderEvent e) {
                return e.getRegion();
            }
        }, 0);
        EventValues.registerEventValue(RegionBorderEvent.class, Player.class, new Getter<Player, RegionBorderEvent>() {
            @Override
            public Player get(final RegionBorderEvent e) {
                return e.getPlayer();
            }
        }, 0);
    }

    private boolean enter;
    @Nullable
    private Literal<Region> regions;

    static final void callEvent(final Region r, final PlayerMoveEvent me, final boolean enter) {
        final Player p = me.getPlayer();
        assert p != null;
        final RegionBorderEvent e = new RegionBorderEvent(!Bukkit.isPrimaryThread(), r, p, enter);
        e.setCancelled(me.isCancelled());
        for (final Trigger t : triggers) {
            if (((EvtRegionBorder) t.getEvent()).applies(e))
                t.execute(e);
        }
        me.setCancelled(e.isCancelled());
    }

    private static final void register() {
        if (registered)
            return;
        Bukkit.getPluginManager().registerEvent(PlayerMoveEvent.class, new Listener() {
            /* empty */
        }, SkriptConfig.defaultEventPriority.value(), ee, Skript.getInstance(), true);
        Bukkit.getPluginManager().registerEvent(PlayerTeleportEvent.class, new Listener() {
            /* empty */
        }, SkriptConfig.defaultEventPriority.value(), ee, Skript.getInstance(), true);
        Bukkit.getPluginManager().registerEvent(PlayerPortalEvent.class, new Listener() {
            /* empty */
        }, SkriptConfig.defaultEventPriority.value(), ee, Skript.getInstance(), true);
        registered = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parseResult) {
        enter = parseResult.mark == 0;
        regions = args.length == 0 ? null : (Literal<Region>) args[0];
        return true;
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {
        final Literal<Region> r = regions;
        return (enter ? "enter" : "leave") + " of " + (r == null ? "a region" : r.toString(e, debug));
    }

    @Override
    public void register(final Trigger t) {
        triggers.add(t);
        register();
    }

    @Override
    public void unregister(final Trigger t) {
        triggers.remove(t);
    }

    @Override
    public void unregisterAll() {
        triggers.clear();
    }

    private boolean applies(final Event e) {
        assert e instanceof RegionBorderEvent;
        if (enter != ((RegionBorderEvent) e).isEntering())
            return false;
        final Literal<Region> r = regions;
        if (r == null)
            return true;
        final Region re = ((RegionBorderEvent) e).getRegion();
        return r.check(e, r1 -> r1.equals(re));
    }

}
