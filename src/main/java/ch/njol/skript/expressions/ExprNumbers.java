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

package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.agents.SkriptAgentKt;
import ch.njol.skript.agents.events.end.ForLoopEndEvent;
import ch.njol.skript.agents.events.start.ForLoopStartEvent;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Güttinger
 */
@Name("Numbers")
@Description({"All numbers between two given numbers, useful for looping.", "Use 'numbers' if your start is not an integer and you want to keep the fractional part of the start number constant, or use 'integers' if you only want to loop integers.", "An integer loop from 1 to a number x can also be written as 'loop x times'."})
@Examples({"loop 5 times: # loops 1, 2, 3, 4, 5", "loop numbers from 2.5 to 5.5: # loops 2.5, 3.5, 4.5, 5.5", "loop integers from 2.9 to 5.1: # same as '3 to 5', i.e. loops 3, 4, 5"})
@Since("1.4.6")
public final class ExprNumbers extends SimpleExpression<Number> {
    static {
        Skript.registerExpression(ExprNumbers.class, Number.class, ExpressionType.COMBINED, "[(all|the)] (numbers|1¦integers) (between|from) %number% (and|to) %number%", "%number% times");
    }

    boolean integer;
    boolean isInLoop;

    @SuppressWarnings("null")
    private Expression<Number> start, end;

    private static boolean isInLoop() {
        final Node node = SkriptLogger.getNode();
        if (node == null) {
            return false;
        }
        final String key = node.getKey();
        if (key == null) {
            return false;
        }
        return key.startsWith("loop ");
    }

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
        start = matchedPattern == 0 ? (Expression<Number>) exprs[0] : new SimpleLiteral<>(1, false);
        end = (Expression<Number>) exprs[1 - matchedPattern];
        isInLoop = isInLoop();
        if (end instanceof Literal) {
            final int amount = ((Literal<Number>) end).getSingle().intValue();
            if (amount == 0 && isInLoop) {
                Skript.warning("Looping zero times makes the code inside of the loop useless");
                return false;
            } else if (amount == 1 && isInLoop) {
                Skript.warning("Since you're looping exactly one time, you could simply remove the loop instead");
            } else if (amount < 0 && isInLoop) {
                Skript.error("Looping a negative amount of times is impossible");
                return false;
            }
        }
        integer = parseResult.mark == 1 || matchedPattern == 1;
        return true;
    }

    @Override
    @Nullable
    protected Number[] get(final Event e) {
        final Number s = start.getSingle(e), f = end.getSingle(e);
        if (s == null || f == null || s.doubleValue() > f.doubleValue())
            return null;
        final Number[] array = integer ? new Long[(int) (Math.floor(f.doubleValue()) - Math.ceil(s.doubleValue()) + 1)] : new Double[(int) Math.floor(f.doubleValue() - s.doubleValue() + 1)];
        final double low = integer ? Math.ceil(s.doubleValue()) : s.doubleValue();
        for (int i = 0; i < array.length; i++) {
            if (integer)
                array[i] = (long) low + i;
            else
                array[i] = low + i;
        }
        return array;
    }

    @Override
    @Nullable
    public Iterator<Number> iterator(final Event e) {
        final Number s = start.getSingle(e), f = end.getSingle(e);
        if (s == null || f == null || s.doubleValue() > f.doubleValue())
            return null;
        return new Iterator<Number>() {
            final double max = integer ? Math.floor(f.doubleValue()) : f.doubleValue();
            double i = integer ? Math.ceil(s.doubleValue()) : s.doubleValue();

            long startTime;
            long endTime;

            @Override
            public boolean hasNext() {
                final boolean hasNext = i <= max;
                if (!hasNext && integer && isInLoop && SkriptAgentKt.isTrackingEnabled()) {
                    endTime = System.nanoTime();
                    SkriptAgentKt.throwEvent(new ForLoopEndEvent((int) max, startTime, endTime));
                }
                return hasNext;
            }

            @SuppressWarnings("null")
            @Override
            public Number next() {
                if (integer && isInLoop && i == 1 && SkriptAgentKt.isTrackingEnabled()) {
                    startTime = System.nanoTime();
                    SkriptAgentKt.throwEvent(new ForLoopStartEvent((int) max));
                }
                if (!hasNext())
                    throw new NoSuchElementException();
                if (integer)
                    return (long) i++;
				return i++;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public String toString(final @Nullable Event e, final boolean debug) {
        return (integer ? "integers" : "numbers") + " from " + start.toString(e, debug) + " to " + end.toString(e, debug);
    }

    @Override
    public boolean isLoopOf(final String s) {
        return integer && ("integer".equalsIgnoreCase(s) || "int".equalsIgnoreCase(s));
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return integer ? Long.class : Double.class;
    }

}
