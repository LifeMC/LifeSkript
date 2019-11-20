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
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36";

    /**
     * Default is 5 seconds for connect and read.
     */
    public static final int CONNECT_TIMEOUT = 5000;
    public static final int READ_TIMEOUT = 5000;

    /**
     * The referer field of the requests.
     */
    private static final String REFERER = Pattern.compile("/releases", Pattern.LITERAL).matcher(Skript.LATEST_VERSION_DOWNLOAD_LINK).replaceAll(Matcher.quoteReplacement("")).trim();

    /**
     * Static magic.
     */
    private WebUtils() {
        throw new UnsupportedOperationException("Static class");
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
        return getResponse(Skript.urlOf(address), contentType, useWorkarounds);
    }

    /**
     * Connects to the given address and returns the web response as
     * {@link String String}.
     *
     * @param url            - The url of the web server / website to
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
    public static final String getResponse(final URL url, final String contentType, final boolean useWorkarounds) throws IOException {
        //Skript.debug("url", url, "contentType", contentType, "useWorkarounds", useWorkarounds, "async", Skript.isBukkitRunning() && !Bukkit.isPrimaryThread());
        if (useWorkarounds)
            Workarounds.initIfNotAlready();

        final URLConnection con = url.openConnection();
        setup(con, contentType, /* followRedirects: */true);

        try (final InputStream is = StreamUtils.getInputStream(con);
             final BufferedInputStream in = new BufferedInputStream(is)) {
            final String encoding = con.getContentEncoding();

            if (encoding != null) {
                if ("gzip".equalsIgnoreCase(encoding)) {
                    try (final GZIPInputStream gzipIs = new GZIPInputStream(in);
                         final BufferedInputStream gzip = new BufferedInputStream(gzipIs)) {
                        return StreamUtils.readString(gzip);
                    }
                }
                if ("deflate".equalsIgnoreCase(encoding)) {
                    try (final InflaterInputStream inf = new InflaterInputStream(in, new Inflater(true));
                         final BufferedInputStream deflate = new BufferedInputStream(inf)) {
                        return StreamUtils.readString(deflate);
                    }
                }
            }

            return StreamUtils.readString(in);
        }
    }

    public static final void setup(final URLConnection con,
                                   @Nullable final String contentType,
                                   final boolean followRedirects) {
        setup(con, contentType, followRedirects, "*/*");
    }

    public static final void setup(final URLConnection con,
                                   @Nullable final String contentType,
                                   final boolean followRedirects,
                                   final String acceptType) {
        con.setAllowUserInteraction(false);

        if (con instanceof HttpURLConnection) {
            final HttpURLConnection conn = (HttpURLConnection) con;

            conn.setInstanceFollowRedirects(followRedirects);
        }

        con.setDoOutput(false);
        con.setUseCaches(false);

        con.setConnectTimeout(CONNECT_TIMEOUT);
        con.setReadTimeout(READ_TIMEOUT);

        con.setRequestProperty("Method", "GET");

        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Encoding", "UTF-8");

        if (contentType != null)
            con.setRequestProperty("Content-Type", contentType.trim());

        con.setRequestProperty("Accept", acceptType);

        con.setRequestProperty("User-Agent", (USER_AGENT + (Skript.version != null ? " Skript/" + Skript.version : "")).trim());
        con.setRequestProperty("Referer", REFERER);
    }

}
