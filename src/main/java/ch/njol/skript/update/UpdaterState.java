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
 * The state of an {@link Updater}.
 *
 * @see Updater
 */
public enum UpdaterState {

    /**
     * The updater has not been started.
     * It may be disabled or just not started yet.
     */
    NOT_STARTED,

    /**
     * Update check is currently in progress.
     */
    CHECKING,

    /**
     * Update is checked, but not yet started to install.
     */
    PENDING_DOWNLOAD,

    /**
     * An update is currently being downloaded.
     */
    DOWNLOADING,

    /**
     * An update is currently being installed.
     */
    INSTALLING,

    /**
     * An update is downloaded and installed,
     * but server must be restarted to complete install.
     */
    PENDING_RESTART,

    /**
     * The updater has successfully downloaded
     * and installed the updates.
     */
    UPDATED,

    /**
     * The updater has encountered an error.
     */
    ERROR

}
