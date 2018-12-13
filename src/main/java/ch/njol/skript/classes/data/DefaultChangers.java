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

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.util.Experience;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class DefaultChangers { //NOSONAR
	
	public final static Changer<Entity> entityChanger = new Changer<Entity>() {
		@SuppressWarnings("unchecked")
		@Override
		@Nullable
		public Class<?>[] acceptChange(final ChangeMode mode) {
			switch (mode) {
				case ADD:
					return CollectionUtils.array(ItemType[].class, Inventory.class, Experience[].class);
				case DELETE:
					return CollectionUtils.array();
				case REMOVE:
					return CollectionUtils.array(PotionEffectType[].class, ItemType[].class, Inventory.class);
				case REMOVE_ALL:
					return CollectionUtils.array(PotionEffectType[].class, ItemType[].class);
				case SET:
				case RESET: // REMIND reset entity? (unshear, remove held item, reset weapon/armour, ...)
					return null;
			}
			assert false;
			return null;
		}
		
		@Override
		public void change(final Entity[] entities, final @Nullable Object[] delta, final ChangeMode mode) {
			if (delta == null) {
				for (final Entity e : entities) {
					if (!(e instanceof Player))
						e.remove();
				}
				return;
			}
			for (final Entity e : entities) {
				for (final Object d : delta) {
					if (d instanceof PotionEffectType) {
						assert mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL;
						if (!(e instanceof LivingEntity))
							continue;
						((LivingEntity) e).removePotionEffect((PotionEffectType) d);
					} else {
						if (e instanceof Player) {
							final Player p = (Player) e;
							if (d instanceof Experience) {
								p.giveExp(((Experience) d).getXP());
							} else if (d instanceof Inventory) {
								final PlayerInventory invi = p.getInventory();
								if (mode == ChangeMode.ADD) {
									for (final ItemStack i : (Inventory) d) {
										if (i != null)
											invi.addItem(i);
									}
								} else {
									invi.removeItem(((Inventory) d).getContents());
								}
							} else if (d instanceof ItemType) {
								final PlayerInventory invi = p.getInventory();
								if (invi == null)
									continue;
								if (mode == ChangeMode.ADD)
									((ItemType) d).addTo(invi);
								else if (mode == ChangeMode.REMOVE)
									((ItemType) d).removeFrom(invi);
								else
									((ItemType) d).removeAll(invi);
							}
						}
					}
				}
				if (e instanceof Player)
					PlayerUtils.updateInventory((Player) e);
			}
		}
	};
	
	public final static Changer<Player> playerChanger = new Changer<Player>() {
		@Override
		@Nullable
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return null;
			return entityChanger.acceptChange(mode);
		}
		
		@Override
		public void change(final Player[] players, final @Nullable Object[] delta, final ChangeMode mode) {
			entityChanger.change(players, delta, mode);
		}
	};
	
	public final static Changer<Entity> nonLivingEntityChanger = new Changer<Entity>() {
		@SuppressWarnings("unchecked")
		@Override
		@Nullable
		public Class<Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return CollectionUtils.array();
			return null;
		}
		
		@Override
		public void change(final Entity[] entities, final @Nullable Object[] delta, final ChangeMode mode) {
			assert mode == ChangeMode.DELETE;
			for (final Entity e : entities) {
				if (e instanceof Player)
					continue;
				e.remove();
			}
		}
	};
	
	public final static Changer<Item> itemChanger = new Changer<Item>() {
		@SuppressWarnings("unchecked")
		@Override
		@Nullable
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemStack.class);
			return nonLivingEntityChanger.acceptChange(mode);
		}
		
		@Override
		public void change(final Item[] what, final @Nullable Object[] delta, final ChangeMode mode) {
			if (mode == ChangeMode.SET) {
				assert delta != null;
				for (final Item i : what)
					i.setItemStack((ItemStack) delta[0]);
			} else {
				nonLivingEntityChanger.change(what, delta, mode);
			}
		}
	};
	
	public final static Changer<Inventory> inventoryChanger = new Changer<Inventory>() {
		@SuppressWarnings("unchecked")
		@Override
		@Nullable
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.RESET)
				return null;
			if (mode == ChangeMode.REMOVE_ALL)
				return CollectionUtils.array(ItemType[].class);
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemType[].class, Inventory.class);
			return CollectionUtils.array(ItemType[].class, Inventory[].class);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void change(final Inventory[] invis, final @Nullable Object[] delta, final ChangeMode mode) {
			for (final Inventory invi : invis) {
				assert invi != null;
				switch (mode) {
					case DELETE:
						invi.clear();
						if (invi instanceof PlayerInventory) {
							((PlayerInventory) invi).setArmorContents(new ItemStack[4]);
							if (((PlayerInventory) invi).getHolder() instanceof Player) {
								final Player p = (Player) ((PlayerInventory) invi).getHolder();
								if (invi.equals(p.getOpenInventory().getBottomInventory()))
									p.getOpenInventory().setCursor(null);
								if (p.getOpenInventory().getTopInventory() instanceof CraftingInventory)
									p.getOpenInventory().getTopInventory().clear();
							}
						}
						break;
					case SET:
						invi.clear();
						//$FALL-THROUGH$
					case ADD:
						assert delta != null;
						for (final Object d : delta) {
							if (d instanceof Inventory) {
								for (final ItemStack i : (Inventory) d) {
									if (i != null)
										invi.addItem(i);
								}
							} else {
								((ItemType) d).addTo(invi);
							}
						}
						break;
					case REMOVE:
					case REMOVE_ALL:
						assert delta != null;
						for (final Object d : delta) {
							if (d instanceof Inventory) {
								assert mode == ChangeMode.REMOVE;
								invi.removeItem(((Inventory) d).getContents());
							} else {
								if (mode == ChangeMode.REMOVE)
									((ItemType) d).removeFrom(invi);
								else
									((ItemType) d).removeAll(invi);
							}
						}
						break;
					case RESET:
						assert false;
				}
				if (invi.getHolder() instanceof Player) {
					((Player) invi.getHolder()).updateInventory();
				}
			}
		}
	};
	
	public final static Changer<Block> blockChanger = new Changer<Block>() {
		@SuppressWarnings("unchecked")
		@Override
		@Nullable
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.RESET)
				return null; // REMIND regenerate?
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemType.class);
			return CollectionUtils.array(ItemType[].class, Inventory[].class);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void change(final Block[] blocks, final @Nullable Object[] delta, final ChangeMode mode) {
			for (final Block block : blocks) {
				assert block != null;
				switch (mode) {
					case SET:
						assert delta != null;
						((ItemType) delta[0]).getBlock().setBlock(block, true);
						break;
					case DELETE:
						block.setTypeId(0, true);
						break;
					case ADD:
					case REMOVE:
					case REMOVE_ALL:
						assert delta != null;
						final BlockState state = block.getState();
						if (!(state instanceof InventoryHolder))
							break;
						final Inventory invi = ((InventoryHolder) state).getInventory();
						if (invi == null)
							continue;
						if (mode == ChangeMode.ADD) {
							for (final Object d : delta) {
								if (d instanceof Inventory) {
									for (final ItemStack i : (Inventory) d) {
										if (i != null)
											invi.addItem(i);
									}
								} else {
									((ItemType) d).addTo(invi);
								}
							}
						} else {
							for (final Object d : delta) {
								if (d instanceof Inventory) {
									invi.removeItem(((Inventory) d).getContents());
								} else {
									if (mode == ChangeMode.REMOVE)
										((ItemType) d).removeFrom(invi);
									else
										((ItemType) d).removeAll(invi);
								}
							}
						}
						state.update();
						break;
					case RESET:
						assert false;
				}
			}
		}
	};
	
}
