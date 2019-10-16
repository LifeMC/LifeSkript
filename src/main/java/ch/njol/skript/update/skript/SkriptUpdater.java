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
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.update.*;
import ch.njol.skript.util.Version;
import ch.njol.util.WebUtils;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * The updater of {@link Skript}.
 */
public final class SkriptUpdater extends AbstractUpdater {
    private final Executor updateCheckerExecutor = getUpdateCheckerExecutor("Skript update checker");

    private final Release current =
            new SkriptRelease(this, Skript.getVersion(),
                    ReleaseChannel.parseOrNightly(Skript.getVersion().toString()),
                    ReleaseStatus.CURRENT, true);

    public SkriptUpdater(final boolean enabled,
                         final ReleaseChannel channel,
                         final long frequency,
                         final TimeUnit unit) {
        super(enabled, channel, frequency, unit);
    }

    @Override
    public boolean isAutoInstallEnabled() {
        return SkriptConfig.automaticallyDownloadNewVersion.value();
    }

    @Override
    public void setAutoInstallEnabled(final boolean enabled) {
        SkriptConfig.automaticallyDownloadNewVersion.setValue(enabled);
    }

    @Override
    public String getName() {
        return "Skript";
    }

    @Override
    public Release getCurrentRelease() {
        return current;
    }

    @Override
    public CompletableFuture<Release> getLatestRelease() {
        state = UpdaterState.CHECKING;
        return CompletableFuture.supplyAsync(() -> {
            final String latestVersion;
            if (Skript.latestVersion != null)
                latestVersion = Skript.latestVersion;
            else {
                assert false : Skript.getVersionWithSuffix();
                latestVersion = Skript.getVersion().toString();
            }
            state = UpdaterState.PENDING_DOWNLOAD;
            return new SkriptRelease(SkriptUpdater.this, new Version(latestVersion), SkriptUpdater.this.channel, ReleaseStatus.LATEST, false);
        }, updateCheckerExecutor);
    }

    @Override
    public void installRelease(final Release release) throws InstalledReleaseException, IOException {
        if (release.isInstalled())
            throw new InstalledReleaseException("Release is already installed");
        if (state == UpdaterState.DOWNLOADING || state == UpdaterState.INSTALLING || state == UpdaterState.PENDING_RESTART)
            throw new InstalledReleaseException("A release is already installed");
        state = UpdaterState.DOWNLOADING;

        current.backup();

        final File serverDirectory = Skript.getInstance().getDataFolder().getParentFile().getCanonicalFile().getParentFile().getCanonicalFile();
        final File updatedJar = Paths.get(serverDirectory.getCanonicalPath(), "plugins", "Skript", "update", "Skript.jar").toFile();

        updatedJar.getParentFile().mkdirs();
        updatedJar.createNewFile();

        try (final InputStream stream = Skript.invoke(Skript.urlOf(release.getDownloadUrl()).openConnection(), connection -> {
            try {
                WebUtils.setup(connection, null, true);

                connection.setRequestProperty("User-Agent", WebUtils.USER_AGENT);
                connection.setRequestProperty("Accept", "application/octet-stream");

                return connection.getInputStream();
            } catch (final IOException e) {
                throw handleError(e, "Can't install the release " + release.getVersion() + " of " + getName());
            }
        });
             final ReadableByteChannel readableByteChannel = Channels.newChannel(stream);
             final FileOutputStream fileOutputStream = new FileOutputStream(updatedJar);
             final FileChannel fileChannel = fileOutputStream.getChannel()) {

            state = UpdaterState.INSTALLING;

            fileChannel
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (state == UpdaterState.ERROR || !updatedJar.exists() || updatedJar.isDirectory())
                return;
            try (final FileInputStream fileInputStream = new FileInputStream(updatedJar);
                 final ReadableByteChannel readableByteChannel = Channels.newChannel(fileInputStream);
                 final FileOutputStream fileOutputStream = new FileOutputStream(Paths.get(serverDirectory.getCanonicalPath(), "/plugins/Skript.jar").toString());
                 final FileChannel fileChannel = fileOutputStream.getChannel()) {

                state = UpdaterState.UPDATED;

                fileChannel
                        .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            } catch (final IOException e) {
                throw handleError(e, "Can't install the release " + release.getVersion() + " of " + getName());
            }
            updatedJar.delete();
            updatedJar.getParentFile().delete();
        }, "Skript update installer"));

        state = UpdaterState.PENDING_RESTART;
    }

}
