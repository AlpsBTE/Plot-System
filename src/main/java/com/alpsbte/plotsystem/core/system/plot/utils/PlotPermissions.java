package com.alpsbte.plotsystem.core.system.plot.utils;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class PlotPermissions {

    private final PlotWorld world;
    private boolean reportedMissingRegion;

    public PlotPermissions(PlotWorld world) {
        this.world = world;
    }

    public PlotPermissions addBuilderPerms(UUID builder) {
        ProtectedRegion region = buildRegion();
        if (region != null) region.getOwners().addPlayer(builder);
        PlotUtils.Cache.clearCache(builder);
        return this;
    }

    public PlotPermissions removeBuilderPerms(UUID builder) {
        ProtectedRegion region = buildRegion();
        if (region != null) region.getOwners().removePlayer(builder);
        PlotUtils.Cache.clearCache(builder);
        return this;
    }

    public void clearAllPerms() {
        ProtectedRegion region = buildRegion();
        if (region != null) region.getOwners().removeAll();
    }

    public boolean hasBuildingPerms(UUID builder) {
        ProtectedRegion region = buildRegion();
        return region != null && region.getOwners().contains(builder);
    }

    public void save() {
        world.unloadWorld(false);
    }

    private @Nullable ProtectedRegion buildRegion() {
        ProtectedRegion region = world.getProtectedBuildRegion();
        if (region == null) reportMissingRegion();
        return region;
    }

    private void reportMissingRegion() {
        if (reportedMissingRegion) return;
        reportedMissingRegion = true;

        AbstractPlot plot = world.getPlot();
        String plotRef = plot == null ? "" : " for plot #" + plot.getId();
        PlotSystem.getPlugin().getComponentLogger().warn(text(
                "Build region '" + world.getRegionName() + "' is missing in world " + world.getWorldName() + plotRef + "!"
        ));
    }
}
