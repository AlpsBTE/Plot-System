/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Payout;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ScoreLeaderboard extends HolographicDisplay {
    private com.alpsbte.plotsystem.core.leaderboards.ScoreLeaderboard.LeaderboardTimeframe sortBy = com.alpsbte.plotsystem.core.leaderboards.ScoreLeaderboard.LeaderboardTimeframe.DAILY;

    public ScoreLeaderboard() {
        super("score-leaderboard");
    }

    @Override
    protected String getTitle() {
        return "§b§lTOP SCORE §7§o(" + StringUtils.capitalize(sortBy.toString()) + ")";
    }

    private class LeaderboardPositionLineWithPayout extends LeaderboardPositionLine {

        public LeaderboardPositionLineWithPayout(int position, String username, int score) {
            super(position, username, score);
        }

        @Override
        public String getLine() {
            try {
                String line = super.getLine();
                Payout payout = Payout.getPayout(sortBy, position);
                if(payout == null) {
                    return line;
                } else {
                    return line + " §7- §e§l$" + payout.getPayoutAmount();
                }
            } catch (SQLException e) {
                return super.getLine() + " §7- §cSQL ERR";
            }
        }
    }

    @Override
    protected List<DataLine> getDataLines() {
        try {
            ArrayList<DataLine> lines = new ArrayList<>();

            List<Map.Entry<String, Integer>> entries = new ArrayList<>(Builder.getBuildersByScore(sortBy).entrySet());

            for(int index = 0; index < 10; index++) {
                Map.Entry<String, Integer> entry = index < entries.size() ? entries.get(index) : null;
                String username = null;
                int score = 0;

                if(entry != null) {
                    username = entry.getKey();
                    score = entry.getValue();
                }

                lines.add(new LeaderboardPositionLineWithPayout(index + 1, username, score));
            }

            return lines;
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getLogger().log(Level.SEVERE, "Could not read data lines.", ex);
        }
        return new ArrayList<>();
    }

    @Override
    protected ItemStack getItem() {
        return new ItemStack(Material.NETHER_STAR);
    }

    @Override
    public void updateHologram() {
        if(isPlaced()) {
            getHologram().clearLines();
            insertLines();
        }
    }

    public void setSortBy(com.alpsbte.plotsystem.core.leaderboards.ScoreLeaderboard.LeaderboardTimeframe sortBy) {
        this.sortBy = sortBy;
        updateHologram();
    }
}
