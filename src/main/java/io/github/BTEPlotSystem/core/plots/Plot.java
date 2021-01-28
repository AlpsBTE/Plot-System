package github.BTEPlotSystem.core.plots;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.CityProject;
import github.BTEPlotSystem.utils.conversion.CoordinateConversion;
import github.BTEPlotSystem.utils.conversion.projection.OutOfProjectionBoundsException;
import github.BTEPlotSystem.utils.enums.Status;
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
    private Vector mcCoordinates;
    private String geoCoordinatesNumeric;
    private String geoCoordinatesNSEW;

    public Plot(int ID) throws SQLException {
        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE idplot = '" + ID + "'");

        if(rs.next()) {
            // City ID
            this.cityProject = new CityProject(rs.getInt("idcity"));

            // Schematic file
            //this.schematic = new File(BTEPlotSystem.getPlotManager().getSchematicPath() + cityProject.getID() + "//" + getID());

            // Builder
            if(getStatus() != Status.unclaimed) {
                this.builder = new Builder(Bukkit.getPlayer(rs.getString("uuidplayer")));
            }

            // Player MC Coordinates
            String[] mcLocation = rs.getString("mcCoordinates").split(",");
            this.mcCoordinates = new Vector(Double.parseDouble(mcLocation[0]),0,Double.parseDouble(mcLocation[1]));

            // Convert MC coordinates to geo coordinates
            try {
                double[] coords = CoordinateConversion.convertToGeo(mcCoordinates.getX(), mcCoordinates.getZ());

                // Geo coordinates numeric
                this.geoCoordinatesNumeric = CoordinateConversion.formatGeoCoordinatesNumeric(coords);

                // Geo coordinates NSEW
                this.geoCoordinatesNSEW = CoordinateConversion.formatGeoCoordinatesNSEW(coords);
            } catch (OutOfProjectionBoundsException e) {
                e.printStackTrace();
            }
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

    public Vector getMcCoordinates() {
        return mcCoordinates;
    }

    public String getGeoCoordinatesNumeric() {
        return geoCoordinatesNumeric;
    }

    public String getGeoCoordinatesNSEW() {
        return geoCoordinatesNSEW;
    }

    public Status getStatus() throws SQLException {
       ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT status FROM plots WHERE idplot = '" + getID() + "'");

       if(rs.next()) {
           return Status.valueOf(rs.getString("status"));
       } else {
           return null;
       }
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
