/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ScoreLeaderboard extends DecentHologramPagedDisplay implements HologramConfiguration {
    private final DecimalFormat df = new DecimalFormat("#.##");
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
                .filter(p -> config.getBoolean(p.configPath)).map(LeaderboardTimeframe::toString).collect(Collectors.toList());
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

        LinkedHashMap<String, Integer> playerRankings = DataProvider.BUILDER.getLeaderboardEntries(sortByLeaderboard);
        if (playerRankings != null) {
            int i = 0;
            for (Map.Entry<String, Integer> entry : playerRankings.entrySet()) {
                lines.set(i, new HologramRegister.LeaderboardPositionLine(i + 1, entry.getKey(), entry.getValue()));
                i++;
            }
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