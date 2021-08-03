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
import github.BTEPlotSystem.core.database.DatabaseConnection;
import github.BTEPlotSystem.core.holograms.PlotsLeaderboard;
import github.BTEPlotSystem.core.holograms.HolographicDisplay;
import github.BTEPlotSystem.core.holograms.ScoreLeaderboard;
import github.BTEPlotSystem.core.database.builder.StatementBuilder;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.utils.enums.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        String sql = "SELECT name FROM plotsystem_builders WHERE uuid = ?";
        return DatabaseConnection.query(new StatementBuilder(sql)
                .setString(getUUID().toString())
                .build()).getString(1);
    }

    public int getScore() throws SQLException {
        String sql = "SELECT score FROM plotsystem_builders WHERE uuid = ?";
        return DatabaseConnection.query(new StatementBuilder(sql)
                .setString(getUUID().toString())
                .build()).getInt(1);
    }

    public int getCompletedBuilds() throws SQLException {
        String sql = "SELECT completed_plots FROM plotsystem_builders WHERE uuid = ?";
        return DatabaseConnection.query(new StatementBuilder(sql)
                .setString(getUUID().toString())
                .build()).getInt(1);
    }

    public Slot getFreeSlot() throws SQLException {
        String sql = "SELECT firsts_slot, second_slot, third_slot FROM plotsystem_builders WHERE uuid = ?";
        ResultSet rs = DatabaseConnection.query(new StatementBuilder(sql)
                .setString(getUUID().toString()).build());

        for(int i = 1; i <= 3; i++) {
            if(rs.getString(i) == null) {
                return Slot.values()[i - 1];
            }
        }
        return null;
    }

    public Plot getPlot(Slot slot) throws SQLException {
        String sql = "SELECT " + slot.name() + " FROM plotsystem_builders WHERE uuid = ?";
        ResultSet rs = DatabaseConnection.query(new StatementBuilder(sql)
                .setString(getUUID().toString()).build());

        int plotID = rs.getInt(1);
        return rs.wasNull() ? null : new Plot(plotID);
    }

    public void addScore(int score) throws SQLException {
        String sql = "UPDATE plotsystem_builders SET score = ? WHERE uuid = ?";
        DatabaseConnection.update(new StatementBuilder(sql)
                .setInt(score).setString(UUID.toString()).build());

        BTEPlotSystem.getHolograms().stream().filter(holo -> holo instanceof ScoreLeaderboard).findFirst().ifPresent(HolographicDisplay::updateHologram);
    }

    public void addCompletedBuild(int amount) throws SQLException {
        String sql = "UPDATE plotsystem_builders SET completed_plots = ? WHERE uuid = ?";
        DatabaseConnection.update(new StatementBuilder(sql)
                .setInt(amount).setString(UUID.toString()).build());

        BTEPlotSystem.getHolograms().stream().filter(holo -> holo instanceof PlotsLeaderboard).findFirst().ifPresent(HolographicDisplay::updateHologram);
    }

    public void setPlot(int plotID, Slot slot) throws SQLException {
        String sql = "UPDATE plotsystem_builders SET " + slot.name() + " = ? WHERE uuid = ?";
        DatabaseConnection.update(new StatementBuilder(sql)
                .setInt(plotID).setString(UUID.toString()).build());
    }

    public void removePlot(Slot slot) throws SQLException {
        String sql = "UPDATE plotsystem_builders SET " + slot.name() + " = DEFAULT(first_slot) WHERE uuid = ?";
        DatabaseConnection.update(new StatementBuilder(sql)
                .setString(UUID.toString()).build());
    }

    public static Builder getBuilderByName(String name) throws SQLException {
        String sql = "SELECT uuid FROM plotsystem_builders WHERE name = ?";
        ResultSet rs = DatabaseConnection.query(new StatementBuilder(sql)
                .setString(name).build());

        if (!rs.wasNull()) {
            return new Builder(java.util.UUID.fromString(rs.getString(1)));
        }
        return null;
    }

    public static List<String> getBuildersByScore(int limit) throws SQLException {
        String sql = "SELECT name, score FROM plotsystem_builders ORDER BY score DESC LIMIT ?";
        ResultSet rs = DatabaseConnection.query(new StatementBuilder(sql)
                .setInt(limit).build());

        List<String> scoreAsFormat = new ArrayList<>();
        if (!rs.wasNull()) {
            while (rs.next()) {
                scoreAsFormat.add(rs.getString(1) + "," + rs.getInt(2));
            }
        }
        return scoreAsFormat;
    }

    public static List<String> getBuildersByCompletedBuilds(int limit) throws SQLException {
        String sql = "SELECT name, completed_plots FROM plotsystem_builders ORDER BY completed_plots DESC LIMIT ?";
        ResultSet rs = DatabaseConnection.query(new StatementBuilder(sql)
                .setInt(limit).build());

        List<String> scoreAsFormat = new ArrayList<>();
        if (!rs.wasNull()) {
            while (rs.next()) {
                scoreAsFormat.add(rs.getString(1) + "," + rs.getInt(2));
            }
        }
        return scoreAsFormat;
    }

    public Slot getSlot (Plot plot) throws SQLException {
        for (Slot slot : Slot.values()) {
            if (getPlot(slot).getID() == plot.getID()) {
                return slot;
            }
        }
        return null;
    }
}
