package github.BTEPlotSystem.core.plots;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.STATUS;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlotManager extends PlotHandler {

    public void CreatePlot(int id, Builder builder) {
        throw new NotImplementedException();
    }

    public List<Plot> getPlots() throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots"));
    }

    public List<Plot> getPlots(STATUS status) throws SQLException {
        System.out.println(status.name());
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE status = '" + status.name() + "'"));
    }

    public List<Plot> getPlots(Builder builder) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE uuidplayer = '" + builder.getPlayer().getUniqueId() + "'"));
    }

    public List<Plot> getPlots(Builder builder, STATUS status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE uuidplayer = '" + builder.getPlayer().getUniqueId() + "' AND status = '" + status.name() + "'"));
    }

    private List<Plot> listPlots(ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        while (rs.next()) {
            plots.add(new Plot(rs.getInt("idplot")));
        }

        return plots;
    }

    public int getPlotSize() {
        return 103;
    }

    public String getSchematicPath() {
        throw new NotImplementedException();
    }
}
