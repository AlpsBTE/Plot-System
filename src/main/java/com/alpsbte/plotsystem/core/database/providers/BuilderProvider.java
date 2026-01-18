package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.alpslib.io.database.SqlHelper;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.holograms.leaderboards.LeaderboardTimeframe;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Slot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BuilderProvider {
    protected static final Map<UUID, Builder> BUILDERS = new HashMap<>();
    private static final String Q_SLOTS_BY_UUID = "SELECT first_slot, second_slot, third_slot FROM builder WHERE uuid = ?;";

    public Builder getBuilderByUUID(UUID uuid) {
        if (BUILDERS.containsKey(uuid)) return BUILDERS.get(uuid);

        String qByUuid = "SELECT name, score, first_slot, second_slot, third_slot, plot_type FROM builder WHERE uuid = ?;";
        return Utils.handleSqlException(null, () -> SqlHelper.runQuery(qByUuid, ps -> {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Builder builder = new Builder(uuid, rs.getString(1), rs.getInt(2), rs.getInt(3),
                            rs.getInt(4), rs.getInt(5), rs.getInt(6));
                BUILDERS.put(uuid, builder); // cache the builder
                return builder;
            }
            return null;
        }));
    }

    public Builder getBuilderByName(String name) {
        for (var i : BUILDERS.values()) {
            if (i.getName().equalsIgnoreCase(name)) {
                return i; // return cached builder if name matches
            }
        }

        String qUuidByName = "SELECT uuid FROM builder WHERE name = ?;";
        return Utils.handleSqlException(null, () -> SqlHelper.runQuery(qUuidByName, ps -> {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String uuid = rs.getString(1);
                if (uuid != null) return getBuilderByUUID(UUID.fromString(uuid));
            }
            return null;
        }));
    }

    public boolean addBuilderIfNotExists(UUID uuid, String name) {
        if (BUILDERS.containsKey(uuid)) return true;

        String qExistsByUuid = "SELECT 1 FROM builder WHERE uuid = ?;";
        String qInsert = "INSERT INTO builder (uuid, name, plot_type) VALUES (?, ?, 1);";
        // builder already exists
        // insert new builder
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qExistsByUuid, ps -> {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return true; // builder already exists

            return SqlHelper.runQuery(qInsert, ps.getConnection(), insertStmt -> {
                insertStmt.setString(1, uuid.toString());
                insertStmt.setString(2, name);
                return insertStmt.executeUpdate() > 0; // insert new builder
            });
        })));
    }

    public boolean setName(@NotNull UUID uuid, String name) {
        String qSetNameByUuid = "UPDATE builder SET name = ? WHERE uuid = ?;";
        // update builder name
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetNameByUuid, ps -> {
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            return ps.executeUpdate() > 0; // update builder name
        })));
    }

    public boolean addScore(@NotNull UUID uuid, int score) {
        String qIncreaseScoreByUuid = "UPDATE builder b SET score = (b.score + ?) WHERE uuid = ?;";
        // increase score by given value
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qIncreaseScoreByUuid, ps -> {
            ps.setInt(1, score);
            ps.setString(2, uuid.toString());
            return ps.executeUpdate() > 0; // increase score by given value
        })));
    }

    public boolean setSlot(UUID uuid, int plotID, @NotNull Slot slot) {
        String qBuilderSetSlotByUuid = "UPDATE builder b SET " + slot.name().toLowerCase() + "_slot = " + (plotID > 0 ? "?" : "DEFAULT(first_slot)") + " WHERE uuid = ?;";
        // update builder slot
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qBuilderSetSlotByUuid, ps -> {
            if (plotID > 0) ps.setInt(1, plotID);
            ps.setString(plotID > 0 ? 2 : 1, uuid.toString());
            return ps.executeUpdate() > 0; // update builder slot
        })));
    }

    public boolean setPlotType(@NotNull UUID uuid, int plotTypeId) {
        String qSetPlotTypeByUuid = "UPDATE builder b SET plot_type = ? WHERE uuid = ?;";
        // update plot type
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetPlotTypeByUuid, ps -> {
            ps.setInt(1, plotTypeId);
            ps.setString(2, uuid.toString());
            return ps.executeUpdate() > 0; // update plot type
        })));
    }

    public int getCompletedBuildsCount(@NotNull UUID uuid) {
        String qBuilderCompletedBuildsCountByUuid = "SELECT COUNT(p.plot_id) AS completed_plots FROM plot p INNER JOIN builder_is_plot_member " +
                "bipm ON p.plot_id = bipm.plot_id WHERE p.status = 'completed' AND (p.owner_uuid = ? OR bipm.uuid = ?);";
        Integer result = Utils.handleSqlException(0, () -> SqlHelper.runQuery(qBuilderCompletedBuildsCountByUuid, ps -> {
            ps.setString(1, uuid.toString());
            ps.setString(2, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        }));
        return result != null ? result : 0;
    }

    public Slot getFreeSlot(@NotNull UUID uuid) {
        return Utils.handleSqlException(null, () -> SqlHelper.runQuery(Q_SLOTS_BY_UUID, ps -> {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null; // no slots found
            for (int i = 1; i <= 3; i++) {
                if (rs.getString(i) == null) return Slot.values()[i - 1]; // return first free slot
            }
            return null; // no free slots found
        }));
    }

    public Slot getSlot(@NotNull UUID uuid, int plotId) {
        return Utils.handleSqlException(null, () -> SqlHelper.runQuery(Q_SLOTS_BY_UUID, ps -> {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null; // no slots found
            if (rs.getInt(1) == plotId) return Slot.FIRST; // first slot
            if (rs.getInt(2) == plotId) return Slot.SECOND; // second slot
            if (rs.getInt(3) == plotId) return Slot.THIRD; // third slot
            return null; // no matching slot found
        }));
    }

    public boolean canNotReviewPlot(@NotNull UUID uuid, Plot plot) {
        return DataProvider.BUILD_TEAM.getReviewerCities(uuid).stream().noneMatch(c -> c.getId().equals(plot.getCityProject().getId()));
    }

    public List<Builder> getReviewersByBuildTeam(int buildTeamId) {
        String qReviewerUuidByBtId = "SELECT uuid FROM build_team_has_reviewer WHERE build_team_id = ?;";
        return Utils.handleSqlException(new ArrayList<>(), () -> SqlHelper.runQuery(qReviewerUuidByBtId, ps -> {
            ps.setInt(1, buildTeamId);
            ResultSet rs = ps.executeQuery();
            List<Builder> builders = new ArrayList<>();
            while (rs.next()) {
                Builder builder = getBuilderByUUID(UUID.fromString(rs.getString(1)));
                if (builder != null) builders.add(builder);
            }
            return builders;
        }));
    }

    /**
     * Retrieves the leaderboard entries for all players within a specified timeframe, including their names and scores.
     *
     * @param sortBy the timeframe used to filter leaderboard data (e.g., daily, weekly, etc.).
     * @return provides a map of player names and their scores, or an empty map if no data is found.
     */
    public Map<String, Integer> getLeaderboardEntries(LeaderboardTimeframe sortBy) {
        return Utils.handleSqlException(new LinkedHashMap<>(), () -> SqlHelper.runQuery(getLeaderboardQuery(sortBy), ps -> {
            ResultSet rs = ps.executeQuery();
            LinkedHashMap<String, Integer> playerEntries = new LinkedHashMap<>();
            while (rs.next()) {
                playerEntries.put(rs.getString(1), rs.getInt(2));
            }
            return playerEntries;
        }));
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
    @Contract(pure = true)
    private static @NotNull String getLeaderboardQuery(@NotNull LeaderboardTimeframe sortBy) {
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