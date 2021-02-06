package github.BTEPlotSystem.core.plots;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.ClipboardFormats;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import static github.BTEPlotSystem.core.plots.PlotManager.getPlots;

public final class PlotGenerator {

    private final Builder builder;
    private final Plot plot;

    private final static Random random = new Random();
    private final World weWorld;
    private Vector plotVector;
    private Region plotRegion;

    public PlotGenerator(int cityID, Builder builder) throws SQLException {
        List<Plot> unclaimedCityPlots = getPlots(cityID, Status.unclaimed);
        int rndPlot = random.nextInt(unclaimedCityPlots.size());
        this.plot = unclaimedCityPlots.get(rndPlot);

        this.builder = builder;

        this.weWorld = new BukkitWorld(builder.getPlayer().getWorld());
    }

    public PlotGenerator(Plot plot, Builder builder) {
        this.plot = plot;
        this.builder = builder;
        this.weWorld = new BukkitWorld(builder.getPlayer().getWorld());
    }

    public void generate() throws Exception {
        generateDefaultPlot();

        generateBuildingOutlines();

        Bukkit.getLogger().log(Level.INFO, "Minimum Point: " + plotRegion.getMinimumPoint().getX() + " / " + plotRegion.getMinimumPoint().getY() + " / " + plotRegion.getMinimumPoint().getZ());
        Bukkit.getLogger().log(Level.INFO, "Maximum Point: " + plotRegion.getMaximumPoint().getX() + " / " + plotRegion.getMaximumPoint().getY() + " / " + plotRegion.getMaximumPoint().getZ());
        //createPlotProtection();

        // TODO: Abandon Plot (clear slot and remove owner from plot and add to open plot at city project)

        // TODO: Finish Plot (set status to unreviewed)

        builder.setPlot(plot.getID(), builder.getFreeSlot());
        plot.setStatus(Status.unfinished);
        plot.setBuilder(builder.getPlayer().getUniqueId().toString());

        // Teleport player
        PlotHandler.TeleportPlayer(plot, builder.getPlayer());
    }

    private void generateDefaultPlot() throws Exception {
        try {
            plotVector = PlotManager.CalculatePlotCoordinates(plot.getID());
            plotRegion = ClipboardFormats.findByFile(PlotManager.getDefaultPlot())
                    .load(PlotManager.getDefaultPlot())
                    .getClipboard().getRegion();

            EditSession editSession =
                    ClipboardFormats.findByFile(PlotManager.getDefaultPlot())
                            .load(PlotManager.getDefaultPlot())
                            .paste(weWorld, plotVector, false, false, null);
            editSession.flushQueue();

            Bukkit.getLogger().log(Level.INFO, "Successfully generated new plot at " + plotVector.getX() + " / " + plotVector.getY() + " / " + plotVector.getZ());
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while loading default plot!", ex);
            throw new Exception(ex);
        }
    }

    private void generateBuildingOutlines() throws Exception {
        try {
            Vector buildingOutlinesCoordinates = Vector.toBlockPoint(
                    plotVector.getX() - (PlotManager.getPlotSize() / 2) + 0.5,
                    plotVector.getY() + 10,
                    plotVector.getZ() + (PlotManager.getPlotSize() / 2) + 0.5
            );

            EditSession editSession = ClipboardFormats.findByFile(plot.getSchematic())
                    .load(plot.getSchematic())
                    .paste(weWorld, buildingOutlinesCoordinates, false, false, null);
            editSession.flushQueue();

            Bukkit.getLogger().log(Level.INFO, "Successfully generated building outlines at " + buildingOutlinesCoordinates.getX() + " / " + buildingOutlinesCoordinates.getY() + " / " + buildingOutlinesCoordinates.getZ());
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while loading building outlines!", ex);
            throw new Exception(ex);
        }
    }

    private void createPlotProtection() {
        ProtectedRegion protectedPlotRegion = new ProtectedCuboidRegion(
                "plot" + plot.getID(),
                BlockVector.toBlockPoint(plotRegion.getMinimumPoint().getX() - 1, plotRegion.getMinimumPoint().getY(), plotRegion.getMinimumPoint().getZ() - 1),
                BlockVector.toBlockPoint(plotRegion.getMaximumPoint().getX() + 1, plotRegion.getMaximumPoint().getY(), plotRegion.getMaximumPoint().getZ() + 1));

        DefaultDomain members = protectedPlotRegion.getMembers();
        members.addPlayer(builder.getUUID());
        members.addGroup("reviewers");
        protectedPlotRegion.setMembers(members);

        protectedPlotRegion.setFlag(DefaultFlag.PASSTHROUGH, StateFlag.State.ALLOW);
        protectedPlotRegion.setFlag(DefaultFlag.PASSTHROUGH.getRegionGroupFlag(), RegionGroup.MEMBERS);

        RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        //RegionManager regions = container.get(BukkitUtil.toWorld(LocalWorldAdapter.wrap(selection.getWorld())));
        //regions.addRegion(region);
    }
}
