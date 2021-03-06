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

package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.registrations.Classes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public final class EvtMoveOn extends SelfRegisteringSkriptEvent { // TODO on jump

    //	private static final HashMap<BlockLocation, List<Trigger>> blockTriggers = new HashMap<BlockLocation, List<Trigger>>();
    private static final HashMap<Integer, List<Trigger>> itemTypeTriggers = new HashMap<>();

    //	private static final class BlockLocation {
//		final World world;
//		final int x, y, z;
//
//		BlockLocation(final World world, final int x, final int y, final int z) {
//			this.world = world;
//			this.x = x;
//			this.y = y;
//			this.z = z;
//		}
//
//		@Override
//		public boolean equals(final Object obj) {
//			if (obj == this)
//				return true;
//			if (!(obj instanceof BlockLocation))
//				return false;
//			final BlockLocation other = (BlockLocation) obj;
//			return world == other.world && x == other.x && y == other.y && z == other.z;
//		}
//
//		@Override
//		public int hashCode() {
//			return world.hashCode() + 29 * (x + 17 * (y + 31 * z));
//		}
//	}
    private static long lastCall;
    private static final EventExecutor executor = (@Nullable final Listener l, @Nullable final Event event) -> {
        if (event == null)
            return;
        final PlayerMoveEvent e = (PlayerMoveEvent) event;
        final Location from = e.getFrom(), to = e.getTo();
        if (System.currentTimeMillis() - lastCall < SkriptEventHandler.moveEventCooldown)
            return; // Prevent too many move events firing
        lastCall = System.currentTimeMillis();
//			if (!blockTriggers.isEmpty()) {
//				final List<Trigger> ts = blockTriggers.get(new BlockLocation(to.getWorld(), to.getBlockX(), to.getBlockY(), to.getBlockZ()));
//				if (ts != null) {
//					for (final Trigger t : ts) {
//						SkriptEventHandler.logTriggerStart(t);
//						t.start(e);
//						SkriptEventHandler.logTriggerEnd(t);
//					}
//				}
//			}
        if (!itemTypeTriggers.isEmpty()) {
            @SuppressWarnings("null") final int id = getOnBlock(to);
            if (id == 0)
                return;
            final List<Trigger> ts = itemTypeTriggers.get(id);
            if (ts == null)
                return;
            final int y = getBlockY(to.getY(), id);
            if (to.getWorld().equals(from.getWorld()) && to.getBlockX() == from.getBlockX() && to.getBlockZ() == from.getBlockZ() && y == getBlockY(from.getY(), getOnBlock(from)) && getOnBlock(from) == id)
                return;
            SkriptEventHandler.logEventStart(e);
            final byte data = to.getWorld().getBlockAt(to.getBlockX(), y, to.getBlockZ()).getData();
            triggersLoop:
            for (final Trigger t : ts) {
                final EvtMoveOn se = (EvtMoveOn) t.getEvent();
                for (final ItemType i : se.types) {
                    if (i.isOfType(id, data)) {
                        SkriptEventHandler.logTriggerStart(t);
                        t.execute(e);
                        SkriptEventHandler.logTriggerEnd(t);
                        continue triggersLoop;
                    }
                }
            }
            SkriptEventHandler.logEventEnd();
        }
    };
    private static boolean registeredExecutor;
//	private World world;
//	private int x, y, z;

    static {
//		Skript.registerEvent(EvtMoveOn.class, PlayerMoveEvent.class, "(step|walk) on <.+>");
        Skript.registerEvent("Move On", EvtMoveOn.class, PlayerMoveEvent.class, EvtMoveOn::new, "(step|walk)[ing] (on|over) %*itemtypes%").description("Called when a player moves onto a certain type of block. Please note that using this event can cause lag if there are many players online.").examples("on walking on dirt or grass", "on stepping on stone").since("2.0");
    }

    @SuppressWarnings("null")
    private ItemType[] types;

    private static final int getOnBlock(final Location l) {
        int id = l.getWorld().getBlockTypeIdAt(l.getBlockX(), (int) Math.ceil(l.getY()) - 1, l.getBlockZ());
        if (id == 0 && Math.abs(l.getY() - l.getBlockY() - 0.5) < Skript.EPSILON) { // fences
            id = l.getWorld().getBlockTypeIdAt(l.getBlockX(), l.getBlockY() - 1, l.getBlockZ());
            if (id != Material.FENCE.getId() && id != 107 && id != 113) // fence gate // nether fence
                return 0;
        }
        return id;
    }

    private static final int getBlockY(final double y, final int id) {
        if ((id == Material.FENCE.getId() || id == 107 || id == 113) && Math.abs(y - Math.floor(y) - 0.5) < Skript.EPSILON) // fence gate // nether fence
            return (int) Math.floor(y) - 1;
        return (int) Math.ceil(y) - 1;
    }

    @SuppressWarnings("null")
    public static final Block getBlock(final PlayerMoveEvent e) {
        return e.getTo().subtract(0, 0.5, 0).getBlock();
    }

    @Override
    public final boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
//		if (parser.regexes.get(0).group().equalsIgnoreCase("<block>")/* && isValidatingInput*/)
//			return true;
//		final Matcher m = Pattern.compile("<block:(.+)>").matcher(parser.regexes.get(0).group());
//		if (m.matches()) {
//			final Block b = (Block) Skript.deserialize("block", m.group(1));
//			if (b == null)
//				return false;
//			world = b.getWorld();
//			x = b.getX();
//			y = b.getY();
//			z = b.getZ();
//		} else {
        @SuppressWarnings("unchecked") final Literal<ItemType> l = (Literal<ItemType>) args[0];//SkriptParser.parseLiteral(parser.regexes.get(0).group(), ItemType.class, ParseContext.EVENT);
        if (l == null)
            return false;
        types = l.getAll();
        for (final ItemType t : types) {
            boolean hasBlock = false;
            for (final ItemData d : t) {
                if (d.getId() == -1) {
                    Skript.error("Can't use an 'on walk' event with an alias that matches all blocks");
                    return false;
                }
                if (d.getId() <= Skript.MAXBLOCKID && d.getId() != 0) // don't allow air
                    hasBlock = true;
            }
            if (!hasBlock) {
                Skript.error(t + " is not a block and can thus not be walked on");
                return false;
            }
        }
//		}
        return true;
    }

    @Override
    public final String toString(@Nullable final Event e, final boolean debug) {
        return "walk on " + Classes.toString(types, false);
//		return "walk on " + (types != null ? Skript.toString(types, false) : "<block:" + world.getName() + ":" + x + "," + y + "," + z + ">");
    }

    @Override
    public final void register(final Trigger trigger) {
//		if (types == null) {
//			final BlockLocation l = new BlockLocation(world, x, y, z);
//			List<Trigger> ts = blockTriggers.get(l);
//			if (ts == null)
//				blockTriggers.put(l, ts = new ArrayList<Trigger>());
//			ts.add(trigger);
//		} else {
        for (final ItemType t : types) {
            for (final ItemData d : t) {
                if (d.getId() > Skript.MAXBLOCKID)
                    continue;
                final List<Trigger> ts = itemTypeTriggers.computeIfAbsent(d.getId(), k -> new ArrayList<>());
                ts.add(trigger);
            }
        }
//		}
        if (!registeredExecutor) {
            Bukkit.getPluginManager().registerEvent(PlayerMoveEvent.class, new EmptyListener(), SkriptConfig.defaultEventPriority.value(), executor, Skript.getInstance(), true);
            registeredExecutor = true;
        }
    }

    @Override
    public final void unregister(final Trigger t) {
//		final Iterator<Entry<BlockLocation, List<Trigger>>> i = blockTriggers.entrySet().iterator();
//		while (i.hasNext()) {
//			final List<Trigger> ts = i.next().getValue();
//			ts.remove(t);
//			if (ts.isEmpty())
//				i.remove();
//		}
        final Iterator<Entry<Integer, List<Trigger>>> i2 = itemTypeTriggers.entrySet().iterator();
        while (i2.hasNext()) {
            final List<Trigger> ts = i2.next().getValue();
            ts.remove(t);
            if (ts.isEmpty())
                i2.remove();
        }
    }

    @Override
    public final void unregisterAll() {
//		blockTriggers.clear();
        itemTypeTriggers.clear();
    }

    private static final class EmptyListener implements Listener {
        EmptyListener() {
            /* implicit super call */
        }
    }

}
