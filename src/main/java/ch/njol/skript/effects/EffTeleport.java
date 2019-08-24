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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Teleport")
@Description("Teleport an entity to a specific location.")
@Examples({"teleport the player to {homes.%player%}", "teleport the attacker to the victim"})
@Since("1.0")
public final class EffTeleport extends Effect {
    static {
        Skript.registerEffect(EffTeleport.class, "teleport %entities% (to|%direction%) %location%");
    }

    @SuppressWarnings("null")
    private Expression<Entity> entities;
    @SuppressWarnings("null")
    private Expression<Location> location;

    /**
     * @param yaw   Notch-yaw
     * @param pitch Notch-pitch
     * @return Whatever the given pitch and yaw represent a cartesian coordinate direction
     */
    private static final boolean ignoreDirection(final float yaw, final float pitch) {
        return (pitch == 0 || Math.abs(pitch - 90) < Skript.EPSILON || Math.abs(pitch + 90) < Skript.EPSILON) && (yaw == 0 || Math.abs(Math.sin(Math.toRadians(yaw))) < Skript.EPSILON || Math.abs(Math.cos(Math.toRadians(yaw))) < Skript.EPSILON);
    }

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
        entities = (Expression<Entity>) exprs[0];
        location = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void execute(final Event e) {
        Location to = location.getSingle(e);
        if (to == null)
            return;
        if (Math.abs(to.getX() - to.getBlockX() - 0.5) < Skript.EPSILON && Math.abs(to.getZ() - to.getBlockZ() - 0.5) < Skript.EPSILON) {
            final Block on = to.getBlock().getRelative(BlockFace.DOWN);
            if (on.getType() != Material.AIR) {
                to = to.clone();
                to.setY(on.getY() + Utils.getBlockHeight(on.getTypeId(), on.getData()));
            }
        }
        for (final Entity entity : entities.getArray(e)) {
            final Location loc;
            if (ignoreDirection(to.getYaw(), to.getPitch())) {
                loc = to.clone();
                loc.setPitch(entity.getLocation().getPitch());
                loc.setYaw(entity.getLocation().getYaw());
            } else {
                loc = to;
            }
            if (loc.getBlock().getType().isSolid()) {
                final Location cl = loc.clone();
                cl.setY(loc.getY() + 1);

                if (!cl.getBlock().getType().isSolid())
                    loc.setY(cl.getY());
            }
            loc.getChunk().load();
            if (e instanceof PlayerRespawnEvent && entity.equals(((PlayerRespawnEvent) e).getPlayer()) && !Delay.isDelayed(e)) {
                ((PlayerRespawnEvent) e).setRespawnLocation(loc);
            } else if (e instanceof PlayerMoveEvent && entity.equals(((PlayerMoveEvent) e).getPlayer()) && !Delay.isDelayed(e)) {
                ((PlayerMoveEvent) e).setTo(loc);
            } else {
                entity.setFallDistance(0.0f);
                entity.teleport(loc);
            }
        }
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {
        return "teleport " + entities.toString(e, debug) + " to " + location.toString(e, debug);
    }

}
