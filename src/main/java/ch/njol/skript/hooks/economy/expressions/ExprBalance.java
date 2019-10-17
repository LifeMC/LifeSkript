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

package ch.njol.skript.hooks.economy.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.hooks.economy.classes.Money;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Money")
@Description("How much virtual money a player has (can be changed). This expression requires Vault and a compatible economy plugin to be installed.")
@Examples({"message \"You have %player's money%\" # the currency name will be added automatically", "remove 20$ from the player's balance # replace '$' by whatever currency you use", "add 200 to the player's account # or omit the currency alltogether"})
@Since("2.0")
@RequiredPlugins({"Vault", "An economy Plugin"})
public final class ExprBalance extends SimplePropertyExpression<OfflinePlayer, Money> {
    static {
        register(ExprBalance.class, Money.class, ExprBalance::new, "(money|balance|[bank] account)", "offlineplayers");
    }

    @Override
    public Money convert(final OfflinePlayer p) {
        return new Money(VaultHook.getBalance(p));
    }

    @Override
    public Class<Money> getReturnType() {
        return Money.class;
    }

    @Override
    protected String getPropertyName() {
        return "money";
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(final ChangeMode mode) {
        if (mode == ChangeMode.REMOVE_ALL)
            return null;
        return new Class<?>[]{Money.class, Number.class};
    }

    @Override
    public void change(final Event e, @Nullable final Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
        assert mode != ChangeMode.REMOVE_ALL;

        if (delta == null) {
            for (final OfflinePlayer p : getExpr().getArray(e))
                VaultHook.remove(p, VaultHook.getBalance(p));
            return;
        }

        final double m = delta[0] instanceof Number ? ((Number) delta[0]).doubleValue() : ((Money) delta[0]).getAmount();
        for (final OfflinePlayer p : getExpr().getArray(e)) {
            switch (mode) {
                case SET:
                    final double b = VaultHook.getBalance(p);
                    if (b < m) {
                        VaultHook.add(p, m - b);
                    } else if (b > m) {
                        VaultHook.remove(p, b - m);
                    }
                    break;
                case ADD:
                    VaultHook.add(p, m);
                    break;
                case REMOVE:
                    VaultHook.remove(p, m);
                    break;
                case DELETE:
                    //noinspection ConstantConditions
                case REMOVE_ALL:
                case RESET:
                    assert false;
            }
        }
    }

}
