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

package ch.njol.yggdrasil;

import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.eclipse.jdt.annotation.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.lang.reflect.Array;
import java.util.IdentityHashMap;

import static ch.njol.yggdrasil.Tag.*;

public abstract class YggdrasilOutputStream implements Flushable, Closeable {

    protected final Yggdrasil yggdrasil;
    private final IdentityHashMap<Object, Integer> writtenObjects = new IdentityHashMap<>();

    // Tag
    private int nextObjectID;

    // Null

    protected YggdrasilOutputStream(final Yggdrasil yggdrasil) {
        this.yggdrasil = yggdrasil;
    }

    // Primitives

    protected abstract void writeTag(final Tag t) throws IOException;

    private final void writeNull() throws IOException {
        writeTag(T_NULL);
    }

    protected abstract void writePrimitiveValue(final Object o) throws IOException;

    protected abstract void writePrimitive_(final Object o) throws IOException;

    // String

    private final void writePrimitive(final Object o) throws IOException {
        final Tag t = getType(o.getClass());
        assert t.isWrapper();
        final Tag p = t.getPrimitive();
        assert p != null;
        writeTag(p);
        writePrimitiveValue(o);
    }

    private final void writeWrappedPrimitive(final Object o) throws IOException {
        final Tag t = getType(o.getClass());
        assert t.isWrapper();
        writeTag(t);
        writePrimitiveValue(o);
    }

    // Array

    protected abstract void writeStringValue(final String s) throws IOException;

    private final void writeString(final String s) throws IOException {
        writeTag(T_STRING);
        writeStringValue(s);
    }

    protected abstract void writeArrayComponentType(final Class<?> componentType) throws IOException;

    protected abstract void writeArrayLength(final int length) throws IOException;

    // Enum

    @SuppressWarnings("EmptyMethod")
    protected abstract void writeArrayEnd() throws IOException;

    private final void writeArray(final Object array) throws IOException {
        final int length = Array.getLength(array);
        final Class<?> ct = array.getClass().getComponentType();
        assert ct != null;
        writeTag(T_ARRAY);
        writeArrayComponentType(ct);
        writeArrayLength(length);
        if (ct.isPrimitive()) {
            for (int i = 0; i < length; i++) {
                final Object p = Array.get(array, i);
                assert p != null;
                writePrimitive_(p);
            }
            writeArrayEnd();
        } else {
            for (final Object o : (Object[]) array)
                writeObject(o);
            writeArrayEnd();
        }
    }

    protected abstract void writeEnumType(final String type) throws IOException;

    protected abstract void writeEnumID(final String id) throws IOException;

    // Class

    private final void writeEnum(final Enum<?> o) throws IOException {
        writeTag(T_ENUM);
        final Class<?> c = o.getDeclaringClass();
        writeEnumType(yggdrasil.getID(c));
        writeEnumID(Yggdrasil.getID(o));
    }

    private final void writeEnum(final PseudoEnum<?> o) throws IOException {
        writeTag(T_ENUM);
        writeEnumType(yggdrasil.getID(o.getDeclaringClass()));
        writeEnumID(o.name());
    }

    // Reference

    protected abstract void writeClassType(final Class<?> c) throws IOException;

    private final void writeClass(final Class<?> c) throws IOException {
        writeTag(T_CLASS);
        writeClassType(c);
    }

    // generic Objects

    protected abstract void writeReferenceID(final int ref) throws IOException;

    protected final void writeReference(final int ref) throws IOException {
        assert ref >= 0;
        writeTag(T_REFERENCE);
        writeReferenceID(ref);
    }

    protected abstract void writeObjectType(final String type) throws IOException;

    protected abstract void writeNumFields(final short numFields) throws IOException;

    protected abstract void writeFieldID(final String id) throws IOException;

    // any Objects

    @SuppressWarnings("EmptyMethod")
    protected abstract void writeObjectEnd() throws IOException;

    @SuppressWarnings({"rawtypes", "unchecked", "unused", "null"})
    private final void writeGenericObject(final Object o, int ref) throws IOException {
        final Class<?> c = o.getClass();
        assert c != null;
        if (!yggdrasil.isSerializable(c))
            throw new NotSerializableException(c.getName());
        final Fields fields;
        final YggdrasilSerializer s = yggdrasil.getSerializer(c);
        if (s != null) {
            fields = s.serialize(o);
            if (fields == null)
                throw new YggdrasilException("The serializer of " + c + " returned null");
            if (!s.canBeInstantiated(c)) {
                ref = ~ref; // ~ instead of - to also get a negative value if ref is 0
                writtenObjects.put(o, ref);
            }
        } else if (o instanceof YggdrasilExtendedSerializable) {
            fields = ((YggdrasilExtendedSerializable) o).serialize();
            if (fields == null)
                throw new YggdrasilException("The serialize() method of " + c + " returned null");
        } else {
            fields = new Fields(o, yggdrasil);
        }
        if (fields.size() > Short.MAX_VALUE)
            throw new YggdrasilException("Class " + c.getCanonicalName() + " has too many fields (" + fields.size() + ')');

        writeTag(T_OBJECT);
        writeObjectType(yggdrasil.getID(c));
        writeNumFields((short) fields.size());
        for (final FieldContext f : fields) {
            writeFieldID(f.id);
            if (f.isPrimitive())
                writePrimitive(f.getPrimitive());
            else
                writeObject(f.getObject());
        }
        writeObjectEnd();

        if (ref < 0)
            writtenObjects.put(o, ~ref);
    }

    public final void writeObject(@Nullable final Object o) throws IOException {
        if (o == null) {
            writeNull();
            return;
        }
        if (writtenObjects.containsKey(o)) {
            final int ref = writtenObjects.get(o);
            if (ref < 0)
                throw new YggdrasilException("Uninstantiable object " + o + " is referenced in its fields' graph");
            writeReference(ref);
            return;
        }
        final int ref = nextObjectID;
        nextObjectID++;
        writtenObjects.put(o, ref);
        final Tag type = getType(o.getClass());
        if (type.isWrapper()) {
            writeWrappedPrimitive(o);
            return;
        }
        switch (type) {
            case T_ARRAY:
                writeArray(o);
                return;
            case T_STRING:
                writeString((String) o);
                return;
            case T_ENUM:
                if (o instanceof Enum)
                    writeEnum((Enum<?>) o);
                else
                    writeEnum((PseudoEnum<?>) o);
                return;
            case T_CLASS:
                writeClass((Class<?>) o);
                return;
            case T_OBJECT:
                writeGenericObject(o, ref);
                return;
            //$CASES-OMITTED$
            default:
                throw new YggdrasilException("unhandled type " + type);
        }
    }

}
