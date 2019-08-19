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

package ch.njol.skript

import ch.njol.skript.config.Config
import ch.njol.skript.config.SectionNode
import ch.njol.skript.util.Version
import org.bukkit.Bukkit
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.easymock.EasyMock.createMock
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException
import java.net.UnknownHostException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Peter Güttinger
 */
class SkriptTest {

    @Test
    fun testVersion() {
        if (Skript.getLatestVersion({ error: Throwable? ->
                    if (error !is UnknownHostException) // Probably no internet access
                        error?.printStackTrace()
                }, false) == null) {
            println("[Skript] Can't check for updates") // Don't hard fail when no internet access
        }

        assertFalse(ScriptLoader.isErrorAllowed(VersionRegistry.STABLE_2_2_15, VersionRegistry.STABLE_2_2_15), "added: ${ScriptLoader.getSourceVersionFrom(VersionRegistry.STABLE_2_2_15)}, source: ${ScriptLoader.getSourceVersionFrom(VersionRegistry.STABLE_2_2_15)}")
        assertTrue(ScriptLoader.isErrorAllowed(VersionRegistry.STABLE_2_2_15, Version(2, 2, 20)), "added: ${ScriptLoader.getSourceVersionFrom(VersionRegistry.STABLE_2_2_15)}, source: ${ScriptLoader.getSourceVersionFrom(Version(2, 2, 20))}")

        assertTrue(ScriptLoader.isErrorAllowed(VersionRegistry.STABLE_2_2_15, Version(2, 2, 100)), "added: ${ScriptLoader.getSourceVersionFrom(VersionRegistry.STABLE_2_2_15)}, source: ${ScriptLoader.getSourceVersionFrom(Version(2, 2, 100))}")
        assertFalse(ScriptLoader.isErrorAllowed(VersionRegistry.STABLE_2_2_15, Version(2, 2, 0)), "added: ${ScriptLoader.getSourceVersionFrom(VersionRegistry.STABLE_2_2_15)}, source: ${ScriptLoader.getSourceVersionFrom(Version(2, 2, 0))}")

        assertThrows(IllegalArgumentException::class.java) { Version("2.x-SNAPSHOT") }

        assertFalse(Version("2.x-SNAPSHOT", true).isStable)

        assertFalse(Version(2).isSmallerThan(Version(2, 0)))
    }

    companion object {
        private val njol = createMock<Player>(Player::class.java)

        //	@Test
        fun main() {
            Thread {
                //				org.bukkit.craftbukkit.Main.main(new String[] {"-nojline"});
            }.start()
            while (Bukkit.getServer() == null) {
                try {
                    Thread.sleep(10)
                } catch (ignored: InterruptedException) {
                }

            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), {
                assertNotNull(Skript.getInstance())
                test()
            }, 2)
        }

        internal fun test() {
            val t = ScriptLoader.loadTrigger(nodeFromString("on rightclick on air:\n kill player")!!)!!
            t.execute(PlayerInteractEvent(njol, Action.LEFT_CLICK_AIR, null, null, BlockFace.SELF))
        }

        private fun nodeFromString(s: String): SectionNode? {
            try {
                return Config(s, "test.sk", true, false, ":").mainNode//.getNode(0);
            } catch (e: IOException) {
                assert(false) { e }
                return null
            }
        }
    }
}
