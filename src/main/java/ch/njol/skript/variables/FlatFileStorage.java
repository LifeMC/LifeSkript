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
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.*;
import ch.njol.util.NotifyingReference;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO use a database (SQLite) instead and only load a limited amount of variables into RAM - e.g. 2 GB (configurable). If more variables are available they will be loaded when
 * accessed. (rem: print a warning when Skript starts)
 * rem: store null variables (in memory) to prevent looking up the same variables over and over again
 *
 * @author Peter Güttinger
 */
public final class FlatFileStorage extends VariablesStorage {

    /**
     * @see StandardCharsets#UTF_8
     * @deprecated Use {@link StandardCharsets#UTF_8}
     */
    @Deprecated
    @SuppressWarnings("null")
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    public static final int REQUIRED_CHANGES_FOR_RESAVE = Integer.getInteger("skript.requiredVariableChangesForSave", 1000);
    @SuppressWarnings("null")
    private static final Pattern csv = Pattern.compile("(?<=^|,)\\s*?([^\",]*?|\"([^\"]|\"\")*?\")\\s*?(,|$)");
    private static final Matcher csvMatcher = csv.matcher("");
    /**
     * Use with find()
     */
    @SuppressWarnings("null")
    private static final Pattern containsWhitespace = Pattern.compile("\\s");
    private static final Matcher containsWhitespaceMatcher = containsWhitespace.matcher("");
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\"\"", Pattern.LITERAL);
    private static final Matcher SPLIT_PATTERN_MATCHER = SPLIT_PATTERN.matcher("");
    private static final Pattern SINGLE_QUOTE = Pattern.compile("\"", Pattern.LITERAL);
    private static final Matcher SINGLE_QUOTE_MATCHER = SINGLE_QUOTE.matcher("");
    static boolean savingVariables;
    private static long savedVariables;
    @Nullable
    private static Date lastSave;
    final AtomicInteger changes = new AtomicInteger();
    /**
     * A Lock on this object must be acquired after connectionLock (if that lock is used) (and thus also after {@link Variables#getReadLock()}).
     */
    private final NotifyingReference<PrintWriter> changesWriter = new NotifyingReference<>();
    private volatile boolean loaded;
    @Nullable
    private Task saveTask;
    private boolean loadError;

    FlatFileStorage(final String name) {
        super(name);
    }

    static final String encode(final byte[] data) {
        final char[] r = new char[data.length << 1];
        for (int i = 0; i < data.length; i++) {
            r[2 * i] = Character.toUpperCase(Character.forDigit((data[i] & 0xF0) >>> 4, 16));
            r[2 * i + 1] = Character.toUpperCase(Character.forDigit(data[i] & 0xF, 16));
        }
        return new String(r);
    }

    private static final byte[] decode(final CharSequence hex) {
        final byte[] r = new byte[hex.length() / 2];
        for (int i = 0; i < r.length; i++) {
            r[i] = (byte) ((Character.digit(hex.charAt(2 * i), 16) << 4) + Character.digit(hex.charAt(2 * i + 1), 16));
        }
        return r;
    }

    @Nullable
    private static final String[] splitCSV(final CharSequence line) {
        final Matcher m = csvMatcher.reset(line);
        int lastEnd = 0;
        final ArrayList<String> r = new ArrayList<>();
        while (m.find()) {
            if (lastEnd != m.start())
                return null;
            final String v = m.group(1);
            if (!v.isEmpty() && v.charAt(0) == '\"')
                r.add(SPLIT_PATTERN_MATCHER.reset(v.substring(1, v.length() - 1)).replaceAll(Matcher.quoteReplacement("\"")));
            else
                r.add(v.trim());
            lastEnd = m.end();
        }
        if (lastEnd != line.length())
            return null;
        return r.toArray(EmptyArrays.EMPTY_STRING_ARRAY);
    }

    private static final void writeCSV(final PrintWriter pw, final String... values) {
        assert values.length == 3; // name, type, value
        for (int i = 0; i < values.length; i++) {
            if (i != 0)
                pw.print(", ");
            String v = values[i];
            if (v != null && (v.contains(",") || v.contains("\"") || v.contains("#") || containsWhitespaceMatcher.reset(v).find()))
                v = '"' + SINGLE_QUOTE_MATCHER.reset(v).replaceAll(Matcher.quoteReplacement("\"\"")) + '"';
            pw.print(v);
        }
        pw.println();
    }

    /**
     * Doesn't lock the connection as required by {@link Variables#variableLoaded(String, Object, VariablesStorage)}.
     */
    @SuppressWarnings({"deprecation", "unused", "null"})
    @Override
    protected final boolean load_i(final SectionNode n) {
        SkriptLogger.setNode(null);

        IOException ioEx = null;
        int unsuccessful = 0;
        final StringBuilder invalid = new StringBuilder(4096);

        final Version v2_0_beta3 = new Version(2, 0, "beta 3");
        final Version v2_1 = new Version(2, 1);
        boolean update2_1 = false;

        try (final BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(Objects.requireNonNull(file))), StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            boolean update2_0_beta3 = false;
            while ((line = r.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#') {
                    if (line.startsWith("# version:")) {
                        try {
                            // will be set later
                            final Version varVersion = new Version(line.substring("# version:".length()).trim());
                            update2_0_beta3 = varVersion.isSmallerThan(v2_0_beta3);
                            update2_1 = varVersion.isSmallerThan(v2_1);
                        } catch (final IllegalArgumentException e) {
                            if (Skript.testing() || Skript.debug())
                                Skript.exception(e);
                        }
                    }
                    continue;
                }
                final String[] split = splitCSV(line);
                if (split == null || split.length != 3) {
                    Skript.error("invalid amount of commas in line " + lineNum + " ('" + line + "')");
                    if (invalid.length() != 0)
                        invalid.append(", ");
                    invalid.append(split == null ? "<unknown>" : split[0]);
                    unsuccessful++;
                    continue;
                }
                if ("null".equals(split[1])) {
                    Variables.variableLoaded(split[0], null, this);
                } else {
                    Object d;
                    if (update2_1)
                        d = Classes.deserialize(split[1], split[2]);
                    else
                        d = Classes.deserialize(split[1], decode(split[2]));
                    if (d == null) {
                        if (invalid.length() != 0)
                            invalid.append(", ");
                        invalid.append(split[0]);
                        unsuccessful++;
                        continue;
                    }
                    if (d instanceof String && update2_0_beta3) {
                        d = Utils.replaceChatStyles((String) d);
                    }
                    Variables.variableLoaded(split[0], d, this);
                }
            }
        } catch (final IOException e) {
            loadError = true;
            ioEx = e;
        }

        final File file = this.file;
        if (file == null) {
            assert false : this;
            return false;
        }

        if (ioEx != null || unsuccessful > 0 || update2_1) {
            if (unsuccessful > 0) {
                Skript.error(unsuccessful + " variable" + (unsuccessful == 1 ? "" : "s") + " could not be loaded!");
                Skript.error("Affected variables: " + invalid);
            }
            if (ioEx != null) {
                Skript.error("An I/O error occurred while loading the variables: " + ExceptionUtils.toString(ioEx));
                Skript.error("This means that some to all variables could not be loaded!");
            }
            try {
                if (update2_1) {
                    Skript.info("[2.1] updating " + file.getName() + " to the new format...");
                }
                final File bu = FileUtils.backup(file);
                if (bu != null)
                    Skript.info("Created a backup of " + file.getName() + " as " + bu.getName());
                loadError = false;
            } catch (final IOException ex) {
                Skript.error("Could not backup " + file.getName() + ": " + ex.getMessage());
            }
        }

        if (update2_1) {
            saveVariables(false);
            Skript.info(file.getName() + " successfully updated.");
        }

        connect();

        saveTask = new Task(Skript.getInstance(), 5 * 60 * 20, 5 * 60 * 20, true) {
            @Override
            public final void run() {
                if (changes.get() >= REQUIRED_CHANGES_FOR_RESAVE && !savingVariables) {
                    saveVariables(false);
                    changes.set(0);
                }
            }
        };

        return ioEx == null;
    }

    @Override
    protected final void allLoaded() {
        // no transaction support
    }

    @Override
    protected final boolean requiresFile() {
        return true;
    }

    @Override
    protected final File getFile(final String file) {
        return new File(file);
    }

    @SuppressWarnings({"resource", "unused", "null"})
    @Override
    protected final boolean save(final String name, @Nullable final String type, @Nullable final byte[] value) {
        synchronized (connectionLock) {
            synchronized (changesWriter) {
                if (!loaded && type == null)
                    return true; // deleting variables is not really required for this kind of storage, as it will be completely rewritten every once in a while, and at least once when the server stops.
                PrintWriter cw;
                while ((cw = changesWriter.get()) == null) {
                    try {
                        changesWriter.wait(); //FIXME sonarlint blocker issue
                    } catch (final InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
                writeCSV(cw, name, type, value == null ? "" : encode(value));
                cw.flush();
                changes.incrementAndGet();
                return true;
            }
        }
    }

    @SuppressWarnings("null")
    @Override
    protected final void disconnect() {
        synchronized (connectionLock) {
            clearChangesQueue();
            synchronized (changesWriter) {
                try (final PrintWriter cw = changesWriter.get()) {
                    changesWriter.set(null);
                }
            }
        }
    }

    @SuppressWarnings({"unused", "null", "resource"})
    @Override
    protected final boolean connect() {
        synchronized (connectionLock) {
            synchronized (changesWriter) {
                if (changesWriter.get() != null)
                    return true;
                try {
                    changesWriter.set(new PrintWriter(new OutputStreamWriter(new FileOutputStream(Objects.requireNonNull(file), true), StandardCharsets.UTF_8)));
                    loaded = true;
                    return true;
                } catch (final FileNotFoundException e) {
                    Skript.exception(e);
                    return false;
                }
            }
        }
    }

    @Override
    public final void close() {
        clearChangesQueue();
        super.close();
        saveVariables(true); // also closes the writer
    }

    /**
     * Completely rewrites the whole file
     *
     * @param finalSave whatever this is the last save in this session or not.
     */
    @SuppressWarnings({"null", "unused"})
    public final void saveVariables(final boolean finalSave) {
        if (finalSave) {
            final Task st = saveTask;
            if (st != null)
                st.cancel();
            final Task bt = backupTask;
            if (bt != null)
                bt.cancel();
        } else if (lastSave != null && TimeUnit.MILLISECONDS.toMinutes(lastSave.difference(new Date()).getMilliSeconds()) < 1L) {
            if (Skript.debug())
                Skript.debug("Skipping save of variables, the last save happened on " + lastSave);
            return; // Skip, this not the final (the one in the shutdown) save and last save is happened <= 1 minutes ago.
        } else if (!Skript.isSkriptRunning())
            return; // Prevent multiple saves when shutting down - it may or may not cause issues but anyway.
        try {
            Variables.getReadLock().lock();
            synchronized (connectionLock) {
                try {
                    final File f = file;
                    if (f == null) {
                        assert false : this;
                        return;
                    }

                    disconnect();

                    if (loadError) {
                        try {
                            final File backup = FileUtils.backup(f);
                            if (backup != null)
                                Skript.info("Created a backup of the old " + f.getName() + " as " + backup.getName());
                            loadError = false;
                        } catch (final IOException e) {
                            Skript.error("Could not backup the old " + f.getName() + ": " + ExceptionUtils.toString(e));
                            Skript.error("No variables are saved!");
                            return;
                        }
                    }

                    final File tempFile = new File(Skript.getInstance().getDataFolder(), "variables.csv.temp");

                    try (final PrintWriter pw = new PrintWriter(tempFile, "UTF-8")) {
                        pw.println("# === Skript's variable storage ===");
                        pw.println("# Please do not modify this file manually!");
                        pw.println("#");
                        pw.println("# version: " + Skript.getVersion());
                        pw.println();

                        if (finalSave)
                            SkriptCommand.setPriority();

                        final Date start = new Date();
                        lastSave = start;

                        savedVariables = 0; // Method may be called multiple times
                        savingVariables = true;

                        final TreeMap<String, Object> variables = Variables.getVariables();

                        final int count = variables.size();
                        final String fileName = file.getName();

                        if (Skript.logHigh())
                            // Unfortunately, this only displays the non-list variable counts.
                            Skript.info("Saving approximately " + count + " variables to '" + fileName + '\'');

                        // reports once per second how many variables were saved. Useful to make clear that Skript is still doing something if it's saving many variables
                        final Thread savingLoggerThread = Skript.newThread(() -> {
                            while (savingVariables) {
                                try {
                                    Thread.sleep(Skript.logVeryHigh() ? 3000L : Skript.logHigh() ? 5000L : Skript.logNormal() ? 10000L : 15000L); // low verbosity won't disable these messages, but makes them more rare
                                } catch (final InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    return;
                                }
                                if (savedVariables == 0 && !Skript.debug() && !Skript.testing())
                                    continue;
                                Skript.info("Saved " + savedVariables + " variables" + (Skript.logHigh() ? " to '" + fileName + '\'' : "") + " so far...");
                            }
                            Thread.currentThread().interrupt();
                        }, "Skript variable save tracker thread");

                        savingLoggerThread.setPriority(Thread.MIN_PRIORITY);
                        savingLoggerThread.setDaemon(true);

                        if (finalSave || Skript.logVeryHigh()) {
                            savingLoggerThread.start();
                        }

                        save(pw, "", variables);

                        savingVariables = false;

                        savingLoggerThread.interrupt(); // In case if not interrupted
                        Skript.info("Saved total of " + savedVariables + " variables" + (Skript.logNormal() ? " in " + start.difference(new Date()) : "") + (Skript.logHigh() ? " to '" + fileName + '\'' : ""));

                        savedVariables = 0; // Method may be called multiple times

                        pw.println();

                        pw.flush();
                        pw.close();

                        FileUtils.move(tempFile, f, true);

                        if (finalSave)
                            SkriptCommand.resetPriority();

                    } catch (final IOException e) {
                        Skript.error("Unable to make a final save of the database '" + databaseName + "' (no variables are lost): " + ExceptionUtils.toString(e));
                    }
                } finally {
                    if (!finalSave) {
                        connect();
                    }
                }
            }
        } finally {
            Variables.getReadLock().unlock();
            final boolean gotLock = Variables.variablesLock.writeLock().tryLock();
            if (gotLock) { // Only process queue now if it doesn't require us to wait
                try {
                    Variables.processChangeQueue();
                } finally {
                    Variables.variablesLock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * Saves the variables.
     * <p>
     * This method uses the sorted variables map to save the variables in order.
     *
     * @param pw
     * @param parent The parent's name with {@link Variable#SEPARATOR} at the end
     * @param map
     */
    @SuppressWarnings({"unchecked", "null"})
    private final void save(final PrintWriter pw, final String parent, final TreeMap<String, Object> map) {
        outer:
        for (final Entry<String, Object> e : map.entrySet()) {
            final Object val = e.getValue();
            if (val == null)
                continue;
            if (val instanceof TreeMap) {
                save(pw, parent + e.getKey() + Variable.SEPARATOR, (TreeMap<String, Object>) val);
            } else {
                final String name = e.getKey() == null ? parent.substring(0, parent.length() - Variable.SEPARATOR.length()) : parent + e.getKey();
                for (final VariablesStorage s : Variables.storages) {
                    if (s.accept(name)) {
                        if (s == this) {
                            final SerializedVariable.Value value = Classes.serialize(val);
                            if (value != null) {
                                writeCSV(pw, name, value.type, encode(value.data));
                                savedVariables++;
                            }
                        }
                        continue outer;
                    }
                }
            }
        }
    }

}
