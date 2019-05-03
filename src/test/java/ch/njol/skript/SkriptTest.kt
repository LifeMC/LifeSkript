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
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.easymock.EasyMock.createMock
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.IOException

/**
 * @author Peter Güttinger
 */
class SkriptTest {

    @Test
    fun testVersion() {
        assertNotNull(Skript.getLatestVersion(Throwable::printStackTrace, false))
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
            t.execute(PlayerInteractEvent(njol, Action.LEFT_CLICK_AIR, null, null, null))
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
