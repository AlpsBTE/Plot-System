package com.alpsbte.plotsystem.core.system.plot.utils;

import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;

import java.util.UUID;

public class PlotPermissions {

    private final PlotWorld world;

    public PlotPermissions(PlotWorld world) {
        this.world = world;
    }

    public PlotPermissions addBuilderPerms(UUID builder) {
        world.getProtectedBuildRegion().getOwners().addPlayer(builder);
        PlotUtils.Cache.clearCache(builder);
        return this;
    }

    public PlotPermissions removeBuilderPerms(UUID builder) {
        world.getProtectedBuildRegion().getOwners().removePlayer(builder);
        PlotUtils.Cache.clearCache(builder);
        return this;
    }

    public void clearAllPerms() {
        world.getProtectedBuildRegion().getOwners().removeAll();
    }

    public boolean hasBuildingPerms(UUID builder) {
        return world.getProtectedBuildRegion().getOwners().contains(builder);
    }

    public void save() {
        world.unloadWorld(false);
    }
}
