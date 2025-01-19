package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.holograms.leaderboards.LeaderboardEntry;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.holograms.leaderboards.LeaderboardTimeframe;
import com.alpsbte.plotsystem.utils.enums.Slot;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BuilderProvider {
    public static final HashMap<UUID, Builder> builders = new HashMap<>();

    public CompletableFuture<Builder> getBuilderByUUID(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (builders.containsKey(uuid)) return builders.get(uuid);
            try (PreparedStatement stmt = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT name, score, first_slot, second_slot, third_slot, plot_type " +
                            "FROM builder WHERE uuid = ?;")) {
                stmt.setString(1, uuid.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Builder(uuid, rs.getString(1), rs.getInt(2), rs.getInt(3),
                                rs.getInt(4), rs.getInt(5), rs.getInt(6));
                    }
                }
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(ex.getMessage());
            }
            return null;
        });
    }

    public CompletableFuture<Builder> getBuilderByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT uuid FROM builder WHERE name = ?;")) {
                stmt.setString(1, name);

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.getString(1);
                }
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(ex.getMessage());
            }
            return null;
        }).thenCompose(uuid -> {
            if (uuid != null) return getBuilderByUUID(UUID.fromString(uuid));
            return null;
        });
    }

    public CompletableFuture<Boolean> setName(UUID uuid, String name) {
        CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = DatabaseConnection.getConnection()
                    .prepareStatement("UPDATE builder SET name = ? WHERE uuid = ?;")) {
                stmt.setString(1, name);
                stmt.setString(2, uuid.toString());
                stmt.executeUpdate();
                return true;
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(ex.getMessage());
            }
            return false;
        });
        return null;
    }

    public CompletableFuture<Boolean> addScore(UUID uuid, int score) {
        CompletableFuture.supplyAsync(() -> {
            try {
                try (PreparedStatement stmt = DatabaseConnection.getConnection()
                        .prepareStatement("UPDATE builder b SET score = (b.score + ?) WHERE uuid = ?;")) {
                    stmt.setInt(1, score);
                    stmt.setString(2, uuid.toString());
                    stmt.executeUpdate();
                    return true;
                }
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(ex.getMessage());
            }
            return false;
        });
        return null;
    }

    public CompletableFuture<Boolean> setSlot(UUID uuid, int plotID, Slot slot) {
        CompletableFuture.supplyAsync(() -> {
            try {
                if (plotID > 0) {
                    try (PreparedStatement stmt = DatabaseConnection.getConnection()
                            .prepareStatement("UPDATE builder b SET " + slot.name().toLowerCase() + " = ? " +
                                    "WHERE uuid = ?;")) {
                        stmt.setInt(1, plotID);
                        stmt.setString(2, uuid.toString());
                        stmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement stmt = DatabaseConnection.getConnection()
                            .prepareStatement("UPDATE builder b SET " + slot.name().toLowerCase() + " = " +
                                    "DEFAULT(first_slot) WHERE uuid = ?;")) {
                        stmt.setString(1, uuid.toString());
                        stmt.executeUpdate();
                    }
                }
                return true;
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(ex.getMessage());
            }
            return false;
        });
        return null;
    }

    public CompletableFuture<Boolean> setPlotType(UUID uuid, int plotTypeId) {
        CompletableFuture.supplyAsync(() -> {
           try {
               if (plotTypeId > 0) {
                   try (PreparedStatement stmt = DatabaseConnection.getConnection()
                           .prepareStatement("UPDATE builder b SET plot_type = ? WHERE uuid = ?;")) {
                       stmt.setInt(1, plotTypeId);
                       stmt.setString(2, uuid.toString());
                       stmt.executeUpdate();
                   }
               } else {
                   try (PreparedStatement stmt = DatabaseConnection.getConnection()
                           .prepareStatement("UPDATE builder b SET plot_type = DEFAULT(plot_type) WHERE uuid = ?;")) {
                       stmt.setString(1, uuid.toString());
                       stmt.executeUpdate();
                   }
               }
               return true;
           } catch (SQLException ex) {
               PlotSystem.getPlugin().getComponentLogger().error(ex.getMessage());
           }
           return false;
        });
        return null;
    }

    public CompletableFuture<Integer> getCompletedBuildsCount(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT COUNT(*) AS completed_plots FROM plot p INNER JOIN builder_has_plot " +
                            "bhp ON p.plot_id = bhp.plot_id WHERE p.status = 'completed' AND bhp.uuid = ?;")) {
                stmt.setString(1, uuid.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(ex.getMessage());
            }
            return 0;
        });
    }

    public CompletableFuture<Slot> getFreeSlot(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT first_slot, second_slot, third_slot FROM builder WHERE uuid = ?;")) {
                stmt.setString(1, uuid.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    for (int i = 1; i <= 3; i++) {
                        if (rs.getString(i) == null) return Slot.values()[i - 1];
                    }
                }
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(ex.getMessage());
            }
            return null;
        });
    }

    /**
     * Retrieves the leaderboard entry for a specific player based on their UUID and the specified timeframe.
     * The leaderboard entry includes the player's score, rank, and total number of players.
     *
     * @param uuid the unique identifier of the player.
     * @param sortBy the timeframe used to filter leaderboard data (e.g., daily, weekly, etc.).
     * @return provides the leaderboard entry for the player, or null if not found.
     */
    public CompletableFuture<LeaderboardEntry> getLeaderboardEntryByUUID(UUID uuid, LeaderboardTimeframe sortBy) {
        CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = DatabaseConnection.getConnection()
                    .prepareStatement(getLeaderboardQuery(uuid, sortBy))) {

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new LeaderboardEntry(rs.getInt(2), rs.getInt(3), rs.getInt(4));
                    }
                }

            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(ex.getMessage());
            }
            return null;
        });
        return null;
    }

    /**
     * Retrieves the leaderboard entries for all players within a specified timeframe, including their names and scores.
     *
     * @param sortBy the timeframe used to filter leaderboard data (e.g., daily, weekly, etc.).
     * @return provides a map of player names and their scores, or null if no data is found.
     */
    public CompletableFuture<Map<String, Integer>> getLeaderboardEntries(LeaderboardTimeframe sortBy) {
        CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = DatabaseConnection.getConnection()
                    .prepareStatement(getLeaderboardQuery(null, sortBy))) {

                try (ResultSet rs = stmt.executeQuery()) {
                    Map<String, Integer> playerEntries = new HashMap<>();
                    while (rs.next()) playerEntries.put(rs.getString(1), rs.getInt(2));
                    return playerEntries;
                }

            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(ex.getMessage());
            }
            return null;
        });
        return null;
    }

    /**
     * Constructs an SQL query to retrieve leaderboard data, optionally filtering by a specific UUID
     * and sorting based on a given timeframe.
     *
     * <p>If a UUID is provided, the query will calculate the leaderboard position for the specified
     * UUID and include the total number of leaderboard entries matching the timeframe criteria.
     * If no UUID is provided, the query will return the top leaderboard entries ordered by score
     * within the specified timeframe.</p>
     *
     * @param uuid the unique identifier of the builder for which to calculate the leaderboard position.
     *             If {@code null}, the query retrieves the top entries instead of a specific position.
     * @param sortBy the timeframe used to filter entries. Determines the minimum date for reviews
     *               (e.g., daily, weekly, monthly, yearly).
     * @return the constructed SQL query as a {@code String}.
     */
    private static String getLeaderboardQuery(@Nullable UUID uuid, LeaderboardTimeframe sortBy) {
        String minimumDate = null;
        switch (sortBy) {
            case DAILY: minimumDate = "(NOW() - INTERVAL 1 DAY)"; break;
            case WEEKLY: minimumDate = "(NOW() - INTERVAL 1 WEEK)"; break;
            case MONTHLY: minimumDate = "(NOW() - INTERVAL 1 MONTH)"; break;
            case YEARLY: minimumDate = "(NOW() - INTERVAL 1 YEAR)"; break;
        }

        return "SELECT b.name, b.score" + (uuid != null ? ", ROW_NUMBER() OVER (ORDER BY b.score DESC) AS position" : "") +
                (uuid != null ? ", (SELECT COUNT(*) FROM builder_has_plot bhp_sub " +
                        "INNER JOIN builder b_sub ON b_sub.uuid = bhp_sub.uuid " +
                        "INNER JOIN plot_review r_sub ON r_sub.plot_id = bhp_sub.plot_id " +
                        (minimumDate != null ? "WHERE r.review_date BETWEEN " + minimumDate + " AND NOW()" : "") + ") " +
                        "AS total_positions" : "") +
                " FROM builder_has_plot bhp" +
                "INNER JOIN builder b ON b.uuid = bhp.uuid" +
                "INNER JOIN plot_review r ON r.plot_id = bhp.plot_id" +
                (minimumDate != null
                        ? "WHERE r.review_date BETWEEN " + minimumDate + " AND NOW()"
                        : "") +
                (uuid != null
                        ? "WHERE b.uuid = ?;"
                        : "ORDER BY b.name, b.score DESC LIMIT 10;"
                );
    }
}
