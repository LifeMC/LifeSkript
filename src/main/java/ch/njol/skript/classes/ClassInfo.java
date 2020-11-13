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

import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Debuggable;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.util.PatternCache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @param <T> The class this info is for
 * @author Peter Güttinger
 */
@SuppressFBWarnings("DM_STRING_VOID_CTOR")
public class ClassInfo<T> implements Debuggable {

    /**
     * Use this as {@link #name(String)} to suppress warnings about missing documentation.
     */
    public static final String NO_DOC = "";
    private static final Pattern VALID_CODENAME = Pattern.compile("[a-z0-9]+");
    private static final Matcher VALID_CODENAME_MATCHER = VALID_CODENAME.matcher("");
    private final Class<T> c;
    private final String codeName;
    private final Noun name;
    private final Set<String> after = new HashSet<>();
    @Nullable
    private DefaultExpression<T> defaultExpression;
    @Nullable
    private Parser<? extends T> parser;
    @Nullable
    private Pattern[] userInputPatterns;
    @Nullable
    private Changer<? super T> changer;
    @Nullable
    private Serializer<? super T> serializer;
    @Nullable
    private Class<?> serializeAs;
    @Nullable
    private Arithmetic<? super T, ?> math;
    @Nullable
    private Class<?> mathRelativeType;
    @Nullable
    private String docName;
    @Nullable
    private String[] description;
    @Nullable
    private String[] usage;
    @Nullable
    private String[] examples;
    @Nullable
    private String since;
    @Nullable
    private String[] requiredPlugins;

    /**
     * Overrides documentation id assigned from class name.
     */
    @Nullable
    private String documentationId;

    // === FACTORY METHODS ===
    @Nullable
    private Set<String> before;

    /**
     * @param c        The class
     * @param codeName The name used in patterns
     */
    public ClassInfo(final Class<T> c, final String codeName) {
        this.c = c;
        if (!isVaildCodeName(codeName))
            throw new IllegalArgumentException("Code names for classes must be lowercase and only consist of latin letters and arabic numbers");
        this.codeName = codeName;
        name = new Noun("types." + codeName);
    }

    private static final boolean isVaildCodeName(final CharSequence name) {
        return VALID_CODENAME_MATCHER.reset(name).matches();
    }

    /**
     * @param parser A parser to parse values of this class or null if not applicable
     */
    public final ClassInfo<T> parser(final Parser<? extends T> parser) {
        //for mundosk - fix assertion error
        //assert this.parser == null;
        this.parser = parser;
        return this;
    }

    /**
     * @param userInputPatterns <u>Regex</u> patterns to match this class, e.g. in the expressions loop-[type], random [type] out of ..., or as command arguments. These patterns
     *                          must be english and match singular and plural.
     * @throws PatternSyntaxException If any of the patterns' syntaxes is invalid
     */
    @SuppressWarnings("null")
    public final ClassInfo<T> user(final String... userInputPatterns) throws PatternSyntaxException {
        assert this.userInputPatterns == null;
        this.userInputPatterns = new Pattern[userInputPatterns.length];
        for (int i = 0; i < userInputPatterns.length; i++) {
            this.userInputPatterns[i] = PatternCache.get(userInputPatterns[i]);
        }
        return this;
    }

    /**
     * @param defaultExpression The default (event) value of this class or null if not applicable
     * @see EventValueExpression
     * @see SimpleLiteral
     */
    public final ClassInfo<T> defaultExpression(final DefaultExpression<T> defaultExpression) {
        assert this.defaultExpression == null;
        if (!defaultExpression.isDefault())
            throw new IllegalArgumentException("defaultExpression.isDefault() must return true for the default expression of a class");
        this.defaultExpression = defaultExpression;
        return this;
    }

    public final ClassInfo<T> serializer(final Serializer<? super T> serializer) {
        assert this.serializer == null;
        if (serializeAs != null)
            throw new IllegalStateException("Can't set a serializer if this class is set to be serialized as another one");
        this.serializer = serializer;
        serializer.register(this);
        return this;
    }

    public final ClassInfo<T> serializeAs(final Class<?> serializeAs) {
        //for skquery - fix assertion error
        //assert this.serializeAs == null;
        if (serializer != null)
            throw new IllegalStateException("Can't set this class to be serialized as another one if a serializer is already set");
        this.serializeAs = serializeAs;
        return this;
    }

    public final ClassInfo<T> changer(final Changer<? super T> changer) {
        assert this.changer == null;
        this.changer = changer;
        return this;
    }

    public final <R> ClassInfo<T> math(final Class<R> relativeType, final Arithmetic<? super T, R> math) {
        assert this.math == null;
        this.math = math;
        mathRelativeType = relativeType;
        return this;
    }

    /**
     * Only used for Skript's documentation.
     *
     * @param name
     * @return This ClassInfo object
     */
    public final ClassInfo<T> name(final String name) {
        assert this.docName == null;
        this.docName = name;
        return this;
    }

    /**
     * Only used for Skript's documentation.
     *
     * @param description
     * @return This ClassInfo object
     */
    public final ClassInfo<T> description(final String... description) {
        assert this.description == null;
        this.description = description;
        return this;
    }

    /**
     * Only used for Skript's documentation.
     *
     * @param usage
     * @return This ClassInfo object
     */
    public final ClassInfo<T> usage(final String... usage) {
        assert this.usage == null;
        this.usage = usage;
        return this;
    }

    // === GETTERS ===

    /**
     * Only used for Skript's documentation.
     *
     * @param examples
     * @return This ClassInfo object
     */
    public final ClassInfo<T> examples(final String... examples) {
        assert this.examples == null;
        this.examples = examples;
        return this;
    }

    /**
     * Only used for Skript's documentation.
     *
     * @param since
     * @return This ClassInfo object
     */
    public final ClassInfo<T> since(final String since) {
        assert this.since == null;
        this.since = since;
        return this;
    }

    /**
     * Other plugin dependencies for this ClassInfo.
     * <p>
     * Only used for Skript's documentation.
     *
     * @param pluginNames
     * @return This ClassInfo object.
     */
    public final ClassInfo<T> requiredPlugins(final String... pluginNames) {
        assert this.requiredPlugins == null;
        this.requiredPlugins = pluginNames;
        return this;
    }

    /**
     * A non-critical ID remapping for ClassInfo.
     * <p>
     * Overrides default documentation id, which is assigned from class name.
     * <p>
     * This is especially useful for inner classes whose names are useless without
     * parent class name as a context.
     * <p>
     * Only used for Skript's documentation.
     *
     * @param id
     * @return This ClassInfo object.
     */
    public final ClassInfo<T> documentationId(final String id) {
        assert this.documentationId == null;
        this.documentationId = id;
        return this;
    }

    public final Class<T> getC() {
        return c;
    }

    public final Noun getName() {
        return name;
    }

    public final String getCodeName() {
        return codeName;
    }

    @Nullable
    public final DefaultExpression<T> getDefaultExpression() {
        return defaultExpression;
    }

    @Nullable
    public final Parser<? extends T> getParser() {
        return parser;
    }

    @Nullable
    public final Pattern[] getUserInputPatterns() {
        return userInputPatterns;
    }

    @Nullable
    public final Changer<? super T> getChanger() {
        return changer;
    }

    @Nullable
    public final Serializer<? super T> getSerializer() {
        return serializer;
    }

    @Nullable
    public final Class<?> getSerializeAs() {
        return serializeAs;
    }

    @Nullable
    public final Arithmetic<? super T, ?> getMath() {
        return math;
    }

    @Nullable
    public final Class<?> getMathRelativeType() {
        return mathRelativeType;
    }

    @Nullable
    public final String[] getDescription() {
        return description;
    }

    @Nullable
    public final String[] getUsage() {
        return usage;
    }

    @Nullable
    public final String[] getExamples() {
        return examples;
    }

    // === ORDERING ===

    @Nullable
    public final String getSince() {
        return since;
    }

    @Nullable
    public String getDocName() {
        return docName;
    }

    @Nullable
    public final String[] getRequiredPlugins() {
        return requiredPlugins;
    }

    /**
     * Gets overridden documentation id of this type. If no override has
     * been set, null is returned, and the caller may try to derive this from
     * the name of {@code #getC()}.
     *
     * @return Documentation id override, or null.
     */
    @Nullable
    public final String getDocumentationId() {
        return documentationId;
    }

    /**
     * Sets one or more classes that this class should occur before in the class info list. This only affects the order in which classes are parsed if it's unknown of which type
     * the parsed string is.
     * <p>
     * Please note that subclasses will always be registered before superclasses, no matter what is defined here or in {@link #after(String...)}.
     * <p>
     * This list can safely contain classes that may not exist.
     *
     * @param before
     * @return this ClassInfo
     */
    public final ClassInfo<T> before(final String... before) {
        assert this.before == null;
        this.before = new HashSet<>(Arrays.asList(before));
        return this;
    }

    /**
     * Sets one or more classes that this class should occur after in the class info list. This only affects the order in which classes are parsed if it's unknown of which type
     * the parsed string is.
     * <p>
     * Please note that subclasses will always be registered before superclasses, no matter what is defined here or in {@link #before(String...)}.
     * <p>
     * This list can safely contain classes that may not exist.
     *
     * @param after
     * @return this ClassInfo
     */
    public final ClassInfo<T> after(final String... after) {
        this.after.addAll(Arrays.asList(after));
        return this;
    }

    /**
     * @return Set of classes that should be after this one. May return null.
     */
    @Nullable
    public final Set<String> before() {
        return before;
    }

    /**
     * @return Set of classes that should be before this one. Never returns null.
     */
    public final Set<String> after() {
        return after;
    }

    // === GENERAL ===

    @Override
    @NonNull
    public final String toString() {
        return name.getSingular();
    }

    public final String toString(final int flags) {
        return name.toString(flags);
    }

    @Override
    @NonNull
    public String toString(@Nullable final Event e, final boolean debug) {
        if (debug)
            return codeName + " (" + c.getCanonicalName() + ')';
        return name.getSingular();
    }

}
