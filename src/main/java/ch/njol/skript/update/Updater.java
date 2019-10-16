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

import ch.njol.util.NonNullPair;
import ch.njol.util.misc.Nameable;
import ch.njol.util.misc.Stateable;
import ch.njol.util.misc.Toggleable;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface Updater extends Toggleable, Nameable, Stateable<UpdaterState> {

    /**
     * Checks if this {@link Updater} is enabled.
     *
     * @return True if this {@link Updater} is enabled.
     */
    @Override
    boolean isEnabled();

    /**
     * Enables or disables this {@link Updater}.
     *
     * @param enabled True to toggle/enable
     */
    @Override
    void setEnabled(final boolean enabled);

    /**
     * Checks if this {@link Updater} is configured to
     * auto install new updates.
     *
     * @return True if this {@link Updater} is configured
     * to auto install new updates.
     */
    boolean isAutoInstallEnabled();

    /**
     * Sets this {@link Updater} to auto install
     * or just notify new updates depending on the given flag.
     *
     * @param enabled The flag to enable or disable automatic
     *                installation of the new updates.
     */
    void setAutoInstallEnabled(final boolean enabled);

    /**
     * Gets the name of this {@link Updater}.
     * <p>
     * For example if this updater is for a plugin,
     * it should return the name of plugin.
     *
     * @return The name of this {@link Updater}.
     */
    @Override
    String getName();

    /**
     * Gets the state of this {@link Updater}.
     *
     * @return The state of this {@link Updater}.
     */
    @Override
    UpdaterState getState();

    /**
     * Gets the preferred release channel of this {@link Updater}
     * as configured.
     *
     * @return The preferred release channel of this {@link Updater}.
     */
    ReleaseChannel getReleaseChannel();

    /**
     * Sets the preferred release channel of this {@link Updater}.
     *
     * @param releaseChannel The new preferred release channel of this
     *                       {@link Updater} to set.
     */
    void setReleaseChannel(final ReleaseChannel releaseChannel);

    /**
     * Gets the check frequency of this {@link Updater}.
     *
     * @return A pair with the frequency and unit.
     * It will return 0 nanoseconds if periodic checking is disabled.
     */
    NonNullPair<Long, TimeUnit> getCheckFrequency();

    /**
     * Sets the check frequency of this {@link Updater}.
     *
     * @param frequency The check frequency to set.
     * @param unit      The unit of the frequency.
     */
    void setCheckFrequency(final long frequency,
                           final TimeUnit unit);

    /**
     * Gets the currently installed {@link Release}.
     *
     * @return The currently installed {@link Release}.
     */
    Release getCurrentRelease();

    /**
     * Gets the latest {@link Release} in the preferred
     * {@link ReleaseChannel}.
     *
     * @return The latest {@link Release} in the selected
     * {@link ReleaseChannel}.
     */
    CompletableFuture<Release> getLatestRelease();

    /**
     * Installs the specified {@link Release}.
     *
     * @param release The {@link Release} to install.
     * @throws InstalledReleaseException If the given release
     *                                   is already installed or another release that requires restart is installed (optional)
     *                                   and server is not restarted.
     * @throws IOException               If backup creating (optional) or update
     *                                   installing fails
     */
    void installRelease(final Release release) throws InstalledReleaseException, IOException;

    /**
     * Checks and installs updates as configured.
     *
     * @return The current {@link Updater} for chaining.
     * @throws IOException If backup creating (optional) or update
     *                     installing fails
     */
    default <T extends Updater> T checkAndInstallUpdates() throws IOException {
        return checkAndInstallUpdates(null);
    }

    /**
     * Checks and installs updates as configured.
     *
     * @param callback The callback to run when the update checking
     *                 and installing is finished.
     * @return The current {@link Updater} for chaining.
     * @throws IOException If backup creating (optional) or update
     *                     installing fails
     */
    <T extends Updater> T checkAndInstallUpdates(@Nullable final Consumer<T> callback) throws IOException;

    /**
     * Registers the periodical update checker of this {@link Updater}.
     * Does nothing if the updater is not enabled.
     * <p>
     * If periodical task is already registered, it cancels and re-
     * registers it.
     *
     * @return The current {@link Updater} for chaining.
     */
    <T extends Updater> T registerListener();

}
