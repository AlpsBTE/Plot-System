package github.BTEPlotSystem.core.plots;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.enums.Difficulty;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class PlotManager {

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
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE uuidplayer = '" + builder.getUUID() + "'"));
    }

    public static List<Plot> getPlots(Builder builder, Status... status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE uuidplayer = '" + builder.getUUID() + "' AND status = '" + getPlots(status).toString() + "'"));
    }

    public static List<Plot> getPlots(int cityID, Status status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE idcity = '" + cityID + "' AND status = '" + status.name() + "'"));
    }

    public static int getMultiplierByDifficulty(Difficulty difficulty) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT multiplier FROM difficulties where name = '" + difficulty.name() + "'");

        if(rs.next()) {
            return rs.getInt(1);
        }
        return 1;
    }

    private static List<Plot> listPlots(ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        // Get plot
        while (rs.next()) {
           plots.add(new Plot(rs.getInt("idplot")));
        }

        return plots;
    }

    public static Plot getPlotByWorld(World plotWorld) throws SQLException {
        return new Plot(Integer.parseInt(plotWorld.getName().substring(2)));
    }

    public static int getPlotSize() {
        return 150;
    }

    public static String getSchematicPath() {
        return BTEPlotSystem.getPlugin().getConfig().getString("schematic-path");
    }
}
