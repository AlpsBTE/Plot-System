package github.BTEPlotSystem.core.plots;

import com.sk89q.worldedit.Vector;;
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
    private final Difficulty difficulty;

    public Plot(int ID) throws SQLException {
        super(ID);

        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idcity, iddifficulty FROM plots WHERE idplot = '" + getID() + "'");
        rs.next();

        // City ID
        this.cityProject = new CityProject(rs.getInt(1));

        // Set Plot Difficulty
        this.difficulty = Difficulty.values()[rs.getInt(2)];
    }

    public int getID() { return ID; }

    public CityProject getCity() { return cityProject; }

    public Difficulty getDifficulty() { return difficulty; }

    public File getSchematic() { return Paths.get(PlotManager.getSchematicPath(), String.valueOf(cityProject.getID()), getID() + ".schematic").toFile(); }

    public Builder getBuilder() {
        try {
            if(getStatus() != Status.unclaimed) {
                ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT uuidplayer FROM plots WHERE idplot = '" + getID() + "'");
                rs.next();

                return new Builder(UUID.fromString(rs.getString(1)));
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return null;
    }

    public Review getReview() {
        try {
            if(getStatus() == Status.complete || wasRejected()) {
                ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idreview FROM plots WHERE idplot = '" + getID() + "'");
                rs.next();

                return new Review(rs.getInt(1));
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return null;
    }

    public String getGeoCoordinatesNumeric() {
        try {
            ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT mcCoordinates FROM plots WHERE idplot = '" + getID() + "'");
            rs.next();

            // Player MC Coordinates
            String[] mcLocation = rs.getString(1).split(",");

            // Added support for the recently added Y plot coordinate
            Vector mcCoordinates;
            if(mcLocation.length == 2) {
                mcCoordinates = new Vector(Double.parseDouble(mcLocation[0]),0,Double.parseDouble(mcLocation[1]));
            } else {
                mcCoordinates = new Vector(Double.parseDouble(mcLocation[0]),Double.parseDouble(mcLocation[1]),Double.parseDouble(mcLocation[2]));
            }

            // Convert MC coordinates to geo coordinates
            try {
                return CoordinateConversion.formatGeoCoordinatesNumeric(CoordinateConversion.convertToGeo(mcCoordinates.getX(), mcCoordinates.getZ()));
            } catch (OutOfProjectionBoundsException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Could not convert MC coordinates to geo coordinates!", ex);
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return null;
    }

    public int getScore() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT score FROM plots WHERE idplot = '" + getID() + "'");

        if(rs.next()) {
            return rs.getInt("score");
        }
        return 0;
    }

    public Status getStatus() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT status FROM plots WHERE idplot = '" + getID() + "'");
        rs.next();

        return Status.valueOf(rs.getString("status"));
    }

    public Date getLastActivity() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT lastActivity FROM plots WHERE idplot = '" + getID() + "'");
        rs.next();

        return rs.getDate(1);
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

    public void setScore(int score) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE plots SET score = ? WHERE idplot = '" + getID() + "'");
        statement.setInt(1, score);
        statement.executeUpdate();
        BTEPlotSystem.getHolograms().stream().filter(holo -> holo.getHologramName().equals("ScoreLeaderboard")).findFirst().get().updateLeaderboard();
    }

    public void setStatus(Status status) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE plots SET status = ? WHERE idplot = '" + getID() + "'");
        statement.setString(1, status.name());
        statement.executeUpdate();
    }

    public void setLastActivity(boolean setNull) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE plots SET lastActivity = ? WHERE idplot = '" + getID() + "'");
        statement.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));

        if(setNull) {
            statement = DatabaseConnection.prepareStatement("UPDATE plots SET lastActivity = DEFAULT(lastActivity) WHERE idplot = '" + getID() + "'");
        }

        statement.executeUpdate();
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
