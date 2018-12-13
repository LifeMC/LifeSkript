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

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.util.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Name / Display Name")
@Description({"Represents a player's minecraft account name, chat display name, or playerlist name, or the custom name of an item or <a href='../classes/#livingentity'>a living entity</a>.", "The differences between the different names are:", "<ul>", "<li>name: Minecraft account name of a player (unmodifiable), or the custom name of an item or mob (modifiable).</li>", "<li>display name: The name of a player as displayed in the chat and messages, e.g. when including %player% in a message. This name can be changed freely and can include colour codes, and is shared among all plugins (e.g. chat plugins will use a changed name).</li>", "<li>tab list name: The name of a player used in the player lists that usually opens with the tab key. Please note that this is limited to 16 characters, including colour codes which are counted as 2 characters each, and that no two players can have the same tab list name at the same time.</li>", "</ul>"})
@Examples({"on join:", "	player has permission \"name.red\"", "	set the player's display name to \"<red>[admin]<gold>%name of player%\"", "	set the player's tablist name to \"<green>%name of player%\"", "set the name of the player's tool to \"Legendary Sword of Awesomeness\""})
@Since("1.4.6 (players' name & display name), <i>unknown</i> (player list name), 2.0 (item name)")
public class ExprName extends SimplePropertyExpression<Object, String> {
	
	final static int ITEMSTACK = 1, ENTITY = 2, PLAYER = 4;
	final static String[] types = {"itemstacks/slots", "livingentities", "players"};
	
	private enum NameType {
		NAME("name", "name[s]", PLAYER | ITEMSTACK | ENTITY, ITEMSTACK | ENTITY) {
			@Override
			void set(final @Nullable Object o, final @Nullable String s) {
				if (o == null)
					return;
				if (o instanceof LivingEntity) {
					((LivingEntity) o).setCustomName(s);
					((LivingEntity) o).setRemoveWhenFarAway(false);
				} else if (o instanceof ItemStack) {
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					if (m != null) {
						m.setDisplayName(s);
						((ItemStack) o).setItemMeta(m);
					}
				} else {
					assert false;
				}
			}
			
			@Override
			@Nullable
			String get(final @Nullable Object o) {
				if (o == null)
					return null;
				if (o instanceof Player) {
					return ((Player) o).getName();
				} else if (o instanceof LivingEntity) {
					return ((LivingEntity) o).getCustomName();
				} else if (o instanceof ItemStack) {
					if (!((ItemStack) o).hasItemMeta())
						return null;
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					return m == null || !m.hasDisplayName() ? null : m.getDisplayName();
				} else {
					assert false;
					return null;
				}
			}
		},
		DISPLAY_NAME("display name", "(display|nick|chat)[ ]name[s]", PLAYER | ITEMSTACK | ENTITY, PLAYER | ITEMSTACK | ENTITY) {
			@Override
			void set(final @Nullable Object o, final @Nullable String s) {
				if (o == null)
					return;
				if (o instanceof Player) {
					((Player) o).setDisplayName(s == null ? ((Player) o).getName() : s + ChatColor.RESET);
				} else if (o instanceof LivingEntity) {
					((LivingEntity) o).setCustomName(s);
					((LivingEntity) o).setCustomNameVisible(s != null);
					((LivingEntity) o).setRemoveWhenFarAway(false);
				} else if (o instanceof ItemStack) {
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					if (m != null) {
						m.setDisplayName(s);
						((ItemStack) o).setItemMeta(m);
					}
				} else {
					assert false;
				}
			}
			
			@Override
			@Nullable
			String get(final @Nullable Object o) {
				if (o == null)
					return null;
				if (o instanceof Player) {
					return ((Player) o).getDisplayName();
				} else if (o instanceof LivingEntity) {
					return ((LivingEntity) o).getCustomName();
				} else if (o instanceof ItemStack) {
					if (!((ItemStack) o).hasItemMeta())
						return null;
					final ItemMeta m = ((ItemStack) o).getItemMeta();
					return m == null || !m.hasDisplayName() ? null : m.getDisplayName();
				} else {
					assert false;
					return null;
				}
			}
		},
		TABLIST_NAME("player list name", "(player|tab)[ ]list name[s]", PLAYER, PLAYER) {
			@Override
			void set(final @Nullable Object o, final @Nullable String s) {
				if (o == null)
					return;
				if (o instanceof Player) {
					try {
						((Player) o).setPlayerListName(s == null ? "" : s.length() > 16 ? s.substring(0, 16) : s);
					} catch (final IllegalArgumentException ignored) {}
				} else {
					assert false;
				}
			}
			
			@Override
			@Nullable
			String get(final @Nullable Object o) {
				if (o == null)
					return null;
				if (o instanceof Player) {
					return ((Player) o).getPlayerListName();
				} else {
					assert false;
					return null;
				}
			}
		};
		
		final String name;
		final String pattern;
		final int from;
		final int acceptChange;
		
		NameType(final String name, final String pattern, final int from, final int change) {
			this.name = name;
			this.pattern = "(" + ordinal() + "¦)" + pattern;
			this.from = from;
			acceptChange = change;
		}
		
		abstract void set(@Nullable Object o, @Nullable String s);
		
		@Nullable
		abstract String get(@Nullable Object o);
		
		String getFrom() {
			final StringBuilder b = new StringBuilder();
			for (int i = 0; i < types.length; i++) {
				if ((from & 1 << i) == 0)
					continue;
				if (1 << i == ITEMSTACK && !Skript.isRunningMinecraft(1, 4, 5))
					continue;
				if (1 << i == ENTITY && !Skript.isRunningMinecraft(1, 5))
					continue;
				if (b.length() != 0)
					b.append("/");
				b.append(types[i]);
			}
			return "" + b;
		}
	}
	
	static {
		for (final NameType n : NameType.values()) {
			register(ExprName.class, String.class, n.pattern, n.getFrom());
		}
	}
	
	@SuppressWarnings("null")
	private NameType type;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		type = NameType.values()[parseResult.mark];
		if (exprs[0] instanceof Variable)
			setExpr(exprs[0].getConvertedExpression(Object.class));
		else
			setExpr(exprs[0]);
		return true;
	}
	
	@Override
	public Class<String> getReturnType() {
		return String.class;
	}
	
	@Override
	protected String getPropertyName() {
		return type.name;
	}
	
	@Override
	@Nullable
	public String convert(final Object o) {
		return type.get(o instanceof Slot ? ((Slot) o).getItem() : o);
	}
	
	private int changeType;
	
	// TODO find a better method for handling changes (in general)
	// e.g. a Changer that takes an object and returns another which should then be saved if applicable (the Changer includes the ChangeMode)
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.DELETE && (type.acceptChange & ~PLAYER) != 0 || mode == ChangeMode.RESET)
			return new Class[0];
		if (mode != ChangeMode.SET)
			return null;
		if ((type.acceptChange & PLAYER) != 0 && Player.class.isAssignableFrom(getExpr().getReturnType())) {
			changeType = PLAYER;
		} else if ((type.acceptChange & ITEMSTACK) != 0 && (getExpr().isSingle() && ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, ItemStack.class, ItemType.class) || Slot.class.isAssignableFrom(getExpr().getReturnType()))) {
			changeType = ITEMSTACK;
		} else if ((type.acceptChange & ENTITY) != 0 && LivingEntity.class.isAssignableFrom(getExpr().getReturnType())) {
			if (type == NameType.NAME && Player.class.isAssignableFrom(getExpr().getReturnType())) {
				Skript.error("Cannot change the Minecraft name of a player. Change the 'display name of <player>' or 'tablist name of <player>' instead.");
				return null;
			}
			changeType = ENTITY;
		}
		return changeType == 0 ? null : CollectionUtils.array(String.class);
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		final String name = delta == null ? null : (String) delta[0];
		if (changeType == ITEMSTACK) {
			if (Slot.class.isAssignableFrom(getExpr().getReturnType())) {
				for (final Slot s : (Slot[]) getExpr().getArray(e)) {
					final ItemStack i = s.getItem();
					type.set(i, name);
					s.setItem(i);
				}
			} else {
				final Object i = getExpr().getSingle(e);
				if (!(i instanceof ItemStack) && !(i instanceof Slot))
					return;
				final ItemStack is = i instanceof Slot ? ((Slot) i).getItem() : (ItemStack) i;
				type.set(is, name);
				if (i instanceof Slot)
					((Slot) i).setItem(is);
				else if (ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, ItemStack.class))
					getExpr().change(e, new Object[] {i}, ChangeMode.SET);
				else
					getExpr().change(e, new ItemType[] {new ItemType((ItemStack) i)}, ChangeMode.SET);
			}
		} else {
			for (final Object o : getExpr().getArray(e)) {
				if (o instanceof LivingEntity || o instanceof Player)
					type.set(o, name);
			}
		}
	}
}
