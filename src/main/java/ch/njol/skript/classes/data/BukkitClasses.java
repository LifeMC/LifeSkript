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

package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.UnresolvedOfflinePlayer;
import ch.njol.skript.classes.*;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Message;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.*;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Fields;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.eclipse.jdt.annotation.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
// TODO vectors
public final class BukkitClasses {

    static final Pattern blockDeserializePattern = Pattern.compile("[:,]");

    static final Matcher parsePatternMatcher = Pattern.compile("(?:(?:the )?world )?\"(.+?)\"", Pattern.CASE_INSENSITIVE).matcher("");
    static final Matcher validPlayerNameMatcher = Pattern.compile("\\S+").matcher("");

    // This speeds up variable saving a lot (like 8 minutes to 10 seconds for 720.000+ variables)
    // Because UUIDs are always known when OfflinePlayer/Player objects are created, but names must be read from world data.
    // It is stable as I tested it a lot, so I decided to make it default. Of course you can also disable it.
    // See https://github.com/LifeMC/LifeSkript/issues/82 for more information about this topic and problem.
    static final boolean dontUseNames = Skript.offlineUUIDSupported && System.getProperty("skript.dontUseNamesForSerialization") == null || Boolean.parseBoolean(System.getProperty("skript.dontUseNamesForSerialization"));

    private static final Pattern LOCATION_PATTERN = Pattern.compile("[:,|/]");
    private static final Pattern CHUNK_PATTERN = Pattern.compile("[:,]");

    private BukkitClasses() {
        throw new UnsupportedOperationException();
    }

    public static final void init() {
        Classes.registerClass(new ClassInfo<>(Entity.class, "entity").user("entit(y|ies)").name("Entity").description("An entity is something in a <a href='#world'>world</a> that's not a <a href='#block'>block</a>, " + "e.g. a <a href='#player'>player</a>, a skeleton, or a zombie, but also <a href='#projectile'>projectiles</a> like arrows, fireballs or thrown potions, " + "or special entities like dropped items, falling blocks or paintings.").usage("player, op, wolf, tamed ocelot, powered creeper, zombie, unsaddled pig, fireball, arrow, dropped item, item frame, etc.").examples("entity is a zombie or creeper", "player is an op", "projectile is an arrow", "shoot a fireball from the player").since("1.0").defaultExpression(new EventValueExpression<>(Entity.class)).parser(new Parser<Entity>() {
            @Override
            @Nullable
            public Entity parse(final String s, final ParseContext context) {
                return null;
            }

            @Override
            public boolean canParse(final ParseContext context) {
                return false;
            }

            @Override
            public String toVariableNameString(final Entity e) {
                return "entity:" + e.getUniqueId().toString().toLowerCase(Locale.ENGLISH);
            }

            @Override
            public String getVariableNamePattern() {
                return "entity:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
            }

            @Override
            public String toString(final Entity e, final int flags) {
                return EntityData.toString(e, flags);
            }
        }).changer(DefaultChangers.entityChanger));

        Classes.registerClass(new ClassInfo<>(LivingEntity.class, "livingentity").user("living ?entit(y|ies)").name("Living Entity").description("A living <a href='#entity'>entity</a>, i.e. a mob or <a href='#player'>player</a>, not inanimate entities like <a href='#projectile'>projectiles</a> or dropped items.").usage("see <a href='#entity'>entity</a>, but ignore inanimate objects").examples("spawn 5 powered creepers", "shoot a zombie from the creeper").since("1.0").defaultExpression(new EventValueExpression<>(LivingEntity.class)).changer(DefaultChangers.entityChanger));

        Classes.registerClass(new ClassInfo<>(Projectile.class, "projectile").user("projectiles?").name("Projectile").description("A projectile, e.g. an arrow, snowball or thrown potion.").usage("arrow, fireball, snowball, thrown potion, etc.").examples("projectile is a snowball", "shoot an arrow at speed 5 from the player").since("1.0").defaultExpression(new EventValueExpression<>(Projectile.class)).changer(DefaultChangers.nonLivingEntityChanger));

        Classes.registerClass(new ClassInfo<>(Block.class, "block").user("blocks?").name("Block").description("A block in a <a href='#world'>world</a>. It has a <a href='#location'>location</a> and a <a href='#itemstack'>type</a>, " + "and can also have a <a href='#direction'>direction</a> (mostly a <a href='../expressions/#ExprFacing'>facing</a>), an <a href='#inventory'>inventory</a>, or other special properties.").usage("").examples("").since("1.0").defaultExpression(new EventValueExpression<>(Block.class)).parser(new Parser<Block>() {
            @Override
            @Nullable
            public final Block parse(final String s, final ParseContext context) {
                return null;
            }

            @Override
            public final boolean canParse(final ParseContext context) {
                return false;
            }

            @SuppressWarnings("deprecation")
            @Override
            public final String toString(final Block b, final int flags) {
                return ItemType.toString(new ItemStack(b.getTypeId(), 1, b.getState().getRawData()), flags);
            }

            @Override
            public final String toVariableNameString(final Block b) {
                return b.getWorld().getName() + ':' + b.getX() + ',' + b.getY() + ',' + b.getZ();
            }

            @Override
            public final String getVariableNamePattern() {
                return ".+:-?\\d+,-?\\d+,-?\\d+";
            }

            @Override
            public final String getDebugMessage(final Block b) {
                return toString(b, 0) + " block (" + b.getWorld().getName() + ':' + b.getX() + ',' + b.getY() + ',' + b.getZ() + ')';
            }
        }).changer(DefaultChangers.blockChanger).serializer(new Serializer<Block>() {
            @SuppressWarnings("null")
            @Override
            public final Fields serialize(final Block b) {
                final Fields f = new Fields();
                f.putObject("world", b.getWorld());
                f.putPrimitive("x", b.getX());
                f.putPrimitive("y", b.getY());
                f.putPrimitive("z", b.getZ());
                return f;
            }

            @Override
            public final void deserialize(final Block o, final Fields f) {
                assert false;
            }

            @Override
            protected final Block deserialize(final Fields fields) throws StreamCorruptedException {
                final World w = fields.getObject("world", World.class);
                final int x = fields.getPrimitive("x", int.class), y = fields.getPrimitive("y", int.class), z = fields.getPrimitive("z", int.class);
                final Block b;
                if (w == null || (b = w.getBlockAt(x, y, z)) == null) // REMIND: hotspot, causes disk read because of block and chunk data
                    throw new StreamCorruptedException();
                return b;
            }

            @Override
            public final boolean mustSyncDeserialization() {
                return true;
            }

            @Override
            public final boolean canBeInstantiated() {
                return false;
            }

            //					return b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ();
            @Deprecated
            @Override
            @Nullable
            public final Block deserialize(final String s) {
                final String[] split = blockDeserializePattern.split(s);
                if (split.length != 4)
                    return null;
                final World w = Bukkit.getWorld(split[0]);
                if (w == null)
                    return null;
                final int[] l = new int[3];
                for (int i = 0; i < 3; i++) {
                    final String n = split[i + 1];
                    if (!SkriptParser.isInteger(n))
                        return null;
                    l[i] = Integer.parseInt(n);
                }
                return w.getBlockAt(l[0], l[1], l[2]);
            }
        }));

        Classes.registerClass(new ClassInfo<>(Location.class, "location").user("locations?").name("Location").description("A location in a <a href='#world'>world</a>. Locations are world-specific and even store a <a href='#direction'>direction</a>, " + "e.g. if you save a location and later teleport to it you will face the exact same direction you did when you saved the location.").usage("").examples("").since("1.0").defaultExpression(new EventValueExpression<>(Location.class)).parser(new Parser<Location>() {
            @Override
            @Nullable
            public final Location parse(final String s, final ParseContext context) {
                return null;
            }

            @Override
            public final boolean canParse(final ParseContext context) {
                return false;
            }

            @Override
            public final String toString(final Location l, final int flags) {
                return "x: " + Skript.toString(l.getX()) + ", y: " + Skript.toString(l.getY()) + ", z: " + Skript.toString(l.getZ());
            }

            @Override
            public final String toVariableNameString(final Location l) {
                return l.getWorld().getName() + ':' + l.getX() + ',' + l.getY() + ',' + l.getZ();
            }

            @Override
            public final String getVariableNamePattern() {
                return "\\S:-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?";
            }

            @Override
            public final String getDebugMessage(final Location l) {
                return '(' + l.getWorld().getName() + ':' + l.getX() + ',' + l.getY() + ',' + l.getZ() + "|yaw=" + l.getYaw() + "/pitch=" + l.getPitch() + ')';
            }
        }).serializer(new Serializer<Location>() {
            @Override
            public final Fields serialize(final Location l) throws NotSerializableException {
                final Fields f = new Fields();
                f.putObject("world", l.getWorld());
                f.putPrimitive("x", l.getX());
                f.putPrimitive("y", l.getY());
                f.putPrimitive("z", l.getZ());
                f.putPrimitive("yaw", l.getYaw());
                f.putPrimitive("pitch", l.getPitch());
                return f;
            }

            @Override
            public final void deserialize(final Location o, final Fields f) throws StreamCorruptedException {
                assert false;
            }

            @Override
            public final Location deserialize(final Fields f) throws StreamCorruptedException, NotSerializableException {
                return new Location(f.getObject("world", World.class), f.getPrimitive("x", double.class), f.getPrimitive("y", double.class), f.getPrimitive("z", double.class), f.getPrimitive("yaw", float.class), f.getPrimitive("pitch", float.class));
            }

            @Override
            public final boolean canBeInstantiated() {
                return false; // no nullary constructor - also, saving the location manually prevents errors should Location ever be changed
            }

            @Override
            public final boolean mustSyncDeserialization() {
                return true;
            }

            //					return l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ() + "|" + l.getYaw() + "/" + l.getPitch();
            @Override
            @Nullable
            public final Location deserialize(final String s) {
                final String[] split = LOCATION_PATTERN.split(s);
                if (split.length != 6)
                    return null;
                final World w = Bukkit.getWorld(split[0]);
                if (w == null)
                    return null;
                try {
                    final double[] l = new double[5];
                    for (int i = 0; i < 5; i++)
                        l[i] = Double.parseDouble(split[i + 1]);
                    return new Location(w, l[0], l[1], l[2], (float) l[3], (float) l[4]);
                } catch (final NumberFormatException e) {
                    return null;
                }
            }
        }));

        // FIXME update doc
        Classes.registerClass(new ClassInfo<>(World.class, "world").user("worlds?").name("World").description("One of the server's worlds. Worlds can be put into scripts by surrounding their name with double quotes, e.g. \"world_nether\", " + "but this might not work reliably as <a href='#string'>text</a> uses the same syntax.").usage("<code>\"world_name\"</code>, e.g. \"world\"").examples("broadcast \"Hello!\" to the world \"world_nether\"").since("1.0, 2.2 (alternate syntax)").after("string").defaultExpression(new EventValueExpression<>(World.class)).parser(new Parser<World>() {
            @Override
            @Nullable
            public final World parse(final String s, final ParseContext context) {
                // REMIND allow shortcuts '[over]world', 'nether' and '[the_]end' (server.properties: 'level-name=world') // inconsistent with 'world is "..."'
                if (context == ParseContext.COMMAND || context == ParseContext.CONFIG)
                    return Bukkit.getWorld(s);
                if (s.isEmpty())
                    return null;
                final Matcher m = parsePatternMatcher.reset(s.trim());
                if (m.matches())
                    return Bukkit.getWorld(m.group(1));
                return null;
            }

            @Override
            public String toString(final World w, final int flags) {
                return w.getName();
            }

            @Override
            public String toVariableNameString(final World w) {
                return w.getName();
            }

            @Override
            public String getVariableNamePattern() {
                return "\\S+";
            }
        }).serializer(new Serializer<World>() {
            @Override
            public Fields serialize(final World w) {
                final Fields f = new Fields();
                f.putObject("name", w.getName());
                return f;
            }

            @Override
            public void deserialize(final World o, final Fields f) {
                assert false;
            }

            @Override
            public boolean canBeInstantiated() {
                return false;
            }

            @Override
            protected World deserialize(final Fields fields) throws StreamCorruptedException {
                final String name = fields.getObject("name", String.class);
                final World w = Bukkit.getWorld(name);
                if (w == null)
                    throw new StreamCorruptedException("Missing world " + name);
                return w;
            }

            //					return w.getName();
            @Override
            @Nullable
            public World deserialize(final String s) {
                return Bukkit.getWorld(s);
            }

            @Override
            public boolean mustSyncDeserialization() {
                return true;
            }
        }));

        Classes.registerClass(new ClassInfo<>(Inventory.class, "inventory").user("inventor(y|ies)").name("Inventory").description("An inventory of a <a href='#player'>player</a> or <a href='#block'>block</a>. Inventories have many effects and conditions regarding the items contained.", "An inventory has a fixed amount of <a href='#slot'>slots</a> which represent a specific place in the inventory, " + "e.g. the <a href='../expressions/#ExprArmorSlot'>helmet slot</a> for players (Please note that slot support is still very limited but will be improved eventually).").usage("").examples("").since("1.0").defaultExpression(new EventValueExpression<>(Inventory.class)).parser(new Parser<Inventory>() {
            @Override
            @Nullable
            public Inventory parse(final String s, final ParseContext context) {
                return null;
            }

            @Override
            public boolean canParse(final ParseContext context) {
                return false;
            }

            @Override
            public String toString(final Inventory i, final int flags) {
                return "inventory of " + Classes.toString(i.getHolder());
            }

            @Override
            public String getDebugMessage(final Inventory i) {
                return "inventory of " + Classes.getDebugMessage(i.getHolder());
            }

            @Override
            public String toVariableNameString(final Inventory i) {
                return "inventory of " + Classes.toString(i.getHolder(), StringMode.VARIABLE_NAME);
            }

            @Override
            public String getVariableNamePattern() {
                return "inventory of .+";
            }
        }).changer(DefaultChangers.inventoryChanger));

        Classes.registerClass(new ClassInfo<>(Player.class, "player").user("players?").name("Player").description("A player. Depending on whatever a player is online or offline several actions can be performed with them, " + "though you won't get any errors when using effects that only work if the player is online (e.g. changing his inventory) on an offline player.", "You have two possibilities to use players as command arguments: &lt;player&gt; and &lt;offline player&gt;. " + "The first requires that the player is online and accepts only part of the name, " + "while the latter doesn't require that the player is online, but the player's name has to be entered exactly.").usage("").examples("").since("1.0").defaultExpression(new EventValueExpression<>(Player.class)).after("string", "world").parser(new Parser<Player>() {
            @Override
            @Nullable
            public Player parse(final String s, final ParseContext context) {
                if (context == ParseContext.COMMAND) {
                    final List<Player> ps = Bukkit.matchPlayer(s);
                    if (ps.size() == 1)
                        return ps.get(0);
                    if (ps.isEmpty())
                        Skript.error("There is no player online whose name starts with '" + s + '\'');
                    else
                        Skript.error("There are several players online whose names start with '" + s + '\'');
                    return null;
                }
//						if (s.matches("\"\\S+\""))
//							return Bukkit.getPlayerExact(s.substring(1, s.length() - 1));
                assert false;
                return null;
            }

            @Override
            public boolean canParse(final ParseContext context) {
                return context == ParseContext.COMMAND;
            }

            @Override
            public String toString(final Player p, final int flags) {
                return p.getName();
            }

            @Override
            public String toVariableNameString(final Player p) {
                if (SkriptConfig.usePlayerUUIDsInVariableNames.value())
                    return p.getUniqueId().toString();
                return p.getName();
            }

            @Override
            public String getVariableNamePattern() {
                if (SkriptConfig.usePlayerUUIDsInVariableNames.value())
                    return "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";
                return "\\S+";
            }

            @Override
            public String getDebugMessage(final Player p) {
                return p.getName() + ' ' + Classes.getDebugMessage(p.getLocation());
            }
        }).changer(DefaultChangers.playerChanger).serializeAs(OfflinePlayer.class));

        Classes.registerClass(new ClassInfo<>(OfflinePlayer.class, "offlineplayer").user("offline ?players?").name("Offlineplayer").description("A player that is possibly offline. See <a href='#player'>player</a> for more information. " + "Please note that while all effects and conditions that require a player can be used with an offline player as well, they will not work if the player is not actually online.").usage("").examples("").since("").defaultExpression(new EventValueExpression<>(OfflinePlayer.class)).after("string", "world").parser(new Parser<OfflinePlayer>() {
            @Override
            @Nullable
            public final OfflinePlayer parse(final String s, final ParseContext context) {
                if (context == ParseContext.COMMAND) {
                    if (!validPlayerNameMatcher.reset(s).matches() || s.length() > 16) {
                        Skript.error("The player name \"" + s + "\" is not a valid player name");
                        return null;
                    }
                    final Player player = Bukkit.getPlayerExact(s);
                    if (player != null)
                        return player; // Player extends OfflinePlayer
                    return new UnresolvedOfflinePlayer(s);
                }
//						if (s.matches("\"\\S+\""))
//							return Bukkit.getOfflinePlayer(s.substring(1, s.length() - 1));
                assert false;
                return null;
            }

            @Override
            public final boolean canParse(final ParseContext context) {
                return context == ParseContext.COMMAND;
            }

            @Override
            public final String toString(final OfflinePlayer p, final int flags) {
                return p.getName();
            }

            @Override
            public final String toVariableNameString(final OfflinePlayer p) {
                if (SkriptConfig.usePlayerUUIDsInVariableNames.value())
                    return p.getUniqueId().toString();
                return p.getName();
            }

            @Override
            public final String getVariableNamePattern() {
                if (SkriptConfig.usePlayerUUIDsInVariableNames.value())
                    return "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";
                return "\\S+";
            }

            @Override
            public final String getDebugMessage(final OfflinePlayer p) {
                if (p.isOnline())
                    return Classes.getDebugMessage(p.getPlayer());
                return p.getName();
            }
        }).serializer(new Serializer<OfflinePlayer>() {
            @Override
            public final Fields serialize(final OfflinePlayer p) {
                final Fields f = new Fields();
                if (Skript.offlineUUIDSupported || dontUseNames)
                    f.putObject("uuid", p.getUniqueId());
                if (!dontUseNames)
                    f.putObject("name", p.getName());
                return f;
            }

            @Override
            public final void deserialize(final OfflinePlayer o, final Fields f) {
                assert false;
            }

            @Override
            public final boolean canBeInstantiated() {
                return false;
            }

            @SuppressWarnings("deprecation")
            @Override
            protected final OfflinePlayer deserialize(final Fields fields) throws StreamCorruptedException {
                if (fields.contains("uuid") && Skript.offlineUUIDSupported || dontUseNames) {
                    final UUID uuid = fields.getObject("uuid", UUID.class);
                    final OfflinePlayer p;
                    if (uuid == null || (p = Bukkit.getOfflinePlayer(uuid)) == null)
                        throw new StreamCorruptedException();
                    return p;
                }
                final String name = fields.getObject("name", String.class);
                final OfflinePlayer p;
                if (name == null || (p = Bukkit.getOfflinePlayer(name)) == null)
                    throw new StreamCorruptedException();
                return p;
            }

            //					return p.getName();
            @Deprecated
            @Override
            @Nullable
            public final OfflinePlayer deserialize(final String s) {
                return Bukkit.getOfflinePlayer(s);
            }

            @Override
            public final boolean mustSyncDeserialization() {
                return true;
            }
        }));

        Classes.registerClass(new ClassInfo<>(CommandSender.class, "commandsender").user("(commands?)? ?(sender|executor)s?").name("Command Sender").description("A player or the console.").usage("use <a href='../expressions/#LitConsole'>the console</a> for the console", "see <a href='#player'>player</a> for players.").examples("on command /pm:", "	command sender is not the console", "	chance of 10%", "	give coal to the player", "	message \"You got a piece of coal for sending that PM!\"").since("1.0").defaultExpression(new EventValueExpression<>(CommandSender.class)).parser(new Parser<CommandSender>() {
            @Override
            @Nullable
            public CommandSender parse(final String s, final ParseContext context) {
                return null;
            }

            @Override
            public boolean canParse(final ParseContext context) {
                return false;
            }

            @Override
            public String toString(final CommandSender s, final int flags) {
                return s.getName();
            }

            @Override
            public String toVariableNameString(final CommandSender s) {
                return s.getName();
            }

            @Override
            public String getVariableNamePattern() {
                return "\\S+";
            }
        }));

        Classes.registerClass(new ClassInfo<>(InventoryHolder.class, "inventoryholder").name(ClassInfo.NO_DOC).defaultExpression(new EventValueExpression<>(InventoryHolder.class)));

        Classes.registerClass(new ClassInfo<>(GameMode.class, "gamemode").user("game ?modes?").name("Game Mode").description("The game modes survival, creative, adventure and spectator.").usage("creative/survival/adventure/spectator").examples("player's gamemode is survival", "set the player argument's game mode to creative").since("1.0").defaultExpression(new SimpleLiteral<>(GameMode.SURVIVAL, true)).parser(new Parser<GameMode>() {
            private final Message[] names = new Message[GameMode.values().length];

            {
                int i = 0;
                for (final GameMode m : GameMode.values()) {
                    names[i++] = new Message("game modes." + m.name());
                }
            }

            @Override
            @Nullable
            public GameMode parse(final String s, final ParseContext context) {
                for (int i = 0; i < names.length; i++) {
                    if (s.equalsIgnoreCase(names[i].toString()))
                        return GameMode.values()[i];
                }
                return null;
            }

            @Override
            public String toString(final GameMode m, final int flags) {
                return names[m.ordinal()].toString();
            }

            @Override
            public String toVariableNameString(final GameMode o) {
                return o.toString().toLowerCase(Locale.ENGLISH);
            }

            @Override
            public String getVariableNamePattern() {
                return "[a-z]+";
            }
        }).serializer(new EnumSerializer<>(GameMode.class)));

        Classes.registerClass(new ClassInfo<>(ItemStack.class, "itemstack").user("item", "material").name("Item / Material").description("An item, e.g. a stack of torches, a furnace, or a wooden sword of sharpness 2. " + "Unlike <a href='#itemtype'>item type</a> an item can only represent exactly one item (e.g. an upside-down cobblestone stair facing west), " + "while an item type can represent a whole range of items (e.g. any cobble stone stairs regardless of direction).", "You don't usually need this type except when you want to make a command that only accepts an exact item.", "Please note that currently 'material' is exactly the same as 'item', i.e. can have an amount & enchantments.").usage("<code>[&lt;number&gt; [of]] &lt;alias&gt; [of &lt;enchantment&gt; &lt;level&gt;]</code>, Where &lt;alias&gt; must be an alias that represents exactly one item " + "(i.e cannot be a general alias like 'sword' or 'plant')").examples("set {_item} to type of the targeted block", "{_item} is a torch").since("1.0").after("number").parser(new Parser<ItemStack>() {
            @Override
            @Nullable
            public final ItemStack parse(final String s, final ParseContext context) {
                ItemType t = Aliases.parseItemType(s);
                if (t == null)
                    return null;
                t = t.getItem();
                if (t.numTypes() != 1) {
                    Skript.error('\'' + s + "' represents multiple materials");
                    return null;
                }
                if (!t.getTypes().get(0).hasDataRange())
                    return t.getRandom();
                if (t.getTypes().get(0).dataMin > 0) {
                    Skript.error('\'' + s + "' represents multiple materials");
                    return null;
                }
                final ItemStack i = t.getRandom();
                assert i != null;
                i.setDurability((short) 0);
                return i;
            }

            @Override
            public final String toString(final ItemStack i, final int flags) {
                return ItemType.toString(i, flags);
            }

            @SuppressWarnings("deprecation")
            @Override
            public final String toVariableNameString(final ItemStack i) {
                final StringBuilder b = new StringBuilder("item:");
                b.append(i.getType().name());
                b.append(':').append(i.getDurability());
                b.append('*').append(i.getAmount());
                for (final Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
                    b.append('#').append(e.getKey().getId());
                    b.append(':').append(e.getValue());
                }
                return b.toString();
            }

            @Override
            public final String getVariableNamePattern() {
                return "item:.+";
            }
        }).serializer(new ConfigurationSerializer<>()));

        Classes.registerClass(new ClassInfo<>(Item.class, "itementity").name(ClassInfo.NO_DOC).since("2.0").changer(DefaultChangers.itemChanger));

        Classes.registerClass(new ClassInfo<>(Biome.class, "biome").user("biomes?").name("Biome").description("All possible biomes Minecraft uses to generate a world.").usage(BiomeUtils.getAllNames()).examples("biome at the player is desert").since("1.4.4").parser(new Parser<Biome>() {
            @Override
            @Nullable
            public final Biome parse(final String s, final ParseContext context) {
                return BiomeUtils.parse(s);
            }

            @Override
            public final String toString(final Biome b, final int flags) {
                return BiomeUtils.toString(b, flags);
            }

            @Override
            public final String toVariableNameString(final Biome b) {
                return b.name();
            }

            @Override
            public final String getVariableNamePattern() {
                return "\\S+";
            }
        }).serializer(new EnumSerializer<>(Biome.class)));

//		PotionEffect is not used; ItemType is used instead
        Classes.registerClass(new ClassInfo<>(PotionEffectType.class, "potioneffecttype").user("potion( ?effect)?( ?type)?s?").name("Potion Effect Type").description("A potion effect type, e.g. 'strength' or 'swiftness'.").usage(StringUtils.join(PotionEffectUtils.getNames(), ", ")).examples("apply swiftness 5 to the player", "apply potion of speed 2 to the player for 60 seconds", "remove invisibility from the victim").since("").parser(new Parser<PotionEffectType>() {
            @Override
            @Nullable
            public PotionEffectType parse(final String s, final ParseContext context) {
                return PotionEffectUtils.parseType(s);
            }

            @Override
            public String toString(final PotionEffectType p, final int flags) {
                return PotionEffectUtils.toString(p, flags);
            }

            @Override
            public String toVariableNameString(final PotionEffectType p) {
                return p.getName();
            }

            @Override
            public String getVariableNamePattern() {
                return ".+";
            }
        }).serializer(new Serializer<PotionEffectType>() {
            @Override
            public Fields serialize(final PotionEffectType o) {
                final Fields f = new Fields();
                f.putObject("name", o.getName());
                return f;
            }

            @Override
            public boolean canBeInstantiated() {
                return false;
            }

            @Override
            public void deserialize(final PotionEffectType o, final Fields f) throws StreamCorruptedException {
                assert false;
            }

            @Override
            protected PotionEffectType deserialize(final Fields fields) throws StreamCorruptedException {
                final String name = fields.getObject("name", String.class);
                final PotionEffectType t = PotionEffectType.getByName(name);
                if (t == null)
                    throw new StreamCorruptedException("Invalid PotionEffectType " + name);
                return t;
            }

            //					return o.getName();
            @Override
            @Nullable
            public PotionEffectType deserialize(final String s) {
                return PotionEffectType.getByName(s);
            }

            @Override
            public boolean mustSyncDeserialization() {
                return false;
            }
        }));

        // REMIND make my own damage cause class (that e.g. stores the attacker entity, the projectile, or the attacking block)
        Classes.registerClass(new ClassInfo<>(DamageCause.class, "damagecause").user("damage causes?").name("Damage Cause").description("The cause/type of a <a href='../events/#damage'>damage event</a>, e.g. lava, fall, fire, drowning, explosion, poison, etc.", "Please note that support for this type is very rudimentary, e.g. lava, fire and burning, as well as projectile and attack are considered different types.").usage(DamageCauseUtils.getAllNames()).examples("").since("2.0").after("itemtype", "itemstack", "entitydata", "entitytype").parser(new Parser<DamageCause>() {
            @Override
            @Nullable
            public DamageCause parse(final String s, final ParseContext context) {
                return DamageCauseUtils.parse(s);
            }

            @Override
            public String toString(final DamageCause d, final int flags) {
                return DamageCauseUtils.toString(d, flags);
            }

            @Override
            public String toVariableNameString(final DamageCause d) {
                return d.name();
            }

            @Override
            public String getVariableNamePattern() {
                return "[a-z0-9_-]+";
            }
        }).serializer(new EnumSerializer<>(DamageCause.class)));

        Classes.registerClass(new ClassInfo<>(Chunk.class, "chunk").user("chunks?").name("Chunk").description("A chunk is a cuboid of 16×16×128 (x×z×y) blocks. Chunks are spread on a fixed rectangular grid in their world.").usage("").examples("").since("2.0").parser(new Parser<Chunk>() {
            @Override
            @Nullable
            public Chunk parse(final String s, final ParseContext context) {
                return null;
            }

            @Override
            public boolean canParse(final ParseContext context) {
                return false;
            }

            @Override
            public String toString(final Chunk c, final int flags) {
                return "chunk (" + c.getX() + ',' + c.getZ() + ") of " + c.getWorld().getName();
            }

            @Override
            public String toVariableNameString(final Chunk c) {
                return c.getWorld().getName() + ':' + c.getX() + ',' + c.getZ();
            }

            @Override
            public String getVariableNamePattern() {
                return ".+:-?[0-9]+,-?[0-9]+";
            }
        }).serializer(new Serializer<Chunk>() {
            @SuppressWarnings("null")
            @Override
            public Fields serialize(final Chunk c) {
                final Fields f = new Fields();
                f.putObject("world", c.getWorld());
                f.putPrimitive("x", c.getX());
                f.putPrimitive("z", c.getZ());
                return f;
            }

            @Override
            public void deserialize(final Chunk o, final Fields f) throws StreamCorruptedException {
                assert false;
            }

            @Override
            public boolean canBeInstantiated() {
                return false;
            }

            @Override
            protected Chunk deserialize(final Fields fields) throws StreamCorruptedException {
                final World w = fields.getObject("world", World.class);
                final int x = fields.getPrimitive("x", int.class), z = fields.getPrimitive("z", int.class);
                final Chunk c;
                if (w == null || (c = w.getChunkAt(x, z)) == null)
                    throw new StreamCorruptedException();
                return c;
            }

            //					return c.getWorld().getName() + ":" + c.getX() + "," + c.getZ();
            @Override
            @Nullable
            public Chunk deserialize(final String s) {
                final String[] split = CHUNK_PATTERN.split(s);
                if (split.length != 3)
                    return null;
                final World w = Bukkit.getWorld(split[0]);
                if (w == null)
                    return null;
                try {
                    final int x = Integer.parseInt(split[1]);
                    final int z = Integer.parseInt(split[1]);
                    return w.getChunkAt(x, z);
                } catch (final NumberFormatException e) {
                    return null;
                }
            }

            @Override
            public boolean mustSyncDeserialization() {
                return true;
            }
        }));

        Classes.registerClass(new ClassInfo<>(Enchantment.class, "enchantment").user("enchantments?").name("Enchantment").description("An enchantment, e.g. 'sharpness' or 'fortune'. Unlike <a href='#enchantmenttype'>enchantment type</a> this type has no level, but you usually don't need to use this type anyway.").usage(StringUtils.join(EnchantmentType.getNames(), ", ")).examples("").since("1.4.6").before("enchantmenttype").parser(new Parser<Enchantment>() {
            @Override
            @Nullable
            public Enchantment parse(final String s, final ParseContext context) {
                return EnchantmentType.parseEnchantment(s);
            }

            @Override
            public String toString(final Enchantment e, final int flags) {
                return EnchantmentType.toString(e, flags);
            }

            @Override
            public String toVariableNameString(final Enchantment e) {
                return e.getName();
            }

            @Override
            public String getVariableNamePattern() {
                return ".+";
            }
        }).serializer(new Serializer<Enchantment>() {
            @Override
            public Fields serialize(final Enchantment e) {
                final Fields f = new Fields();
                f.putObject("name", e.getName());
                return f;
            }

            @Override
            public boolean canBeInstantiated() {
                return false;
            }

            @Override
            public void deserialize(final Enchantment o, final Fields f) throws StreamCorruptedException {
                assert false;
            }

            @Override
            protected Enchantment deserialize(final Fields fields) throws StreamCorruptedException {
                final String name = fields.getObject("name", String.class);
                final Enchantment e = Enchantment.getByName(name);
                if (e == null)
                    throw new StreamCorruptedException("Invalid enchantment " + name);
                return e;
            }

            //					return e.getId();
            @SuppressWarnings("deprecation")
            @Override
            @Nullable
            public Enchantment deserialize(final String s) {
                try {
                    return Enchantment.getById(Integer.parseInt(s));
                } catch (final NumberFormatException e) {
                    return null;
                }
            }

            @Override
            public boolean mustSyncDeserialization() {
                return false;
            }
        }));

    }
}
