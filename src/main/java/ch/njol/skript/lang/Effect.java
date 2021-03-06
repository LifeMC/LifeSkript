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

import ch.njol.skript.Skript;
import ch.njol.skript.lang.function.EffFunctionCall;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;

/**
 * An effect which is unconditionally executed when reached, and execution will usually continue with the next item of the trigger after this effect is executed (the stop effect
 * for example stops the trigger, i.e. nothing else will be executed after it)
 *
 * @author Peter Güttinger
 * @see Skript#registerEffect(Class, String...)
 */
public abstract class Effect extends Statement {

    protected Effect() {
        /* implicit super call */
    }

    @SuppressWarnings({"rawtypes", "unchecked", "null"})
    @Nullable
    public static final Effect parse(final String s, @Nullable final String defaultError) {
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            final EffFunctionCall f = EffFunctionCall.parse(s);
            if (f != null) {
                log.printLog();
                return f;
            }
            if (log.hasError()) {
                log.printError();
                return null;
            }
            log.printError();
        } finally {
            log.stop();
        }
        return SkriptParser.<Effect>parse(s, (Iterator/*<? extends SyntaxElementInfo<Effect>>*/) Skript.getEffects().iterator(), defaultError);
    }

    /**
     * Executes this effect.
     *
     * @param e
     */
    protected abstract void execute(final Event e);

    @Override
    public final boolean run(final Event e) {
        execute(e);
        return true;
    }

}
