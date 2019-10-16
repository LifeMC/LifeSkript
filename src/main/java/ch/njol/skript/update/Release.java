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

import ch.njol.skript.util.Date;
import ch.njol.skript.util.Version;
import ch.njol.util.misc.Stateable;
import ch.njol.util.misc.Versionable;

import java.io.IOException;

/**
 * Represents a {@link Release}.
 */
public interface Release extends Versionable, Stateable<ReleaseStatus> {

    /**
     * Gets the updater used to fetch
     * this {@link Release}.
     *
     * You can install this {@link Release}
     * with the returned {@link Updater}.
     *
     * @return The updater used to fetch
     * this {@link Release}.
     */
    <T extends Updater> T getUpdater();

    /**
     * Gets the {@link Version} of this {@link Release}.
     *
     * @return The {@link Version} of this {@link Release}.
     */
    @Override
    Version getVersion();

    /**
     * Gets the release channel of this {@link Release}.
     *
     * @return The release channel of this {@link Release}.
     */
    ReleaseChannel getReleaseChannel();

    /**
     * Gets the state of this {@link Release}.
     *
     * @return The state of this {@link Release}.
     */
    @Override
    ReleaseStatus getState();

    /**
     * Checks if this {@link Release} is the
     * currently installed {@link Release}.
     *
     * Note that you can't update to this
     * release if this the currently installed release.
     *
     * @return True if this {@link Release}
     * is the currently installed {@link Release}.
     */
    boolean isInstalled();

    /**
     * Gets the release date of this {@link Release}.
     *
     * @return The release date of this {@link Release}.
     */
    Date getReleaseDate();

    /**
     * Gets the release notes of this {@link Release}.
     *
     * @return The release notes of this {@link Release}.
     */
    String getReleaseNotes();

    /**
     * Gets the download URL of this {@link Release}.
     *
     * @return The download URL of this {@link Release}.
     */
    String getDownloadUrl();

    /**
     * Backups this release. Works only on the
     * currently installed {@link Release}.
     *
     * @return The current {@link Release} for chaining.
     *
     * @throws InstalledReleaseException If this release
     * is not the currently installed {@link Release}.
     *
     * @throws IOException If backup creation failed.
     */
    <T extends Release> T backup() throws InstalledReleaseException, IOException;

}
