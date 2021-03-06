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

package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Converter.ConverterInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.EmptyArrays;
import ch.njol.skript.util.PropertyManager;
import ch.njol.skript.util.Slot;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.variables.DatabaseStorage;
import ch.njol.skript.variables.SerializedVariable;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Tag;
import ch.njol.yggdrasil.YggdrasilInputStream;
import ch.njol.yggdrasil.YggdrasilOutputStream;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.Contract;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public final class Classes {

    private static final List<ClassInfo<?>> tempClassInfos = new ArrayList<>(100);
    private static final HashMap<Class<?>, ClassInfo<?>> exactClassInfos = new HashMap<>(100);
    private static final HashMap<Class<?>, ClassInfo<?>> superClassInfos = new HashMap<>(100);
    private static final HashMap<String, ClassInfo<?>> classInfosByCodeName = new HashMap<>(100);
    /**
     * Consists of {@link ch.njol.yggdrasil.Yggdrasil#MAGIC_NUMBER} and {@link Variables#YGGDRASIL_VERSION}
     */
    private static final byte[] YGGDRASIL_START = {(byte) 'Y', (byte) 'g', (byte) 'g', 0, Variables.YGGDRASIL_VERSION >>> 8 & 0xFF, Variables.YGGDRASIL_VERSION & 0xFF};
    @SuppressWarnings("null")
    private static ClassInfo<?>[] classInfos;
    private static final boolean disableEnumClassInfos = Boolean.getBoolean("skript.disableEnumClassInfos");
    private static final boolean disableEnumTypes = Boolean.getBoolean("skript.disableEnumTypes");
    private static final boolean disableTypeManagers = Boolean.getBoolean("skript.disableTypeManagers");

    private Classes() {
        throw new UnsupportedOperationException();
    }

    private static final boolean enableSoftErrors = PropertyManager.getBoolean("skript.enableSoftErrors");

    /**
     * @param info info about the class to register
     */
    public static final <T> void registerClass(final ClassInfo<T> info) {
        Skript.checkAcceptRegistrations();
        if (classInfosByCodeName.containsKey(info.getCodeName()))
			if (Skript.testing() && Skript.debug() || enableSoftErrors)
				throw new IllegalArgumentException("Can't register " + info.getC().getName() + " with the code name " + info.getCodeName() + " because that name is already used by " + classInfosByCodeName.get(info.getCodeName()) + '(' + classInfosByCodeName.get(info.getCodeName()).getC().getName() + ')');
			else if (Skript.logNormal()) { // Do not throw a hard error for compatibility reasons, for example when another add-on or Skript adds some class that exists in another
				Skript.warning(info.getC().getName() + " with the code name " + info.getCodeName() + " is already used by " + classInfosByCodeName.get(info.getCodeName()) + '(' + classInfosByCodeName.get(info.getCodeName()).getC().getName() + ')');
				return;
			}
		if (exactClassInfos.containsKey(info.getC()))
            throw new IllegalArgumentException("Can't register the class info " + info.getCodeName() + " because the class " + info.getC().getName() + " is already registered");
        if (info.getCodeName().length() > DatabaseStorage.MAX_CLASS_CODENAME_LENGTH)
            throw new IllegalArgumentException("The codename '" + info.getCodeName() + "' is too long to be saved in a database, the maximum length allowed is " + DatabaseStorage.MAX_CLASS_CODENAME_LENGTH);
        exactClassInfos.put(info.getC(), info);
        classInfosByCodeName.put(info.getCodeName(), info);
        tempClassInfos.add(info);
    }

    public static final void onRegistrationsStop() {

        sortClassInfos();

        // validate serializeAs
        for (final ClassInfo<?> ci : getClassInfos()) {
            if (ci.getSerializeAs() != null) {
                final ClassInfo<?> sa = getExactClassInfo(ci.getSerializeAs());
                if (sa == null) {
                    Skript.error(ci.getCodeName() + "'s 'serializeAs' class is not registered");
                } else if (sa.getSerializer() == null) {
                    Skript.error(ci.getCodeName() + "'s 'serializeAs' class is not serializable");
                }
            }
        }

        // register to Yggdrasil
        for (final ClassInfo<?> ci : getClassInfos()) {
            final Serializer<?> s = ci.getSerializer();
            if (s != null)
                Variables.yggdrasil.registerClassResolver(s);
        }
    }

    /**
     * Sorts the class infos according to sub/superclasses and relations set with {@link ClassInfo#before(String...)} and {@link ClassInfo#after(String...)}.
     */
    @SuppressFBWarnings("LI_LAZY_INIT_STATIC")
    private static final void sortClassInfos() {
        assert classInfos == null;

        if (SkriptConfig.addonSafetyChecks.value())
            removeNullElements();

        // merge before, after & sub/supertypes in after
        for (final ClassInfo<?> ci : tempClassInfos) {
            final Set<String> before = ci.before();
            if (before != null && !before.isEmpty()) {
                for (final ClassInfo<?> ci2 : tempClassInfos) {
                    if (before.contains(ci2.getCodeName())) {
                        ci2.after().add(ci.getCodeName());
                        before.remove(ci2.getCodeName());
                        if (before.isEmpty())
                            break;
                    }
                }
            }
        }
        for (final ClassInfo<?> ci : tempClassInfos) {
            for (final ClassInfo<?> ci2 : tempClassInfos) {
                if (ci == ci2)
                    continue;
                if (ci.getC().isAssignableFrom(ci2.getC()))
                    ci.after().add(ci2.getCodeName());
            }
        }

        // remove unresolvable dependencies (and print a warning if testing)
        for (final ClassInfo<?> ci : tempClassInfos) {
            final Set<String> s = new HashSet<>();
            final Set<String> before = ci.before();
            if (before != null) {
                for (final String b : before) {
                    if (getClassInfoNoError(b) == null) {
                        s.add(b);
                    }
                }
                before.removeAll(s);
            }
            for (final String a : ci.after()) {
                if (getClassInfoNoError(a) == null) {
                    s.add(a);
                }
            }
            ci.after().removeAll(s);
            if (!s.isEmpty() && Skript.testing() && Skript.debug())
                Skript.info(s.size() + " dependency/ies could not be resolved for " + ci + ": " + StringUtils.join(s, ", "));
        }

        final List<ClassInfo<?>> classInfos = new ArrayList<>(tempClassInfos.size());

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < tempClassInfos.size(); i++) {
                final ClassInfo<?> ci = tempClassInfos.get(i);
                if (ci.after().isEmpty()) {
                    classInfos.add(ci);
                    tempClassInfos.remove(i);
                    i--;
                    for (final ClassInfo<?> ci2 : tempClassInfos)
                        ci2.after().remove(ci.getCodeName());
                    changed = true;
                }
            }
        }

        Classes.classInfos = classInfos.toArray(EmptyArrays.EMPTY_CLASSINFO_ARRAY);

        // check for circular dependencies
        if (!tempClassInfos.isEmpty()) {
            final StringBuilder b = new StringBuilder(4096);
            for (final ClassInfo<?> c : tempClassInfos) {
                if (b.length() != 0)
                    b.append(", ");
                b.append(c.getCodeName()).append(" (after: ").append(StringUtils.join(c.after(), ", ")).append(')');
            }
            throw new IllegalStateException("ClassInfos with circular dependencies detected: " + b);
        }

        // debug message
        if (Skript.debug()) {
            final StringBuilder b = new StringBuilder(4096);
            for (final ClassInfo<?> ci : classInfos) {
                if (b.length() != 0)
                    b.append(", ");
                b.append(ci.getCodeName());
            }
            Skript.info("All registered classes in order: " + b);
        }

    }

    @SuppressWarnings({"null", "unused"})
    private static final void removeNullElements() {
        if (Skript.testing() && Skript.debug()) {
            for (final Iterator<ClassInfo<?>> iterator = tempClassInfos.iterator(); iterator.hasNext(); ) {
                final ClassInfo<?> ci = iterator.next();
                if (ci.getC() == null) {
                    Skript.warning("The class info \"" + ci.getCodeName() + "\" does not have a valid backing java class, and it removed from the class info list.");
                    iterator.remove();
                }
            }
        } else
            tempClassInfos.removeIf(ci -> ci.getC() == null);
    }

    private static final void checkAllowClassInfoInteraction() {
        if (Skript.isAcceptRegistrations())
            throw new IllegalStateException("Cannot use classinfos until registration is over");
    }

    @SuppressWarnings({"null", "unused"})
    public static final List<ClassInfo<?>> getClassInfos() {
        checkAllowClassInfoInteraction();
        final ClassInfo<?>[] ci = classInfos;
        if (ci == null)
            return Collections.emptyList();
        return Collections.unmodifiableList(Arrays.asList(ci));
    }

    /**
     * This method can be called even while Skript is loading.
     *
     * @param codeName
     * @return The ClassInfo with the given code name
     * @throws SkriptAPIException If the given class was not registered
     */
    public static final ClassInfo<?> getClassInfo(final String codeName) {
        final ClassInfo<?> ci = classInfosByCodeName.get(codeName);
        if (ci == null)
            throw new SkriptAPIException("No class info found for " + codeName);
        return ci;
    }

    /**
     * This method can be called even while Skript is loading.
     *
     * @param codeName
     * @return The class info registered with the given code name or null if the code name is invalid or not yet registered
     */
    @Nullable
    public static final ClassInfo<?> getClassInfoNoError(@Nullable final String codeName) {
        return classInfosByCodeName.get(codeName);
    }

    /**
     * Gets the class info for the given class.
     * <p>
     * This method can be called even while Skript is loading.
     *
     * @param c The exact class to get the class info for
     * @return The class info for the given class of null if no info was found.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static final <T> ClassInfo<T> getExactClassInfo(@Nullable final Class<T> c) {
        if (c == ch.njol.skript.util.slot.Slot.class)
            return (ClassInfo<T>) exactClassInfos.get(Slot.class);
        return (ClassInfo<T>) exactClassInfos.get(c);
    }

    /**
     * Gets the class info of the given class or its closest registered superclass. This method will never return null unless <tt>c</tt> is null.
     *
     * @param c
     * @return The closest superclass's info
     */
    @SuppressWarnings({"unchecked", "null"})
    @Contract("null->null")
    public static final <T> ClassInfo<? super T> getSuperClassInfo(@Nullable final Class<T> c) {
        // Check null status
        if (c == null)
            return null;
        checkAllowClassInfoInteraction();
        final ClassInfo<?> i = superClassInfos.get(c);
        if (i != null)
            return (ClassInfo<? super T>) i;
        for (final ClassInfo<?> ci : getClassInfos()) {
            if (ci.getC().isAssignableFrom(c)) {
                if (!Skript.isAcceptRegistrations())
                    superClassInfos.put(c, ci);
                return (ClassInfo<? super T>) ci;
            }
        }
        assert false;
        return null;
    }

    /**
     * Gets a class by its code name
     *
     * @param codeName
     * @return the class with the given code name
     * @throws SkriptAPIException If the given class was not registered
     */
    public static final Class<?> getClass(final String codeName) {
        checkAllowClassInfoInteraction();
        return getClassInfo(codeName).getC();
    }

    /**
     * As the name implies
     *
     * @param name
     * @return the class info or null if the name was not recognized
     */
    @Nullable
    public static final ClassInfo<?> getClassInfoFromUserInput(String name) {
        checkAllowClassInfoInteraction();
        name = name.trim().toLowerCase(Locale.ENGLISH);
        for (final ClassInfo<?> ci : classInfos) {
            final Pattern[] uip = ci.getUserInputPatterns();
            if (uip == null)
                continue;
            for (final Pattern pattern : uip) {
                if (pattern.matcher(name).matches())
                    return ci;
            }
        }
        return null;
    }

    /**
     * As the name implies
     *
     * @param name
     * @return the class or null if the name was not recognized
     */
    @Nullable
    public static final Class<?> getClassFromUserInput(final String name) {
        checkAllowClassInfoInteraction();
        final ClassInfo<?> ci = getClassInfoFromUserInput(name);
        return ci == null ? null : ci.getC();
    }

    /**
     * Gets the default of a class
     *
     * @param codeName
     * @return the expression holding the default value or null if this class doesn't have one
     * @throws SkriptAPIException If the given class was not registered
     */
    @Nullable
    public static final DefaultExpression<?> getDefaultExpression(final String codeName) {
        checkAllowClassInfoInteraction();
        return getClassInfo(codeName).getDefaultExpression();
    }

    /**
     * Gets the default expression of a class
     *
     * @param c The class
     * @return The expression holding the default value or null if this class doesn't have one
     */
    @Nullable
    public static final <T> DefaultExpression<T> getDefaultExpression(final Class<T> c) {
        checkAllowClassInfoInteraction();
        final ClassInfo<T> ci = getExactClassInfo(c);
        return ci == null ? null : ci.getDefaultExpression();
    }

    /**
     * Gets the name a class was registered with.
     *
     * @param c The exact class
     * @return The name of the class or null if the given class wasn't registered.
     */
    @Nullable
    public static final String getExactClassName(final Class<?> c) {
        checkAllowClassInfoInteraction();
        final ClassInfo<?> ci = exactClassInfos.get(c);
        return ci == null ? null : ci.getCodeName();
    }

    @Nullable
    public static Parser<?> lastCheckedParser;

    /**
     * Parses without trying to convert anything.
     * <p>
     * Can log an error xor other log messages.
     *
     * @param s
     * @param c
     * @return The parsed object
     */
    @SuppressWarnings({"unchecked", "null"})
    @Nullable
    public static final <T> T parseSimple(final String s, final Class<T> c, final ParseContext context) {
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            for (final ClassInfo<?> info : classInfos) {
                final Parser<?> parser = info.getParser();
                if ((lastCheckedParser = parser) == null || !parser.canParse(context) || !c.isAssignableFrom(info.getC()) || !isAllowed(parser))
                    continue;
                log.clear();
                final T t = (T) parser.parse(s, context);
                if (t != null) {
                    log.printLog();
                    return t;
                }
            }
            log.printError();
        } finally {
            log.stop();
            lastCheckedParser = null;
        }
        return null;
    }

    public static final Map<Class<?>, Class<?>> enclosingClassCache =
            new HashMap<>(100);

    @Nullable
    public static final Class<?> getEnclosingClass(@Nullable final Class<?> clazz) {
        if (clazz == null)
            return null;

        if (enclosingClassCache.containsKey(clazz))  // Can contain null
            return enclosingClassCache.get(clazz);

        final Class<?> enclosingClass = clazz.getEnclosingClass();
        enclosingClassCache.put(clazz, enclosingClass);

        return enclosingClass;
    }

    private static final boolean isAllowed(@Nullable final Parser<?> parser) {
        if (parser == null)
            return false;
        if (!disableEnumClassInfos && !disableEnumTypes && !disableTypeManagers)
            return true;
        final Class<?> enclosingClass;
        if ((enclosingClass = getEnclosingClass(parser.getClass())) != null) {
            final String name = enclosingClass.getName();
            return (!disableEnumClassInfos || !name.contains("EnumClassInfo")) &&
                    (!disableEnumTypes || !name.contains("EnumType")) &&
                    (!disableTypeManagers || !name.contains("TypeManager"));
        }
        return true;
    }

    /**
     * Parses a string to get an object of the desired type.
     * <p>
     * Instead of repeatedly calling this with the same class argument, you should get a parser with {@link #getParser(Class)} and use it for parsing.
     * <p>
     * Can log an error if it returned null.
     *
     * @param s The string to parse
     * @param c The desired type. The returned value will be of this type or a subclass if it.
     * @return The parsed object
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Nullable
    public static final <T> T parse(final String s, final Class<T> c, final ParseContext context) {
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            T t = parseSimple(s, c, context);
            if (t != null) {
                log.printLog();
                return t;
            }
            for (final ConverterInfo<?, ?> conv : Converters.getConverters()) {
                if (context == ParseContext.COMMAND && (conv.options & Converter.NO_COMMAND_ARGUMENTS) != 0)
                    continue;
                if (c.isAssignableFrom(conv.to)) {
                    log.clear();
                    final Object o = parseSimple(s, conv.from, context);
                    if (o != null) {
                        t = (T) ((Converter) conv.converter).convert(o);
                        if (t != null) {
                            log.printLog();
                            return t;
                        }
                    }
                }
            }
            log.printError();
        } finally {
            log.stop();
        }
        return null;
    }

    /**
     * Gets a parser for parsing instances of the desired type from strings. The returned parser may only be used for parsing, i.e. you must not use its toString methods.
     *
     * @param to
     * @return A parser to parse object of the desired type
     */
    @SuppressWarnings({"unused", "unchecked", "null"})
    @Nullable
    public static final <T> Parser<? extends T> getParser(final Class<T> to) {
        checkAllowClassInfoInteraction();
        final ClassInfo<?>[] classInfos = Classes.classInfos;
        if (classInfos == null)
            return null;
        for (int i = classInfos.length - 1; i >= 0; i--) {
            final ClassInfo<?> ci = classInfos[i];
            if (to.isAssignableFrom(ci.getC()) && ci.getParser() != null)
                return (Parser<? extends T>) ci.getParser();
        }
        for (final ConverterInfo<?, ?> conv : Converters.getConverters()) {
            if (to.isAssignableFrom(conv.to)) {
                for (int i = classInfos.length - 1; i >= 0; i--) {
                    final ClassInfo<?> ci = classInfos[i];
                    final Parser<?> parser = ci.getParser();
                    if (conv.from.isAssignableFrom(ci.getC()) && parser != null)
                        return Classes.createConvertedParser(parser, (Converter<?, ? extends T>) conv.converter);
                }
            }
        }
        return null;
    }

    /**
     * Gets a parser for an exactly known class. You should usually use {@link #getParser(Class)} instead of this method.
     * <p>
     * The main benefit of this method is that it's the only class info method of Skript that can be used while Skript is initializing and thus useful for parsing configs.
     *
     * @param c
     * @return A parser to parse object of the desired type
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static final <T> Parser<? extends T> getExactParser(final Class<T> c) {
        if (Skript.isAcceptRegistrations()) {
            for (final ClassInfo<?> ci : tempClassInfos) {
                if (ci.getC() == c)
                    return (Parser<? extends T>) ci.getParser();
            }
            return null;
        }
        final ClassInfo<T> ci = getExactClassInfo(c);
        return ci == null ? null : ci.getParser();
    }

    private static final <F, T> Parser<T> createConvertedParser(final Parser<?> parser, final Converter<F, T> converter) {
        return new Parser<T>() {
            @SuppressWarnings("unchecked")
            @Override
            @Nullable
            public T parse(final String s, final ParseContext context) {
                final Object f = parser.parse(s, context);
                if (f == null)
                    return null;
                return converter.convert((F) f);
            }

            @Override
            public String toString(final T o, final int flags) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toVariableNameString(final T o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getVariableNamePattern() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @param o Any object, preferably not an array: use {@link Classes#toString(Object[], boolean)} instead.
     * @return String representation of the object (using a parser if found or {@link String#valueOf(Object)} otherwise).
     * @see #toString(Object, StringMode)
     * @see #toString(Object[], boolean)
     * @see #toString(Object[], boolean, StringMode)
     * @see Parser
     */
    public static final String toString(@Nullable final Object o) {
        return toString(o, StringMode.MESSAGE, 0, null, false);
    }

    public static final String toString(@Nullable final Object o,
                                        @Nullable final VariableString variableName, final boolean debug) {
        return toString(o, StringMode.MESSAGE, 0, variableName, debug);
    }

    public static final String getDebugMessage(@Nullable final Object o) {
        return toString(o, StringMode.DEBUG, 0, null, false);
    }

    public static final <T> String toString(@Nullable final T o, final StringMode mode) {
        return toString(o, mode, 0, null, false);
    }

    private static final <T> String toString(@Nullable final T o, final StringMode mode, final int flags,
                                             @Nullable final VariableString variableName, final boolean debug) {
        assert flags == 0 || mode == StringMode.MESSAGE;
        if (o == null) {
            if (SkriptConfig.warnWhenUsingNoneValues.value() && mode != StringMode.DEBUG)
                if (variableName != null)
                    Skript.warning("Usage of none is detected - the variable " + variableName + " is null (mode: " + mode + ", flags: " + flags + ", debug: " + debug + ')');
                else
                    Skript.warning("Usage of none is detected - probably some variable or expression returned null (mode: " + mode + ", flags: " + flags + ')');
            return Language.get("none");
        }
        if (o.getClass().isArray()) {
            if (((Object[]) o).length == 0) {
                if (SkriptConfig.warnWhenUsingNoneValues.value() && mode != StringMode.DEBUG)
                    if (variableName != null)
                        Skript.warning("Usage of none is detected - the variable " + variableName + " is empty (mode: " + mode + ", flags: " + flags + ", type: " + o.getClass().getCanonicalName() + ", debug: " + debug + ')');
                    else
                        Skript.warning("Usage of none is detected - probably some list variable or expression is empty (mode: " + mode + ", flags: " + flags + ", type: " + o.getClass().getCanonicalName() + ')');
                return Language.get("none");
            }
            final StringBuilder b = new StringBuilder(4096);
            boolean first = true;
            for (final Object i : (Object[]) o) {
                if (!first)
                    b.append(", ");
                b.append(toString(i, mode, flags, null, false));
                first = false;
            }
            return "[" + b + ']';
        }
        for (final ClassInfo<?> ci : classInfos) {
            final Parser<?> parser = ci.getParser();
            if (parser != null && ci.getC().isInstance(o)) {
                @SuppressWarnings("unchecked") final String s = mode == StringMode.MESSAGE ? ((Parser<T>) parser).toString(o, flags) : mode == StringMode.DEBUG ? '[' + ci.getCodeName() + ':' + ((Parser<T>) parser).toString(o, mode) + ']' : ((Parser<T>) parser).toString(o, mode);
                return s;
            }
        }
        return (mode == StringMode.VARIABLE_NAME ? "object:" : "") + o;
    }

    public static final String toString(final Object[] os, final int flags, final boolean and) {
        return toString(os, and, null, StringMode.MESSAGE, flags);
    }

    public static final String toString(final Object[] os, final int flags, @Nullable final ChatColor c) {
        return toString(os, true, c, StringMode.MESSAGE, flags);
    }

    public static final String toString(final Object[] os, final boolean and) {
        return toString(os, and, null, StringMode.MESSAGE, 0);
    }

    public static final String toString(final Object[] os, final boolean and, final StringMode mode) {
        if (os.length == 0)
            return toString(null);
        if (os.length == 1)
            return toString(os[0], mode, 0, null, false);
        return toString(os, and, null, mode, 0);
    }

    private static final String toString(final Object[] os, final boolean and, @Nullable final ChatColor c, final StringMode mode, final int flags) {
        if (os.length == 0)
            return toString(null);
        if (os.length == 1)
            return toString(os[0], mode, flags, null, false);
        final StringBuilder b = new StringBuilder(4096);
        for (int i = 0; i < os.length; i++) {
            if (i != 0) {
                if (c != null)
                    b.append(c);
                if (i == os.length - 1)
                    b.append(and ? " and " : " or ");
                else
                    b.append(", ");
            }
            b.append(toString(os[i], mode, flags, null, false));
        }
        return b.toString();
    }

    private static final byte[] getYggdrasilStart(final ClassInfo<?> c) throws NotSerializableException {
        assert Enum.class.isAssignableFrom(Kleenean.class) && Tag.getType(Kleenean.class) == Tag.T_ENUM : Tag.getType(Kleenean.class); // TODO why is this check here?
        final Tag t = Tag.getType(c.getC());
        assert t.isWrapper() || t == Tag.T_STRING || t == Tag.T_OBJECT || t == Tag.T_ENUM;
        final byte[] cn = t == Tag.T_OBJECT || t == Tag.T_ENUM ? Variables.yggdrasil.getID(c.getC()).getBytes(StandardCharsets.UTF_8) : null;
        final byte[] r = new byte[YGGDRASIL_START.length + 1 + (cn == null ? 0 : 1 + cn.length)];
        int i = 0;
        for (; i < YGGDRASIL_START.length; i++)
            r[i] = YGGDRASIL_START[i];
        r[i++] = t.tag;
        if (cn != null) {
            r[i++] = (byte) cn.length;
            for (final byte element : cn)
                r[i++] = element;
        }
        assert i == r.length;
        return r;
    }

    /**
     * Must be called on the appropriate thread for the given value (i.e. the main thread currently)
     */
    @Nullable
    public static final SerializedVariable.Value serialize(@Nullable Object o) {
        if (o == null)
            return null;

        ClassInfo<?> ci = getSuperClassInfo(o.getClass());
        if (ci.getSerializeAs() != null) {
            ci = getExactClassInfo(ci.getSerializeAs());
            if (ci == null) {
                assert false : o.getClass();
                return null;
            }
            o = Converters.convert(o, ci.getC());
            if (o == null) {
                assert false : ci.getCodeName();
                return null;
            }
        }

        final Serializer<?> s = ci.getSerializer();
        if (s == null) // value cannot be saved
            return null;

        /* FIXME Fix this error
        [00:51:05] [Craft Scheduler Thread - 1037/WARN]: Uncaught exception in thread "Craft Scheduler Thread - 1037"
        [00:51:05] [Craft Scheduler Thread - 1037/WARN]: org.apache.commons.lang.UnhandledException: Plugin Skript v2.2.15 generated an exception while executing task 110
            at org.bukkit.craftbukkit.v1_7_R4.scheduler.CraftAsyncTask.run(CraftAsyncTask.java:55)
            at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
            at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
            at java.lang.Thread.run(Thread.java:813)
        Caused by: java.lang.AssertionError
            at ch.njol.skript.registrations.Classes.serialize(Classes.java:686)
            at ch.njol.skript.variables.FlatFileStorage.save(FlatFileStorage.java:419)
            at ch.njol.skript.variables.FlatFileStorage.save(FlatFileStorage.java:413)
            at ch.njol.skript.variables.FlatFileStorage.saveVariables(FlatFileStorage.java:377)
            at ch.njol.skript.variables.FlatFileStorage$1.run(FlatFileStorage.java:241)
            at org.bukkit.craftbukkit.v1_7_R4.scheduler.CraftTask.run(CraftTask.java:59)
            at org.bukkit.craftbukkit.v1_7_R4.scheduler.CraftAsyncTask.run(CraftAsyncTask.java:52)
            ... 3 more
         */
        //assert !s.mustSyncDeserialization() || Bukkit.isPrimaryThread() : o.getClass().getCanonicalName();

        final ByteArrayOutputStream bout = new ByteArrayOutputStream(4096);
        try (final YggdrasilOutputStream yout = Variables.yggdrasil.newOutputStream(bout)) {
            yout.writeObject(o);
            yout.flush();
            final byte[] r = bout.toByteArray();
            final byte[] start = getYggdrasilStart(ci);
            for (int i = 0; i < start.length; i++)
                assert r[i] == start[i] : o + " (" + ci.getC().getName() + "); " + Arrays.toString(start) + ", " + Arrays.toString(r);
            final byte[] r2 = new byte[r.length - start.length];
            System.arraycopy(r, start.length, r2, 0, r2.length);

            // FIXME Fix assertion error with UnresolvedOfflinePlayer
            //Object d;
            //assert equals(o, d = deserialize(ci, new ByteArrayInputStream(r2))) : o + " (" + o.getClass() + ") != " + d + " (" + (d == null ? null : d.getClass()) + "): " + Arrays.toString(r);

            return new SerializedVariable.Value(ci.getCodeName(), r2);
        } catch (final IOException e) { // shouldn't happen
            Skript.exception(e);
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static final boolean equals(@Nullable final Object o, @Nullable final Object d) {
        if (o instanceof Chunk) { // CraftChunk does neither override equals nor is it a "coordinate-specific singleton" like Block
            if (!(d instanceof Chunk))
                return false;
            final Chunk c1 = (Chunk) o, c2 = (Chunk) d;
            return c1.getWorld().equals(c2.getWorld()) && c1.getX() == c2.getX() && c1.getZ() == c2.getZ();
        }
        return Objects.equals(o, d);
    }

    @Nullable
    public static final Object deserialize(final ClassInfo<?> type, final byte[] value) {
        return deserialize(type, new ByteArrayInputStream(value));
    }

    @Nullable
    public static final Object deserialize(final String type, final byte[] value) {
        final ClassInfo<?> ci = getClassInfoNoError(type);
        if (ci == null)
            return null;
        return deserialize(ci, new ByteArrayInputStream(value));
    }

    @Nullable
    public static final Object deserialize(final ClassInfo<?> type, final InputStream value) {
        if (Skript.testing()) {
            final Serializer<?> s;
            //noinspection AssertWithSideEffects
            assert (s = type.getSerializer()) != null && (!s.mustSyncDeserialization() || Bukkit.isPrimaryThread()) : type + "; " + s + "; " + Bukkit.isPrimaryThread();
        }
        BufferedInputStream buf = null;
        try (final InputStream newValue = new SequenceInputStream(new ByteArrayInputStream(getYggdrasilStart(type)), value instanceof BufferedInputStream ? value : (buf = new BufferedInputStream(value)));
             final YggdrasilInputStream in = Variables.yggdrasil.newInputStream(newValue)) {

            return in.readObject();
        } catch (final IOException e) { // i.e. invalid save
            if (Skript.testing() || Skript.debug())
                Skript.exception(e);
            return null;
        } finally {
            try {
                value.close();

                if (buf != null)
                    buf.close();
            } catch (final IOException e) {
                if (Skript.testing() || Skript.debug())
                    Skript.exception(e);
            }
        }
    }

    /**
     * Deserialises an object.
     * <p>
     * This method must only be called from Bukkits main thread!
     *
     * @param type
     * @param value
     * @return Deserialised value or null if the input is invalid
     * @deprecated not used anymore, see {@link Classes#deserialize(ClassInfo, InputStream)},
     * {@link Classes#deserialize(ClassInfo, byte[])} and {@link Classes#deserialize(String, byte[])}.
     */
    @Deprecated
    @Nullable
    public static final Object deserialize(final String type, final String value) {
        assert Bukkit.isPrimaryThread();
        final ClassInfo<?> ci = getClassInfoNoError(type);
        if (ci == null)
            return null;
        final Serializer<?> s = ci.getSerializer();
        if (s == null)
            return null;
        return s.deserialize(value);
    }

}
