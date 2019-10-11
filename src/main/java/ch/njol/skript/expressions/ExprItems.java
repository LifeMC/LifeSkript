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
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EmptyArrays;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.ArrayIterator;
import ch.njol.util.coll.iterator.CheckedIterator;
import ch.njol.util.coll.iterator.IteratorIterable;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Güttinger
 */
@Name("Items")
@Description("Items or blocks of a specific type, useful for looping.")
@Examples({"loop items of type ore and log:", "	block contains loop-item", "	message \"Theres at least one %loop-item% in this block\"", "drop all blocks at the player # drops one of every block at the player"})
@Since("")
public final class ExprItems extends SimpleExpression<ItemStack> {

    static {
        Skript.registerExpression(ExprItems.class, ItemStack.class, ExpressionType.COMBINED, "[(all|every)] item(s|[ ]types)", "items of type[s] %itemtypes%", "[(all|every)] block(s|[ ]types)", "blocks of type[s] %itemtypes%");
    }

    @Nullable
    private Expression<ItemType> types;
    private boolean blocks;
    @Nullable
    private ItemStack[] buffer;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
        if (vars.length > 0)
            types = (Expression<ItemType>) vars[0];
        blocks = matchedPattern >= 2;
        if (types instanceof Literal) {
            for (final ItemType t : ((Literal<ItemType>) types).getAll())
                t.setAll(true);
        }
        return true;
    }

    @SuppressWarnings("null")
    @Override
    protected ItemStack[] get(final Event e) {
        if (buffer != null)
            return buffer;
        final ArrayList<ItemStack> r = new ArrayList<>();
        for (final ItemStack is : new IteratorIterable<>(iterator(e)))
            r.add(is);
        if (types instanceof Literal)
            return buffer = r.toArray(EmptyArrays.EMPTY_ITEMSTACK_ARRAY);
        return r.toArray(EmptyArrays.EMPTY_ITEMSTACK_ARRAY);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nullable
    public Iterator<ItemStack> iterator(final Event e) {
        final Iterator<ItemStack> iter;
        if (types == null) {
            iter = new ItemStackIterator();
        } else {
            @SuppressWarnings("null") final Iterator<ItemType> it = new ArrayIterator<>(types.getArray(e));
            if (!it.hasNext())
                return null;
            iter = new TypedItemStackIterator(it);
        }

        if (!blocks)
            return iter;

        return new CheckedIterator<>(iter, is -> is != null && is.getTypeId() <= Skript.MAXBLOCKID);
    }

    @Override
    public Class<ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public String toString(@Nullable final Event e, final boolean debug) {
        final Expression<ItemType> types = this.types;
        return (blocks ? "blocks" : "items") + (types != null ? " of type" + (types.isSingle() ? "" : "s") + ' ' + types.toString(e, debug) : "");
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public boolean isLoopOf(final String s) {
        return (blocks ? "block" : "item").equalsIgnoreCase(s);
    }

    private static final class ItemStackIterator implements Iterator<ItemStack> {

        private final Iterator<Material> iterator = new ArrayIterator<>(Material.values());

        ItemStackIterator() {
            /* implicit super call */
        }

        @Override
        public final boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public final ItemStack next() {
            return new ItemStack(iterator.next());
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException(); //FIXME make sure this not causes issues
        }

    }

    private static final class TypedItemStackIterator implements Iterator<ItemStack> {

        private final Iterator<ItemType> it;

        @SuppressWarnings("null")
        Iterator<ItemStack> current;

        TypedItemStackIterator(final Iterator<ItemType> it) {
            this.it = it;
            current = it.next().getAll().iterator();
        }

        @SuppressWarnings("null")
        @Override
        public final boolean hasNext() {
            while (!current.hasNext() && it.hasNext()) {
                current = it.next().getAll().iterator();
            }
            return current.hasNext();
        }

        @SuppressWarnings("null")
        @Override
        public final ItemStack next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return current.next();
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException(); //FIXME make sure this not causes issues
        }

    }
}
