package github.BTEPlotSystem.core.plots;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.CityProject;
import github.BTEPlotSystem.utils.STATUS;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Plot {

    private final int ID;
    private CityProject cityProject;
    private Builder builder;
    private File schematic;
    private Location mcCoordinates;
    private String geoCoordinatesNumeric;
    private String geoCoordinatesNSEW;

    public Plot(int ID) throws SQLException {
        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE idplot = '" + ID + "'");

        if(rs.next()) {
            // City ID
            this.cityProject = new CityProject(rs.getInt("idcity"));

            // Schematic File
            //this.schematic = new File(BTEPlotSystem.getPlotManager().getSchematicPath() + cityProject.getID() + "//" + getID());

            // Builder
            this.builder = new Builder(Bukkit.getPlayer(rs.getString("uuidplayer")));

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

    public CityProject getCity() {
        return cityProject;
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

    public STATUS getStatus() throws SQLException {
        return STATUS.values()[DatabaseConnection.createStatement().executeQuery("SELECT status FROM plots WHERE idplot = '" + getID() + "'").getInt("status") - 1];
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
