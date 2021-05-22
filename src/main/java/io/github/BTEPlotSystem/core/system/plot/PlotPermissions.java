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

package github.BTEPlotSystem.core.system.plot;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class PlotPermissions {

    private final int plotID;

    public PlotPermissions(int plotID) {
        this.plotID = plotID;
    }

    public PlotPermissions addBuilderPerms(UUID builder) {
        getPlotRegion().getOwners().addPlayer(builder);
        return this;
    }

    public PlotPermissions removeBuilderPerms(UUID builder) {
        getPlotRegion().getOwners().removePlayer(builder);
        return this;
    }

    public PlotPermissions addReviewerPerms() {
        getPlotRegion().getOwners().addGroup("staff");
        return this;
    }

    public PlotPermissions removeReviewerPerms() {
        getPlotRegion().getOwners().removeGroup("staff");
        return this;
    }

    public PlotPermissions clearAllPerms() {
        getPlotRegion().getOwners().removeAll();
        return this;
    }

    public boolean hasReviewerPerms() {
        return getPlotRegion().getOwners().getGroups().contains("staff");
    }


    public void save() {
        try {
            PlotHandler.unloadPlot(new Plot(plotID));
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    public ProtectedRegion getPlotRegion() {
        RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();

        String worldName = "P-" + plotID;
        try {
            PlotHandler.loadPlot(new Plot(plotID));
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }

        RegionManager regionManager = container.get(Bukkit.getWorld(worldName));
        return regionManager.getRegion("p-" + plotID);
    }
}
