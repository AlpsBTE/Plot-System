/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021-2022, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.plot.generator.PlotWorldGenerator;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.data.DataException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

public class PlotWorld extends AbstractWorld {
    private final Builder plotOwner;

    public PlotWorld(@NotNull Plot plot) throws SQLException {
        super("P-" + plot.getID(), plot);
        this.plotOwner = plot.getPlotOwner();
    }

    @Override
    public <T extends PlotWorldGenerator> boolean generateWorld(@NotNull Class<T> generator) {
        if (!isWorldGenerated()) {
            if (generator.isAssignableFrom(DefaultPlotGenerator.class)) {
               new DefaultPlotGenerator(getPlot(), plotOwner);
            } else return false;
            return true;
        }
        return false;
    }

    @Override
    public boolean loadWorld() {
        // Generate plot if it doesn't exist
        if (!isWorldGenerated() && getPlot().getFinishedSchematic().exists()) {
            new DefaultPlotGenerator(getPlot(), plotOwner) {
                @Override
                protected void generateOutlines(@NotNull File plotSchematic, @Nullable File environmentSchematic) throws DataException, SQLException, IOException, MaxChangedBlocksException {
                    super.generateOutlines(getPlot().getFinishedSchematic(), null);
                }

                @Override
                protected boolean init() {
                    return true;
                }

                @Override
                protected void onComplete(boolean failed) throws SQLException {
                    getPlot().getPermissions().clearAllPerms();
                    super.onComplete(true);
                }
            };
            if (!isWorldGenerated() || !isWorldLoaded()) {
                Bukkit.getLogger().log(Level.WARNING, "Could not regenerate world " + getWorldName() + " for plot " + getPlot().getID() + "!");
                return false;
            }
            return true;
        } else return super.loadWorld();
    }

    @Override
    public boolean unloadWorld(boolean movePlayers) {
        if (super.unloadWorld(movePlayers)) {
            try {
                if (getPlot().getStatus() == Status.completed && getBukkitWorld().getPlayers().isEmpty()) {
                    deleteWorld();
                    return true;
                }
                return !isWorldLoaded();
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An SQL error occurred!", ex);
            }
        }
        return false;
    }

    @Override
    public boolean teleportPlayer(@NotNull Player player) {
        if (super.teleportPlayer(player)) {
            try {
                player.sendMessage(Utils.getInfoMessageFormat(LangUtil.get(player, LangPaths.Message.Info.TELEPORTING_PLOT, String.valueOf(getPlot().getID()))));

                player.playSound(player.getLocation(), Utils.TeleportSound, 1, 1);
                player.setAllowFlight(true);
                player.setFlying(true);

                Utils.updatePlayerInventorySlots(player);
                PlotHandler.sendLinkMessages(getPlot(), player);

                if(getPlot().getPlotOwner().getUUID().equals(player.getUniqueId())) {
                    getPlot().setLastActivity(false);
                }

                return true;
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An SQL error occurred!", ex);
            }
        }
        return false;
    }
}
