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

import ch.njol.skript.classes.ClassInfo;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class JavaFunction<T> extends Function<T> {

    @Nullable
    private String[] description;
    @Nullable
    private String[] examples;
    @Nullable
    private String since;

    protected JavaFunction(final String name, final ClassInfo<T> returnType, final boolean single, final Parameter<?>... parameters) {
        this(name, parameters, returnType, single);
    }

    protected JavaFunction(final String name, final Parameter<?>[] parameters, final ClassInfo<T> returnType, final boolean single) {
        super(name, parameters, returnType, single);
    }

    @Override
    @Nullable
    public abstract T[] execute(FunctionEvent<? extends T> e, Object[][] params);

    /**
     * Only used for Skript's documentation.
     *
     * @param description
     * @return This JavaFunction object
     */
    public final JavaFunction<T> description(final String... description) {
        assert this.description == null;
        this.description = description;
        return this;
    }

    /**
     * Only used for Skript's documentation.
     *
     * @param examples
     * @return This JavaFunction object
     */
    public final JavaFunction<T> examples(final String... examples) {
        assert this.examples == null;
        this.examples = examples;
        return this;
    }

    /**
     * Only used for Skript's documentation.
     *
     * @param since
     * @return This JavaFunction object
     */
    public final JavaFunction<T> since(final String since) {
        assert this.since == null;
        this.since = since;
        return this;
    }

    @Nullable
    public final String[] getDescription() {
        return description;
    }

    @Nullable
    public final String[] getExamples() {
        return examples;
    }

    @Nullable
    public final String getSince() {
        return since;
    }

    @Override
    public boolean resetReturnValue() {
        return true;
    }

}
