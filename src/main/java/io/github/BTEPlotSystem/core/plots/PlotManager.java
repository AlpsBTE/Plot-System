package github.BTEPlotSystem.core.plots;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.STATUS;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlotManager extends PlotHandler {

    public void CreatePlot(int id, Builder builder) {

    }

    public List<Plot> getPlots() throws SQLException {
        return getPlots(DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots"));
    }

    public List<Plot> getPlotsByStatus(STATUS status) throws SQLException {
        return getPlots(DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE status = '" + status + "'"));
    }

    private List<Plot> getPlots(ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        while (rs.next()) {
            plots.add(new Plot(rs.getInt("plotid")));
        }

        return plots;
    }

    public int getPlotSize() {
        return 103;
    }

    public String getSchematicPath() {
        return "";
    }
}
