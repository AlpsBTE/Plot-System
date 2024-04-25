/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LegacyLoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.holograms.HologramRegister;
import com.alpsbte.plotsystem.core.holograms.PlotsLeaderboard;
import com.alpsbte.plotsystem.core.holograms.ScoreLeaderboard;
//import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;
import com.aseanbte.aseanlib.hologram.DecentHologramDisplay;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Builder {

    public static final HashMap<UUID, Builder> builders = new HashMap<>();

    private final UUID uuid;
    public PlotType plotType;

    private Builder(UUID UUID) {
        this.uuid = UUID;
    }

    public static Builder byUUID(UUID uuid){
        if(builders.containsKey(uuid))
            return builders.get(uuid);

        Builder builder = new Builder(uuid);
        builders.put(uuid, builder);

        return builders.get(uuid);
    }


    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public java.util.UUID getUUID() {
        return uuid;
    }

    public boolean isOnline() { return Bukkit.getPlayer(uuid) != null; }

    public String getName() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            if (rs.next()) {
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return s;
            }

            DatabaseConnection.closeResultSet(rs);

            return getPlayer() != null ? getPlayer().getName() : "";
        }
    }

    public int getScore() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT score FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            if (rs.next()) {
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return i;
            }

            DatabaseConnection.closeResultSet(rs);
            return 0;
        }
    }

    public int getCompletedBuilds() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT completed_plots FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            if (rs.next()) {
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return i;
            }

            DatabaseConnection.closeResultSet(rs);
            return 0;
        }
    }

    public Slot getFreeSlot() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT first_slot, second_slot, third_slot FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

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
        }
    }

    public Plot getPlot(Slot slot) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT " + slot.name().toLowerCase() + " FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            int plotID = -1;
            if (rs.next()) plotID = rs.getInt(1);

            boolean boo = rs.wasNull();
            DatabaseConnection.closeResultSet(rs);

            return boo ? null : new Plot(plotID);
        }
    }

    public ItemStack getPlotMenuItem(Plot plot, int slotIndex, Player langPlayer) throws SQLException {
        if (plot == null) {
            return new ItemBuilder(Material.MAP, 1 + slotIndex)
                    .setName("§b§l" + LangUtil.getInstance().get(getPlayer(), LangPaths.MenuTitle.SLOT).toUpperCase() + " " + (slotIndex + 1))
                    .setLore(new LegacyLoreBuilder()
                            .addLines("§7" + LangUtil.getInstance().get(langPlayer, LangPaths.MenuDescription.SLOT),
                                    "",
                                    "§6§l" + LangUtil.getInstance().get(langPlayer, LangPaths.Plot.STATUS) + ": §7§lUnassigned") // Can't translate because name is stored in the database
                            .build())
                    .build();
        }

        return new ItemBuilder(Material.FILLED_MAP, 1 + slotIndex)
                .setName("§b§l" + LangUtil.getInstance().get(langPlayer, LangPaths.MenuTitle.SLOT).toUpperCase() + " " + (slotIndex + 1))
                .setLore(new LegacyLoreBuilder()
                        .addLines("§7" + LangUtil.getInstance().get(langPlayer, LangPaths.Plot.ID) + ": §f" + plot.getID(),
                                "§7" + LangUtil.getInstance().get(langPlayer, LangPaths.Plot.CITY) + ": §f" + plot.getCity().getName(),
                                "§7" + LangUtil.getInstance().get(langPlayer, LangPaths.Plot.DIFFICULTY) + ": §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase(),
                                "",
                                "§6§l" + LangUtil.getInstance().get(langPlayer, LangPaths.Plot.STATUS) + ": §7§l" + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1)
                        ).build())
                .build();
    }

    public void addScore(int score) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_builders SET score = ? WHERE uuid = ?")
                .setValue(getScore() + score).setValue(getUUID().toString())
                .executeUpdate();

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> HologramRegister.getActiveDisplays().stream()
                .filter(leaderboard -> leaderboard instanceof ScoreLeaderboard).findFirst().ifPresent(DecentHologramDisplay::reloadAll));
    }

    public void addCompletedBuild(int amount) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_builders SET completed_plots = ? WHERE uuid = ?")
                .setValue(getCompletedBuilds() + amount).setValue(getUUID().toString())
                .executeUpdate();

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> HologramRegister.getActiveDisplays().stream()
                .filter(leaderboard -> leaderboard instanceof PlotsLeaderboard).findFirst().ifPresent(DecentHologramDisplay::reloadAll));
    }

    public void setPlot(int plotID, Slot slot) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_builders SET " + slot.name().toLowerCase() + " = ? WHERE uuid = ?")
                .setValue(plotID).setValue(getUUID().toString())
                .executeUpdate();
    }

    public void removePlot(Slot slot) throws SQLException {
        if (slot != null) { // If not null, plot is already removed from player slot
            DatabaseConnection.createStatement("UPDATE plotsystem_builders SET " + slot.name().toLowerCase() + " = DEFAULT(first_slot) WHERE uuid = ?")
                    .setValue(getUUID().toString())
                    .executeUpdate();
        }
    }

    public static Builder getBuilderByName(String name) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT uuid FROM plotsystem_builders WHERE name = ?")
                .setValue(name).executeQuery()) {

            if (rs.next()) {
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return Builder.byUUID(UUID.fromString(s));
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    private static String getBuildersByScoreQuery(ScoreLeaderboard.LeaderboardTimeframe sortBy, int limit) {
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

    public static int getBuilderScore(UUID uuid, ScoreLeaderboard.LeaderboardTimeframe sortBy) throws SQLException {
        String query = getBuildersByScoreQuery(sortBy, 0);

        try(ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            boolean found = false;
            int score = 0;
            while(rs.next() && !found) {
                if(rs.getString(3).equals(uuid.toString())) {
                    found = true;
                    score = rs.getInt(4);
                }
            }

            if(!found) score = -1;

            DatabaseConnection.closeResultSet(rs);
            return score;
        }
    }

    /**
     * Select both country and city entries as ID and name, intersect with city ID ordered by country ID
     * <pre>
     * {@code
     *   SELECT countries.id AS country_id, city_projects.visible, countries.name AS country_name, city_projects.id AS project_id, city_projects.name AS project_name
     *   FROM plotsystem_countries AS countries
     *   LEFT JOIN plotsystem_city_projects AS city_projects ON countries.id = city_projects.country_id
     *   WHERE city_projects.visible = 1
     *   ORDER BY country_id
     * }</pre> SQL Query statement
     * @return A List of every registered city project data as a pair of country entry and city entry.
     * @throws SQLException If Database connection is lost
     */
    public static ArrayList<DatabaseEntry<DatabaseEntry<Integer, String>,
        DatabaseEntry<Integer, String>>> getProjectsSorted() throws SQLException
    {
        String query = "SELECT countries.id AS country_id, city_projects.visible, "
                + "countries.name AS country_name, "
                + "city_projects.id AS project_id, "
                + "city_projects.name AS project_name "
                + "FROM plotsystem_countries AS countries\n"
                + "LEFT JOIN plotsystem_city_projects AS city_projects ON countries.id = city_projects.country_id\n"
                + "WHERE city_projects.visible = 1\n"
                + "ORDER BY country_id";

        try(ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            ArrayList<DatabaseEntry<DatabaseEntry<Integer, String>,
                DatabaseEntry<Integer, String>>> result = new ArrayList<>();
            while(rs.next()) {
                result.add(new DatabaseEntry<>(
                    new DatabaseEntry<>(rs.getInt("country_id"), rs.getString("country_name")),
                    new DatabaseEntry<>(rs.getInt("project_id"), rs.getString("project_name"))
                ));
            }
            DatabaseConnection.closeResultSet(rs);
            return result;
        }
    }

    /**
     * Select city_id as "city_id", plot id as "id", "difficulty_id", "difficulty"
     * Ordered by each city project id, do this will be incrementing from 1
     * <pre>
     * {@code
     *   SELECT city_projects.id AS city_id, city_projects.visible, difficulties.name AS difficulty FROM plotsystem_city_projects AS city_projects
     *   RIGHT JOIN plotsystem_plots AS plots ON city_projects.id = plots.city_project_id
     *   INNER JOIN plotsystem_difficulties AS difficulties ON difficulty_id = difficulties.id
     *   WHERE city_projects.visible = 1
     *   ORDER BY city_projects.id
     * }</pre> SQL Query statement
     * @return A List of every City ID containing a List of all plot entries in the city project.
     * @throws SQLException If Database connection is lost
     */
    public static ArrayList<DatabaseEntry<Integer, ArrayList<DatabaseEntry<Integer,
        DatabaseEntry<String, String>>>>> getPlotsSorted() throws SQLException
    {
        String query = "SELECT city_projects.id AS city_id, city_projects.visible, "
                + "plots.status, plots.id, plots.difficulty_id AS difficulty_id, "
                + "difficulties.name AS difficulty "
                + "FROM plotsystem_city_projects AS city_projects\n"
                + "RIGHT JOIN plotsystem_plots AS plots "
                + "ON city_projects.id = plots.city_project_id\n"
                + "INNER JOIN plotsystem_difficulties AS difficulties "
                + "ON difficulty_id = difficulties.id\n"
                + "WHERE city_projects.visible = 1\n"
                + "ORDER BY city_projects.id";

        try(ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            ArrayList<DatabaseEntry<Integer,    // City ID parent
                ArrayList<DatabaseEntry<Integer,// List of all plots in the parent City ID
                DatabaseEntry<String, String>>  // the plot entry containing a pair of Status and Difficulty
            >>> result = new ArrayList<>();

            int currentCursor = 0;
            ArrayList<DatabaseEntry<Integer, DatabaseEntry<String, String>>> entries = new ArrayList<>();

            while(rs.next()) {
                int currentCity = rs.getInt("city_id");
                if(currentCity != currentCursor)
                {
                    if(!entries.isEmpty()) {
                        result.add(new DatabaseEntry<>(currentCursor, new ArrayList<>(entries)));
                        if(!rs.isLast()) entries.clear();
                    }
                    currentCursor++;
                }

                entries.add(new DatabaseEntry<>(rs.getInt("id"),
                    new DatabaseEntry<>(rs.getString("status"),
                    rs.getString("difficulty"))));
                if(rs.isLast()) result.add(new DatabaseEntry<>(currentCity, entries));
            }

            DatabaseConnection.closeResultSet(rs);
            return result;
        }
    }

    public static int getBuilderScorePosition(UUID uuid, ScoreLeaderboard.LeaderboardTimeframe sortBy) throws SQLException {
        String query = getBuildersByScoreQuery(sortBy, 0);

        try(ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            boolean found = false;
            int position = 0;
            while(rs.next() && !found) {
                position++;
                if(rs.getString(3).equals(uuid.toString())) {
                    found = true;
                }
            }

            if(!found) position = -1;

            DatabaseConnection.closeResultSet(rs);
            return position;
        }
    }

    public static int getBuildersInSort(ScoreLeaderboard.LeaderboardTimeframe sortBy) throws SQLException {
        String query = "SELECT COUNT(*) FROM (" + getBuildersByScoreQuery(sortBy, 0) + ") results";

        try(ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            rs.next();
            int position = rs.getInt(1);

            DatabaseConnection.closeResultSet(rs);
            return position;
        }
    }

    public static class DatabaseEntry<K, V> {
        private final K key;
        private final V value;

        DatabaseEntry(K k, V v) {
            this.key = k;
            this.value = v;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    public static List<DatabaseEntry<String, Integer>> getBuildersByScore(ScoreLeaderboard.LeaderboardTimeframe sortBy) throws SQLException {
        String query = getBuildersByScoreQuery(sortBy, 10);

        try(ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            ArrayList<DatabaseEntry<String, Integer>> lines = new ArrayList<>();

            while(rs.next()) {
                lines.add(new DatabaseEntry<>(rs.getString(2), rs.getInt(4)));
            }

            DatabaseConnection.closeResultSet(rs);
            return lines;
        }
    }

    public static List<DatabaseEntry<String, Integer>> getBuildersByCompletedBuilds(int limit) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name, completed_plots FROM plotsystem_builders ORDER BY completed_plots DESC LIMIT ?")
                .setValue(limit).executeQuery()) {

            ArrayList<DatabaseEntry<String, Integer>> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new DatabaseEntry<>(rs.getString(1), rs.getInt(2)));
            }

            DatabaseConnection.closeResultSet(rs);
            return results;
        }
    }

    public Slot getSlot(Plot plot) throws SQLException {
        for (Slot slot : Slot.values()) {
            Plot slotPlot = getPlot(slot);
            if (slotPlot != null && slotPlot.getID() == plot.getID()) {
                return slot;
            }
        }
        return null;
    }

    public PlotType getPlotTypeSetting() {
        if(plotType != null)
            return plotType;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT setting_plot_type FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            if (rs.next()) {
                int id = rs.getInt(1);
                this.plotType = PlotType.byId(id);
                DatabaseConnection.closeResultSet(rs);

                return plotType;
            }
            DatabaseConnection.closeResultSet(rs);
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE,"An error occurred while getting language setting from database", ex);
        }
        return null;
    }

    public void setPlotTypeSetting(PlotType plotType){
        try {
            if (plotType == null) {
                DatabaseConnection.createStatement("UPDATE plotsystem_builders SET setting_plot_type = DEFAULT(setting_plot_type) WHERE uuid = ?")
                        .setValue(getUUID().toString()).executeUpdate();
            } else {
                DatabaseConnection.createStatement("UPDATE plotsystem_builders SET setting_plot_type = ? WHERE uuid = ?")
                        .setValue(plotType.getId()).setValue(getUUID().toString())
                        .executeUpdate();
            }
        }catch (SQLException ex){
            Bukkit.getLogger().log(Level.SEVERE,"An error occurred while getting language setting from database", ex);
        }

        this.plotType = plotType;
    }

    public Reviewer getAsReviewer() throws SQLException {
        return new Reviewer(getUUID());
    }

    public boolean isReviewer() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT COUNT(builder_uuid) FROM plotsystem_builder_is_reviewer WHERE builder_uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            DatabaseConnection.closeResultSet(rs);
            return count > 0;
        }
    }

    public static class Reviewer {
        private final List<BuildTeam> buildTeams;

        public Reviewer(UUID reviewerUUID) throws SQLException {
            this.buildTeams = BuildTeam.getBuildTeamsByReviewer(reviewerUUID);
        }

        public List<Country> getCountries() {
            Set<Integer> countries = new HashSet<>();
            buildTeams.forEach(b -> {
                try {
                    countries.addAll(b.getCountries().stream().map(Country::getID).collect(Collectors.toList()));
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            });

            return countries.stream().map(c -> {
                try {
                    return new Country(c);
                } catch (SQLException ex) { Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex); }
                return null;
            }).collect(Collectors.toList());
        }
    }
}
