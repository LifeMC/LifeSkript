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

package ch.njol.skript.update;

/**
 * Thrown to indicate a release is already installed.
 *
 * This either may represent "can't install a release two times" or
 * just "can't install two updates at same time, restart required"
 */
public final class InstalledReleaseException extends Exception {
    private static final long serialVersionUID = -567096800630632837L;

    public InstalledReleaseException() {
        /* implicit super call */
    }

    public InstalledReleaseException(final String message) {
        super(message);
    }

    public InstalledReleaseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InstalledReleaseException(final Throwable cause) {
        super(cause);
    }
}
