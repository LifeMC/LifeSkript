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

import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

/**
 * @author Peter Güttinger
 */
public final class ThrownPotionData extends EntityData<ThrownPotion> {
    private static final Adjective m_adjective = new Adjective("entities.thrown potion.adjective");

    static {
        register(ThrownPotionData.class, "thrown potion", ThrownPotion.class, "thrown potion");
    }

    @Nullable
    private ItemType[] types;

    @SuppressWarnings("deprecation")
    @Override
    protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
        if (exprs.length > 0 && exprs[0] != null) {
            //noinspection RedundantCast
            if (Converters.convert((ItemType[]) exprs[0].getAll(), ItemType.class, t -> {
                ItemType r = null;
                for (final ItemData d : t.getTypes()) {
                    if (d.getId() == Material.POTION.getId()) {
                        if (r == null)
                            r = new ItemType(d);
                        else
                            r.add(d);
                    }
                }
                return r;
            }).length == 0) {
                return false; // no error message - other things can be thrown as well
            }
        }
        return false;
    }

    @Override
    protected boolean init(@Nullable final Class<? extends ThrownPotion> c, @Nullable final ThrownPotion e) {
        if (e != null) {
            final ItemStack i = e.getItem();
            if (i == null)
                return false;
            types = new ItemType[]{new ItemType(i)};
        }
        return true;
    }

    @Override
    protected boolean match(final ThrownPotion entity) {
        if (types != null) {
            for (final ItemType t : types) {
                if (t.isOfType(entity.getItem()))
                    return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void set(final ThrownPotion entity) {
        if (types != null) {
            final ItemType t = CollectionUtils.getRandom(types);
            assert t != null;
            entity.setItem(t.getRandom());
        }
    }

    @Override
    public Class<? extends ThrownPotion> getType() {
        return ThrownPotion.class;
    }

    @Override
    public EntityData<ThrownPotion> getSuperType() {
        return new ThrownPotionData();
    }

    @Override
    public boolean isSupertypeOf(final EntityData<?> e) {
        if (!(e instanceof ThrownPotionData))
            return false;
        final ThrownPotionData d = (ThrownPotionData) e;
        if (types != null) {
            return d.types != null && ItemType.isSubset(types, d.types);
        }
        return true;
    }

    @Override
    public String toString(final int flags) {
        final ItemType[] types = this.types;
        if (types == null)
            return super.toString(flags);
        return Noun.getArticleWithSpace(types[0].getTypes().get(0).getGender(), flags) +
                m_adjective.toString(types[0].getTypes().get(0).getGender(), flags) +
                ' ' +
                Classes.toString(types, flags & Language.NO_ARTICLE_MASK, false);
    }

    //		return ItemType.serialize(types);
    @Override
    @Deprecated
    protected boolean deserialize(final String s) {
        if (s.isEmpty())
            return true;
        types = ItemType.deserialize(s);
        return types != null;
    }

    @Override
    protected boolean equals_i(final EntityData<?> obj) {
        if (!(obj instanceof ThrownPotionData))
            return false;
        return Arrays.equals(types, ((ThrownPotionData) obj).types);
    }

    @Override
    protected int hashCode_i() {
        return Arrays.hashCode(types);
    }

}
