/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package github.BTEPlotSystem.core.system.plot;

import com.sk89q.worldedit.Vector;;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.CityProject;
import github.BTEPlotSystem.core.system.Review;
import github.BTEPlotSystem.utils.conversion.CoordinateConversion;
import github.BTEPlotSystem.utils.conversion.projection.OutOfProjectionBoundsException;
import github.BTEPlotSystem.utils.enums.PlotDifficulty;
import github.BTEPlotSystem.utils.enums.Slot;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class Plot extends PlotPermissions {

    private final int ID;
    private CityProject cityProject;
    private PlotDifficulty plotDifficulty;

    public Plot(int ID) throws SQLException {
        super(ID);
        this.ID = ID;

        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT idcity, iddifficulty FROM plots WHERE idplot = ?");
            ps.setInt(1, getID());

            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                this.cityProject = new CityProject(rs.getInt(1));
                this.plotDifficulty = PlotDifficulty.values()[rs.getInt(2) - 1];
            }
        }
    }

    public int getID() { return ID; }

    public CityProject getCity() { return cityProject; }

    public PlotDifficulty getDifficulty() { return plotDifficulty; }

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

    public Builder getBuilder() throws SQLException {
        if(getStatus() != Status.unclaimed) {
            try (Connection con = DatabaseConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT uuidplayer FROM plots WHERE idplot = ?");
                ps.setInt(1, getID());
                ResultSet rs = ps.executeQuery();
                rs.next();
                return new Builder(UUID.fromString(rs.getString(1)));
            }
        }
        return null;
    }

    public Review getReview() throws SQLException {
        if(getStatus() == Status.complete || isRejected()) {
            try (Connection con = DatabaseConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT idreview FROM plots WHERE idplot = ?");
                ps.setInt(1, getID());
                ResultSet rs = ps.executeQuery();
                rs.next();
                return new Review(rs.getInt(1));
            }
        }
        return null;
    }

    public String getGeoCoordinatesNumeric() throws SQLException {
        // Convert MC coordinates to geo coordinates
        Vector mcCoordinates = getMinecraftCoordinates();
        try {
            return CoordinateConversion.formatGeoCoordinatesNumeric(CoordinateConversion.convertToGeo(mcCoordinates.getX(), mcCoordinates.getZ()));
        } catch (OutOfProjectionBoundsException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not convert MC coordinates to geo coordinates!", ex);
        }
        return null;
    }

    public Vector getMinecraftCoordinates() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT mcCoordinates FROM plots WHERE idplot = ?");
            ps.setInt(1, getID());
            ResultSet rs = ps.executeQuery();
            rs.next();

            // Player MC Coordinates
            String[] mcLocation = rs.getString(1).split(",");
            return new Vector(Double.parseDouble(mcLocation[0]),Double.parseDouble(mcLocation[1]),Double.parseDouble(mcLocation[2]));
        }
    }

    public int getScore() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT score FROM plots WHERE idplot = ?");
            ps.setInt(1, getID());
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                int score = rs.getInt(1);
                if(!rs.wasNull()) {
                    return score;
                }
            }
            return -1;
        }
    }

    public Status getStatus() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT status FROM plots WHERE idplot = ?");
            ps.setInt(1, getID());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return Status.valueOf(rs.getString(1));
        }
    }

    public Date getLastActivity() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT lastActivity FROM plots WHERE idplot = ?");
            ps.setInt(1, getID());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDate(1);
        }
    }

    public Slot getSlot() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT firstSlot, secondSlot, thirdSlot FROM players WHERE uuid = ?");
            ps.setString(1, getBuilder().getUUID().toString());
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                for(int i = 1; i <= 3; i++) {
                    if(rs.getInt(i) == getID()) {
                        return Slot.values()[i - 1];
                    }
                }
            }
        }
        return null;
    }

    public String getOSMMapsLink() throws SQLException {
        return "https://www.openstreetmap.org/#map=19/" + getGeoCoordinatesNumeric().replace(",", "/");
    }

    public String getGoogleMapsLink() throws SQLException {
        return "https://www.google.com/maps/place/"+ getGeoCoordinatesNumeric();
    }

    public String getGoogleEarthLink() throws SQLException {
        return "https://earth.google.com/web/@" + getGeoCoordinatesNumeric() + ",0a,1000d,20y,-0h,0t,0r";
    }

    public String getWorldName() {
        return "P-" + getID();
    }

    public World getPlotWorld() { return Bukkit.getWorld(getWorldName()); }

    public void setBuilder(String UUID) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps;
            if(UUID == null) {
                ps = con.prepareStatement("UPDATE plots SET uuidplayer = DEFAULT(uuidplayer) WHERE idplot = ?");
                ps.setInt(1, getID());
            } else {
                ps = con.prepareStatement("UPDATE plots SET uuidplayer = ? WHERE idplot = ?");
                ps.setString(1, UUID);
                ps.setInt(2, getID());
            }
            ps.executeUpdate();
        }
    }

    public void setScore(int score) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps;
            if(score == -1) {
                ps = con.prepareStatement("UPDATE plots SET score = DEFAULT(score) WHERE idplot = ?");
                ps.setInt(1, getID());
            } else {
                ps = con.prepareStatement("UPDATE plots SET score = ? WHERE idplot = ?");
                ps.setInt(1, score);
                ps.setInt(2, getID());
            }
            ps.executeUpdate();
        }
    }

    public void setStatus(Status status) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE plots SET status = ? WHERE idplot = '" + getID() + "'");
            ps.setString(1, status.name());
            ps.executeUpdate();
        }
    }

    public void setLastActivity(boolean setNull) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps;
            if(setNull) {
                ps = con.prepareStatement("UPDATE plots SET lastActivity = DEFAULT(lastActivity) WHERE idplot = ?");
                ps.setInt(1, getID());
            } else {
                ps = con.prepareStatement("UPDATE plots SET lastActivity = ? WHERE idplot = ?");
                ps.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
                ps.setInt(2, getID());
            }
            ps.executeUpdate();
        }
    }

    public boolean isPasted() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT isPasted FROM plots WHERE idplot = '" + getID() + "'");
            rs.next();
            return rs.getBoolean(1);
        }
    }

    public boolean isReviewed() throws SQLException {
        return getReview() != null;
    }

    public boolean isRejected() throws SQLException {
        return (getStatus() == Status.unfinished || getStatus() == Status.unreviewed) && getScore() != -1; // -1 == null
    }
}
