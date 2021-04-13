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
import github.BTEPlotSystem.utils.enums.PlotDifficulty;
import github.BTEPlotSystem.utils.enums.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Builder {

    private final UUID UUID;
    private String name;

    public Builder(UUID UUID) throws SQLException {
        this.UUID = UUID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT name FROM players WHERE uuid = '" + UUID + "'");

        if(rs.next()) {
            this.name = rs.getString("name");
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(UUID);
    }

    public java.util.UUID getUUID() {
        return UUID;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() { return Bukkit.getPlayer(UUID) != null; }

    public int getScore() throws SQLException {
        ResultSet rs =  DatabaseConnection.createStatement().executeQuery("SELECT score FROM players WHERE uuid = '" + getUUID() + "'");

        if(rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public int getCompletedBuilds() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT completedBuilds FROM players WHERE uuid = '" + getUUID() + "'");

        if(rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public Slot getFreeSlot() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT firstSlot, secondSlot, thirdSlot FROM players WHERE uuid = '" + getUUID() + "'");

        if(rs.next()) {
            if(rs.getString(Slot.firstSlot.name()) == null) {
                return Slot.firstSlot;
            } else if(rs.getString(Slot.secondSlot.name()) == null) {
                return Slot.secondSlot;
            } else if(rs.getString(Slot.thirdSlot.name()) == null) {
                return Slot.thirdSlot;
            }
        }
        return null;
    }

    public Plot getPlot(Slot slot) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT " + slot.name() +" FROM players WHERE uuid = '" + getUUID() + "'");

        if (rs.next()) {
            int plotID = rs.getInt(1);

            if(!rs.wasNull()) {
                return new Plot(plotID);
            } else {
                return null;
            }
        }
        return null;
    }

    public PlotDifficulty getSelectedDifficulty() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT selectedDifficulty FROM players WHERE uuid = '" + getUUID() + "'");
        rs.next();

        return PlotDifficulty.values()[(rs.getInt(1) - 1)];
    }

    public void setSelectedDifficulty(PlotDifficulty plotDifficulty) throws SQLException {
        PreparedStatement ps = DatabaseConnection.prepareStatement("UPDATE players SET selectedDifficulty = ? WHERE uuid = '" + getUUID() + "'");
        ps.setInt(1, plotDifficulty.ordinal() + 1);
        ps.executeUpdate();
    }

    public void addScore(int score) throws SQLException {
        DatabaseConnection.prepareStatement(
                "UPDATE players SET score = " + (getScore() + score) + " WHERE uuid = '" + getUUID() + "'"
        ).executeUpdate();
        BTEPlotSystem.getHolograms().stream().filter(holo -> holo.getHologramName().equals("ScoreLeaderboard")).findFirst().get().updateLeaderboard();
    }

    public void addCompletedBuild(int amount) throws SQLException {
        DatabaseConnection.prepareStatement(
                "UPDATE players SET completedBuilds = '" + (getCompletedBuilds() + amount) + "' WHERE uuid = '" + getUUID() + "'"
        ).executeUpdate();
    }

    public void setPlot(int plotID, Slot slot) throws SQLException {
        DatabaseConnection.prepareStatement("UPDATE players SET " + slot.name() + " = '" + plotID + "' WHERE uuid = '" + getUUID() + "'").executeUpdate();
    }

    public void removePlot(Slot slot) throws SQLException {
       PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE players SET " + slot.name() + " = DEFAULT(firstSlot) WHERE uuid = '" + getUUID() + "'");
       statement.executeUpdate();
    }

    public static Builder getBuilderByName(String name) {
        try {
            ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT uuid FROM players WHERE name = '" + name + "'");

            if(rs.next()) {
                return new Builder(java.util.UUID.fromString(rs.getString("uuid")));
            }
            return null;
        } catch (Exception ignore) { }
        return null;
    }

    public static List<String> getBuildersByScore(int limit) throws SQLException {
        List<String> scoreAsFormat = new ArrayList<>();
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT name, score FROM players ORDER BY score DESC LIMIT " + limit);

        while (rs.next()) {
            scoreAsFormat.add(rs.getString("name") + "," + rs.getInt("score"));
        }

        return scoreAsFormat;
    }

    public static List<String> getBuildersByCompletedBuilds(int limit) throws SQLException {
        List<String> completedBuildsAsFormat = new ArrayList<>();
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT name, completedBuilds FROM players ORDER BY completedBuilds DESC LIMIT " + limit);

        while (rs.next()) {
            completedBuildsAsFormat.add(rs.getString("name") + "," + rs.getInt("completedBuilds"));
        }

        return completedBuildsAsFormat;
    }
}
