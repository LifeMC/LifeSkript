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

import java.util.Objects;

public final class ExpressionInfo<E extends Expression<T>, T> extends SyntaxElementInfo<E> {

    public final Class<T> returnType;

    public ExpressionInfo(final String[] patterns, final Class<T> returnType, final Class<E> c) throws IllegalArgumentException {
        super(patterns, c);
        this.returnType = returnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionInfo<?, ?> that = (ExpressionInfo<?, ?>) o;

        return Objects.equals(returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        return returnType != null ? returnType.hashCode() : 0;
    }
}
