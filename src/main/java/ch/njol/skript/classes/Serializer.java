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

package ch.njol.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializer;
import org.eclipse.jdt.annotation.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Peter Güttinger
 */
public abstract class Serializer<T> extends YggdrasilSerializer<T> {

    @Nullable
    protected ClassInfo<? extends T> info;

    final void register(final ClassInfo<? extends T> info) {
        assert this.info == null && info != null;
        this.info = info;
    }

    @Override
    @Nullable
    public Class<? extends T> getClass(final String id) {
        final ClassInfo<? extends T> info = this.info;
        assert info != null;
        return id.equals(info.getCodeName()) ? info.getC() : null;
    }

    @Override
    @Nullable
    public String getID(final Class<?> c) {
        final ClassInfo<? extends T> info = this.info;
        assert info != null;
        return info.getC().isAssignableFrom(c) ? info.getCodeName() : null;
    }

    @Override
    @Nullable
    public <E extends T> E newInstance(final Class<E> c) {
        final ClassInfo<? extends T> info = this.info;
        assert info != null;
        assert info.getC().isAssignableFrom(c);
        try {
            final Constructor<E> constr = c.getDeclaredConstructor();
            constr.setAccessible(true);
            return constr.newInstance();
        } catch (final InstantiationException | NoSuchMethodException e) {
            throw new SkriptAPIException("Serializer of " + info.getCodeName() + " must override newInstance(), canBeInstantiated() or mustSyncDeserialization() if its class does not have a nullary constructor");
        } catch (final SecurityException e) {
            throw Skript.exception("Security manager present");
        } catch (final IllegalArgumentException | IllegalAccessException e) {
            assert false;
            return null;
        } catch (final InvocationTargetException e) {
            throw Skript.sneakyThrow(e.getCause());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>This method must be thread-safe</b>. Use {@link ch.njol.skript.util.Task#callSync(java.util.concurrent.Callable)} if you need to serialise on Bukkit's main thread.
     */
    @Override
    public abstract Fields serialize(final T o) throws NotSerializableException;

    @Override
    public abstract void deserialize(final T o, final Fields f) throws StreamCorruptedException, NotSerializableException;

    /**
     * Not currently used (everything happens on Bukkit's main thread).
     *
     * @return Whatever deserialisation must be done on Bukkit's main thread.
     */
    public abstract boolean mustSyncDeserialization();

    @Override
    public boolean canBeInstantiated(final Class<? extends T> c) {
        assert info != null && info.getC().isAssignableFrom(c);
        return canBeInstantiated();
    }

    /**
     * Returns whatever the class should be instantiated using its nullary constructor or not. Return false if the class has no nullary constructor or if you do not have control
     * over the source of the class (e.g. if it's from an API).
     * <p>
     * You must override and use {@link #deserialize(Fields)} if this method returns false ({@link #deserialize(Object, Fields)} will no be used anymore in this case).
     */
    protected abstract boolean canBeInstantiated();

    @SuppressWarnings("unchecked")
    @Override
    public <E extends T> E deserialize(final Class<E> c, final Fields fields) throws StreamCorruptedException, NotSerializableException {
        final ClassInfo<? extends T> info = this.info;
        assert info != null;
        assert info.getC().isAssignableFrom(c);
        return (E) deserialize(fields);
    }

    /**
     * Used to deserialise Bukkit objects and other stuff that cannot be instantiated, e.g. a plugin may and should not create a new instance of {@link org.bukkit.World}, but use
     * {@link org.bukkit.Bukkit#getWorld(String)} to get an existing world object.
     *
     * @param fields The Fields object that holds the information about the serialised object
     * @return The deserialised object. Must not be null (throw an exception instead).
     * @throws StreamCorruptedException If the given data is invalid or incomplete
     * @throws NotSerializableException
     */
    @SuppressWarnings("unused")
    protected T deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
        throw new SkriptAPIException("deserialize(Fields) has not been overridden in " + getClass() + " (serializer of " + info + ')');
    }

    /**
     * Deserialises an object from a string returned by this serializer or an earlier version thereof.
     * <p>
     * This method should only return null if the input is invalid (i.e. not produced by {@link #serialize(Object)} or an older version of that method)
     * <p>
     * This method must only be called from Bukkit's main thread if {@link #mustSyncDeserialization()} returned true.
     *
     * @param s
     * @return The deserialised object or null if the input is invalid. An error message may be logged to specify the cause.
     * @deprecated This method uses the old format according to comment on the code and was
     * deprecated in the commit https://github.com/Njol/Skript/commit/8d9dd58d8b33c7918b25cc5942b2a7b59f686f12#diff-cfd4f33a253ef540909a9c039905eeaeR125
     */
    @Deprecated
    @Nullable
    public T deserialize(final String s) {
        return null; // if this method is not overridden then no objects of this class will ever have been saved using the old format, so any input is invalid.
    }

}
