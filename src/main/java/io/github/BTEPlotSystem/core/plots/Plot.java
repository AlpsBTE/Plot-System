package github.BTEPlotSystem.core.plots;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.CityProject;
import github.BTEPlotSystem.utils.Review;
import github.BTEPlotSystem.utils.conversion.CoordinateConversion;
import github.BTEPlotSystem.utils.conversion.projection.OutOfProjectionBoundsException;
import github.BTEPlotSystem.utils.enums.Difficulty;
import github.BTEPlotSystem.utils.enums.Slot;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;

import java.io.File;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class Plot extends PlotPermissions {

    private final int ID;
    private final CityProject cityProject;
    private final Builder builder;
    private double[] geoCoordinates;
    private final Difficulty difficulty;
    private final Date lastActivity;
    private Review review;

    public Plot(int ID) throws SQLException {
        super(ID);
        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM plots WHERE idplot = '" + getID() + "'");
        rs.next();

        // City ID
        this.cityProject = new CityProject(rs.getInt("idcity"));

        // Builder and Plot Coordinates
        this.builder = (getStatus() != Status.unclaimed) ?
                new Builder(UUID.fromString(rs.getString("uuidplayer"))) :
                null;

        // Set Plot Difficulty
        this.difficulty = Difficulty.values()[rs.getInt("iddifficulty")];

        // Set Plot Last Player Activity Date
        this.lastActivity = rs.getDate("lastActivity");

        // Set Review Class
        if(getStatus() == Status.complete || wasRejected()) {
            review = new Review(rs.getInt("idreview"));
        }

        // Player MC Coordinates
        String[] mcLocation = rs.getString("mcCoordinates").split(",");

       // Added support for the recently added Y plot coordinate
        Vector mcCoordinates;
        if(mcLocation.length == 2) {
            mcCoordinates = new Vector(Double.parseDouble(mcLocation[0]),0,Double.parseDouble(mcLocation[1]));
        } else {
            mcCoordinates = new Vector(Double.parseDouble(mcLocation[0]),Double.parseDouble(mcLocation[1]),Double.parseDouble(mcLocation[2]));
        }

        // Convert MC coordinates to geo coordinates
        try {
            this.geoCoordinates = CoordinateConversion.convertToGeo(mcCoordinates.getX(), mcCoordinates.getZ());
        } catch (OutOfProjectionBoundsException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not convert MC coordinates to geo coordinates!", ex);
        }
    }

    public int getID() { return ID; }

    public CityProject getCity() { return cityProject; }

    public Builder getBuilder() { return builder; }

    public Difficulty getDifficulty() { return difficulty; }

    public Date getLastActivity() { return lastActivity; }

    public Review getReview() { return review; }

    public File getSchematic() { return Paths.get(PlotManager.getSchematicPath(), String.valueOf(cityProject.getID()), getID() + ".schematic").toFile(); }

    public String getGeoCoordinatesNumeric() { return CoordinateConversion.formatGeoCoordinatesNumeric(geoCoordinates); }

    public Status getStatus() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT status FROM plots WHERE idplot = '" + getID() + "'");
        rs.next();

        return Status.valueOf(rs.getString("status"));
    }

    public int getScore() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT score FROM plots WHERE idplot = '" + getID() + "'");

        if(rs.next()) {
            return rs.getInt("score");
        }
        return 0;
    }

    public Slot getSlot() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT firstSlot,secondSlot,thirdSlot FROM players WHERE uuid = '" + getBuilder().getUUID() + "'");

        if(rs.next()) {
            if (rs.getInt(1) == getID()) {
                return Slot.firstSlot;
            } else if (rs.getInt(2) == getID()) {
                return Slot.secondSlot;
            } else if (rs.getInt(3) == getID()) {
                return Slot.thirdSlot;
            }
        }
        return null;
    }

    public void setBuilder(String UUID) throws SQLException {
        PreparedStatement statement;
        if(UUID != null) {
            statement = DatabaseConnection.prepareStatement("UPDATE plots SET uuidplayer = ? WHERE idplot = '" + getID() + "'");
            statement.setString(1, UUID);
        } else {
            statement = DatabaseConnection.prepareStatement("UPDATE plots SET uuidplayer = DEFAULT(uuidplayer) WHERE idplot = '" + getID() + "'");
        }
        statement.executeUpdate();
    }

    // Update plot status
    public void setStatus(Status status) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE plots SET status = ? WHERE idplot = '" + getID() + "'");
        statement.setString(1, status.name());
        statement.executeUpdate();
    }

    public void setScore(int score) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE plots SET score = ? WHERE idplot = '" + getID() + "'");
        statement.setInt(1, score);
        statement.executeUpdate();
        BTEPlotSystem.getHolograms().stream().filter(holo -> holo.getHologramName().equals("ScoreLeaderboard")).findFirst().get().updateLeaderboard();
    }

    // Get Open Street Maps link
    public String getOSMMapsLink() {
        return "https://www.openstreetmap.org/#map=19/" + getGeoCoordinatesNumeric().replace(",", "/");
    }

    // Get Google Maps link
    public String getGoogleMapsLink() {
        return "https://www.google.com/maps/place/"+ getGeoCoordinatesNumeric();
    }

    // Get Google Earth Web link
    public String getGoogleEarthLink() {
        return "https://earth.google.com/web/@" + getGeoCoordinatesNumeric() + ",0a,1000d,20y,-0h,0t,0r";
    }

    public boolean isReviewed(){
        return getReview() != null;
    }

    public boolean wasRejected() throws SQLException {
        return (getStatus() == Status.unfinished || getStatus() == Status.unreviewed) && getScore() != 0;
    }
}
