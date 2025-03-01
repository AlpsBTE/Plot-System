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

package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class DefaultPlotGenerator extends AbstractPlotGenerator {
    public static final Map<UUID, LocalDateTime> playerPlotGenerationHistory = new HashMap<>();

    public DefaultPlotGenerator(CityProject city, PlotDifficulty plotDifficulty, Builder builder) {
        this(DataProvider.PLOT.getPlots(city, plotDifficulty, Status.unclaimed).get(Utils.getRandom().nextInt(DataProvider.PLOT.getPlots(city, plotDifficulty, Status.unclaimed).size())), builder);
    }

    public DefaultPlotGenerator(@NotNull AbstractPlot plot, @NotNull Builder builder) {
        super(plot, builder);
    }

    public DefaultPlotGenerator(@NotNull AbstractPlot plot, @NotNull Builder builder, PlotType plotType) {
        super(plot, builder, plotType);
    }

    @Override
    protected boolean init() {
        if (getBuilder().getFreeSlot() != null) {
            if (DefaultPlotGenerator.playerPlotGenerationHistory.containsKey(getBuilder().getUUID())) {
                if (DefaultPlotGenerator.playerPlotGenerationHistory.get(getBuilder().getUUID()).isBefore(LocalDateTime.now().minusSeconds(10))) {
                    DefaultPlotGenerator.playerPlotGenerationHistory.remove(getBuilder().getUUID());
                } else {
                    getBuilder().getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getBuilder().getPlayer(), LangPaths.Message.Error.PLEASE_WAIT)));
                    getBuilder().getPlayer().playSound(getBuilder().getPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                    return false;
                }
            }

            DefaultPlotGenerator.playerPlotGenerationHistory.put(getBuilder().getUUID(), LocalDateTime.now());
            getBuilder().getPlayer().sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getBuilder().getPlayer(), LangPaths.Message.Info.CREATING_PLOT)));
            getBuilder().getPlayer().playSound(getBuilder().getPlayer().getLocation(), Utils.SoundUtils.CREATE_PLOT_SOUND, 1, 1);
            return true;
        } else {
            getBuilder().getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getBuilder().getPlayer(), LangPaths.Message.Error.ALL_SLOTS_OCCUPIED)));
            getBuilder().getPlayer().playSound(getBuilder().getPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
        }
        return false;
    }

    @Override
    protected void generateOutlines() throws IOException, WorldEditException {
        if (plot instanceof Plot) {
            byte[] completedSchematic = ((Plot) plot).getCompletedSchematic();
            if (completedSchematic != null) {
                Mask airMask = new BlockTypeMask(BukkitAdapter.adapt(world.getBukkitWorld()), BlockTypes.AIR);
                pasteSchematic(airMask, completedSchematic, world, true);
            } else super.generateOutlines();
        } else super.generateOutlines();

        // If the player is playing in his own world, then additionally generate the plot in the city world
        if (PlotWorld.isOnePlotWorld(world.getWorldName()) && plotVersion >= 3 && plot.getStatus() != Status.completed) {
            // Generate city plot world if it doesn't exist
            new AbstractPlotGenerator(plot, getBuilder(), PlotType.CITY_INSPIRATION_MODE) {
                @Override
                protected boolean init() {
                    return true;
                }

                @Override
                protected void createPlotProtection() {}

                @Override
                protected void onComplete(boolean failed, boolean unloadWorld) throws SQLException {
                    super.onComplete(true, true);
                }

                @Override
                protected void onException(Throwable ex) {
                    PlotSystem.getPlugin().getComponentLogger().warn(text("Could not generate plot in city world " + world.getWorldName() + "!"), ex);
                }
            };
        }
    }

    @Override
    protected void onComplete(boolean failed, boolean unloadWorld) throws SQLException {
        super.onComplete(failed, false);

        if (!failed) {
            getBuilder().setSlot(getBuilder().getFreeSlot(), plot.getID());
            plot.setStatus(Status.unfinished);
            ((Plot) plot).setPlotType(plotType);
            ((Plot) plot).setPlotOwner(getBuilder());
            PlotUtils.Cache.clearCache(getBuilder().getUUID());

            plot.getWorld().teleportPlayer(getBuilder().getPlayer());
            LangUtil.getInstance().broadcast(LangPaths.Message.Info.CREATED_NEW_PLOT, getBuilder().getName());
        }
    }
}
