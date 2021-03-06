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

package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Comparator.Relation;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.function.ExprFunctionCall;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.Checker;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Contains")
@Description("Checks whatever an inventory contains the given item, a text contains another piece of text, or a list of objects (e.g. a {list variable::*}) contains another object.")
@Examples({"block contains 20 cobblestone", "player has 4 flint and 2 iron ingots"})
@Since("1.0")
public final class CondContains extends Condition {
    static {
        Skript.registerCondition(CondContains.class, CondContains::new, "%inventories% ha(s|ve) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
                "%strings/inventories/objects% contain[s] %strings/itemtypes/objects%",
                "%inventories% (do[es](n't| not)| not) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
                "%strings/inventories/objects% (do[es](n't| not)| not) contain[s] %strings/itemtypes/objects%");
    }

    @SuppressWarnings("null")
    private Expression<?> containers;
    @SuppressWarnings("null")
    private Expression<?> items;

    @SuppressWarnings({"unchecked", "null", "unused"})
    @Override
    public final boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        containers = exprs[0].getConvertedExpression(Object.class);
        if (containers == null)
            return false;
        if (!(containers instanceof Variable) && !String.class.isAssignableFrom(containers.getReturnType()) && !Inventory.class.isAssignableFrom(containers.getReturnType()) && containers.getReturnType() != Object.class) {
            final ParseLogHandler h = SkriptLogger.startParseLogHandler();
            try {
                Expression<?> c = containers.getConvertedExpression(String.class);
                if (c == null)
                    c = containers.getConvertedExpression(Inventory.class);
                if (c == null) {
                    h.printError();
                    return false;
                }
                containers = c;
                h.printLog();
            } finally {
                h.stop();
            }
        }
        items = exprs[1].getConvertedExpression(Object.class);
        if (items == null)
            return false;
        setNegated(matchedPattern >= 2);
        return true;
    }

    @SuppressWarnings("null")
    @Override
    public final boolean check(final Event e) {
        final boolean caseSensitive = SkriptConfig.caseSensitive.value();

        // Special case for list variables and functions that return them
        if ((containers instanceof Variable || containers instanceof ExprFunctionCall)
                && !containers.isSingle()) {
            for (final Object value : containers.getAll(e)) {
                for (final Object searched : items.getAll(e)) {
                    if (Relation.EQUAL.is(Comparators.compare(searched, value))) {
                        return !isNegated();
                    }
                }
            }
            return isNegated();
        }

        return containers.check(e,
                (Checker<Object>) container -> {
                    if (container instanceof Inventory || container instanceof InventoryHolder) {
                        final Inventory inventory = container instanceof InventoryHolder ? ((InventoryHolder) container).getInventory() : (Inventory) container;
                        return items.check(e, (Checker<Object>) type -> {
                            if (type instanceof ItemType)
                                return ((ItemType) type).isContainedIn(inventory);
                            if (type instanceof ItemStack)
                                return inventory.contains((ItemStack) type);
                            return false;
                        });
                    }

                    if (container instanceof String) {
                        final String s = (String) container;
                        return items.check(e,
                                (Checker<Object>) type -> {
                                    if (type instanceof Variable) {
                                        @SuppressWarnings("unchecked") final String toFind = ((Variable<String>) type).getSingle(e);
                                        if (toFind != null)
                                            return StringUtils.contains(s, toFind, caseSensitive);
                                    }
                                    return type instanceof String && StringUtils.contains(s, (String) type, caseSensitive);
                                });
                    }

                    // OK, so we have a variable...
                    final Object val = container instanceof Expression
                            ? ((Expression<?>) container).getSingle(e) : container;

                    final Inventory inventory = Converters.convert(val, Inventory.class);
                    if (inventory != null) {
                        return items.check(e, (Checker<Object>) type -> {
                            if (type instanceof ItemType)
                                return ((ItemType) type).isContainedIn(inventory);
                            if (type instanceof ItemStack)
                                return inventory.contains((ItemStack) type);
                            return false;
                        });
                    }

                    final String s = Converters.convert(val, String.class);
                    if (s != null) {
                        return items.check(e,
                                (Checker<Object>) type -> {
                                    if (type instanceof Variable) {
                                        @SuppressWarnings("unchecked") final String toFind = ((Variable<String>) type).getSingle(e);
                                        if (toFind != null)
                                            return StringUtils.contains(s, toFind, caseSensitive);
                                    }
                                    return type instanceof String && StringUtils.contains(s, (String) type, caseSensitive);
                                });
                    }

                    assert false : "container: " + (container != null ? container.getClass().getCanonicalName() : "null") + ", containers: [r = " + containers.getReturnType().getCanonicalName() + ", c = " + containers.getClass().getCanonicalName() + ']';
                    return false;
                }, isNegated());
    }

    @Override
    public final String toString(@Nullable final Event e, final boolean debug) {
        return containers.toString(e, debug) + (isNegated() ? " doesn't contain " : " contains ") + items.toString(e, debug);
    }

}
