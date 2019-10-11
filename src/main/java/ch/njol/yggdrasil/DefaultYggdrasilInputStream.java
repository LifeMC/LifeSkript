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

import org.eclipse.jdt.annotation.NonNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static ch.njol.yggdrasil.Tag.T_ARRAY;
import static ch.njol.yggdrasil.Tag.T_REFERENCE;

//Naming conventions:
// x(): read info & data (e.g. content type, contents) [i.e. no tag]
// _x(): read data only (e.g. contents)

public final class DefaultYggdrasilInputStream extends YggdrasilInputStream {

    private final InputStream in;
    private final short version;
    private final List<String> readShortStrings = new ArrayList<>();

    // private

    public DefaultYggdrasilInputStream(final Yggdrasil y, final InputStream in) throws IOException {
        super(y);
        this.in = in;
        final int m = readInt();
        if (m != Yggdrasil.MAGIC_NUMBER)
            throw new StreamCorruptedException("Not an Yggdrasil stream");
        version = readShort();
        if (version <= 0 || version > Yggdrasil.LATEST_VERSION)
            throw new StreamCorruptedException("Input was saved using a later version of Yggdrasil");
    }

    /**
     * @throws EOFException If the end of the stream is reached
     */
    private final int read() throws IOException {
        final int b = in.read();
        if (b < 0)
            throw new EOFException();
        return b;
    }

    private final void readFully(final byte[] buf) throws IOException {
        readFully(buf, 0, buf.length);
    }

    private final void readFully(final byte[] buf, int off, final int len) throws IOException {
        int l = len;
        while (l > 0) {
            final int n = in.read(buf, off, l);
            if (n < 0)
                throw new EOFException("Expected " + len + " bytes, but could only read " + (len - l));
            off += n;
            l -= n;
        }
    }

    private final String readShortString() throws IOException {
        final int length = read();
        if (length == (T_REFERENCE.tag & 0xFF)) {
            final int i = version <= 1 ? readInt() : readUnsignedInt();
            if (i < 0 || i > readShortStrings.size())
                throw new StreamCorruptedException("Invalid short string reference " + i);
            return readShortStrings.get(i);
        }
        final byte[] d = new byte[length];
        {
            readFully(d);
        }
        final String s;
        {
            s = new String(d, StandardCharsets.UTF_8);
        }
        if (length > 4)
            readShortStrings.add(s);
        return s;
    }

    // Tag

    @Override
    protected final Tag readTag() throws IOException {
        final int t = read();
        final Tag tag = Tag.byID(t);
        if (tag == null)
            throw new StreamCorruptedException("Invalid tag 0x" + Integer.toHexString(t));
        return tag;
    }

    // Primitives

    private final byte readByte() throws IOException {
        return (byte) read();
    }

    private final short readShort() throws IOException {
        return (short) (read() << 8 | read());
    }

    private final short readUnsignedShort() throws IOException {
        final int b = read();
        if ((b & 0x80) != 0)
            return (short) (b & ~0x80);
        return (short) (b << 8 | read());
    }

    private final int readInt() throws IOException {
        return read() << 24 | read() << 16 | read() << 8 | read();
    }

    private final int readUnsignedInt() throws IOException {
        final int b = read();
        if ((b & 0x80) != 0)
            return (b & ~0x80) << 8 | read();
        return b << 24 | read() << 16 | read() << 8 | read();
    }

    private final long readLong() throws IOException {
        return (long) read() << 56 | (long) read() << 48 | (long) read() << 40 | (long) read() << 32 | (long) read() << 24 | read() << 16 | read() << 8 | read();
    }

    private final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    private final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    private final char readChar() throws IOException {
        return (char) readShort();
    }

    private final boolean readBoolean() throws IOException {
        final int r = read();
        if (r == 0)
            return false;
        if (r == 1)
            return true;
        throw new StreamCorruptedException("Invalid boolean value " + r);
    }

    @Override
    protected final Object readPrimitive(final Tag type) throws IOException {
        switch (type) {
            case T_BYTE:
                return readByte();
            case T_SHORT:
                return readShort();
            case T_INT:
                return readInt();
            case T_LONG:
                return readLong();
            case T_FLOAT:
                return readFloat();
            case T_DOUBLE:
                return readDouble();
            case T_CHAR:
                return readChar();
            case T_BOOLEAN:
                return readBoolean();
            //$CASES-OMITTED$
            default:
                throw new YggdrasilException("Internal error; " + type);
        }
    }

    @Override
    protected final Object readPrimitive_(final Tag type) throws IOException {
        return readPrimitive(type);
    }

    // String

    @Override
    protected final String readString() throws IOException {
        final int length = readUnsignedInt();
        final byte[] d = new byte[length];
        {
            readFully(d);
        }
        {
            return new String(d, StandardCharsets.UTF_8);
        }
    }

    // Array

    @Override
    protected final Class<?> readArrayComponentType() throws IOException {
        return readClass();
    }

    @Override
    protected final int readArrayLength() throws IOException {
        return readUnsignedInt();
    }

    // Enum

    @Override
    protected final Class<?> readEnumType() throws IOException {
        return yggdrasil.getClass(readShortString());
    }

    @Override
    protected final String readEnumID() throws IOException {
        return readShortString();
    }

    // Class

    @SuppressWarnings("null")
    @Override
    protected final Class<?> readClass() throws IOException {
        Tag type;
        int dim = 0;
        while ((type = readTag()) == T_ARRAY)
            dim++;
        @NonNull
        Class<?> c;
        switch (type) {
            case T_OBJECT:
            case T_ENUM:
                c = yggdrasil.getClass(readShortString());
                break;
            case T_BOOLEAN:
            case T_BOOLEAN_OBJ:
            case T_BYTE:
            case T_BYTE_OBJ:
            case T_CHAR:
            case T_CHAR_OBJ:
            case T_DOUBLE:
            case T_DOUBLE_OBJ:
            case T_FLOAT:
            case T_FLOAT_OBJ:
            case T_INT:
            case T_INT_OBJ:
            case T_LONG:
            case T_LONG_OBJ:
            case T_SHORT:
            case T_SHORT_OBJ:
            case T_CLASS:
            case T_STRING:
                c = type.c;
                assert c != null;
                break;
            case T_NULL:
            case T_REFERENCE:
                throw new StreamCorruptedException("unexpected tag " + type);
            default:
                throw new YggdrasilException("Internal error; " + type);
        }
        while (dim-- > 0)
            c = Array.newInstance(c, 0).getClass();
        return c;
    }

    // Reference

    @Override
    protected final int readReference() throws IOException {
        return readUnsignedInt();
    }

    // generic Object

    @Override
    protected final Class<?> readObjectType() throws IOException {
        return yggdrasil.getClass(readShortString());
    }

    @Override
    protected final short readNumFields() throws IOException {
        return readUnsignedShort();
    }

    @Override
    protected final String readFieldID() throws IOException {
        return readShortString();
    }

    // stream

    @Override
    public final void close() throws IOException {
        try {
            read();
            throw new StreamCorruptedException("Stream still has data, at least " + (1 + in.available()) + " bytes remaining");
        } catch (final EOFException ignored) {
            /* ignored */
        } finally {
            in.close();
        }
    }

}
