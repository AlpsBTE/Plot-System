package github.BTEPlotSystem.core.plots;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.enums.Status;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlotManager extends PlotHandler {

    public static void ClaimPlot(int cityID, Builder builder) {
        throw new NotImplementedException();
    }

    public static List<Plot> getPlots() throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots"));
    }

    public static List<Plot> getPlots(Status status) throws SQLException {
        System.out.println(status.name());
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE status = '" + status.name() + "'"));
    }

    public static List<Plot> getPlots(Builder builder) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE uuidplayer = '" + builder.getPlayer().getUniqueId() + "'"));
    }

    public static List<Plot> getPlots(Builder builder, Status status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE uuidplayer = '" + builder.getPlayer().getUniqueId() + "' AND status = '" + status.name() + "'"));
    }

    private static List<Plot> listPlots(ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        while (rs.next()) {
            plots.add(new Plot(rs.getInt("idplot")));
        }

        return plots;
    }

    public static int getPlotSize() {
        return 103;
    }

    public static String getSchematicPath() {
        throw new NotImplementedException();
    }
}
