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
import ch.njol.util.WebUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Workarounds for Java & Minecraft & Bukkit quirks
 * <p>
 * See {@link ch.njol.skript.Skript#onLoad()} for
 * other work arounds.
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

        System.err.println("Uncaught exception in the thread \"" + t.getName() + "\"");
        e.printStackTrace();

        System.err.flush();
        System.out.flush();
    };
    private static boolean init;

    static {
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
        throw new UnsupportedOperationException();
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
        oldValues.put("file.encoding", System.getProperty("file.encoding"));
        System.setProperty("file.encoding", "UTF-8");

        oldValues.put("sun.jnu.encoding", System.getProperty("sun.jnu.encoding"));
        System.setProperty("sun.jnu.encoding", "UTF-8");

        oldValues.put("sun.stderr.encoding", System.getProperty("sun.stderr.encoding"));
        System.setProperty("sun.stderr.encoding", "UTF-8");

        oldValues.put("sun.stdout.encoding", System.getProperty("sun.stdout.encoding"));
        System.setProperty("sun.stdout.encoding", "UTF-8");

        // Http Agent Fix
        oldValues.put("http.agent", System.getProperty("http.agent"));
        System.setProperty("http.agent", WebUtils.USER_AGENT);

        // Language Fix
        oldValues.put("user.language", System.getProperty("user.language"));
        System.setProperty("user.language", "en");

        // Country Fix
        oldValues.put("user.country", System.getProperty("user.country"));
        System.setProperty("user.country", "US");

        // Keep Alive Fix
        oldValues.put("paper.playerconnection.keepalive", System.getProperty("paper.playerconnection.keepalive"));
        System.setProperty("paper.playerconnection.keepalive", "60");

        // LifeSkript
        oldValues.put("using.lifeskript", System.getProperty("using.lifeskript"));
        System.setProperty("using.lifeskript", "true");

        // Change Some Default Settings
        URLConnection.setDefaultAllowUserInteraction(false);
        HttpURLConnection.setFollowRedirects(true);

        /* System properties */
    }

}
