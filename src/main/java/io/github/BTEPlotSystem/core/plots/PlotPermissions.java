package github.BTEPlotSystem.core.plots;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PlotPermissions {

    private final int plotID;

    public PlotPermissions(int plotID) {
        this.plotID = plotID;
    }

    public void addBuilderPerms(UUID builder) {
        getPlotRegion().getOwners().addPlayer(builder);
    }

    public void removeBuilderPerms(UUID builder) {
        getPlotRegion().getOwners().removePlayer(builder);
    }

    public void addReviewerPerms() {
        getPlotRegion().getOwners().addGroup("staff");
    }

    public void removeReviewerPerms() {
        getPlotRegion().getOwners().removeGroup("staff");
    }

    public void clearAllPerms() {
        getPlotRegion().getOwners().removeAll();
    }

    public boolean hasReviewerPerms() {
        return getPlotRegion().getOwners().getGroups().contains("staff");
    }

    public ProtectedRegion getPlotRegion() {
        RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        RegionManager regionManager = container.get(Bukkit.getWorld("P-" + plotID));
        return regionManager.getRegion("p-" + plotID);
    }
}
