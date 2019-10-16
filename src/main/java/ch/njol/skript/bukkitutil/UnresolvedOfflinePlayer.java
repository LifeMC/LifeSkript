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
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a {@link OfflinePlayer} but not resolved yet.
 * <p>
 * You should use this class for speeding up the UUID resolving process.
 * <p>
 * If a method requires the player to be resolved and method has return value, then the player is resolved in
 * the caller thread (usually main thread), and the method is processed normally.
 * <p>
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

                final boolean flag = Skript.testing() && Skript.debug();

                if (flag)
                    Skript.debug("Resolved the player " + p.getName());

                if (!p.actionQueue.isEmpty()) {
                    int actions = 0;

                    for (final Runnable action : p.actionQueue) {
                        if (action != null) {
                            p.actionQueue.remove(action);
                            action.run();
                            actions++;
                        } else
                            assert false : p.getName();
                    }

                    if (actions > 0)
                        Skript.debug("Ran " + actions + " queued actions for the player " + p.getName());
                }

                if (SkriptAgentKt.isTrackingEnabled())
                    SkriptAgentKt.throwEvent(new ResolvedPlayerEvent(p));

                if (toResolve.isEmpty())
                    Thread.sleep(1000L);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }, "Skript offline player resolver thread");
    final Queue<Runnable> actionQueue = new LinkedBlockingQueue<>();
    final String name;
    @Nullable
    OfflinePlayer bukkitOfflinePlayer;

    /**
     * Creates a new un resolved offline player from a player name.
     * The player will be resolved in an async thread via {@link Bukkit#getOfflinePlayer(String)}.
     * If a method that requires player to be resolved runs before it resolved in async thread,
     * the player will be resolved in the caller thread.
     *
     * @param name The name of the player to use it on {@link Bukkit#getOfflinePlayer(String)}.
     */
    public UnresolvedOfflinePlayer(final String name) {
        assert name != null;
        if (!threadStarted.get()) { // Do not start in the static initializer, only start it when required.
            threadStarted.set(true);
            resolverThread.setPriority(Thread.MIN_PRIORITY);
            resolverThread.start();
            Skript.closeOnDisable(resolverThread::interrupt);
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
    public final boolean resolveNow() {

        if (bukkitOfflinePlayer != null)
            return false; // Return false if already resolved.

        toResolve.remove(this); // Remove method does nothing if the queue not contains the element.

        if (getPlayer() != null) {
            bukkitOfflinePlayer = getPlayer();
            return true; // Anyways, set with this call.
        }

        if (Skript.testing() && Skript.debug())
            Skript.debug("Resolving unresolved offline player \"" + getName() + "\" immediately because it is needed for something!");

        bukkitOfflinePlayer = Bukkit.getOfflinePlayer(name); // Resolve now.
        // Javadoc says: "This method may involve a blocking web request to get the UUID for the given name."
        // This the reason we should use this class for offline players :D

        assert bukkitOfflinePlayer != null;
        return true;

    }

    /**
     * @see org.bukkit.permissions.ServerOperator#isOp()
     */
    @Override
    @SuppressWarnings("null")
    public final boolean isOp() {
        resolveNow();
        return bukkitOfflinePlayer.isOp();
    }

    /**
     * @see org.bukkit.permissions.ServerOperator#setOp(boolean)
     */
    @Override
    @SuppressWarnings("null")
    public final void setOp(final boolean value) {
        actionQueue.add(() -> bukkitOfflinePlayer.setOp(value));
    }

    /**
     * @see org.bukkit.configuration.serialization.ConfigurationSerializable#serialize()
     */
    @Override
    @SuppressWarnings("null")
    public final Map<String, Object> serialize() {
        resolveNow();
        return bukkitOfflinePlayer.serialize();
    }

    /**
     * @see OfflinePlayer#isOnline()
     */
    @Override
    @SuppressWarnings("null")
    public final boolean isOnline() {
        // Don't resolve just to check the online status. Try to get online player version.
        return bukkitOfflinePlayer != null ? bukkitOfflinePlayer.isOnline() : getPlayer() != null && getPlayer().isOnline();
    }

    /**
     * @see OfflinePlayer#getName()
     */
    @Override
    @SuppressWarnings("null")
    public final String getName() {
        // We already know its name, just to ensure, return from the real Bukkit offline player if available.
        return bukkitOfflinePlayer != null ? bukkitOfflinePlayer.getName() : name;
    }

    /**
     * @see OfflinePlayer#getUniqueId()
     */
    @Override
    @SuppressWarnings("null")
    public final UUID getUniqueId() {
        resolveNow();
        return bukkitOfflinePlayer.getUniqueId();
    }

    /**
     * @see OfflinePlayer#isBanned()
     */
    @Override
    @SuppressWarnings("null")
    public final boolean isBanned() {
        resolveNow();
        return bukkitOfflinePlayer.isBanned();
    }

    /**
     * @see OfflinePlayer#setBanned(boolean)
     * @deprecated Use {@link org.bukkit.BanList#addBan(String, String, java.util.Date, String)} or {@link org.bukkit.BanList#pardon(String)} to enhance functionality
     */
    @Override
    @SuppressWarnings({"null", "deprecation"})
    @Deprecated
    public final void setBanned(final boolean banned) {
        actionQueue.add(() -> bukkitOfflinePlayer.setBanned(banned));
    }

    /**
     * @see OfflinePlayer#isWhitelisted()
     */
    @Override
    @SuppressWarnings({"null", "unused"})
    public final boolean isWhitelisted() {
        if (bukkitOfflinePlayer != null)
            return bukkitOfflinePlayer.isWhitelisted();
        if (getPlayer() != null)
            return getPlayer().isWhitelisted();
        resolveNow();
        return bukkitOfflinePlayer.isWhitelisted();
    }

    /**
     * @see OfflinePlayer#setWhitelisted(boolean)
     */
    @Override
    @SuppressWarnings("null")
    public final void setWhitelisted(final boolean value) {
        actionQueue.add(() -> bukkitOfflinePlayer.setWhitelisted(value));
    }

    /**
     * @see OfflinePlayer#getPlayer()
     */
    @Override
    @Nullable
    public final Player getPlayer() {
        return bukkitOfflinePlayer != null ? bukkitOfflinePlayer.getPlayer() : Bukkit.getPlayerExact(name);
    }

    /**
     * @see OfflinePlayer#getFirstPlayed()
     */
    @Override
    @SuppressWarnings("null")
    public final long getFirstPlayed() {
        resolveNow();
        return bukkitOfflinePlayer.getFirstPlayed();
    }

    /**
     * @see OfflinePlayer#getLastPlayed()
     */
    @Override
    @SuppressWarnings("null")
    public final long getLastPlayed() {
        resolveNow();
        return bukkitOfflinePlayer.getLastPlayed();
    }

    /**
     * @see OfflinePlayer#hasPlayedBefore()
     */
    @Override
    @SuppressWarnings("null")
    public final boolean hasPlayedBefore() {
        resolveNow();
        return bukkitOfflinePlayer.hasPlayedBefore();
    }

    /**
     * @see OfflinePlayer#getBedSpawnLocation()
     */
    @Override
    @SuppressWarnings("null")
    public final Location getBedSpawnLocation() {
        resolveNow();
        return bukkitOfflinePlayer.getBedSpawnLocation();
    }

}
