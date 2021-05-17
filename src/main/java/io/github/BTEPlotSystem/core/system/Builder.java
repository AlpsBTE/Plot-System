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

package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.utils.enums.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Builder {

    private final UUID UUID;

    public Builder(UUID UUID) throws SQLException {
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
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT name FROM players WHERE uuid = ?");
            ps.setString(1, UUID.toString());
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                return rs.getString(1);
            }
            return null;
        }
    }

    public int getScore() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT score FROM players WHERE uuid = ?");
            ps.setString(1, getUUID().toString());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public int getCompletedBuilds() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT completedBuilds FROM players WHERE uuid = ?");
            ps.setString(1, getUUID().toString());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public Slot getFreeSlot() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT firstSlot, secondSlot, thirdSlot FROM players WHERE uuid = ?");
            ps.setString(1, getUUID().toString());
            ResultSet rs = ps.executeQuery();
            rs.next();

            for(int i = 1; i <= 3; i++) {
                if(rs.getString(i) == null) {
                    return Slot.values()[i - 1];
                }
            }
            return null;
        }
    }

    public Plot getPlot(Slot slot) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT " + slot.name() + " FROM players WHERE uuid = ?");
            ps.setString(1, getUUID().toString());
            ResultSet rs = ps.executeQuery();
            rs.next();

            int plotID = rs.getInt(1);
            return rs.wasNull() ? null : new Plot(plotID);
        }
    }

    public void addScore(int score) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE players SET score = ? WHERE uuid = ?");
            ps.setInt(1, getScore() + score);
            ps.setString(2, getUUID().toString());
            ps.executeUpdate();
        }
        BTEPlotSystem.getHolograms().stream().filter(holo -> holo.getHologramName().equals("ScoreLeaderboard")).findFirst().get().updateLeaderboard();
    }

    public void addCompletedBuild(int amount) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE players SET completedBuilds = ? WHERE uuid = ?");
            ps.setInt(1, getCompletedBuilds() + amount);
            ps.setString(2, getUUID().toString());
            ps.executeUpdate();
        }
        BTEPlotSystem.getHolograms().stream().filter(holo -> holo.getHologramName().equals("BuildsLeaderboard")).findFirst().get().updateLeaderboard();
    }

    public void setPlot(int plotID, Slot slot) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE players SET " + slot.name() + " = ? WHERE uuid = ?");
            ps.setInt(1, plotID);
            ps.setString(2, getUUID().toString());
            ps.executeUpdate();
        }
    }

    public void removePlot(Slot slot) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE players SET " + slot.name() + " = DEFAULT(firstSlot) WHERE uuid = ?");
            ps.setString(1, getUUID().toString());
            ps.executeUpdate();
        }
    }

    public static Builder getBuilderByName(String name) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT uuid FROM players WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                return new Builder(java.util.UUID.fromString(rs.getString(1)));
            }
            return null;
        }
    }

    public static List<String> getBuildersByScore(int limit) throws SQLException {
        List<String> scoreAsFormat = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT name, score FROM players ORDER BY score DESC LIMIT ?");
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                scoreAsFormat.add(rs.getString(1) + "," + rs.getInt(2));
            }
            return scoreAsFormat;
        }
    }

    public static List<String> getBuildersByCompletedBuilds(int limit) throws SQLException {
        List<String> completedBuildsAsFormat = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT name, completedBuilds FROM players ORDER BY completedBuilds DESC LIMIT ?");
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                completedBuildsAsFormat.add(rs.getString(1) + "," + rs.getInt(2));
            }
            return completedBuildsAsFormat;
        }
    }
}
