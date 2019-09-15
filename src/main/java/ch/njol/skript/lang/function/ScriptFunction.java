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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.function.Functions.FunctionData;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.variables.Variables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public final class ScriptFunction<T> extends Function<T> {

    final Trigger trigger;
    public boolean ignoreEmptyReturn;
    private boolean returnValueSet;
    @Nullable
    private T[] returnValue;

    @SuppressWarnings("null")
    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public ScriptFunction(final String name, final Parameter<?>[] parameters, final SectionNode node, @Nullable final ClassInfo<T> returnType, final boolean single) {
        super(name, parameters, returnType, single);

        // here to allow recursion
        Functions.functions.put(name, new FunctionData(this));

        Functions.currentFunction = this;
        try {
            trigger = new Trigger(node.getConfig().getFile(), "function " + name, new SimpleEvent(), ScriptLoader.loadItems(node));
        } finally {
            Functions.currentFunction = null;
        }
    }

    /**
     * Should only be called by {@link ch.njol.skript.effects.EffReturn}.
     *
     * @param e
     * @param value
     */
    public void setReturnValue(final FunctionEvent<? extends T> e, final @Nullable T[] value) {
        setReturnValue(e, value, false);
    }

    /**
     * Should only be called by {@link ch.njol.skript.effects.EffReturn}.
     *
     * @param e
     * @param value
     * @param ignoreEmptyReturn
     */
    public void setReturnValue(final FunctionEvent<? extends T> e, final @Nullable T[] value, final boolean ignoreEmptyReturn) {
        assert !returnValueSet;
        returnValueSet = true;
        returnValue = value;
        this.ignoreEmptyReturn = ignoreEmptyReturn;
    }

    @Override
    @Nullable
    public final T[] execute(final FunctionEvent<? extends T> e, final Object[][] params) {
        for (int i = 0; i < parameters.length; i++) {
            final Parameter<?> p = parameters[i];
            final Object[] val = params[i];
            if (val != null && !p.isNone) {
                if (p.single && val.length > 0) {
                    Variables.setVariable(p.name, val[0], e, true);
                } else {
                    for (int j = 0; j < val.length; j++) {
                        Variables.setVariable(p.name + "::" + (j + 1), val[j], e, true);
                    }
                }
            }
        }
        trigger.execute(e);
        return returnValue;
    }

    @Override
    public boolean resetReturnValue() {
        returnValue = null;
        returnValueSet = false;
        return true;
    }

}
