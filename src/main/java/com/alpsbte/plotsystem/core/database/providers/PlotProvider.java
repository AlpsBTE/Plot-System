package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.Review;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotProvider {
    public Plot getPlotById(int plotId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement("SELECT city_project_id, difficulty_id, status," +
                    " score, outline_bounds, last_activity_date, plot_version FROM plot WHERE plot_id = ?")) {
                stmt.setInt(1, plotId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Status status = Status.valueOf(rs.getString(3));
                        if (status != Status.unclaimed) {
                            try (PreparedStatement stmt2 = con.prepareStatement("SELECT uuid, 'owner' AS role FROM " +
                                    "builder_is_plot_owner WHERE plot_id = ? UNION SELECT uuid, 'member' AS role FROM " +
                                    "builder_is_plot_member WHERE plot_id = ?;")) {
                                stmt2.setInt(1, plotId);
                                stmt2.setInt(2, plotId);
                                UUID plotOwner;
                                List<UUID> memberUUIDs = new ArrayList<>();
                                try (ResultSet rs2 = stmt2.executeQuery()) {
                                    while (rs2.next()) {
                                        UUID uuid = UUID.fromString(rs2.getString(1));
                                        String role = rs2.getString(2);
                                        if (role.equals("owner")) plotOwner = uuid;
                                        else memberUUIDs.add(uuid);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) { Utils.logSqlException(ex); }
        return null;
    }

    public List<Plot> getPlots(Status status) {
        // TODO: implement
        return List.of();
    }

    public List<Plot> getPlots(CityProject city, Status... statuses) {
        // TODO: implement
        return List.of();
    }

    public List<Plot> getPlots(CityProject city, PlotDifficulty plotDifficulty, Status status) {
        // TODO: implement
        return List.of();
    }

    public List<Plot> getPlots(List<CityProject> cities, Status... statuses) {
        // TODO: implement
        return List.of();
    }

    public List<Plot> getPlots(List<Country> countries, Status status) {
        List<CityProject> cities = new ArrayList<>();
        countries.forEach(c -> cities.addAll(c.getCityProjects()));
        return getPlots(cities, status);
    }

    public List<Plot> getPlots(Builder builder) {
        // TODO: get plots where builder is either owner or member
        return List.of();
    }

    public List<Plot> getPlots(Builder builder, Status... statuses) {
        // TODO: get plots where builder is either owner or member and filter by status
        return List.of();
    }

    public List<Builder> getPlotMembers(int plotId) {
        // TODO: implement
        return null;
    }

    public Review getReview(int plotId) {
        // TODO: implement
        return null;
    }

    public byte[] getInitialSchematic(int plotId) {
        // TODO: implement
        return null;
    }

    public byte[] getCompletedSchematic(int plotId) {
        // TODO: implement
        return null;
    }

    public boolean setCompletedSchematic(int plotId, byte[] completedSchematic) {
        // TODO: implement
        // set to default if completedSchematic is null (for undoing review)
        return false;
    }

    public boolean setPlotOwner(int plotId, UUID ownerUUID) {
        // TODO: implement
        // (should also be able to be set to null in case a plot gets abandoned)
        return false;
    }

    public boolean setLastActivity(int plotId, LocalDate activityDate) {
        // TODO: implement
        // set last_activity to null if activityDate is null
        return false;
    }

    public boolean setStatus(int plotId, Status status) {
        // TODO: implement
        return false;
    }

    public boolean setPlotMembers(int plotId, List<Builder> members) {
        // TODO: implement
        return false;
    }

    public boolean setPlotType(int plotId, PlotType type) {
        // TODO: implement
        return false;
    }

    public boolean deletePlot(int plotId) {
        // TODO: implement
        return false;
    }

    public TutorialPlot getTutorialPlotById(int tutorialPlotId) {
        // TODO: implement
        return null;
    }
}
