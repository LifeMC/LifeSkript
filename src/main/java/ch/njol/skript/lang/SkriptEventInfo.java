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

package ch.njol.skript.lang;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.util.EmptyArrays;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SkriptEventInfo<E extends SkriptEvent> extends SyntaxElementInfo<E> {

    /**
     * Use this as {@link #description(String...)} to prevent warnings about missing documentation.
     */
    public static final String[] NO_DOC = EmptyArrays.EMPTY_STRING_ARRAY;
    private static final Matcher HTML = Pattern.compile("[#'\"<>/&]").matcher("");
    private static final Matcher WHITESPACE = Pattern.compile("\\s+").matcher("");
    public final String name;

    private final String id;
    public Class<? extends Event>[] events;
    @Nullable
    private String[] description;
    @Nullable
    private String[] examples;
    @Nullable
    private String since;
    @Nullable
    private String documentationID;
    @Nullable
    private String[] requiredPlugins;

    /**
     * @param name     Capitalised name of the event without leading "On" which is added automatically (Start the name with an asterisk to prevent this).
     * @param patterns
     * @param c        The SkriptEvent's class
     * @param events   The Bukkit-Events this SkriptEvent listens to
     */
    public SkriptEventInfo(String name, final String[] patterns, final Class<E> c, final Class<? extends Event>[] events) {
        super(patterns, c);
        assert name != null;
        assert patterns != null && patterns.length > 0;
        assert c != null;
        //skript-mirror
        //assert events != null && events.length > 0;

        for (int i = 0; i < events.length; i++) {
            for (int j = i + 1; j < events.length; j++) {
                if (events[i].isAssignableFrom(events[j]) || events[j].isAssignableFrom(events[i]))
                    throw new SkriptAPIException("The event " + name + " (" + c.getName() + ") registers with super/subclasses " + events[i].getName() + " and " + events[j].getName());
            }
        }

        this.events = events;

        if (!name.isEmpty() && name.charAt(0) == '*') {
            this.name = name = name.substring(1);
        } else {
            this.name = "On " + name;
        }

        // uses the name without 'on ' or '*'
        this.id = WHITESPACE.reset(HTML.reset(name.toLowerCase(Locale.ENGLISH)).replaceAll("")).replaceAll("_");
    }

    /**
     * A non-critical ID remapping for syntax elements to register using a class multiple times.
     * <p>
     * Only used for Skript's documentation.
     *
     * @param id The ID remapping for syntax elements to register using a class multiple times.
     * @return This SkriptEventInfo object
     */
    public SkriptEventInfo<E> documentationID(final String id) {
        assert this.documentationID == null;
        this.documentationID = id;
        return this;
    }

    /**
     * Other plugin dependencies for a syntax element
     * <p>
     * Only used for Skript's documentation
     *
     * @param pluginNames The required plugin names.
     * @return This SkriptEventInfo object
     */
    public SkriptEventInfo<E> requiredPlugins(final String... pluginNames) {
        assert this.requiredPlugins == null;
        this.requiredPlugins = pluginNames;
        return this;
    }

    /**
     * Only used for Skript's documentation.
     *
     * @param description
     * @return This SkriptEventInfo object
     */
    public SkriptEventInfo<E> description(final String... description) {
        assert this.description == null;
        this.description = description;
        return this;
    }

    /**
     * Only used for Skript's documentation.
     *
     * @param examples
     * @return This SkriptEventInfo object
     */
    public SkriptEventInfo<E> examples(final String... examples) {
        assert this.examples == null;
        this.examples = examples;
        return this;
    }

    /**
     * Only used for Skript's documentation.
     *
     * @param since
     * @return This SkriptEventInfo object
     */
    public SkriptEventInfo<E> since(final String since) {
        assert this.since == null;
        this.since = since;
        return this;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String[] getDescription() {
        return description;
    }

    @Nullable
    public String[] getExamples() {
        return examples;
    }

    @Nullable
    public String getSince() {
        return since;
    }

    @Nullable
    public String[] getRequiredPlugins() {
        return requiredPlugins;
    }

    @Nullable
    public String getDocumentationID() {
        return documentationID;
    }
}
