package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.plots.Plot;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public Plot getActivePlot(Slot slot) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT firstSlot, secondSlot, thirdSlot FROM players WHERE uuid = '" + player.getUniqueId() + "'");

        switch (slot) {
            case FIRST:
                return new Plot(rs.getInt("firstSlot"));
            case SECOND:
                return new Plot(rs.getInt("secondSlot"));
            case THIRD:
                return new Plot(rs.getInt("thirdSlot"));
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

    public void setFirstPlot(Plot plot) throws SQLException {
        DatabaseConnection.prepareStatement("UPDATE players SET firstSlot = " + plot.getID() + " WHERE uuid = '" + player.getUniqueId() + "'");
    }

    public void setSecondPlot(Plot plot) throws SQLException {
        DatabaseConnection.prepareStatement("UPDATE players SET secondPlot = " + plot.getID() + " WHERE uuid = '" + player.getUniqueId() + "'");
    }

    public void setThirdPlot(Plot plot) throws SQLException {
        DatabaseConnection.prepareStatement("UPDATE players SET thirdPlot = " + plot.getID() + " WHERE uuid = '" + player.getUniqueId() + "'");
    }
}
