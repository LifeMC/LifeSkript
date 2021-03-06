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

package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.*;
import ch.njol.skript.variables.SerializedVariable.Value;
import ch.njol.util.Closeable;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

// FIXME ! large databases (>25 MB) cause the server to be unresponsive instead of loading slowly

/*
    More details about the slow loading / saving issue

    The loading is not slow, but maybe setting variables
    after load can be slow. I tested with > 60MB variables.csv file

    and it loaded in like 10~ seconds, setted variables and fully
    finished in 24~ seconds.

    But saving is a horrible mess. It takes 5 minutes to save my
    720.000~ variables.

    It is mostly caused by recursive list variables.
 */

/**
 * @author Peter Güttinger
 */
public abstract class VariablesStorage implements Closeable {

    private static final int QUEUE_SIZE = 1000, FIRST_WARNING = 300;
    private static final int WARNING_INTERVAL = 10;
    private static final int ERROR_INTERVAL = 10;
    @Nullable
    public static VariablesStorage instance;
    protected final String databaseName;
    /**
     * Must be locked after {@link Variables#getReadLock()} (if that lock is used at all)
     */
    protected final byte[] connectionLock = EmptyArrays.EMPTY_BYTE_ARRAY;
    final LinkedBlockingQueue<SerializedVariable> changesQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);
    // created in the constructor, started in load()
    private final Thread writeThread;
    protected volatile boolean closed;
    @Nullable
    protected File file;
    @Nullable
    protected Task backupTask;
    /**
     * null for '.*' or '.+'
     */
    @Nullable
    private Matcher variablePatternMatcher;
    private long lastWarning = Long.MIN_VALUE;
    private long lastError = Long.MIN_VALUE;

    protected VariablesStorage(final String name) {
        instance = this;

        databaseName = name;
        writeThread = Skript.newThread(() -> {
            while (!closed) {
                try {
                    final SerializedVariable var = changesQueue.take();
                    final Value d = var.value;
                    if (d != null)
                        save(var.name, d.type, d.data);
                    else
                        save(var.name, null, null);
                } catch (final InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break; // Server probably shutting down.
                }
            }
        }, "Skript variable save thread for database '" + name + '\'');
    }

    @Nullable
    protected final String getValue(final SectionNode n, final String key) {
        return getValue(n, key, String.class);
    }

    @Nullable
    protected final <T> T getValue(final SectionNode n, final String key, final Class<T> type) {
        final String v = n.getValue(key);
        if (v == null) {
            Skript.error("The config is missing the entry for '" + key + "' in the database '" + databaseName + '\'');
            return null;
        }
        final ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            final T r = Classes.parse(v, type, ParseContext.CONFIG);
            if (r == null)
                log.printError("The entry for '" + key + "' in the database '" + databaseName + "' must be " + Classes.getSuperClassInfo(type).getName().withIndefiniteArticle());
            else
                log.printLog();
            return r;
        } finally {
            log.stop();
        }
    }

    public final boolean load(final SectionNode n) {
        final String pattern = getValue(n, "pattern");
        if (pattern == null)
            return false;
        try {
            final Pattern variablePattern = ".*".equals(pattern) || ".+".equals(pattern) ? null : PatternCache.get(pattern);
            if (variablePattern != null)
                variablePatternMatcher = variablePattern.matcher("");
        } catch (final PatternSyntaxException e) {
            Skript.error("Invalid pattern '" + pattern + "': " + e.getLocalizedMessage());
            return false;
        }

        if (requiresFile()) {
            final String f = getValue(n, "file");
            if (f == null)
                return false;
            final File file = getFile(f).getAbsoluteFile();
            this.file = file;
            if (file.exists() && !file.isFile()) {
                Skript.error("The database file '" + file.getName() + "' must be an actual file, not a directory.");
                return false;
            }
            try {
                file.createNewFile();
            } catch (final IOException e) {
                Skript.error("Cannot create the database file '" + file.getName() + "': " + e.getLocalizedMessage());
                return false;
            }
            if (!file.canWrite()) {
                Skript.error("Cannot write to the database file '" + file.getName() + "'!");
                return false;
            }
            if (!file.canRead()) {
                Skript.error("Cannot read from the database file '" + file.getName() + "'!");
                Skript.error("This means that no variables will be available and can also prevent new variables from being saved!");
                try {
                    final File backup = FileUtils.backup(file);
                    if (backup != null)
                        Skript.error("A backup of your variables.csv was created as " + backup.getName());
                } catch (final IOException e) {
                    Skript.error("Failed to create a backup of your variables.csv: " + e.getLocalizedMessage());
                }
                return false;
            }

            if (!"0".equals(getValue(n, "backup interval"))) {
                final Timespan backupInterval = getValue(n, "backup interval", Timespan.class);
                if (backupInterval != null && TimeUnit.MILLISECONDS.toSeconds(backupInterval.getMilliSeconds()) >= 1L)
                    startBackupTask(backupInterval);
            }
        }

        if (!load_i(n))
            return false;

        writeThread.start();
        //Skript.closeOnDisable(this);

        return true;
    }

    /**
     * Loads variables stored here.
     *
     * @return Whatever the database could be loaded successfully, i.e. whatever the config is correct and all variables could be loaded
     */
    protected abstract boolean load_i(final SectionNode n);

    /**
     * Called after all storages have been loaded, and variables have been redistributed if settings have changed. This should commit the first transaction (which is not empty if
     * variables have been moved from another database to this one or vice versa), and start repeating transactions if applicable.
     */
    protected abstract void allLoaded();

    protected abstract boolean requiresFile();

    protected abstract File getFile(final String file);

    /**
     * (Re)connects to the database (not called on the first connect - do this in {@link #load_i(SectionNode)}).
     *
     * @return Whatever the connection could be re-established. An error should be printed by this method prior to returning false.
     */
    protected abstract boolean connect();

    /**
     * Disconnects from the database.
     */
    protected abstract void disconnect();

    public final void startBackupTask(final Timespan t) {
        final File file = this.file;
        if (file == null || t.getTicks_i() == 0)
            return;
        backupTask = new Task(Skript.getInstance(), t.getTicks_i(), t.getTicks_i(), true) {
            @Override
            public void run() {
                synchronized (connectionLock) {
                    disconnect();
                    try {
                        FileUtils.backup(file);
                    } catch (final IOException e) {
                        Skript.error("Automatic variables backup failed: " + e.getLocalizedMessage());
                    } finally {
                        connect();
                    }
                }
            }
        };
    }

    @SuppressWarnings("null")
    boolean accept(@Nullable final String var) {
        if (var == null)
            return false;
        return variablePatternMatcher == null || variablePatternMatcher.reset(var).matches();
    }

    /**
     * May be called from a different thread than Bukkit's main thread.
     */
    final void save(final SerializedVariable var) {
        if (changesQueue.size() > FIRST_WARNING && lastWarning < System.currentTimeMillis() - WARNING_INTERVAL * 1000) {
            Skript.warning("Cannot write variables to the database '" + databaseName + "' at sufficient speed; server performance may suffer and many variables will be lost if the server crashes. (this warning will be repeated at most once every " + WARNING_INTERVAL + " seconds)");
            lastWarning = System.currentTimeMillis();
        }
        if (!changesQueue.offer(var)) {
            if (lastError < System.currentTimeMillis() - ERROR_INTERVAL * 1000) {
                Skript.error("Skript cannot save any variables to the database '" + databaseName + "'. The server will hang and may crash if no more variables can be saved.");
                lastError = System.currentTimeMillis();
            }
            try {
                changesQueue.put(var);
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Called when Skript gets disabled. The default implementation will wait for all variables to be saved before setting {@link #closed} to true and stopping the write thread,
     * thus <tt>super.close()</tt> must be called if this method is overridden!
     */
    @Override
    public void close() {
        while (!changesQueue.isEmpty()) {
            try {
                Thread.sleep(10L);
            } catch (final InterruptedException ignored) {
                closed = true;
                writeThread.interrupt();

                Thread.currentThread().interrupt();
                return; // Assume all variables are saved
            }
        }
    }

    /**
     * Clears the queue of unsaved variables. Only used if all variables are saved immediately after calling this method.
     */
    protected final void clearChangesQueue() {
        changesQueue.clear();
    }

    /**
     * Saves a variable. This is called from the main thread while variables are transferred between databases, and from the {@link #writeThread} afterwards.
     *
     * @param name
     * @param type
     * @param value
     * @return Whatever the variable was saved
     */
    protected abstract boolean save(String name, @Nullable String type, @Nullable byte[] value);

}
