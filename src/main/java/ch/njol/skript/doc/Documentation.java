/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

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
import ch.njol.skript.util.Utils;
import ch.njol.util.Callback;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.IteratorIterable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * TODO list special expressions for events and event values
 * TODO compare doc in code with changed one of the webserver and warn about differences?
 * 
 * @author Peter Güttinger
 */
@SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
public class Documentation {
	
	public final static void generate() {
		if (!generate)
			return;
		try {
			final PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(Skript.getInstance().getDataFolder(), "doc.sql")), "UTF-8"));
			asSql(pw);
			pw.flush();
			pw.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public final static boolean generate = Skript.testing() && new File(Skript.getInstance().getDataFolder(), "generate-doc").exists(); // don't generate the documentation on normal servers
	
	private final static void asSql(final PrintWriter pw) {
		pw.println("-- syntax elements");
//		pw.println("DROP TABLE IF EXISTS syntax_elements;");
		pw.println("CREATE TABLE IF NOT EXISTS syntax_elements (" +
				"id VARCHAR(20) NOT NULL PRIMARY KEY," +
				"name VARCHAR(100) NOT NULL," +
				"type ENUM('condition','effect','expression','event') NOT NULL," +
				"patterns VARCHAR(2000) NOT NULL," +
				"description VARCHAR(2000) NOT NULL," +
				"examples VARCHAR(2000) NOT NULL," +
				"since VARCHAR(100) NOT NULL" +
				");");
		pw.println("UPDATE syntax_elements SET patterns='';");
		pw.println();
		pw.println("-- expressions");
		for (final ExpressionInfo<?, ?> e : new IteratorIterable<ExpressionInfo<?, ?>>(Skript.getExpressions())) {
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
		pw.println("CREATE TABLE IF NOT EXISTS classes (" +
				"id VARCHAR(20) NOT NULL PRIMARY KEY," +
				"name VARCHAR(100) NOT NULL," +
				"description VARCHAR(2000) NOT NULL," +
				"patterns VARCHAR(2000) NOT NULL," +
				"`usage` VARCHAR(2000) NOT NULL," +
				"examples VARCHAR(2000) NOT NULL," +
				"since VARCHAR(100) NOT NULL" +
				");");
		pw.println("UPDATE classes SET patterns='';");
		pw.println();
		for (final ClassInfo<?> info : Classes.getClassInfos()) {
			assert info != null;
			insertClass(pw, info);
		}
		
		pw.println();
		pw.println();
		pw.println("-- functions");
		pw.println("CREATE TABLE IF NOT EXISTS functions (" +
				"name VARCHAR(100) NOT NULL," +
				"parameters VARCHAR(2000) NOT NULL," +
				"description VARCHAR(2000) NOT NULL," +
				"examples VARCHAR(2000) NOT NULL," +
				"since VARCHAR(100) NOT NULL" +
				");");
		for (final JavaFunction<?> func : Functions.getJavaFunctions()) {
			assert func != null;
			insertFunction(pw, func);
		}
	}
	
	private final static String convertRegex(final String regex) {
		if (StringUtils.containsAny(regex, ".[]\\*+"))
			Skript.error("Regex '" + regex + "' contains unconverted Regex syntax");
		return escapeHTML("" + regex
				.replaceAll("\\((.+?)\\)\\?", "[$1]")
				.replaceAll("(.)\\?", "[$1]"));
	}
	
	private final static String cleanPatterns(final String patterns) {
		final String s = StringUtils.replaceAll("" +
				escapeHTML(patterns) // escape HTML
				.replaceAll("(?<=[\\(\\|])[-0-9]+?¦", "") // remove marks
				.replace("()", "") // remove empty mark setting groups (mark¦)
				.replaceAll("\\(([^|]+?)\\|\\)", "[$1]") // replace (mark¦x|) groups with [x]
				.replaceAll("\\(\\|([^|]+?)\\)", "[$1]") // dito
				.replaceAll("\\((.+?)\\|\\)", "[($1)]") // replace (a|b|) with [(a|b)]
				.replaceAll("\\(\\|(.+?)\\)", "[($1)]") // dito
		, "(?<!\\\\)%(.+?)(?<!\\\\)%", new Callback<String, Matcher>() { // link & fancy types
			@Override
			public String run(final Matcher m) {
				String s = m.group(1);
				if (s.startsWith("-"))
					s = s.substring(1);
				String flag = "";
				if (s.startsWith("*") || s.startsWith("~")) {
					flag = s.substring(0, 1);
					s = s.substring(1);
				}
				final int a = s.indexOf("@");
				if (a != -1)
					s = s.substring(0, a);
				final StringBuilder b = new StringBuilder("%");
				b.append(flag);
				boolean first = true;
				for (final String c : s.split("/")) {
					assert c != null;
					if (!first)
						b.append("/");
					first = false;
					final NonNullPair<String, Boolean> p = Utils.getEnglishPlural(c);
					final ClassInfo<?> ci = Classes.getClassInfoNoError(p.getFirst());
					if (ci != null && ci.getDocName() != null && ci.getDocName() != ClassInfo.NO_DOC) {
						b.append("<a href='../classes/#").append(p.getFirst()).append("'>").append(ci.getName().toString(p.getSecond())).append("</a>");
					} else {
						b.append(c);
						if (ci != null && ci.getDocName() != ClassInfo.NO_DOC)
							Skript.warning("Used class " + p.getFirst() + " has no docName/name defined");
					}
				}
				return "" + b.append("%").toString();
			}
		});
		assert s != null : patterns;
		return s;
	}
	
	private final static void insertSyntaxElement(final PrintWriter pw, final SyntaxElementInfo<?> info, final String type) {
		if (info.c.getAnnotation(NoDoc.class) != null)
			return;
		if (info.c.getAnnotation(Name.class) == null || info.c.getAnnotation(Description.class) == null || info.c.getAnnotation(Examples.class) == null || info.c.getAnnotation(Since.class) == null) {
			Skript.warning("" + info.c.getSimpleName() + " is missing information");
			return;
		}
		final String desc = validateHTML(StringUtils.join(info.c.getAnnotation(Description.class).value(), "<br/>"), type + "s");
		final String since = validateHTML(info.c.getAnnotation(Since.class).value(), type + "s");
		if (desc == null || since == null) {
			Skript.warning("" + info.c.getSimpleName() + "'s description or 'since' is invalid");
			return;
		}
		final String patterns = cleanPatterns(StringUtils.join(info.patterns, "\n", 0, info.c == CondCompare.class ? 8 : info.patterns.length));
		insertOnDuplicateKeyUpdate(pw, "syntax_elements",
				"id, name, type, patterns, description, examples, since",
				"patterns = TRIM(LEADING '\n' FROM CONCAT(patterns, '\n', '" + escapeSQL(patterns) + "'))",
				escapeHTML("" + info.c.getSimpleName()),
				escapeHTML(info.c.getAnnotation(Name.class).value()),
				type,
				patterns,
				desc,
				escapeHTML(StringUtils.join(info.c.getAnnotation(Examples.class).value(), "\n")),
				since);
	}
	
	private final static void insertEvent(final PrintWriter pw, final SkriptEventInfo<?> info) {
		if (info.getDescription() == SkriptEventInfo.NO_DOC)
			return;
		if (info.getDescription() == null || info.getExamples() == null || info.getSince() == null) {
			Skript.warning("" + info.getName() + " (" + info.c.getSimpleName() + ") is missing information");
			return;
		}
		for (final SkriptEventInfo<?> i : Skript.getEvents()) {
			if (info.getId().equals(i.getId()) && info != i && i.getDescription() != null && i.getDescription() != SkriptEventInfo.NO_DOC) {
				Skript.warning("Duplicate event id '" + info.getId() + "'");
				return;
			}
		}
		final String desc = validateHTML(StringUtils.join(info.getDescription(), "<br/>"), "events");
		final String since = validateHTML(info.getSince(), "events");
		if (desc == null || since == null) {
			Skript.warning("description or 'since' of " + info.getName() + " (" + info.c.getSimpleName() + ") is invalid");
			return;
		}
		final String patterns = cleanPatterns(info.getName().startsWith("On ") ? "[on] " + StringUtils.join(info.patterns, "\n[on] ") : StringUtils.join(info.patterns, "\n"));
		insertOnDuplicateKeyUpdate(pw, "syntax_elements",
				"id, name, type, patterns, description, examples, since",
				"patterns = '" + escapeSQL(patterns) + "'",
				escapeHTML(info.getId()),
				escapeHTML(info.getName()),
				"event",
				patterns,
				desc,
				escapeHTML(StringUtils.join(info.getExamples(), "\n")),
				since);
	}
	
	private final static void insertClass(final PrintWriter pw, final ClassInfo<?> info) {
		if (info.getDocName() == ClassInfo.NO_DOC)
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
		final String patterns = info.getUserInputPatterns() == null ? "" : convertRegex(StringUtils.join(info.getUserInputPatterns(), "\n"));
		insertOnDuplicateKeyUpdate(pw, "classes",
				"id, name, description, patterns, `usage`, examples, since",
				"patterns = TRIM(LEADING '\n' FROM CONCAT(patterns, '\n', '" + escapeSQL(patterns) + "'))",
				escapeHTML(info.getCodeName()),
				escapeHTML(info.getDocName()),
				desc,
				patterns,
				usage,
				escapeHTML(StringUtils.join(info.getExamples(), "\n")),
				since);
	}
	
	private final static void insertFunction(final PrintWriter pw, final JavaFunction<?> func) {
		final StringBuilder params = new StringBuilder();
		for (final Parameter<?> p : func.getParameters()) {
			if (params.length() != 0)
				params.append(", ");
			params.append(p.toString());
		}
		final String desc = validateHTML(StringUtils.join(func.getDescription(), "<br/>"), "functions");
		final String since = validateHTML(func.getSince(), "functions");
		if (desc == null || since == null) {
			Skript.warning("Function " + func.getName() + "'s description or 'since' is invalid");
			return;
		}
		replaceInto(pw, "functions", "name, parameters, description, examples, since",
				escapeHTML(func.getName()),
				escapeHTML(params.toString()),
				desc,
				escapeHTML(StringUtils.join(func.getExamples(), "\n")),
				since);
	}
	
	private final static void insertOnDuplicateKeyUpdate(final PrintWriter pw, final String table, final String fields, final String update, final String... values) {
		for (int i = 0; i < values.length; i++)
			values[i] = escapeSQL("" + values[i]);
		pw.println("INSERT INTO " + table + " (" + fields + ") VALUES ('" + StringUtils.join(values, "','") + "') ON DUPLICATE KEY UPDATE " + update + ";");
	}
	
	private final static void replaceInto(final PrintWriter pw, final String table, final String fields, final String... values) {
		for (int i = 0; i < values.length; i++)
			values[i] = escapeSQL("" + values[i]);
		pw.println("REPLACE INTO " + table + " (" + fields + ") VALUES ('" + StringUtils.join(values, "','") + "');");
	}
	
	private static ArrayList<Pattern> validation = new ArrayList<Pattern>();
	static {
		validation.add(Pattern.compile("<" + "(?!a href='|/a>|br ?/|/?(i|b|u|code|pre|ul|li|em)>)"));
		validation.add(Pattern.compile("(?<!</a|'|br ?/|/?(i|b|u|code|pre|ul|li|em))" + ">"));
	}
	
	private final static String[] urls = {"expressions", "effects", "conditions"};
	
	@Nullable
	private final static String validateHTML(@Nullable String html, final String baseURL) {
		if (html == null) {
			assert false;
			return null;
		}
		for (final Pattern p : validation) {
			if (p.matcher(html).find())
				return null;
		}
		html = "" + html.replaceAll("&(?!(amp|lt|gt|quot);)", "&amp;");
		final Matcher m = Pattern.compile("<a href='(.*?)'>").matcher(html);
		linkLoop: while (m.find()) {
			final String url = m.group(1);
			final String[] s = url.split("#");
			if (s.length == 1)
				continue;
			if (s[0].isEmpty())
				s[0] = "../" + baseURL + "/";
			if (s[0].startsWith("../") && s[0].endsWith("/")) {
				if (s[0].equals("../classes/")) {
					if (Classes.getClassInfoNoError(s[1]) != null)
						continue;
				} else if (s[0].equals("../events/")) {
					for (final SkriptEventInfo<?> i : Skript.getEvents()) {
						if (s[1].equals(i.getId()))
							continue linkLoop;
					}
				} else if (s[0].equals("../functions/")) {
					if (Functions.getFunction("" + s[1]) != null)
						continue;
				} else {
					final int i = CollectionUtils.indexOf(urls, s[0].substring("../".length(), s[0].length() - 1));
					if (i != -1) {
						try {
							Class.forName("ch.njol.skript." + urls[i] + "." + s[1]);
							continue;
						} catch (final ClassNotFoundException e) {}
					}
				}
			}
			Skript.warning("invalid link '" + url + "' found in '" + html + "'");
		}
		return html;
	}
	
	private final static String escapeSQL(final String s) {
		return "" + s.replace("'", "\\'").replace("\"", "\\\"");
	}
	
	public final static String escapeHTML(final @Nullable String s) {
		if (s == null) {
			assert false;
			return "";
		}
		return "" + s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
	
}
