/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.*;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotProvider {
    private static final String PLOT_SQL_COLUMNS = "plot.plot_id, plot.city_project_id, plot.difficulty_id, " +
            "plot.owner_uuid, plot.status, plot.score, plot.outline_bounds, plot.last_activity_date, " +
            "plot.plot_version, plot.plot_type";

    public Plot getPlotById(int plotId) {
        String query = "SELECT city_project_id, difficulty_id, owner_uuid, status, score, " +
                "outline_bounds, last_activity_date, plot_version, plot_type FROM plot WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, plotId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                CityProject cityProject = DataProvider.CITY_PROJECT.getById(rs.getString(1)).orElseThrow();
                PlotDifficulty difficulty = DataProvider.DIFFICULTY.getDifficultyById(rs.getString(2)).orElseThrow().getDifficulty();
                UUID ownerUUID = UUID.fromString(rs.getString(3));
                Status status = Status.valueOf(rs.getString(4));
                int score = rs.getInt(5);
                String outlineBounds = rs.getString(6);
                LocalDate lastActivity = rs.getDate(7).toLocalDate();
                double version = rs.getDouble(8);
                PlotType type = PlotType.byId(rs.getInt(9));

                return new Plot(plotId, cityProject, difficulty, ownerUUID, status, score, outlineBounds,
                        lastActivity, version, type, getPlotMembers(plotId));
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return null;
    }

    public List<Plot> getPlots(Status status) {
        String query = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot WHERE status = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Plot> plots = new ArrayList<>();
                while (rs.next()) {
                    plots.add(extractPlot(rs));
                }
                return plots;
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return List.of();
    }

    public List<Plot> getPlots(CityProject city, Status... statuses) {
        String query = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot WHERE city_project_id = ?";
        query += statuses.length > 0 ? " AND status IN (" + "?,".repeat(statuses.length - 1) + "?);" : ";";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, city.getID());
            for (int i = 0; i < statuses.length; i++) stmt.setString(i + 2, statuses[i].name());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Plot> plots = new ArrayList<>();
                while (rs.next()) plots.add(extractPlot(rs));
                return plots;
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return List.of();
    }

    public List<Plot> getPlots(CityProject city, PlotDifficulty plotDifficulty, Status status) {
        String query = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot WHERE city_project_id = ? AND difficulty_id = ? " +
                "AND status = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, city.getID());
            stmt.setString(2, plotDifficulty.name());
            stmt.setString(3, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Plot> plots = new ArrayList<>();
                while (rs.next()) plots.add(extractPlot(rs));
                return plots;
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return List.of();
    }

    public List<Plot> getPlots(List<CityProject> cities, Status... statuses) {
        if (cities.isEmpty()) return List.of();

        String cityPlaceholders = "?,".repeat(cities.size() - 1) + "?";
        String query = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot WHERE city_project_id IN (" + cityPlaceholders + ")";
        query += statuses.length > 0 ? " AND status IN (?,".repeat(statuses.length - 1) + "?);" : ";";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            int index = 1;
            for (CityProject city : cities) stmt.setString(index++, city.getID());
            for (Status status : statuses) stmt.setString(index++, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Plot> plots = new ArrayList<>();
                while (rs.next()) plots.add(extractPlot(rs));
                return plots;
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return List.of();
    }

    public List<Plot> getPlots(Builder builder, Status... statuses) {
        String query = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot LEFT JOIN builder_is_plot_member pm ON " +
                "plot.plot_id = pm.plot_id WHERE (plot.owner_uuid = ? OR pm.uuid = ?)";
        query += statuses.length > 0 ? "AND plot.status IN (?,".repeat(statuses.length - 1) + "?);" : ";";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String builderUUID = builder.getUUID().toString();
            stmt.setString(1, builderUUID);
            stmt.setString(2, builderUUID);
            for (int i = 0; i < statuses.length; i++) stmt.setString(i + 3, statuses[i].name());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Plot> plots = new ArrayList<>();
                while (rs.next()) plots.add(extractPlot(rs));
                return plots;
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return List.of();
    }

    private Plot extractPlot(ResultSet rs) throws SQLException {
        int plotId = rs.getInt("plot_id");
        CityProject cityProject = DataProvider.CITY_PROJECT.getById(rs.getString("city_project_id")).orElseThrow();
        PlotDifficulty difficulty = DataProvider.DIFFICULTY.getDifficultyById(rs.getString("difficulty_id")).orElseThrow().getDifficulty();
        UUID ownerUUID = UUID.fromString(rs.getString("owner_uuid"));
        Status status = Status.valueOf(rs.getString("status"));
        int score = rs.getInt("score");
        String outlineBounds = rs.getString("outline_bounds");
        LocalDate lastActivity = rs.getDate("last_activity_date").toLocalDate();
        double version = rs.getDouble("plot_version");
        PlotType type = PlotType.byId(rs.getInt("plot_type"));

        return new Plot(plotId, cityProject, difficulty, ownerUUID, status, score, outlineBounds, lastActivity, version, type, getPlotMembers(plotId));
    }

    public List<Builder> getPlotMembers(int plotId) {
        String query = "SELECT uuid FROM builder_is_plot_member WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, plotId);

            List<Builder> members = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    members.add(Builder.byUUID(UUID.fromString(rs.getString(1))));
            }
            return members;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return new ArrayList<>();
    }

    public Review getReview(int plotId) {
        // TODO: implement
        return null;
    }

    public byte[] getInitialSchematic(int plotId) {
        return getSchematic(plotId, "initial_schematic");
    }

    public byte[] getCompletedSchematic(int plotId) {
        return getSchematic(plotId, "complete_schematic");
    }

    private byte[] getSchematic(int plotId, String name) {
        String query = "SELECT ? FROM plot WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, plotId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getBytes(1);
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return null;
    }

    public boolean setCompletedSchematic(int plotId, byte[] completedSchematic) {
        String query = "UPDATE plot SET complete_schematic = ? WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBytes(1, completedSchematic);
            stmt.setInt(2, plotId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setPlotOwner(int plotId, UUID ownerUUID) {
        String updateQuery = "UPDATE plot SET owner_uuid = ? WHERE plot_id = ?;";
        String defaultUpdateQuery = "UPDATE plot SET owner_uuid = DEFAULT(owner_uuid) WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(ownerUUID == null ? defaultUpdateQuery : updateQuery)) {
            if (ownerUUID == null) {
                stmt.setInt(1, plotId);
            } else {
                stmt.setString(1, ownerUUID.toString());
                stmt.setInt(2, plotId);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setLastActivity(int plotId, LocalDate activityDate) {
        String query = "UPDATE plot SET last_activity_date = ? WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDate(1, activityDate == null ? null : Date.valueOf(activityDate));
            stmt.setInt(2, plotId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setStatus(int plotId, Status status) {
        String query = "UPDATE plot SET status = ? WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, plotId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setPlotMembers(int plotId, List<Builder> members) {
        String deleteQuery = "DELETE FROM builder_is_plot_member WHERE plot_id = ?;";
        String insertQuery = "INSERT INTO builder_is_plot_member (plot_id, uuid) VALUES (?, ?);";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                 PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

                // Delete existing members
                deleteStmt.setInt(1, plotId);
                deleteStmt.executeUpdate();

                // Insert new members if list is not empty
                if (!members.isEmpty()) {
                    for (Builder member : members) {
                        insertStmt.setInt(1, plotId);
                        insertStmt.setString(2, member.getUUID().toString());
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }

                conn.commit(); // Commit transaction
                return true;
            } catch (SQLException ex) {
                conn.rollback();  // Rollback if anything fails
                Utils.logSqlException(ex);
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setPlotType(int plotId, PlotType type) {
        String query = "UPDATE plot SET plot_type = ? WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, type.getId());
            stmt.setInt(2, plotId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setPasted(int plotId, boolean pasted) {
        String query = "UPDATE plot SET is_pasted = ? WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, pasted);
            stmt.setInt(2, plotId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean deletePlot(int plotId) {
        String query = "DELETE FROM plot WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, plotId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }
}
