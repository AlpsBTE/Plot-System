package github.BTEPlotSystem.core.plots;

import com.boydti.fawe.object.FawePlayer;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.ClipboardFormats;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.World;
import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class PlotManager {

    public static void ClaimPlot(int cityID, Builder builder) throws SQLException {
        List<Plot> unclaimedCityPlots = getPlots(cityID, Status.unclaimed);
        Plot plot = new Plot(new Random().nextInt(unclaimedCityPlots.size() + 1));

        World weWorld = new BukkitWorld(builder.getPlayer().getWorld());
        Vector plotCoordinates;

        try {
            plotCoordinates = plot.getPlotCoordinates();
        } catch (Exception ex) {
            builder.getPlayer().sendMessage("§8§l>> §cAn error occurred while claiming the plot. Please try again.");
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while calculating new plot location!", ex);
            return;
        }

        // Generate default plot
        try {
            EditSession editSession =
                            ClipboardFormats.findByFile(getDefaultPlot())
                            .load(getDefaultPlot())
                            .paste(weWorld, plotCoordinates, false, true, null);
            editSession.flushQueue();

            Bukkit.getLogger().log(Level.INFO, "Successfully generated new plot at " + plotCoordinates.getX() + " / " + plotCoordinates.getY() + " / " + plotCoordinates.getZ());
        } catch (Exception ex) {
            builder.getPlayer().sendMessage("§8§l>> §cAn error occurred while claiming the plot. Please try again.");
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while loading new plot!", ex);
            return;
        }

        // Updates values and database
        builder.setPlot(plot.getID(), builder.getFreeSlot());
        plot.setStatus(Status.unfinished);
        plot.setBuilder(builder.getPlayer().getUniqueId().toString());

        // TODO: Spawn default plot platform schematic

        // TODO: Spawn plot schematic

        // TODO: Set plot building protection

        // TODO: Abandon Plot (clear slot and remove owner from plot and add to open plot at city project)

        // TODO: Finish Plot (set status to unreviewed)

        // Teleport player
        PlotHandler.TeleportPlayer(plot, builder.getPlayer());
    }

    public static List<Plot> getPlots() throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots"));
    }

    public static List<Plot> getPlots(Status... status) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT idplot FROM plots WHERE status = ");

        for(int i = 0; i < status.length; i++) {
            query.append("'").append(status[i].name()).append("'");

            query.append((i != status.length - 1) ? " OR status = " : "");
        }

        return listPlots(DatabaseConnection.createStatement().executeQuery(query.toString()));
    }

    public static List<Plot> getPlots(Builder builder) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE uuidplayer = '" + builder.getPlayer().getUniqueId() + "'"));
    }

    public static List<Plot> getPlots(Builder builder, Status status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE uuidplayer = '" + builder.getPlayer().getUniqueId() + "' AND status = '" + status.name() + "'"));
    }

    public static List<Plot> getPlots(int cityID, Status status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE idcity = '" + cityID + "' AND status = '" + status.name() + "'"));
    }

    private static List<Plot> listPlots(ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        // Get plot
        while (rs.next()) {
           plots.add(new Plot(rs.getInt("idplot")));
        }

        return plots;
    }

    public static Vector CalculatePlotCoordinates(int plotID) {
        // Get row of the plot
        int row = (int) (Math.floor((plotID - 1) / 5)) + 1;

        // Get column of the plot
        int column = ((plotID - 1) % getMaxRowsSize()) + 1;

        int xCoords = (getPlotSize() * row) * 20;
        int zCoords = (getPlotSize() * column) * 20;

        return Vector.toBlockPoint(xCoords, 70, zCoords);
    }

    public static int getPlotSize() {
        return 103;
    }

    public static int getMaxRowsSize() {
        return 5;
    }

    public static String getSchematicPath() {
        return BTEPlotSystem.getPlugin().getConfig().getString("schematic-path");
    }

    public static File getDefaultPlot() {
        return new File(BTEPlotSystem.getPlugin().getConfig().getString("default-plot-schematic"));
    }
}
