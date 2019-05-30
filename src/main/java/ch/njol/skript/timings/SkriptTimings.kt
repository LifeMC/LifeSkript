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

@file:JvmName("SkriptTimings")

package ch.njol.skript.timings

import ch.njol.skript.Skript
import co.aikar.timings.Timing
import co.aikar.timings.Timings
import org.bukkit.Bukkit
import org.eclipse.jdt.annotation.Nullable

@Volatile
private var enabled: Boolean = false

private var skript: Skript? = null // Initialized on Skript load, before any timings would be used anyway

private val syncMethods: Boolean = Skript.methodExists(Timings::class.java, "startTimingIfSync")

@Nullable
fun start(name: String): Any? {
    if (!enabled()) // Timings disabled
        return null
    val timing = Timings.of(skript!!, name)
    if (syncMethods)
        timing!!.startTimingIfSync() // No warning spam in async code
    else if (Bukkit.isPrimaryThread())
        timing!!.startTiming()
    @Suppress("SENSELESS_COMPARISON")
    assert(timing != null)
    return timing
}

fun stop(@Nullable timing: Any?) {
    if (timing == null) // Timings disabled
        return
    if (!enabled())
        return
    if (timing is Timing) {
        if (syncMethods)
            timing.stopTimingIfSync()
        else if (Bukkit.isPrimaryThread())
            timing.stopTiming()
    }
}

fun enabled(): Boolean {
    // First check if we can run timings (enabled in settings + running Paper or LifeSpigot)
    // After that (we know that class exists), check if server has timings running
    return enabled && Timings.isTimingsEnabled()
}

fun setEnabled(flag: Boolean) {
    enabled = flag
}

fun setSkript(plugin: Skript) {
    skript = plugin
}
