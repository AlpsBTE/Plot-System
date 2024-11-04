package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.alpslib.hologram.DecentHologramDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class HologramRegister {
    /**
     * Registers the {@link ScoreLeaderboard}, {@link PlotsLeaderboard} and adds
     * them to {@link DecentHologramDisplay#activeDisplays}.
     */
    public static void init() {
        new ScoreLeaderboard();
        new PlotsLeaderboard();
    }

    public static void reload() {
        for (DecentHologramDisplay display : DecentHologramDisplay.activeDisplays) {
            // Register and create holograms
            if (PlotSystem.getPlugin().getConfig().getBoolean(((HologramConfiguration) display).getEnablePath())) {
                display.setEnabled(true);
                for (Player player : Objects.requireNonNull(Bukkit.getWorld(display.getLocation().getWorld().getName())).getPlayers())
                    display.create(player);
            } else {
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

        for (DecentHologramDisplay display : DecentHologramDisplay.activeDisplays) {
            if (!Objects.equals(display.getId(), id)) continue;
            display.setLocation(newLocation);
        }
    }

    public static class LeaderboardPositionLine extends DecentHologramDisplay.TextLine {
        public LeaderboardPositionLine(int position, String username, int score) {
            super("§e#" + position + " " + (username != null ? "§a" + username : "§8No one, yet") + " §7- §b" + score);
        }
    }
}