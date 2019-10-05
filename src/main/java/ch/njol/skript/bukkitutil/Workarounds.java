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

package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.util.EmptyStacktraceException;
import ch.njol.util.Math2;
import ch.njol.util.WebUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Workarounds for Java & Minecraft & Bukkit quirks
 * <p>
 * See {@link Skript#onLoad()} for
 * other workarounds.
 *
 * @author Peter Güttinger
 */
public final class Workarounds {

    private static final Map<String, String> oldValues =
            new HashMap<>();
    public static boolean exceptionsDisabled;
    public static final Thread.UncaughtExceptionHandler uncaughtHandler = (t, e) -> {
        if (exceptionsDisabled)
            return;

        if (t == null || e == null)
            return;

        if (e instanceof EmptyStacktraceException)
            return;

        if (e instanceof AssertionError) {
            e.printStackTrace();

            return;
        }

        if (System.err == null || System.err.checkError()) {
            assert false : "Standard error output stream " + (System.err == null ? "is null" : "had an exception");

            return;
        }

        System.err.println("Uncaught exception in the thread \"" + t.getName() + '"');
        e.printStackTrace();

        System.err.flush();
        System.out.flush();
    };
    private static boolean init;

    static {
        // fix problems in updating jar files by disabling default caching of URL connections.
        // URLConnection default caching should be disabled since it causes jar file locking issues and JVM crashes in updating jar files.
        // Changes to jar files won't be noticed in all cases when caching is enabled.
        // sun.net.www.protocol.jar.JarURLConnection leaves the JarFile instance open if URLConnection caching is enabled.
        try {
            final URL url = new URL("jar:file://valid_jar_url_syntax.jar!/");
            final URLConnection urlConnection = url.openConnection();

            urlConnection.setDefaultUseCaches(false);
        } catch (final IOException e) {
            throw Skript.sneakyThrow(e);
        }

        Skript.closeOnEnable(() -> {
            assert Skript.isBukkitRunning();

            // Allows to properly remove a player's tool in right click events

            Bukkit.getPluginManager().registerEvent(PlayerInteractEvent.class, new Listener() {
                /* empty */
            }, EventPriority.LOWEST, (listener, event) -> {
                if (event instanceof PlayerInteractEvent) {
                    final PlayerInteractEvent e = (PlayerInteractEvent) event;
                    if (e.hasItem() && (e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getType() == Material.AIR || e.getPlayer().getItemInHand().getAmount() == 0))
                        e.setUseItemInHand(Result.DENY);
                }
            }, Skript.getInstance(), false);

            /* Bukkit uses reflection to call methods - We use our own hacky way above.
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler(priority = EventPriority.HIGHEST)
                public void onInteract(final PlayerInteractEvent e) {
                    if (e.hasItem() && (e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getType() == Material.AIR || e.getPlayer().getItemInHand().getAmount() == 0))
                        e.setUseItemInHand(Result.DENY);
                }
            }, Skript.getInstance());
            */
        });
    }

    private Workarounds() {
        throw new UnsupportedOperationException("Static class");
    }

    @SuppressWarnings("null")
    public static final String getOriginalProperty(final String key) {
        final String value = oldValues.get(key);
        return value != null ? value : System.getProperty(key);
    }

    public static final void initIfNotAlready() {
        if (!init) {
            init();
        }
    }

    public static final void setProperty(final String key,
                                          final String value) {
        oldValues.put(key, System.getProperty(key));
        System.setProperty(key, value);
    }

    public static final void setProperty(final String key,
                                          final int value) {
        setProperty(key, Integer.toString(value));
    }

    public static final void init() {
        init = true;

        // Server Thread
        final Runnable bukkitHandler = () -> Bukkit.getScheduler().runTask(Skript.getInstance(), () -> Thread.currentThread().setUncaughtExceptionHandler(uncaughtHandler));

        if (Skript.isSkriptRunning())
            bukkitHandler.run();
        else
            Skript.closeOnEnable(bukkitHandler::run);

        /* System properties */
        oldValues.clear();

        // UTF-8 Fixes
        setProperty("file.encoding", "UTF-8");
        setProperty("sun.jnu.encoding", "UTF-8");

        setProperty("sun.stderr.encoding", "UTF-8");
        setProperty("sun.stdout.encoding", "UTF-8");

        // Http Agent Fix
        setProperty("http.agent", WebUtils.USER_AGENT);

        // Locale Fixes
        setProperty("user.language", "en");
        setProperty("user.country", "US");

        // Keep Alive Fix
        setProperty("paper.playerconnection.keepalive", "60");

        // LifeSkript
        setProperty("using.lifeskript", "true");

        // Kotlin
        setProperty("kotlinx.coroutines.debug", "off");

        // Netty
        setProperty("io.netty.eventLoopThreads", Math2.min(4, Runtime.getRuntime().availableProcessors()));

        // Web
        setProperty("sun.net.http.errorstream.enableBuffering", "true");

        setProperty("sun.net.client.defaultConnectTimeout", WebUtils.CONNECT_TIMEOUT);
        setProperty("sun.net.client.defaultReadTimeout", WebUtils.READ_TIMEOUT);

        // Change Some Default Settings
        URLConnection.setDefaultAllowUserInteraction(false);
        HttpURLConnection.setFollowRedirects(true);

        /* System properties */
    }

}
