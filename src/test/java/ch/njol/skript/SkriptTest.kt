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

import ch.njol.skript.Skript.*
import ch.njol.skript.bukkitutil.Compatibility.getClass
import ch.njol.skript.bukkitutil.Compatibility.getClassNoSuper
import ch.njol.skript.config.Config
import ch.njol.skript.config.SectionNode
import ch.njol.skript.lang.Condition
import ch.njol.skript.lang.Statement
import ch.njol.skript.util.EmptyStacktraceException
import ch.njol.skript.util.PatternCache
import ch.njol.skript.util.Version
import org.bukkit.Bukkit
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.easymock.EasyMock.mock
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URISyntaxException
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * @author Peter Güttinger
 */
class SkriptTest {

    @Test
    fun testVersion() {
        if (getLatestVersion({ error: Throwable? ->
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

    @Test
    fun testVersionRegistry() {
        var result = "Unknown-Version"
        var stream = Thread.currentThread().contextClassLoader.getResourceAsStream("version")

        if (stream == null) {
            try {
                val path = Paths.get(Thread.currentThread().contextClassLoader.getResource(".")!!.toURI()!!)
                val file = File(path.parent.parent.toString(), "version")

                if (file.exists()) {
                    stream = FileInputStream(file)
                }
            } catch (e: FileNotFoundException) {
                sneakyThrow(e)
            } catch (e: URISyntaxException) {
                sneakyThrow(e)
            }
        }

        try {
            result = String(stream!!.readBytes(), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            sneakyThrow(e)
        } finally {
            try {
                stream?.close()
            } catch (ignored: IOException) {
                /* ignored */
            }
        }

        assertNotNull(result)

        val version = Version(result)
        val registry = "STABLE_" + version.major + "_" + version.minor + "_" + version.revision

        assertTrue(fieldExists(VersionRegistry::class.java, registry))
        assertNotNull(fieldForName(VersionRegistry::class.java, registry)!!.get(null))
    }

    @Test
    fun testPatterns() {
        assertTrue(PATTERN_ON_SPACE_MATCHER.reset(" ").matches())
        assertTrue(NUMBER_PATTERN_MATCHER.reset(/*Random.nextInt().toString()*/"12").matches())

        assertSame(PatternCache.get("\\d+"), PatternCache.get("\\d+"))
    }

    @Test
    fun testClassLoader() {
        assertNotNull(getTrueClassLoader())
    }

    @Test
    fun testFindLoadedClass() {
        assertTrue(isClassLoaded("ch.njol.skript.Skript"))
        assertTrue(isClassLoaded("ch.njol.skript.SkriptTest"))

        assertFalse(isClassLoaded("ch.njol.skript.SkriptTest$Companion"))
    }

    @Test
    fun testReflection() {
        assertTrue(methodExists(Skript::class.java, "classExists", String::class.java))

        assertTrue(classExists("ch.njol.skript.Skript"))
        assertNotNull(classForName("ch.njol.skript.Skript"))

        assertNotNull(methodForName(SkriptTest::class.java, "testReflection"))
        assertNotNull(getConstructor(Skript::class.java))
    }

    @Test
    fun testGetAllThreads() {
        assertTrue(getAllThreads().isNotEmpty())
    }

    @Test
    fun testSneakyThrow() {
        assertThrows(IOException::class.java) {
            sneakyThrow(IOException())
        }
        assertThrows(EmptyStacktraceException::class.java) {
            sneakyThrow(EmptyStacktraceException())
        }
    }

    @Test
    fun testCompatibility() {
        assertSame(Skript::class.java, getClassNoSuper("ch.njol.skript.SkriptTest", "ch.njol.skript.Skript"))
        assertSame(Condition::class.java, getClass<Statement>("ch.njol.skript.lang.Effect", "ch.njol.skript.lang.Condition"))
    }

    companion object {
        private val njol = mock<Player>(Player::class.java)

        //	@Test
        fun testMain() {
            Thread {
                //				org.bukkit.craftbukkit.Main.main(new String[] {"-nojline"});
            }.start()
            while (Bukkit.getServer() == null) {
                try {
                    Thread.sleep(100L)
                } catch (ignored: InterruptedException) {
                    /* ignored */
                }
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(getInstance(), {
                assertNotNull(getInstance())
                test()
            }, 3L)
        }

        internal fun test() {
            val t = ScriptLoader.loadTrigger(nodeFromString("on rightclick on air:\n kill player")!!)!!
            t.execute(PlayerInteractEvent(njol, Action.LEFT_CLICK_AIR, null, null, BlockFace.SELF) as Event?)
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
