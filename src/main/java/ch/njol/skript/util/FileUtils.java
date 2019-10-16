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

package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Converter;
import org.apache.commons.lang.time.FastDateFormat;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * @author Peter Güttinger
 */
public final class FileUtils {

    private static final FastDateFormat backupFormat = FastDateFormat.getInstance("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);

    private FileUtils() {
        throw new UnsupportedOperationException("Static class");
    }

    /**
     * @return The current date and time
     */
    public static final String getBackupSuffix() {
        return backupFormat.format(System.currentTimeMillis());
    }

    /**
     * @return null when backups are disabled from config
     */
    @Nullable
    public static final File backup(final File f) throws IOException {
        if (Boolean.getBoolean("skript.disableBackupsCompletely"))
            return null;
        String name = f.getName();
        final int c = name.lastIndexOf('.');
        final String ext = c == -1 ? null : name.substring(c + 1);
        if (c != -1)
            name = name.substring(0, c);
        final File backupFolder = new File(Skript.getInstance().getDataFolder(), "backups" + File.separator);
        if (!backupFolder.exists() && !backupFolder.mkdirs())
            throw new IOException("Cannot create backups folder");
        final File backup = new File(backupFolder, name + '_' + getBackupSuffix() + (ext == null ? "" : '.' + ext));
        if (backup.exists())
            throw new IOException("Backup file " + backup.getName() + " does already exist");
        copy(f, backup);
        return backup;
    }

    public static final File move(final File from, final File to, final boolean replace) throws IOException {
        if (!replace && to.exists())
            throw new IOException("Can't rename " + from.getName() + " to " + to.getName() + ": The target file already exists");
        if (replace)
            Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        else
            Files.move(from.toPath(), to.toPath(), StandardCopyOption.ATOMIC_MOVE);
        return to;
    }

    public static final void copy(final File from, final File to) throws IOException {
        Files.copy(from.toPath(), to.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    }

    /**
     * @param directory
     * @param renamer   Renames files. Return null to leave a file as-is.
     * @return A collection of all changed files (with their new names)
     * @throws IOException If renaming one of the files caused an IOException. Some files might have been renamed already.
     */
    public static final Collection<File> renameAll(final File directory, final Converter<String, String> renamer) throws IOException {
        final Collection<File> changed = new ArrayList<>();
        final File[] files = directory.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.isDirectory()) {
                    changed.addAll(renameAll(f, renamer));
                } else {
                    final String name = f.getName();
                    if (name == null)
                        continue;
                    final String newName = renamer.convert(name);
                    if (newName == null)
                        continue;
                    final File newFile = new File(f.getParent(), newName);
                    move(f, newFile, false);
                    changed.add(newFile);
                }
            }
        }
        return changed;
    }

    /**
     * Saves the contents of an InputStream in a file.
     *
     * @param in   The InputStream to read from. This stream will not be closed when this method returns.
     * @param file The file to save to. Will be replaced if it exists, or created if it doesn't.
     * @throws IOException
     */
    public static final void save(final InputStream in, final File file) throws IOException {
        file.getParentFile().mkdirs();
        try (final FileOutputStream out = new FileOutputStream(file)) {
            final byte[] buffer = new byte[16 << 10];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
        }
    }

}
