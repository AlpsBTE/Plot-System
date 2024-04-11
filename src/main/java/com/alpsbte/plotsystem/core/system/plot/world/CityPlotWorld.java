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

package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.fastasyncworldedit.core.FaweAPI;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.google.common.annotations.Beta;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CityPlotWorld extends PlotWorld {
    public CityPlotWorld(@NotNull Plot plot) throws SQLException {
        super("C-" + plot.getCity().getID(), plot);
    }

    @Override
    public boolean teleportPlayer(@NotNull Player player) {
        if (super.teleportPlayer(player)) {
            try {
                player.playSound(player.getLocation(), Utils.SoundUtils.TELEPORT_SOUND, 1, 1);
                player.setAllowFlight(true);
                player.setFlying(true);

                if (getPlot() != null) {
                    player.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(player, LangPaths.Message.Info.TELEPORTING_PLOT, String.valueOf(getPlot().getID()))));

                    Utils.updatePlayerInventorySlots(player);
                    PlotUtils.ChatFormatting.sendLinkMessages((Plot) getPlot(), player);

                    if(getPlot().getPlotOwner().getUUID().equals(player.getUniqueId())) {
                        getPlot().setLastActivity(false);
                    }
                }

                return true;
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }
        return false;
    }

    @Override
    public String getRegionName() {
        return super.getRegionName() + "-" + getPlot().getID();
    }


    @Beta
    @Override
    public int getPlotHeight() throws IOException {
        return getPlot().getVersion() >= 3 ? MIN_WORLD_HEIGHT + getWorldHeight() : getPlotHeightCentered();
    }

    @Beta
    @Override
    public int getPlotHeightCentered() throws IOException {
        return Math.min(MIN_WORLD_HEIGHT + getWorldHeight() + super.getPlotHeightCentered(), PlotWorld.MAX_WORLD_HEIGHT);
    }

    /**
     * Calculate additional height for the plot
     * @return additional height
     * @throws IOException if the outline schematic fails to load
     */
    @Beta
    public int getWorldHeight() throws IOException {
        Clipboard clipboard = FaweAPI.load(getPlot().getOutlinesSchematic());
        int plotHeight = clipboard != null ? clipboard.getMinimumPoint().getBlockY() : MIN_WORLD_HEIGHT;
        int heightThreshold = 50; // Additional height the plot use to save as schematic need to be included as a threshold

        // Plots created below min world height are not supported
        if (plotHeight + heightThreshold < MIN_WORLD_HEIGHT) throw new IOException("Plot height is not supported");

        // Move Y height to a usable value below 256 blocks
        while (plotHeight >= (heightThreshold + 100)) {
            plotHeight -= (heightThreshold + 100);
        }
        return plotHeight;
    }

    /**
     * Gets all players located on the plot in the city plot world
     * @return a list of players located on the plot
     */
    public List<Player> getPlayersOnPlot() throws SQLException {
        List<Player> players = new ArrayList<>();
        if (getPlot() != null && getPlot().getWorld().isWorldLoaded() && !getPlot().getWorld().getBukkitWorld().getPlayers().isEmpty()) {
            for (Player player : getPlot().getWorld().getBukkitWorld().getPlayers()) {
                if (PlotUtils.isPlayerOnPlot(getPlot(), player)) players.add(player);
            }
            return players;
        }
        return players;
    }
}
