package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

public class TutorialPlotGenerator extends AbstractPlotGenerator {
    private boolean buildingEnabled = false;
    private boolean worldEditEnabled = false;

    public TutorialPlotGenerator(@NotNull AbstractPlot plot, @NotNull Builder builder) {
        super(plot, builder, PlotType.TUTORIAL);
    }

    @Override
    protected boolean init() {
        return true;
    }

    public void generateOutlines(int schematicId) throws IOException, WorldEditException {
        ((TutorialPlot) plot).setTutorialSchematic(schematicId);
        generateOutlines();
    }

    @Override
    protected void setBuildRegionPermissions(@NotNull ProtectedRegion region) {
        region.setFlag(Flags.BUILD, isBuildingEnabled() ? StateFlag.State.ALLOW : StateFlag.State.DENY);
        region.setFlag(Flags.BUILD.getRegionGroupFlag(), RegionGroup.OWNERS);

        if (DependencyManager.isWorldGuardExtraFlagsEnabled())
            region.setFlag(new StateFlag("worldedit", false, RegionGroup.OWNERS), isWorldEditEnabled() ? StateFlag.State.ALLOW : StateFlag.State.DENY);

        try {
            Objects.requireNonNull(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world.getBukkitWorld()))).save();
        } catch (StorageException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while saving plot tutorial region!"), ex);
        }
    }

    @Override
    protected void onComplete(boolean failed, boolean unloadWorld) {
        super.onComplete(failed, false);
    }

    public boolean isBuildingEnabled() {
        return buildingEnabled;
    }

    public void setBuildingEnabled(boolean buildingEnabled) {
        this.buildingEnabled = buildingEnabled;
        setBuildRegionPermissions(world.getProtectedBuildRegion());
    }

    public boolean isWorldEditEnabled() {
        return worldEditEnabled;
    }

    public void setWorldEditEnabled(boolean worldEditEnabled) {
        this.worldEditEnabled = worldEditEnabled;
        setBuildRegionPermissions(world.getProtectedBuildRegion());
    }
}
