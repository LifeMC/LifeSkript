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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.util.*;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings({"rawtypes", "deprecation"})
public final class DefaultComparators {

    static final Map<Class<? extends Entity>, Material> entityMaterials = new LinkedHashMap<>(100);

    // EntityData - ItemType
    public static final Comparator<EntityData, ItemType> entityItemComparator = (e, i) -> {
        if (e instanceof Item)
            return Relation.get(i.isOfType(((Item) e).getItemStack()));
        if (e instanceof ThrownPotion)
            return Relation.get(i.isOfType(Material.POTION.getId(), PotionEffectUtils.guessData((ThrownPotion) e)));
        if (Skript.classExists("org.bukkit.entity.WitherSkull") && e instanceof WitherSkull)
            return Relation.get(i.isOfType(Material.SKULL_ITEM.getId(), (short) 1));
        if (entityMaterials.containsKey(e.getType()))
            return Relation.get(i.isOfType(entityMaterials.get(e.getType()).getId(), (short) 0));
        for (final Entry<Class<? extends Entity>, Material> m : entityMaterials.entrySet()) {
            if (m.getKey().isAssignableFrom(e.getType()))
                return Relation.get(i.isOfType(m.getValue().getId(), (short) 0));
        }
        return Relation.NOT_EQUAL;
    };

    static {
        // to fix comparisons of eggs, arrows, etc. (e.g. 'projectile is an arrow')
        // TODO !Update with every version [entities]
        if (Skript.fieldExists(Material.class, "BOAT"))
            entityMaterials.put(Boat.class, Material.BOAT);
        entityMaterials.put(Painting.class, Material.PAINTING);
        entityMaterials.put(Arrow.class, Material.ARROW);
        entityMaterials.put(Egg.class, Material.EGG);
        if (Skript.fieldExists(Material.class, "RAW_CHICKEN"))
            entityMaterials.put(Chicken.class, Material.RAW_CHICKEN);
        entityMaterials.put(EnderPearl.class, Material.ENDER_PEARL);
        entityMaterials.put(Snowball.class, Material.SNOW_BALL);
        entityMaterials.put(ThrownExpBottle.class, Material.EXP_BOTTLE);
        if (Skript.classExists("org.bukkit.entity.FishHook")) {
            entityMaterials.put(FishHook.class, Material.FISHING_ROD);
        } else if (Skript.classExists("org.bukkit.entity.Fish")) {
            entityMaterials.put(Fish.class, Material.FISHING_ROD);
        }
        entityMaterials.put(TNTPrimed.class, Material.TNT);
        entityMaterials.put(Slime.class, Material.SLIME_BALL);
        if (Skript.classExists("org.bukkit.entity.ItemFrame"))
            entityMaterials.put(ItemFrame.class, Material.ITEM_FRAME);
        if (Skript.classExists("org.bukkit.entity.ArmorStand"))
            entityMaterials.put(ArmorStand.class, Material.ARMOR_STAND);
        if (Skript.classExists("org.bukkit.entity.Firework"))
            entityMaterials.put(Firework.class, Material.FIREWORK);
        if (Skript.classExists("org.bukkit.entity.minecart.StorageMinecart")) {
            entityMaterials.put(org.bukkit.entity.minecart.StorageMinecart.class, Material.STORAGE_MINECART);
            entityMaterials.put(org.bukkit.entity.minecart.PoweredMinecart.class, Material.POWERED_MINECART);
            entityMaterials.put(RideableMinecart.class, Material.MINECART);
            entityMaterials.put(HopperMinecart.class, Material.HOPPER_MINECART);
            entityMaterials.put(ExplosiveMinecart.class, Material.EXPLOSIVE_MINECART);
        } else if (Skript.classExists("org.bukkit.entity.StorageMinecart")) {
            entityMaterials.put(StorageMinecart.class, Material.STORAGE_MINECART);
            entityMaterials.put(PoweredMinecart.class, Material.POWERED_MINECART);
        }
        entityMaterials.put(Minecart.class, Material.MINECART);
    }

    private DefaultComparators() {
        throw new UnsupportedOperationException();
    }

    public static final void init() {
        // EntityData - ItemType
        Comparators.registerComparator(EntityData.class, ItemType.class, entityItemComparator);

        // Number - Number
        Comparators.registerComparator(Number.class, Number.class, new Comparator<Number, Number>() {
            @Override
            public Relation compare(final Number n1, final Number n2) {
                if (n1 instanceof Long && n2 instanceof Long)
                    return Relation.get(n1.longValue() - n2.longValue());
                final double diff = n1.doubleValue() - n2.doubleValue();
                if (Math.abs(diff) < Skript.EPSILON)
                    return Relation.EQUAL;
                return Relation.get(diff);
            }

            @Override
            public boolean supportsOrdering() {
                return true;
            }
        });

        // ItemStack - ItemType
        Comparators.registerComparator(ItemStack.class, ItemType.class, (is, it) -> Relation.get(it.isOfType(is)));

        // Block - ItemType
        Comparators.registerComparator(Block.class, ItemType.class, (b, it) -> Relation.get(it.isOfType(b)));

        // ItemType - ItemType
        Comparators.registerComparator(ItemType.class, ItemType.class, (i1, i2) -> Relation.get(i2.isSupertypeOf(i1)));

        // Block - Block
        Comparators.registerComparator(Block.class, Block.class, (b1, b2) -> Relation.get(b1.equals(b2)));

        // Entity - EntityData
        Comparators.registerComparator(Entity.class, EntityData.class, (e, t) -> Relation.get(t.isInstance(e)));
        // EntityData - EntityData
        Comparators.registerComparator(EntityData.class, EntityData.class, (t1, t2) -> Relation.get(t2.isSupertypeOf(t1)));
        // CommandSender - CommandSender
        Comparators.registerComparator(CommandSender.class, CommandSender.class, (s1, s2) -> Relation.get(s1.equals(s2)));

        // OfflinePlayer - OfflinePlayer
        Comparators.registerComparator(OfflinePlayer.class, OfflinePlayer.class, (p1, p2) -> Relation.get(Objects.equals(p1.getName(), p2.getName())));

        // OfflinePlayer - String
        Comparators.registerComparator(OfflinePlayer.class, String.class, (p, name) -> {
            final String offlineName = p.getName();
            return offlineName == null ? Relation.NOT_EQUAL : Relation.get(offlineName.equalsIgnoreCase(name));
        });

        // World - String
        Comparators.registerComparator(World.class, String.class, (w, name) -> Relation.get(w.getName().equalsIgnoreCase(name)));

        // String - String
        Comparators.registerComparator(String.class, String.class, (s1, s2) -> Relation.get(StringUtils.equals(s1, s2, SkriptConfig.caseSensitive.value())));

        // Date - Date
        Comparators.registerComparator(Date.class, Date.class, new Comparator<Date, Date>() {
            @Override
            public Relation compare(final Date d1, final Date d2) {
                return Relation.get(d1.compareTo(d2));
            }

            @Override
            public boolean supportsOrdering() {
                return true;
            }
        });

        // Time - Time
        Comparators.registerComparator(Time.class, Time.class, new Comparator<Time, Time>() {
            @Override
            public Relation compare(final Time t1, final Time t2) {
                return Relation.get(t1.getTime() - t2.getTime());
            }

            @Override
            public boolean supportsOrdering() {
                return true;
            }
        });

        // Timespan - Timespan
        Comparators.registerComparator(Timespan.class, Timespan.class, new Comparator<Timespan, Timespan>() {
            @Override
            public Relation compare(final Timespan t1, final Timespan t2) {
                return Relation.get(t1.getMilliSeconds() - t2.getMilliSeconds());
            }

            @Override
            public boolean supportsOrdering() {
                return true;
            }
        });

        // Time - Timeperiod
        Comparators.registerComparator(Time.class, Timeperiod.class, (t, p) -> Relation.get(p.contains(t)));

        // StructureType - StructureType
        Comparators.registerComparator(StructureType.class, StructureType.class, (s1, s2) -> Relation.get(CollectionUtils.containsAll(s2.getTypes(), s2.getTypes())));

        // Object - ClassInfo
        Comparators.registerComparator(Object.class, ClassInfo.class, (o, c) -> Relation.get(c.getC().isInstance(o) || o instanceof ClassInfo && c.getC().isAssignableFrom(((ClassInfo<?>) o).getC())));

        // DamageCause - ItemType
        Comparators.registerComparator(DamageCause.class, ItemType.class, (dc, t) -> {
            switch (dc) {
                case FIRE:
                    return Relation.get(t.isOfType(Material.FIRE.getId(), (short) -1));
                case LAVA:
                    return Relation.get(t.isOfType(Material.LAVA.getId(), (short) -1) && t.isOfType(Material.STATIONARY_LAVA.getId(), (short) -1));
                case MAGIC:
                    return Relation.get(t.isOfType(Material.POTION.getId(), (short) -1));
                //$CASES-OMITTED$
                default:
                    return Relation.NOT_EQUAL;
            }
        });
        // DamageCause - EntityData
        Comparators.registerComparator(DamageCause.class, EntityData.class, (dc, e) -> {
            switch (dc) {
                case ENTITY_ATTACK:
                    return Relation.get(e.isSupertypeOf(EntityData.fromClass(Entity.class)));
                case PROJECTILE:
                    return Relation.get(e.isSupertypeOf(EntityData.fromClass(Projectile.class)));
                case WITHER:
                    return Relation.get(e.isSupertypeOf(EntityData.fromClass(Wither.class)));
                case FALLING_BLOCK:
                    return Relation.get(e.isSupertypeOf(EntityData.fromClass(FallingBlock.class)));
                //$CASES-OMITTED$
                default:
                    return Relation.NOT_EQUAL;
            }
        });
    }
}
