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

package ch.njol.skript;

import ch.njol.skript.hooks.Hook;
import ch.njol.skript.update.script.ScriptUpdater;
import ch.njol.skript.util.ScriptOptions;
import ch.njol.skript.util.Version;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

final class ScriptConfig {

    private ScriptConfig() {
        throw new UnsupportedOperationException("Static class");
    }

    static final boolean isConfig(final String event) {
        return "configuration".equalsIgnoreCase(event);
    }

    static final ConfigParseResult tryParse(final File f, final List<String> duplicateCheckList, final String key,
                                            final String value, final AtomicReference<Version> scriptVersion, final AtomicReference<Version> currentScriptVersion) throws IOException, IllegalArgumentException {
        if ("source".equalsIgnoreCase(key)) {
            if (duplicateCheckList.contains("source")) {
                Skript.error("Duplicate source configuration setting");
                return ConfigParseResult.CONTINUE;
            }
            scriptVersion.set(new Version(value, true));
            currentScriptVersion.set(scriptVersion.get());
            duplicateCheckList.add("source");
        } else if ("target".equalsIgnoreCase(key)) {
            final Version target = new Version(value, true);
            if (duplicateCheckList.contains("target")) {
                Skript.error("Duplicate target configuration setting");
                return ConfigParseResult.CONTINUE;
            }
            // Source: The version that script is written and tested with.
            // Target: Actual minimum version that script supports.
            if (Skript.getVersion().isSmallerThan(target)) {
                Skript.error("This script requires Skript version " + value);
                return ConfigParseResult.ABORT_PARSING;
            }
            if (target.isLargerThan(scriptVersion.get())) // It is redundant to require a version higher than source version
                Skript.warning("This script is written in source version " + scriptVersion.get() + " but it requires " + target + " target version, please change source version to " + target + " or decrease the minimum target requirement for this script.");
            if (scriptVersion.get() == null || scriptVersion.get().isSmallerThan(target))
                scriptVersion.set(target);
            duplicateCheckList.add("target");
        } else if ("release".equalsIgnoreCase(key)) {
            if (duplicateCheckList.contains("release")) {
                Skript.error("Duplicate release configuration setting");
                return ConfigParseResult.CONTINUE;
            }
            // Release sets both source and target to the same value, i.e a shortcut
            final ConfigParseResult sourceResult = tryParse(f, duplicateCheckList, "source", value, scriptVersion, currentScriptVersion);
            if (sourceResult != ConfigParseResult.OK)
                return sourceResult;

            final ConfigParseResult targetResult = tryParse(f, duplicateCheckList, "target", value, scriptVersion, currentScriptVersion);
            if (targetResult != ConfigParseResult.OK)
                return targetResult;
            duplicateCheckList.add("release");
        } else if ("loops".equalsIgnoreCase(key)) {
            if (duplicateCheckList.contains("loops")) {
                Skript.error("Duplicate loops configuration setting");
                return ConfigParseResult.CONTINUE;
            }
            if (ScriptLoader.currentScript != null) {
                ScriptOptions.getInstance().setUsesNewLoops(Objects.requireNonNull(Objects.requireNonNull(ScriptLoader.currentScript).getFile()), !"old".equalsIgnoreCase(value));
                duplicateCheckList.add("loops");
            } else
                assert false : "null current script";
        } else if ("requires minecraft".equalsIgnoreCase(key)) {
            if (duplicateCheckList.contains("requires minecraft")) {
                Skript.error("Duplicate requires minecraft configuration setting");
                return ConfigParseResult.CONTINUE;
            }
            if (Skript.getMinecraftVersion().isSmallerThan(new Version(value, true))) {
                Skript.error("This script requires Minecraft version " + value);
                return ConfigParseResult.ABORT_PARSING;
            }
            duplicateCheckList.add("requires minecraft");
        } else if ("requires plugin".equalsIgnoreCase(key)) {
            if (Skript.getAddon(value) != null && ScriptLoader.isWarningAllowed(VersionRegistry.STABLE_2_2_16))
                Skript.warning("Use 'requires addon' instead of 'requires plugin' for add-ons.");

            if (Hook.isHookEnabled(value) && ScriptLoader.isWarningAllowed(VersionRegistry.STABLE_2_2_16))
                Skript.warning("Use 'requires hook' instead of 'requires plugin' for hooks.");

            if (!Bukkit.getPluginManager().isPluginEnabled(value)) {
                // This can be duplicate-able to require more than one plugin

                if (Bukkit.getPluginManager().getPlugin(value) != null) // exists, but not enabled
                    Skript.error("This script requires plugin " + value + ", but that plugin is not enabled currently.");
                else // it does not exist at all
                    Skript.error("This script requires plugin " + value);
                return ConfigParseResult.ABORT_PARSING;
            }
            if ("Skript".equalsIgnoreCase(value))
                Skript.warning("Requiring Skript is redundant. Please remove this requires plugin section.");
        } else if ("requires addon".equalsIgnoreCase(key) && Skript.getAddon(value) == null) {
            // This can be duplicate-able to require more than one addon

            if (Bukkit.getPluginManager().getPlugin(value) != null && !Bukkit.getPluginManager().isPluginEnabled(value)) // exists, but not enabled
                Skript.error("This script requires addon " + value + ", but that addon is not enabled currently.");
            else if (Bukkit.getPluginManager().getPlugin(value) == null) // it does not exist at all
                Skript.error("This script requires addon " + value);
            else // it exists, but it's not registered to Skript
                Skript.error("This script requires addon " + value + ", but that addon is not correctly registered to Skript currently.");
            return ConfigParseResult.ABORT_PARSING;
        } else if ("requires hook".equalsIgnoreCase(key) && !Hook.isHookEnabled(value)) {
            // This can be duplicate-able to require more than one hook

            if (Bukkit.getPluginManager().getPlugin(value) != null && !Bukkit.getPluginManager().isPluginEnabled(value)) // exists, but not enabled
                Skript.error("This script requires plugin " + value + ", but that plugin is not enabled currently.");
            else if (Bukkit.getPluginManager().getPlugin(value) == null) // it does not exist at all
                Skript.error("This script requires plugin " + value);
            else // it exists, but Skript is not hooked to it
                Skript.error("This script requires hook " + value + ", but Skript is currently not hooked to that plugin.");
            return ConfigParseResult.ABORT_PARSING;
        } else if ("load after".equalsIgnoreCase(key)) {
            return ConfigParseResult.SPECIAL_LOAD_AFTER;  // This has a special handling in the ScriptLoader, not our work
        } else if (ScriptUpdater.Parser.checkValid(key)) {
            ScriptUpdater.Parser.parse(f, duplicateCheckList, key, value);
        } else if (Skript.logHigh()) { // Only print on the high verbosity
            Skript.warning("Configuration option \"" + key + "\" is not supported");
        }
        return ConfigParseResult.OK;
    }

    enum ConfigParseResult {
        /**
         * Everything should be OK, which means no errors
         * are encountered, but warnings may have been printed.
         */
        OK,

        /**
         * Fatal error or requirements does not met. Abort parsing.
         */
        ABORT_PARSING,

        /**
         * A non-fatal error was printed or invalid input is received,
         * but it does not prevent the parse of the script, just continue parsing.
         */
        CONTINUE,

        /**
         * Specially handled by the script loader itself, not our work
         */
        SPECIAL_LOAD_AFTER,
    }

}
