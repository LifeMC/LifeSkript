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

package ch.njol.skript.update.addon;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.update.*;
import ch.njol.skript.util.Version;
import ch.njol.util.WebUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * General updater for {@link SkriptAddon}s.
 * <p>
 * You can register your add-on with {@link AddonKnowledge#addKnowledge(SkriptAddon, String, String)}.<p />
 * <p />If your add-on is supported natively by default, you don't need to do anything.
 * <p>
 * {@link Skript Skript} uses workarounds and correct handling of JAR updating,
 * so your add-on does not break in run-time.
 *
 * @see AddonKnowledge#addKnowledge(SkriptAddon, String, String)
 */
public final class AddonUpdater extends AbstractUpdater {

    private final SkriptAddon addon;
    private final String downloadUrl;
    private final String versionCheckUrl;
    private final Executor updateCheckerExecutor;
    private final Release current;
    public AddonUpdater(final SkriptAddon addon,

                        final String versionCheckUrl,
                        final String downloadUrl,

                        final boolean enabled,
                        final ReleaseChannel channel,
                        final long frequency,
                        final TimeUnit unit) {
        super(enabled, channel, frequency, unit);

        this.addon = addon;

        this.downloadUrl = downloadUrl;
        this.versionCheckUrl = versionCheckUrl;

        updateCheckerExecutor = getUpdateCheckerExecutor(addon.getName() + " update checker");

        current = new AddonRelease(this.addon, this.downloadUrl, this, this.addon.version,
                ReleaseChannel.parseOrNightly(this.addon.version.toString()),
                ReleaseStatus.CURRENT, true);
    }

    @Override
    public boolean isAutoInstallEnabled() {
        return SkriptConfig.automaticallyDownloadNewAddonVersions.value();
    }

    @Override
    public void setAutoInstallEnabled(final boolean enabled) {
        SkriptConfig.automaticallyDownloadNewAddonVersions.setValue(enabled);
    }

    @Override
    public String getName() {
        return addon.plugin.getName();
    }

    @Override
    public Release getCurrentRelease() {
        return current;
    }

    @Override
    public CompletableFuture<Release> getLatestRelease() {
        state = UpdaterState.CHECKING;
        return CompletableFuture.supplyAsync(() -> {
            final Version latestVersion = getVersionFromUrl(versionCheckUrl);
            state = UpdaterState.PENDING_DOWNLOAD;

            return new AddonRelease(addon, downloadUrl, AddonUpdater.this, latestVersion, AddonUpdater.this.channel, ReleaseStatus.LATEST, false);
        }, updateCheckerExecutor);
    }

    @Override
    public void installRelease(final Release release) throws InstalledReleaseException, IOException {
        if (release.isInstalled())
            throw new InstalledReleaseException("Release is already installed");
        state = UpdaterState.DOWNLOADING;

        current.backup();

        final File serverDirectory = Skript.getInstance().getDataFolder().getParentFile().getCanonicalFile().getParentFile().getCanonicalFile();
        final File updatedJar = Paths.get(serverDirectory.getCanonicalPath(), "plugins", "Skript", "update", Objects.requireNonNull(addon.getFile()).getName()).toFile();

        updatedJar.getParentFile().mkdirs();
        updatedJar.createNewFile();

        final String url = release.getDownloadUrl();

        try (final InputStream stream = Skript.invoke(Skript.urlOf(url).openConnection(), connection -> {
            try {
                WebUtils.setup(connection, null, true);

                connection.setRequestProperty("User-Agent", WebUtils.USER_AGENT + ' ' + addon.getName() + '/' + current.getVersion());

                if (url.toLowerCase(Locale.ENGLISH).contains("github".toLowerCase(Locale.ENGLISH)))
                    connection.setRequestProperty("Accept", "application/octet-stream");

                return connection.getInputStream();
            } catch (final IOException e) {
                throw handleError(e, "Can't download the release " + release.getVersion() + " of " + getName());
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
                 final FileOutputStream fileOutputStream = new FileOutputStream(Paths.get(serverDirectory.getCanonicalPath(), "/plugins/" + Objects.requireNonNull(addon.getFile(), addon::getName).getName()).toString());
                 final FileChannel fileChannel = fileOutputStream.getChannel()) {

                state = UpdaterState.UPDATED;

                fileChannel
                        .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            } catch (final IOException e) {
                throw handleError(e, "Can't install the release " + release.getVersion() + " of " + getName());
            }
            updatedJar.delete();
            updatedJar.getParentFile().delete();
        }, addon.getName() + " update installer"));

        state = UpdaterState.PENDING_RESTART;
    }

    public static final class AddonKnowledge {

        private static final AddonKnowledge[] defaultKnowledge = {
                new AddonKnowledge("SharpSK",
                        "https://raw.githubusercontent.com/TheDGOfficial/SharpSK/master/src/main/resources/plugin.yml",
                        "https://github.com/TheDGOfficial/SharpSK/releases/download/%version%/SharpSK-%version%.jar"),
                new AddonKnowledge("skRayFall",
                        "https://raw.githubusercontent.com/eyesniper2/skRayFall/master/build.gradle",
                        "https://skripttools.net/dl/skRayFall+%version%.jar"
                        /*"https://dev.bukkit.org/projects/skrayfall/files/latest"*/) // TODO maybe support downloads from CloudFlare-sites?
                /*
                new AddonKnowledge("skUtilities",
                        "https://raw.githubusercontent.com/tim740/skUtilities/master/latest.ver",
                        "https://github.com/tim740/skUtilities/releases/download/v%version%/skUtilities.v%version%.jar"),
                new AddonKnowledge("Skellet",
                        "https://raw.githubusercontent.com/TheLimeGlass/Skellett/master/gradle.properties",
                        "https://github.com/TheLimeGlass/Skellett/releases/download/%version%/Skellett-Legacy.jar")*/
        };

        private static final Map<String, AddonKnowledge> knowledge =
                new HashMap<>(defaultKnowledge.length);

        static {
            for (final AddonKnowledge addonKnowledge : defaultKnowledge)
                addKnowledge(addonKnowledge);
        }

        private final String addon;
        private final String versionCheckUrl;
        private final String downloadUrl;
        @Nullable
        private AddonUpdater updater;

        public AddonKnowledge(final String addon,
                              final String versionCheckUrl,
                              final String downloadUrl) {
            this.addon = addon;
            this.versionCheckUrl = versionCheckUrl;
            this.downloadUrl = downloadUrl;
        }

        /**
         * Constructs a new {@link AddonKnowledge} with the given parameters
         * and adds it to the add-on updater dictionary.
         *
         * @param addon           The {@link Skript Skript} addon, can be get using {@link Skript#registerAddon(JavaPlugin)}.
         * @param versionCheckUrl The version check URL of the add-on.
         * @param downloadUrl     The download URL of the add-on. Use %version% placeholder for latest version.
         */
        public static final void addKnowledge(final SkriptAddon addon,
                                              final String versionCheckUrl,
                                              final String downloadUrl) {
            addKnowledge(new AddonKnowledge(addon.getName(), versionCheckUrl, downloadUrl));
        }

        /**
         * Constructs a new {@link AddonKnowledge} with the given parameters
         * and adds it to the add-on updater dictionary.
         *
         * @param addonName       The name of the add-on, must be same as the plugin's name.
         * @param versionCheckUrl The version check URL of the add-on.
         * @param downloadUrl     The download URL of the add-on. Use %version% placeholder for latest version.
         */
        public static final void addKnowledge(final String addonName,
                                              final String versionCheckUrl,
                                              final String downloadUrl) {
            addKnowledge(new AddonKnowledge(addonName, versionCheckUrl, downloadUrl));
        }

        /**
         * Adds the given knowledge to the add-on updater dictionary.
         *
         * @param addonKnowledge The add-on knowledge to use.
         */
        public static final void addKnowledge(final AddonKnowledge addonKnowledge) {
            if (knowledge.containsKey(addonKnowledge.addon))
                return; // Natively supported by default or programmer's mistake
            knowledge.put(addonKnowledge.addon, addonKnowledge);
        }

        @Nullable
        public static final AddonKnowledge get(final SkriptAddon addon) {
            return get(addon.getName());
        }

        @Nullable
        public static final AddonKnowledge get(final String addonName) {
            return knowledge.get(addonName);
        }

        public final AddonUpdater getUpdater() {
            AddonUpdater addonUpdater = updater;
            if (addonUpdater == null) {
                final SkriptAddon skriptAddon = Skript.getAddon(addon);
                assert skriptAddon != null : addon;

                addonUpdater = new AddonUpdater(skriptAddon, versionCheckUrl, downloadUrl, SkriptConfig.checkForNewAddonVersions.value(), ReleaseChannel.parseOrNightly(skriptAddon.version.toString()), SkriptConfig.addonUpdateCheckInterval.value().getMilliSeconds(), TimeUnit.MILLISECONDS);
            }
            if (updater == null)
                updater = addonUpdater;
            return addonUpdater;
        }

        @Override
        public final boolean equals(@Nullable final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final AddonKnowledge that = (AddonKnowledge) o;

            if (!addon.equals(that.addon)) return false;
            if (!versionCheckUrl.equals(that.versionCheckUrl)) return false;
            return downloadUrl.equals(that.downloadUrl);
        }

        @Override
        public final int hashCode() {
            int result = addon.hashCode();
            result = 31 * result + versionCheckUrl.hashCode();
            result = 31 * result + downloadUrl.hashCode();
            return result;
        }
    }

}
