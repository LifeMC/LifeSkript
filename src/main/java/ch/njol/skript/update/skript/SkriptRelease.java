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

package ch.njol.skript.update.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.update.InstalledReleaseException;
import ch.njol.skript.update.Release;
import ch.njol.skript.update.ReleaseChannel;
import ch.njol.skript.update.ReleaseStatus;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Version;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SkriptRelease implements Release {
    private static final Matcher ISSUES_MATCHER =
            Pattern.compile("issues", Pattern.LITERAL).matcher("");

    private final SkriptUpdater updater;
    private final Version version;
    private final ReleaseChannel channel;
    private final ReleaseStatus status;
    private final boolean installed;

    public SkriptRelease(final SkriptUpdater updater,
                         final Version version,
                         final ReleaseChannel channel,
                         final ReleaseStatus status,
                         final boolean installed) {
        this.updater = updater;
        this.version = version;

        this.channel = channel;

        this.status = status;
        this.installed = installed;
    }

    @Override
    public SkriptUpdater getUpdater() {
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
        return ISSUES_MATCHER.reset(Skript.ISSUES_LINK).replaceAll(Matcher.quoteReplacement("releases"))
                + "/download/" + version + '/' + (Skript.isOptimized ? "Skript-optimized.EXPERIMENTAL.jar" : "Skript.jar");
    }

    @Override
    public SkriptRelease backup() throws InstalledReleaseException, IOException {
        FileUtils.backup(Skript.getInstance().getFile());

        return this;
    }
}
