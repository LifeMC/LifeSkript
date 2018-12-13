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
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.events.EvtMoveOn;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.DelayedChangeBlock;
import ch.njol.skript.util.Getter;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.painting.PaintingEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.ItemStack;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings({"unchecked", "deprecation"})
public final class BukkitEventValues {
	
	static {
		
		// === WorldEvents ===
		EventValues.registerEventValue(WorldEvent.class, World.class, new Getter<World, WorldEvent>() {
			@Override
			@Nullable
			public World get(final WorldEvent e) {
				return e.getWorld();
			}
		}, 0);
		// StructureGrowEvent - a WorldEvent
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, new Getter<Block, StructureGrowEvent>() {
			@Override
			@Nullable
			public Block get(final StructureGrowEvent e) {
				return e.getLocation().getBlock();
			}
		}, 0);
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, new Getter<Block, StructureGrowEvent>() {
			@Override
			@Nullable
			public Block get(final StructureGrowEvent e) {
				for (final BlockState bs : e.getBlocks()) {
					if (bs.getLocation().equals(e.getLocation()))
						return new BlockStateBlock(bs);
				}
				return e.getLocation().getBlock();
			}
		}, 1);
		// WeatherEvent - not a WorldEvent (wtf ô_Ô)
		EventValues.registerEventValue(WeatherEvent.class, World.class, new Getter<World, WeatherEvent>() {
			@Override
			@Nullable
			public World get(final WeatherEvent e) {
				return e.getWorld();
			}
		}, 0);
		// ChunkEvents
		EventValues.registerEventValue(ChunkEvent.class, Chunk.class, new Getter<Chunk, ChunkEvent>() {
			@Override
			@Nullable
			public Chunk get(final ChunkEvent e) {
				return e.getChunk();
			}
		}, 0);
		
		// === BlockEvents ===
		EventValues.registerEventValue(BlockEvent.class, Block.class, new Getter<Block, BlockEvent>() {
			@Override
			@Nullable
			public Block get(final BlockEvent e) {
				return e.getBlock();
			}
		}, 0);
		EventValues.registerEventValue(BlockEvent.class, World.class, new Getter<World, BlockEvent>() {
			@Override
			@Nullable
			public World get(final BlockEvent e) {
				return e.getBlock().getWorld();
			}
		}, 0);
		// REMIND workaround of the event's location being at the entity in block events that have an entity event value
		EventValues.registerEventValue(BlockEvent.class, Location.class, new Getter<Location, BlockEvent>() {
			@Override
			@Nullable
			public Location get(final BlockEvent e) {
				return BlockUtils.getLocation(e.getBlock());
			}
		}, 0);
		// BlockPlaceEvent
		EventValues.registerEventValue(BlockPlaceEvent.class, Player.class, new Getter<Player, BlockPlaceEvent>() {
			@Override
			@Nullable
			public Player get(final BlockPlaceEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(BlockPlaceEvent.class, Block.class, new Getter<Block, BlockPlaceEvent>() {
			@Override
			public Block get(final BlockPlaceEvent e) {
				return new BlockStateBlock(e.getBlockReplacedState());
			}
		}, -1);
		// BlockFadeEvent
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new Getter<Block, BlockFadeEvent>() {
			@Override
			@Nullable
			public Block get(final BlockFadeEvent e) {
				return e.getBlock();
			}
		}, -1);
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new Getter<Block, BlockFadeEvent>() {
			@Override
			public Block get(final BlockFadeEvent e) {
				return new DelayedChangeBlock(e.getBlock(), e.getNewState());
			}
		}, 0);
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new Getter<Block, BlockFadeEvent>() {
			@Override
			public Block get(final BlockFadeEvent e) {
				return new BlockStateBlock(e.getNewState());
			}
		}, 1);
		// BlockFormEvent
		EventValues.registerEventValue(BlockFormEvent.class, Block.class, new Getter<Block, BlockFormEvent>() {
			@Override
			@Nullable
			public Block get(final BlockFormEvent e) {
				if (e instanceof BlockSpreadEvent)
					return e.getBlock();
				return new BlockStateBlock(e.getNewState());
			}
		}, 0);
		EventValues.registerEventValue(BlockFormEvent.class, Block.class, new Getter<Block, BlockFormEvent>() {
			@Override
			@Nullable
			public Block get(final BlockFormEvent e) {
				return e.getBlock();
			}
		}, -1);
		// BlockDamageEvent
		EventValues.registerEventValue(BlockDamageEvent.class, Player.class, new Getter<Player, BlockDamageEvent>() {
			@Override
			@Nullable
			public Player get(final BlockDamageEvent e) {
				return e.getPlayer();
			}
		}, 0);
		// BlockBreakEvent
		EventValues.registerEventValue(BlockBreakEvent.class, Player.class, new Getter<Player, BlockBreakEvent>() {
			@Override
			@Nullable
			public Player get(final BlockBreakEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new Getter<Block, BlockBreakEvent>() {
			@Override
			@Nullable
			public Block get(final BlockBreakEvent e) {
				return e.getBlock();
			}
		}, -1);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new Getter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				return new DelayedChangeBlock(e.getBlock());
			}
		}, 0);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new Getter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				final BlockState s = e.getBlock().getState();
				s.setType(s.getType() == Material.ICE ? Material.STATIONARY_WATER : Material.AIR);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 1);
		// BlockFromToEvent
		EventValues.registerEventValue(BlockFromToEvent.class, Block.class, new Getter<Block, BlockFromToEvent>() {
			@Override
			@Nullable
			public Block get(final BlockFromToEvent e) {
				return e.getToBlock();
			}
		}, 1);
		// BlockIgniteEvent
		EventValues.registerEventValue(BlockIgniteEvent.class, Player.class, new Getter<Player, BlockIgniteEvent>() {
			@Override
			@Nullable
			public Player get(final BlockIgniteEvent e) {
				return e.getPlayer();
			}
		}, 0);
		// BlockDispenseEvent
		EventValues.registerEventValue(BlockDispenseEvent.class, ItemStack.class, new Getter<ItemStack, BlockDispenseEvent>() {
			@Override
			@Nullable
			public ItemStack get(final BlockDispenseEvent e) {
				return e.getItem();
			}
		}, 0);
		// BlockCanBuildEvent
		EventValues.registerEventValue(BlockCanBuildEvent.class, Block.class, new Getter<Block, BlockCanBuildEvent>() {
			@Override
			@Nullable
			public Block get(final BlockCanBuildEvent e) {
				return e.getBlock();
			}
		}, -1);
		EventValues.registerEventValue(BlockCanBuildEvent.class, Block.class, new Getter<Block, BlockCanBuildEvent>() {
			@Override
			public Block get(final BlockCanBuildEvent e) {
				final BlockState s = e.getBlock().getState();
				s.setTypeId(e.getMaterialId());
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 0);
		// SignChangeEvent
		EventValues.registerEventValue(SignChangeEvent.class, Player.class, new Getter<Player, SignChangeEvent>() {
			@Override
			@Nullable
			public Player get(final SignChangeEvent e) {
				return e.getPlayer();
			}
		}, 0);
		
		// === EntityEvents ===
		EventValues.registerEventValue(EntityEvent.class, Entity.class, new Getter<Entity, EntityEvent>() {
			@Override
			@Nullable
			public Entity get(final EntityEvent e) {
				return e.getEntity();
			}
		}, 0, "Use 'attacker' and/or 'victim' in damage events", EntityDamageEvent.class);
		EventValues.registerEventValue(EntityEvent.class, World.class, new Getter<World, EntityEvent>() {
			@Override
			@Nullable
			public World get(final EntityEvent e) {
				return e.getEntity() == null ? null : e.getEntity().getWorld(); // no idea why it could be null, but it can happen
			}
		}, 0);
		// EntityDamageEvent
		EventValues.registerEventValue(EntityDamageEvent.class, DamageCause.class, new Getter<DamageCause, EntityDamageEvent>() {
			@Override
			@Nullable
			public DamageCause get(final EntityDamageEvent e) {
				return e.getCause();
			}
		}, 0);
		EventValues.registerEventValue(EntityDamageByEntityEvent.class, Projectile.class, new Getter<Projectile, EntityDamageByEntityEvent>() {
			@Override
			@Nullable
			public Projectile get(final EntityDamageByEntityEvent e) {
				if (e.getDamager() instanceof Projectile)
					return (Projectile) e.getDamager();
				return null;
			}
		}, 0);
		// EntityDeathEvent
		EventValues.registerEventValue(EntityDeathEvent.class, Projectile.class, new Getter<Projectile, EntityDeathEvent>() {
			@Override
			@Nullable
			public Projectile get(final EntityDeathEvent e) {
				final EntityDamageEvent ldc = e.getEntity().getLastDamageCause();
				if (ldc instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) ldc).getDamager() instanceof Projectile)
					return (Projectile) ((EntityDamageByEntityEvent) ldc).getDamager();
				return null;
			}
		}, 0);
		EventValues.registerEventValue(EntityDeathEvent.class, DamageCause.class, new Getter<DamageCause, EntityDeathEvent>() {
			@Override
			@Nullable
			public DamageCause get(final EntityDeathEvent e) {
				final EntityDamageEvent ldc = e.getEntity().getLastDamageCause();
				return ldc == null ? null : ldc.getCause();
			}
		}, 0);
		// ProjectileHitEvent
		EventValues.registerEventValue(ProjectileHitEvent.class, Entity.class, new Getter<Entity, ProjectileHitEvent>() {
			@Override
			@Nullable
			public Entity get(final ProjectileHitEvent e) {
				assert false;
				return e.getEntity();
			}
		}, 0, "Use 'projectile' and/or 'shooter' in projectile hit events", ProjectileHitEvent.class);
		EventValues.registerEventValue(ProjectileHitEvent.class, Projectile.class, new Getter<Projectile, ProjectileHitEvent>() {
			@Override
			@Nullable
			public Projectile get(final ProjectileHitEvent e) {
				return e.getEntity();
			}
		}, 0);
		// ProjectileLaunchEvent
		EventValues.registerEventValue(ProjectileLaunchEvent.class, Entity.class, new Getter<Entity, ProjectileLaunchEvent>() {
			@Override
			@Nullable
			public Entity get(final ProjectileLaunchEvent e) {
				assert false;
				return e.getEntity();
			}
		}, 0, "Use 'projectile' and/or 'shooter' in shoot events", ProjectileLaunchEvent.class);
		EventValues.registerEventValue(ProjectileLaunchEvent.class, Projectile.class, new Getter<Projectile, ProjectileLaunchEvent>() {
			@Override
			@Nullable
			public Projectile get(final ProjectileLaunchEvent e) {
				return e.getEntity();
			}
		}, 0);
		// EntityTameEvent
		EventValues.registerEventValue(EntityTameEvent.class, Player.class, new Getter<Player, EntityTameEvent>() {
			@Override
			@Nullable
			public Player get(final EntityTameEvent e) {
				return e.getOwner() instanceof Player ? (Player) e.getOwner() : null;
			}
		}, 0);
		// EntityChangeBlockEvent
		EventValues.registerEventValue(EntityChangeBlockEvent.class, Block.class, new Getter<Block, EntityChangeBlockEvent>() {
			@Override
			@Nullable
			public Block get(final EntityChangeBlockEvent e) {
				return e.getBlock();
			}
		}, 0);
		
		// --- PlayerEvents ---
		EventValues.registerEventValue(PlayerEvent.class, Player.class, new Getter<Player, PlayerEvent>() {
			@Override
			@Nullable
			public Player get(final PlayerEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(PlayerEvent.class, World.class, new Getter<World, PlayerEvent>() {
			@Override
			@Nullable
			public World get(final PlayerEvent e) {
				return e.getPlayer().getWorld();
			}
		}, 0);
		// PlayerBedEnterEvent
		EventValues.registerEventValue(PlayerBedEnterEvent.class, Block.class, new Getter<Block, PlayerBedEnterEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBedEnterEvent e) {
				return e.getBed();
			}
		}, 0);
		// PlayerBedLeaveEvent
		EventValues.registerEventValue(PlayerBedLeaveEvent.class, Block.class, new Getter<Block, PlayerBedLeaveEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBedLeaveEvent e) {
				return e.getBed();
			}
		}, 0);
		// PlayerBucketEvents
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, new Getter<Block, PlayerBucketFillEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBucketFillEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		}, 0);
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, new Getter<Block, PlayerBucketFillEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBucketFillEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setTypeId(0);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 1);
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new Getter<Block, PlayerBucketEmptyEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerBucketEmptyEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		}, -1);
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new Getter<Block, PlayerBucketEmptyEvent>() {
			@Override
			public Block get(final PlayerBucketEmptyEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setType(e.getBucket() == Material.WATER_BUCKET ? Material.STATIONARY_WATER : Material.STATIONARY_LAVA);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 0);
		// PlayerDropItemEvent
		EventValues.registerEventValue(PlayerDropItemEvent.class, Item.class, new Getter<Item, PlayerDropItemEvent>() {
			@Override
			@Nullable
			public Item get(final PlayerDropItemEvent e) {
				return e.getItemDrop();
			}
		}, 0);
		EventValues.registerEventValue(PlayerDropItemEvent.class, ItemStack.class, new Getter<ItemStack, PlayerDropItemEvent>() {
			@Override
			@Nullable
			public ItemStack get(final PlayerDropItemEvent e) {
				return e.getItemDrop().getItemStack();
			}
		}, 0);
		// PlayerPickupItemEvent
		EventValues.registerEventValue(PlayerPickupItemEvent.class, Item.class, new Getter<Item, PlayerPickupItemEvent>() {
			@Override
			@Nullable
			public Item get(final PlayerPickupItemEvent e) {
				return e.getItem();
			}
		}, 0);
		EventValues.registerEventValue(PlayerPickupItemEvent.class, ItemStack.class, new Getter<ItemStack, PlayerPickupItemEvent>() {
			@Override
			@Nullable
			public ItemStack get(final PlayerPickupItemEvent e) {
				return e.getItem().getItemStack();
			}
		}, 0);
		// PlayerItemConsumeEvent
		if (Skript.classExists("org.bukkit.event.player.PlayerItemConsumeEvent")) {
			EventValues.registerEventValue(PlayerItemConsumeEvent.class, ItemStack.class, new Getter<ItemStack, PlayerItemConsumeEvent>() {
				@Override
				@Nullable
				public ItemStack get(final PlayerItemConsumeEvent e) {
					return e.getItem();
				}
			}, 0);
		}
		// PlayerItemBreakEvent
		if (Skript.classExists("org.bukkit.event.player.PlayerItemBreakEvent")) {
			EventValues.registerEventValue(PlayerItemBreakEvent.class, ItemStack.class, new Getter<ItemStack, PlayerItemBreakEvent>() {
				@Override
				@Nullable
				public ItemStack get(final PlayerItemBreakEvent e) {
					return e.getBrokenItem();
				}
			}, 0);
		}
		// PlayerInteractEntityEvent
		EventValues.registerEventValue(PlayerInteractEntityEvent.class, Entity.class, new Getter<Entity, PlayerInteractEntityEvent>() {
			@Override
			@Nullable
			public Entity get(final PlayerInteractEntityEvent e) {
				return e.getRightClicked();
			}
		}, 0);
		// PlayerInteractEvent
		EventValues.registerEventValue(PlayerInteractEvent.class, Block.class, new Getter<Block, PlayerInteractEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerInteractEvent e) {
				return e.getClickedBlock();
			}
		}, 0);
		// PlayerShearEntityEvent
		EventValues.registerEventValue(PlayerShearEntityEvent.class, Entity.class, new Getter<Entity, PlayerShearEntityEvent>() {
			@Override
			@Nullable
			public Entity get(final PlayerShearEntityEvent e) {
				return e.getEntity();
			}
		}, 0);
		// PlayerMoveEvent
		EventValues.registerEventValue(PlayerMoveEvent.class, Block.class, new Getter<Block, PlayerMoveEvent>() {
			@Override
			@Nullable
			public Block get(final PlayerMoveEvent e) {
				return EvtMoveOn.getBlock(e);
			}
		}, 0);
		
		// --- HangingEvents ---
		// 1.4.3
		if (Skript.classExists("org.bukkit.event.hanging.HangingEvent")) {
			EventValues.registerEventValue(HangingEvent.class, Hanging.class, new Getter<Hanging, HangingEvent>() {
				@Override
				@Nullable
				public Hanging get(final HangingEvent e) {
					return e.getEntity();
				}
			}, 0);
			EventValues.registerEventValue(HangingEvent.class, World.class, new Getter<World, HangingEvent>() {
				@Override
				@Nullable
				public World get(final HangingEvent e) {
					return e.getEntity().getWorld();
				}
			}, 0);
			// HangingPlaceEvent
			EventValues.registerEventValue(HangingPlaceEvent.class, Player.class, new Getter<Player, HangingPlaceEvent>() {
				@Override
				@Nullable
				public Player get(final HangingPlaceEvent e) {
					return e.getPlayer();
				}
			}, 0);
		} else if (Skript.classExists("org.bukkit.event.painting.PaintingEvent")) {
			EventValues.registerEventValue(PaintingEvent.class, Painting.class, new Getter<Painting, PaintingEvent>() {
				@Override
				@Nullable
				public Painting get(final PaintingEvent e) {
					return e.getPainting();
				}
			}, 0);
			EventValues.registerEventValue(PaintingEvent.class, World.class, new Getter<World, PaintingEvent>() {
				@Override
				@Nullable
				public World get(final PaintingEvent e) {
					return e.getPainting().getWorld();
				}
			}, 0);
			// PaintingPlaceEvent
			EventValues.registerEventValue(PaintingPlaceEvent.class, Player.class, new Getter<Player, PaintingPlaceEvent>() {
				@Override
				@Nullable
				public Player get(final PaintingPlaceEvent e) {
					return e.getPlayer();
				}
			}, 0);
		}
		
		// --- VehicleEvents ---
		EventValues.registerEventValue(VehicleEvent.class, Vehicle.class, new Getter<Vehicle, VehicleEvent>() {
			@Override
			@Nullable
			public Vehicle get(final VehicleEvent e) {
				return e.getVehicle();
			}
		}, 0);
		EventValues.registerEventValue(VehicleEvent.class, World.class, new Getter<World, VehicleEvent>() {
			@Override
			@Nullable
			public World get(final VehicleEvent e) {
				return e.getVehicle().getWorld();
			}
		}, 0);
		EventValues.registerEventValue(VehicleExitEvent.class, LivingEntity.class, new Getter<LivingEntity, VehicleExitEvent>() {
			@Override
			@Nullable
			public LivingEntity get(final VehicleExitEvent e) {
				return e.getExited();
			}
		}, 0);
		EventValues.registerEventValue(VehicleEvent.class, Entity.class, new Getter<Entity, VehicleEvent>() {
			@Override
			@Nullable
			public Entity get(final VehicleEvent e) {
				return e.getVehicle().getPassenger();
			}
		}, 0);
		
		// === CommandEvents ===
		// PlayerCommandPreprocessEvent is a PlayerEvent
		EventValues.registerEventValue(ServerCommandEvent.class, CommandSender.class, new Getter<CommandSender, ServerCommandEvent>() {
			@Override
			@Nullable
			public CommandSender get(final ServerCommandEvent e) {
				return e.getSender();
			}
		}, 0);
		EventValues.registerEventValue(CommandEvent.class, CommandSender.class, new Getter<CommandSender, CommandEvent>() {
			@Override
			public CommandSender get(final CommandEvent e) {
				return e.getSender();
			}
		}, 0);
		EventValues.registerEventValue(CommandEvent.class, World.class, new Getter<World, CommandEvent>() {
			@Override
			@Nullable
			public World get(final CommandEvent e) {
				return e.getSender() instanceof Player ? ((Player) e.getSender()).getWorld() : null;
			}
		}, 0);
		
		// === InventoryEvents ===
		EventValues.registerEventValue(InventoryClickEvent.class, Player.class, new Getter<Player, InventoryClickEvent>() {
			@Override
			@Nullable
			public Player get(final InventoryClickEvent e) {
				return e.getWhoClicked() instanceof Player ? (Player) e.getWhoClicked() : null;
			}
		}, 0);
		EventValues.registerEventValue(InventoryClickEvent.class, World.class, new Getter<World, InventoryClickEvent>() {
			@Override
			@Nullable
			public World get(final InventoryClickEvent e) {
				return e.getWhoClicked().getWorld();
			}
		}, 0);
		EventValues.registerEventValue(CraftItemEvent.class, ItemStack.class, new Getter<ItemStack, CraftItemEvent>() {
			@Override
			@Nullable
			public ItemStack get(final CraftItemEvent e) {
				return e.getRecipe().getResult();
			}
		}, 0);
		
	}
	
}
