package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.plots.Plot;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Builder {

    private final Player player;

    private Plot firstPlot;
    private Plot secondPlot;
    private Plot thirdPlot;

    public Builder(Player player) throws SQLException {
        this.player = player;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM players WHERE uuid = " + player.getUniqueId());

        if(rs.next()) {
            // First Plot
            this.firstPlot = new Plot(rs.getInt("firstSlot"));

            // Second Plot
            this.secondPlot = new Plot(rs.getInt("secondSlot"));

            // Third Plot
            this.thirdPlot = new Plot(rs.getInt("thirdSlot"));
        }
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

    public List<Plot> getPlots() {
        return Arrays.asList(firstPlot, secondPlot, thirdPlot);
    }

    public Plot getPlot(Slot slot) {
        if(slot == Slot.first) {
            return firstPlot;
        } else if(slot == Slot.second) {
            return secondPlot;
        } else if(slot == Slot.third){
            return thirdPlot;
        }
        return null;
    }

    public void addScore(int score) throws SQLException {
        DatabaseConnection.prepareStatement(
                "UPDATE players SET score = " + (getScore() + score) + " WHERE uuid = " + player.getUniqueId()
        );
    }

    public void addCompletedBuild() throws SQLException {
        DatabaseConnection.prepareStatement(
                "UPDATE players SET completedBuilds = " + (getCompletedBuilds() + 1) + " WHERE uuid = " + player.getUniqueId()
        );
    }

    public void setPlot(Plot plot, Slot slot) throws SQLException {
        String query = "UPDATE players SET {slot} = " + plot.getID() + " WHERE uuid = " + player.getUniqueId();

        if(slot == Slot.first) {
            DatabaseConnection.prepareStatement(query.replace("{slot}", "firstPlot"));
        } else if(slot == Slot.second) {
            DatabaseConnection.prepareStatement(query.replace("{slot}", "secondPlot"));
        } else if(slot == Slot.third){
            DatabaseConnection.prepareStatement(query.replace("{slot}", "thirdPlot"));
        }
    }
}
