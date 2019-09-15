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

import ch.njol.skript.util.EmptyArrays;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import static kotlin.test.AssertionsKt.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("resource")
public final class YggdrasilTest {

    public static final PETest1 PET1_3 = new PETest1("PET1_3");
    @SuppressWarnings("rawtypes")
    public static final ArrayList[] EMPTY_RAW_ARRAY_LIST_ARRAY = new ArrayList[0];
    static final Yggdrasil y = new Yggdrasil();
    static final String modifiedClassID = "something random";
    static Class<?> currentModifiedClass = UnmodifiedClass.class;

    static {
        y.registerSingleClass(TestEnum.class);
        y.registerSingleClass(PETest1.class);
        y.registerSingleClass(PETest1.PETest2.class);
        y.registerSingleClass(TestClass1.class);
        y.registerSingleClass(TestClass2.class);
    }

    static {
        y.registerClassResolver(new ClassResolver() {
            @Override
            @Nullable
            public String getID(final Class<?> c) {
                if (c == currentModifiedClass)
                    return modifiedClassID;
                return null;
            }

            @Override
            @Nullable
            public Class<?> getClass(final String id) {
                if (id.equals(modifiedClassID))
                    return currentModifiedClass;
                return null;
            }
        });
    }

    // random objects
    /* private constructor is tested -> */
    @SuppressWarnings("synthetic-access")
    final Object[] random = {1, .5, true, 'a', "abc", "multi\nline\r\nstring\rwith\t\n\r\ttabs \u2001\nand\n\u00A0other\u2000\nwhitespace\0-\0", 2L, (byte) -1, (short) 124, Float.POSITIVE_INFINITY, Byte.MIN_VALUE, Byte.MAX_VALUE, (byte) -1, Short.MIN_VALUE, Short.MAX_VALUE, (short) -1, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, Long.MIN_VALUE, Long.MAX_VALUE, -1L, Float.MIN_NORMAL, Float.MIN_VALUE, Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, Double.MIN_NORMAL, Double.MIN_VALUE, Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, (byte) 0x12, (short) 0x1234, 0x12345678, 0x123456789abcdef0L, Float.intBitsToFloat(0x12345678), Double.longBitsToDouble(0x123456789abcdef0L),

            new double[]{0, 1, Double.MIN_NORMAL, Double.POSITIVE_INFINITY, Double.MAX_VALUE, -500, 0.123456, Double.NaN}, new float[]{.1f, 7f, 300}, new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x9a, (byte) 0xbc, (byte) 0xde, (byte) 0xf0}, new long[][][]{{{0}, {0, 5, 7}, null, EmptyArrays.EMPTY_LONG_ARRAY}}, new Object[][]{{new int[]{0, 4}}, null, {EmptyArrays.EMPTY_INT_ARRAY, null, new int[]{-1, 300, 42}}, EmptyArrays.EMPTY_INTEGER_ARRAY, new Integer[]{5, 7, null}, {null, null, new int[][]{null, {5, 7}, EmptyArrays.EMPTY_INT_ARRAY}}}, new ArrayList[][]{{new ArrayList<>(Arrays.asList(1, 2, null, 9, 100)), null, null, new ArrayList<>(Collections.emptyList())}, {null}, null, EMPTY_RAW_ARRAY_LIST_ARRAY},

            Object.class, ArrayList.class, new ArrayList<>(Arrays.asList(1, 2, 3)), new HashSet<>(Arrays.asList(1, 4, 3, 3, 2)), new HashMap<>(), new LinkedList<>(Arrays.asList(4, 3, 2, 1)),

            TestEnum.SOMETHING, PETest1.PET1_0, PETest1.PETest2.PET1_1, PETest1.PET1_2, PET1_3, PETest1.PETest2.PET2_1, PETest1.PET2_2, PETest1.PETest2.PET2_0, new TestClass1(), new TestClass1("foo"), new TestClass2(20)
    };

    @SuppressWarnings("null")
    private static final byte[] save(final @Nullable Object o) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final YggdrasilOutputStream s = y.newOutputStream(out);
        s.writeObject(o);
        s.flush();
        s.close();
        return out.toByteArray();
    }

//    @SuppressWarnings("null")
//    private static String saveXML(final @Nullable Object o) throws IOException {
//        final ByteArrayOutputStream out = new ByteArrayOutputStream();
//        final YggXMLOutputStream s = y.newXMLOutputStream(out);
//        s.writeObject(o);
//        s.flush();
//        s.close();
//        return out.toString("utf-8");
//    }

    @Nullable
    private static final Object load(final byte[] d) throws IOException {
        final YggdrasilInputStream l = y.newInputStream(new ByteArrayInputStream(d));
        return l.readObject();
    }

//    @Nullable
//    private static Object loadXML(final String xml) throws IOException {
//        final YggdrasilInputStream l = y.newXMLInputStream(new ByteArrayInputStream(xml.getBytes("utf-8")));
//        return l.readObject();
//    }

    private static final boolean equals(final @Nullable Object o1, final @Nullable Object o2) {
        if (o1 == null || o2 == null)
            return o1 == o2;
        if (o1.getClass() != o2.getClass())
            return false;
        if (o1.getClass().isArray()) {
            final int l1 = Array.getLength(o1);
            final int l2 = Array.getLength(o2);
            if (l1 != l2)
                return false;
            for (int i = 0; i < l1; i++) {
                if (!equals(Array.get(o1, i), Array.get(o2, i)))
                    return false;
            }
            return true;
        }
        if (o1 instanceof Collection) {
            final Iterator<?> i1 = ((Collection<?>) o1).iterator(), i2 = ((Collection<?>) o2).iterator();
            while (i1.hasNext()) {
                if (!i2.hasNext())
                    return false;
                if (!equals(i1.next(), i2.next()))
                    return false;
            }
            return !i1.hasNext();
        }
        return o1.equals(o2);
    }

//    private static final class CollectionTests {
//        Collection<?> al = new ArrayList<>(Arrays.asList(1, 2, 3)),
//                hs = new HashSet<>(Arrays.asList(1, 2, 3, 3, 4)),
//                ll = new LinkedList<>(Arrays.asList(4, 3, 2, 1));
//        Map<?, ?> hm = new HashMap<>();
//    }

    private static final String toString(final @Nullable Object o) {
        if (o == null)
            return "null";
        if (o.getClass().isArray()) {
            final StringBuilder b = new StringBuilder("[");
            b.append(o.getClass().getCanonicalName()).append("::");
            final int l = Array.getLength(o);
            for (int i = 0; i < l; i++) {
                if (i != 0)
                    b.append(", ");
                b.append(toString(Array.get(o, i)));
            }
            b.append("]");
            return "" + b;
        }
        return "" + o;
    }

//    @Test
//    public void generalXMLTest() throws IOException {
//        System.out.println();
//        for (final Object o : random) {
//            final String d = saveXML(o);
//            System.out.println(o + ": " + d);
//            System.out.println();
//            final Object l = loadXML(d);
//            assertEquals(o, l, toString(o) + " <> " + toString(l));
//            final String d2 = saveXML(l);
//            assertEquals(d, d2, toString(o) + "\n" + toString(d) + " <>\n" + toString(d2));
//        }
//    }

    @SuppressWarnings({"EmptyMethod", "unused"})
    private static final void print(final @Nullable Object o, final byte[] d) {
        /*
        System.out.print(o);
        System.out.print(": ");
        for (final byte b : d) {
            if (Pattern.matches("[a-zA-Z.]", "" + (char) b)) {
                System.out.print((char) b);
            } else {
                final String h = Integer.toHexString(b & 0xFF);
                System.out.print(" " + (h.length() == 1 ? "0" : "") + h + " ");
            }
        }
        System.out.println();
        System.out.println();
        */
    }

    @Test
    public void generalTest() throws IOException {
        //System.out.println();
        for (final Object o : random) {
            final byte[] d = save(o);
            print(o, d);
            final Object l = load(d);
            assertTrue(equals(o, l), o.getClass().getName() + ": " + toString(o) + " <> " + toString(l));
            final byte[] d2 = save(l);
            assertTrue(equals(d, d2), o.getClass().getName() + ": " + toString(o) + "\n" + toString(d) + " <>\n" + toString(d2));
        }
    }

    @SuppressWarnings("static-method")
    @Test
    public void keepReferencesTest() throws IOException {
        //System.out.println();
        final Object ref = new Object();
        final Map<Integer, Object> m = new HashMap<>();
        m.put(1, ref);
        m.put(2, new Object());
        m.put(3, ref);
        final byte[] md = save(m);
        print(m, md);
        @SuppressWarnings("unchecked") final Map<Integer, Object> ms = (Map<Integer, Object>) load(md);
        assertTrue(ms != null && ms.get(1) == ms.get(3) && ms.get(1) != ms.get(2), String.valueOf(ms));
    }

    @SuppressWarnings("static-method")
    @Test
    public void renameTest() throws IOException {
        //System.out.println();
        currentModifiedClass = UnmodifiedClass.class;
        final UnmodifiedClass o1 = new UnmodifiedClass(200);
        final byte[] d1 = save(o1);
        print(o1, d1);
        currentModifiedClass = ModifiedClass.class;
        final ModifiedClass o2 = (ModifiedClass) load(d1);
        assertNotNull(o2);
        assertEquals(o1.unchanged, o2.changed);

        currentModifiedClass = ModifiedClass.class;
        final ModifiedClass o3 = new ModifiedClass();
        final byte[] d3 = save(o3);
        print(o3, d3);
        currentModifiedClass = UnmodifiedClass.class;
        final UnmodifiedClass o4 = (UnmodifiedClass) load(d3);
        assertNotNull(o4);
        assertEquals(o3.changed, o4.unchanged);
    }

    @YggdrasilID("test-enum #!~/\r\n\t\\\"'<>&amp;,.:'`´¢⽰杻鱶")
    private enum TestEnum implements YggdrasilSerializable {
        SOMETHING, SOMETHINGELSE
    }

    @YggdrasilID("PETest1")
    private static class PETest1 extends PseudoEnum<PETest1> {
        public static final PETest1 PET1_0 = new PETest1("PET1_0 #!~/\r\n\t\\\"'<>&amp;,.:'`´¢⽰杻鱶");
        public static final PETest2 PET2_2 = new PETest2("PET2_2");
        public static final PETest1 PET1_2 = new PETest1("PET1_2") {
            /* empty */
        };

        protected PETest1(final String name) {
            super(name);
        }

        @YggdrasilID("PETest2")
        public static class PETest2 extends PETest1 {
            public static final PETest2 PET2_0 = new PETest2("PET2_0");
            public static final PETest2 PET2_1 = new PETest2("PET2_1") {
                @Override
                public String toString() {
                    return "PET2_1!!!";
                }
            };
            public static final PETest1 PET1_1 = new PETest1("PET1_1");

            protected PETest2(final String name) {
                super(name);
            }

        }

    }

    @YggdrasilID("TestClass1")
    private static final class TestClass1 implements YggdrasilSerializable {
        @Nullable
        private final String blah;

        TestClass1() {
            blah = "blah";
        }

        public TestClass1(final String b) {
            blah = b;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            final String blah = this.blah;
            result = prime * result + (blah == null ? 0 : blah.hashCode());
            return result;
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof TestClass1))
                return false;
            final TestClass1 other = (TestClass1) obj;
            final String blah = this.blah;
            if (blah == null) {
                return other.blah == null;
            }
            return blah.equals(other.blah);
        }

        @Override
        public String toString() {
            return "" + blah;
        }
    }

    @YggdrasilID("TestClass2")
    private static final class TestClass2 implements YggdrasilExtendedSerializable {
        private static final int DEFAULT = 5;
        private final int someFinalInt;
        private transient boolean ok;

        @SuppressWarnings("unused")
        public TestClass2() {
            someFinalInt = DEFAULT;
        }

        TestClass2(final int what) {
            assertNotEquals(DEFAULT, what);
            someFinalInt = what;
            ok = true;
        }

        @Override
        public Fields serialize() throws NotSerializableException {
            return new Fields(this);
        }

        @Override
        public void deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
            fields.setFields(this);
            assertFalse(ok);
            if (someFinalInt != DEFAULT)
                ok = true;
            assertTrue(ok);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (ok ? 1231 : 1237);
            result = prime * result + someFinalInt;
            return result;
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof TestClass2))
                return false;
            final TestClass2 other = (TestClass2) obj;
            if (ok != other.ok)
                return false;
            return someFinalInt == other.someFinalInt;
        }

        @Override
        public String toString() {
            return ok + "; " + someFinalInt;
        }
    }

    private static final class UnmodifiedClass implements YggdrasilSerializable {
        final int unchanged;

        @SuppressWarnings("unused")
        UnmodifiedClass() {
            unchanged = -10;
        }

        UnmodifiedClass(final int c) {
            unchanged = c;
        }
    }

    private static final class ModifiedClass implements YggdrasilSerializable {
        @YggdrasilID("unchanged")
        final int changed;

        ModifiedClass() {
            changed = -20;
        }

        @SuppressWarnings("unused")
        ModifiedClass(final int c) {
            changed = c;
        }
    }

}
