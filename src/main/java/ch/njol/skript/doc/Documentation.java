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

package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.conditions.CondCompare;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.MatcherCache;
import ch.njol.skript.util.PatternCache;
import ch.njol.skript.util.Utils;
import ch.njol.util.LineSeparators;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.IteratorIterable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO list special expressions for events and event values
 * TODO compare doc in code with changed one of the webserver and warn about differences?
 *
 * @author Peter Güttinger
 */
@SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
public final class Documentation {

    public static final boolean generate = Skript.testing() && new File(Skript.getInstance().getDataFolder(), "generate-doc").exists(); // don't generate the documentation on normal servers
    private static final Collection<Pattern> validation = new ArrayList<>();
    private static final String[] urls = {"expressions", "effects", "conditions"};

    static {
        validation.add(Pattern.compile('<' + "(?!a href='|/a>|br ?/|/?(i|b|u|code|pre|ul|li|em)>)"));
        validation.add(Pattern.compile("(?<!</a|'|br ?/|/?(i|b|u|code|pre|ul|li|em))" + '>'));
    }

    private Documentation() {
        throw new UnsupportedOperationException();
    }

    public static final void generate() {
        if (!generate)
            return;
        try (final PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(Skript.getInstance().getDataFolder(), "doc.sql")), StandardCharsets.UTF_8))) {
            asSql(pw);
            pw.flush();
        } catch (final Throwable tw) {
            tw.printStackTrace();
        }
    }

    private static final void asSql(final PrintWriter pw) {
        pw.println("-- syntax elements");
//		pw.println("DROP TABLE IF EXISTS syntax_elements;");
        pw.println("CREATE TABLE IF NOT EXISTS syntax_elements (" + "id VARCHAR(20) NOT NULL PRIMARY KEY," + "name VARCHAR(100) NOT NULL," + "type ENUM('condition','effect','expression','event') NOT NULL," + "patterns VARCHAR(2000) NOT NULL," + "description VARCHAR(2000) NOT NULL," + "examples VARCHAR(2000) NOT NULL," + "since VARCHAR(100) NOT NULL" + ");");
        pw.println("UPDATE syntax_elements SET patterns='';");
        pw.println();
        pw.println("-- expressions");
        for (final ExpressionInfo<?, ?> e : new IteratorIterable<>(Skript.getExpressions())) {
            assert e != null;
            insertSyntaxElement(pw, e, "expression");
        }
        pw.println();
        pw.println("-- effects");
        for (final SyntaxElementInfo<?> info : Skript.getEffects()) {
            assert info != null;
            insertSyntaxElement(pw, info, "effect");
        }
        pw.println();
        pw.println("-- conditions");
        for (final SyntaxElementInfo<?> info : Skript.getConditions()) {
            assert info != null;
            insertSyntaxElement(pw, info, "condition");
        }
        pw.println();
        pw.println("-- events");
        for (final SkriptEventInfo<?> info : Skript.getEvents()) {
            assert info != null;
            insertEvent(pw, info);
        }

        pw.println();
        pw.println();
        pw.println("-- classes");
//		pw.println("DROP TABLE IF EXISTS classes;");
        pw.println("CREATE TABLE IF NOT EXISTS classes (" + "id VARCHAR(20) NOT NULL PRIMARY KEY," + "name VARCHAR(100) NOT NULL," + "description VARCHAR(2000) NOT NULL," + "patterns VARCHAR(2000) NOT NULL," + "`usage` VARCHAR(2000) NOT NULL," + "examples VARCHAR(2000) NOT NULL," + "since VARCHAR(100) NOT NULL" + ");");
        pw.println("UPDATE classes SET patterns='';");
        pw.println();
        for (final ClassInfo<?> info : Classes.getClassInfos()) {
            assert info != null;
            insertClass(pw, info);
        }

        pw.println();
        pw.println();
        pw.println("-- functions");
        pw.println("CREATE TABLE IF NOT EXISTS functions (" + "name VARCHAR(100) NOT NULL," + "parameters VARCHAR(2000) NOT NULL," + "description VARCHAR(2000) NOT NULL," + "examples VARCHAR(2000) NOT NULL," + "since VARCHAR(100) NOT NULL" + ");");
        for (final JavaFunction<?> func : Functions.getJavaFunctions()) {
            assert func != null;
            insertFunction(pw, func);
        }
    }

    private static final String convertRegex(final String regex) {
        if (StringUtils.containsAny(regex, ".[]\\*+"))
            Skript.error("Regex '" + regex + "' contains unconverted Regex syntax");
        return escapeHTML(regex.replaceAll("\\((.+?)\\)\\?", "[$1]").replaceAll("(.)\\?", "[$1]"));
    }

    private static final String cleanPatterns(final String patterns) {
        // link & fancy types
        final String s = StringUtils.replaceAll(escapeHTML(patterns) // escape HTML
                        .replaceAll("(?<=[(|])[-0-9]+?¦", "") // remove marks
                        .replace("()", "") // remove empty mark setting groups (mark¦)
                        .replaceAll("\\(([^|]+?)\\|\\)", "[$1]") // replace (mark¦x|) groups with [x]
                        .replaceAll("\\(\\|([^|]+?)\\)", "[$1]") // dito
                        .replaceAll("\\((.+?)\\|\\)", "[($1)]") // replace (a|b|) with [(a|b)]
                        .replaceAll("\\(\\|(.+?)\\)", "[($1)]") // dito
                , "(?<!\\\\)%(.+?)(?<!\\\\)%", m -> {
                    @SuppressWarnings("null")
                    String s1 = m.group(1);
                    if (s1.startsWith("-"))
                        s1 = s1.substring(1);
                    String flag = "";
                    if (s1.startsWith("*") || s1.startsWith("~")) {
                        flag = s1.substring(0, 1);
                        s1 = s1.substring(1);
                    }
                    final int a = s1.indexOf('@');
                    if (a != -1)
                        s1 = s1.substring(0, a);
                    final StringBuilder b = new StringBuilder("%");
                    b.append(flag);
                    boolean first = true;
                    for (final String c : s1.split("/")) {
                        assert c != null;
                        if (!first)
                            b.append('/');
                        first = false;
                        final NonNullPair<String, Boolean> p = Utils.getEnglishPlural(c);
                        final ClassInfo<?> ci = Classes.getClassInfoNoError(p.getFirst());
                        if (ci != null && ci.getDocName() != null && !ClassInfo.NO_DOC.equals(ci.getDocName())) {
                            b.append("<a href='../classes/#").append(p.getFirst()).append("'>").append(ci.getName().toString(p.getSecond())).append("</a>");
                        } else {
                            b.append(c);
                            if (ci != null && !Objects.equals(ci.getDocName(), ClassInfo.NO_DOC))
                                Skript.warning("Used class " + p.getFirst() + " has no docName/name defined");
                        }
                    }
                    return b.append('%').toString();
                });
        assert s != null : patterns;
        return s;
    }

    private static final void insertSyntaxElement(final PrintWriter pw, final SyntaxElementInfo<?> info, final String type) {
        if (info.c.getAnnotation(NoDoc.class) != null)
            return;
        if (info.c.getAnnotation(Name.class) == null || info.c.getAnnotation(Description.class) == null || info.c.getAnnotation(Examples.class) == null || info.c.getAnnotation(Since.class) == null) {
            Skript.warning(info.c.getSimpleName() + " is missing information");
            return;
        }
        final String desc = validateHTML(StringUtils.join(info.c.getAnnotation(Description.class).value(), "<br/>"), type + 's');
        final String since = validateHTML(info.c.getAnnotation(Since.class).value(), type + 's');
        if (desc == null || since == null) {
            Skript.warning(info.c.getSimpleName() + "'s description or 'since' is invalid");
            return;
        }
        final String patterns = cleanPatterns(StringUtils.join(info.patterns, LineSeparators.UNIX, 0, info.c == CondCompare.class ? 8 : info.patterns.length));
        insertOnDuplicateKeyUpdate(pw, "syntax_elements", "id, name, type, patterns, description, examples, since", "patterns = TRIM(LEADING '" + LineSeparators.UNIX + "' FROM CONCAT(patterns, '" + LineSeparators.UNIX + "', '" + escapeSQL(patterns) + "'))", escapeHTML(info.c.getSimpleName()), escapeHTML(info.c.getAnnotation(Name.class).value()), type, patterns, desc, escapeHTML(StringUtils.join(info.c.getAnnotation(Examples.class).value(), LineSeparators.UNIX)), since);
    }

    private static final void insertEvent(final PrintWriter pw, final SkriptEventInfo<?> info) {
        if (info.getDescription() == SkriptEventInfo.NO_DOC)
            return;
        if (info.getDescription() == null || info.getExamples() == null || info.getSince() == null) {
            Skript.warning(info.getName() + " (" + info.c.getSimpleName() + ") is missing information");
            return;
        }
        for (final SkriptEventInfo<?> i : Skript.getEvents()) {
            if (info.getId().equals(i.getId()) && info != i && i.getDescription() != null && i.getDescription() != SkriptEventInfo.NO_DOC) {
                Skript.warning("Duplicate event id '" + info.getId() + '\'');
                return;
            }
        }
        final String desc = validateHTML(StringUtils.join(info.getDescription(), "<br/>"), "events");
        final String since = validateHTML(info.getSince(), "events");
        if (desc == null || since == null) {
            Skript.warning("description or 'since' of " + info.getName() + " (" + info.c.getSimpleName() + ") is invalid");
            return;
        }
        final String patterns = cleanPatterns(info.getName().startsWith("On ") ? "[on] " + StringUtils.join(info.patterns, LineSeparators.UNIX + "[on] ") : StringUtils.join(info.patterns, LineSeparators.UNIX));
        insertOnDuplicateKeyUpdate(pw, "syntax_elements", "id, name, type, patterns, description, examples, since", "patterns = '" + escapeSQL(patterns) + '\'', escapeHTML(info.getId()), escapeHTML(info.getName()), "event", patterns, desc, escapeHTML(StringUtils.join(info.getExamples(), LineSeparators.UNIX)), since);
    }

    private static final void insertClass(final PrintWriter pw, final ClassInfo<?> info) {
        if (Objects.equals(info.getDocName(), ClassInfo.NO_DOC))
            return;
        if (info.getDocName() == null || info.getDescription() == null || info.getUsage() == null || info.getExamples() == null || info.getSince() == null) {
            Skript.warning("Class " + info.getCodeName() + " is missing information");
            return;
        }
        final String desc = validateHTML(StringUtils.join(info.getDescription(), "<br/>"), "classes");
        final String usage = validateHTML(StringUtils.join(info.getUsage(), "<br/>"), "classes");
        final String since = info.getSince() == null ? "" : validateHTML(info.getSince(), "classes");
        if (desc == null || usage == null || since == null) {
            Skript.warning("Class " + info.getCodeName() + "'s description, usage or 'since' is invalid");
            return;
        }
        final String patterns = info.getUserInputPatterns() == null ? "" : convertRegex(StringUtils.join(info.getUserInputPatterns(), LineSeparators.UNIX));
        insertOnDuplicateKeyUpdate(pw, "classes", "id, name, description, patterns, `usage`, examples, since", "patterns = TRIM(LEADING '" + LineSeparators.UNIX + "' FROM CONCAT(patterns, '" + LineSeparators.UNIX + "', '" + escapeSQL(patterns) + "'))", escapeHTML(info.getCodeName()), escapeHTML(info.getDocName()), desc, patterns, usage, escapeHTML(StringUtils.join(info.getExamples(), LineSeparators.UNIX)), since);
    }

    private static final void insertFunction(final PrintWriter pw, final JavaFunction<?> func) {
        final StringBuilder params = new StringBuilder(4096);
        for (final Parameter<?> p : func.getParameters()) {
            if (params.length() != 0)
                params.append(", ");
            params.append(p);
        }
        final String desc = validateHTML(StringUtils.join(func.getDescription(), "<br/>"), "functions");
        final String since = validateHTML(func.getSince(), "functions");
        if (desc == null || since == null) {
            Skript.warning("Function " + func.getName() + "'s description or 'since' is invalid");
            return;
        }
        replaceInto(pw, "functions", "name, parameters, description, examples, since", escapeHTML(func.getName()), escapeHTML(params.toString()), desc, escapeHTML(StringUtils.join(func.getExamples(), LineSeparators.UNIX)), since);
    }

    private static final void insertOnDuplicateKeyUpdate(final PrintWriter pw, final String table, final String fields, final String update, final String... values) {
        for (int i = 0; i < values.length; i++)
            values[i] = escapeSQL(values[i]);
        pw.println("INSERT INTO " + table + " (" + fields + ") VALUES ('" + StringUtils.join(values, "','") + "') ON DUPLICATE KEY UPDATE " + update + ';');
    }

    private static final void replaceInto(final PrintWriter pw, final String table, final String fields, final String... values) {
        for (int i = 0; i < values.length; i++)
            values[i] = escapeSQL(values[i]);
        pw.println("REPLACE INTO " + table + " (" + fields + ") VALUES ('" + StringUtils.join(values, "','") + "');");
    }

    @SuppressWarnings("null")
    @Nullable
    private static final String validateHTML(@Nullable String html, final String baseURL) {
        if (html == null) {
            assert false;
            return null;
        }
        for (final Pattern p : validation) {
            if (MatcherCache.getMatcher(p, html).find())
                return null;
        }
        html = html.replaceAll("&(?!(amp|lt|gt|quot);)", "&amp;");
        final Matcher m = MatcherCache.getMatcher(PatternCache.get("<a href='(.*?)'>"), html);
        linkLoop:
        while (m.find()) {
            final String url = m.group(1);
            final String[] s = url.split("#");
            if (s.length == 1)
                continue;
            if (s[0].isEmpty())
                s[0] = "../" + baseURL + '/';
            if (s[0].startsWith("../") && s[0].endsWith("/")) {
                switch (s[0]) {
                    case "../classes/":
                        if (Classes.getClassInfoNoError(s[1]) != null)
                            continue;
                        break;
                    case "../events/":
                        for (final SkriptEventInfo<?> i : Skript.getEvents()) {
                            if (s[1].equals(i.getId()))
                                continue linkLoop;
                        }
                        break;
                    case "../functions/":
                        if (Functions.getFunction(s[1]) != null)
                            continue;
                        break;
                    default:
                        final int i = CollectionUtils.indexOf(urls, s[0].substring("../".length(), s[0].length() - 1));
                        if (i != -1) {
                            try {
                                Class.forName("ch.njol.skript." + urls[i] + '.' + s[1]);
                                continue;
                            } catch (final ClassNotFoundException e) {
                                if (Skript.testing() || Skript.debug())
                                    Skript.exception(e);
                            }
                        }
                        break;
                }
            }
            Skript.warning("invalid link '" + url + "' found in '" + html + '\'');
        }
        return html;
    }

    private static final String escapeSQL(final String s) {
        return s.replace("'", "\\'").replace("\"", "\\\"");
    }

    public static final String escapeHTML(@Nullable final String s) {
        if (s == null) {
            assert false;
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

}
