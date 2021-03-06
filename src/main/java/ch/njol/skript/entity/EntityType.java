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

package ch.njol.skript.entity;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.bukkit.entity.Entity;
import org.eclipse.jdt.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public final class EntityType implements Cloneable, YggdrasilSerializable {

    private static final Pattern ENTITY_TYPE_PATTERN_ONE = Pattern.compile("\\d+ .+");
    private static final Matcher ENTITY_TYPE_PATTERN_ONE_MATCHER = ENTITY_TYPE_PATTERN_ONE.matcher("");

    private static final Pattern ENTITY_TYPE_PATTERN_TWO = Pattern.compile("(?i)an? .+");
    private static final Matcher ENTITY_TYPE_PATTERN_TWO_MATCHER = ENTITY_TYPE_PATTERN_TWO.matcher("");

    static {
        Classes.registerClass(new ClassInfo<>(EntityType.class, "entitytype").name("Entity Type with Amount").description("An <a href='#entitydata'>entity type</a> with an amount, e.g. '2 zombies'. I might remove this type in the future and make a more general 'type' type, i.e. a type that has a number and a type.").usage("&lt;<a href='#number'>number</a>&gt; &lt;entity type&gt;").examples("spawn 5 creepers behind the player").since("1.3").defaultExpression(new SimpleLiteral<>(new EntityType(Entity.class, 1), true)).parser(new Parser<EntityType>() {
            @Override
            @Nullable
            public EntityType parse(final String s, final ParseContext context) {
                return EntityType.parse(s);
            }

            @Override
            public String toString(final EntityType t, final int flags) {
                return t.toString(flags);
            }

            @Override
            public String toVariableNameString(final EntityType t) {
                return "entitytype:" + t;
            }

            @Override
            public String getVariableNamePattern() {
                return "entitytype:.+";
            }
        }).serializer(new YggdrasilSerializer<EntityType>() {
            //						return t.amount + "*" + EntityData.serializer.serialize(t.data);
            @Override
            @Deprecated
            @Nullable
            public EntityType deserialize(final String s) {
                final String[] split = s.split("\\*", 2);
                if (split.length != 2)
                    return null;
                @SuppressWarnings("null") final EntityData<?> d = EntityData.serializer.deserialize(split[1]);
                if (d == null)
                    return null;
                final String amount = split[0];
                if (!SkriptParser.isInteger(amount))
                    return null;
                return new EntityType(d, Integer.parseInt(amount));
            }

        }));
    }

    public final EntityData<?> data;
    public int amount = -1;

    /**
     * Only used for deserialisation
     */
    @SuppressWarnings({"unused", "null"})
    private EntityType() {
        data = null;
    }

    public EntityType(final EntityData<?> data, final int amount) {
        assert data != null;
        this.data = data;
        this.amount = amount;
    }

    public EntityType(final Class<? extends Entity> c, final int amount) {
        assert c != null;
        data = EntityData.fromClass(c);
        this.amount = amount;
    }

    public EntityType(final Entity e) {
        data = EntityData.fromEntity(e);
    }

    public EntityType(final EntityType other) {
        amount = other.amount;
        data = other.data;
    }

    @SuppressWarnings("null")
    @Nullable
    static final EntityType parse(String s) {
        assert s != null && !s.isEmpty();
        s = s.trim();

        int amount = -1;
        if (ENTITY_TYPE_PATTERN_ONE_MATCHER.reset(s).matches()) {
            amount = Utils.parseInt(s.split(" ", 2)[0]);
            s = s.split(" ", 2)[1];
        } else if (ENTITY_TYPE_PATTERN_TWO_MATCHER.reset(s).matches()) {
            s = s.split(" ", 2)[1];
        }
//		final Pair<String, Boolean> p = Utils.getPlural(s, amount != 1 && amount != -1);
//		s = p.first;
        final EntityData<?> data = EntityData.parseWithoutIndefiniteArticle(s);
        if (data == null)
            return null;
        return new EntityType(data, amount);
    }

    public boolean isInstance(final Entity entity) {
        return data.isInstance(entity);
    }

    @Override
    public String toString() {
        return getAmount() == 1 ? data.toString(0) : amount + " " + data.toString(Language.F_PLURAL);
    }

    public String toString(final int flags) {
        return getAmount() == 1 ? data.toString(flags) : amount + " " + data.toString(flags | Language.F_PLURAL);
    }

    public int getAmount() {
        return amount == -1 ? 1 : amount;
    }

    public boolean sameType(final EntityType other) {
        return data.equals(other.data);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public EntityType clone() {
        return new EntityType(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + amount;
        result = prime * result + data.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof EntityType))
            return false;
        final EntityType other = (EntityType) obj;
        if (amount != other.amount)
            return false;
        return data.equals(other.data);
    }

}
