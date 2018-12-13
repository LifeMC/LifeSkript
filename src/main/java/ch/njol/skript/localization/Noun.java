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
 * Copyright 2011-2014 Peter Güttinger
 * 
 */

package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.localization.Language.LanguageListenerPriority;
import ch.njol.skript.util.ObjectUtils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public class Noun extends Message {
	
	public final static String GENDERS_SECTION = "genders.";
	
	// TODO remove NO_GENDER and add boolean/flag uncountable (e.g. Luft: 'die Luft', aber nicht 'eine Luft')
	public final static int PLURAL = -2, NO_GENDER = -3; // -1 is sometimes used as 'not set'
	public final static String PLURAL_TOKEN = "x", NO_GENDER_TOKEN = "-";
	
	@Nullable
	private String singular, plural;
	private int gender;
	
	public Noun(final String key) {
		super(key);
	}
	
	@SuppressWarnings("null")
	@Override
	protected void onValueChange() {
		String value = getValue();
		if (value == null) {
			plural = singular = key;
			gender = 0;
			return;
		}
		final int g = value.lastIndexOf('@');
		if (g != -1) {
			gender = getGender("" + value.substring(g + 1).trim(), key);
			value = "" + value.substring(0, g).trim();
		} else {
			gender = 0;
		}
		final NonNullPair<String, String> p = Noun.getPlural(value);
		singular = p.getFirst();
		plural = p.getSecond();
		if (gender == PLURAL && !ObjectUtils.equals(singular, plural))
			Skript.warning("Noun '" + key + "' is of gender 'plural', but has different singular and plural values.");
	}
	
	@Override
	public String toString() {
		validate();
		return "" + singular;
	}
	
	public String toString(final boolean plural) {
		validate();
		return plural ? "" + this.plural : "" + singular;
	}
	
	public String withIndefiniteArticle() {
		return toString(Language.F_INDEFINITE_ARTICLE);
	}
	
	public String getIndefiniteArticle() {
		validate();
		return gender == PLURAL || gender == NO_GENDER ? "" : "" + indefiniteArticles.get(gender);
	}
	
	public String withDefiniteArticle() {
		return toString(Language.F_DEFINITE_ARTICLE);
	}
	
	public String withDefiniteArticle(final boolean plural) {
		return toString(Language.F_DEFINITE_ARTICLE | (plural ? Language.F_PLURAL : 0));
	}
	
	public String getDefiniteArticle() {
		validate();
		return gender == PLURAL ? definitePluralArticle : gender == NO_GENDER ? "" : "" + definiteArticles.get(gender);
	}
	
	public int getGender() {
		validate();
		return gender;
	}
	
	/**
	 * Returns the article appropriate for the given gender & flags.
	 * 
	 * @param flags
	 * @return The article with a trailing space (as no article is possible in which case the empty string is returned)
	 */
	public static String getArticleWithSpace(final int gender, final int flags) {
		if (gender == PLURAL) {
			if ((flags & Language.F_DEFINITE_ARTICLE) != 0)
				return definitePluralArticle + " ";
		} else if (gender == NO_GENDER) {
			// nothing
		} else if ((flags & Language.F_DEFINITE_ARTICLE) != 0) {
			if (gender < 0 || gender >= definiteArticles.size()) {
				assert false : gender;
				return "";
			}
			return definiteArticles.get(gender) + " ";
		} else if ((flags & Language.F_INDEFINITE_ARTICLE) != 0) {
			if (gender < 0 || gender >= indefiniteArticles.size()) {
				assert false : gender;
				return "";
			}
			return indefiniteArticles.get(gender) + " ";
		}
		return "";
	}
	
	/**
	 * @param flags
	 * @return <tt>{@link #getArticleWithSpace(int, int) getArticleWithSpace}(getGender(), flags)</tt>
	 */
	public final String getArticleWithSpace(final int flags) {
		return getArticleWithSpace(getGender(), flags);
	}
	
	public String toString(final int flags) {
		validate();
		final StringBuilder b = new StringBuilder();
		b.append(getArticleWithSpace(flags));
		b.append((flags & Language.F_PLURAL) != 0 ? plural : singular);
		return "" + b.toString();
	}
	
	public String withAmount(final double amount) {
		validate();
		return Skript.toString(amount) + " " + (amount == 1 ? singular : plural);
	}
	
	public String withAmount(final double amount, final int flags) {
		validate();
		if (amount == 1) {
			if (gender == NO_GENDER)
				return toString((flags & Language.F_PLURAL) != 0);
			if (gender == PLURAL) {
				if ((flags & Language.F_DEFINITE_ARTICLE) != 0)
					return definitePluralArticle + " " + plural;
				return "" + plural;
			}
			if ((flags & Language.F_DEFINITE_ARTICLE) != 0)
				return (flags & Language.F_PLURAL) != 0 ? definitePluralArticle + " " + plural : definiteArticles.get(gender) + " " + singular;
			if ((flags & Language.F_INDEFINITE_ARTICLE) != 0)
				return indefiniteArticles.get(gender) + " " + singular;
			if ((flags & Language.F_PLURAL) != 0)
				return "" + plural;
		}
		return Skript.toString(amount) + " " + (amount == 1 ? singular : plural);
	}
	
	public String toString(final Adjective a, final int flags) {
		validate();
		final StringBuilder b = new StringBuilder();
		b.append(getArticleWithSpace(flags));
		b.append(a.toString(gender, flags));
		b.append(" ");
		b.append((flags & Language.F_PLURAL) != 0 ? plural : singular);
		return "" + b.toString();
	}
	
	public String toString(final Adjective[] adjectives, final int flags, final boolean and) {
		validate();
		if (adjectives.length == 0)
			return toString(flags);
		final StringBuilder b = new StringBuilder();
		b.append(getArticleWithSpace(flags));
		b.append(Adjective.toString(adjectives, getGender(), flags, and));
		b.append(" ");
		b.append(toString(flags));
		return "" + b.toString();
	}
	
	public String getSingular() {
		validate();
		return "" + singular;
	}
	
	public String getPlural() {
		validate();
		return "" + plural;
	}
	
	/**
	 * @param s String with ¦ plural markers but without a @gender
	 * @return (singular, plural)
	 */
	public static NonNullPair<String, String> getPlural(final String s) {
		final NonNullPair<String, String> r = new NonNullPair<String, String>("", "");
		int part = 3; // 1 = singular, 2 = plural, 3 = both
		int i = StringUtils.count(s, '¦');
		int last = 0, c = -1;
		while ((c = s.indexOf('¦', c + 1)) != -1) {
			final String x = s.substring(last, c);
			if ((part & 1) != 0)
				r.setFirst(r.getFirst() + x);
			if ((part & 2) != 0)
				r.setSecond(r.getSecond() + x);
			part = i >= 2 ? part % 3 + 1 : part == 2 ? 3 : 2;
			last = c + 1;
			i--;
		}
		final String x = s.substring(last);
		if ((part & 1) != 0)
			r.setFirst(r.getFirst() + x);
		if ((part & 2) != 0)
			r.setSecond(r.getSecond() + x);
		return r;
	}
	
	/**
	 * Normalizes plural markers, i.e. increases the total number of markers to a multiple of 3 without changing the string's meaning.
	 * <p>
	 * A @gender at the end of the string will be treated correctly.
	 * 
	 * @param s Some string
	 * @return The same string with normalized plural markers
	 */
	public static String normalizePluralMarkers(final String s) {
		final int c = StringUtils.count(s, '¦');
		if (c % 3 == 0)
			return s;
		if (c % 3 == 2) {
			final int g = s.lastIndexOf('@');
			if (g == -1)
				return s + "¦";
			return s.substring(0, g) + "¦" + s.substring(g);
		}
		final int x = s.lastIndexOf('¦');
		final int g = s.lastIndexOf('@');
		if (g == -1)
			return s.substring(0, x) + "¦" + s.substring(x) + "¦";
		return s.substring(0, x) + "¦" + s.substring(x, g) + "¦" + s.substring(g);
	}
	
	final static HashMap<String, Integer> genders = new HashMap<String, Integer>();
	
	/**
	 * @param gender Gender id as defined in [language].lang (i.e. without the leading @)
	 * @param key Key to use in error messages§
	 * @return The gender's id
	 */
	public static int getGender(final String gender, final String key) {
		if (gender.equalsIgnoreCase(PLURAL_TOKEN))
			return PLURAL;
		if (gender.equalsIgnoreCase(NO_GENDER_TOKEN))
			return NO_GENDER;
		final Integer i = genders.get(gender);
		if (i != null)
			return i;
		Skript.warning("Undefined gender '" + gender + "' at " + key);
		return 0;
	}
	
	@SuppressWarnings("null")
	@Nullable
	public static String getGenderID(final int gender) {
		if (gender == PLURAL)
			return PLURAL_TOKEN;
		if (gender == NO_GENDER)
			return NO_GENDER_TOKEN;
		return (Language.useLocal && Language.localized != null ? Language.localized : Language.english).get(GENDERS_SECTION + gender + ".id");
	}
	
	/**
	 * For use by {@link Aliases}
	 * 
	 * @param s String
	 * @param key Key to report in case of error
	 * @return (stripped string, gender or -1 if none)
	 */
	public static NonNullPair<String, Integer> stripGender(String s, final String key) {
		final int c = s.lastIndexOf('@');
		int g = -1;
		if (c != -1) {
			g = getGender("" + s.substring(c + 1).trim(), key);
			s = "" + s.substring(0, c).trim();
		}
		return new NonNullPair<String, Integer>(s, g);
	}
	
	final static List<String> indefiniteArticles = new ArrayList<String>(3);
	final static List<String> definiteArticles = new ArrayList<String>(3);
	static String definitePluralArticle = "";
	
	final static List<String> localIndefiniteArticles = new ArrayList<String>(3);
	final static List<String> localDefiniteArticles = new ArrayList<String>(3);
	static String localDefinitePluralArticle = "";
	
	static {
		Language.addListener(new LanguageChangeListener() {
			@SuppressWarnings({"null", "unused"})
			@Override
			public void onLanguageChange() {
				Map<String, String> lang = Language.useLocal ? Language.localized : Language.english;
				if (lang == null)
					lang = Language.english;
				genders.clear();
				indefiniteArticles.clear();
				definiteArticles.clear();
				for (int i = 0; i < 100; i++) {
					final String g = lang.get(GENDERS_SECTION + i + ".id");
					if (g == null)
						break;
					if (g.equalsIgnoreCase(PLURAL_TOKEN) || g.equalsIgnoreCase(NO_GENDER_TOKEN)) {
						Skript.error("gender #" + i + " uses a reserved character as ID, please use something different!");
						continue;
					}
					genders.put(g, i);
					final String ia = lang.get(GENDERS_SECTION + i + ".indefinite article");
					indefiniteArticles.add(ia == null ? "" : ia);
					final String da = lang.get(GENDERS_SECTION + i + ".definite article");
					definiteArticles.add(da == null ? "" : da);
				}
				if (genders.isEmpty()) {
					Skript.error("No genders defined in language file " + Language.getName() + ".lang!");
					indefiniteArticles.add("");
					definiteArticles.add("");
				}
				final String dpa = lang.get(GENDERS_SECTION + "plural.definite article");
				if (dpa == null)
					Skript.error("Missing entry '" + GENDERS_SECTION + "plural.definite article' in the " + Language.getName() + " language file!");
				definitePluralArticle = dpa == null ? "" : dpa;
				
				if (Language.useLocal || localIndefiniteArticles.isEmpty()) {
					localIndefiniteArticles.clear();
					localIndefiniteArticles.addAll(indefiniteArticles);
					localDefiniteArticles.clear();
					localDefiniteArticles.addAll(definiteArticles);
					localDefinitePluralArticle = definitePluralArticle;
				}
			}
		}, LanguageListenerPriority.EARLIEST);
	}
	
	public static String stripIndefiniteArticle(final String s) {
		for (final String a : indefiniteArticles) {
			if (StringUtils.startsWithIgnoreCase(s, a + " "))
				return "" + s.substring(a.length() + 1);
		}
		return s;
	}
	
	public static boolean isIndefiniteArticle(final String s) {
		return indefiniteArticles.contains(s.toLowerCase());
	}
	
	public static boolean isLocalIndefiniteArticle(final String s) {
		return localIndefiniteArticles.contains(s.toLowerCase());
	}
	
	public static boolean isDefiniteArticle(final String s) {
		return definiteArticles.contains(s.toLowerCase()) || definitePluralArticle.equalsIgnoreCase(s);
	}
	
	public static boolean isLocalDefiniteArticle(final String s) {
		return localDefiniteArticles.contains(s.toLowerCase()) || localDefinitePluralArticle.equalsIgnoreCase(s);
	}
	
	public static String toString(final String singular, final String plural, final int gender, final int flags) {
		//FIXME Is this should be getArticleWithSpace(gender, flags) ?
		return getArticleWithSpace(flags, gender) + ((flags & Language.F_PLURAL) != 0 ? plural : singular);
	}
	
}
