package github.BTEPlotSystem.core.plots;

import github.BTEPlotSystem.core.DatabaseConnection;
import org.bukkit.Location;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

enum STATUS {
    unclaimed, unfinished, unreviewed, complete
}

public class Plot {

    private final int ID;
    private File schematic;
    private STATUS status;
    private Location mcCoordinates;
    private String geoCoordinatesNumeric;
    private String geoCoordinatesNSEW;

    public Plot(int ID) throws SQLException {
        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE idplot = " + ID);

        if(rs.next()) {
            // TODO: Set schematic file name structure
            this.schematic = null;

            // Set status
            this.status = STATUS.valueOf(rs.getString("status"));

            // Set MC Coordinates
            String[] mcLocation = rs.getString("mcCoordinates").split(",");
            this.mcCoordinates = new Location(null, Double.parseDouble(mcLocation[0]), Double.parseDouble(mcLocation[1]), Double.parseDouble(mcLocation[2]));

            // Set numeric geo coordinates
            this.geoCoordinatesNumeric = rs.getString("geoCoordinatesNumeric");

            // Set NSEW geo coordinates
            this.geoCoordinatesNSEW = rs.getString("geoCoordinatesNSEW");
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

    public String getGeoCoordinatesNumeric() {
        return geoCoordinatesNumeric;
    }

    public String getGeoCoordinatesNSEW() { return geoCoordinatesNSEW; }

    public STATUS getStatus() { return status; }

    public String getOSMMapsLink() {
        return "https://www.openstreetmap.org/#map=16/" + getGeoCoordinatesNumeric().replace(",", "/");
    }

    public String getGoogleMapsLink() {
        return "https://www.google.com/maps/place/" + getGeoCoordinatesNSEW() + "/@" + getGeoCoordinatesNumeric() + ",15z";
    }

    public String getGoogleEarthLink() {
        return "https://earth.google.com/web/@" + getGeoCoordinatesNumeric() + ",0a,10000d,1y,-0h,0t,0r";
    }
}
