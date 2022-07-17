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

package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.HologramManager;
import com.alpsbte.plotsystem.core.holograms.HolographicDisplay;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.holograms.PlotsLeaderboard;
import com.alpsbte.plotsystem.core.holograms.ScoreLeaderboard;
import com.alpsbte.plotsystem.core.system.plot.PlotType;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.utils.items.builder.LoreBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class Builder {

    public static HashMap<UUID, Builder> builders = new HashMap<>();

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

    public ItemStack getPlotMenuItem(Slot slot, Player langPlayer) throws SQLException {
        Plot plot = getPlot(slot);
        int i = Arrays.asList(Slot.values()).indexOf(slot);

        if (plot == null) {
            return new ItemBuilder(Material.EMPTY_MAP, 1 + i)
                    .setName("§b§l" + LangUtil.get(getPlayer(), LangPaths.MenuTitle.SLOT).toUpperCase() + " " + (i + 1))
                    .setLore(new LoreBuilder()
                            .addLines("§7" + LangUtil.get(langPlayer, LangPaths.MenuDescription.SLOT),
                                    "",
                                    "§6§l" + LangUtil.get(langPlayer, LangPaths.Plot.STATUS) + ": §7§lUnassigned") // Cant translate because name is stored in the database
                            .build())
                    .build();
        }

        return new ItemBuilder(Material.MAP, 1 + i)
                .setName("§b§l" + LangUtil.get(langPlayer, LangPaths.MenuTitle.SLOT).toUpperCase() + " " + (i + 1))
                .setLore(new LoreBuilder()
                        .addLines("§7" + LangUtil.get(langPlayer, LangPaths.Plot.ID) + ": §f" + plot.getID(),
                                "§7" + LangUtil.get(langPlayer, LangPaths.Plot.CITY) + ": §f" + plot.getCity().getName(),
                                "§7" + LangUtil.get(langPlayer, LangPaths.Plot.DIFFICULTY) + ": §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase(),
                                "",
                                "§6§l" + LangUtil.get(langPlayer, LangPaths.Plot.STATUS) + ": §7§l" + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1)
                        ).build())
                .build();
    }

    public void addScore(int score) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_builders SET score = ? WHERE uuid = ?")
                .setValue(getScore() + score).setValue(getUUID().toString())
                .executeUpdate();

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> HologramManager.getHolograms().stream().filter(holo -> holo instanceof ScoreLeaderboard).findFirst().ifPresent(HolographicDisplay::updateHologram));
    }

    public void addCompletedBuild(int amount) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_builders SET completed_plots = ? WHERE uuid = ?")
                .setValue(getCompletedBuilds() + amount).setValue(getUUID().toString())
                .executeUpdate();

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> HologramManager.getHolograms().stream().filter(holo -> holo instanceof PlotsLeaderboard).findFirst().ifPresent(HolographicDisplay::updateHologram));
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

    private static String getBuildersByScoreQuery(com.alpsbte.plotsystem.core.leaderboards.ScoreLeaderboard.LeaderboardTimeframe sortBy, int limit) {
        String minimumDate = null;
        switch (sortBy) {
            case DAILY:
                minimumDate = "(NOW() - INTERVAL 1 DAY)";
                break;
            case MONTHLY:
                minimumDate = "(NOW() - INTERVAL 1 MONTH)";
                break;
            case YEARLY:
                minimumDate = "(NOW() - INTERVAL 1 YEAR)";
                break;
            case LIFETIME:
                // no limits
                minimumDate = null;
                break;
        }

        // get plot id, owner username, owner uuid, score & date
        // sort by score & limit (if set above) by timeframe
        String query = "SELECT plots.id, builders.name, plots.owner_uuid, plots.score, reviews.review_date\n" +
                "FROM plotsystem_plots AS plots\n" +
                "INNER JOIN plotsystem_reviews AS reviews ON plots.review_id = reviews.id\n" +
                "INNER JOIN plotsystem_builders AS builders ON builders.uuid = plots.owner_uuid\n" +
                (minimumDate != null
                        ? "WHERE reviews.review_date BETWEEN " + minimumDate + " AND NOW()\n"
                        : "") +
                "ORDER BY score DESC\n" +
                (limit > 0 ? "LIMIT " + limit : "");

        return query;
    }

    public static int getBuilderScore(UUID uuid, com.alpsbte.plotsystem.core.leaderboards.ScoreLeaderboard.LeaderboardTimeframe sortBy) throws SQLException {
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

    public static int getBuilderScorePosition(UUID uuid, com.alpsbte.plotsystem.core.leaderboards.ScoreLeaderboard.LeaderboardTimeframe sortBy) throws SQLException {
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

    public static int getBuildersInSort(com.alpsbte.plotsystem.core.leaderboards.ScoreLeaderboard.LeaderboardTimeframe sortBy) throws SQLException {
        String query = "SELECT COUNT(*) FROM (" + getBuildersByScoreQuery(sortBy, 0) + ") results";

        try(ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
            rs.next();
            int position = rs.getInt(1);

            DatabaseConnection.closeResultSet(rs);
            return position;
        }
    }

    public static class DatabaseEntry<K, V> {
        private K key;
        private V value;

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

    public static List<DatabaseEntry<String, Integer>> getBuildersByScore(com.alpsbte.plotsystem.core.leaderboards.ScoreLeaderboard.LeaderboardTimeframe sortBy) throws SQLException {
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

    public static List<String> getBuildersByScore(int limit) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name, score FROM plotsystem_builders ORDER BY score DESC LIMIT ?")
                .setValue(limit).executeQuery()) {

            List<String> scoreAsFormat = new ArrayList<>();
            while (rs.next()) {
                scoreAsFormat.add(rs.getString(1) + "," + rs.getInt(2));
            }

            DatabaseConnection.closeResultSet(rs);
            return scoreAsFormat;
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

    public String getLanguageTag() {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT lang FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {
            if (rs.next()) {
                String tag = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return tag;
            }
            DatabaseConnection.closeResultSet(rs);
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while getting language setting from database", ex);
        }
        return null;
    }

    public void setLanguageTag(String langTag) throws SQLException {
        if (langTag == null) {
            DatabaseConnection.createStatement("UPDATE plotsystem_builders SET lang = DEFAULT(lang) WHERE uuid = ?")
                    .setValue(getUUID().toString()).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_builders SET lang = ? WHERE uuid = ?")
                    .setValue(langTag).setValue(getUUID().toString())
                    .executeUpdate();
        }
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


}
