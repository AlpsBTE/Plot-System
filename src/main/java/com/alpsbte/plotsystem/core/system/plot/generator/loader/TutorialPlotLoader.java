package com.alpsbte.plotsystem.core.system.plot.generator.loader;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

public class TutorialPlotLoader extends AbstractPlotLoader {
    private boolean buildingEnabled = false;
    private boolean worldEditEnabled = false;

    public TutorialPlotLoader(@NotNull AbstractPlot plot, Builder builder) {
        super(plot, builder, PlotType.TUTORIAL, new OnePlotWorld(plot));
    }

    public void generateOutlines(int schematicId) throws Exception {
        ((TutorialPlot) plot).setTutorialSchematic(schematicId);
        fetchSchematicData();
        generateStructure();
    }

    @Override
    protected void setBuildRegionPermissions(@NotNull ProtectedRegion region) {
        region.setFlag(Flags.BUILD, isBuildingEnabled() ? StateFlag.State.ALLOW : StateFlag.State.DENY);
        region.setFlag(Flags.BUILD.getRegionGroupFlag(), RegionGroup.OWNERS);

        if (DependencyManager.isWorldGuardExtraFlagsEnabled())
            region.setFlag(new StateFlag("worldedit", false, RegionGroup.OWNERS), isWorldEditEnabled() ? StateFlag.State.ALLOW : StateFlag.State.DENY);

        try {
            Objects.requireNonNull(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(plotWorld.getBukkitWorld()))).save();
        } catch (StorageException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while saving plot tutorial region!"), ex);
        }
    }

    public boolean isBuildingEnabled() {
        return buildingEnabled;
    }

    public void setBuildingEnabled(boolean buildingEnabled) {
        this.buildingEnabled = buildingEnabled;
        setBuildRegionPermissions(plotWorld.getProtectedBuildRegion());
    }

    public boolean isWorldEditEnabled() {
        return worldEditEnabled;
    }

    public void setWorldEditEnabled(boolean worldEditEnabled) {
        this.worldEditEnabled = worldEditEnabled;
        setBuildRegionPermissions(plotWorld.getProtectedBuildRegion());
    }

    @Override
    protected void onCompletion() {}
}
