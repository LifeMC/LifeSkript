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

package ch.njol.skript.config;

import org.eclipse.jdt.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Peter Güttinger
 */
public final class ConfigReader extends BufferedReader {

    /**
     * @see StandardCharsets
     * @deprecated Use {@link StandardCharsets}
     */
    @Deprecated
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    @Nullable
    private String line;

    private boolean reset;
    private int ln;

    private boolean hasNonEmptyLine;

    public ConfigReader(final InputStream source) {
        super(new InputStreamReader(source, StandardCharsets.UTF_8));
    }

    public ConfigReader(final InputStream source, final int bufferLength) {
        super(new InputStreamReader(source, StandardCharsets.UTF_8), bufferLength);
    }

    @Override
    @Nullable
    public final String readLine() throws IOException {
        if (reset) {
            reset = false;
        } else {
            {
                line = stripUTF8BOM(super.readLine());
            }
            ++ln;
        }
        return line;
    }

    @Nullable
    private final String stripUTF8BOM(@Nullable final String line) {
        if (!hasNonEmptyLine && line != null && !line.isEmpty()) {
            hasNonEmptyLine = true;
            if (line.charAt(0) == '\uFEFF') {
                return line.substring(1);
            }
        }
        return line;
    }

    @Override
    public final void reset() {
        if (reset)
            throw new IllegalStateException("reset was called twice without a readLine in between");
        reset = true;
    }

    /**
     * @see ConfigReader#getLineNumber()
     * @deprecated Bad naming, backwards
     * compatibility.
     */
    @Deprecated
    public final int getLineNum() {
        return ln;
    }

    public final int getLineNumber() {
        return ln;
    }

    @Nullable
    public final String getLine() {
        return line;
    }

    @Override
    public final boolean markSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void mark(final int readAheadLimit) throws IOException {
        throw new UnsupportedOperationException();
    }

}
