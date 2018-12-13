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

package ch.njol.skript.hooks.economy.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Arithmetic;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Comparators;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.StringUtils;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class Money {
	static {
		Classes.registerClass(new ClassInfo<Money>(Money.class, "money").user("money").name("Money").description("A certain amount of money. Please note that this differs from <a href='#number'>numbers</a> as it includes a currency symbol or name, but usually the two are interchangeable, e.g. you can both <code>add 100$ to the player's balance</code> and <code>add 100 to the player's balance</code>.").usage("<code>&lt;number&gt; $</code> or <code>$ &lt;number&gt;</code>, where '$' is your server's currency, e.g. '10 rupees' or '£5.00'").examples("add 10£ to the player's account", "remove Fr. 9.95 from the player's money", "set the victim's money to 0", "increase the attacker's balance by the level of the victim * 100").since("2.0").before("itemtype", "itemstack").parser(new Parser<Money>() {
			@Override
			@Nullable
			public Money parse(final String s, final ParseContext context) {
				return Money.parse(s);
			}
			
			@Override
			public String toString(final Money m, final int flags) {
				return m.toString();
			}
			
			@Override
			public String toVariableNameString(final Money o) {
				return "money:" + o.amount;
			}
			
			@Override
			public String getVariableNamePattern() {
				return "money:-?\\d+(\\.\\d+)?";
			}
		}).math(Money.class, new Arithmetic<Money, Money>() {
			@Override
			public Money difference(final Money first, final Money second) {
				final double d = Math.abs(first.getAmount() - second.getAmount());
				if (d < Skript.EPSILON)
					return new Money(0);
				return new Money(d);
			}
			
			@Override
			public Money add(final Money value, final Money difference) {
				return new Money(value.amount + difference.amount);
			}
			
			@Override
			public Money subtract(final Money value, final Money difference) {
				return new Money(value.amount - difference.amount);
			}
		}));
		
		Comparators.registerComparator(Money.class, Money.class, new Comparator<Money, Money>() {
			@Override
			public Relation compare(final Money m1, final Money m2) {
				return Relation.get(m1.amount - m2.amount);
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		Comparators.registerComparator(Money.class, Number.class, new Comparator<Money, Number>() {
			@Override
			public Relation compare(final Money m, final Number n) {
				return Relation.get(m.amount - n.doubleValue());
			}
			
			@Override
			public boolean supportsOrdering() {
				return true;
			}
		});
		
		Converters.registerConverter(Money.class, Double.class, new Converter<Money, Double>() {
			@SuppressWarnings("null")
			@Override
			public Double convert(final Money m) {
				return m.getAmount();
			}
		});
	}
	
	final double amount;
	
	public Money(final double amount) {
		this.amount = amount;
	}
	
	public double getAmount() {
		return amount;
	}
	
	@SuppressWarnings({"null", "unused"})
	@Nullable
	public static Money parse(final String s) {
		if (VaultHook.economy == null) {
//			Skript.error("No economy plugin detected");
			return null;
		}
		final String singular = VaultHook.economy.currencyNameSingular(), plural = VaultHook.economy.currencyNamePlural();
		if (!plural.isEmpty()) {
			if (StringUtils.endsWithIgnoreCase(s, plural)) {
				try {
					final double d = Double.parseDouble(s.substring(0, s.length() - plural.length()).trim());
					return new Money(d);
				} catch (final NumberFormatException ignored) {}
			} else if (StringUtils.startsWithIgnoreCase(s, plural)) {
				try {
					final double d = Double.parseDouble(s.substring(plural.length()).trim());
					return new Money(d);
				} catch (final NumberFormatException ignored) {}
			}
		}
		if (!singular.isEmpty()) {
			if (StringUtils.endsWithIgnoreCase(s, singular)) {
				try {
					final double d = Double.parseDouble(s.substring(0, s.length() - singular.length()).trim());
					return new Money(d);
				} catch (final NumberFormatException ignored) {}
			} else if (StringUtils.startsWithIgnoreCase(s, singular)) {
				try {
					final double d = Double.parseDouble(s.substring(singular.length()).trim());
					return new Money(d);
				} catch (final NumberFormatException ignored) {}
			}
		}
//		try {
//			return new Money(Double.parseDouble(s));
//		} catch (final NumberFormatException e) {}
		return null;
	}
	
	@Override
	public String toString() {
		return "" + VaultHook.economy.format(amount);
	}
	
}
