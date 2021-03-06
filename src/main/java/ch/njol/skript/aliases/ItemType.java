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

package ch.njol.skript.aliases;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Unit;
import ch.njol.skript.localization.*;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.Container;
import ch.njol.skript.util.Container.ContainerType;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.iterator.EmptyIterable;
import ch.njol.util.coll.iterator.SingleItemIterable;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.jdt.annotation.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ContainerType(ItemStack.class)
@SuppressWarnings("deprecation")
public final class ItemType implements Unit, Iterable<ItemData>, Container<ItemStack>, YggdrasilExtendedSerializable {

    // 1.4.5
    public static final boolean itemMetaSupported = Skript.classExists("org.bukkit.inventory.meta.ItemMeta");
    private static final Message m_named = new Message("aliases.named");
    private static final Random random = new Random();
    @SuppressWarnings("RegExpEmptyAlternationBranch")
    private static final Matcher ITEMTYPE_SEPARATOR = Pattern.compile("||", Pattern.LITERAL).matcher("");
    private static final Pattern ITEMTYPE_SPLITTER = Pattern.compile("(?!<\\|)\\|(?!\\|)");
    /**
     * Note to self: use {@link #add_(ItemData)} to add item datas, don't add them directly to this list.
     */
    final ArrayList<ItemData> types = new ArrayList<>();
    // TODO empty == unenchanted, add expression "unenchanted <item>"
    @Nullable
    transient Map<Enchantment, Integer> enchantments;
    /**
     * Guaranteed to be of type ItemMeta.
     */
    @Nullable
    transient Object meta;
    private boolean all;
    private int amount = -1;
    /**
     * How many different items this item type represents
     */
    private int numItems;
    /**
     * ItemTypes to use instead of this one if adding to an inventory or setting a block.
     */
    @Nullable
    private ItemType item, block;

    public ItemType() {
    }

    public ItemType(final int id) {
        add_(new ItemData(id));
    }

    public ItemType(final int id, final short data) {
        add_(new ItemData(id, data));
    }

    public ItemType(final ItemData d) {
        add_(d.clone());
    }

    public ItemType(final ItemStack i) {
        amount = i.getAmount();
        add_(new ItemData(i));
        if (!i.getEnchantments().isEmpty())
            enchantments = new HashMap<>(i.getEnchantments());
        if (itemMetaSupported) {
            meta = i.getItemMeta();
            unsetItemMetaEnchs((ItemMeta) meta);
        }
    }

    public ItemType(final Block b) {
//		amount = 1;
        add_(new ItemData(b.getTypeId(), b.getData()));
        // TODO metadata - spawners, skulls, etc.
    }

    private ItemType(final ItemType i) {
        all = i.all;
        amount = i.amount;
        numItems = i.numItems;
        final ItemType bl = i.block, it = i.item;
        block = bl == null ? null : bl.clone();
        item = it == null ? null : it.clone();
        final Object m = i.meta;
        meta = m == null ? null : ((ItemMeta) m).clone();
        for (final ItemData d : i) {
            types.add(d.clone());
        }
        if (i.enchantments != null)
            enchantments = new HashMap<>(i.enchantments);
    }

    private static final void unsetItemMetaEnchs(@Nullable final ItemMeta meta) {
        if (meta == null)
            return;
        for (final Enchantment e : meta.getEnchants().keySet())
            meta.removeEnchant(e);
    }

    public static final ItemStack[] getCopiedContents(final Inventory invi) {
        final ItemStack[] buf = invi.getContents();
        for (int i = 0; i < buf.length; i++)
            if (buf[i] != null)
                buf[i] = buf[i].clone();
        return buf;
    }

    private static final boolean addTo(@Nullable final ItemStack item, final ItemStack[] buf) {
        if (item == null || item.getType() == Material.AIR)
            return true;
        int added = 0;
        for (final ItemStack element : buf) {
            if (Utils.itemStacksEqual(item, element)) {
                final int toAdd = Math.min(element.getMaxStackSize() - element.getAmount(), item.getAmount() - added);
                added += toAdd;
                element.setAmount(element.getAmount() + toAdd);
                if (added == item.getAmount())
                    return true;
            }
        }
        for (int i = 0; i < buf.length; i++) {
            if (buf[i] == null) {
                final int toAdd = Math.min(item.getMaxStackSize(), item.getAmount() - added);
                added += toAdd;
                buf[i] = item.clone();
                buf[i].setAmount(toAdd);
                if (added == item.getAmount())
                    return true;
            }
        }
        return false;
    }

    /**
     * Tests whatever a given set of ItemTypes is a subset of another set of ItemTypes.
     * <p>
     * This method works differently that normal set operations, as is e.g. returns true if set == {everything}.
     *
     * @param set
     * @param sub
     * @return Whatever all item types in <tt>sub</tt> have at least one {@link #isSupertypeOf(ItemType) super type} in <tt>set</tt>
     */
    public static final boolean isSubset(final ItemType[] set, final ItemType[] sub) {
        outer:
        for (final ItemType i : sub) {
            assert i != null;
            for (final ItemType t : set) {
                if (t.isSupertypeOf(i))
                    continue outer;
            }
            return false;
        }
        return true;
    }

    /**
     * Loads an array of ItemTypes separated by '|'
     *
     * @deprecated use {@link ItemType#deserialize(Fields)} instead
     */
    @Deprecated
    @Nullable
    public static final ItemType[] deserialize(final String s) {
        if (s.isEmpty())
            return null;
        final String[] split = ITEMTYPE_SPLITTER.split(s);
        final ItemType[] types = new ItemType[split.length];
        for (int i = 0; i < types.length; i++) {
            final ItemType t = (ItemType) Classes.deserialize("itemtype", ITEMTYPE_SEPARATOR.reset(split[i]).replaceAll(Matcher.quoteReplacement("|")));
            if (t == null)
                return null;
            types[i] = t;
        }
        return types;
    }

    public static final String toString(final ItemStack i) {
        return new ItemType(i).toString();
    }

    public static final String toString(final ItemStack i, final int flags) {
        return new ItemType(i).toString(flags);
    }

    /**
     * Removes the item and block aliases from this alias as it now represents a different item.
     */
    public void modified() {
        item = block = null;
    }

    /**
     * @return amount or 1 if amount == -1
     */
    @Override
    public int getAmount() {
        return amount == -1 ? 1 : amount;
    }

    @Override
    public void setAmount(final double amount) {
        setAmount((int) amount);
    }

    public void setAmount(final int amount) {
        this.amount = amount;
        if (item != null)
            item.amount = amount;
        if (block != null)
            block.amount = amount;
    }

    /**
     * Only use this method if you know what you're doing.
     *
     * @return the internal amount, i.e. -1 or the same as {@link #getAmount()}.
     */
    public int getInternalAmount() {
        return amount;
    }

    public boolean isAll() {
        return all;
    }

    @Nullable
    public Object getItemMeta() {
        return meta;
    }

    public void setItemMeta(final Object meta) {
        if (!itemMetaSupported || !(meta instanceof ItemMeta))
            throw new IllegalStateException(String.valueOf(meta));
        unsetItemMetaEnchs((ItemMeta) meta);
        this.meta = meta;
        if (item != null) {
            item = item.clone();
            item.meta = meta;
        }
        if (block != null) {
            block = block.clone();
            block.meta = meta;
        }
    }

    /**
     * @param item
     * @return Whatever the given item has correct enchantments & ItemMeta, but doesn't check its type
     */
    private final boolean hasMeta(@Nullable final ItemStack item) {
        final Map<Enchantment, Integer> enchs = enchantments;
        if (enchs != null) {
            if (item == null)
                return false;
            for (final Entry<Enchantment, Integer> e : enchs.entrySet()) {
                if (e.getValue() == -1 ? item.getEnchantmentLevel(e.getKey()) == 0 : item.getEnchantmentLevel(e.getKey()) != e.getValue())
                    return false;
            }
        }
        final Object meta = this.meta;
        if (meta != null) {
            if (item == null)
                return false;
            final ItemMeta m = item.getItemMeta();
            unsetItemMetaEnchs(m);
            return meta.equals(m);
        }
        return true;
    }

    public boolean isOfType(@Nullable final ItemStack item) {
        if (item == null)
            return isOfType(0, (short) 0);
        if (!hasMeta(item))
            return false;
        return isOfType(item.getTypeId(), item.getDurability());
    }

    public boolean isOfType(@Nullable final Block block) {
        if (enchantments != null)
            return false;
        if (block == null)
            return isOfType(0, (short) 0);
        return isOfType(block.getTypeId(), block.getData());
        // TODO metadata
    }

    public boolean isOfType(final int id, final short data) {
        for (final ItemData type : types) {
            if (type.isOfType(id, data))
                return true;
        }
        return false;
    }

    public boolean isSupertypeOf(final ItemType other) {
//		if (all != other.all)
//			return false;
        if (amount != -1 && other.amount != amount)
            return false;
        final Map<Enchantment, Integer> enchs = enchantments;
        if (enchs != null) {
            final Map<Enchantment, Integer> oenchs = other.enchantments;
            if (oenchs == null)
                return false;
            for (final Entry<Enchantment, Integer> o : oenchs.entrySet()) {
                final Integer t = enchs.get(o.getKey());
                if (!Objects.equals(t, o.getValue()))
                    return false;
            }
        }
        if (meta != null && !meta.equals(other.meta))
            return false;
        outer:
        for (final ItemData o : other.types) {
            assert o != null;
            for (final ItemData t : types) {
                if (t.isSupertypeOf(o))
                    continue outer;
            }
            return false;
        }
        return true;
    }

    public ItemType getItem() {
        final ItemType item = this.item;
        return item == null ? this : item;
    }

    void setItem(@Nullable final ItemType item) {
        if (equals(item)) { // can happen if someone defines a 'x' and 'x item/block' alias that have the same value, e.g. 'dirt' and 'dirt block'
            this.item = null;
        } else {
            if (item != null) {
                if (item.item != null || item.block != null) {
                    assert false : this + "; item=" + item + ", item.item=" + item.item + ", item.block=" + item.block;
                    this.item = null;
                    return;
                }
                item.setAmount(amount);
            }
            this.item = item;
        }
    }

    public ItemType getBlock() {
//		if (block == null) {
//			ItemType i = clone();
//			for (int j = 0; j < i.numTypes(); j++) {
//				final ItemData d = i.getTypes().get(j);
//				if (d.getId() > Skript.MAXBLOCKID) {
//					i.remove(d);
//					j--;
//				}
//			}
//			if (i.getTypes().isEmpty())
//				return this;
//			return block = i;
//		}
        final ItemType block = this.block;
        return block == null ? this : block;
    }

    void setBlock(@Nullable final ItemType block) {
        if (equals(block)) {
            this.block = null;
        } else {
            if (block != null) {
                if (block.item != null || block.block != null) {
                    assert false : this + "; block=" + block + ", block.item=" + block.item + ", block.block=" + block.block;
                    this.block = null;
                    return;
                }
                block.setAmount(amount);
            }
            this.block = block;
        }
    }

    /**
     * @return Whatever this ItemType has at least one ItemData that represents an item
     */
    public boolean hasItem() {
        for (final ItemData d : types) {
            if (d.getId() > Skript.MAXBLOCKID)
                return true;
        }
        return false;
    }

    /**
     * @return Whatever this ItemType has at least one ItemData that represents a block
     */
    public boolean hasBlock() {
        for (final ItemData d : types) {
            if (d.getId() <= Skript.MAXBLOCKID)
                return true;
        }
        return false;
    }

    /**
     * Sets the given block to this ItemType
     *
     * @param block        The block to set
     * @param applyPhysics Whatever to run a physics check just after setting the block
     * @return Whatever the block was successfully set
     */
    public boolean setBlock(final Block block, final boolean applyPhysics) {
        for (final ItemData d : types) {
            if (d.typeid > Skript.MAXBLOCKID)
                continue;
            if (BlockUtils.set(block, d.typeid, (byte) d.dataMin, (byte) d.dataMax, applyPhysics))
                return true;
        }
        return false;
    }

    /**
     * Intersects all ItemDatas with all ItemDatas of the given ItemType, returning an ItemType with at most n*m ItemDatas, where n = #ItemDatas of this ItemType, and m =
     * #ItemDatas of the argument.
     *
     * @param other
     * @return A new item type which is the intersection of the two item types or null if the intersection is empty.
     * @see ItemData#intersection(ItemData)
     */
    @Nullable
    public ItemType intersection(final ItemType other) {
        if (amount != -1 || other.amount != -1 || enchantments != null || other.enchantments != null)
            throw new IllegalStateException("ItemType.intersection(ItemType) must only be used to instersect aliases");
        final ItemType r = new ItemType();
        for (final ItemData d1 : types) {
            for (final ItemData d2 : other.types) {
                assert d2 != null; // strangely d1 doesn't cause a warning...
                r.add_(d1.intersection(d2));
            }
        }
        if (r.types.isEmpty())
            return null;
        return r;
    }

    /**
     * @param type Some ItemData. Only a copy of it will be stored.
     */
    public void add(@Nullable final ItemData type) {
        if (type != null)
            add_(type.clone());
    }

    /**
     * @param type A cloned or newly created ItemData
     */
    private final void add_(@Nullable final ItemData type) {
        if (type != null) {
            types.add(type);
            numItems += type.numItems();
            modified();
        }
    }

    public void addAll(final Iterable<ItemData> types) {
        for (final ItemData type : types) {
            if (type != null) {
                this.types.add(type);
                numItems += type.numItems();
            }
        }
        modified();
    }

    public void remove(final ItemData type) {
        if (types.remove(type)) {
            numItems -= type.numItems();
            modified();
        }
    }

    void remove(final int index) {
        final ItemData type = types.remove(index);
        numItems -= type.numItems();
        modified();
    }

    public void addEnchantment(final Enchantment e, final int level) {
        Map<Enchantment, Integer> enchs = enchantments;
        if (enchs == null)
            enchantments = enchs = new HashMap<>();
        enchs.put(e, level);
    }

    @SuppressWarnings("null")
    public void addEnchantments(final Map<Enchantment, Integer> enchantments) {
        if (this.enchantments == null)
            this.enchantments = new HashMap<>(enchantments);
        else
            this.enchantments.putAll(enchantments);
    }

    public void clearEnchantments() {
        if (enchantments != null)
            enchantments.clear();
    }

    @Nullable
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    @Override
    public Iterator<ItemStack> containerIterator() {
        return new ItemStackIterator(types, meta, enchantments, getAmount());
    }

    /**
     * Gets all ItemStacks this ItemType represents. Only use this if you know what you're doing, as it returns only one element if this is not an 'every' alias.
     *
     * @return An Iterable whose iterator will always return the same item(s)
     */
    public Iterable<ItemStack> getAll() {
        if (!all) {
            final ItemStack i = getRandom();
            if (i == null)
                return EmptyIterable.get();
            return new SingleItemIterable<>(i);
        }
        return this::containerIterator;
    }

    public void setAll(final boolean all) {
        this.all = all;
    }

    @Nullable
    public ItemStack removeAll(@Nullable final ItemStack item) {
        final boolean wasAll = all;
        final int oldAmount = amount;
        all = true;
        amount = -1;
        try {
            return removeFrom(item);
        } finally {
            all = wasAll;
            amount = oldAmount;
        }
    }

    /**
     * Removes this type from the item stack if appropriate
     *
     * @param item
     * @return The passed ItemStack or null if the resulting amount is <= 0
     */
    @Nullable
    public ItemStack removeFrom(@Nullable final ItemStack item) {
        if (item == null)
            return null;
        if (!isOfType(item))
            return item;
        if (all && amount == -1)
            return null;
        final int a = item.getAmount() - getAmount();
        if (a <= 0)
            return null;
        item.setAmount(a);
        return item;
    }

    /**
     * Adds this ItemType to the given item stack
     *
     * @param item
     * @return The passed ItemStack or a new one if the passed is null or air
     */
    @Nullable
    public ItemStack addTo(@Nullable final ItemStack item) {
        if (item == null || item.getTypeId() == 0)
            return getRandom();
        if (isOfType(item))
            item.setAmount(Math.min(item.getAmount() + getAmount(), item.getMaxStackSize()));
        return item;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ItemType clone() {
        return new ItemType(this);
    }

    /**
     * @return One random ItemStack that this ItemType represents. If you have a List or an Inventory, use {@link #addTo(Inventory)} or {@link #addTo(List)} respectively.
     * @see #addTo(Inventory)
     * @see #addTo(ItemStack)
     * @see #addTo(ItemStack[])
     * @see #addTo(List)
     * @see #removeFrom(Inventory)
     * @see #removeFrom(ItemStack)
     * @see #removeFrom(List...)
     */
    @Nullable
    public ItemStack getRandom() {
        if (numItems == 0)
            return null;
        int item = random.nextInt(numItems);
        int i = -1;
        while (item >= 0)
            item -= types.get(++i).numItems();
        final ItemStack is = types.get(i).getRandom();
        is.setAmount(getAmount());
        if (meta != null)
            is.setItemMeta(Bukkit.getItemFactory().asMetaFor((ItemMeta) meta, is.getType()));
        if (enchantments != null)
            is.addUnsafeEnchantments(enchantments);
        return is;
    }

    /**
     * Test whatever this ItemType can be put into the given inventory completely.
     * <p>
     * REMIND If this ItemType represents multiple items with OR, this function will immediately return false.<br/>
     * CondCanHold currently blocks aliases without 'every'/'all' as temporary solution.
     *
     * @param inventory The inventory to check if has space
     * @return Whatever this item type can be added to the given inventory
     */
    public boolean hasSpace(final Inventory inventory) {
        if (!all && (getItem().types.size() != 1 || getItem().types.get(0).hasDataRange() || getItem().types.get(0).typeid == -1)) {
            return false;
        }
        return addTo(getCopiedContents(inventory));
    }

    /**
     * @return List of ItemDatas. The returned list is not modifiable, use {@link #add(ItemData)} and {@link #remove(ItemData)} if you need to change the list, or use the
     * {@link #iterator()}.
     */
    @SuppressWarnings("null")
    public List<ItemData> getTypes() {
        return Collections.unmodifiableList(types);
    }

    /**
     * Gets the type of this {@link ItemType}
     * or {@code null} if not found.
     *
     * @return The type of this {@link ItemType}
     * or {@code null} if not found.
     */
    @Nullable
    public Material getMaterial() {
        final ItemData data = types.get(0);
        if (data == null)
            throw new IllegalStateException("material not found");
        return data.getType();
    }

    /**
     * Gets the type of this {@link ItemType}
     * or {@link Material#AIR} if not found.
     *
     * @return The type of this {@link ItemType}
     * or {@link Material#AIR} if not found.
     */
    public Material materialOrAir() {
        final Material material = getMaterial();
        return material != null ? material : Material.AIR;
    }

    public int numTypes() {
        return types.size();
    }

    /**
     * @return How many different items this item type represents
     */
    public int numItems() {
        return numItems;
    }

    @Override
    public Iterator<ItemData> iterator() {
        return new ItemDataIterator(this, types);
    }

    public boolean isContainedIn(final Inventory invi) {
        return isContainedIn((Iterable<ItemStack>) invi);
    }

    public boolean isContainedIn(final Iterable<ItemStack> items) {
        for (final ItemData d : types) {
            int found = 0;
            for (final ItemStack i : items) {
                if (d.isOfType(i) && hasMeta(i)) {
                    found += i == null ? 1 : i.getAmount();
                    if (found >= getAmount()) {
                        if (!all)
                            return true;
                        break;
                    }
                }
            }
            if (all && found < getAmount())
                return false;
        }
        return all;
    }

    public boolean isContainedIn(final ItemStack[] list) {
        for (final ItemData d : types) {
            int found = 0;
            for (final ItemStack i : list) {
                if (d.isOfType(i) && hasMeta(i)) {
                    found += i == null ? 1 : i.getAmount();
                    if (found >= getAmount()) {
                        if (!all)
                            return true;
                        break;
                    }
                }
            }
            if (all && found < getAmount())
                return false;
        }
        return all;
    }

    public boolean removeAll(final Inventory invi) {
        final boolean wasAll = all;
        final int oldAmount = amount;
        all = true;
        amount = -1;
        try {
            return removeFrom(invi);
        } finally {
            all = wasAll;
            amount = oldAmount;
        }
    }

    /**
     * Removes this type from the given inventory. Does not call updateInventory for players.
     *
     * @param invi
     * @return Whatever everything could be removed from the inventory
     */
    public boolean removeFrom(final Inventory invi) {
        final ItemStack[] buf = invi.getContents();
        final ItemStack[] armour = invi instanceof PlayerInventory ? ((PlayerInventory) invi).getArmorContents() : null;

        @SuppressWarnings("unchecked") final boolean ok = removeFrom(Arrays.asList(buf), armour == null ? null : Arrays.asList(armour));

        invi.setContents(buf);
        if (armour != null)
            ((PlayerInventory) invi).setArmorContents(armour);
        return ok;
    }

    @SafeVarargs
    public final boolean removeAll(final List<ItemStack>... lists) {
        final boolean wasAll = all;
        final int oldAmount = amount;
        all = true;
        amount = -1;
        try {
            return removeFrom(lists);
        } finally {
            all = wasAll;
            amount = oldAmount;
        }
    }

    /**
     * @param lists The lists to remove this type from. Each list should implement {@link RandomAccess} or this method will be slow.
     * @return Whatever this whole item type could be removed (i.e. returns false if the lists didn't contain this item type completely)
     */
    @SafeVarargs
    public final boolean removeFrom(final List<ItemStack>... lists) {
        int removed = 0;
        boolean ok = true;

        for (final ItemData d : types) {
            if (all)
                removed = 0;
            for (final List<ItemStack> list : lists) {
                if (list == null)
                    continue;
                assert list instanceof RandomAccess;
                for (int i = 0; i < list.size(); i++) {
                    final ItemStack is = list.get(i);
                    if (is != null && d.isOfType(is) && hasMeta(is)) {
                        if (all && amount == -1) {
                            list.set(i, null);
                            removed = 1;
                            continue;
                        }
                        final int toRemove = Math.min(is.getAmount(), getAmount() - removed);
                        removed += toRemove;
                        if (toRemove == is.getAmount()) {
                            list.set(i, null);
                        } else {
                            is.setAmount(is.getAmount() - toRemove);
                        }
                        if (removed == getAmount()) {
                            if (!all)
                                return true;
                            break;
                        }
                    }
                }
            }
            if (all)
                ok &= removed == getAmount();
        }

        if (!all)
            return false;
        return ok;
    }

    /**
     * Adds this ItemType to the given list, without filling existing stacks.
     *
     * @param list
     */
    public void addTo(final List<ItemStack> list) {
        if (!all) {
            list.add(getItem().getRandom());
            return;
        }
        for (final ItemStack is : getItem().getAll())
            list.add(is);
    }

    /**
     * Tries to add this ItemType to the given inventory. Does not call updateInventory for players.
     *
     * @param invi
     * @return Whatever everything could be added to the inventory
     */
    public final boolean addTo(final Inventory invi) {
        // important: don't use inventory.add() - it ignores max stack sizes
        final ItemStack[] buf = invi.getContents();
        if (buf == null)
            return false;
        final boolean b = addTo(buf);
        invi.setContents(buf);
        return b;
    }

//	/**
//	 * Saves an array of ItemTypes, separated by '|'
//	 */
//	public static final String serialize(final ItemType[] types) {
//		if (types == null)
//			return "";
//		final StringBuilder b = new StringBuilder();
//		for (final ItemType t : types) {
//			if (b.length() != 0)
//				b.append("|");
//			final Pair<String, String> p = Classes.serialize(t);
//			assert p.first.equals("itemtype");
//			b.append(p.second.replace("|", "||"));
//		}
//		return b.toString();
//	}

    public boolean addTo(final ItemStack[] buf) {
        if (!all) {
            return addTo(getItem().getRandom(), buf);
        }
        boolean ok = true;
        for (final ItemStack is : getItem().getAll()) {
            ok &= addTo(is, buf);
        }
        return ok;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (all ? 1231 : 1237);
        result = prime * result + amount;
        final Map<Enchantment, Integer> enchs = enchantments;
        result = prime * result + (enchs == null ? 0 : enchs.hashCode());
        final Object m = meta;
        result = prime * result + (m == null ? 0 : m.hashCode());
        result = prime * result + types.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ItemType))
            return false;
        final ItemType other = (ItemType) obj;
        if (all != other.all)
            return false;
        if (amount != other.amount)
            return false;
        final Map<Enchantment, Integer> enchs = enchantments;
        if (enchs == null) {
            if (other.enchantments != null)
                return false;
        } else if (!enchs.equals(other.enchantments))
            return false;
        final Object m = meta;
        if (m == null) {
            if (other.meta != null)
                return false;
        } else if (!m.equals(other.meta))
            return false;
        return types.equals(other.types);
    }

    @Override
    public String toString() {
        return toString(false, 0, null);
    }

    @Override
    public String toString(final int flags) {
        return toString(false, flags, null);
    }

    public String toString(final int flags, @Nullable final Adjective a) {
        return toString(false, flags, a);
    }

    private final String toString(final boolean debug, final int flags, @Nullable final Adjective a) {
        final StringBuilder b = new StringBuilder(4096);
//		if (types.size() == 1 && !types.get(0).hasDataRange()) {
//			if (getAmount() != 1)
//				b.append(amount + " ");
//			if (isAll())
//				b.append(getAmount() == 1 ? "every " : "of every ");
//		} else {
//			if (getAmount() != 1)
//				b.append(amount + " of ");
//			b.append(isAll() ? "every " : "any ");
//		}
        final boolean plural = amount != 1 && amount != -1 || (flags & Language.F_PLURAL) != 0;
        if (amount != -1 && amount != 1) {
            b.append(amount).append(' ');
        } else {
            b.append(Noun.getArticleWithSpace(types.get(0).getGender(), flags));
        }
        if (a != null)
            b.append(a.toString(types.get(0).getGender(), flags));
        for (int i = 0; i < types.size(); i++) {
            if (i != 0) {// this belongs here as size-1 can be 0
                if (i == types.size() - 1)
                    b.append(' ').append(all ? GeneralWords.and : GeneralWords.or).append(' ');
                else
                    b.append(", ");
            }
            b.append(types.get(i).toString(debug, plural));
        }
        final Map<Enchantment, Integer> enchs = enchantments;
        if (enchs == null)
            return b.toString();
        b.append(Language.getSpaced("enchantments.of"));
        int i = 0;
        for (final Entry<Enchantment, Integer> e : enchs.entrySet()) {
            if (i != 0) {
                if (i != enchs.size() - 1)
                    b.append(", ");
                else
                    b.append(' ').append(GeneralWords.and).append(' ');
            }
            final Enchantment ench = e.getKey();
            if (ench == null)
                continue;
            b.append(EnchantmentType.toString(ench));
            b.append(' ');
            b.append(e.getValue());
            i++;
        }
        if (meta != null) {
            final ItemMeta m = (ItemMeta) meta;
            if (m.hasDisplayName()) {
                b.append(' ').append(m_named).append(' ');
                b.append('"').append(m.getDisplayName()).append('"');
            }
            if (debug)
                b.append(" meta=[").append(meta).append(']');
        }
        return b.toString();
    }

    public String getDebugMessage() {
        return toString(true, 0, null);
    }

    @Override
    public Fields serialize() throws NotSerializableException {
        final Fields f = new Fields(this);
        // both are serialisable with Yggdrasil
        f.putObject("enchantments", enchantments);
        f.putObject("meta", meta);
        return f;
    }

    @Override
    public void deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
        enchantments = fields.getAndRemoveObject("enchantments", Map.class);
        meta = fields.getAndRemoveObject("meta", Object.class);
        if (meta != null && !(meta instanceof ItemMeta))
            throw new StreamCorruptedException();
        fields.setFields(this);
    }

    private static final class ItemStackIterator implements Iterator<ItemStack> {
        @Nullable
        private final transient Map<Enchantment, Integer> enchantments;
        private final Iterator<ItemData> iter;

        @Nullable
        private final transient Object meta;
        private final int amount;

        @Nullable
        private Iterator<ItemStack> currentDataIter;

        ItemStackIterator(final Iterable<ItemData> types,
                          @Nullable final Object meta,
                          @Nullable final Map<Enchantment, Integer> enchantments,
                          final int amount) {
            iter = types.iterator();
            this.meta = meta;

            this.enchantments = enchantments;
            this.amount = amount;
        }

        @Override
        public final boolean hasNext() {
            Iterator<ItemStack> cdi = currentDataIter;
            while (iter.hasNext() && (cdi == null || !cdi.hasNext())) {
                currentDataIter = cdi = iter.next().getAll();
            }
            return cdi != null && cdi.hasNext();
        }

        @Override
        public final ItemStack next() {
            if (!hasNext())
                throw new NoSuchElementException();
            final Iterator<ItemStack> cdi = currentDataIter;
            if (cdi == null)
                throw new NoSuchElementException();
            final ItemStack is = cdi.next();
            is.setAmount(amount);
            if (meta != null)
                is.setItemMeta(Bukkit.getItemFactory().asMetaFor((ItemMeta) meta, is.getType()));
            if (enchantments != null)
                is.addUnsafeEnchantments(enchantments);
            return is;
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ItemDataIterator implements Iterator<ItemData> {
        private final ItemType itemType;
        private final List<ItemData> types;

        private int next;

        ItemDataIterator(final ItemType itemType,
                         final List<ItemData> types) {
            this.itemType = itemType;
            this.types = types;
        }

        @Override
        public final boolean hasNext() {
            return next < types.size();
        }

        @SuppressWarnings("null")
        @Override
        public final ItemData next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return types.get(next++);
        }

        @Override
        public final void remove() {
            if (next <= 0)
                throw new IllegalStateException();
            itemType.remove(--next);
        }
    }

}
