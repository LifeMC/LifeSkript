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

package ch.njol.yggdrasil;

import ch.njol.util.coll.BidiHashMap;
import ch.njol.util.coll.BidiMap;
import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public final class SimpleClassResolver implements ClassResolver {

    private final BidiMap<Class<?>, String> classes = new BidiHashMap<>();

    public final void registerClass(final Class<?> c, final String id) {
        final String oldId = classes.put(c, id);
        if (oldId != null && !oldId.equals(id))
            throw new YggdrasilException("Changed id of " + c + " from " + oldId + " to " + id);
    }

    @Override
    @Nullable
    public final Class<?> getClass(final String id) {
        return classes.getKey(id);
    }

    @Override
    @Nullable
    public final String getID(final Class<?> c) {
        return classes.getValue(c);
    }

}
