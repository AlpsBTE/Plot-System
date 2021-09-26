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

package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class RawPlotGenerator extends AbstractPlotGenerator {

    public RawPlotGenerator(Plot plot, Builder builder) {
        super(plot, builder);
    }

    @Override
    protected CompletableFuture<Boolean> init() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    protected CompletableFuture<Boolean> generateWorld() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    protected CompletableFuture<Boolean> configureWorld(@NotNull MultiverseWorld mvWorld) {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    protected CompletableFuture<Boolean> generateOutlines(@NotNull File plotSchematic) {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    protected CompletableFuture<Boolean> createProtection() {
        RegionContainer container = PlotSystem.DependencyManager.getWorldGuard().getRegionContainer();
        RegionManager regionManager = container.get(getPlot().getPlotWorld());

        if (regionManager != null) {
            for (String regionID : regionManager.getRegions().keySet()) {
                regionManager.removeRegion(regionID);
            }
        } else {
            Bukkit.getLogger().log(Level.SEVERE, "Region Manager is null!");
            return CompletableFuture.completedFuture(false);
        }

        return super.createProtection();
    }

    @Override
    protected void onComplete(boolean failed) {}

    @Override
    protected void onException(Throwable ex) {
        Bukkit.getLogger().log(Level.SEVERE, "An error occurred while cleaning plot!");
        getBuilder().getPlayer().sendMessage(Utils.getErrorMessageFormat("An error occurred while cleaning plot! Please try again!"));
        getBuilder().getPlayer().playSound(getBuilder().getPlayer().getLocation(), Utils.ErrorSound,1,1);
    }
}
