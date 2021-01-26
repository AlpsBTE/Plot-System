package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.utils.enums.Slot;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Builder {

    private final Player player;

    public Builder(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public int getScore() throws SQLException {
        return DatabaseConnection.createStatement().executeQuery("SELECT score FROM players WHERE uuid = " + player.getUniqueId()).getInt("score");
    }

    public int getCompletedBuilds() throws SQLException {
        return DatabaseConnection.createStatement().executeQuery("SELECT completedBuilds FROM players WHERE uuid = " + player.getUniqueId()).getInt("completedBuilds");
    }

    public Slot getFreeSlot() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT firstSlot, secondSlot, thirdSlot FROM players WHERE uuid = '" + player.getUniqueId() + "'");

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
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT " + slot.name() +" FROM players WHERE uuid = '" + player.getUniqueId() + "'");

        if (rs.next()) {
            return new Plot(rs.getInt(slot.name()));
        }

        return null;
    }

    public void addScore(int score) throws SQLException {
        DatabaseConnection.prepareStatement(
                "UPDATE players SET score = " + (getScore() + score) + " WHERE uuid = '" + player.getUniqueId() + "'"
        );
    }

    public void addCompletedBuild() throws SQLException {
        DatabaseConnection.prepareStatement(
                "UPDATE players SET completedBuilds = " + (getCompletedBuilds() + 1) + " WHERE uuid = '" + player.getUniqueId() + "'"
        );
    }

    public void setPlot(int plotID, Slot slot) throws SQLException {
        DatabaseConnection.prepareStatement("UPDATE players SET " + slot.name() + " = " + plotID + " WHERE uuid = '" + player.getUniqueId() + "'");
    }
}
