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

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.update.*;
import ch.njol.skript.util.Version;
import ch.njol.util.WebUtils;
import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * General updater for scripts. Used in {@link ch.njol.skript.ScriptLoader}.
 */
public final class ScriptUpdater extends AbstractUpdater {

    private final File script;
    private final String scriptName;
    private final String downloadUrl;
    private final String versionCheckUrl;
    private final Executor updateCheckerExecutor;
    private final Release current;
    private boolean isAutoInstall;

    public ScriptUpdater(final File script,
                         final String scriptName,

                         final Version currentVersion,

                         final String versionCheckUrl,
                         final String downloadUrl,

                         final boolean enabled,
                         final ReleaseChannel channel,
                         final long frequency,
                         final TimeUnit unit,

                         final boolean isAutoInstall) {
        super(enabled, channel, frequency, unit);

        this.script = script;
        this.scriptName = scriptName;

        this.downloadUrl = downloadUrl;
        this.versionCheckUrl = versionCheckUrl;

        this.isAutoInstall = isAutoInstall;

        updateCheckerExecutor = getUpdateCheckerExecutor(scriptName + " update checker");

        current = new ScriptRelease(this.script, this.downloadUrl, this, currentVersion,
                ReleaseChannel.parseOrNightly(currentVersion.toString()),
                ReleaseStatus.CURRENT, true);
    }

    @Override
    public boolean isAutoInstallEnabled() {
        return isAutoInstall;
    }

    @Override
    public void setAutoInstallEnabled(final boolean enabled) {
        isAutoInstall = enabled;
    }

    @Override
    public String getName() {
        return scriptName;
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

            return new ScriptRelease(script, downloadUrl, ScriptUpdater.this, latestVersion, ScriptUpdater.this.channel, ReleaseStatus.LATEST, false);
        }, updateCheckerExecutor);
    }

    @Override
    public void installRelease(final Release release) throws InstalledReleaseException, IOException {
        if (release.isInstalled())
            throw new InstalledReleaseException("Release is already installed");
        state = UpdaterState.DOWNLOADING;

        current.backup();

        final File serverDirectory = Skript.getInstance().getDataFolder().getParentFile().getCanonicalFile().getParentFile().getCanonicalFile();
        final File updatedScript = Paths.get(serverDirectory.getCanonicalPath(), "plugins", "Skript", "update", script.getName()).toFile();

        updatedScript.getParentFile().mkdirs();
        updatedScript.createNewFile();

        final String url = release.getDownloadUrl();

        try (final InputStream stream = Skript.invoke(Skript.urlOf(url).openConnection(), connection -> {
            try {
                WebUtils.setup(connection, null, true);

                connection.setRequestProperty("User-Agent", WebUtils.USER_AGENT + ' ' + scriptName + '/' + current.getVersion());

                if (url.toLowerCase(Locale.ENGLISH).contains("github".toLowerCase(Locale.ENGLISH)))
                    connection.setRequestProperty("Accept", "application/octet-stream");

                return connection.getInputStream();
            } catch (final IOException e) {
                throw handleError(e, "Can't install the release " + release.getVersion() + " of " + scriptName);
            }
        });
             final ReadableByteChannel readableByteChannel = Channels.newChannel(stream);
             final FileOutputStream fileOutputStream = new FileOutputStream(updatedScript);
             final FileChannel fileChannel = fileOutputStream.getChannel()) {

            state = UpdaterState.INSTALLING;

            fileChannel
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }

        if (state == UpdaterState.ERROR || !updatedScript.exists() || updatedScript.isDirectory())
            return;

        try (final FileInputStream fileInputStream = new FileInputStream(updatedScript);
             final ReadableByteChannel readableByteChannel = Channels.newChannel(fileInputStream);
             final FileOutputStream fileOutputStream = new FileOutputStream(script);
             final FileChannel fileChannel = fileOutputStream.getChannel()) {

            state = UpdaterState.UPDATED;

            fileChannel
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (final IOException e) {
            throw handleError(e, "Can't install the release " + release.getVersion() + " of " + scriptName);
        }

        updatedScript.delete();
        updatedScript.getParentFile().delete();

        // Apply changes immediately
        Bukkit.getScheduler().runTask(Skript.getInstance(), () -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                "skript reload " + script.getName()));
    }

    public static final class Parser {
        private static final ch.njol.skript.classes.Parser<Boolean> booleanParser =
                (ch.njol.skript.classes.Parser<Boolean>) Objects.requireNonNull(Objects.requireNonNull(Classes.getExactClassInfo(Boolean.class))
                        .getParser());

        private static final Predicate<String> parseBoolean = value -> booleanParser.parse(value, ParseContext.DEFAULT);
        @Nullable
        private static String scriptName;
        @Nullable
        private static Version scriptVersion;
        @Nullable
        private static String versionCheckUrl;
        @Nullable
        private static String latestVersionDownloadUrl;
        @Nullable
        private static ReleaseChannel releaseChannel;
        private static boolean isAutoInstall = true;

        private Parser() {
            throw new UnsupportedOperationException("Static class");
        }

        public static final boolean checkValid(final String key) {
            return "script name".equalsIgnoreCase(key) ||
                    "version".equalsIgnoreCase(key) ||
                    "version check url".equalsIgnoreCase(key) ||
                    "latest version download url".equalsIgnoreCase(key) ||
                    "release channel".equalsIgnoreCase(key) ||
                    "auto install".equalsIgnoreCase(key) ||
                    "updater enabled".equalsIgnoreCase(key);
        }

        public static final void clearValues() {
            scriptName = null;
            scriptVersion = null;
            versionCheckUrl = null;
            latestVersionDownloadUrl = null;
            releaseChannel = null;
            isAutoInstall = true;
        }

        public static final boolean parse(final File script,
                                          final Collection<String> duplicateCheckList,
                                          final String key,
                                          final String value) throws IOException {
            assert checkValid(key);

            if ("script name".equalsIgnoreCase(key)) {
                if (duplicateCheckList.contains("script name")) {
                    Skript.error("Duplicate script name configuration setting");
                    return false;
                }
                scriptName = value;
                duplicateCheckList.add("script name");
            } else if ("version".equalsIgnoreCase(key)) {
                if (duplicateCheckList.contains("version")) {
                    Skript.error("Duplicate version configuration setting");
                    return false;
                }
                scriptVersion = new Version(value, true);
                duplicateCheckList.add("version");
            } else if ("version check url".equalsIgnoreCase(key)) {
                if (duplicateCheckList.contains("version check url")) {
                    Skript.error("Duplicate version check url configuration setting");
                    return false;
                }
                versionCheckUrl = value;
                duplicateCheckList.add("version check url");
            } else if ("latest version download url".equalsIgnoreCase(key)) {
                if (duplicateCheckList.contains("latest version download url")) {
                    Skript.error("Duplicate latest version download url configuration setting");
                    return false;
                }
                latestVersionDownloadUrl = value.replace("\\github\\b", "https://github.com")
                        .replace("\\githubRaw\\b", "https://raw.githubusercontent.com");
                versionCheckUrl = latestVersionDownloadUrl;
                duplicateCheckList.add("latest version download url");
            } else if ("release channel".equalsIgnoreCase(key)) {
                if (duplicateCheckList.contains("release channel")) {
                    Skript.error("Duplicate release channel configuration setting");
                    return false;
                }
                final ReleaseChannel parsed = ReleaseChannel.parse(value);
                if (parsed == null) {
                    Skript.error('"' + value + "\" is not a valid release channel");
                    return false;
                }
                releaseChannel = parsed;
                duplicateCheckList.add("release channel");
            } else if ("auto install".equalsIgnoreCase(key)) {
                if (duplicateCheckList.contains("auto install")) {
                    Skript.error("Duplicate auto install configuration setting");
                    return false;
                }
                isAutoInstall = parseBoolean.test(value); // not Boolean#parseBoolean to also accept words from lang/%lang%.sk, such as 'yes' and 'no'
                duplicateCheckList.add("auto install");
            } else if ("updater enabled".equalsIgnoreCase(key)) {
                if (duplicateCheckList.contains("updater enabled")) {
                    Skript.error("Duplicate updater enabled configuration setting");
                    return false;
                }
                final boolean enabled = parseBoolean.test(value);
                if (enabled) {
                    // Eclipse forces local variables for guaranteeing non-null status
                    final String localScriptName = scriptName;
                    final Version localScriptVersion = scriptVersion;
                    final String localVersionCheckUrl = versionCheckUrl;
                    final String localLatestVersionDownloadUrl = latestVersionDownloadUrl;
                    final ReleaseChannel localReleaseChannel = releaseChannel;
                    if (localScriptName == null) {
                        Skript.error("Script name must be specified to use updater");
                        return false;
                    }
                    if (localScriptVersion == null) {
                        Skript.error("Script version must be specified to use updater");
                        return false;
                    }
                    if (localVersionCheckUrl == null) {
                        Skript.error("Version check URL must be specified to use updater");
                        return false;
                    }
                    if (localLatestVersionDownloadUrl == null) {
                        Skript.error("Latest version download link must be specified to use updater");
                        return false;
                    }
                    if (localReleaseChannel == null) {
                        Skript.error("Release channel must be specified to use updater");
                        return false;
                    }
                    new ScriptUpdater(script, localScriptName, localScriptVersion, localVersionCheckUrl, localLatestVersionDownloadUrl, true, localReleaseChannel, 1L, TimeUnit.MINUTES, isAutoInstall)
                            .registerListener()
                            .checkAndInstallUpdates(); // Initial check
                }
                duplicateCheckList.add("updater enabled");
            } else
                assert false : key;

            return true;
        }
    }

}
