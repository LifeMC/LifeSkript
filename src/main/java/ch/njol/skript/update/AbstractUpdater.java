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

import ch.njol.skript.Skript;
import ch.njol.skript.update.addon.AddonUpdater;
import ch.njol.skript.update.script.ScriptUpdater;
import ch.njol.skript.update.skript.SkriptUpdater;
import ch.njol.skript.util.EmptyStacktraceException;
import ch.njol.skript.util.Version;
import ch.njol.util.LineSeparators;
import ch.njol.util.NonNullPair;
import ch.njol.util.WebUtils;
import ch.njol.util.misc.Versionable;
import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A pre-made abstract implementation of {@link Updater}.
 * <p>
 * You must still override the {@link Updater#installRelease(Release)},
 * {@link Updater#getLatestRelease()} and {@link Updater#getCurrentRelease()} methods.<p />
 * <p>
 * <p />This class also contains various utility methods dealing with updater stuff,
 * for examples you can view the implementations.
 *
 * @see SkriptUpdater
 * @see ScriptUpdater
 * @see AddonUpdater
 */
public abstract class AbstractUpdater implements Updater {

    private static final Pattern VERSION_PATTERN = Pattern.compile("version: ");
    private static final Pattern ALTERNATIVE_VERSION_PATTERN = Pattern.compile("version=");

    private static final Pattern UNIX_NEW_LINE = Pattern.compile(LineSeparators.UNIX, Pattern.LITERAL);
    private static final Matcher UNIX_NEW_LINE_MATCHER = UNIX_NEW_LINE.matcher("");

    private static final Matcher EXPR_VERSION_PATTERN = Pattern.compile("%version%", Pattern.LITERAL).matcher("");
    private static final Matcher SINGLE_COMMENT_PATTERN_MATCHER = Pattern.compile("#", Pattern.LITERAL).matcher("");

    private static final Matcher WINDOWS_NEW_LINE = Pattern.compile(LineSeparators.DOS, Pattern.LITERAL).matcher("");
    private static final Matcher MAC_NEW_LINE = Pattern.compile(LineSeparators.MAC, Pattern.LITERAL).matcher("");

    private static final Matcher UNSPACED_VERSION_PATTERN = Pattern.compile("version:", Pattern.LITERAL).matcher("");
    private static final Matcher SPACED_VERSION_PATTERN = Pattern.compile("version = ", Pattern.LITERAL).matcher("");

    private static final Matcher SINGLE_QUOTE_PATTERN = Pattern.compile("'", Pattern.LITERAL).matcher("");
    private static final Matcher DOUBLE_QUOTE_PATTERN = Pattern.compile("\"", Pattern.LITERAL).matcher("");

    private static final String EMPTY_STRING_REGEX_READY = Matcher.quoteReplacement("");
    protected UpdaterState state;
    protected ReleaseChannel channel;
    private boolean enabled;
    @Nullable
    private SoftReference<Throwable> lastError;
    private long frequency;
    private TimeUnit unit;
    private int taskId = -1;

    protected AbstractUpdater(final boolean enabled,
                              final ReleaseChannel channel,
                              final long frequency,
                              final TimeUnit unit) {
        this.enabled = enabled;
        state = UpdaterState.NOT_STARTED;

        this.channel = channel;

        this.frequency = frequency;
        this.unit = unit;
    }

    private static final String stripLineEndings(final CharSequence s) {
        return UNIX_NEW_LINE_MATCHER.reset(MAC_NEW_LINE.reset(WINDOWS_NEW_LINE.reset(s).replaceAll("")).replaceAll("")).replaceAll("");
    }

    @Nullable
    private static final Version getVersionOfUrl(final String versionCheckUrl) throws IOException {
        return getVersion(WebUtils.getResponse(versionCheckUrl));
    }

    @Nullable
    public static final Version getVersion(@Nullable final String versionText) {
        if (versionText == null)
            return null;

        final Version version = getVersionFromLines(versionText);
        if (version != null)
            return version;

        final String uncommonVersionText = SINGLE_COMMENT_PATTERN_MATCHER
                .reset(versionText.trim().toLowerCase(Locale.ENGLISH))
                .replaceAll(Matcher.quoteReplacement(""));

        final Version uncommonVersion = getVersionFromLines(uncommonVersionText);
        if (uncommonVersion != null)
            return uncommonVersion;

        try {
            return new Version(uncommonVersionText);
        } catch (final IllegalArgumentException e) {
            return new Version(uncommonVersionText, true);
        }
    }

    @Nullable
    private static final Version getVersionFromLines(final CharSequence versionText) {
        final Version unixLine = getVersionFromLines(versionText, UNIX_NEW_LINE);
        if (unixLine != null)
            return unixLine;

        return getVersionFromLines(versionText, MAC_NEW_LINE.pattern());
    }

    @Nullable
    private static final Version getVersionFromLines(final CharSequence versionText,
                                                     final Pattern lineDeterminer) {
        for (final String line : lineDeterminer.split(versionText)) {
            final String trimmedLine = stripLineEndings(line.trim());

            if (trimmedLine.startsWith("version = "))
                return versionFromPattern(ALTERNATIVE_VERSION_PATTERN, SPACED_VERSION_PATTERN.reset(trimmedLine).replaceAll(Matcher.quoteReplacement("version=")));
            if (trimmedLine.startsWith("version: "))
                return versionFromPattern(VERSION_PATTERN, trimmedLine);

            if (trimmedLine.startsWith("version="))
                return versionFromPattern(ALTERNATIVE_VERSION_PATTERN, trimmedLine);
            if (trimmedLine.startsWith("version:"))
                return versionFromPattern(VERSION_PATTERN, UNSPACED_VERSION_PATTERN.reset(trimmedLine).replaceAll(Matcher.quoteReplacement("version: ")));
        }

        return null;
    }

    private static final Version versionFromPattern(final Pattern pattern,
                                                    final CharSequence versionText) {
        return new Version(SINGLE_QUOTE_PATTERN.reset(DOUBLE_QUOTE_PATTERN.reset(MAC_NEW_LINE.reset(WINDOWS_NEW_LINE.reset(pattern.split(versionText)[1]).replaceAll(Matcher.quoteReplacement(LineSeparators.UNIX))).replaceAll(Matcher.quoteReplacement(LineSeparators.UNIX)).split(LineSeparators.UNIX)[0]).replaceAll(EMPTY_STRING_REGEX_READY)).replaceAll(EMPTY_STRING_REGEX_READY).trim());
    }

    public static final String replaceVersionInDownloadUrl(final Versionable versionable,
                                                           final CharSequence downloadUrl) {
        return EXPR_VERSION_PATTERN.reset(downloadUrl).replaceAll(Matcher.quoteReplacement(versionable.getVersion().toString()));
    }

    protected final Executor getUpdateCheckerExecutor(final String threadName) {
        return Executors.newFixedThreadPool(1, r -> {
            final Thread thread = Skript.newThread(r, threadName);
            thread.setPriority(Thread.MIN_PRIORITY);

            final Thread.UncaughtExceptionHandler oldHandler = thread.getUncaughtExceptionHandler();
            thread.setUncaughtExceptionHandler((t, e) -> {
                state = UpdaterState.ERROR;
                lastError = new SoftReference<>(e);

                oldHandler.uncaughtException(t, e);
            });

            return thread;
        });
    }

    protected final RuntimeException handleError(@Nullable final Throwable error,
                                                 @Nullable String... info) {
        if (error instanceof EmptyStacktraceException || error != null && error.getCause() instanceof EmptyStacktraceException)
            throw new EmptyStacktraceException();

        state = UpdaterState.ERROR;
        lastError = new SoftReference<>(error);

        if (info == null || info.length < 1)
            info = new String[]{"Error occurred in the updater of " + getName()};

        return Skript.exception(error, info);
    }

    private final RuntimeException handleNoInternetConnection(final IOException e,
                                                              final String versionCheckUrl) {
        assert e instanceof UnknownHostException || e instanceof SocketTimeoutException : e.getClass();
        if (Skript.testing() || Skript.logHigh())
            return handleError(e, versionCheckUrl);
        return new EmptyStacktraceException(); // Ignore the error, no internet connection
    }

    protected final Version getVersionFromUrl(final String versionCheckUrl) {
        final Version latestVersion;
        try {
            latestVersion = AbstractUpdater.getVersionOfUrl(versionCheckUrl);
        } catch (final UnknownHostException | SocketTimeoutException e) {
            throw handleNoInternetConnection(e, versionCheckUrl);
        } catch (final IOException e) {
            throw handleError(e, "Can't check updates of " + getName() + " from " + versionCheckUrl);
        }
        if (latestVersion == null) {
            throw handleError(new IllegalStateException("null latestVersion"));
        }
        return latestVersion;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        if (!enabled)
            if (taskId != -1)
                Bukkit.getScheduler().cancelTask(taskId);
            else
                registerListener();
    }

    @Override
    public final UpdaterState getState() {
        return state;
    }

    /**
     * Returns the last (managed) error encountered in the
     * updater.
     *
     * @return The last managed error encountered in the updater.
     */
    @Nullable
    public final SoftReference<Throwable> getLastError() {
        return lastError;
    }

    @Override
    public ReleaseChannel getReleaseChannel() {
        return channel;
    }

    @Override
    public void setReleaseChannel(final ReleaseChannel releaseChannel) {
        channel = releaseChannel;
    }

    @Override
    public NonNullPair<Long, TimeUnit> getCheckFrequency() {
        return new NonNullPair<>(frequency, unit);
    }

    @Override
    public final void setCheckFrequency(final long frequency, final TimeUnit unit) {
        this.frequency = frequency;
        this.unit = unit;
    }

    @Override
    public final <T extends Updater> T checkAndInstallUpdates(@Nullable final Consumer<T> callback) throws IOException {
        final T instance = (T) this;
        if (!enabled)
            return instance;
        getLatestRelease().whenComplete((latest, e) -> {
            final Runnable runCallback = () -> {
                if (callback != null)
                    callback.accept(instance);
            };
            if (e != null) {
                runCallback.run();

                throw handleError(e);
            }
            if (latest == null) {
                runCallback.run();

                return;
            }
            final Release current = getCurrentRelease();
            if (!latest.isInstalled() && latest.getState() == ReleaseStatus.LATEST && latest.getVersion().isLargerThan(current.getVersion()) && latest.getReleaseChannel() == current.getReleaseChannel()) {
                if (isAutoInstallEnabled()) {
                    Skript.info("Updating " + getName() + " from the version " + current.getVersion() + " to " + latest.getVersion());
                    try {
                        installRelease(latest);
                    } catch (final InstalledReleaseException | IOException ex) {
                        runCallback.run();

                        throw handleError(ex);
                    }
                    final String successfullyUpdated = "Successfully updated the " + getName() + " from the version " + current.getVersion() + " to " + latest.getVersion();
                    switch (state) {
                        case NOT_STARTED:
                        case CHECKING:
                        case PENDING_DOWNLOAD:
                        case DOWNLOADING:
                            //$FALL-THROUGH$
                        case INSTALLING: // Something bad happened
                            assert false : state;
                            break;
                        case PENDING_RESTART: // Update is waiting for a restart to fully complete
                            Skript.info(successfullyUpdated + ". A restart is required to complete install.");
                            break;
                        case UPDATED: // Update is immediately applied
                            Skript.info(successfullyUpdated);
                            break;
                        case ERROR: // Error occurred when installing the update
                            Skript.error("Error occurred when installing updated version " + latest.getVersion() + " of " + getName() + " version " + current.getVersion());
                            break;
                    }
                } else { // Automatic installing of the new updates are disabled
                    Skript.warning("There is a new version available for " + getName() + ". The new version is " + latest.getVersion() + ". You are running " + current.getVersion() + '.');
                }
                setEnabled(false); // Updater is done its work
            }
            runCallback.run();
        });
        return instance;
    }

    @Override
    public final <T extends Updater> T registerListener() {
        final T instance = (T) this;
        if (!enabled)
            return instance;

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        final long ticks = unit.toMillis(frequency) / 50;

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), () -> {
            try {
                checkAndInstallUpdates();
            } catch (final IOException e) {
                Skript.exception(e);
            }
        }, ticks, ticks);

        return instance;
    }

}
