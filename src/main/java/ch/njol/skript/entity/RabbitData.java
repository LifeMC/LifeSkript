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

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.entity.Rabbit;

public final class RabbitData extends EntityData<Rabbit> {

    static {
        if (Skript.classExists("org.bukkit.entity.Rabbit")) {
            EntityData.register(RabbitData.class, "rabbit", Rabbit.class, 0, "rabbit", "black rabbit", "black and white rabbit", "brown rabbit", "gold rabbit", "salt and pepper rabbit", "killer rabbit", "white rabbit");
        }
    }

    private int type;

    private static final Rabbit.Type typeFromInt(final int i) {
        switch (i) {
            case 1:
                return Rabbit.Type.BLACK;
            case 2:
                return Rabbit.Type.BLACK_AND_WHITE;
            case 3:
                return Rabbit.Type.BROWN;
            case 4:
                return Rabbit.Type.GOLD;
            case 5:
                return Rabbit.Type.SALT_AND_PEPPER;
            case 6:
                return Rabbit.Type.THE_KILLER_BUNNY;
            case 7:
                return Rabbit.Type.WHITE;
            default:
                break;
        }
        return Rabbit.Type.BLACK;
    }

    private static final int intFromType(final Rabbit.Type type) {
        switch (type) {
            case BLACK:
                return 1;
            case BLACK_AND_WHITE:
                return 2;
            case BROWN:
                return 3;
            case GOLD:
                return 4;
            case SALT_AND_PEPPER:
                return 5;
            case THE_KILLER_BUNNY:
                return 6;
            case WHITE:
                return 7;
            default:
                return 0;
        }
    }

    @Override
    protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
        type = matchedPattern;
        return true;
    }

    @SuppressWarnings("null")
    @Override
    protected boolean init(final Class<? extends Rabbit> c, final Rabbit rabbit) {
        type = rabbit == null ? 0 : intFromType(rabbit.getRabbitType());
        return true;
    }

    @Override
    public void set(final Rabbit entity) {
        if (type != 0)
            entity.setRabbitType(typeFromInt(type));
    }

    @SuppressWarnings("null")
    @Override
    protected boolean match(final Rabbit entity) {
        return type == 0 || intFromType(entity.getRabbitType()) == type;
    }

    @Override
    public Class<? extends Rabbit> getType() {
        return Rabbit.class;
    }

    @Override
    public EntityData<Rabbit> getSuperType() {
        return new RabbitData();
    }

    @Override
    protected int hashCode_i() {
        return type;
    }

    @Override
    protected boolean equals_i(final EntityData<?> obj) {
        if (!(obj instanceof RabbitData))
            return false;
        final RabbitData other = (RabbitData) obj;
        return type == other.type;
    }

    @Override
    public boolean isSupertypeOf(final EntityData<?> e) {
        return e instanceof RabbitData && (type == 0 || ((RabbitData) e).type == type);
    }

}
