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

package ch.njol.skript.util;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Locale;

/**
 * @author Peter Güttinger
 */
public class EquipmentSlot extends Slot {

    private final EntityEquipment e;
    private final EquipSlot slot;

    public EquipmentSlot(final EntityEquipment e, final EquipSlot slot) {
        this.e = e;
        this.slot = slot;
    }

    @Override
    @Nullable
    public ItemStack getItem() {
        return slot.get(e);
    }

    @Override
    public void setItem(@Nullable final ItemStack item) {
        slot.set(e, item);
        if (e.getHolder() instanceof Player)
            PlayerUtils.updateInventory((Player) e.getHolder());
    }

    @Override
    public String toString_i() {
        return "the " + slot.name().toLowerCase(Locale.ENGLISH) + " of " + Classes.toString(e.getHolder()); // TODO localise?
    }

    public enum EquipSlot {
        TOOL {
            @Override
            @Nullable
            public ItemStack get(final EntityEquipment e) {
                return e.getItemInHand();
            }

            @Override
            public void set(final EntityEquipment e, @Nullable final ItemStack item) {
                e.setItemInHand(item);
            }
        },
        HELMET {
            @Override
            @Nullable
            public ItemStack get(final EntityEquipment e) {
                return e.getHelmet();
            }

            @Override
            public void set(final EntityEquipment e, @Nullable final ItemStack item) {
                e.setHelmet(item);
            }
        },
        CHESTPLATE {
            @Override
            @Nullable
            public ItemStack get(final EntityEquipment e) {
                return e.getChestplate();
            }

            @Override
            public void set(final EntityEquipment e, @Nullable final ItemStack item) {
                e.setChestplate(item);
            }
        },
        LEGGINGS {
            @Override
            @Nullable
            public ItemStack get(final EntityEquipment e) {
                return e.getLeggings();
            }

            @Override
            public void set(final EntityEquipment e, @Nullable final ItemStack item) {
                e.setLeggings(item);
            }
        },
        BOOTS {
            @Override
            @Nullable
            public ItemStack get(final EntityEquipment e) {
                return e.getBoots();
            }

            @Override
            public void set(final EntityEquipment e, @Nullable final ItemStack item) {
                e.setBoots(item);
            }
        };

        @Nullable
        public abstract ItemStack get(final EntityEquipment e);

        public abstract void set(final EntityEquipment e, @Nullable final ItemStack item);

    }

}
