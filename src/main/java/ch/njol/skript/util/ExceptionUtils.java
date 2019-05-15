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

package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;

public final class ExceptionUtils {

    private static final String IO_NODE = "io exceptions";

    private ExceptionUtils() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static final String toString(final IOException e) {
        if (Language.keyExists(IO_NODE + "." + e.getClass().getSimpleName())) {
            return Language.format(IO_NODE + "." + e.getClass().getSimpleName(), e.getLocalizedMessage());
        }
        if (Skript.testing())
            e.printStackTrace();
        return e.getLocalizedMessage();
    }

}
