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

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.InventorySlot;
import ch.njol.skript.util.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Furnace Slot")
@Description({"A slot of a furnace, i.e. either the ore, fuel or result slot.", "Remember to use '<a href='#ExprBlock'>block</a>' and not 'furnace', as 'furnace' is not an existing expression."})
@Examples({"set the fuel slot of the clicked block to a lava bucket", "set the block's ore slot to 64 iron ore", "give the result of the block to the player", "clear the result slot of the block"})
@Since("1.0")
@Events({"smelt", "fuel burn"})
public final class ExprFurnaceSlot extends PropertyExpression<Block, Slot> {
    private static final int ORE = 0, FUEL = 1, RESULT = 2;
    private static final String[] slotNames = {"ore", "fuel", "result"};

    static {
        register(ExprFurnaceSlot.class, Slot.class, "(" + ORE + "¦ore|" + FUEL + "¦fuel|" + RESULT + "¦result)[s] [slot[s]]", "blocks");
    }

    int slot;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
        setExpr((Expression<Block>) exprs[0]);
        slot = parser.mark;
        return true;
    }

    @Override
    protected Slot[] get(final Event e, final Block[] source) {
        return get(source, new Getter<Slot, Block>() {
            @SuppressWarnings("null")
            @Override
            @Nullable
            public Slot get(final Block b) {
                if (b.getType() != Material.FURNACE && b.getType() != Material.BURNING_FURNACE)
                    return null;
                if (getTime() >= 0 && (e instanceof FurnaceSmeltEvent && b.equals(((FurnaceSmeltEvent) e).getBlock()) || e instanceof FurnaceBurnEvent && b.equals(((FurnaceBurnEvent) e).getBlock())) && !Delay.isDelayed(e)) {
                    return new FurnaceEventSlot(e, ((Furnace) b.getState()).getInventory(), slot, getTime());
                }
                return new InventorySlot(((Furnace) b.getState()).getInventory(), slot);
            }
        });
    }

    @Override
    public Class<Slot> getReturnType() {
        return Slot.class;
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        if (e == null)
            return "the " + (getTime() == -1 ? "past " : getTime() == 1 ? "future " : "") + slotNames[slot] + " slot of " + getExpr().toString(null, debug);
        return Classes.getDebugMessage(getSingle(e));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean setTime(final int time) {
        return setTime(time, getExpr(), FurnaceSmeltEvent.class, FurnaceBurnEvent.class);
    }

    private static final class FurnaceEventSlot extends InventorySlot {

        private final Event e;

        private final int slot;
        private final int time;

        FurnaceEventSlot(final Event e, final FurnaceInventory invi, final int slot, final int time) {
            super(invi, slot);
            this.e = e;

            this.slot = slot;
            this.time = time;
        }

        @Override
        @Nullable
        public final ItemStack getItem() {
            if (e instanceof FurnaceSmeltEvent) {
                if (slot == RESULT) {
                    if (time >= 0)
                        return ((FurnaceSmeltEvent) e).getResult().clone();
                    return super.getItem();
                }
                if (slot == ORE) {
                    if (time <= 0) {
                        return super.getItem();
                    }
                    final ItemStack i = super.getItem();
                    if (i == null)
                        return null;
                    i.setAmount(i.getAmount() - 1);
                    return i.getAmount() == 0 ? new ItemStack(Material.AIR, 1) : i;
                }
                return super.getItem();
            }
            if (slot == FUEL) {
                if (time <= 0) {
                    return super.getItem();
                }
                final ItemStack i = super.getItem();
                if (i == null)
                    return null;
                i.setAmount(i.getAmount() - 1);
                return i.getAmount() == 0 ? new ItemStack(Material.AIR, 1) : i;
            }
            return super.getItem();
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public final void setItem(@Nullable final ItemStack item) {
            if (e instanceof FurnaceSmeltEvent) {
                if (slot == RESULT && time >= 0) {
                    if (item == null || item.getType() == Material.AIR) { // null/air crashes the server on account of a NPE if using event.setResult(...)
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> FurnaceEventSlot.super.setItem(null));
                    } else {
                        ((FurnaceSmeltEvent) e).setResult(item);
                    }
                } else if (slot == ORE && time >= 0) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> FurnaceEventSlot.super.setItem(item));
                } else {
                    super.setItem(item);
                }
            } else {
                if (slot == FUEL && time >= 0) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> FurnaceEventSlot.super.setItem(item));
                } else {
                    super.setItem(item);
                }
            }
        }

    }

}
