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
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.UnparsedLiteral;

/**
 * A class that contains methods based around
 * making it easier to deal with {@link UnparsedLiteral}
 * objects.
 */
public class LiteralUtils {
	
	/**
	 * Checks an {@link Expression} for {@link UnparsedLiteral} objects
	 * and converts them if found.
	 *
	 * @param expr The expression to check for {@link UnparsedLiteral} objects
	 * @param <T> {@code expr}'s type
	 * @return {@code expr} without {@link UnparsedLiteral} objects
	 */
	@SuppressWarnings({"unchecked", "null"})
	public static <T> Expression<T> defendExpression(final Expression<?> expr) {
		if (expr instanceof ExpressionList) {
			final Expression<?>[] exprs = ((ExpressionList<?>) expr).getExpressions();
			for (int i = 0; i < exprs.length; i++)
				exprs[i] = defendExpression(exprs[i]);
		} else if (expr instanceof UnparsedLiteral) {
			final Literal<?> parsedLiteral = ((UnparsedLiteral) expr).getConvertedExpression(Object.class);
			return (Expression<T>) (parsedLiteral == null ? expr : parsedLiteral);
		}
		return (Expression<T>) expr;
	}
	
	/**
	 * Checks if an Expression contains {@link UnparsedLiteral}
	 * objects.
	 *
	 * @param expr The Expression to check for {@link UnparsedLiteral} objects
	 * @return Whether or not {@code expr} contains {@link UnparsedLiteral} objects
	 */
	public static boolean hasUnparsedLiteral(final Expression<?> expr) {
		if (expr instanceof UnparsedLiteral) {
			return true;
		} else if (expr instanceof ExpressionList) {
			for (final Expression<?> ex : ((ExpressionList<?>) expr).getExpressions()) {
				if (ex instanceof UnparsedLiteral) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if the passed Expressions are non-null
	 * and do not contain {@link UnparsedLiteral} objects.
	 *
	 * @param expressions The expressions to check for {@link UnparsedLiteral} objects
	 * @return Whether or not the passed expressions contain {@link UnparsedLiteral} objects
	 */
	@SuppressWarnings("null")
	public static boolean canInitSafely(final Expression<?>... expressions) {
		for (final Expression<?> expression : expressions) {
			if (expression == null || hasUnparsedLiteral(expression)) {
				return false;
			}
		}
		return true;
	}
	
}
