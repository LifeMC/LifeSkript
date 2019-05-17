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
import ch.njol.skript.agents.SkriptAgentKt;
import ch.njol.skript.agents.events.end.ResolvedPlayerEvent;
import ch.njol.skript.agents.events.start.UnresolvedPlayerEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a {@link OfflinePlayer} but not resolved yet.
 * You should use this class for speeding up the UUID resolving process.
 * If a method requires the player to be resolved and method has return value, then the player is resolved in
 * caller thread (usually main thread) and the method is processed normally.
 * IF method has no return value, the method's action runs after the player is resolved.
 *
 * @author Njol (idea and first version), TheDGOfficial (recode)
 * @since 2.2-Fixes-V12 (Njol never implemented this)
 */
public final class UnresolvedOfflinePlayer implements OfflinePlayer {

    static final BlockingQueue<UnresolvedOfflinePlayer> toResolve = new LinkedBlockingQueue<>();
    static final AtomicBoolean threadStarted = new AtomicBoolean();
    @SuppressWarnings("deprecation")
    static final Thread resolverThread = Skript.newThread(() -> {
        while (Skript.isSkriptRunning()) {
            try {
                final UnresolvedOfflinePlayer p = toResolve.take(); // Takes the next unresolved player and removes from the queue.

                // See: https://github.com/LifeMC/LifeSkript/issues/4
                //noinspection ConstantConditions
                if (p == null)
                    continue;

                if (p.bukkitOfflinePlayer != null) // If already resolved by the resolveNow method, just ignore it.
                    continue;

                p.bukkitOfflinePlayer = Bukkit.getOfflinePlayer(p.name);

                if (!p.actionQueue.isEmpty())
                    for (final Runnable action : p.actionQueue)
                        if (action != null) {
                            p.actionQueue.remove(action);
                            action.run();
                        }

                if (SkriptAgentKt.isTrackingEnabled())
                    SkriptAgentKt.throwEvent(new ResolvedPlayerEvent(p));

                if (toResolve.isEmpty())
                    Thread.sleep(1000L);
            } catch (final InterruptedException e) {
                //Skript.exception(e, "An error occured when resolving offline player UUID's in a background thread. Skipping, but maybe this error printed several times if your server is problematic. Anyway, please report this error to ensure the problem.");
                break;
            }
        }
    }, "Skript offline player resolver thread");
    final BlockingQueue<Runnable> actionQueue = new LinkedBlockingQueue<>();
    final String name;
    @Nullable
    OfflinePlayer bukkitOfflinePlayer;

    /**
     * Creates a new un resolved offline player from a player name.
     * The player will be resolved in a async thread via {@link Bukkit#getOfflinePlayer(String)}.
     * If a method that requires player to be resolved runs before it resolved in async thread,
     * the player will be resolved in the caller thread.
     *
     * @param name The name of the player to use it on {@link Bukkit#getOfflinePlayer(String)}.
     */
    public UnresolvedOfflinePlayer(final String name) {
        if (!threadStarted.get()) { // Do not start in the static initializer, only start it when required.
            threadStarted.set(true);
            resolverThread.setPriority(Thread.MIN_PRIORITY);
            resolverThread.start();
            Skript.closeOnDisable(() -> {
                try {
                    resolverThread.interrupt();
                } catch (final Throwable ignored) { /* ignored */ }
            });
        }
        this.name = name;
        toResolve.add(this); // Try to resolve on a background thread, if possible.
        if (SkriptAgentKt.isTrackingEnabled())
            SkriptAgentKt.throwEvent(new UnresolvedPlayerEvent(this));
    }

    /**
     * Resolves this un resolved offline player, only IF it not already resolved.
     *
     * @return Returns true if the player is not already resolved and resolved via this call.
     */
    @SuppressWarnings({"deprecation", "null", "unused"})
    public boolean resolveNow() {

        if (bukkitOfflinePlayer != null)
            return false; // Return false if already resolved.

        toResolve.remove(this); // Remove method does nothing if the queue not contains the element.

        if (getPlayer() != null) {
            bukkitOfflinePlayer = getPlayer();
            return true; // Anyways, setted with this call.
        }

        bukkitOfflinePlayer = Bukkit.getOfflinePlayer(name); // Resolve now.
        // Javadoc says: "This method may involve a blocking web request to get the UUID for the given name."
        // This the reason we should use this class for offline players :D

        return true;

    }

    /**
     * @see org.bukkit.permissions.ServerOperator#isOp()
     */
    @SuppressWarnings("null")
    public boolean isOp() {
        resolveNow();
        return bukkitOfflinePlayer.isOp();
    }

    /**
     * @see org.bukkit.permissions.ServerOperator#setOp(boolean)
     */
    @SuppressWarnings("null")
    public void setOp(final boolean value) {
        actionQueue.add(() -> bukkitOfflinePlayer.setOp(value));
    }

    /**
     * @see org.bukkit.configuration.serialization.ConfigurationSerializable#serialize()
     */
    @SuppressWarnings("null")
    public Map<String, Object> serialize() {
        resolveNow();
        return bukkitOfflinePlayer.serialize();
    }

    /**
     * @see org.bukkit.OfflinePlayer#isOnline()
     */
    @SuppressWarnings("null")
    public boolean isOnline() {
        // Don't resolve just to check the online status. Try to get online player version.
        return bukkitOfflinePlayer != null ? bukkitOfflinePlayer.isOnline() : getPlayer() != null && getPlayer().isOnline();
    }

    /**
     * @see org.bukkit.OfflinePlayer#getName()
     */
    @SuppressWarnings("null")
    public String getName() {
        // We already know it's name, just to ensure, return from the real Bukkit offline player if available.
        return bukkitOfflinePlayer != null ? bukkitOfflinePlayer.getName() : name;
    }

    /**
     * @see org.bukkit.OfflinePlayer#getUniqueId()
     */
    @SuppressWarnings("null")
    public UUID getUniqueId() {
        resolveNow();
        return bukkitOfflinePlayer.getUniqueId();
    }

    /**
     * @see org.bukkit.OfflinePlayer#isBanned()
     */
    @SuppressWarnings("null")
    public boolean isBanned() {
        resolveNow();
        return bukkitOfflinePlayer.isBanned();
    }

    /**
     * @see org.bukkit.OfflinePlayer#setBanned(boolean)
     */
    @SuppressWarnings({"null", "deprecation"})
    @Deprecated
    public void setBanned(final boolean banned) {
        actionQueue.add(() -> bukkitOfflinePlayer.setBanned(banned));
    }

    /**
     * @see org.bukkit.OfflinePlayer#isWhitelisted()
     */
    @SuppressWarnings({"null", "unused"})
    public boolean isWhitelisted() {
        if (bukkitOfflinePlayer != null)
            return bukkitOfflinePlayer.isWhitelisted();
        else if (getPlayer() != null)
            return getPlayer().isWhitelisted();
        else {
            resolveNow();
            return bukkitOfflinePlayer.isWhitelisted();
        }
    }

    /**
     * @see org.bukkit.OfflinePlayer#setWhitelisted(boolean)
     */
    @SuppressWarnings("null")
    public void setWhitelisted(final boolean value) {
        actionQueue.add(() -> bukkitOfflinePlayer.setWhitelisted(value));
    }

    /**
     * @see org.bukkit.OfflinePlayer#getPlayer()
     */
    @SuppressWarnings({"null", "deprecation"})
    public Player getPlayer() {
        return bukkitOfflinePlayer != null ? bukkitOfflinePlayer.getPlayer() : Bukkit.getPlayerExact(name);
    }

    /**
     * @see org.bukkit.OfflinePlayer#getFirstPlayed()
     */
    @SuppressWarnings("null")
    public long getFirstPlayed() {
        resolveNow();
        return bukkitOfflinePlayer.getFirstPlayed();
    }

    /**
     * @see org.bukkit.OfflinePlayer#getLastPlayed()
     */
    @SuppressWarnings("null")
    public long getLastPlayed() {
        resolveNow();
        return bukkitOfflinePlayer.getLastPlayed();
    }

    /**
     * @see org.bukkit.OfflinePlayer#hasPlayedBefore()
     */
    @SuppressWarnings("null")
    public boolean hasPlayedBefore() {
        resolveNow();
        return bukkitOfflinePlayer.hasPlayedBefore();
    }

    /**
     * @see org.bukkit.OfflinePlayer#getBedSpawnLocation()
     */
    @SuppressWarnings("null")
    public Location getBedSpawnLocation() {
        resolveNow();
        return bukkitOfflinePlayer.getBedSpawnLocation();
    }

}
