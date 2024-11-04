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

import com.alpsbte.alpslib.hologram.DecentHologramDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PlotsLeaderboard extends DecentHologramDisplay implements HologramConfiguration {
    protected PlotsLeaderboard() {
        super(ConfigPaths.PLOTS_LEADERBOARD, null, false);
        setLocation(HologramRegister.getLocation(this));
        setEnabled(PlotSystem.getPlugin().getConfig().getBoolean(getEnablePath()));
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BaseItems.LEADERBOARD_PLOT.getItem());
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return "§b§lCOMPLETED PLOTS §6§l[Lifetime]";
    }

    @Override
    public List<DataLine<?>> getContent(UUID playerUUID) {
        try {
            ArrayList<DataLine<?>> lines = new ArrayList<>();

            List<Builder.DatabaseEntry<String, Integer>> entries = Builder.getBuildersByCompletedBuilds(10);
            for (int i = 0; i < 10; i++) {
                Builder.DatabaseEntry<String, Integer> entry = i < entries.size() && entries.get(i).getValue() != 0 ? entries.get(i) : null;
                lines.add(new HologramRegister.LeaderboardPositionLine(i + 1, entry != null ? entry.getKey() : null, entry != null ? entry.getValue() : 0));
            }

            return lines;
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getLogger().log(Level.SEVERE, "An error occurred while reading leaderboard content", ex);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean hasViewPermission(UUID uuid) {
        return true;
    }

    @Override
    public String getEnablePath() {
        return ConfigPaths.PLOTS_LEADERBOARD_ENABLE;
    }

    @Override
    public String getXPath() {
        return ConfigPaths.PLOTS_LEADERBOARD_X;
    }

    @Override
    public String getYPath() {
        return ConfigPaths.PLOTS_LEADERBOARD_Y;
    }

    @Override
    public String getZPath() {
        return ConfigPaths.PLOTS_LEADERBOARD_Z;
    }
}