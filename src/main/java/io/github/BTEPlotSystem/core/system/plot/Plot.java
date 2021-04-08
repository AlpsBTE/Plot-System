package github.BTEPlotSystem.core.system.plot;

import com.sk89q.worldedit.Vector;;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.CityProject;
import github.BTEPlotSystem.core.system.Review;
import github.BTEPlotSystem.utils.conversion.CoordinateConversion;
import github.BTEPlotSystem.utils.conversion.projection.OutOfProjectionBoundsException;
import github.BTEPlotSystem.utils.enums.Difficulty;
import github.BTEPlotSystem.utils.enums.Slot;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class Plot extends PlotPermissions {

    private final int ID;
    private CityProject cityProject;
    private Difficulty difficulty;

    public Plot(int ID) throws SQLException {
        super(ID);

        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idcity, iddifficulty FROM plots WHERE idplot = '" + getID() + "'");

        if(rs.next()) {
            this.cityProject = new CityProject(rs.getInt(1));

            // Set Plot Difficulty
            this.difficulty = Difficulty.values()[rs.getInt(2) - 1];
        }
    }

    public int getID() { return ID; }

    public CityProject getCity() { return cityProject; }

    public Difficulty getDifficulty() { return difficulty; }

    public File getOutlinesSchematic() throws IOException {
        File file = Paths.get(PlotManager.getOutlinesSchematicPath(), String.valueOf(cityProject.getID()), getID() + ".schematic").toFile();
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        return file;
    }

    public File getFinishedSchematic() throws IOException {
        File file = Paths.get(PlotManager.getFinishedSchematicPath(), String.valueOf(cityProject.getID()), getID() + ".schematic").toFile();
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        return file;
    }

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
            if(getStatus() == Status.complete || isRejected()) {
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
            // Convert MC coordinates to geo coordinates
            Vector mcCoordinates = getMinecraftCoordinates();
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

    public Vector getMinecraftCoordinates() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT mcCoordinates FROM plots WHERE idplot = '" + getID() + "'");
        rs.next();

        // Player MC Coordinates
        String[] mcLocation = rs.getString(1).split(",");

        // Added support for the lately implemented Y plot coordinate
        if(mcLocation.length == 2) {
            return new Vector(Double.parseDouble(mcLocation[0]),0,Double.parseDouble(mcLocation[1]));
        } else {
            return new Vector(Double.parseDouble(mcLocation[0]),Double.parseDouble(mcLocation[1]),Double.parseDouble(mcLocation[2]));
        }
    }

    public int getScore() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT score FROM plots WHERE idplot = '" + getID() + "'");

        if(rs.next()) {
            int score = rs.getInt(1);
            if(rs.wasNull()) {
                return -1;
            } else {
                return score;
            }
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

    public String getOSMMapsLink() {
        return "https://www.openstreetmap.org/#map=19/" + getGeoCoordinatesNumeric().replace(",", "/");
    }

    public String getGoogleMapsLink() {
        return "https://www.google.com/maps/place/"+ getGeoCoordinatesNumeric();
    }

    public String getGoogleEarthLink() {
        return "https://earth.google.com/web/@" + getGeoCoordinatesNumeric() + ",0a,1000d,20y,-0h,0t,0r";
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
        PreparedStatement statement;
        if(score == -1) {
          statement = DatabaseConnection.prepareStatement("UPDATE plots SET score = DEFAULT(score) WHERE idplot = '" + getID() + "'");
        } else {
            statement = DatabaseConnection.prepareStatement("UPDATE plots SET score = ? WHERE idplot = '" + getID() + "'");
            statement.setInt(1, score);
        }
        statement.executeUpdate();
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

    public boolean isPasted() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT isPasted FROM plots WHERE idplot = '" + getID() + "'");
        rs.next();

        return rs.getBoolean(1);
    }

    public boolean isReviewed(){
        return getReview() != null;
    }

    public boolean isRejected() throws SQLException {
        return (getStatus() == Status.unfinished || getStatus() == Status.unreviewed) && getScore() != -1; // -1 == null
    }
}
