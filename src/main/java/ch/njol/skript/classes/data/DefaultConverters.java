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
 *  along with Skript. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2019 Peter Güttinger and contributors
 *
 */

package ch.njol.skript.classes.data;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.Slot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings({"rawtypes", "deprecation"})
public final class DefaultConverters {

    static {

        // OfflinePlayer - PlayerInventory
        Converters.registerConverter(OfflinePlayer.class, PlayerInventory.class, p -> {
            if (!p.isOnline())
                return null;
            return p.getPlayer().getInventory();
        }, Converter.NO_COMMAND_ARGUMENTS);
        // OfflinePlayer - Player
        Converters.registerConverter(OfflinePlayer.class, Player.class, OfflinePlayer::getPlayer, Converter.NO_COMMAND_ARGUMENTS);

        // TODO improve handling of interfaces
        // CommandSender - Player
        Converters.registerConverter(CommandSender.class, Player.class, s -> {
            if (s instanceof Player)
                return (Player) s;
            return null;
        });
        // Entity - Player
        Converters.registerConverter(Entity.class, Player.class, e -> {
            if (e instanceof Player)
                return (Player) e;
            return null;
        });
        // Entity - LivingEntity // Entity->Player is used if this doesn't exist
        Converters.registerConverter(Entity.class, LivingEntity.class, e -> {
            if (e instanceof LivingEntity)
                return (LivingEntity) e;
            return null;
        });

        // Block - Inventory
        Converters.registerConverter(Block.class, Inventory.class, b -> {
            if (b.getState() instanceof InventoryHolder)
                return ((InventoryHolder) b.getState()).getInventory();
            return null;
        }, Converter.NO_COMMAND_ARGUMENTS);

        // Entity - Inventory
        Converters.registerConverter(Entity.class, Inventory.class, e -> {
            if (e instanceof InventoryHolder)
                return ((InventoryHolder) e).getInventory();
            return null;
        }, Converter.NO_COMMAND_ARGUMENTS);

        // Block - ItemStack
        Converters.registerConverter(Block.class, ItemStack.class, b -> new ItemStack(b.getTypeId(), 1, b.getData()), Converter.NO_LEFT_CHAINING | Converter.NO_COMMAND_ARGUMENTS);

        // Location - Block
//		Converters.registerConverter(Location.class, Block.class, new Converter<Location, Block>() {
//			@Override
//			public Block convert(final Location l) {
//				return l.getBlock();
//			}
//		});
        Converters.registerConverter(Block.class, Location.class, BlockUtils::getLocation, Converter.NO_COMMAND_ARGUMENTS);

        // Entity - Location
        Converters.registerConverter(Entity.class, Location.class, Entity::getLocation, Converter.NO_COMMAND_ARGUMENTS);
        // Entity - EntityData
        Converters.registerConverter(Entity.class, EntityData.class, EntityData::fromEntity, Converter.NO_COMMAND_ARGUMENTS);
        // EntityData - EntityType
        Converters.registerConverter(EntityData.class, EntityType.class, data -> new EntityType(data, -1));

        // Location - World
//		Skript.registerConverter(Location.class, World.class, new Converter<Location, World>() {
//			private static final long serialVersionUID = 3270661123492313649L;
//
//			@Override
//			public World convert(final Location l) {
//				if (l == null)
//					return null;
//				return l.getWorld();
//			}
//		});

        // ItemType - ItemStack
        Converters.registerConverter(ItemType.class, ItemStack.class, ItemType::getRandom);
        Converters.registerConverter(ItemStack.class, ItemType.class, ItemType::new);

        // Experience - XpOrbData
        Converters.registerConverter(Experience.class, XpOrbData.class, e -> new XpOrbData(e.getXP()));
        Converters.registerConverter(XpOrbData.class, Experience.class, e -> new Experience(e.getExperience()));

//		// Item - ItemStack
//		Converters.registerConverter(Item.class, ItemStack.class, new Converter<Item, ItemStack>() {
//			@Override
//			public ItemStack convert(final Item i) {
//				return i.getItemStack();
//			}
//		});

        // Slot - ItemStack
        Converters.registerConverter(Slot.class, ItemStack.class, s -> {
            final ItemStack i = s.getItem();
            if (i == null)
                return new ItemStack(Material.AIR, 1);
            return i;
        });
//		// Slot - Inventory
//		Skript.addConverter(Slot.class, Inventory.class, new Converter<Slot, Inventory>() {
//			@Override
//			public Inventory convert(final Slot s) {
//				if (s == null)
//					return null;
//				return s.getInventory();
//			}
//		});

        // Block - InventoryHolder
        Converters.registerConverter(Block.class, InventoryHolder.class, b -> {
            if (b.getState() == null)
                return null;
            final BlockState s = b.getState();
            if (s instanceof InventoryHolder)
                return (InventoryHolder) s;
            return null;
        }, Converter.NO_COMMAND_ARGUMENTS);
//		Skript.registerConverter(InventoryHolder.class, Block.class, new Converter<InventoryHolder, Block>() {
//			@Override
//			public Block convert(final InventoryHolder h) {
//				if (h == null)
//					return null;
//				if (h instanceof BlockState)
//					return ((BlockState) h).getBlock();
//				return null;
//			}
//		});

//		// World - Time
//		Skript.registerConverter(World.class, Time.class, new Converter<World, Time>() {
//			@Override
//			public Time convert(final World w) {
//				if (w == null)
//					return null;
//				return new Time((int) w.getTime());
//			}
//		});

        // Enchantment - EnchantmentType
        Converters.registerConverter(Enchantment.class, EnchantmentType.class, e -> new EnchantmentType(e, -1));

    }

    public DefaultConverters() {
        super();
    }
}
