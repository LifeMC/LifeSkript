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

package ch.njol.util.coll;

import ch.njol.util.coll.iterator.ReversedListIterator;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Peter Güttinger
 */
public final class ReversedListView<T> implements List<T> {

    private final List<T> list;

    public ReversedListView(final List<T> list) {
        assert list != null;
        this.list = list;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public boolean contains(@Nullable final Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new ReversedListIterator<>(list);
    }

    @Override
    public ListIterator<T> listIterator() {
        return new ReversedListIterator<>(list);
    }

    @Override
    public ListIterator<T> listIterator(final int index) {
        return new ReversedListIterator<>(list, index);
    }

    @Override
    public @NotNull Object[] toArray() {
        final Object[] r = new Object[size()];
        int i = 0;
        for (final Object o : this)
            r[i++] = o;
        return r;
    }

    @Override
    @SuppressWarnings({"unchecked", "null"})
    public @NotNull <R> R[] toArray(final R[] a) {
        final R[] t = a.length >= size() ? a : (R[]) Array.newInstance(a.getClass().getComponentType(), size());
        int i = 0;
        for (final T o : this)
            t[i++] = (R) o;
        if (t.length > size())
            t[size()] = null;
        return t;
    }

    @SuppressWarnings("null")
    @Override
    public boolean add(final T e) {
        list.add(0, e);
        return true;
    }

    @Override
    public boolean remove(@Nullable final Object o) {
        @SuppressWarnings("unlikely-arg-type") final int i = list.lastIndexOf(o);
        if (i != -1)
            list.remove(i);
        return i != -1;
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public boolean containsAll(@Nullable final Collection<?> c) {
        return list.containsAll(c);
    }

    @SuppressWarnings("null")
    @Override
    public boolean addAll(final Collection<? extends T> c) {
        for (final T o : c)
            list.add(0, o);
        return true;
    }

    @SuppressWarnings("null")
    @Override
    public boolean addAll(final int index, final Collection<? extends T> c) {
        final int i = size() - index;
        for (final T o : c)
            list.add(i, o);
        return true;
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public boolean removeAll(@Nullable final Collection<?> c) {
        return list.removeAll(c);
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public boolean retainAll(@Nullable final Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    @Nullable
    public T get(final int index) {
        return list.get(size() - index - 1);
    }

    @SuppressWarnings("null")
    @Override
    @Nullable
    public T set(final int index, final T element) {
        return list.set(size() - index - 1, element);
    }

    @SuppressWarnings("null")
    @Override
    public void add(final int index, final T element) {
        list.add(size() - index, element);
    }

    @Override
    @Nullable
    public T remove(final int index) {
        return list.remove(size() - index - 1);
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public int indexOf(@Nullable final Object o) {
        return size() - list.lastIndexOf(o) - 1;
    }

    @SuppressWarnings("unlikely-arg-type")
    @Override
    public int lastIndexOf(@Nullable final Object o) {
        return size() - list.indexOf(o) - 1;
    }

    @Override
    public ReversedListView<T> subList(final int fromIndex, final int toIndex) {
        return new ReversedListView<>(list.subList(size() - toIndex, size() - fromIndex));
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (final T e : this)
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof List<?>))
            return false;
        final List<?> other = (List<?>) obj;
        if (other.size() != this.size())
            return false;
        final Iterator<?> os = other.iterator();
        for (final T value : this) {
            final Object o = os.next();
            if (!Objects.equals(value, o))
                return false;
        }
        return true;
    }

}
