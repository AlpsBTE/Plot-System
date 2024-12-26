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

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
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
import java.sql.SQLException;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

public class TutorialPlotGenerator extends AbstractPlotGenerator {
    private boolean buildingEnabled = false;
    private boolean worldEditEnabled = false;

    public TutorialPlotGenerator(@NotNull AbstractPlot plot, @NotNull Builder builder) throws SQLException {
        super(plot, builder, PlotType.TUTORIAL);
    }

    @Override
    protected boolean init() {
        return true;
    }

    public void generateOutlines(int schematicId) throws SQLException, IOException, WorldEditException {
        generateOutlines(((TutorialPlot) plot).getOutlinesSchematic(schematicId), null);
    }

    @Override
    protected void setBuildRegionPermissions(ProtectedRegion region) {
        region.setFlag(Flags.BUILD, isBuildingEnabled() ? StateFlag.State.ALLOW : StateFlag.State.DENY);
        region.setFlag(Flags.BUILD.getRegionGroupFlag(), RegionGroup.OWNERS);

        if (PlotSystem.DependencyManager.isWorldGuardExtraFlagsEnabled())
            region.setFlag(new StateFlag("worldedit", false, RegionGroup.OWNERS), isWorldEditEnabled() ? StateFlag.State.ALLOW : StateFlag.State.DENY);

        try {
            Objects.requireNonNull(WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world.getBukkitWorld()))).save();
        } catch (StorageException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while saving plot tutorial region!"), ex);
        }
    }

    @Override
    protected void onComplete(boolean failed, boolean unloadWorld) throws SQLException {
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
