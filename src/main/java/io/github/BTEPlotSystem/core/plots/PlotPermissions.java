package github.BTEPlotSystem.core.plots;

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
        if(Bukkit.getWorld("P-" + plotID) != null) {
            try {
                PlotHandler.unloadPlot(new Plot(plotID));
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }
    }

    public ProtectedRegion getPlotRegion() {
        RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();

        String worldName = "P-" + plotID;
        if(Bukkit.getWorld(worldName) == null) {
            try {
                PlotHandler.loadPlot(new Plot(plotID));
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }

        RegionManager regionManager = container.get(Bukkit.getWorld(worldName));
        return regionManager.getRegion("p-" + plotID);
    }
}
