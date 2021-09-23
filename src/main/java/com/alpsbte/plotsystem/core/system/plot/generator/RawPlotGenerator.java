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
