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

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.LogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.EmptyArrays;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.NonNullIterator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Peter Güttinger
 */
@Name("Entities")
@Description("all entities in all world, in a specific world or in a radius around a certain location, e.g. 'all players', 'all creepers in the player's world', or 'players in radius 100 of the player'.")
@Examples({"kill all creepers in the player's world", "send \"Psst!\" to all players witin 100 meters of the player", "give a diamond to all ops", "heal all tamed wolves in radius 2000 around {town center}"})
@Since("1.2.1")
public final class ExprEntities extends SimpleExpression<Entity> {
    // World#getNearbyEntities only available on 1.8 and above.
    public static final boolean getNearbyEntities = Skript.methodExists(World.class, "getNearbyEntities", Location.class, double.class, double.class, double.class);
    // We don't want to try the World#getNearbyEntities method everytime in case of a fail.
    private static boolean hardFail;

    static {
        Skript.registerExpression(ExprEntities.class, Entity.class, ExpressionType.PATTERN_MATCHES_EVERYTHING, "[all] %*entitydatas% [(in|of) [world[s]] %-worlds%]", "[all] entities of type[s] %entitydatas% [(in|of) [world[s]] %-worlds%]", "[all] %*entitydatas% (within|[with]in radius) %number% [(block[s]|met(er|re)[s])] (of|around) %location%", "[all] entities of type[s] %entitydatas% in radius %number% (of|around) %location%");
    }

    @SuppressWarnings("null")
    private Expression<? extends EntityData<?>> types;
    @Nullable
    private Expression<World> worlds;
    private Class<? extends Entity> returnType = Entity.class;
    @Nullable
    private Expression<Number> radius;
    @Nullable
    private Expression<Location> center;
    @Nullable
    private Expression<? extends Entity> centerEntity;
    private int matchedPattern;

    /**
     * A safe way for getting nearby entities by a location and a x, y, z value.
     * This the version-safe mirror of the original method, {@link World#getNearbyEntities(Location, double, double, double)}.
     *
     * @param l The location.
     * @param x The x value.
     * @param y The y value.
     * @param z The z value.
     * @return The collection of entities nearby the given arguments.
     */
    @Nullable
    public static final Collection<Entity> getNearbyEntities(final Location l, final double x, final double y, final double z) {
        if (getNearbyEntities) {
            return l.getWorld().getNearbyEntities(l, x, y, z);
        }
        // Don't try it, known to be not exist
        if (hardFail) {

            // Return empty collection. The warning should be already printed in first hard fail.
            return Collections.emptyList();
        }
        try {

            // Try it
            final Collection<Entity> col = l.getWorld().getNearbyEntities(l, x, y, z);

            // Success
            hardFail = false;
            return col;

        } catch (final NoSuchMethodError e) { // Method not exists

            if (!hardFail) { // Give the warning only in first use
                Skript.warning("This server version not supports getNearbyEntities method. This method is only available on minecraft 1.8 and above. The LifeSpigot adds this method to lower versions. Look it LifeSpigot if you want to fix this issue, or just don't use the entities expression.");
            }

            // Return empty collection (list) in case of a hard fail.
            hardFail = true;
            return Collections.emptyList();
        }
    }

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        this.matchedPattern = matchedPattern;
        types = (Expression<? extends EntityData<?>>) exprs[0];
        if (matchedPattern % 2 == 0) {
            for (final EntityData<?> d : ((Literal<EntityData<?>>) types).getAll()) {
                if (d.isPlural().isFalse() || d.isPlural().isUnknown() && !StringUtils.startsWithIgnoreCase(parseResult.expr, "all"))
                    return false;
            }
        }
        if (matchedPattern < 2) {
            worlds = (Expression<World>) exprs[exprs.length - 1];
        } else {
            radius = (Expression<Number>) exprs[exprs.length - 2];
            center = (Expression<Location>) exprs[exprs.length - 1];
            final BlockingLogHandler log = SkriptLogger.startLogHandler(new BlockingLogHandler());
            try {
                if (center.getSource().getReturnType() != Location.class) // Ensure that no location -> entity...
                    centerEntity = center.getSource().getConvertedExpression(Entity.class); // ... when no entity exists
            } finally {
                log.stop();
            }
        }
        if (types instanceof Literal && ((Literal<EntityData<?>>) types).getAll().length == 1) {
            returnType = ((Literal<EntityData<?>>) types).getSingle().getType();
        }
        return true;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return returnType;
    }

    @Override
    protected Entity[] get(final Event e) {
        if (matchedPattern >= 2) {
            final Iterator<? extends Entity> iter = iterator(e);
            if (iter == null || !iter.hasNext())
                return EmptyArrays.EMPTY_ENTITY_ARRAY;
            final List<Entity> l = new ArrayList<>();
            while (iter.hasNext())
                l.add(iter.next());
            return l.toArray((Entity[]) Array.newInstance(returnType, l.size()));
        }
        return EntityData.getAll(types.getAll(e), returnType, worlds != null ? worlds.getArray(e) : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isLoopOf(final String s) {
        if (!(types instanceof Literal<?>))
            return false;
        final LogHandler h = SkriptLogger.startLogHandler(new BlockingLogHandler());
        try {
            final EntityData<?> d = EntityData.parseWithoutIndefiniteArticle(s);
            if (d != null) {
                for (final EntityData<?> t : ((Literal<EntityData<?>>) types).getAll()) {
                    assert t != null;
                    if (!d.isSupertypeOf(t))
                        return false;
                }
                return true;
            }
        } finally {
            h.stop();
        }
        return false;
    }

    @Override
    @Nullable
    @SuppressWarnings("null")
    public Iterator<? extends Entity> iterator(final Event e) {
        if (matchedPattern >= 2) {
            final Location l;
            if (centerEntity != null) {
                final Entity en = centerEntity.getSingle(e);
                if (en == null)
                    return null;
                l = en.getLocation();
            } else {
                assert center != null;
                l = center.getSingle(e);
                if (l == null)
                    return null;
            }
            assert radius != null;
            final Number n = radius.getSingle(e);
            if (n == null)
                return null;
            final double d = n.doubleValue();
            final Collection<Entity> es = getNearbyEntities(l, d, d, d);
            final double radiusSquared = d * d * Skript.EPSILON_MULT;
            final EntityData<?>[] ts = types.getAll(e);
            return new CheckedIterator<>(es.iterator(), e1 -> {
                if (e1 == null || e1.getLocation().distanceSquared(l) > radiusSquared)
                    return false;
                for (final EntityData<?> t : ts) {
                    if (t.isInstance(e1))
                        return true;
                }
                return false;
            });
        }
        if (worlds == null && returnType == Player.class)
            return super.iterator(e);
        return new EntityIterator(returnType, worlds, e, types);
    }

    @SuppressWarnings("null")
    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        return "all entities of types " + types.toString(e, debug) + (worlds != null ? " in " + worlds.toString(e, debug) : radius != null && center != null ? " in radius " + radius.toString(e, debug) + " around " + center.toString(e, debug) : "");
    }

    private static final class EntityIterator extends NonNullIterator<Entity> {
        private final World[] ws;

        private final EntityData<?>[] ts;
        private final Class<? extends Entity> returnType;
        private int w = -1;
        @Nullable
        private Iterator<? extends Entity> curIter;

        EntityIterator(final Class<? extends Entity> returnType,
                       @Nullable final Expression<World> worlds,
                       final Event e,
                       final Expression<? extends EntityData<?>> types) {
            this.returnType = returnType;
            ws = worlds == null ? Bukkit.getWorlds().toArray(EmptyArrays.EMPTY_WORLD_ARRAY) : worlds.getArray(e);
            ts = types.getAll(e);
        }

        @Override
        @Nullable
        @SuppressWarnings("null")
        protected final Entity getNext() {
            while (Skript.isSkriptRunning()) {
                while (curIter == null || !curIter.hasNext()) {
                    w++;
                    if (w == ws.length)
                        return null;
                    curIter = ws[w].getEntitiesByClass(returnType).iterator();
                }
                while (curIter.hasNext()) {
                    final Entity current = curIter.next();
                    for (final EntityData<?> t : ts) {
                        if (t.isInstance(current))
                            return current;
                    }
                }
            }
            return null;
        }
    }

}
