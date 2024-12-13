package com.alpsbte.plotsystem.core.database;

import com.alpsbte.alpslib.hologram.DecentHologramDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.data.DataException;
import com.alpsbte.plotsystem.core.holograms.PlotsLeaderboard;
import com.alpsbte.plotsystem.core.holograms.ScoreLeaderboard;
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.data.BuilderProvider;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.enums.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public class BuilderProviderSql implements BuilderProvider {
    @Override
    public String getName(UUID uuid) throws DataException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name FROM plotsystem_builders WHERE uuid = ?")
                .setValue(uuid.toString()).executeQuery()) {

            if (rs.next()) {
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return s;
            }

            DatabaseConnection.closeResultSet(rs);

            Player p = Bukkit.getPlayer(uuid);
            return p != null ? p.getName() : "";
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public int getScore(UUID uuid) {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT score FROM plotsystem_builders WHERE uuid = ?")
                .setValue(uuid.toString()).executeQuery()) {

            if (rs.next()) {
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return i;
            }

            DatabaseConnection.closeResultSet(rs);
            return 0;
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public int getCompletedBuildsCount(UUID uuid) {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT completed_plots FROM plotsystem_builders WHERE uuid = ?")
                .setValue(uuid.toString()).executeQuery()) {

            if (rs.next()) {
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return i;
            }

            DatabaseConnection.closeResultSet(rs);
            return 0;
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public Slot getFreeSlot(UUID uuid) {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT first_slot, second_slot, third_slot FROM plotsystem_builders WHERE uuid = ?")
                .setValue(uuid.toString()).executeQuery()) {

            if (rs.next()) {
                for (int i = 1; i <= 3; i++) {
                    if (rs.getString(i) == null) {
                        DatabaseConnection.closeResultSet(rs);
                        return Slot.values()[i - 1];
                    }
                }
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public Plot getPlot(UUID uuid, Slot slot) throws DataException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT " + slot.name().toLowerCase() + " FROM plotsystem_builders WHERE uuid = ?")
                .setValue(uuid.toString()).executeQuery()) {

            int plotID = -1;
            if (rs.next()) plotID = rs.getInt(1);

            boolean boo = rs.wasNull();
            DatabaseConnection.closeResultSet(rs);

            return boo ? null : new Plot(plotID);
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public void addScore(UUID uuid, int score) throws DataException {
        try {
            DatabaseConnection.createStatement("UPDATE plotsystem_builders SET score = ? WHERE uuid = ?")
                    .setValue(getScore(uuid) + score).setValue(uuid.toString())
                    .executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> DecentHologramDisplay.activeDisplays.stream()
                .filter(leaderboard -> leaderboard instanceof ScoreLeaderboard).findFirst().ifPresent(DecentHologramDisplay::reloadAll));
    }

    @Override
    public void addCompletedBuild(UUID uuid, int amount) throws DataException {
        try {
            DatabaseConnection.createStatement("UPDATE plotsystem_builders SET completed_plots = ? WHERE uuid = ?")
                    .setValue(getCompletedBuildsCount(uuid) + amount).setValue(uuid.toString())
                    .executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> DecentHologramDisplay.activeDisplays.stream()
                .filter(leaderboard -> leaderboard instanceof PlotsLeaderboard).findFirst().ifPresent(DecentHologramDisplay::reloadAll));
    }

    @Override
    public void setPlot(UUID uuid, int plotID, Slot slot) throws DataException {
        try {
            DatabaseConnection.createStatement("UPDATE plotsystem_builders SET " + slot.name().toLowerCase() + " = ? WHERE uuid = ?")
                    .setValue(plotID).setValue(uuid.toString())
                    .executeUpdate();
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public void removePlot(UUID uuid, Slot slot) throws DataException {
        if (slot != null) { // If not null, plot is already removed from player slot
            try {
                DatabaseConnection.createStatement("UPDATE plotsystem_builders SET " + slot.name().toLowerCase() + " = DEFAULT(first_slot) WHERE uuid = ?")
                        .setValue(uuid.toString())
                        .executeUpdate();
            } catch (SQLException e) {
                throw new DataException(e.getMessage());
            }
        }
    }

    @Override
    public Builder getBuilderByName(String name) throws DataException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT uuid FROM plotsystem_builders WHERE name = ?")
                .setValue(name).executeQuery()) {

            if (rs.next()) {
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return Builder.byUUID(UUID.fromString(s));
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public PlotType getPlotTypeSetting(UUID uuid) {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT setting_plot_type FROM plotsystem_builders WHERE uuid = ?")
                .setValue(uuid.toString()).executeQuery()) {

            if (rs.next()) {
                int id = rs.getInt(1);
                PlotType plotType = PlotType.byId(id);
                DatabaseConnection.closeResultSet(rs);

                return plotType;
            }
            DatabaseConnection.closeResultSet(rs);
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while getting language setting from database"), ex);
        }
        return null;
    }

    @Override
    public void setPlotTypeSetting(UUID uuid, PlotType plotType) {
        try {
            if (plotType == null) {
                DatabaseConnection.createStatement("UPDATE plotsystem_builders SET setting_plot_type = DEFAULT(setting_plot_type) WHERE uuid = ?")
                        .setValue(uuid.toString()).executeUpdate();
            } else {
                DatabaseConnection.createStatement("UPDATE plotsystem_builders SET setting_plot_type = ? WHERE uuid = ?")
                        .setValue(plotType.getId()).setValue(uuid.toString())
                        .executeUpdate();
            }
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while getting language setting from database"), ex);
        }
    }

    @Override
    public boolean isReviewer(UUID uuid) throws DataException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT COUNT(builder_uuid) FROM plotsystem_builder_is_reviewer WHERE builder_uuid = ?")
                .setValue(uuid.toString()).executeQuery()) {

            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            DatabaseConnection.closeResultSet(rs);
            return count > 0;
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public List<Country> getReviewerCountries(List<BuildTeam> buildTeams) {
        Set<Integer> countries = new HashSet<>();
        buildTeams.forEach(b -> {
            try {
                countries.addAll(b.getCountries().stream().map(Country::getID).collect(Collectors.toList()));
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        });

        return countries.stream().map(c -> {
            try {
                return new Country(c);
            } catch (SQLException ex) {PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);}
            return null;
        }).collect(Collectors.toList());
    }

    @Override
    public int getLeaderboardScore(UUID uuid, ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException {
        String query = getLeaderboardScoreQuery(sortBy, 0);

        try (ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            boolean found = false;
            int score = 0;
            while (rs.next() && !found) {
                if (rs.getString(3).equals(uuid.toString())) {
                    found = true;
                    score = rs.getInt(4);
                }
            }

            if (!found) score = -1;

            DatabaseConnection.closeResultSet(rs);
            return score;
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public int getLeaderboardPosition(UUID uuid, ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException {
        String query = getLeaderboardScoreQuery(sortBy, 0);

        try (ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            boolean found = false;
            int position = 0;
            while (rs.next() && !found) {
                position++;
                if (rs.getString(3).equals(uuid.toString())) {
                    found = true;
                }
            }

            if (!found) position = -1;

            DatabaseConnection.closeResultSet(rs);
            return position;
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public int getLeaderboardEntryCount(ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException {
        String query = "SELECT COUNT(*) FROM (" + getLeaderboardScoreQuery(sortBy, 0) + ") results";

        try (ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            rs.next();
            int position = rs.getInt(1);

            DatabaseConnection.closeResultSet(rs);
            return position;
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public HashMap<String, Integer> getLeaderboardEntriesByScore(ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException {
        String query = getLeaderboardScoreQuery(sortBy, 10);

        try (ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            HashMap<String, Integer> lines = new HashMap<>();

            while (rs.next()) {
                lines.put(rs.getString(2), rs.getInt(4));
            }

            DatabaseConnection.closeResultSet(rs);
            return lines;
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    @Override
    public HashMap<String, Integer> getLeaderboardEntriesByCompletedBuilds(int limit) throws DataException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name, completed_plots FROM plotsystem_builders ORDER BY completed_plots DESC LIMIT ?")
                .setValue(limit).executeQuery()) {

            HashMap<String, Integer> results = new HashMap<>();
            while (rs.next()) {
                results.put(rs.getString(1), rs.getInt(2));
            }

            DatabaseConnection.closeResultSet(rs);
            return results;
        } catch (SQLException e) {
            throw new DataException(e.getMessage());
        }
    }

    private String getLeaderboardScoreQuery(ScoreLeaderboard.LeaderboardTimeframe sortBy, int limit) {
        String minimumDate = null;
        switch (sortBy) {
            case DAILY:
                minimumDate = "(NOW() - INTERVAL 1 DAY)";
                break;
            case WEEKLY:
                minimumDate = "(NOW() - INTERVAL 1 WEEK)";
                break;
            case MONTHLY:
                minimumDate = "(NOW() - INTERVAL 1 MONTH)";
                break;
            case YEARLY:
                minimumDate = "(NOW() - INTERVAL 1 YEAR)";
                break;
            default:
                // no limits
                break;
        }

        // get plot id, owner username, owner uuid, score & date
        // sort by score & limit (if set above) by timeframe
        return "SELECT plots.id, builders.name, plots.owner_uuid, SUM(plots.score) AS score, reviews.review_date FROM plotsystem_plots AS plots\n" +
                "INNER JOIN plotsystem_reviews AS reviews ON plots.review_id = reviews.id\n" +
                "INNER JOIN plotsystem_builders AS builders ON builders.uuid = plots.owner_uuid\n" +
                (minimumDate != null
                        ? "WHERE reviews.review_date BETWEEN " + minimumDate + " AND NOW()\n"
                        : "") +
                "GROUP BY plots.owner_uuid \n" +
                "ORDER BY score DESC, builders.name\n" +
                (limit > 0 ? "LIMIT " + limit : "");
    }
}
