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

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.logging.Level;

public final class LeaderboardManager extends HologramManager {

    public static void init() {
        for (DecentHologramDisplay display : activeDisplays) {
            Bukkit.getLogger().log(Level.INFO, "Enabling Hologram: " + PlotSystem.getPlugin().getConfig().getBoolean(((HologramConfiguration) display).getEnablePath()));

            // Register and create holograms
            if (PlotSystem.getPlugin().getConfig().getBoolean(((HologramConfiguration) display).getEnablePath()))
                for (Player player : Objects.requireNonNull(Bukkit.getWorld(display.getLocation().getWorld().getName())).getPlayers())
                    display.create(player);
            else display.removeAll();
        }
        activeDisplays.add(new ScoreLeaderboard());
        activeDisplays.add(new PlotsLeaderboard());
    }

    public static class LeaderboardPositionLine extends DecentHologramDisplay.TextLine {
        public LeaderboardPositionLine(int position, String username, int score) {
            super("§e#" + position + " " + (username != null ? "§a" + username : "§8No one, yet") + " §7- §b" + score);
        }
    }
}