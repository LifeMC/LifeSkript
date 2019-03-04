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
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.ConfigurationSerializer;
import ch.njol.skript.classes.EnumSerializer;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.localization.RegexMessage;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.Slot;
import ch.njol.skript.util.StructureType;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.VisualEffect;
import ch.njol.skript.util.WeatherType;
import ch.njol.yggdrasil.Fields;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.StreamCorruptedException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("rawtypes")
public final class SkriptClasses {
	
	public SkriptClasses() {
		super();
	}
	
	static {
		Classes.registerClass(new ClassInfo<ClassInfo>(ClassInfo.class, "classinfo").user("types?").name("Type").description("Represents a type, e.g. number, object, item type, location, block, world, entity type, etc.", "This is mostly used for expressions like 'event-&lt;type&gt;', '&lt;type&gt;-argument', 'loop-&lt;type&gt;', etc., e.g. event-world, number-argument and loop-player.").usage("See the type name patterns of all types - including this one").examples("{variable} is a number # check whether the variable contains a number, e.g. -1 or 5.5", "{variable} is a type # check whether the variable contains a type, e.g. number or player", "{variable} is an object # will always succeed if the variable is set as everything is an object, even types.", "disable PvP in the event-world", "kill the loop-entity").since("2.0").after("entitydata", "entitytype", "itemtype").parser(new Parser<ClassInfo>() {
			@Override
			@Nullable
			public ClassInfo parse(final String s, final ParseContext context) {
				return Classes.getClassInfoFromUserInput(Noun.stripIndefiniteArticle(s));
			}
			
			@Override
			public String toString(final ClassInfo c, final int flags) {
				return c.toString(flags);
			}
			
			@Override
			public String toVariableNameString(final ClassInfo c) {
				return c.getCodeName();
			}
			
			@Override
			public String getDebugMessage(final ClassInfo c) {
				return c.getCodeName();
			}
			
			@Override
			public String getVariableNamePattern() {
				return "\\S+";
			}
		}).serializer(new Serializer<ClassInfo>() {
			@Override
			public Fields serialize(final ClassInfo c) {
				final Fields f = new Fields();
				f.putObject("codeName", c.getCodeName());
				return f;
			}
			
			@Override
			public boolean canBeInstantiated() {
				return false;
			}
			
			@Override
			public void deserialize(final ClassInfo o, final Fields f) throws StreamCorruptedException {
				assert false;
			}
			
			@Override
			protected ClassInfo deserialize(final Fields fields) throws StreamCorruptedException {
				final String codeName = fields.getObject("codeName", String.class);
				if (codeName == null)
					throw new StreamCorruptedException();
				final ClassInfo<?> ci = Classes.getClassInfoNoError(codeName);
				if (ci == null)
					throw new StreamCorruptedException("Invalid ClassInfo " + codeName);
				return ci;
			}
		
//					return c.getCodeName();
			@Override
			@Nullable
			public ClassInfo deserialize(final String s) {
				return Classes.getClassInfoNoError(s);
			}
			
			@Override
			public boolean mustSyncDeserialization() {
				return false;
			}
		}));
		
		Classes.registerClass(new ClassInfo<WeatherType>(WeatherType.class, "weathertype").user("weather ?types?", "weather conditions?", "weathers?").name("Weather Type").description("The weather types sunny, rainy, and thundering.").usage("clear/sun/sunny, rain/rainy/raining, and thunder/thundering/thunderstorm").examples("is raining", "is sunny in the player's world", "message \"It is %weather in the argument's world% in %world of the argument%\"").since("1.0").defaultExpression(new SimpleLiteral<WeatherType>(WeatherType.CLEAR, true)).parser(new Parser<WeatherType>() {
			@Override
			@Nullable
			public WeatherType parse(final String s, final ParseContext context) {
				return WeatherType.parse(s);
			}
			
			@Override
			public String toString(final WeatherType o, final int flags) {
				return o.toString(flags);
			}
			
			@Override
			public String toVariableNameString(final WeatherType o) {
				return "" + o.name().toLowerCase();
			}
			
			@Override
			public String getVariableNamePattern() {
				return "[a-z]+";
			}
		}).serializer(new EnumSerializer<WeatherType>(WeatherType.class)));
		
		Classes.registerClass(new ClassInfo<ItemType>(ItemType.class, "itemtype").user("item ?types?", "items", "materials").name("Item Type").description("An item type is an alias, e.g. 'a pickaxe', 'all plants', etc., and can result in different items when added to an inventory, " + "and unlike <a href='#itemstack'>items</a> they are well suited for checking whether an inventory contains a certain item or whether a certain item is of a certain type.", "An item type can also have one or more <a href='#enchantmenttype'>enchantments</a> with or without a specific level defined, " + "and can optionally start with 'all' or 'every' to make this item type represent <i>all</i> types that the alias represents, including data ranges.").usage("<code>[&lt;number&gt; [of]] [all/every] &lt;alias&gt; [of &lt;enchantment&gt; [&lt;level&gt;] [,/and &lt;more enchantments...&gt;]]</code>").examples("give 4 torches to the player", "add all slabs to the inventory of the block", "player's tool is a diamond sword of sharpness", "remove a pickaxes of fortune 4 from {stored items::*}", "set {_item} to 10 of every upside-down stair", "block is dirt or farmland").since("1.0").before("itemstack", "entitydata", "entitytype").after("number", "integer", "long", "time").parser(new Parser<ItemType>() {
			@Override
			@Nullable
			public ItemType parse(final String s, final ParseContext context) {
				return Aliases.parseItemType(s);
			}
			
			@Override
			public String toString(final ItemType t, final int flags) {
				return t.toString(flags);
			}
			
			@Override
			public String getDebugMessage(final ItemType t) {
				return t.getDebugMessage();
			}
			
			@SuppressWarnings("deprecation")
			@Override
			public String toVariableNameString(final ItemType t) {
				final StringBuilder b = new StringBuilder("itemtype:");
				b.append(t.getInternalAmount());
				b.append(",").append(t.isAll());
				for (final ItemData d : t.getTypes()) {
					b.append(",").append(d.getId());
					b.append(":").append(d.dataMin);
					b.append("/").append(d.dataMax);
				}
				final Map<Enchantment, Integer> enchs = t.getEnchantments();
				if (enchs != null && !enchs.isEmpty()) {
					b.append("|");
					for (final Entry<Enchantment, Integer> e : enchs.entrySet()) {
						b.append("#").append(e.getKey().getId());
						b.append(":").append(e.getValue());
					}
				}
				return "" + b.toString();
			}
			
			@Override
			public String getVariableNamePattern() {
				return "itemtype:.+";
			}
		}).serializer(new YggdrasilSerializer<ItemType>() {
//						final StringBuilder b = new StringBuilder();
//						b.append(t.getInternalAmount());
//						b.append("," + t.isAll());
//						for (final ItemData d : t.getTypes()) {
//							b.append("," + d.getId());
//							b.append(":" + d.dataMin);
//							b.append("/" + d.dataMax);
//						}
//						if (t.getEnchantments() != null) {
//							b.append("|");
//							for (final Entry<Enchantment, Integer> e : t.getEnchantments().entrySet()) {
//								b.append("#" + e.getKey().getId());
//								b.append(":" + e.getValue());
//							}
//						}
//						if (t.getItemMeta() != null) {
//							b.append("¦");
//							b.append(ConfigurationSerializer.serializeCS((ItemMeta) t.getItemMeta()).replace("¦", "¦¦"));
//						}
//						return b.toString();
			@Override
			@Deprecated
			@Nullable
			public ItemType deserialize(final String s) {
				final String[] ss = s.split("\\|");
				if (ss.length > 2)
					return null;
				final String[] split = ss[0].split("[,:/]");
				if (split.length < 5 || (split.length - 2) % 3 != 0)
					return null;
				final ItemType t = new ItemType();
				try {
					t.setAmount(Integer.parseInt(split[0]));
					if ("true".equals(split[1]))
						t.setAll(true);
					else if ("false".equals(split[1]))
						t.setAll(false);
					else
						return null;
					for (int i = 2; i < split.length; i += 3) {
						t.add(new ItemData(Integer.parseInt(split[i]), Short.parseShort(split[i + 1]), Short.parseShort(split[i + 2])));
					}
				} catch (final NumberFormatException e) {
					return null;
				}
				if (ss.length == 2) {
					final String[] sss = ss[1].split("¦", 2);
					if (!sss[0].isEmpty()) {
						final String[] es = sss[0].split("#");
						for (final String e : es) {
							if (e.isEmpty())
								continue;
							final String[] en = e.split(":");
							if (en.length != 2)
								return null;
							try {
								final Enchantment ench = Enchantment.getById(Integer.parseInt(en[0]));
								if (ench == null)
									return null;
								t.addEnchantment(ench, Integer.parseInt(en[1]));
							} catch (final NumberFormatException ex) {
								return null;
							}
						}
					}
					if (sss.length == 2) {
						if (!ItemType.itemMetaSupported)
							return null;
						final ItemMeta m = ConfigurationSerializer.deserializeCSOld("" + sss[1].replace("¦¦", "¦"), ItemMeta.class);
						if (m == null)
							return null;
						t.setItemMeta(m);
					}
				}
				return t;
			}
		}));
		
		Classes.registerClass(new ClassInfo<Time>(Time.class, "time").user("times?").name("Time").description("A time is a point in a minecraft day's time (i.e. ranges from 0:00 to 23:59), which can vary per world.", "See <a href='#date'>date</a> and <a href='#timespan'>timespan</a> for the other time types of Skript.").usage("<code>##:##</code>", "<code>##[:##][ ]am/pm</code>").examples("at 20:00:", "	time is 8 pm", "	broadcast \"It's %time%\"").since("1.0").defaultExpression(new EventValueExpression<Time>(Time.class)).parser(new Parser<Time>() {
			@Override
			@Nullable
			public Time parse(final String s, final ParseContext context) {
				return Time.parse(s);
			}
			
			@Override
			public String toString(final Time t, final int flags) {
				return t.toString();
			}
			
			@Override
			public String toVariableNameString(final Time o) {
				return "time:" + o.getTicks();
			}
			
			@Override
			public String getVariableNamePattern() {
				return "time:\\d+";
			}
		}).serializer(new YggdrasilSerializer<Time>() {
//						return "" + t.getTicks();
			@Override
			@Nullable
			public Time deserialize(final String s) {
				try {
					return new Time(Integer.parseInt(s));
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public boolean mustSyncDeserialization() {
				return false;
			}
		}));
		
		Classes.registerClass(new ClassInfo<Timespan>(Timespan.class, "timespan").user("time ?spans?").name("Timespan").description("A timespan is a difference of two different dates or times, e.g '10 minutes'. Timespans are always displayed as real life time, but can be defined as minecraft time, e.g. '5 minecraft days and 12 hours'.", "See <a href='#date'>date</a> and <a href='#time'>time</a> for the other time types of Skript.").usage("<code>&lt;number&gt; [minecraft/mc/real/rl/irl] ticks/seconds/minutes/hours/days [[,/and] &lt;more...&gt;</code>]", "<code>[###:]##:##[.####]</code> ([hours:]minutes:seconds[.milliseconds])").examples("every 5 minecraft days:", "	wait a minecraft second and 5 ticks", "every 10 mc days and 12 hours:", "	halt for 12.7 irl minutes, 12 hours and 120.5 seconds").since("1.0").parser(new Parser<Timespan>() {
			@Override
			@Nullable
			public Timespan parse(final String s, final ParseContext context) {
				try {
					return Timespan.parse(s);
				} catch (final IllegalArgumentException e) {
					Skript.error("'" + s + "' is not a valid timespan");
					return null;
				}
			}
			
			@Override
			public String toString(final Timespan t, final int flags) {
				return t.toString(flags);
			}
			
			@Override
			public String toVariableNameString(final Timespan o) {
				return "timespan:" + o.getMilliSeconds();
			}
			
			@Override
			public String getVariableNamePattern() {
				return "timespan:\\d+";
			}
		}).serializer(new YggdrasilSerializer<Timespan>() {
//						return "" + t.getMilliSeconds();
			@Override
			@Nullable
			public Timespan deserialize(final String s) {
				try {
					return new Timespan(Long.parseLong(s));
				} catch (final NumberFormatException e) {
					return null;
				}
			}
			
			@Override
			public boolean mustSyncDeserialization() {
				return false;
			}
		}).math(Timespan.class, new Arithmetic<Timespan, Timespan>() {
			@Override
			public Timespan difference(final Timespan t1, final Timespan t2) {
				return new Timespan(Math.abs(t1.getMilliSeconds() - t2.getMilliSeconds()));
			}
			
			@Override
			public Timespan add(final Timespan value, final Timespan difference) {
				return new Timespan(value.getMilliSeconds() + difference.getMilliSeconds());
			}
			
			@Override
			public Timespan subtract(final Timespan value, final Timespan difference) {
				return new Timespan(Math.max(0, value.getMilliSeconds() - difference.getMilliSeconds()));
			}
		}));
		
		// TODO remove
		Classes.registerClass(new ClassInfo<Timeperiod>(Timeperiod.class, "timeperiod").user("time ?periods?", "durations?").name("Timeperiod").description("A period of time between two <a href='#time'>times</a>. Mostly useful since you can use this to test for whether it's day, night, dusk or dawn in a specific world.", "This type might be removed in the future as you can use 'time of world is between x and y' as a replacement.").usage("<code>##:## - ##:##</code>", "dusk/day/dawn/night").examples("time in world is night").since("1.0").before("timespan") // otherwise "day" gets parsed as '1 day'
				.defaultExpression(new SimpleLiteral<Timeperiod>(new Timeperiod(0, 23999), true)).parser(new Parser<Timeperiod>() {
					@Override
					@Nullable
					public Timeperiod parse(final String s, final ParseContext context) {
						if ("day".equalsIgnoreCase(s)) {
							return new Timeperiod(0, 11999);
						} else if ("dusk".equalsIgnoreCase(s)) {
							return new Timeperiod(12000, 13799);
						} else if ("night".equalsIgnoreCase(s)) {
							return new Timeperiod(13800, 22199);
						} else if ("dawn".equalsIgnoreCase(s)) {
							return new Timeperiod(22200, 23999);
						}
						final int c = s.indexOf('-');
						if (c == -1) {
							final Time t = Time.parse(s);
							if (t == null)
								return null;
							return new Timeperiod(t.getTicks());
						}
						final Time t1 = Time.parse("" + s.substring(0, c).trim());
						final Time t2 = Time.parse("" + s.substring(c + 1).trim());
						if (t1 == null || t2 == null)
							return null;
						return new Timeperiod(t1.getTicks(), t2.getTicks());
					}
					
					@Override
					public String toString(final Timeperiod o, final int flags) {
						return o.toString();
					}
					
					@Override
					public String toVariableNameString(final Timeperiod o) {
						return "timeperiod:" + o.start + "-" + o.end;
					}
					
					@Override
					public String getVariableNamePattern() {
						return "timeperiod:\\d+-\\d+";
					}
				}).serializer(new YggdrasilSerializer<Timeperiod>() {
//						return t.start + "-" + t.end;
					@Override
					@Nullable
					public Timeperiod deserialize(final String s) {
						final String[] split = s.split("-");
						if (split.length != 2)
							return null;
						try {
							return new Timeperiod(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
						} catch (final NumberFormatException e) {
							return null;
						}
					}
				}));
		
		Classes.registerClass(new ClassInfo<Date>(Date.class, "date").user("dates?").name("Date").description("A date is a certain point in the real world's time which can currently only be obtained with <a href='../expressions/#ExprNow'>now</a>.", "See <a href='#time'>time</a> and <a href='#timespan'>timespan</a> for the other time types of Skript.").usage("").examples("set {_yesterday} to now", "subtract a day from {_yesterday}", "# now {_yesterday} represents the date 24 hours before now").since("1.4").serializer(new YggdrasilSerializer<Date>() {
//						return "" + d.getTimestamp();
			@Override
			@Nullable
			public Date deserialize(final String s) {
				try {
					return new Date(Long.parseLong(s));
				} catch (final NumberFormatException e) {
					return null;
				}
			}
		}).math(Timespan.class, new Arithmetic<Date, Timespan>() {
			@Override
			public Timespan difference(final Date first, final Date second) {
				return first.difference(second);
			}
			
			@Override
			public Date add(final Date value, final Timespan difference) {
				return new Date(value.getTimestamp() + difference.getMilliSeconds());
			}
			
			@Override
			public Date subtract(final Date value, final Timespan difference) {
				return new Date(value.getTimestamp() - difference.getMilliSeconds());
			}
		}));
		
		Classes.registerClass(new ClassInfo<Direction>(Direction.class, "direction").user("directions?").name("Direction").description("A direction, e.g. north, east, behind, 5 south east, 1.3 meters to the right, etc.", "<a href='#location'>Locations</a> and some <a href='#block'>blocks</a> also have a direction, but without a length.", "Please note that directions have changed extensively in the betas and might not work perfectly. They can also not be used as command arguments.").usage("see <a href='../expressions/#ExprDirection'>direction (expression)</a>").examples("set the block below the victim to a chest", "loop blocks from the block infront of the player to the block 10 below the player:", "	set the block behind the loop-block to water").since("2.0").defaultExpression(new SimpleLiteral<Direction>(new Direction(new double[] {0, 0, 0}), true)).parser(new Parser<Direction>() {
			@Override
			@Nullable
			public Direction parse(final String s, final ParseContext context) {
				return null;
			}
			
			@Override
			public boolean canParse(final ParseContext context) {
				return false;
			}
			
			@Override
			public String toString(final Direction o, final int flags) {
				return o.toString();
			}
			
			@Override
			public String toVariableNameString(final Direction o) {
				return o.toString();
			}
			
			@Override
			public String getVariableNamePattern() {
				return ".*";
			}
		}).serializer(new YggdrasilSerializer<Direction>() {
//						return o.serialize();
			@Override
			@Deprecated
			@Nullable
			public Direction deserialize(final String s) {
				return Direction.deserialize(s);
			}
		}));
		
		Classes.registerClass(new ClassInfo<Slot>(Slot.class, "slot").user("(inventory )?slots?").name("Inventory Slot").description("Represents a single slot of an <a href='#inventory'>inventory</a>. " + "Notable slots are the <a href='../expressions/#ExprArmorSlot'>armour slots</a> and <a href='../expressions/#ExprFurnaceSlot'>furnace slots</a>. ", "The most important property that distinguishes a slot from an <a href='#itemstack'>item</a> is its ability to be changed, e.g. it can be set, deleted, enchanted, etc. " + "(Some item expressions can be changed as well, e.g. items stored in variables. " + "For that matter: slots are never saved to variables, only the items they represent at the time when the variable is set).", "Please note that <a href='../expressions/#ExprTool'>tool</a> can be regarded a slot, but it can actually change it's position, i.e. doesn't represent always the same slot.").usage("").examples("set tool of player to dirt", "delete helmet of the victim", "set the colour of the player's tool to green", "enchant the player's chestplate with projectile protection 5").since("").defaultExpression(new EventValueExpression<Slot>(Slot.class)).changer(new Changer<Slot>() {
			@SuppressWarnings("unchecked")
			@Override
			@Nullable
			public Class<Object>[] acceptChange(final ChangeMode mode) {
				if (mode == ChangeMode.RESET)
					return null;
				return new Class[] {ItemType.class, ItemStack.class};
			}
			
			@Override
			public void change(final Slot[] slots, final @Nullable Object[] deltas, final ChangeMode mode) {
				final Object delta = deltas == null ? null : deltas[0];
				for (final Slot slot : slots) {
					switch (mode) {
						case SET:
							assert delta != null;
							slot.setItem(delta instanceof ItemStack ? (ItemStack) delta : ((ItemType) delta).getItem().getRandom());
							break;
						case ADD:
							assert delta != null;
							if (delta instanceof ItemStack) {
								final ItemStack i = slot.getItem();
								if (i == null || i.getType() == Material.AIR || Utils.itemStacksEqual(i, (ItemStack) delta)) {
									if (i != null && i.getType() != Material.AIR) {
										i.setAmount(Math.min(i.getAmount() + ((ItemStack) delta).getAmount(), i.getMaxStackSize()));
										slot.setItem(i);
									} else {
										slot.setItem((ItemStack) delta);
									}
								}
							} else {
								slot.setItem(((ItemType) delta).getItem().addTo(slot.getItem()));
							}
							break;
						case REMOVE:
						case REMOVE_ALL:
							assert delta != null;
							if (delta instanceof ItemStack) {
								final ItemStack i = slot.getItem();
								if (i != null && Utils.itemStacksEqual(i, (ItemStack) delta)) {
									final int a = mode == ChangeMode.REMOVE_ALL ? 0 : i.getAmount() - ((ItemStack) delta).getAmount();
									if (a <= 0) {
										slot.setItem(null);
									} else {
										i.setAmount(a);
										slot.setItem(i);
									}
								}
							} else {
								if (mode == ChangeMode.REMOVE)
									slot.setItem(((ItemType) delta).removeFrom(slot.getItem()));
								else
									// REMOVE_ALL
									slot.setItem(((ItemType) delta).removeAll(slot.getItem()));
							}
							break;
						case DELETE:
							slot.setItem(null);
							break;
						case RESET:
							assert false;
					}
				}
			}
		}).serializeAs(ItemStack.class));
		
		Classes.registerClass(new ClassInfo<Color>(Color.class, "color").user("colou?rs?").name("Colour").description("Wool, dye and chat colours.").usage("black, dark grey/dark gray, grey/light grey/gray/light gray/silver, white, blue/dark blue, cyan/aqua/dark cyan/dark aqua, light blue/light cyan/light aqua, green/dark green, light green/lime/lime green, yellow/light yellow, orange/gold/dark yellow, red/dark red, pink/light red, purple/dark purple, magenta/light purple, brown/indigo").examples("color of the sheep is red or black", "set the colour of the block to green", "message \"You're holding a <%color of tool%>%color of tool%<reset> wool block\"").since("").parser(new Parser<Color>() {
			@Override
			@Nullable
			public Color parse(final String s, final ParseContext context) {
				return Color.byName(s);
			}
			
			@Override
			public String toString(final Color c, final int flags) {
				return c.toString();
			}
			
			@Override
			public String toVariableNameString(final Color o) {
				return "" + o.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
			}
			
			@Override
			public String getVariableNamePattern() {
				return "[a-z ]+";
			}
		}).serializer(new EnumSerializer<Color>(Color.class)));
		
		Classes.registerClass(new ClassInfo<StructureType>(StructureType.class, "structuretype").user("tree ?types?", "trees?").name("Tree Type").description("A tree type represents a tree species or a huge mushroom species. These can be generated in a world with the <a href='../effects/#EffTree'>generate tree</a> effect.").usage("<code>[any] &lt;general tree/mushroom type&gt;</code>, e.g. tree/any jungle tree/etc.", "<code>&lt;specific tree/mushroom species&gt;</code>, e.g. red mushroom/small jungle tree/big regular tree/etc.").examples("grow any regular tree at the block", "grow a huge red mushroom above the block").since("").defaultExpression(new SimpleLiteral<StructureType>(StructureType.TREE, true)).parser(new Parser<StructureType>() {
			@Override
			@Nullable
			public StructureType parse(final String s, final ParseContext context) {
				return StructureType.fromName(s);
			}
			
			@Override
			public String toString(final StructureType o, final int flags) {
				return o.toString(flags);
			}
			
			@Override
			public String toVariableNameString(final StructureType o) {
				return "" + o.name().toLowerCase();
			}
			
			@Override
			public String getVariableNamePattern() {
				return "[a-z ]+";
			}
		}).serializer(new EnumSerializer<StructureType>(StructureType.class)));
		
		Classes.registerClass(new ClassInfo<EnchantmentType>(EnchantmentType.class, "enchantmenttype").user("enchant(ing|ment) types?").name("Enchantment Type").description("An enchantment with an optional level, e.g. 'sharpness 2' or 'fortune'.").usage("<code>&lt;enchantment&gt; [&lt;level&gt;]</code>").examples("enchant the player's tool with sharpness 5", "helmet is enchanted with waterbreathing").since("1.4.6").parser(new Parser<EnchantmentType>() {
			@Override
			@Nullable
			public EnchantmentType parse(final String s, final ParseContext context) {
				return EnchantmentType.parse(s);
			}
			
			@Override
			public String toString(final EnchantmentType t, final int flags) {
				return t.toString();
			}
			
			@Override
			public String toVariableNameString(final EnchantmentType o) {
				return o.toString();
			}
			
			@Override
			public String getVariableNamePattern() {
				return ".+";
			}
		}).serializer(new YggdrasilSerializer<EnchantmentType>() {
//						return o.getType().getId() + ":" + o.getLevel();
			@SuppressWarnings("deprecation")
			@Override
			@Nullable
			public EnchantmentType deserialize(final String s) {
				final String[] split = s.split(":");
				if (split.length != 2)
					return null;
				try {
					final Enchantment ench = Enchantment.getById(Integer.parseInt(split[0]));
					if (ench == null)
						return null;
					return new EnchantmentType(ench, Integer.parseInt(split[1]));
				} catch (final NumberFormatException e) {
					return null;
				}
			}
		}));
		
		Classes.registerClass(new ClassInfo<Experience>(Experience.class, "experience").name("Experience").description("Experience points. Please note that Bukkit only allows to give XP, but not remove XP from players. " + "You can however change a player's <a href='../expressions/#ExprLevel'>level</a> and <a href='../expressions/#ExprLevelProgress'>level progress</a> freely.").usage("<code>[&lt;number&gt;] ([e]xp|experience [point[s]])</code>").examples("give 10 xp to the player").since("2.0").parser(new Parser<Experience>() {
			private final RegexMessage pattern = new RegexMessage("types.experience.pattern", Pattern.CASE_INSENSITIVE);
			
			@Override
			@Nullable
			public Experience parse(String s, final ParseContext context) {
				int xp = -1;
				if (s.matches("\\d+ .+")) {
					xp = Utils.parseInt("" + s.substring(0, s.indexOf(' ')));
					s = "" + s.substring(s.indexOf(' ') + 1);
				}
				if (pattern.matcher(s).matches())
					return new Experience(xp);
				return null;
			}
			
			@Override
			public String toString(final Experience xp, final int flags) {
				return xp.toString();
			}
			
			@Override
			public String toVariableNameString(final Experience xp) {
				return "" + xp.getXP();
			}
			
			@Override
			public String getVariableNamePattern() {
				return "\\d+";
			}
		}).serializer(new YggdrasilSerializer<Experience>() {
//						return "" + xp;
			@Override
			@Nullable
			public Experience deserialize(final String s) {
				try {
					return new Experience(Integer.parseInt(s));
				} catch (final NumberFormatException e) {
					return null;
				}
			}
		}));
		
		Classes.registerClass(new ClassInfo<VisualEffect>(VisualEffect.class, "visualeffect").name("Visual Effect").description("A visible effect, e.g. particles.").examples("show wolf hearts on the clicked wolf", "play mob spawner flames at the targeted block to the player").usage(VisualEffect.getAllNames()).since("2.1").user("(visual|particle) effects?").parser(new Parser<VisualEffect>() {
			@Override
			@Nullable
			public VisualEffect parse(final String s, final ParseContext context) {
				return VisualEffect.parse(s);
			}
			
			@Override
			public String toString(final VisualEffect e, final int flags) {
				return e.toString(flags);
			}
			
			@Override
			public String toVariableNameString(final VisualEffect e) {
				return e.toString();
			}
			
			@Override
			public String getVariableNamePattern() {
				return ".*";
			}
		}).serializer(new YggdrasilSerializer<VisualEffect>()));
	}
	
}
