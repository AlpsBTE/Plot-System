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
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.holograms.leaderboards.LeaderboardTimeframe;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.enums.Slot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BuilderProvider {
    public static final Map<UUID, Builder> builders = new HashMap<>();

    public Builder getBuilderByUUID(UUID uuid) {
        if (builders.containsKey(uuid)) return builders.get(uuid);

        String query = "SELECT name, score, first_slot, second_slot, third_slot, plot_type FROM builder WHERE uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Builder(uuid, rs.getString(1), rs.getInt(2), rs.getInt(3),
                            rs.getInt(4), rs.getInt(5), rs.getInt(6));
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return null;
    }

    public Builder getBuilderByName(String name) {
        String query = "SELECT uuid FROM builder WHERE name = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                String uuid = rs.getString(1);
                if (uuid != null) return getBuilderByUUID(UUID.fromString(uuid));
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return null;
    }

    public boolean addBuilderIfNotExists(UUID uuid, String name) {
        if (builders.containsKey(uuid)) return true;

        String selectQuery = "SELECT 1 FROM builder WHERE uuid = ?;";
        String insertQuery = "INSERT INTO builder (uuid, name, plot_type) VALUES (?, ?, 1);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
            selectStmt.setString(1, uuid.toString());
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) return true;
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, uuid.toString());
                insertStmt.setString(2, name);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setName(UUID uuid, String name) {
        String query = "UPDATE builder SET name = ? WHERE uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean addScore(UUID uuid, int score) {
        String query = "UPDATE builder b SET score = (b.score + ?) WHERE uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, score);
            stmt.setString(2, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setSlot(UUID uuid, int plotID, Slot slot) {
        String query = "UPDATE builder b SET " + slot.name().toLowerCase() + "_slot = " +
                (plotID > 0 ? "?" : "DEFAULT(first_slot)") + " WHERE uuid = ?;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (plotID > 0) stmt.setInt(1, plotID);
            stmt.setString(plotID > 0 ? 2 : 1, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setPlotType(UUID uuid, int plotTypeId) {
        String query = "UPDATE builder b SET plot_type = " +
                (plotTypeId > 0 ? "?" : "DEFAULT(plot_type)") + " WHERE uuid = ?;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (plotTypeId > 0) stmt.setInt(1, plotTypeId);
            stmt.setString(plotTypeId > 0 ? 2 : 1, uuid.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public int getCompletedBuildsCount(UUID uuid) {
        String query = "SELECT COUNT(p.plot_id) AS completed_plots FROM plot p INNER JOIN builder_is_plot_member " +
                "bipm ON p.plot_id = bipm.plot_id WHERE p.status = 'completed' AND (p.owner_uuid = ? OR bipm.uuid = ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return 0;
    }

    public Slot getFreeSlot(UUID uuid) {
        String query = "SELECT first_slot, second_slot, third_slot FROM builder WHERE uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                for (int i = 1; i <= 3; i++) {
                    if (rs.getString(i) == null) return Slot.values()[i - 1];
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return null;
    }

    public Slot getSlot(UUID uuid, int plotId) {
        String query = "SELECT first_slot, second_slot, third_slot FROM builder WHERE uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                if (rs.getInt(1) == plotId) return Slot.FIRST;
                if (rs.getInt(2) == plotId) return Slot.SECOND;
                if (rs.getInt(3) == plotId) return Slot.THIRD;
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canReviewPlot(UUID uuid, Plot plot) {
        // TODO: cache
        String query = "SELECT build_team_id FROM build_team_has_reviewer WHERE uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<BuildTeam> team = DataProvider.BUILD_TEAM.getBuildTeam(rs.getInt(1));
                    if (team.isEmpty()) continue;
                    if (team.get().getCityProjects().stream().anyMatch(c -> Objects.equals(c.getID(), plot.getCityProject().getID())))
                        return true;
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean isAnyReviewer(UUID uuid) {
        // TODO: cache
        String query = "SELECT uuid FROM build_team_has_reviewer WHERE uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return true;
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public List<Builder> getReviewersByBuildTeam(int buildTeamId) {
        List<Builder> builders = new ArrayList<>();
        String query = "SELECT uuid FROM build_team_has_reviewer WHERE build_team_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, buildTeamId);
            try (ResultSet rs = stmt.executeQuery()) {
                stmt.setInt(1, buildTeamId);

                while (rs.next()) {
                    Builder builder = getBuilderByUUID(UUID.fromString(rs.getString(1)));
                    if (builder != null) builders.add(builder);
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return builders;
    }

    /**
     * Retrieves the leaderboard entries for all players within a specified timeframe, including their names and scores.
     *
     * @param sortBy the timeframe used to filter leaderboard data (e.g., daily, weekly, etc.).
     * @return provides a map of player names and their scores, or null if no data is found.
     */
    public LinkedHashMap<String, Integer> getLeaderboardEntries(LeaderboardTimeframe sortBy) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(getLeaderboardQuery(sortBy))) {

            try (ResultSet rs = stmt.executeQuery()) {
                LinkedHashMap<String, Integer> playerEntries = new LinkedHashMap<>();
                while (rs.next()) playerEntries.put(rs.getString(1), rs.getInt(2));
                return playerEntries;
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return null;
    }

    /**
     * Constructs a SQL query to retrieve leaderboard data by sorting based on a given timeframe.
     *
     * <p>This query returns the top leaderboard entries ordered by the score
     * within the specified timeframe.</p>
     *
     * @param sortBy the timeframe used to filter entries. Determines the minimum date for reviews
     *               (e.g., daily, weekly, monthly, yearly).
     * @return the constructed SQL query as a {@code String}.
     */
    private static String getLeaderboardQuery(LeaderboardTimeframe sortBy) {
        String minimumDate = switch (sortBy) {
            case DAILY -> "(NOW() - INTERVAL 1 DAY)";
            case WEEKLY -> "(NOW() - INTERVAL 1 WEEK)";
            case MONTHLY -> "(NOW() - INTERVAL 1 MONTH)";
            case YEARLY -> "(NOW() - INTERVAL 1 YEAR)";
            default -> null;
        };

        return "WITH latest_reviews AS ( "
                + "    SELECT pr.* "
                + "    FROM plot_review pr "
                + "    INNER JOIN ( "
                + "        SELECT plot_id, MAX(review_date) AS latest_review_date "
                + "        FROM plot_review "
                + "        GROUP BY plot_id "
                + "    ) latest ON pr.plot_id = latest.plot_id AND pr.review_date = latest.latest_review_date "
                + "), "
                + "plot_member_counts AS ( "
                + "    SELECT plot_id, COUNT(*) AS member_count "
                + "    FROM builder_is_plot_member "
                + "    GROUP BY plot_id "
                + "), "
                + "all_builders AS ( "
                + "    SELECT "
                + "        p.owner_uuid AS builder_uuid, "
                + "        p.plot_id "
                + "    FROM plot p "
                + "    WHERE p.status = 'completed' "
                + "    UNION ALL "
                + "    SELECT "
                + "        bipm.uuid AS builder_uuid, "
                + "        bipm.plot_id "
                + "    FROM builder_is_plot_member bipm "
                + "    JOIN plot p ON p.plot_id = bipm.plot_id "
                + "    WHERE p.status = 'completed' "
                + ") "
                + "SELECT b.name, SUM( "
                + "    IF(pmc.member_count IS NULL OR pmc.member_count = 0, lr.score, FLOOR(lr.score / (pmc.member_count + 1))) "
                + ") AS total_score "
                + "FROM all_builders ab "
                + "JOIN builder b ON b.uuid = ab.builder_uuid "
                + "JOIN latest_reviews lr ON lr.plot_id = ab.plot_id "
                + "LEFT JOIN plot_member_counts pmc ON pmc.plot_id = ab.plot_id "
                + (minimumDate != null ? "WHERE lr.review_date BETWEEN " + minimumDate + " AND NOW() " : "")
                + "GROUP BY b.name "
                + "ORDER BY total_score DESC, b.name "
                + "LIMIT 10;";
    }
}