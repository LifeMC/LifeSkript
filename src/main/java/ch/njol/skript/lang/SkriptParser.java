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

package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.command.Argument;
import ch.njol.skript.command.Commands;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.expressions.ExprParse;
import ch.njol.skript.lang.function.ExprFunctionCall;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.FunctionReference;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Used for parsing my custom patterns.<br>
 * <br>
 * Note: All parse methods print one error at most xor any amount of warnings and lower level log messages. If the given string doesn't match any pattern then nothing is printed.
 * 
 * @author Peter Güttinger
 */
public final class SkriptParser {
	
	final String expr;
	
	public final static int PARSE_EXPRESSIONS = 1;
	public final static int PARSE_LITERALS = 2;
	public final static int ALL_FLAGS = PARSE_EXPRESSIONS | PARSE_LITERALS;
	
	private final int flags;
	public final ParseContext context;
	
	public SkriptParser(final String expr) {
		this(expr, ALL_FLAGS);
	}
	
	public SkriptParser(final String expr, final int flags) {
		this(expr, flags, ParseContext.DEFAULT);
	}
	
	/**
	 * Constructs a new SkriptParser object that can be used to parse the given expression.
	 * <p>
	 * A SkriptParser can be re-used indefinitely for the given expression, but to parse a new expression a new SkriptParser has to be created.
	 * 
	 * @param expr The expression to parse
	 * @param flags Some parse flags ({@link #PARSE_EXPRESSIONS}, {@link #PARSE_LITERALS})
	 * @param context The parse context
	 */
	public SkriptParser(final String expr, final int flags, final ParseContext context) {
		assert expr != null;
		assert (flags & ALL_FLAGS) != 0;
		this.expr = "" + expr.trim();
		this.flags = flags;
		this.context = context;
	}
	
	public SkriptParser(final SkriptParser other, final String expr) {
		this(expr, other.flags, other.context);
	}
	
	public final static String wildcard = "[^\"]*?(?:\"[^\"]*?\"[^\"]*?)*?";
	public final static String stringMatcher = "\"[^\"]*?(?:\"\"[^\"]*)*?\"";
	
	public final static class ParseResult {
		
		public final Expression<?>[] exprs;
		public final List<MatchResult> regexes = new ArrayList<MatchResult>(1);
		
		public final String expr;
		
		/**
		 * Defaults to 0. Any marks encountered in the pattern will be XORed with the existing value,
		 * in particular if only one mark is encountered this value will be set to that mark.
		 */
		public int mark;
		
		public ParseResult(final SkriptParser parser, final String pattern) {
			expr = parser.expr;
			exprs = new Expression<?>[countUnescaped(pattern, '%') / 2];
		}
		
	}
	
	private final static class MalformedPatternException extends RuntimeException {
		
		private static final long serialVersionUID = -5133477361763823946L;
		
		public MalformedPatternException(final String pattern, final String message) {
			this(pattern, message, null);
		}
		
		public MalformedPatternException(final String pattern, final String message, final @Nullable Throwable cause) {
			super(message + " [pattern: " + pattern + "]", cause);
		}
		
	}
	
	/**
	 * Parses a single literal, i.e. not lists of literals.
	 * <p>
	 * Prints errors.
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> Literal<? extends T> parseLiteral(String expr, final Class<T> c, final ParseContext context) {
		expr = "" + expr.trim();
		if (expr.isEmpty())
			return null;
		return new UnparsedLiteral(expr).getConvertedExpression(context, c);
	}
	
	/**
	 * Parses a string as one of the given syntax elements.
	 * <p>
	 * Can print an error.
	 */
	@Nullable
	public static <T extends SyntaxElement> T parse(String expr, final Iterator<? extends SyntaxElementInfo<T>> source, final @Nullable String defaultError) {
		expr = "" + expr.trim();
		if (expr.isEmpty()) {
			Skript.error(defaultError);
			return null;
		}
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final T e = new SkriptParser(expr).parse(source);
			if (e != null) {
				log.printLog();
				return e;
			}
			log.printError(defaultError);
			return null;
		} finally {
			log.stop();
		}
	}
	
	@Nullable
	public static <T extends SyntaxElement> T parseStatic(String expr, final Iterator<? extends SyntaxElementInfo<? extends T>> source, final @Nullable String defaultError) {
		expr = "" + expr.trim();
		if (expr.isEmpty()) {
			Skript.error(defaultError);
			return null;
		}
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		final T e;
		try {
			e = new SkriptParser(expr, PARSE_LITERALS).parse(source);
			if (e != null) {
				log.printLog();
				return e;
			}
			log.printError(defaultError);
			return null;
		} finally {
			log.stop();
		}
	}
	
	@Nullable
	private <T extends SyntaxElement> T parse(final Iterator<? extends SyntaxElementInfo<? extends T>> source) {
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			while (source.hasNext()) {
				final SyntaxElementInfo<? extends T> info = source.next();
				patternsLoop: for (int i = 0; i < info.patterns.length; i++) {
					log.clear();
					try {
						final String pattern = info.patterns[i];
						assert pattern != null;
						final ParseResult res = parse_i(pattern, 0, 0);
						if (res != null) {
							int x = -1;
							for (int j = 0; (x = nextUnescaped(pattern, '%', x + 1)) != -1; j++) {
								final int x2 = nextUnescaped(pattern, '%', x + 1);
								if (res.exprs[j] == null) {
									final String name = pattern.substring(x + 1, x2);
									if (!name.startsWith("-")) {
										final ExprInfo vi = getExprInfo(name);
										final DefaultExpression<?> expression = vi.classes[0].getDefaultExpression();
										if (expression == null)
											throw new SkriptAPIException("The class '" + vi.classes[0].getCodeName() + "' does not provide a default expression. Either allow null (with %-" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
										if (!(expression instanceof Literal) && (vi.flagMask & PARSE_EXPRESSIONS) == 0)
											throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' is not a literal. Either allow null (with %-*" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
										if (expression instanceof Literal && (vi.flagMask & PARSE_LITERALS) == 0)
											throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' is a literal. Either allow null (with %-~" + vi.classes[0].getCodeName() + "%) or make it mandatory [pattern: " + info.patterns[i] + "]");
										if (!vi.isPlural[0] && !expression.isSingle())
											throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' is not a single-element expression. Change your pattern to allow multiple elements or make the expression mandatory [pattern: " + info.patterns[i] + "]");
										if (vi.time != 0 && !expression.setTime(vi.time))
											throw new SkriptAPIException("The default expression of '" + vi.classes[0].getCodeName() + "' does not have distinct time states. [pattern: " + info.patterns[i] + "]");
										if (!expression.init())
											continue patternsLoop;
										res.exprs[j] = expression;
									}
								}
								x = x2;
							}
							final T t = info.c.newInstance();
							if (t.init(res.exprs, i, ScriptLoader.hasDelayBefore, res)) {
								log.printLog();
								return t;
							}
						}
					} catch (final InstantiationException e) {
						assert false;
					} catch (final IllegalAccessException e) {
						assert false;
					}
				}
			}
			log.printError();
			return null;
		} finally {
			log.stop();
		}
	}
	
	@SuppressWarnings("null")
	private final static Pattern varPattern = Pattern.compile("((the )?var(iable)? )?\\{([^{}]|%\\{|}%)+}", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Prints errors
	 */
	@Nullable
	private static <T> Variable<T> parseVariable(final String expr, final Class<? extends T>[] returnTypes) {
		if (varPattern.matcher(expr).matches())
			return Variable.newInstance("" + expr.substring(expr.indexOf('{') + 1, expr.lastIndexOf('}')), returnTypes);
		return null;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Nullable
	private <T> Expression<? extends T> parseSingleExpr(final boolean allowUnparsedLiteral, @Nullable final LogEntry error, final Class<? extends T>... types) {
		assert types.length > 0;
		assert types.length == 1 || !CollectionUtils.contains(types, Object.class);
		if (expr.isEmpty())
			return null;
		if (context != ParseContext.COMMAND && expr.startsWith("(") && expr.endsWith(")") && next(expr, 0, context) == expr.length())
			return new SkriptParser(this, "" + expr.substring(1, expr.length() - 1)).parseSingleExpr(allowUnparsedLiteral, error, types);
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			if (context == ParseContext.DEFAULT || context == ParseContext.EVENT) {
				final Variable<? extends T> var = parseVariable(expr, types);
				if (var != null) {
					if ((flags & PARSE_EXPRESSIONS) == 0) {
						Skript.error("Variables cannot be used here.");
						log.printError();
						return null;
					}
					log.printLog();
					return var;
				} else if (log.hasError()) {
					log.printError();
					return null;
				}
				final FunctionReference<T> fr = parseFunction(types);
				if (fr != null) {
					log.printLog();
					return new ExprFunctionCall(fr);
				} else if (log.hasError()) {
					log.printError();
					return null;
				}
			}
			log.clear();
			if ((flags & PARSE_EXPRESSIONS) != 0) {
				final Expression<?> e;
				if (expr.startsWith("\"") && expr.endsWith("\"") && expr.length() != 1 && (types[0] == Object.class || CollectionUtils.contains(types, String.class))) {
					e = VariableString.newInstance("" + expr.substring(1, expr.length() - 1));
				} else {
					e = SkriptParser.<Expression<?>>parse(expr, (Iterator) Skript.getExpressions(types), null);
				}
				if (e != null) {
					for (final Class<? extends T> t : types) {
						if (t.isAssignableFrom(e.getReturnType())) {
							log.printLog();
							return (Expression<? extends T>) e;
						}
					}
					if (e instanceof Variable) {
						final Class<T>[] objTypes = (Class<T>[]) types;
						return e.getConvertedExpression(objTypes);
					} else {
						for (final Class<? extends T> t : types) {
							final Expression<? extends T> r = e.getConvertedExpression(t);
							if (r != null) {
								log.printLog();
								return r;
							}
						}
					}
					log.printError(e.toString(null, false) + " " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
					return null;
				}
				log.clear();
			}
			if ((flags & PARSE_LITERALS) == 0) {
				log.printError();
				return null;
			}
			if (types[0] == Object.class) {
				if (!allowUnparsedLiteral) {
					log.printError();
					return null;
				}
				log.clear();
				log.printLog();
				final LogEntry e = log.getError();
				return (Literal<? extends T>) new UnparsedLiteral(expr, e != null && (error == null || e.quality > error.quality) ? e : error);
			}
			for (final Class<? extends T> c : types) {
				log.clear();
				assert c != null;
				final T t = Classes.parse(expr, c, context);
				if (t != null) {
					log.printLog();
					return new SimpleLiteral<T>(t, false);
				}
			}
			log.printError();
			return null;
		} finally {
			log.stop();
		}
	}
	
	/**
	 * Matches ',', 'and', 'or', etc. as well as surrounding whitespace.
	 * <p>
	 * group 1 is null for ',', otherwise it's one of and/or/nor (not necessarily lowercase).
	 */
	@SuppressWarnings("null")
	public final static Pattern listSplitPattern = Pattern.compile("\\s*,?\\s+(and|n?or)\\s+|\\s*,\\s*", Pattern.CASE_INSENSITIVE);
	
	private final static String MULTIPLE_AND_OR = "List has multiple 'and' or 'or', will default to 'and'. Use brackets if you want to define multiple lists.";
	private final static String MISSING_AND_OR = "List is missing 'and' or 'or', defaulting to 'and'";
	
	private boolean suppressMissingAndOrWarnings;
	
	private SkriptParser suppressMissingAndOrWarnings() {
		suppressMissingAndOrWarnings = true;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> Expression<? extends T> parseExpression(final Class<? extends T>... types) {
		if (expr.length() == 0)
			return null;
		
		assert types != null && types.length > 0;
		assert types.length == 1 || !CollectionUtils.contains(types, Object.class);
		
		final boolean isObject = types.length == 1 && types[0] == Object.class;
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			//Mirre
			if (isObject && (flags & PARSE_LITERALS) != 0) {
				// Hack as items use '..., ... and ...' for enchantments. Numbers and times are parsed beforehand as they use the same (deprecated) id[:data] syntax.
				final SkriptParser p = new SkriptParser(expr, PARSE_LITERALS, context);
				p.suppressMissingAndOrWarnings = suppressMissingAndOrWarnings; // If we suppress warnings here, we suppress them in parser what we created too
				for (final Class<?> c : new Class[] {Number.class, Time.class, ItemType.class, ItemStack.class}) {
					final Expression<?> e = p.parseExpression(c);
					if (e != null) {
						log.printLog();
						return (Expression<? extends T>) e;
					}
					log.clear();
				}
			}
			//Mirre
			final Expression<? extends T> r = parseSingleExpr(false, null, types);
			if (r != null) {
				log.printLog();
				return r;
			}
			log.clear();
			
			final List<int[]> pieces = new ArrayList<int[]>();
			{
				final Matcher m = listSplitPattern.matcher(expr);
				int i = 0, j = 0;
				for (; i >= 0 && i <= expr.length(); i = next(expr, i, context)) {
					if (i == expr.length() || m.region(i, expr.length()).lookingAt()) {
						pieces.add(new int[] {j, i});
						if (i == expr.length())
							break;
						j = i = m.end();
					}
				}
				if (i != expr.length()) {
					assert i == -1 && context != ParseContext.COMMAND : i + "; " + expr;
					log.printError("Invalid brackets/variables/text in '" + expr + "'", ErrorQuality.NOT_AN_EXPRESSION);
					return null;
				}
			}
			
			if (pieces.size() == 1) { // not a list of expressions, and a single one has failed to parse above
				if (expr.startsWith("(") && expr.endsWith(")") && next(expr, 0, context) == expr.length()) {
					log.clear();
					log.printLog();
					return new SkriptParser(this, "" + expr.substring(1, expr.length() - 1)).parseExpression(types);
				}
				if (isObject && (flags & PARSE_LITERALS) != 0) { // single expression - can return an UnparsedLiteral now
					log.clear();
					log.printLog();
					return (Expression<? extends T>) new UnparsedLiteral(expr, log.getError());
				}
				// results in useless errors most of the time
//				log.printError("'" + expr + "' " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
				log.printError();
				return null;
			}
			
			Kleenean and = Kleenean.UNKNOWN;
			boolean isLiteralList = true;
			
			final List<Expression<? extends T>> ts = new ArrayList<Expression<? extends T>>();
			
			outer: for (int b = 0; b < pieces.size();) {
				for (int a = pieces.size() - b; a >= 1; a--) {
					if (b == 0 && a == pieces.size()) // i.e. the whole expression - already tried to parse above
						continue;
					final int x = pieces.get(b)[0], y = pieces.get(b + a - 1)[1];
					final String subExpr = "" + expr.substring(x, y).trim();
					assert subExpr.length() < expr.length() : subExpr;
					
					final Expression<? extends T> t;
					
					if (subExpr.startsWith("(") && subExpr.endsWith(")") && next(subExpr, 0, context) == subExpr.length())
						t = new SkriptParser(this, subExpr).parseExpression(types); // only parse as possible expression list if its surrounded by brackets
					else
						t = new SkriptParser(this, subExpr).parseSingleExpr(a == 1, log.getError(), types); // otherwise parse as a single expression only
					if (t != null) {
						isLiteralList &= t instanceof Literal;
						ts.add(t);
						if (b != 0) {
							final String d = expr.substring(pieces.get(b - 1)[1], x).trim();
							if (!",".equals(d)) {
								if (and.isUnknown()) {
									and = Kleenean.get(!"or".equalsIgnoreCase(d)); // nor is and
								} else {
									if (and != Kleenean.get(!"or".equalsIgnoreCase(d))) {
										Skript.warning(MULTIPLE_AND_OR + " List: " + expr);
										and = Kleenean.TRUE;
									}
								}
							}
						}
						b += a;
						continue outer;
					}
				}
				log.printError();
				return null;
			}
			
//			String lastExpr = expr;
//			int end = expr.length();
//			int expectedEnd = -1;
//			boolean last = false;
//			while (m.find() || (last = !last)) {
//				if (expectedEnd == -1) {
//					if (last)
//						break;
//					expectedEnd = m.start();
//				}
//				final int start = last ? 0 : m.end();
//				final Expression<? extends T> t;
//				if (context != ParseContext.COMMAND && (start < expr.length() && expr.charAt(start) == '(' || end - 1 > 0 && expr.charAt(end - 1) == ')')) {
//					if (start < expr.length() && expr.charAt(start) == '(' && end - 1 > 0 && expr.charAt(end - 1) == ')' && next(expr, start, context) == end)
//						t = new SkriptParser(lastExpr = "" + expr.substring(start + 1, end - 1), flags, context).parseExpression(types);
//					else
//						t = null;
//				} else {
//					t = new SkriptParser(lastExpr = "" + expr.substring(start, end), flags, context).parseSingleExpr(types);
//				}
//				if (t != null) {
//					isLiteralList &= t instanceof Literal;
//					if (!last && m.group(1) != null) {
//						if (and.isUnknown()) {
//							and = Kleenean.get(!m.group(1).equalsIgnoreCase("or")); // nor is and
//						} else {
//							if (and != Kleenean.get(!m.group(1).equalsIgnoreCase("or"))) {
//								Skript.warning(MULTIPLE_AND_OR);
//								and = Kleenean.TRUE;
//							}
//						}
//					}
//					ts.addFirst(t);
//					if (last)
//						break;
//					end = m.start();
//					m.region(0, end);
//				} else {
//					log.clear();
//					if (last)
//						end = -2; // fails the test below
//				}
//			}
//			if (end != expectedEnd) {
//				log.printError("'" + lastExpr + "' " + Language.get("is") + " " + notOfType(types), ErrorQuality.NOT_AN_EXPRESSION);
//				return null;
//			}
			
			log.printLog();
			
			if (ts.size() == 1)
				return ts.get(0);
			
			if (and.isUnknown() && !suppressMissingAndOrWarnings)
				Skript.warning(MISSING_AND_OR + ": " + expr);
			
			final Class<? extends T>[] exprRetTypes = new Class[ts.size()];
			for (int i = 0; i < ts.size(); i++)
				exprRetTypes[i] = ts.get(i).getReturnType();
			
			if (isLiteralList) {
				final Literal<T>[] ls = ts.toArray(new Literal[0]);
				assert ls != null;
				return new LiteralList<T>(ls, (Class<T>) Utils.getSuperType(exprRetTypes), !and.isFalse());
			} else {
				final Expression<T>[] es = ts.toArray(new Expression[0]);
				assert es != null;
				return new ExpressionList<T>(es, (Class<T>) Utils.getSuperType(exprRetTypes), !and.isFalse());
			}
		} finally {
			log.stop();
		}
	}
	
//	@SuppressWarnings("unchecked")
//	@Nullable
//	private final Expression<?> parseObjectExpression() {
//		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
//		try {
//			if ((flags & PARSE_EXPRESSIONS) != 0) {
//				final Expression<?> r = new SkriptParser(expr, PARSE_EXPRESSIONS, context).parseSingleExpr(Object.class);
//				if (r != null) {
//					log.printLog();
//					return r;
//				}
//				if ((flags & PARSE_LITERALS) == 0) {
//					log.printError();
//					return null;
//				}
//				log.clear();
//			}
//
//			if ((flags & PARSE_LITERALS) != 0) {
//				// Hack as items use '..., ... and ...' for enchantments. Numbers and times are parsed beforehand as they use the same (deprecated) id[:data] syntax.
//				final SkriptParser p = new SkriptParser(expr, PARSE_LITERALS, context);
//				for (final Class<?> c : new Class[] {Number.class, Time.class, ItemType.class, ItemStack.class}) {
//					final Expression<?> e = p.parseExpression(c);
//					if (e != null) {
//						log.printLog();
//						return e;
//					}
//					log.clear();
//				}
//			}
//		} finally {
//			// log has been printed already or is not used after this (except for the error)
//			log.clear();
//			log.printLog();
//		}
//
//		final Matcher m = listSplitPattern.matcher(expr);
//		if (!m.find())
//			return new UnparsedLiteral(expr, log.getError());
//		m.reset();
//
//		final List<Expression<?>> ts = new ArrayList<Expression<?>>();
//		Kleenean and = Kleenean.UNKNOWN;
//		boolean last = false;
//		boolean isLiteralList = true;
//		int start = 0;
//		while (!last) {
//			final Expression<?> t;
//			if (context != ParseContext.COMMAND && expr.charAt(start) == '(') {
//				final int end = next(expr, start, context);
//				if (end == -1)
//					return null;
//				last = end == expr.length();
//				if (!last) {
//					m.region(end, expr.length());
//					if (!m.lookingAt())
//						return null;
//				}
//				t = new SkriptParser("" + expr.substring(start + 1, end - 1), flags, context).parseObjectExpression();
//			} else {
//				m.region(start, expr.length());
//				last = !m.find();
//				final String sub = last ? expr.substring(start) : expr.substring(start, m.start());
//				t = new SkriptParser("" + sub, flags, context).parseSingleExpr(Object.class);
//			}
//			if (t == null)
//				return null;
//			if (!last)
//				start = m.end();
//
//			isLiteralList &= t instanceof Literal;
//			if (!last && m.group(1) != null) {
//				if (and.isUnknown()) {
//					and = Kleenean.get(!m.group(1).equalsIgnoreCase("or")); // nor is and
//				} else {
//					if (and != Kleenean.get(!m.group(1).equalsIgnoreCase("or"))) {
//						Skript.warning(MULTIPLE_AND_OR);
//						and = Kleenean.TRUE;
//					}
//				}
//			}
//			ts.add(t);
//		}
//		assert ts.size() >= 1 : expr;
//		if (ts.size() == 1)
//			return ts.get(0);
//		if (and.isUnknown())
//			Skript.warning(MISSING_AND_OR);
//
//		final Class<?>[] exprRetTypes = new Class[ts.size()];
//		int i = 0;
//		for (final Expression<?> t : ts)
//			exprRetTypes[i++] = t.getReturnType();
//
//		if (isLiteralList) {
//			final Literal<Object>[] ls = ts.toArray(new Literal[ts.size()]);
//			assert ls != null;
//			return new LiteralList<Object>(ls, (Class<Object>) Utils.getSuperType(exprRetTypes), !and.isFalse());
//		} else {
//			final Expression<Object>[] es = ts.toArray(new Expression[ts.size()]);
//			assert es != null;
//			return new ExpressionList<Object>(es, (Class<Object>) Utils.getSuperType(exprRetTypes), !and.isFalse());
//		}
//	}
	
	@SuppressWarnings("null")
	private final static Pattern functionCallPattern = Pattern.compile("(" + Functions.functionNamePattern + ")\\((.*)\\)");
	
	/**
	 * @param types The required return type or null if it is not used (e.g. when calling a void function)
	 * @return The parsed function, or null if the given expression is not a function call or is an invalid function call (check for an error to differentiate these two)
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> FunctionReference<T> parseFunction(final @Nullable Class<? extends T>... types) {
		if (context != ParseContext.DEFAULT && context != ParseContext.EVENT)
			return null;
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			final Matcher m = functionCallPattern.matcher(expr);
			if (!m.matches()) {
				log.printLog();
				return null;
			}
			if ((flags & PARSE_EXPRESSIONS) == 0) {
				Skript.error("Functions cannot be used here (or there is a problem with your arguments).");
				log.printError();
				return null;
			}
			final String functionName = "" + m.group(1);
			final Function<?> function = Functions.getFunction(functionName);
			if (function == null) {
				Skript.error("The function '" + functionName + "' does not exist");
				log.printError();
				return null;
			}
			final String args = m.group(2);
			final Expression<?>[] params;
			if (args.length() != 0) {
				final Expression<?> ps = new SkriptParser(args, flags | PARSE_LITERALS, context).suppressMissingAndOrWarnings().parseExpression(Object.class);
				if (ps == null) {
					log.printError();
					return null;
				}
				if (ps instanceof ExpressionList) {
					if (!ps.getAnd()) {
						Skript.error("Function arguments must be separated by commas and optionally an 'and', but not an 'or'." + " Put the 'or' into a second set of parentheses if you want to make it a single parameter, e.g. 'give(player, (sword or axe))'");
						log.printError();
						return null;
					}
					params = ((ExpressionList<?>) ps).getExpressions();
				} else {
					params = new Expression[] {ps};
				}
			} else {
				params = new Expression[0];
			}
//			final List<Expression<?>> params = new ArrayList<Expression<?>>();
//			if (args.length() != 0) {
//				final int p = 0;
//				int j = 0;
//				for (int i = 0; i != -1 && i <= args.length(); i = next(args, i, context)) {
//					if (i == args.length() || args.charAt(i) == ',') {
//						final Expression<?> e = new SkriptParser("" + args.substring(j, i).trim(), flags | PARSE_LITERALS, context).parseExpression(function.getParameter(p).getType().getC());
//						if (e == null) {
//							log.printError("Can't understand this expression: '" + args.substring(j, i) + "'", ErrorQuality.NOT_AN_EXPRESSION);
//							return null;
//						}
//						params.add(e);
//						j = i + 1;
//					}
//				}
//			}
			@SuppressWarnings("null")
			final FunctionReference<T> e = new FunctionReference<T>(functionName, SkriptLogger.getNode(), ScriptLoader.currentScript != null ? ScriptLoader.currentScript.getFile() : null, types, params);//.toArray(new Expression[params.size()]));
			if (!e.validateFunction(true)) {
				log.printError();
				return null;
			}
			log.printLog();
			return e;
		} finally {
			log.stop();
		}
	}
	
	/**
	 * Prints parse errors (i.e. must start a ParseLog before calling this method)
	 */
	public static boolean parseArguments(final String args, final ScriptCommand command, final ScriptCommandEvent event) {
		final SkriptParser parser = new SkriptParser(args, PARSE_LITERALS, ParseContext.COMMAND);
		final ParseResult res = parser.parse_i(command.getPattern(), 0, 0);
		if (res == null)
			return false;
		
		final List<Argument<?>> as = command.getArguments();
		assert as.size() == res.exprs.length;
		for (int i = 0; i < res.exprs.length; i++) {
			if (res.exprs[i] == null)
				as.get(i).setToDefault(event);
			else
				as.get(i).set(event, res.exprs[i].getArray(event));
		}
		return true;
	}
	
	/**
	 * Parses the text as the given pattern as {@link ParseContext#COMMAND}.
	 * <p>
	 * Prints parse errors (i.e. must start a ParseLog before calling this method)
	 */
	@Nullable
	public static ParseResult parse(final String text, final String pattern) {
		return new SkriptParser(text, PARSE_LITERALS, ParseContext.COMMAND).parse_i(pattern, 0, 0);
	}
	
	@Nullable
	public static NonNullPair<SkriptEventInfo<?>, SkriptEvent> parseEvent(final String event, final String defaultError) {
		final RetainingLogHandler log = SkriptLogger.startRetainingLog();
		try {
			final NonNullPair<SkriptEventInfo<?>, SkriptEvent> e = new SkriptParser(event, PARSE_LITERALS, ParseContext.EVENT).parseEvent();
			if (e != null) {
				log.printLog();
				return e;
			}
			log.printErrors(defaultError);
			return null;
		} finally {
			log.stop();
		}
	}
	
	@Nullable
	private NonNullPair<SkriptEventInfo<?>, SkriptEvent> parseEvent() {
		assert context == ParseContext.EVENT;
		assert flags == PARSE_LITERALS;
		final ParseLogHandler log = SkriptLogger.startParseLogHandler();
		try {
			for (final SkriptEventInfo<?> info : Skript.getEvents()) {
				for (int i = 0; i < info.patterns.length; i++) {
					log.clear();
					try {
						final String pattern = info.patterns[i];
						assert pattern != null;
						final ParseResult res = parse_i(pattern, 0, 0);
						if (res != null) {
							final SkriptEvent e = info.c.newInstance();
							final Literal<?>[] ls = Arrays.copyOf(res.exprs, res.exprs.length, Literal[].class);
							assert ls != null;
							if (!e.init(ls, i, res)) {
								log.printError();
								return null;
							}
							log.printLog();
							return new NonNullPair<SkriptEventInfo<?>, SkriptEvent>(info, e);
						}
					} catch (final InstantiationException e) {
						assert false;
					} catch (final IllegalAccessException e) {
						assert false;
					}
				}
			}
			log.printError(null);
			return null;
		} finally {
			log.stop();
		}
	}
	
	/**
	 * Finds the closing bracket of the group at <tt>start</tt> (i.e. <tt>start</tt> has to be <i>in</i> a group).
	 * 
	 * @param pattern
	 * @param closingBracket The bracket to look for, e.g. ')'
	 * @param openingBracket A bracket that opens another group, e.g. '('
	 * @param start This must not be the index of the opening bracket!
	 * @param isGroup Whether <tt>start</tt> is assumed to be in a group (will print an error if this is not the case, otherwise it returns <tt>pattern.length()</tt>)
	 * @return The index of the next bracket
	 * @throws MalformedPatternException If the group is not closed
	 */
	private static int nextBracket(final String pattern, final char closingBracket, final char openingBracket, final int start, final boolean isGroup) throws MalformedPatternException {
		int n = 0;
		for (int i = start; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '\\') {
				i++;
			} else if (pattern.charAt(i) == closingBracket) {
				if (n == 0) {
					if (!isGroup)
						throw new MalformedPatternException(pattern, "Unexpected closing bracket '" + closingBracket + "'");
					return i;
				}
				n--;
			} else if (pattern.charAt(i) == openingBracket) {
				n++;
			}
		}
		if (isGroup)
			throw new MalformedPatternException(pattern, "Missing closing bracket '" + closingBracket + "'");
		return -1;
	}
	
	/**
	 * Gets the next occurrence of a character in a string that is not escaped with a preceding backslash.
	 * 
	 * @param pattern
	 * @param c The character to search for
	 * @param from The index to start searching from
	 * @return The next index where the character occurs unescaped or -1 if it doesn't occur.
	 */
	private static int nextUnescaped(final String pattern, final char c, final int from) {
		for (int i = from; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '\\') {
				i++;
			} else if (pattern.charAt(i) == c) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Counts how often the given character occurs in the given string, ignoring any escaped occurrences of the character.
	 * 
	 * @param pattern
	 * @param c The character to search for
	 * @return The number of unescaped occurrences of the given character
	 */
	static int countUnescaped(final String pattern, final char c) {
		return countUnescaped(pattern, c, 0, pattern.length());
	}
	
	static int countUnescaped(final String pattern, final char c, final int start, final int end) {
		assert start >= 0 && start <= end && end <= pattern.length() : start + ", " + end + "; " + pattern.length();
		int r = 0;
		for (int i = start; i < end; i++) {
			final char x = pattern.charAt(i);
			if (x == '\\') {
				i++;
			} else if (x == c) {
				r++;
			}
		}
		return r;
	}
	
	/**
	 * Find the next unescaped (i.e. single) double quote in the string.
	 * 
	 * @param s
	 * @param from Index after the starting quote
	 * @return Index of the end quote
	 */
	private static int nextQuote(final String s, final int from) {
		for (int i = from; i < s.length(); i++) {
			if (s.charAt(i) == '"') {
				if (i == s.length() - 1 || s.charAt(i + 1) != '"')
					return i;
				i++;
			}
		}
		return -1;
	}
	
	/**
	 * @param cs
	 * @return "not an x" or "neither an x, a y nor a z"
	 */
	public static String notOfType(final Class<?>... cs) {
		if (cs.length == 1) {
			final Class<?> c = cs[0];
			assert c != null;
			return Language.get("not") + " " + Classes.getSuperClassInfo(c).getName().withIndefiniteArticle();
		} else {
			final StringBuilder b = new StringBuilder(Language.get("neither") + " ");
			for (int k = 0; k < cs.length; k++) {
				if (k != 0) {
					if (k != cs.length - 1)
						b.append(", ");
					else
						b.append(" ").append(Language.get("nor")).append(" ");
				}
				final Class<?> c = cs[k];
				assert c != null;
				b.append(Classes.getSuperClassInfo(c).getName().withIndefiniteArticle());
			}
			return "" + b.toString();
		}
	}
	
	public static String notOfType(final ClassInfo<?>... cs) {
		if (cs.length == 1) {
			return Language.get("not") + " " + cs[0].getName().withIndefiniteArticle();
		} else {
			final StringBuilder b = new StringBuilder(Language.get("neither") + " ");
			for (int k = 0; k < cs.length; k++) {
				if (k != 0) {
					if (k != cs.length - 1)
						b.append(", ");
					else
						b.append(" ").append(Language.get("nor")).append(" ");
				}
				b.append(cs[k].getName().withIndefiniteArticle());
			}
			return "" + b.toString();
		}
	}
	
	/**
	 * Returns the next character in the expression, skipping strings, variables and parentheses (unless <tt>context</tt> is {@link ParseContext#COMMAND}).
	 * 
	 * @param expr The expression
	 * @param i The last index
	 * @return The next index (can be expr.length()), or -1 if an invalid string, variable or bracket is found or if <tt>i >= expr.length()</tt>.
	 * @throws StringIndexOutOfBoundsException if <tt>i < 0</tt>
	 */
	public static int next(final String expr, final int i, final ParseContext context) {
		if (i >= expr.length())
			return -1;
		if (i < 0)
			throw new StringIndexOutOfBoundsException(i);
		if (context == ParseContext.COMMAND)
			return i + 1;
		final char c = expr.charAt(i);
		if (c == '"') {
			final int i2 = nextQuote(expr, i + 1);
			return i2 < 0 ? -1 : i2 + 1;
		} else if (c == '{') {
			final int i2 = VariableString.nextVariableBracket(expr, i + 1);
			return i2 < 0 ? -1 : i2 + 1;
		} else if (c == '(') {
			for (int j = i + 1; j >= 0 && j < expr.length(); j = next(expr, j, context)) {
				if (expr.charAt(j) == ')')
					return j + 1;
			}
			return -1;
		}
		return i + 1;
	}
	
	private static int getGroupLevel(final String pattern, final int j) {
		assert j >= 0 && j <= pattern.length() : j + "; " + pattern;
		int level = 0;
		for (int i = 0; i < j; i++) {
			final char c = pattern.charAt(i);
			if (c == '\\') {
				i++;
			} else if (c == '(') {
				level++;
			} else if (c == ')') {
				if (level == 0)
					throw new MalformedPatternException(pattern, "Unexpected closing bracket ')'");
				level--;
			}
		}
		return level;
	}
	
	/**
	 * Prints errors
	 * 
	 * @param pattern
	 * @param i Position in the input string
	 * @param j Position in the pattern
	 * @return Parsed result or null on error (which does not imply that an error was printed)
	 */
	@Nullable
	private ParseResult parse_i(final String pattern, int i, int j) {
		ParseResult res;
		int end, i2;
		
		while (j < pattern.length()) {
			switch (pattern.charAt(j)) {
				case '[': {
					final ParseLogHandler log = SkriptLogger.startParseLogHandler();
					try {
						res = parse_i(pattern, i, j + 1);
						if (res != null) {
							log.printLog();
							return res;
						}
						log.clear();
						j = nextBracket(pattern, ']', '[', j + 1, true) + 1;
						res = parse_i(pattern, i, j);
						if (res == null)
							log.printError();
						else
							log.printLog();
						return res;
					} finally {
						log.stop();
					}
				}
				case '(': {
					final ParseLogHandler log = SkriptLogger.startParseLogHandler();
					try {
						final int start = j;
						for (; j < pattern.length(); j++) {
							log.clear();
							if (j == start || pattern.charAt(j) == '|') {
								int mark = 0;
								if (j != pattern.length() - 1 && ('0' <= pattern.charAt(j + 1) && pattern.charAt(j + 1) <= '9' || pattern.charAt(j + 1) == '-')) {
									final int j2 = pattern.indexOf('¦', j + 2);
									if (j2 != -1) {
										try {
											mark = Integer.parseInt(pattern.substring(j + 1, j2));
											j = j2;
										} catch (final NumberFormatException e) { /* skipped */ }
									}
								}
								res = parse_i(pattern, i, j + 1);
								if (res != null) {
									log.printLog();
									res.mark ^= mark; // doesn't do anything if no mark was set as x ^ 0 == x
									return res;
								}
							} else if (pattern.charAt(j) == '(') {
								j = nextBracket(pattern, ')', '(', j + 1, true);
							} else if (pattern.charAt(j) == ')') {
								break;
							} else if (j == pattern.length() - 1) {
								throw new MalformedPatternException(pattern, "Missing closing bracket ')'");
							}
						}
						log.printError();
						return null;
					} finally {
						log.stop();
					}
				}
				case '%': {
					if (i == expr.length())
						return null;
					end = pattern.indexOf('%', j + 1);
					if (end == -1)
						throw new MalformedPatternException(pattern, "Odd number of '%'");
					if (end == pattern.length() - 1) {
						i2 = expr.length();
					} else {
						i2 = next(expr, i, context);
						if (i2 == -1)
							return null;
					}
					final ParseLogHandler log = SkriptLogger.startParseLogHandler();
					final String name = "" + pattern.substring(j + 1, end);
					final ExprInfo vi = getExprInfo(name);
					try {
						for (; i2 != -1; i2 = next(expr, i2, context)) {
							log.clear();
							res = parse_i(pattern, i2, end + 1);
							if (res != null) {
								final ParseLogHandler log2 = SkriptLogger.startParseLogHandler();
								try {
									for (int k = 0; k < vi.classes.length; k++) {
										if ((flags & vi.flagMask) == 0)
											continue;
										log2.clear();
										@SuppressWarnings("unchecked")
										final Expression<?> e = new SkriptParser("" + expr.substring(i, i2), flags & vi.flagMask, context).parseExpression(vi.classes[k].getC());
										if (e != null) {
											if (!vi.isPlural[k] && !e.isSingle()) {
												if (context == ParseContext.COMMAND) {
													Skript.error(Commands.m_too_many_arguments.toString(vi.classes[k].getName().getIndefiniteArticle(), vi.classes[k].getName().toString()), ErrorQuality.SEMANTIC_ERROR);
													return null;
												} else {
													Skript.error("'" + expr.substring(0, i) + "<...>" + expr.substring(i2) + "' can only accept a single " + vi.classes[k].getName() + ", not more", ErrorQuality.SEMANTIC_ERROR);
													return null;
												}
											}
											if (vi.time != 0) {
												if (e instanceof Literal<?>)
													return null;
												if (ScriptLoader.hasDelayBefore == Kleenean.TRUE) {
													Skript.error("Cannot use time states after the event has already passed", ErrorQuality.SEMANTIC_ERROR);
													return null;
												}
												if (!e.setTime(vi.time)) {
													Skript.error(e + " does not have a " + (vi.time == -1 ? "past" : "future") + " state", ErrorQuality.SEMANTIC_ERROR);
													return null;
												}
											}
											log2.printLog();
											log.printLog();
											res.exprs[countUnescaped(pattern, '%', 0, j) / 2] = e;
											return res;
										}
									}
									return null;
								} finally {
									log2.printError();
								}
							}
						}
					} finally {
						if (!log.isStopped())
							log.printError();
					}
					return null;
				}
				case '<': {
					end = pattern.indexOf('>', j + 1);// not next()
					if (end == -1)
						throw new MalformedPatternException(pattern, "Missing closing regex bracket '>'");
					Pattern p;
					try {
						p = Pattern.compile(pattern.substring(j + 1, end));
					} catch (final PatternSyntaxException e) {
						throw new MalformedPatternException(pattern, "Invalid regex <" + pattern.substring(j + 1, end) + ">", e);
					}
					final ParseLogHandler log = SkriptLogger.startParseLogHandler();
					try {
						final Matcher m = p.matcher(expr);
						for (i2 = next(expr, i, context); i2 != -1; i2 = next(expr, i2, context)) {
							log.clear();
							m.region(i, i2);
							if (m.matches()) {
								res = parse_i(pattern, i2, end + 1);
								if (res != null) {
									res.regexes.add(0, m.toMatchResult());
									log.printLog();
									return res;
								}
							}
						}
						log.printError(null);
						return null;
					} finally {
						log.stop();
					}
				}
				case ']':
				case ')':
					j++;
					continue;
				case '|':
					final int newJ = nextBracket(pattern, ')', '(', j + 1, getGroupLevel(pattern, j) != 0);
					if (newJ == -1) {
						if (i == expr.length()) {
							j = pattern.length();
							break;
						} else {
							i = 0;
							j++;
							continue;
						}
					} else {
						j = newJ + 1;
						break;
					}
				case ' ':
					if (i == 0 || i == expr.length() || i > 0 && expr.charAt(i - 1) == ' ') {
						j++;
						continue;
					} else if (expr.charAt(i) != ' ') {
						return null;
					}
					i++;
					j++;
					continue;
				case '\\':
					j++;
					if (j == pattern.length())
						throw new MalformedPatternException(pattern, "Must not end with a backslash");
					//$FALL-THROUGH$
				default:
					if (i == expr.length() || Character.toLowerCase(pattern.charAt(j)) != Character.toLowerCase(expr.charAt(i)))
						return null;
					i++;
					j++;
			}
		}
		if (i == expr.length() && j == pattern.length())
			return new ParseResult(this, pattern);
		return null;
	}
	
	/**
	 * Validates a user-defined pattern (used in {@link ExprParse}).
	 * 
	 * @param pattern
	 * @return The pattern with %codenames% and a boolean array that contains whetehr the expressions are plural or not
	 */
	@Nullable
	public static NonNullPair<String, boolean[]> validatePattern(final String pattern) {
		final List<Boolean> ps = new ArrayList<Boolean>();
		int groupLevel = 0, optionalLevel = 0;
		final Deque<Character> groups = new LinkedList<Character>();
		final StringBuilder b = new StringBuilder(pattern.length());
		int last = 0;
		for (int i = 0; i < pattern.length(); i++) {
			final char c = pattern.charAt(i);
			if (c == '(') {
				groupLevel++;
				groups.addLast(c);
			} else if (c == '|') {
				if (groupLevel == 0 || groups.peekLast() != '(' && groups.peekLast() != '|')
					return error("Cannot use the pipe character '|' outside of groups. Escape it if you want to match a literal pipe: '\\|'");
				groups.removeLast();
				groups.addLast(c);
			} else if (c == ')') {
				if (groupLevel == 0 || groups.peekLast() != '(' && groups.peekLast() != '|')
					return error("Unexpected closing group bracket ')'. Escape it if you want to match a literal bracket: '\\)'");
				if (groups.peekLast() == '(')
					return error("(...|...) groups have to contain at least one pipe character '|' to separate it into parts. Escape the brackets if you want to match literal brackets: \"\\(not a group\\)\"");
				groupLevel--;
				groups.removeLast();
			} else if (c == '[') {
				optionalLevel++;
				groups.addLast(c);
			} else if (c == ']') {
				if (optionalLevel == 0 || groups.peekLast() != '[')
					return error("Unexpected closing optional bracket ']'. Escape it if you want to match a literal bracket: '\\]'");
				optionalLevel--;
				groups.removeLast();
			} else if (c == '<') {
				final int j = pattern.indexOf('>', i + 1);
				if (j == -1)
					return error("Missing closing regex bracket '>'. Escape the '<' if you want to match a literal bracket: '\\<'");
				try {
					Pattern.compile(pattern.substring(i + 1, j));
				} catch (final PatternSyntaxException e) {
					return error("Invalid regular expression '" + pattern.substring(i + 1, j) + "': " + e.getLocalizedMessage());
				}
				i = j;
			} else if (c == '>') {
				return error("Unexpected closing regex bracket '>'. Escape it if you want to match a literal bracket: '\\>'");
			} else if (c == '%') {
				final int j = pattern.indexOf('%', i + 1);
				if (j == -1)
					return error("Missing end sign '%' of expression. Escape the percent sign to match a literal '%': '\\%'");
				final NonNullPair<String, Boolean> p = Utils.getEnglishPlural("" + pattern.substring(i + 1, j));
				final ClassInfo<?> ci = Classes.getClassInfoFromUserInput(p.getFirst());
				if (ci == null)
					return error("The type '" + p.getFirst() + "' could not be found. Please check your spelling or escape the percent signs if you want to match literal %s: \"\\%not an expression\\%\"");
				ps.add(p.getSecond());
				b.append(pattern.substring(last, i + 1));
				b.append(Utils.toEnglishPlural(ci.getCodeName(), p.getSecond()));
				last = j;
				i = j;
			} else if (c == '\\') {
				if (i == pattern.length() - 1)
					return error("Pattern must not end in an unescaped backslash. Add another backslash to escape it, or remove it altogether.");
				i++;
			}
		}
		b.append(pattern.substring(last));
		final boolean[] plurals = new boolean[ps.size()];
		for (int i = 0; i < plurals.length; i++)
			plurals[i] = ps.get(i);
		return new NonNullPair<String, boolean[]>("" + b.toString(), plurals);
	}
	
	@Nullable
	private static NonNullPair<String, boolean[]> error(final String error) {
		Skript.error("Invalid pattern: " + error);
		return null;
	}
	
	private final static Message m_quotes_error = new Message("skript.quotes error");
	private final static Message m_brackets_error = new Message("skript.brackets error");
	
	public static boolean validateLine(final String line) {
		if (StringUtils.count(line, '"') % 2 != 0) {
			Skript.error(m_quotes_error.toString());
			return false;
		}
		for (int i = 0; i < line.length(); i = next(line, i, ParseContext.DEFAULT)) {
			if (i == -1) {
				Skript.error(m_brackets_error.toString());
				return false;
			}
		}
		return true;
	}
	
	private final static class ExprInfo {
		public ExprInfo(final int length) {
			classes = new ClassInfo[length];
			isPlural = new boolean[length];
		}
		
		final ClassInfo<?>[] classes;
		final boolean[] isPlural;
		boolean isOptional;
		int flagMask = ~0;
		int time;
	}
	
	private static final HashMap<String, ExprInfo> exprInfoCache = new HashMap<String, ExprInfo>();
	
	private static ExprInfo getExprInfo(final String s) throws MalformedPatternException, IllegalArgumentException, SkriptAPIException {
		ExprInfo r = exprInfoCache.get(s);
		if (r == null) {
			r = createExprInfo(s);
			exprInfoCache.put(s, r);
		}
		
		return r;
	}
	
	private static ExprInfo createExprInfo(String s) throws MalformedPatternException, IllegalArgumentException, SkriptAPIException {
		final ExprInfo r = new ExprInfo(StringUtils.count(s, '/') + 1);
		r.isOptional = s.startsWith("-");
		if (r.isOptional)
			s = "" + s.substring(1);
		if (s.startsWith("*")) {
			s = "" + s.substring(1);
			r.flagMask &= ~PARSE_EXPRESSIONS;
		} else if (s.startsWith("~")) {
			s = "" + s.substring(1);
			r.flagMask &= ~PARSE_LITERALS;
		}
		if (!r.isOptional) {
			r.isOptional = s.startsWith("-");
			if (r.isOptional)
				s = "" + s.substring(1);
		}
		final int a = s.indexOf('@');
		if (a != -1) {
			r.time = Integer.parseInt(s.substring(a + 1));
			s = "" + s.substring(0, a);
		}
		final String[] classes = s.split("/");
		assert classes.length == r.classes.length;
		for (int i = 0; i < classes.length; i++) {
			final NonNullPair<String, Boolean> p = Utils.getEnglishPlural("" + classes[i]);
			r.classes[i] = Classes.getClassInfo(p.getFirst());
			r.isPlural[i] = p.getSecond();
		}
		return r;
	}
	
}
