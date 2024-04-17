package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public abstract class HologramManager {
    public static List<DecentHologramDisplay> activeDisplays = new ArrayList<>();

    public static void reload() {
        for (DecentHologramDisplay display : activeDisplays) {
            Bukkit.getLogger().log(Level.INFO, "Enabling Hologram: " + PlotSystem.getPlugin().getConfig().getBoolean(((HologramConfiguration) display).getEnablePath()));

            // Register and create holograms
            if (PlotSystem.getPlugin().getConfig().getBoolean(((HologramConfiguration) display).getEnablePath())) {
                display.setEnabled(true);
                for (Player player : Objects.requireNonNull(Bukkit.getWorld(display.getLocation().getWorld().getName())).getPlayers())
                    display.create(player);
            }
            else {
                display.setEnabled(false);
                display.removeAll();
            }
        }
    }

    public static Location getLocation(HologramConfiguration configPaths) {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();

        return new Location(Objects.requireNonNull(Utils.getSpawnLocation().getWorld()),
                config.getDouble(configPaths.getXPath()),
                config.getDouble(configPaths.getYPath()),
                config.getDouble(configPaths.getZPath())
        );
    }

    public static void saveLocation(String id, HologramConfiguration configPaths, Location newLocation) {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        config.set(configPaths.getEnablePath(), true);
        config.set(configPaths.getXPath(), newLocation.getX());
        config.set(configPaths.getYPath(), newLocation.getY());
        config.set(configPaths.getZPath(), newLocation.getZ());
        ConfigUtil.getInstance().saveFiles();

        LeaderboardManager.getActiveDisplays().stream().filter(leaderboard -> leaderboard.getId().equals(id)).findFirst()
                .ifPresent(holo -> holo.setLocation(newLocation));
    }

    public static List<DecentHologramDisplay> getActiveDisplays() {
        return activeDisplays;
    }
}