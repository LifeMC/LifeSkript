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

package ch.njol.skript.aliases;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.NodeValidator;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.localization.*;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import ch.njol.skript.util.Date;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
public final class Aliases {

    /**
     * Note to self: never use this, use {@link #getAlias_i(String)} instead.
     */
    private static final HashMap<String, ItemType> aliases_english = new HashMap<>(12585);
    private static final HashMap<String, ItemType> aliases_localised = new HashMap<>(localizedAliases() ? 1000 : 0);
    private static final HashMap<Integer, MaterialName> materialNames_english = new HashMap<>(Material.values().length);
    private static final HashMap<Integer, MaterialName> materialNames_localised = new HashMap<>(localizedAliases() ? Material.values().length : 0);
    // this is not an alias!
    private static final ItemType everything = new ItemType();
    private static final Message m_brackets_error = new Message("aliases.brackets error");
    private static final ArgsMessage m_invalid_brackets = new ArgsMessage("aliases.invalid brackets");
    private static final ArgsMessage m_empty_alias = new ArgsMessage("aliases.empty alias");
    private static final ArgsMessage m_unknown_variation = new ArgsMessage("aliases.unknown variation");
    private static final Message m_starting_with_number = new Message("aliases.starting with number");
    private static final Message m_missing_aliases = new Message("aliases.missing aliases");
    private static final Message m_empty_string = new Message("aliases.empty string");
    private static final ArgsMessage m_invalid_item_data = new ArgsMessage("aliases.invalid item data");
    private static final ArgsMessage m_invalid_id = new ArgsMessage("aliases.invalid id");
    private static final Message m_invalid_block_data = new Message("aliases.invalid block data");
    private static final ArgsMessage m_invalid_item_type = new ArgsMessage("aliases.invalid item type");
    private static final ArgsMessage m_out_of_data_range = new ArgsMessage("aliases.out of data range");
    private static final Message m_invalid_range = new Message("aliases.invalid range");
    private static final ArgsMessage m_invalid_section = new ArgsMessage("aliases.invalid section");
    private static final ArgsMessage m_section_not_found = new ArgsMessage("aliases.section not found");
    private static final ArgsMessage m_not_a_section = new ArgsMessage("aliases.not a section");
    private static final Message m_unexpected_non_variation_section = new Message("aliases.unexpected non-variation section");
    private static final Message m_unexpected_section = new Message("aliases.unexpected section");
    private static final ArgsMessage m_loaded_x_aliases_from = new ArgsMessage("aliases.loaded x aliases from");
    private static final ArgsMessage m_loaded_x_aliases = new ArgsMessage("aliases.loaded x aliases");
    @SuppressWarnings("null")
    private static final Pattern numberWordPattern = Pattern.compile("\\d+\\s+.+");
    private static final Matcher numberWordPatternMatcher = numberWordPattern.matcher("");
    private static final RegexMessage p_any = new RegexMessage("aliases.any", "", " (.+)", Pattern.CASE_INSENSITIVE);
    private static final Message m_any = new Message("aliases.any-skp");
    private static final RegexMessage p_every = new RegexMessage("aliases.every", "", " (.+)", Pattern.CASE_INSENSITIVE);
    private static final RegexMessage p_of_every = new RegexMessage("aliases.of every", "(\\d+) ", " (.+)", Pattern.CASE_INSENSITIVE);
    private static final RegexMessage p_of = new RegexMessage("aliases.of", "(\\d+) (?:", " )?(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_", Pattern.LITERAL);
    private static final Matcher UNDERSCORE_PATTERN_MATCHER = UNDERSCORE_PATTERN.matcher("");
    static String itemSingular = "item";
    static String itemPlural = "items";
    @Nullable
    static String itemGender;
    static String blockSingular = "block";
    static String blockPlural = "blocks";
    @Nullable
    static String blockGender;

    static {
        everything.setAll(true);
        everything.add(new ItemData());
    }

    private Aliases() {
        throw new UnsupportedOperationException();
    }

    private static final HashMap<String, ItemType> getAliases() {
        return Language.isUsingLocal() ? aliases_localised : aliases_english;
    }

    @Nullable
    private static final ItemType getAlias_i(final String s) {
        final ItemType t = ScriptLoader.getScriptAliases().get(s);
        if (t != null)
            return t;
        return getAliases().get(s);
    }

    private static final HashMap<Integer, MaterialName> getMaterialNames() {
        return Language.isUsingLocal() ? materialNames_localised : materialNames_english;
    }

    private static final int nextBracket(final String s, final char closingBracket, final char openingBracket, final int start) {
        assert s.charAt(start) == openingBracket;
        int n = 0;
        for (int i = start + 1; i < s.length(); i++) {
            if (s.charAt(i) == '\\') {
                i++;
            } else if (s.charAt(i) == closingBracket) {
                if (n == 0)
                    return i;
                n--;
            } else if (s.charAt(i) == openingBracket) {
                n++;
            }
        }
        Skript.error(m_invalid_brackets.toString(openingBracket + closingBracket));
        return -1;
    }

    /**
     * Concatenates parts of an alias's name. This currently 'lowercases' the first character of any part if there's no space in front of it. It also replaces double spaces with a
     * single one and trims the resulting string.
     *
     * @param parts
     */
    @SuppressWarnings("null")
    private static final String concatenate(final String... parts) {
        assert parts.length >= 2;
        final StringBuilder b = new StringBuilder(4096);
        for (final String part : parts) {
            if (part.isEmpty())
                continue;
            if (b.length() == 0) {
                b.append(part);
                continue;
            }
            final char c = part.charAt(0);
            if (Character.isUpperCase(c) && b.charAt(b.length() - 1) != ' ') {
                b.append(Character.toLowerCase(c)).append(part.substring(1));
            } else {
                b.append(part);
            }
        }
        return b.toString().replace("  ", " ").trim();
    }

    /**
     * @param name       Mixedcase string with no whitespace besides spaces and no double spaces.
     * @param value      The alias's value, used for {variations}
     * @param variations
     * @return A map containing all parsed aliases
     */
    static final LinkedHashMap<String, ItemType> getAliases(final String name, final ItemType value, final Variations variations) {
        final LinkedHashMap<String, ItemType> r = new LinkedHashMap<>(); // LinkedHashMap to preserve order for item names
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if ("[({".indexOf(c) != -1) {
                final int end = nextBracket(name, "])}".charAt("[({".indexOf(c)), c, i);
                if (end == -1)
                    return r;
                if (c == '[') {
                    r.putAll(getAliases(concatenate(name.substring(0, i), name.substring(i + 1, end), name.substring(end + 1)), value, variations));
                    r.putAll(getAliases(concatenate(name.substring(0, i), name.substring(end + 1)), value, variations));
                } else if (c == '(') {
                    int n = 0;
                    int last = i;
                    boolean hasParts = false;
                    for (int j = i + 1; j < end; j++) {
                        final char x = name.charAt(j);
                        if (x == '(') {
                            n++;
                        } else if (x == ')') {
                            n--;
                        } else if (x == '|') {
                            if (n > 0)
                                continue;
                            hasParts = true;
                            r.putAll(getAliases(concatenate(name.substring(0, i), name.substring(last + 1, j), name.substring(end + 1)), value, variations));
                            last = j;
                        }
                    }
                    if (!hasParts) {
                        Skript.error(m_brackets_error.toString());
                        return r;
                    }
                    r.putAll(getAliases(concatenate(name.substring(0, i), name.substring(last + 1, end), name.substring(end + 1)), value, variations));
                } else {
                    assert c == '{';
                    continue;
                }
                return r;
            }
        }

        // variations and <any>/<block>/etc. are replaced last because they replace genders

        int i = name.indexOf('{');
        if (i != -1) {
            final int end = name.indexOf('}', i + 1);
            if (end == -1) {// checked above
                assert false;
                return r;
            }
            final String var = name.substring(i + 1, end);
            if (variations.containsKey(var)) {
                boolean hasDefault = false;
                for (final Entry<String, ItemType> v : variations.get(var).entrySet()) {
                    final String n;
                    if ("{default}".equalsIgnoreCase(v.getKey())) {
                        hasDefault = true;
                        if (v.getValue() == null)
                            continue;
                        n = concatenate(name.substring(0, i), name.substring(end + 1));
                    } else {
                        final int g = v.getKey().lastIndexOf('@');
                        if (g == -1) {
                            n = concatenate(name.substring(0, i), v.getKey(), name.substring(end + 1));
                        } else {
                            final String n0 = concatenate(name.substring(0, i), v.getKey().substring(0, g), name.substring(end + 1));
                            final int c0 = n0.lastIndexOf('@');
                            n = c0 == -1 ? n0 : n0.substring(0, c0).trim() + v.getKey().substring(g);
                        }
                    }
                    final ItemType t = v.getValue().intersection(value);
                    if (t != null)
                        r.putAll(getAliases(n.intern(), t, variations));
                    else
                        Skript.warning(m_empty_alias.toString(n));
                }
                if (!hasDefault)
                    r.putAll(getAliases(concatenate(name.substring(0, i), name.substring(end + 1)), value, variations));
            } else {
                Skript.error(m_unknown_variation.toString(var));
            }
            return r;
        }

        i = name.indexOf('<');
        if (i != -1) {
            final int end = name.indexOf('>', i + 1);
            if (end != -1) {
                final String x = name.substring(i + 1, end);
                if ("any".equalsIgnoreCase(x)) {
                    String s = name.substring(0, i) + m_any + name.substring(end + 1);
                    final int g = s.lastIndexOf('@');
                    if (g != -1)
                        s = s.substring(0, g + 1) + '-';
                    r.putAll(getAliases(s, value, variations));
                    return r;
                }
                final String[][] os = {{"item", itemSingular, itemPlural, itemGender}, {"block", blockSingular, blockPlural, blockGender}, {"item/block", itemSingular, itemPlural, itemGender, blockSingular, blockPlural, blockGender}, {"block/item", blockSingular, blockPlural, blockGender, itemSingular, itemPlural, itemGender},
                };
                for (final String[] o : os) {
                    if (x.equalsIgnoreCase(o[0])) {
                        for (int j = 1; j < o.length; j += 3) {
                            String s = name.substring(0, i) + '¦' + o[j] + '¦' + o[j + 1] + '¦' + name.substring(end + 1);
                            if (o[j + 2] != null) {
                                final NonNullPair<String, Integer> p = Noun.stripGender(s, s);
                                s = p.getFirst() + '@' + o[j + 2];
                            }
                            r.put(s, value);
                        }
                        return r;
                    }
                }
            }
        }

        r.put(name, value);
        r.remove("");
        return r;
    }

    /**
     * Parses & adds new aliases
     *
     * @param name       mixedcase string
     * @param value
     * @param variations
     * @return amount of added aliases
     */
    @SuppressWarnings("null")
    static final int addAliases(final String name, final String value, final Variations variations) {
        final ItemType t = parseAlias(value);
        if (t == null) {
            return 0;
        }
        final HashMap<String, ItemType> aliases = getAliases();
        final HashMap<String, ItemType> as = getAliases(name.replaceAll("\\s+", " "), t, variations);
        boolean printedStartingWithNumberError = false;
//		boolean printedSyntaxError = false;
        for (final Entry<String, ItemType> e : as.entrySet()) {
            final String s = e.getKey().trim().replaceAll("\\s+", " ");
            final NonNullPair<String, Integer> g = Noun.stripGender(s, e.getKey());
            final NonNullPair<String, String> p = Noun.getPlural(g.getFirst());
            final String lcs = p.getFirst().toLowerCase(Locale.ENGLISH);
            final String lcp = p.getSecond().toLowerCase(Locale.ENGLISH);
            if (numberWordPatternMatcher.reset(lcs).matches() || numberWordPatternMatcher.reset(lcp).matches()) {
                if (!printedStartingWithNumberError) {
                    Skript.error(m_starting_with_number.toString());
                    printedStartingWithNumberError = true;
                }
                continue;
            }
//			if (lc.contains(",") || lc.contains(" and ") || lc.contains(" or ")) {
//				if (!printedSyntaxError) {
//					Skript.error("aliases must not contain syntax elements (comma, 'and', 'or')");
//					printedSyntaxError = true;
//				}
//				continue;
//			}
            boolean b;
            final ItemType alias;
            if ((b = lcs.endsWith(itemSingular)) || lcp.endsWith(itemPlural)) {
                String m = b ? lcs.substring(0, lcs.length() - itemSingular.length()) : lcp.substring(0, lcp.length() - itemPlural.length());
                if (m.endsWith(" ") || m.endsWith("-"))
                    m = m.substring(0, m.length() - 1);
                final ItemType si = aliases.get(m);
                if (si != null)
                    si.setItem(e.getValue());
                alias = e.getValue();
            } else if ((b = lcs.endsWith(blockSingular)) || lcp.endsWith(blockPlural)) {
                String m = b ? lcs.substring(0, lcs.length() - blockSingular.length()) : lcp.substring(0, lcp.length() - blockPlural.length());
                if (m.endsWith(" ") || m.endsWith("-"))
                    m = m.substring(0, m.length() - 1);
                final ItemType si = aliases.get(m);
                if (si != null)
                    si.setBlock(e.getValue());
                alias = e.getValue();
            } else {
                final ItemType[] ib = new ItemType[2];
                final String[] ibs = {itemSingular, blockSingular};
                final String[] seps = {" ", "", "-"};
                for (int i = 0; i < ib.length; i++) {
                    for (final String sep : seps) {
                        ib[i] = aliases.get(lcs + sep + ibs[i]);
                        if (ib[i] != null)
                            break;
                    }
                }
                if (ib[0] == null && ib[1] == null) {
                    alias = e.getValue();
                } else {
                    alias = e.getValue().clone();
                    alias.setItem(ib[0]);
                    alias.setBlock(ib[1]);
                }
            }
            aliases.put(lcs, alias);
            aliases.put(lcp, alias);

            //if (logSpam()) // over 10k aliases in the default aliases.sk as of MC 1.5
            //	info("added alias " + s + " for " + e.getValue());

            final HashMap<Integer, MaterialName> materialNames = getMaterialNames();

            if (alias.getTypes().size() == 1) {
                final ItemData d = alias.getTypes().get(0);
                MaterialName n = materialNames.get(d.getId());
                if (d.dataMin == -1 && d.dataMax == -1) {
                    if (n != null) {
                        if (n.singular.equals(String.valueOf(d.getId())) && n.singular.equals(n.plural)) {
                            n.singular = p.getFirst();
                            n.plural = p.getSecond();
                        }
                    } else {
                        materialNames.put(d.getId(), new MaterialName(d.getId(), p.getFirst(), p.getSecond(), g.getSecond()));
                    }
                } else {
                    if (n == null)
                        materialNames.put(d.getId(), n = new MaterialName(d.getId(), String.valueOf(d.getId()), String.valueOf(d.getId()), g.getSecond()));
                    final NonNullPair<Short, Short> data = new NonNullPair<>(d.dataMin, d.dataMax);
                    n.names.put(data, p);
                }
            }
        }
        return as.size();
    }

    /**
     * Gets the custom name of a material, or the default if none is set.
     *
     * @param id
     * @param data
     * @return The material's name
     */
    public static final String getMaterialName(final int id, final short data, final boolean plural) {
        return getMaterialName(id, data, data, plural);
    }

    public static final String getDebugMaterialName(final int id, final short data, final boolean plural) {
        return getDebugMaterialName(id, data, data, plural);
    }

    @SuppressWarnings("null")
    public static final String getMaterialName(final int id, final short dataMin, final short dataMax, final boolean plural) {
        final MaterialName n = getMaterialNames().get(id);
        if (n == null) {
            return String.valueOf(id);
        }
        return n.toString(dataMin, dataMax, plural);
    }

    public static final String getDebugMaterialName(final int id, final short dataMin, final short dataMax, final boolean plural) {
        final MaterialName n = getMaterialNames().get(id);
        if (n == null) {
            return id + ":" + dataMin + (dataMax == dataMin ? "" : "-" + dataMax);
        }
        return n.getDebugName(dataMin, dataMax, plural);
    }

    /**
     * @return The gender of item or -1 if no name is found
     * @deprecated use {@link Aliases#getGender(int)} instead
     */
    @SuppressWarnings("unused")
    @Deprecated
    public static final int getGender(final int id, final short dataMin, final short dataMax) {
        return getGender(id); // Not sure why Njol added these unused arguments
    }

    /**
     * @return The gender of item or -1 if no name is found
     */
    public static final int getGender(final int id) {
        final MaterialName n = getMaterialNames().get(id);
        if (n != null)
            return n.gender;
        return -1;
    }

    /**
     * @return how many ids are missing an alias, including the 'any id' (-1)
     */
    @SuppressWarnings("null")
    static final int addMissingMaterialNames() {
        final HashMap<Integer, MaterialName> materialNames = getMaterialNames();
        int r = 0;
        final StringBuilder missing = new StringBuilder(m_missing_aliases + " ");
        for (final Material m : Material.values()) {
            if (materialNames.get(m.getId()) == null) {
                materialNames.put(m.getId(), new MaterialName(m.getId(), m.toString().toLowerCase(Locale.ENGLISH).replace('_', ' '), m.toString().toLowerCase(Locale.ENGLISH).replace('_', ' '), 0));
                missing.append(m.getId()).append(", ");
                r++;
            }
        }
        final MaterialName m = materialNames.get(-1);
        if (m == null) {
            materialNames.put(-1, new MaterialName(-1, Language.get("aliases.anything"), Language.get("aliases.anything"), 0));
            missing.append("<any>, ");
            r++;
        }
        if (r > 0 && Skript.testing() && Skript.debug())
            Skript.warning(missing.substring(0, missing.length() - 2));
        return r;
    }

    /**
     * Parses an ItemType to be used as an alias, i.e. it doesn't parse 'all'/'every' and the amount.
     *
     * @param s mixed case string
     * @return A new ItemType representing the given value
     */
    @Nullable
    public static final ItemType parseAlias(final String s) {
        if (s.isEmpty()) {
            Skript.error(m_empty_string.toString());
            return null;
        }
        if ("*".equals(s))
            return everything;

        final ItemType t = new ItemType();

        final String[] types = s.split("\\s*,\\s*");
        for (final String type : types) {
            if (type == null || parseType(type, t, true) == null)
                return null;
        }

        return t;
    }

    /**
     * Parses an ItemType.
     * <p>
     * Prints errors.
     *
     * @param s
     * @return The parsed ItemType or null if the input is invalid.
     */
    @Nullable
    public static final ItemType parseItemType(String s) {
        if (s.isEmpty())
            return null;
        s = s.trim();

        final ItemType t = new ItemType();

        Matcher m;
        if ((m = p_of_every.matcher(s)).matches()) {
            t.setAmount(Utils.parseInt(m.group(1)));
            t.setAll(true);
            s = m.group(m.groupCount());
        } else if ((m = p_of.matcher(s)).matches()) {
            t.setAmount(Utils.parseInt(m.group(1)));
            s = m.group(m.groupCount());
        } else if ((m = p_every.matcher(s)).matches()) {
            t.setAll(true);
            s = m.group(m.groupCount());
        } else {
            final int l = s.length();
            s = Noun.stripIndefiniteArticle(s);
            if (s.length() != l) // had indefinite article
                t.setAmount(1);
        }

        final String lc = s.toLowerCase(Locale.ENGLISH);
        final String of = Language.getSpaced("enchantments.of").toLowerCase(Locale.ENGLISH);
        int c = -1;
        outer:
        while ((c = lc.indexOf(of, c + 1)) != -1) {
            final ItemType t2 = t.clone();
            final BlockingLogHandler log = SkriptLogger.startLogHandler(new BlockingLogHandler());
            try {
                if (parseType(s.substring(0, c), t2, false) == null)
                    continue;
            } finally {
                log.stop();
            }
            if (t2.numTypes() == 0)
                continue;
            final Map<Enchantment, Integer> enchantments = new HashMap<>();
            final String[] enchs = lc.substring(c + of.length()).split("\\s*(,|" + Pattern.quote(Language.get("and")) + ")\\s*");
            for (final String ench : enchs) {
                final EnchantmentType e = EnchantmentType.parse(ench);
                if (e == null)
                    continue outer;
                enchantments.put(e.getType(), e.getLevel());
            }
            t2.addEnchantments(enchantments);
            return t2;
        }

        if (parseType(s, t, false) == null)
            return null;

        if (t.numTypes() == 0)
            return null;

        return t;
    }

    /**
     * Prints errors.
     *
     * @param s       The string holding the type, can be either a number, or an alias, plus an optional data part. Case does not matter.
     * @param t       The ItemType to add the parsed ItemData(s) to (i.e. this ItemType will be modified)
     * @param isAlias Whatever this type is parsed for an alias.
     * @return The given item type or null if the input couldn't be parsed.
     */
    @Nullable
    private static final ItemType parseType(final String s, final ItemType t, final boolean isAlias) {
        int c = s.indexOf(':');
        if (c == -1)
            c = s.length();
        ItemData data = null;
        if (c != s.length()) {
            data = parseData(s.substring(c + 1));
            if (data == null) {
                Skript.error(m_invalid_item_data.toString(s.substring(c)));
                return null;
            }
        }
        final String type = s.substring(0, c);
        if (type.isEmpty()) {
            t.add(data == null ? new ItemData() : data);
            return t;
        }
        if (Skript.NUMBER_PATTERN.matcher(type).matches()) {
            ItemData d = new ItemData(Utils.parseInt(type));
            final Material m = d.getType();
            if (m == null) {
                Skript.error(m_invalid_id.toString(d.getId()));
                return null;
            }
            if (data != null) {
                if (d.getId() <= Skript.MAXBLOCKID && (data.dataMax > 15 || data.dataMin > 15)) {
                    Skript.error(m_invalid_block_data.toString());
                    return null;
                }
                d = d.intersection(data);
            }
            if (!isAlias && d != null && !SkriptConfig.disableUsingIdInsteadOfAliasWarnings.value()) {
                Skript.warning("Using an ID instead of an alias is discouraged! " + (d.toString().equals(type) ? "Please create an alias for '" + type + (type.equals(s) ? "" : " or '" + s + '\'') + "' (" + d.getType().name() + ") in aliases-english.sk or the script's aliases section and use that instead." : "Please replace '" + s + "' with '" + d.toString(true, false) + '\''));
            }
            t.add(d);
            //if (Skript.fieldForName(Material.class, m.name()).isAnnotationPresent(Deprecated.class))
            //Skript.warning("Material \"" + m.name() + "\" is deprecated"); // FIXME implement this
            return t;
        }
        final ItemType i;
        if ((i = getAlias(type)) != null) {
            for (ItemData d : i) {
                if (data != null) {
                    if (d.getId() <= Skript.MAXBLOCKID && (data.dataMax > 15 || data.dataMin > 15)) {
                        Skript.error(m_invalid_block_data.toString());
                        return null;
                    }
                    d = d.intersection(data);
                } else {
                    d = d.clone();
                }
                t.add(d);
            }
            if (data == null) {
                if (i.getItem() != i)
                    t.setItem(i.getItem().clone());
                if (i.getBlock() != i)
                    t.setBlock(i.getBlock().clone());
            }
            return t;
        }
        if (isAlias)
            Skript.error(m_invalid_item_type.toString(s));
        return null;
    }

    /**
     * Gets an alias from the aliases defined in the config.
     *
     * @param s The alias to get, case does not matter
     * @return A copy of the ItemType represented by the given alias or null if no such alias exists.
     */
    @Nullable
    private static final ItemType getAlias(final String s) {
        String lc = UNDERSCORE_PATTERN_MATCHER.reset(s.toLowerCase(Locale.ENGLISH)).replaceAll(Matcher.quoteReplacement(" "));
        final Matcher m = p_any.matcher(lc);
        if (m.matches()) {
            lc = m.group(m.groupCount());
        }
        ItemType i;
        if ((i = getAlias_i(lc)) != null)
            return i.clone();
        boolean b;
        if ((b = lc.endsWith(' ' + blockSingular)) || lc.endsWith(' ' + blockPlural)) {
            if ((i = getAlias_i(s.substring(0, s.length() - (b ? blockSingular : blockPlural).length() - 1))) != null) {
                i = i.clone();
                for (int j = 0; j < i.numTypes(); j++) {
                    final ItemData d = i.getTypes().get(j);
                    if (d.getId() > Skript.MAXBLOCKID) {
                        i.remove(d);
                        j--;
                    }
                }
                if (i.getTypes().isEmpty())
                    return null;
                return i;
            }
        } else if ((b = lc.endsWith(' ' + itemSingular)) || lc.endsWith(' ' + itemPlural)) {
            if ((i = getAlias_i(s.substring(0, s.length() - (b ? itemSingular : itemPlural).length() - 1))) != null) {
                for (int j = 0; j < i.numTypes(); j++) {
                    final ItemData d = i.getTypes().get(j);
                    if (d.getId() != -1 && d.getId() <= Skript.MAXBLOCKID) {
                        i.remove(d);
                        j--;
                    }
                }
                if (i.getTypes().isEmpty())
                    return null;
                return i;
            }
        }
        return null;
    }

    /**
     * Gets the data part of an item data
     *
     * @param s Everything after ':'
     * @return ItemData with only the dataMin and dataMax set
     */
    @Nullable
    private static final ItemData parseData(final String s) {
        if (s.isEmpty())
            return new ItemData();
        if (!s.matches("\\d+(-\\d+)?"))
            return null;
        final ItemData t = new ItemData();
        int i = s.indexOf('-');
        if (i == -1)
            i = s.length();
        try {
            t.dataMin = Short.parseShort(s.substring(0, i));
            t.dataMax = i == s.length() ? t.dataMin : Short.parseShort(s.substring(i + 1));
        } catch (final NumberFormatException e) { // overflow
            Skript.error(m_out_of_data_range.toString(Short.MAX_VALUE));
            return null;
        }
        if (t.dataMin > t.dataMax) {
            Skript.error(m_invalid_range.toString());
            return null;
        }
        return t;
    }

    public static final void clear() {
        aliases_english.clear();
        aliases_localised.clear();
        materialNames_english.clear();
        materialNames_localised.clear();
    }

    @SuppressWarnings({"null", "resource"})
    public static final void load() {
//        final boolean flag = Skript.logHigh();

//        Date start = null;

//        if (flag)
//            start = new Date();

        final boolean wasLocal = Language.isUsingLocal();
        try {
            for (int l = 0; l < 2; l++) {
                Language.setUseLocal(l == 1);
                if (l == 1 && !Language.isUsingLocal())
                    break;

                final String dataFolder = Skript.getInstance().getDataFolder().getPath();
                final String aliasesFileName = "aliases-" + Language.getName() + ".sk";

                final Config aliasConfig;

                try (final JarFile jar = new JarFile(Skript.getInstance().getFile(), false)) {
                    File file = new File(Paths.get(dataFolder, aliasesFileName).toString());
                    @Nullable InputStream stream = null;
                    if (!file.exists()) { // If it's not exists in the data folder (plugins/Skript)
                        final JarEntry entry = jar.getJarEntry(aliasesFileName);
                        if (entry != null) {
                            stream = jar.getInputStream(entry);
                            file = null;
                        }
                        if (file != null && !file.exists()) // If it's also not exists on the JAR file
                            Skript.error("Could not find the " + Language.getName() + " aliases file " + file.getName());
                    }
                    assert file != null || stream != null;
                    if (file != null)
                        try (final FileInputStream is = new FileInputStream(file)) {
                            aliasConfig = new Config(is, file, false, true, "=");
                        }
                    else {
                        try (final BufferedInputStream buf = new BufferedInputStream(stream)) {
                            aliasConfig = new Config(buf, Skript.getInstance().getFile().getName() + '/' + aliasesFileName, false, true, "=");
                        }
                    }
                } catch (final IOException e) {
                    Skript.error("Could not load the " + Language.getName() + " aliases config: " + e.getLocalizedMessage());
                    return;
                }

                final ArrayList<String> aliasNodes = new ArrayList<>();

                aliasConfig.validate((NodeValidator) new SectionValidator().addEntry("aliases", s -> {
                    for (final String n : s.split(","))
                        aliasNodes.add(n.trim());
                }, false).addEntry("item", s -> {
                    final NonNullPair<String, Integer> g = Noun.stripGender(s, "item");
                    itemGender = Noun.getGenderID(g.getSecond());
                    final NonNullPair<String, String> p = Noun.getPlural(g.getFirst());
                    itemSingular = p.getFirst().toLowerCase(Locale.ENGLISH);
                    itemPlural = p.getSecond().toLowerCase(Locale.ENGLISH);
                }, false).addEntry("block", s -> {
                    final NonNullPair<String, Integer> g = Noun.stripGender(s, "block");
                    blockGender = Noun.getGenderID(g.getSecond());
                    final NonNullPair<String, String> p = Noun.getPlural(g.getFirst());
                    blockSingular = p.getFirst().toLowerCase(Locale.ENGLISH);
                    blockPlural = p.getSecond().toLowerCase(Locale.ENGLISH);
                }, false).setAllowUndefinedSections(true));

                for (final Node node : aliasConfig.getMainNode()) {
                    if (node instanceof SectionNode && !aliasNodes.contains(node.getKey())) {
                        Skript.error(m_invalid_section.toString(node.getKey()));
                    }
                }

                final Variations variations = new Variations();
                int num = 0;
                for (final String an : aliasNodes) {
                    final Node node = aliasConfig.getMainNode().get(an);
                    SkriptLogger.setNode(node);
                    if (node == null) {
                        Skript.error(m_section_not_found.toString(an));
                        continue;
                    }
                    if (!(node instanceof SectionNode)) {
                        Skript.error(m_not_a_section.toString(an));
                        continue;
                    }
                    int i = 0;
                    for (final Node n : (SectionNode) node) {
                        if (n instanceof EntryNode) {
                            i += addAliases(n.getKey(), ((EntryNode) n).getValue(), variations);
                        } else if (n instanceof SectionNode) {
                            final String key = n.getKey();
                            if (key == null) {
                                assert false;
                                continue;
                            }
                            if (!(key.startsWith("{") && key.endsWith("}"))) {
                                Skript.error(m_unexpected_non_variation_section.toString());
                                continue;
                            }
                            final HashMap<String, ItemType> vs = new HashMap<>();
                            for (final Node a : (SectionNode) n) {
                                if (a instanceof SectionNode) {
                                    Skript.error(m_unexpected_section.toString());
                                    continue;
                                }
                                if (!(a instanceof EntryNode)) {
                                    continue;
                                }
                                final boolean noDefault = ((EntryNode) a).getValue().isEmpty() && "{default}".equalsIgnoreCase(a.getKey());
                                final ItemType t = noDefault ? null : parseAlias(((EntryNode) a).getValue());
                                if (t != null || noDefault)
                                    vs.put(Noun.normalizePluralMarkers(a.getKey()), t);
                            }
                            variations.put(key.substring(1, key.length() - 1), vs);
                        }
                    }
                    if (Skript.logVeryHigh())
                        Skript.info(m_loaded_x_aliases_from.toString(i, node.getKey()));
                    num += i;
                }
                SkriptLogger.setNode(null);
                addMissingMaterialNames();

                if (Skript.logNormal())
                    Skript.info(m_loaded_x_aliases.toString(num));
                if (Skript.logHigh())
                    Skript.info("Loaded " + aliases_localised.size() + " localized aliases");

//			if (!SkriptConfig.keepConfigsLoaded.value())
//				aliasConfig = null;

            }
        } finally {
            Language.setUseLocal(wasLocal);
        }

//        if (flag)
//            Skript.info("Loaded aliases in " + start.difference(new Date()));
    }

    public static final boolean localizedAliases() {
        final boolean isLocalized = !"english".equals(Language.getName());

        if (isLocalized)
            Skript.debug("Using localized aliases");

        return isLocalized;
    }

    static final class Variations extends HashMap<String, HashMap<String, ItemType>> {
        private static final long serialVersionUID = -139481665727386819L;
    }

}
