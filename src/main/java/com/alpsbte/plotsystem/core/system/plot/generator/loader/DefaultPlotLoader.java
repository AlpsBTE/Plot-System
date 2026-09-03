/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system.plot.generator.loader;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DefaultPlotLoader extends AbstractPlotLoader {
    public DefaultPlotLoader(@NotNull AbstractPlot plot, Builder builder, PlotType plotType, PlotWorld plotWorld) {
        super(plot, builder, plotType, plotWorld);
    }

    public DefaultPlotLoader(@NotNull AbstractPlot plot, Builder builder, PlotType plotType, PlotWorld plotWorld, boolean completionActionsEnabled) {
        super(plot, builder, plotType, plotWorld, completionActionsEnabled);
    }

    public DefaultPlotLoader(@NotNull AbstractPlot plot, Builder builder) {
        this(plot, builder, builder.getPlotType());
    }

    public DefaultPlotLoader(@NotNull AbstractPlot plot, Builder builder, PlotType plotType) {
        this(plot, builder, plotType, PlotWorld.getByType(plotType, (Plot) plot));
    }

    @Override
    protected void generateStructure() throws Exception {
        if (!(plot instanceof Plot p)) {
            super.generateStructure();
            return;
        }

        byte[] completedSchematic = p.getCompletedSchematic();
        if (completedSchematic != null) {
            runFaweAsync(() -> pasteSchematic(true, completedSchematic, plotWorld, false, true)).get();
        } else super.generateStructure();
        copyToCityWorld(completedSchematic == null ? schematicBytes : completedSchematic, completedSchematic != null);
    }

    @Override
    protected void onCompletion() {
        if (!completionActionsEnabled) return;
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            if (builder == null || builder.getPlayer() == null) return;
            PlotUtils.Cache.clearCache(builder.getUUID());
            plotWorld.teleportPlayer(builder.getPlayer());
            LangUtil.getInstance().broadcast(LangPaths.Message.Info.CREATED_NEW_PLOT, builder.getName());
        });
    }

    protected void copyToCityWorld(byte[] structureBytes, boolean completedSchematic) throws IOException {
        assert plot instanceof Plot;
        if (plot.getStatus() == Status.completed) return;
        if (!PlotWorld.isOnePlotWorld(plotWorld.getWorldName())) return;

        // If the player is playing in his own world, then additionally generate the plot in the city world
        CityPlotWorld cityPlotWorld = new CityPlotWorld((Plot) plot);
        try {
            ensureWorldGenerated(cityPlotWorld);
            runFaweAsync(() -> AbstractPlotLoader.pasteSchematic(completedSchematic, structureBytes, cityPlotWorld, false, true)).get();
        } catch (Exception e) {
            if (e.getCause() instanceof IOException ioException) throw ioException;
            throw new IOException("Could not copy plot to city world!", e);
        }
    }
}
