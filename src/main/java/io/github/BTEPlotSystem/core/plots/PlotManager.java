package github.BTEPlotSystem.core.plots;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.CityProject;
import github.BTEPlotSystem.utils.enums.Status;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PlotManager {

    public static void ClaimPlot(int cityID, Builder builder) throws SQLException {
        int claimedCityPlots = CityProject.getPlotsCount(cityID);
        List<Plot> unclaimedCityPlots = getPlots(cityID, Status.unclaimed);
        Plot newPlot = new Plot(new Random().nextInt(unclaimedCityPlots.size() + 1));

        // TODO: Calculate newPlot spawn location

        // Updates values and database
        builder.setPlot(newPlot.getID(), builder.getFreeSlot());
        newPlot.setStatus(Status.unfinished);

        // TODO: Update newPlot coordinates location

        // TODO: Spawn default plot platform schematic

        // TODO: Spawn plot schematic

        // TODO: Set plot building protection

        // TODO: Abandon Plot (clear slot and remove owner from plot and add to open plot at city project)

        // TODO: Finish Plot (set status to unreviewed)

        // Teleport player
        PlotHandler.TeleportPlayer(newPlot, builder.getPlayer());
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

    public static int getPlotSize() {
        return 103;
    }

    public static String getSchematicPath() {
        return BTEPlotSystem.getPlugin().getConfig().getString("PlotSystem.schematic-path");
    }

    public static File getDefaultPlot() {
        return new File(BTEPlotSystem.getPlugin().getConfig().getString("PlotSystem.default-plot-schematic"));
    }
}
