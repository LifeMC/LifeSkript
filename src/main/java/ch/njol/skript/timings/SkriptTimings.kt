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
import ch.njol.skript.util.EmptyArrays
import co.aikar.timings.Timing
import co.aikar.timings.Timings
import org.bukkit.Bukkit
import org.bukkit.plugin.SimplePluginManager
import org.eclipse.jdt.annotation.Nullable
import java.lang.reflect.Method

@Volatile
private var enabled: Boolean = false

private var skript: Skript? = null // Initialized on Skript load, before any timings would be used anyway

private val timings: Boolean = Skript.classExists("co.aikar.timings.Timings") && Skript.classExists("co.aikar.timings.Timing")

private val syncMethods: Boolean = timings && Skript.methodExists(Timings::class.java, "startTimingIfSync")
private val isEnabledMethod: Boolean = timings && Skript.methodExists(Timings::class.java, "isTimingsEnabled")

private val startTimingVoid: Boolean = timings && Skript.methodExists(Timings::class.java, "startTiming", EmptyArrays.EMPTY_CLASS_ARRAY, Void::class.javaPrimitiveType)

private val startTimingMethod: Method? = if (timings && !startTimingVoid)
    Skript.methodForName(Timing::class.java, "startTiming", EmptyArrays.EMPTY_CLASS_ARRAY, Timing::class.java)
else
    null

@JvmField
var timingsEnabled: Boolean = false

@Nullable
fun start(name: String): Any? {
    if (!enabled()) // Timings disabled
        return null
    val timing = Timings.of(skript!!, name)
    if (syncMethods)
        timing!!.startTimingIfSync() // No warning spam in async code
    else if (Bukkit.isPrimaryThread()) {
        if (startTimingVoid)
            timing!!.startTiming()
        else
            startTimingMethod!!.invoke(timing)
    }
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
    return enabled && (!isEnabledMethod || Timings.isTimingsEnabled()) && timingsEnabled && (Bukkit.getPluginManager() !is SimplePluginManager || (Bukkit.getPluginManager() as? SimplePluginManager)?.useTimings() == true)
}

fun setEnabled(flag: Boolean) {
    if (!flag) {
        enabled = flag
        return
    }
    if (syncMethods || startTimingVoid || startTimingMethod != null)
        enabled = true
    else {
        Skript.warning("Can't enable timings on an unsupported environment!")
        enabled = false // Just to make sure
    }
}

fun setSkript(plugin: Skript) {
    skript = plugin
}
