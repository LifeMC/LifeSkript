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

package ch.njol.skript.update.script;

import ch.njol.skript.update.*;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Version;

import java.io.File;
import java.io.IOException;

public final class ScriptRelease implements Release {

    private final ScriptUpdater updater;
    private final Version version;
    private final ReleaseChannel channel;
    private final ReleaseStatus status;
    private final boolean installed;

    private final File script;
    private final String downloadUrl;

    public ScriptRelease(final File script,
                         final String downloadUrl,

                         final ScriptUpdater updater,
                         final Version version,
                         final ReleaseChannel channel,
                         final ReleaseStatus status,
                         final boolean installed) {
        this.script = script;
        this.downloadUrl = downloadUrl;

        this.updater = updater;
        this.version = version;

        this.channel = channel;

        this.status = status;
        this.installed = installed;
    }

    @Override
    public ScriptUpdater getUpdater() {
        return updater;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public ReleaseChannel getReleaseChannel() {
        return channel;
    }

    @Override
    public ReleaseStatus getState() {
        return status;
    }

    @Override
    public boolean isInstalled() {
        return installed;
    }

    @Override
    public Date getReleaseDate() {
        // TODO release date
        throw new UnsupportedOperationException();
    }

    @Override
    public String getReleaseNotes() {
        // TODO release notes
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDownloadUrl() {
        return AbstractUpdater.replaceVersionInDownloadUrl(this, downloadUrl);
    }

    @Override
    public ScriptRelease backup() throws InstalledReleaseException, IOException {
        FileUtils.backup(script);

        return this;
    }
}
