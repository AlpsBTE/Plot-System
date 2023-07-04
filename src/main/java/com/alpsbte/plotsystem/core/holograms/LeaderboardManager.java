/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.alpslib.hologram.HolographicDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import me.filoghost.holographicdisplays.api.Position;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.List;

public final class LeaderboardManager {
    private LeaderboardManager() {}
    private static final List<HolographicDisplay> leaderboards = Arrays.asList(
            new ScoreLeaderboard(ConfigPaths.SCORE_LEADERBOARD),
            new PlotsLeaderboard(ConfigPaths.PLOTS_LEADERBOARD)
    );

    public static void reloadLeaderboards() {
        if (PlotSystem.DependencyManager.isHolographicDisplaysEnabled()) {
            for (HolographicDisplay leaderboard : leaderboards) {
                String path = ConfigPaths.HOLOGRAMS + leaderboard.getId();
                if (PlotSystem.getPlugin().getConfig().getBoolean(path + ConfigPaths.LEADERBOARD_ENABLED))
                    leaderboard.create(getPosition(leaderboard.getId()));
            }
        }
    }

    public static Position getPosition(String id) {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        String path = ConfigPaths.HOLOGRAMS + id;

        return Position.of(Utils.getSpawnLocation().getWorld().getName(),
                config.getDouble(path + ConfigPaths.LEADERBOARD_X),
                config.getDouble(path + ConfigPaths.LEADERBOARD_Y),
                config.getDouble(path + ConfigPaths.LEADERBOARD_Z)
        );
    }

    public static void savePosition(String id, Location newLocation) {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        String path = ConfigPaths.HOLOGRAMS + id;

        config.set(path + ConfigPaths.LEADERBOARD_ENABLED, true);
        config.set(path + ConfigPaths.LEADERBOARD_X, newLocation.getX());
        config.set(path + ConfigPaths.LEADERBOARD_Y, newLocation.getY() + 4);
        config.set(path + ConfigPaths.LEADERBOARD_Z, newLocation.getZ());
        ConfigUtil.getInstance().saveFiles();

        reloadLeaderboards();
    }

    public static class LeaderboardPositionLine extends HolographicDisplay.TextLine {
        public LeaderboardPositionLine(int position, String username, int score) {
            super("§e#" + position + " " + (username != null ? "§a" + username : "§8No one, yet") + " §7- §b" + score);
        }
    }

    public static List<HolographicDisplay> getLeaderboards() {
        return leaderboards;
    }
}