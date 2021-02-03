package github.BTEPlotSystem.core.plots;

import com.sk89q.worldedit.Vector;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.CityProject;
import github.BTEPlotSystem.utils.conversion.CoordinateConversion;
import github.BTEPlotSystem.utils.conversion.projection.OutOfProjectionBoundsException;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Plot {

    private final int ID;
    private CityProject cityProject;
    private Builder builder;
    private Vector mcCoordinates;
    private Vector plotCoordinates;
    private String geoCoordinatesNumeric;
    private String geoCoordinatesNSEW;

    public Plot(int ID) throws SQLException {
        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE idplot = '" + ID + "'");

        if(rs.next()) {
            // City ID
            this.cityProject = new CityProject(rs.getInt("idcity"));

            // Builder and Plot Coordinates
            if(getStatus() != Status.unclaimed) {
                this.builder = new Builder(Bukkit.getPlayer(rs.getString("uuidplayer")));

                String[] plotLocation = rs.getString("plotCoordinates").split(",");
                this.plotCoordinates = new Vector(Double.parseDouble(plotLocation[0]), Double.parseDouble(plotLocation[1]), Double.parseDouble(plotLocation[2]));
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
        System.out.println(PlotManager.getSchematicPath().concat(cityProject + "/" + getID() + ".schematic"));
        return new File(PlotManager.getSchematicPath().concat(cityProject + "/" + getID() + ".schematic"));
    }

    public Vector getMcCoordinates() {
        return mcCoordinates;
    }

    public Vector getPlotCoordinates() { return plotCoordinates; }

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

    public void setStatus(Status status) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE plots SET status = ? WHERE idplot = '" + getID() + "'");
        statement.setString(1, status.name());
        statement.executeUpdate();
    }

    public void setPlotCoordinates(Vector vector) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE plots SET plotCoordinates = ? WHERE idplot = '" + getID() + "'");
        statement.setString(1, vector.getX() + "," + vector.getY() + "," + vector.getZ());
        statement.executeUpdate();
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
