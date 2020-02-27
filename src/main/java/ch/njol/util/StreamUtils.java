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

package ch.njol.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Utility class about streams.
 * Not to be confused with Java 8 streams.
 *
 * @since 2.2.18
 */
public final class StreamUtils {

    private StreamUtils() {
        throw new UnsupportedOperationException("Static class");
    }

    /**
     * Returns the {@link InputStream} of the given connection.
     * It returns the error stream if server returns an error code.
     * <p>
     * Do not forget to close the returned {@link InputStream} after
     * reading with {@link StreamUtils#readString(InputStream)}, preferably use try-with resources.
     *
     * @param con The connection to get {@link InputStream} from it.
     * @return The {@link InputStream} of the given connection to read response.
     * @throws IOException If a connection error occurs, note that
     *                     this method may also suppress this exception and return an error stream.
     */
    @SuppressWarnings({"null", "resource"})
    public static final InputStream getInputStream(final URLConnection con) throws IOException {
        InputStream is = null;
        IOException error = null;
        try {
            is = con.getInputStream();
        } catch (final IOException e) {
            error = e;

            if (con instanceof HttpURLConnection)
                is = ((HttpURLConnection) con).getErrorStream();
        }
        if (is == null && error != null)
            throw error; // not a HttpURLConnection or a JDK bug
        return is;
    }

    /**
     * Reads a complete {@link String} from the given {@link InputStream}.
     * Uses default <b>unix line terminators</b> in the return {@link String}.
     *
     * @param is The {@link InputStream} to read {@link String} from it.
     * @return The complete read {@link String}.
     * @throws IOException If any error occurs when reading.
     * @implNote Uses {@link LineSeparators#UNIX} as line separator.
     * @see StreamUtils#readString(InputStream, String)
     */
    public static final String readString(final InputStream is) throws IOException {
        return readString(is, LineSeparators.UNIX);
    }

    /**
     * Reads a complete {@link String} from the given {@link InputStream}.
     * Uses the given line terminators in the return {@link String}.
     *
     * @param is            The {@link InputStream} to read {@link String} from it.
     * @param lineSeparator The line terminator/separator to use
     * @return The complete read {@link String}.
     * @throws IOException If any error occurs when reading.
     * @see LineSeparators
     * @see StreamUtils#readString(InputStream)
     */
    public static final String readString(final InputStream is,
                                          final String lineSeparator) throws IOException {
        final StringBuilder responseBody = new StringBuilder(4096);
        try (final InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8);
             final BufferedReader br = new BufferedReader(ir)) {
            String line;

            while ((line = br.readLine()) != null) {
                responseBody.append(line.trim()).append(lineSeparator);
            }

            return responseBody.toString().trim();
        }
    }

}
