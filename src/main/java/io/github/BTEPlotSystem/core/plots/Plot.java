package github.BTEPlotSystem.core.plots;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.STATUS;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Plot {

    private final int ID;
    private Builder builder;
    private File schematic;
    private STATUS status;
    private Location mcCoordinates;
    private String geoCoordinatesNumeric;
    private String geoCoordinatesNSEW;

    public Plot(int ID) throws SQLException {
        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE idplot = " + ID);

        if(rs.next()) {
            // TODO: Schematic file name structure
            this.schematic = null;

            // TODO: City ID

            // Builder
            this.builder = new Builder(Bukkit.getPlayer(rs.getString("uuidplayer")));

            // Status
            this.status = STATUS.valueOf(rs.getString("status"));

            // MC Coordinates
            String[] mcLocation = rs.getString("mcCoordinates").split(",");
            this.mcCoordinates = new Location(null, Double.parseDouble(mcLocation[0]), Double.parseDouble(mcLocation[1]), Double.parseDouble(mcLocation[2]));

            // Numeric Geo Coordinates
            this.geoCoordinatesNumeric = rs.getString("geoCoordinatesNumeric");

            // NSEW Geo Coordinates
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

    public String getGeoCoordinatesNSEW() {
        return geoCoordinatesNSEW;
    }

    public STATUS getStatus() {
        return status;
    }

    public Builder getBuilder() {
        return builder;
    }

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
