package com.alpsbte.plotsystem.utils;

import com.alpsbte.plotsystem.PlotSystem;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

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

    public static @Nullable org.bukkit.plugin.Plugin getDiscordIntegration() {
        return PlotSystem.getPlugin().getServer().getPluginManager().getPlugin("DiscordPlotSystem");
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
    public static MultiverseCoreApi getMultiverseCore() {
        return MultiverseCoreApi.get();
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
