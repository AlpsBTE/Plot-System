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
import com.alpsbte.plotsystem.core.holograms.HolographicDisplay;
// import com.alpsbte.plotsystem.utils.io.language.LanguageFile;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.holograms.PlotsLeaderboard;
import com.alpsbte.plotsystem.core.holograms.ScoreLeaderboard;
import com.alpsbte.plotsystem.utils.enums.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
// import java.util.logging.Level;

public class Builder {

    private final UUID UUID;

    public Builder(UUID UUID) {
        this.UUID = UUID;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(UUID);
    }

    public java.util.UUID getUUID() {
        return UUID;
    }

    public boolean isOnline() { return Bukkit.getPlayer(UUID) != null; }

    public String getName() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            if (rs.next()) {
                return rs.getString(1);
            }
            return getPlayer() != null ? getPlayer().getName() : "";
        }
    }

    public int getScore() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT score FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public int getCompletedBuilds() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT completed_plots FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public Slot getFreeSlot() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT first_slot, second_slot, third_slot FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            if (rs.next()) {
                for(int i = 1; i <= 3; i++) {
                    if(rs.getString(i) == null) {
                        return Slot.values()[i - 1];
                    }
                }
            }
            return null;
        }
    }

    public Plot getPlot(Slot slot) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT " + slot.name().toLowerCase() + " FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getUUID().toString()).executeQuery()) {

            int plotID = -1;
            if (rs.next()) plotID = rs.getInt(1);
            return rs.wasNull() ? null : new Plot(plotID);
        }
    }

    public void addScore(int score) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_builders SET score = ? WHERE uuid = ?")
                .setValue(getScore() + score).setValue(getUUID().toString())
                .executeUpdate();

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> PlotSystem.getHolograms().stream().filter(holo -> holo instanceof ScoreLeaderboard).findFirst().ifPresent(HolographicDisplay::updateHologram));
    }

    public void addCompletedBuild(int amount) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_builders SET completed_plots = ? WHERE uuid = ?")
                .setValue(getCompletedBuilds() + amount).setValue(getUUID().toString())
                .executeUpdate();

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> PlotSystem.getHolograms().stream().filter(holo -> holo instanceof PlotsLeaderboard).findFirst().ifPresent(HolographicDisplay::updateHologram));
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
                return new Builder(java.util.UUID.fromString(rs.getString(1)));
            }
            return null;
        }
    }

    public static List<String> getBuildersByScore(int limit) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name, score FROM plotsystem_builders ORDER BY score DESC LIMIT ?")
                .setValue(limit).executeQuery()) {

            List<String> scoreAsFormat = new ArrayList<>();
            while (rs.next()) {
                scoreAsFormat.add(rs.getString(1) + "," + rs.getInt(2));
            }
            return scoreAsFormat;
        }
    }

    public static List<String> getBuildersByCompletedBuilds(int limit) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name, completed_plots FROM plotsystem_builders ORDER BY completed_plots DESC LIMIT ?")
                .setValue(limit).executeQuery()) {

            List<String> scoreAsFormat = new ArrayList<>();
            while (rs.next()) {
                scoreAsFormat.add(rs.getString(1) + "," + rs.getInt(2));
            }
            return scoreAsFormat;
        }
    }

    public Slot getSlot (Plot plot) throws SQLException {
        for (Slot slot : Slot.values()) {
            Plot slotPlot = getPlot(slot);
            if (slotPlot != null && slotPlot.getID() == plot.getID()) {
                return slot;
            }
        }
        return null;
    }

    /*public LanguageFile getLanguage() {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT lang FROM plotsystem_builders WHERE uuid = ?")
                .setValue(getPlayer().getUniqueId().toString()).executeQuery()) {
            if (rs.next()) {
                String tag = rs.getString(1);
                return LanguageFile.languageList.stream().filter(l -> l.getTag().equals(tag)).findFirst().orElse(null);
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE,"An error occurred while getting language setting from database",ex);
        }
        return null;
    }*/
}
