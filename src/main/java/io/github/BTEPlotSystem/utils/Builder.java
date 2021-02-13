package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.utils.enums.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
            return new Plot(rs.getInt(slot.name()));
        }

        return null;
    }

    public void addScore(int score) throws SQLException {
        DatabaseConnection.prepareStatement(
                "UPDATE players SET score = " + (getScore() + score) + " WHERE uuid = '" + getUUID() + "'"
        ).executeUpdate();
    }

    public void addCompletedBuild() throws SQLException {
        DatabaseConnection.prepareStatement(
                "UPDATE players SET completedBuilds = '" + (getCompletedBuilds() + 1) + "' WHERE uuid = '" + getUUID() + "'"
        ).executeUpdate();
    }

    public void setPlot(int plotID, Slot slot) throws SQLException {
        DatabaseConnection.prepareStatement("UPDATE players SET " + slot.name() + " = '" + plotID + "' WHERE uuid = '" + getUUID() + "'").executeUpdate();
    }

    public void removePlot(Slot slot) throws SQLException {
       PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE players SET " + slot.name() + " = DEFAULT(firstSlot) WHERE uuid = '" + getUUID() + "'");
       statement.executeUpdate();
    }

    public static List<String> getBuildersByScore(int limit) throws SQLException {
        List<String> scoreAsFormat = new ArrayList<>();
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT name, score FROM players ORDER BY score DESC LIMIT " + limit);

        while (rs.next()) {
            scoreAsFormat.add(rs.getString("name") + ", " + rs.getInt("score"));
        }

        return scoreAsFormat;
    }

    public static List<String> getBuildersByCompletedBuilds(int limit) throws SQLException {
        List<String> completedBuildsAsFormat = new ArrayList<>();
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT name, completedBuilds FROM players ORDER BY completedBuilds DESC LIMIT " + limit);

        while (rs.next()) {
            completedBuildsAsFormat.add(rs.getString("name") + ", " + rs.getInt("completedBuilds"));
        }

        return completedBuildsAsFormat;
    }
}
