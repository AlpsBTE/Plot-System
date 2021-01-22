package github.BTEPlotSystem.core.plots;

import github.BTEPlotSystem.core.DatabaseConnection;
import org.bukkit.Location;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

enum STATUS {
    unclaimed, unfinished, unreviewed, completed
}

public class Plot {

    private final int ID;
    private File schematic;
    private STATUS status;
    private Location mcCoordinates;
    private String geoCoordinates;

    public Plot(int ID) throws SQLException {
        this.ID = ID;

        Statement st = DatabaseConnection.getConnection().createStatement();

        ResultSet rs = st.executeQuery("SELECT * FROM plots WHERE ID=" + ID);

        if(rs.next()) {
            // TODO: Set file name structure
            this.schematic = null;

            // Set status
            this.status = STATUS.valueOf(rs.getString("status"));

            // Set MC Coordinates
            String[] mcLocation = rs.getString("mcCoordinates").split(",");
            this.mcCoordinates = new Location(null, Double.parseDouble(mcLocation[0]), Double.parseDouble(mcLocation[1]), Double.parseDouble(mcLocation[2]));

            // Set Geo Coordinates
            this.geoCoordinates = rs.getString("geoCoordinates");
        }
    }

    public int getID() {
        return ID;
    }

    public File getSchematic() {
        return schematic;
    }

    public Location getMcCoordinates() {
        return mcCoordinates;
    }

    public String getGeoCoordinates() {
        return geoCoordinates;
    }

    public STATUS getStatus() {
        return status;
    }
}
