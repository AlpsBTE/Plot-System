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

package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Review;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.conversion.CoordinateConversion;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.ftp.FTPManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Plot implements IPlot {
    private final int ID;

    private List<BlockVector2D> outline;

    private PlotWorld plotWorld;
    private PlotPermissions plotPermissions;

    public Plot(int ID) throws SQLException {
        this.ID = ID;
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public CityProject getCity() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT city_project_id FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()){
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return new CityProject(i);
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    @Override
    public PlotDifficulty getDifficulty() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT difficulty_id FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()){
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return PlotDifficulty.values()[i - 1];
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    @Override
    public List<BlockVector2D> getOutline() throws SQLException {
        if(outline != null)
            return outline;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT outline FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()){
                List<BlockVector2D> locations = new ArrayList<>();
                String[] list = rs.getString(1).split("\\|");

                for(String s : list) {
                    String[] locs = s.split(",");
                    locations.add(new BlockVector2D(Double.parseDouble(locs[0]), Double.parseDouble(locs[1])));
                }

                outline = locations;

                DatabaseConnection.closeResultSet(rs);
                return locations;
            }

            DatabaseConnection.closeResultSet(rs);
        }

        return null;
    }

    @Override
    public Builder getPlotOwner() throws SQLException {
        if(getStatus() != Status.unclaimed) {
            try (ResultSet rs = DatabaseConnection.createStatement("SELECT owner_uuid FROM plotsystem_plots WHERE id = ?")
                    .setValue(this.ID).executeQuery()) {

                if (rs.next()){
                    String s = rs.getString(1);
                    DatabaseConnection.closeResultSet(rs);
                    return new Builder(UUID.fromString(s));
                }

                DatabaseConnection.closeResultSet(rs);
            }
        }
        return null;
    }

    @Override
    public void setPlotOwner(String UUID) throws SQLException {
        if (UUID == null) {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET owner_uuid = DEFAULT(owner_uuid) WHERE id = ?")
                    .setValue(this.ID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET owner_uuid = ? WHERE id = ?")
                    .setValue(UUID).setValue(this.ID).executeUpdate();
        }
    }

    @Override
    public List<Builder> getPlotMembers() throws SQLException {
        List<Builder> builders = new ArrayList<>();

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT member_uuids FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if(rs.next()) {
                String members = rs.getString(1);
                if(!rs.wasNull()) {
                    String[] uuidMembers = members.split(",");

                    for (String uuid : uuidMembers) {
                        builders.add(new Builder(UUID.fromString(uuid)));
                    }
                }
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return builders;
    }

    @Override
    public void setPlotMembers(@NotNull List<Builder> plotMembers) throws SQLException {
        // Convert plot member list to string
        String plotMemberAsString = plotMembers.stream().map(member -> member.getUUID().toString()).collect(Collectors.joining(","));
        Bukkit.getLogger().log(Level.SEVERE,plotMemberAsString);

        if(!plotMembers.isEmpty()) {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET member_uuids = ? WHERE id = ?")
                    .setValue(plotMemberAsString).setValue(this.ID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET member_uuids = DEFAULT(member_uuids) WHERE id = ?")
                    .setValue(this.ID).executeUpdate();
        }
    }

    @Override
    public Review getReview() throws SQLException {
        if(getStatus() == Status.completed || isRejected()) {
            try (ResultSet rs = DatabaseConnection.createStatement("SELECT review_id FROM plotsystem_plots WHERE id = ?")
                    .setValue(this.ID).executeQuery()) {

                if (rs.next()) {
                    int i = rs.getInt(1);
                    DatabaseConnection.closeResultSet(rs);
                    return new Review(i);
                }

                DatabaseConnection.closeResultSet(rs);
            }
        }
        return null;
    }

    @Override
    public PlotWorld getWorld() {
        if (plotWorld == null) plotWorld = new PlotWorld(this);
        return plotWorld;
    }

    @Override
    public PlotPermissions getPermissions() {
        if (plotPermissions == null) plotPermissions = new PlotPermissions(getWorld());
        return plotPermissions;
    }

    @Override
    public int getTotalScore() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT score FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if(rs.next()) {
                int score = rs.getInt(1);
                if(!rs.wasNull()) {
                    DatabaseConnection.closeResultSet(rs);
                    return score;
                }
            }

            DatabaseConnection.closeResultSet(rs);

            return -1;
        }
    }

    @Override
    public int getSharedScore() throws SQLException {
        int score = getTotalScore();
        if (score != -1 && !getPlotMembers().isEmpty()) {
            return (int) Math.floor(score / (getPlotMembers().size() + 1d));
        }
        return score;
    }

    @Override
    public void setTotalScore(int score) throws SQLException {
        if (score == -1) {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET score = DEFAULT(score) WHERE id = ?")
                    .setValue(this.ID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET score = ? WHERE id = ?")
                    .setValue(score).setValue(this.ID).executeUpdate();
        }
    }

    @Override
    public Status getStatus() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT status FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return Status.valueOf(s);
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    @Override
    public void setStatus(@NotNull Status status) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_plots SET status = ? WHERE id = ?")
                .setValue(status.name()).setValue(this.ID).executeUpdate();
    }

    @Override
    public Date getLastActivity() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT last_activity FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                Date d = rs.getDate(1);
                DatabaseConnection.closeResultSet(rs);
                return d;
            }

            DatabaseConnection.closeResultSet(rs);

            return null;
        }
    }

    @Override
    public void setLastActivity(boolean setNull) throws SQLException {
        if(setNull) {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET last_activity = DEFAULT(last_activity) WHERE id = ?")
                    .setValue(this.ID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET last_activity = ? WHERE id = ?")
                    .setValue(java.sql.Date.valueOf(LocalDate.now())).setValue(this.ID).executeUpdate();
        }
    }

    @Override
    public Date getCreateDate() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT create_date FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                Date d = rs.getDate(1);
                DatabaseConnection.closeResultSet(rs);
                return d;
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    @Override
    public Builder getPlotCreator() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT create_player FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return new Builder(UUID.fromString(s));
            }

            DatabaseConnection.closeResultSet(rs);

            return null;
        }
    }

    @Override
    public Slot getSlot() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT first_slot, second_slot, third_slot FROM plotsystem_builders WHERE uuid = ?")
                .setValue(this.getPlotOwner().getUUID().toString()).executeQuery()) {

            if(rs.next()) {
                for(int i = 1; i <= 3; i++) {
                    int slot = rs.getInt(i);
                    if(!rs.wasNull() && slot == getID()) {
                        DatabaseConnection.closeResultSet(rs);
                        return Slot.values()[i - 1];
                    }
                }
            }

            DatabaseConnection.closeResultSet(rs);

            return null;
        }
    }

    @Override
    public File getOutlinesSchematic() {
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    File file = Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(getCity().getCountry().getServer().getID()), String.valueOf(getCity().getID()), getID() + ".schematic").toFile();

                    if(!file.exists()) {
                        if (getCity().getCountry().getServer().getFTPConfiguration() != null) {
                            FTPManager.downloadSchematic(FTPManager.getFTPUrl(getCity().getCountry().getServer(), getCity().getID()), file);
                        }
                    }
                    return file;
                } catch (SQLException | URISyntaxException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException ex) {
            return null;
        }
    }

    @Override
    public File getFinishedSchematic() {
        try {
            return Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(getCity().getCountry().getServer().getID()), "finishedSchematics", String.valueOf(getCity().getID()), getID() + ".schematic").toFile();
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return null;
    }

    @Override
    public String getGeoCoordinates() throws SQLException {
        // Convert MC coordinates to geo coordinates
        Vector mcCoordinates = getMinecraftCoordinates();
        try {
            return CoordinateConversion.formatGeoCoordinatesNumeric(CoordinateConversion.convertToGeo(mcCoordinates.getX(), mcCoordinates.getZ()));
        } catch (OutOfProjectionBoundsException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not convert MC coordinates to geo coordinates!", ex);
        }
        return null;
    }

    @Override
    public Vector getMinecraftCoordinates() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT mc_coordinates FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                String[] mcLocation = rs.getString(1).split(",");
                DatabaseConnection.closeResultSet(rs);
                return new Vector(Double.parseDouble(mcLocation[0]), Double.parseDouble(mcLocation[1]), Double.parseDouble(mcLocation[2]));
            }

            DatabaseConnection.closeResultSet(rs);

            return null;
        }
    }

    public Vector getCenter(){
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT mc_coordinates FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()){
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);

                return Vector.toBlockPoint(
                        Double.parseDouble(s.split(",")[0]),
                        Double.parseDouble(s.split(",")[1]),
                        Double.parseDouble(s.split(",")[2])
                );
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }catch (Exception ex){
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return null;
        }
    }

    public String getOSMMapsLink() throws SQLException {
        return "https://www.openstreetmap.org/#map=19/" + getGeoCoordinates().replace(",", "/");
    }

    public String getGoogleMapsLink() throws SQLException {
        return "https://www.google.com/maps/place/"+ getGeoCoordinates();
    }

    public String getGoogleEarthLink() throws SQLException {
        return "https://earth.google.com/web/@" + getGeoCoordinates() + ",0a,1000d,20y,-0h,0t,0r";
    }

    @Override
    public void setPasted(boolean pasted) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_plots SET pasted = ? WHERE id = ?")
                .setValue(pasted).setValue(this.ID).executeUpdate();
    }

    public void addPlotMember(Builder member) throws SQLException {
        List<Builder> members = getPlotMembers();
        if (members.size() < 3 && members.stream().noneMatch(m -> m.getUUID().equals(member.getUUID()))) {
            Slot slot = member.getFreeSlot();
            if (slot != null) {
                members.add(member);
                setPlotMembers(members);

                member.setPlot(this.ID, slot);
                getPermissions().addBuilderPerms(member.getUUID());
            }
        }
    }

    public void removePlotMember(Builder member) throws SQLException {
        List<Builder> members = getPlotMembers();
        if (!members.isEmpty() && members.stream().anyMatch(m -> m.getUUID().equals(member.getUUID()))) {
            members.remove(members.stream().filter(m -> m.getUUID().equals(member.getUUID())).findFirst().orElse(null));
            setPlotMembers(members);

            Slot slot = member.getSlot(this);
            if (slot != null) {
                member.removePlot(slot);
            }
            if (getWorld().isWorldGenerated()) getPermissions().removeBuilderPerms(member.getUUID());
        }
    }

    @Override
    public boolean isReviewed() throws SQLException {
        return getReview() != null;
    }

    public boolean isRejected() throws SQLException {
        return (getStatus() == Status.unfinished || getStatus() == Status.unreviewed) && getTotalScore() != -1; // -1 == null
    }
}
