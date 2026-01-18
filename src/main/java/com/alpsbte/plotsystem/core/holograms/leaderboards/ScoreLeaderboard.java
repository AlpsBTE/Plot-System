package com.alpsbte.plotsystem.core.holograms.leaderboards;

import com.alpsbte.alpslib.hologram.DecentHologramPagedDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.holograms.HologramConfiguration;
import com.alpsbte.plotsystem.core.holograms.HologramRegister;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ScoreLeaderboard extends DecentHologramPagedDisplay implements HologramConfiguration {
    private LeaderboardTimeframe sortByLeaderboard;

    public ScoreLeaderboard() {
        super("score-leaderboard", null, false, PlotSystem.getPlugin());
        setEnabled(PlotSystem.getPlugin().getConfig().getBoolean(getEnablePath()));
        setLocation(HologramRegister.getLocation(this));
    }

    @Override
    public List<String> getPages() {
        if (ConfigUtil.getInstance() == null) return new ArrayList<>();
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        return Arrays.stream(LeaderboardTimeframe.values())
                .filter(p -> config.getBoolean(p.configPath)).map(LeaderboardTimeframe::toString).toList();
    }

    @Override
    public void create(Player player) {
        if (getPages().isEmpty()) {
            PlotSystem.getPlugin().getLogger().log(Level.WARNING, "Unable to initialize Score-Leaderboard - No display pages enabled! Check config for display-options.");
            return;
        }

        super.create(player);
    }

    @Override
    public boolean hasViewPermission(UUID uuid) {
        return true;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BaseItems.LEADERBOARD_SCORE.getItem());
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return "§b§lTOP SCORE §6§l[" + (sortByLeaderboard.toString().charAt(0) +
                sortByLeaderboard.toString().toLowerCase().substring(1)) + "]";
    }

    @Override
    public List<DataLine<?>> getHeader(UUID playerUUID) {
        sortByLeaderboard = LeaderboardTimeframe.valueOf(currentPage);
        return super.getHeader(playerUUID);
    }

    @Override
    public List<DataLine<?>> getContent(UUID playerUUID) {
        ArrayList<DataLine<?>> lines = new ArrayList<>();

        for (int index = 0; index < 10; index++) {
            lines.add(new HologramRegister.LeaderboardPositionLine(index + 1, null, 0));
        }

        Map<String, Integer> playerRankings = DataProvider.BUILDER.getLeaderboardEntries(sortByLeaderboard);
        int i = 0;
        for (Map.Entry<String, Integer> entry : playerRankings.entrySet()) {
            lines.set(i, new HologramRegister.LeaderboardPositionLine(i + 1, entry.getKey(), entry.getValue()));
            i++;
        }
        return lines;
    }

    @Override
    public long getInterval() {
        return PlotSystem.getPlugin().getConfig().getInt(ConfigPaths.DISPLAY_OPTIONS_INTERVAL) * 20L;
    }

    @Override
    public String getEnablePath() {
        return ConfigPaths.SCORE_LEADERBOARD_ENABLE;
    }

    @Override
    public String getXPath() {
        return ConfigPaths.SCORE_LEADERBOARD_X;
    }

    @Override
    public String getYPath() {
        return ConfigPaths.SCORE_LEADERBOARD_Y;
    }

    @Override
    public String getZPath() {
        return ConfigPaths.SCORE_LEADERBOARD_Z;
    }
}