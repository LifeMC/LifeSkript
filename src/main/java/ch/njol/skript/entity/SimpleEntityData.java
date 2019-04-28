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

package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.yggdrasil.Fields;
import org.bukkit.entity.*;
import org.eclipse.jdt.annotation.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Güttinger
 */
public final class SimpleEntityData extends EntityData<Entity> {

    private static final List<SimpleEntityDataInfo> types = new ArrayList<>();

    static {
        types.add(new SimpleEntityDataInfo("arrow", Arrow.class));
        types.add(new SimpleEntityDataInfo("boat", Boat.class));
        types.add(new SimpleEntityDataInfo("blaze", Blaze.class));
        types.add(new SimpleEntityDataInfo("chicken", Chicken.class));
        types.add(new SimpleEntityDataInfo("mooshroom", MushroomCow.class));
        types.add(new SimpleEntityDataInfo("cow", Cow.class));
        types.add(new SimpleEntityDataInfo("cave spider", CaveSpider.class));
        types.add(new SimpleEntityDataInfo("egg", Egg.class));
        types.add(new SimpleEntityDataInfo("ender crystal", EnderCrystal.class));
        types.add(new SimpleEntityDataInfo("ender dragon", EnderDragon.class));
        types.add(new SimpleEntityDataInfo("ender pearl", EnderPearl.class));
        types.add(new SimpleEntityDataInfo("small fireball", SmallFireball.class));
        types.add(new SimpleEntityDataInfo("fireball", Fireball.class));
        types.add(new SimpleEntityDataInfo("ghast", Ghast.class));
        types.add(new SimpleEntityDataInfo("giant", Giant.class));
        types.add(new SimpleEntityDataInfo("iron golem", IronGolem.class));
        types.add(new SimpleEntityDataInfo("magma cube", MagmaCube.class));
        types.add(new SimpleEntityDataInfo("slime", Slime.class));
        types.add(new SimpleEntityDataInfo("painting", Painting.class));
        types.add(new SimpleEntityDataInfo("zombie pigman", PigZombie.class));
        types.add(new SimpleEntityDataInfo("silverfish", Silverfish.class));
        types.add(new SimpleEntityDataInfo("snowball", Snowball.class));
        types.add(new SimpleEntityDataInfo("snow golem", Snowman.class));
        types.add(new SimpleEntityDataInfo("spider", Spider.class));
        types.add(new SimpleEntityDataInfo("squid", Squid.class));
        types.add(new SimpleEntityDataInfo("bottle of enchanting", ThrownExpBottle.class));
        types.add(new SimpleEntityDataInfo("tnt", TNTPrimed.class));
        types.add(new SimpleEntityDataInfo("zombie", Zombie.class));

        if (Skript.classExists("org.bukkit.entity.ItemFrame")) {
            types.add(new SimpleEntityDataInfo("item frame", ItemFrame.class));
            types.add(new SimpleEntityDataInfo("bat", Bat.class));
            types.add(new SimpleEntityDataInfo("witch", Witch.class));
            types.add(new SimpleEntityDataInfo("wither", Wither.class));
            types.add(new SimpleEntityDataInfo("wither skull", WitherSkull.class));
        }
        if (Skript.classExists("org.bukkit.entity.Firework"))
            types.add(new SimpleEntityDataInfo("firework", Firework.class));
        if (Skript.classExists("org.bukkit.entity.Endermite")) {
            types.add(new SimpleEntityDataInfo("endermite", Endermite.class));
        }
        if (Skript.classExists("org.bukkit.entity.ArmorStand")) {
            types.add(new SimpleEntityDataInfo("armor stand", ArmorStand.class));
        }
        // TODO !Update with every version [entities]

        // supertypes
        types.add(new SimpleEntityDataInfo("human", HumanEntity.class, true));
        types.add(new SimpleEntityDataInfo("monster", Monster.class, true));
        types.add(new SimpleEntityDataInfo("creature", Creature.class, true));
        types.add(new SimpleEntityDataInfo("projectile", Projectile.class, true));
        types.add(new SimpleEntityDataInfo("living entity", LivingEntity.class, true));
        types.add(new SimpleEntityDataInfo("entity", Entity.class, true));

        types.add(new SimpleEntityDataInfo("any fireball", Fireball.class, true));
    }

    static {
        final String[] codeNames = new String[types.size()];
        int i = 0;
        for (final SimpleEntityDataInfo info : types) {
            codeNames[i++] = info.codeName;
        }
        EntityData.register(SimpleEntityData.class, "simple", Entity.class, 0, codeNames);
    }

    @SuppressWarnings("hiding")
    private transient SimpleEntityDataInfo info;

    public SimpleEntityData() {
        this(Entity.class);
    }

    private SimpleEntityData(final SimpleEntityDataInfo info) {
        assert info != null;
        this.info = info;
        matchedPattern = types.indexOf(info);
    }

    public SimpleEntityData(final Class<? extends Entity> c) {
        assert c != null && c.isInterface() : c;
        int i = 0;
        for (final SimpleEntityDataInfo info : types) {
            if (info.c.isAssignableFrom(c)) {
                this.info = info;
                matchedPattern = i;
                return;
            }
            i++;
        }
        throw new IllegalStateException();
    }

    public SimpleEntityData(final Entity e) {
        int i = 0;
        for (final SimpleEntityDataInfo info : types) {
            if (info.c.isInstance(e)) {
                this.info = info;
                matchedPattern = i;
                return;
            }
            i++;
        }
        throw new IllegalStateException();
    }

    @SuppressWarnings("null")
    @Override
    protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
        info = types.get(matchedPattern);
        assert info != null : matchedPattern;
        return true;
    }

    @Override
    protected boolean init(final @Nullable Class<? extends Entity> c, final @Nullable Entity e) {
        assert false;
        return false;
    }

    @Override
    public void set(final Entity entity) {
    }

    @Override
    public boolean match(final Entity e) {
        if (info.isSupertype)
            return info.c.isInstance(e);
        for (final SimpleEntityDataInfo info : types) {
            if (info.c.isInstance(e))
                return this.info.c == info.c;
        }
        assert false;
        return false;
    }

    @Override
    public Class<? extends Entity> getType() {
        return info.c;
    }

    @Override
    protected int hashCode_i() {
        return info.hashCode();
    }

    @Override
    protected boolean equals_i(final EntityData<?> obj) {
        if (!(obj instanceof SimpleEntityData))
            return false;
        final SimpleEntityData other = (SimpleEntityData) obj;
        return info.equals(other.info);
    }

    @Override
    public Fields serialize() throws NotSerializableException {
        final Fields f = super.serialize();
        f.putObject("info.codeName", info.codeName);
        return f;
    }

    @Override
    public void deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
        final String codeName = fields.getAndRemoveObject("info.codeName", String.class);
        for (final SimpleEntityDataInfo i : types) {
            if (i.codeName.equals(codeName)) {
                info = i;
                super.deserialize(fields);
                return;
            }
        }
        throw new StreamCorruptedException("Invalid SimpleEntityDataInfo code name " + codeName);
    }

    //		return info.c.getName();
    @Override
    @Deprecated
    protected boolean deserialize(final String s) {
        try {
            final Class<?> c = Class.forName(s);
            for (final SimpleEntityDataInfo i : types) {
                if (i.c == c) {
                    info = i;
                    return true;
                }
            }
            return false;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean isSupertypeOf(final EntityData<?> e) {
        return info.c == e.getType() || info.isSupertype && info.c.isAssignableFrom(e.getType());
    }

    @Override
    public EntityData<Entity> getSuperType() {
        return new SimpleEntityData(info);
    }

    public static final class SimpleEntityDataInfo {
        final String codeName;
        final Class<? extends Entity> c;
        final boolean isSupertype;

        SimpleEntityDataInfo(final String codeName, final Class<? extends Entity> c) {
            this(codeName, c, false);
        }

        SimpleEntityDataInfo(final String codeName, final Class<? extends Entity> c, final boolean isSupertype) {
            this.codeName = codeName;
            this.c = c;
            this.isSupertype = isSupertype;
        }

        @Override
        public int hashCode() {
            return c.hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof SimpleEntityDataInfo))
                return false;
            final SimpleEntityDataInfo other = (SimpleEntityDataInfo) obj;
            if (c != other.c)
                return false;
            assert codeName.equals(other.codeName);
            assert isSupertype == other.isSupertype;
            return true;
        }
    }

}
