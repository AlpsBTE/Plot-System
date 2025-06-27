/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.utils;

import com.alpsbte.plotsystem.PlotSystem;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DependencyManager {
    private DependencyManager() {}

    /**
     * @return True if ParticleNativeAPI is present
     */
    public static boolean isParticleNativeAPIEnabled() {
        return PlotSystem.getPlugin().getServer().getPluginManager().isPluginEnabled("ParticleNativeAPI");
    }

    public static boolean isMultiverseInventoriesEnabled() {
        return PlotSystem.getPlugin().getServer().getPluginManager().isPluginEnabled("Multiverse-Inventories");
    }

    public static boolean isWorldGuardExtraFlagsEnabled() {
        return PlotSystem.getPlugin().getServer().getPluginManager().isPluginEnabled("WorldGuardExtraFlags");
    }

    /**
     * @param worldName Name of the world
     * @return Config path for the world
     */
    public static @NotNull String getMultiverseInventoriesConfigPath(String worldName) {
        return DependencyManager.isMultiverseInventoriesEnabled() ? Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Multiverse-Inventories")).getDataFolder() + "/worlds/" + worldName : "";
    }

    /**
     * @return Multiverse-Core instance
     */
    public static MultiverseCore getMultiverseCore() {
        return (MultiverseCore) PlotSystem.getPlugin().getServer().getPluginManager().getPlugin("Multiverse-Core");
    }

    /**
     * @return World Guard instance
     */
    public static WorldGuardPlugin getWorldGuard() {
        return WorldGuardPlugin.inst();
    }

    /**
     * @param worldName Name of the world
     * @return Config path for the world
     */
    public static @NotNull String getWorldGuardConfigPath(String worldName) {
        return Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("WorldGuard")).getDataFolder() + "/worlds/" + worldName;
    }
}
