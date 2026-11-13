package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.alpslib.hologram.DecentHologramDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.leaderboards.ScoreLeaderboard;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class HologramRegister {
    private HologramRegister() {}

    private static final List<ScoreLeaderboard> leaderboards = new ArrayList<>();

    /**
     * Registers the {@link ScoreLeaderboard}.
     */
    public static void init() {
        leaderboards.add(new ScoreLeaderboard());
    }

    public static List<ScoreLeaderboard> getLeaderboards() {
        return Collections.unmodifiableList(leaderboards);
    }

    /**
     * Re-applies location and enabled state from the config to all leaderboards.
     */
    public static void reload() {
        for (ScoreLeaderboard leaderboard : leaderboards) {
            leaderboard.setLocation(getLocation(leaderboard));
            if (PlotSystem.getPlugin().getConfig().getBoolean(leaderboard.getEnablePath())) {
                leaderboard.setEnabled(true);
                for (Player player : Objects.requireNonNull(Bukkit.getWorld(leaderboard.getLocation().getWorld().getName())).getPlayers())
                    leaderboard.create(player);
            } else {
                leaderboard.setEnabled(false);
                leaderboard.removeAll();
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

    public static void saveLocation(ScoreLeaderboard leaderboard, Location newLocation) {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        config.set(leaderboard.getEnablePath(), true);
        config.set(leaderboard.getXPath(), newLocation.getX());
        config.set(leaderboard.getYPath(), newLocation.getY());
        config.set(leaderboard.getZPath(), newLocation.getZ());
        ConfigUtil.getInstance().saveFiles();

        leaderboard.setLocation(newLocation);
    }

    public static class LeaderboardPositionLine extends DecentHologramDisplay.TextLine {
        public LeaderboardPositionLine(int position, String username, int score) {
            super("§e#" + position + " " + (username != null ? "§a" + username : "§8No one, yet") + " §7- §b" + score);
        }
    }
}