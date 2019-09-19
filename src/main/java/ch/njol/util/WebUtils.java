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

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.Workarounds;
import org.eclipse.jdt.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * A utility class about Web.
 *
 * @author TheDGOfficial
 * @since 2.2-Fixes-V10b
 */
public final class WebUtils {

    /**
     * The current chrome user agent.
     */
    public static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36";
    private static final Pattern RELEASES = Pattern.compile("/releases", Pattern.LITERAL);

    /**
     * Static magic.
     */
    private WebUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Connects to the given address and returns the web response as
     * {@link String String}.
     * This the overloaded version of the original
     * {@link WebUtils#getResponse(String, String)} method. This overloaded version
     * of the original method is just uses the default content type (json).
     *
     * @param address - The url (address) of the web server / website to connect
     *                and get response from it.
     * @return The web response from the given url as {@link String
     * String}, maybe null in some cases.
     * @throws IOException If any connection errors occurred when making the http
     *                     request to the address.
     */
    @Nullable
    public static final String getResponse(final String address) throws IOException {
        return getResponse(address, true);
    }

    /**
     * Connects to the given address and returns the web response as
     * {@link String String}.
     * This the overloaded version of the original
     * {@link WebUtils#getResponse(String, String)} method. This overloaded version
     * of the original method is just uses the default content type (json).
     *
     * @param address - The url (address) of the web server / website to connect
     *                and get response from it.
     * @return The web response from the given url as {@link String
     * String}, maybe null in some cases.
     * @throws IOException If any connection errors occurred when making the http
     *                     request to the address.
     */
    @Nullable
    public static final String getResponse(final String address, final boolean useWorkarounds) throws IOException {
        return getResponse(address, "application/json; charset=utf-8", useWorkarounds);
    }

    /**
     * Connects to the given address and returns the web response as
     * {@link String String}.
     *
     * @param address     - The url (address) of the web server / website to
     *                    connect and get response from it.
     * @param contentType - The content type header of the http web request to the
     *                    selected address / url.
     * @return The web response from the given url as {@link String
     * String}, maybe null in some cases.
     * @throws IOException If any connection errors occurred when making the http
     *                     request to the address.
     */
    @Nullable
    public static final String getResponse(final String address, final String contentType) throws IOException {
        return getResponse(address, contentType, true);
    }

    /**
     * Connects to the given address and returns the web response as
     * {@link String String}.
     *
     * @param address        - The url (address) of the web server / website to
     *                       connect and get response from it.
     * @param contentType    - The content type header of the http web request to the
     *                       selected address / url.
     * @param useWorkarounds - Pass false to not use workarounds.
     * @return The web response from the given url as {@link String
     * String}, maybe null in some cases.
     * @throws IOException If any connection errors occurred when making the http
     *                     request to the address.
     */
    @Nullable
    public static final String getResponse(final String address, final String contentType, final boolean useWorkarounds) throws IOException {
        if (useWorkarounds)
            Workarounds.initIfNotAlready();

        BufferedInputStream in = null;
        BufferedReader br = null;

        try {
            final URL url = new URL(address);
            final URLConnection con = url.openConnection();

            con.setAllowUserInteraction(false);

            con.setDoOutput(false);
            con.setUseCaches(false);

            con.setRequestProperty("Method", "GET");

            con.setRequestProperty("Charset", "UTF-8");
            con.setRequestProperty("Encoding", "UTF-8");

            con.setRequestProperty("Content-Type", contentType.trim());
            con.setRequestProperty("Accept", "*/*");

            con.setRequestProperty("User-Agent", USER_AGENT.trim());
            con.setRequestProperty("Referer", RELEASES.matcher(Skript.LATEST_VERSION_DOWNLOAD_LINK).replaceAll(Matcher.quoteReplacement("")).trim());

            in = new BufferedInputStream(con.getInputStream());

            final String encoding = con.getContentEncoding();

            if (encoding != null) {
                if ("gzip".equalsIgnoreCase(encoding)) {
                    in = new BufferedInputStream(new GZIPInputStream(in));
                } else if ("deflate".equalsIgnoreCase(encoding)) {
                    in = new BufferedInputStream(new InflaterInputStream(in, new Inflater(true)));
                }
            }

            final StringBuilder responseBody = new StringBuilder(4096);
            br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String line;

            while ((line = br.readLine()) != null) {
                responseBody.append(line.trim()).append('\n');
            }

            in.close();
            br.close();

            in = null;
            br = null;

            return responseBody.toString().trim();
        } finally {
            if (in != null) {
                in.close();
            }

            if (br != null) {
                br.close();
            }
        }

    }

}
