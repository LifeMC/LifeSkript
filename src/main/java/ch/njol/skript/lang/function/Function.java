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

package ch.njol.skript.lang.function;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.agents.SkriptAgentKt;
import ch.njol.skript.agents.events.end.FunctionEndEvent;
import ch.njol.skript.agents.events.start.FunctionStartEvent;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;

/**
 * @author Peter Güttinger
 */
public abstract class Function<T> {

    /**
     * Execute functions even when some parameters are not present.
     * Field is updated by SkriptConfig in case of reloads.
     */
    public static boolean executeWithNulls = SkriptConfig.executeFunctionsWithMissingParams.value();

    final String name;

    final Parameter<?>[] parameters;

    @Nullable
    final ClassInfo<T> returnType;
    final boolean single;

    protected Function(final String name, final Parameter<?>[] parameters, @Nullable final ClassInfo<T> returnType, final boolean single) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.single = single;
    }

    public final String getName() {
        return name;
    }

    @SuppressWarnings("null")
    public final Parameter<?> getParameter(final int index) {
        return parameters[index];
    }

    public final Parameter<?>[] getParameters() {
        return parameters;
    }

    @Nullable
    public final ClassInfo<T> getReturnType() {
        return returnType;
    }

    public final boolean isSingle() {
        return single;
    }

    // TODO allow setting parameters by name
    @SuppressWarnings({"null", "unused"})
    public final int getMinParameters() {
        for (int i = parameters.length - 1; i >= 0; i--) {
            if (parameters[i].def == null && !parameters[i].isNone)
                return i + 1;
        }
        return 0;
    }

    public final int getMaxParameters() {
        return parameters.length;
    }

    /**
     * @param params An array with at least {@link #getMinParameters()} elements and at most {@link #getMaxParameters()} elements.
     * @return The result of the function
     */
    @SuppressWarnings("null")
    @Nullable
    public final T[] execute(final Object[][] params) {
        if (params.length > parameters.length) {
            assert false : params.length;
            return null;
        }
        final Object[][] ps = params.length < parameters.length ? Arrays.copyOf(params, parameters.length) : params;
        final FunctionEvent<? extends T> e = new FunctionEvent<>(!Bukkit.isPrimaryThread(), this);
        for (int i = 0; i < parameters.length; i++) {
            final Parameter<?> p = parameters[i];
            final Object[] val = i < params.length ? ps[i] : p.def != null ? p.def.getArray(e) : null;
            if (!executeWithNulls && !p.isNone && (val == null || val.length == 0))
                return null;
            ps[i] = val;
        }
        final boolean trackingEnabled = SkriptAgentKt.isTrackingEnabled();
        if (trackingEnabled)
            SkriptAgentKt.throwEvent(new FunctionStartEvent(this, params));
        final long startTime = System.nanoTime();
        final T[] r = execute(e, ps);
        resetReturnValue();
        final long endTime = System.nanoTime();
        if (trackingEnabled)
            SkriptAgentKt.throwEvent(new FunctionEndEvent(this, params, startTime, endTime));
        assert returnType == null ? r == null : r == null || (r.length <= 1 || !single) && !CollectionUtils.contains(r, null) && returnType.getC().isAssignableFrom(r.getClass().getComponentType()) : this + "; " + Arrays.toString(r);
        return r == null || r.length > 0 || this instanceof ScriptFunction && ((ScriptFunction<T>) this).ignoreEmptyReturn ? r : null;
    }

    /**
     * @param e
     * @param params An array containing as many arrays as this function has parameters. The contained arrays are neither null nor empty, and are of type Object[] (i.e. not of the
     *               actual parameters' types).
     * @return Whatever this function is supposed to return. May be null or empty, but must not contain null elements.
     */
    @Nullable
    public abstract T[] execute(final FunctionEvent<? extends T> e, final Object[][] params);

    /**
     * Resets the return value of the {@code Function}.
     * Should be called right after execution.
     *
     * @return Whether or not the return value was successfully reset
     */
    public abstract boolean resetReturnValue();

    @Override
    public final String toString() {
        return "function " + name;
    }

}
