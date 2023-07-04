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

package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.system.plot.PlotType;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class DefaultPlotGenerator extends AbstractPlotGenerator {
    public final static Map<UUID, LocalDateTime> playerPlotGenerationHistory = new HashMap<>();

    public DefaultPlotGenerator(int cityID, PlotDifficulty plotDifficulty, Builder builder) throws SQLException {
        this(PlotManager.getPlots(cityID, plotDifficulty, Status.unclaimed).get(new Random().nextInt(PlotManager.getPlots(cityID, plotDifficulty, Status.unclaimed).size())), builder);
    }

    public DefaultPlotGenerator(@NotNull Plot plot, @NotNull Builder builder) throws SQLException {
        super(plot, builder);
    }

    public DefaultPlotGenerator(@NotNull Plot plot, @NotNull Builder builder, PlotType plotType) throws SQLException {
        super(plot, builder, plotType);
    }

    @Override
    protected boolean init() {
        try {
            if (getBuilder().getFreeSlot() != null) {
                if (DefaultPlotGenerator.playerPlotGenerationHistory.containsKey(getBuilder().getUUID())) {
                    if (DefaultPlotGenerator.playerPlotGenerationHistory.get(getBuilder().getUUID()).isBefore(LocalDateTime.now().minusSeconds(10))) {
                        DefaultPlotGenerator.playerPlotGenerationHistory.remove(getBuilder().getUUID());
                    } else {
                        getBuilder().getPlayer().sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(getBuilder().getPlayer(), LangPaths.Message.Error.PLEASE_WAIT)));
                        getBuilder().getPlayer().playSound(getBuilder().getPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                        return false;
                    }
                }

                DefaultPlotGenerator.playerPlotGenerationHistory.put(getBuilder().getUUID(), LocalDateTime.now());
                getBuilder().getPlayer().sendMessage(Utils.ChatUtils.getInfoMessageFormat(LangUtil.getInstance().get(getBuilder().getPlayer(), LangPaths.Message.Info.CREATING_PLOT)));
                getBuilder().getPlayer().playSound(getBuilder().getPlayer().getLocation(), Utils.SoundUtils.CREATE_PLOT_SOUND, 1, 1);
                return true;
            } else {
                getBuilder().getPlayer().sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(getBuilder().getPlayer(), LangPaths.Message.Error.ALL_SLOTS_OCCUPIED)));
                getBuilder().getPlayer().playSound(getBuilder().getPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.INFO, "A SQL error occurred!", ex);
        }
        return false;
    }

    @Override
    protected void onComplete(boolean failed, boolean unloadWorld) throws SQLException {
        super.onComplete(failed, false);

        if (!failed) {
            getPlot().getWorld().teleportPlayer(getBuilder().getPlayer());
            LangUtil.getInstance().broadcast(LangPaths.Message.Info.CREATED_NEW_PLOT, getPlot().getPlotOwner().getName());
        }
    }
}
