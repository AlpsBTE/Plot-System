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
    private int score;
    private int completedBuilds;

    private Plot firstPlot;
    private Plot secondPlot;
    private Plot thirdPlot;

    public Builder(Player player) throws SQLException {
        this.player = player;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM players WHERE uuid = " + player.getUniqueId());

        if(rs.next()) {
            // Player Score
            this.score = rs.getInt("score");

            // Completed Builds
            this.completedBuilds = rs.getInt("completedBuilds");

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

    public int getScore() {
        return score;
    }

    public int getCompletedBuilds() {
        return completedBuilds;
    }

    public List<Plot> getPlots() {
        return Arrays.asList(firstPlot, secondPlot, thirdPlot);
    }

    public Plot getPlotBySlot(Slot slot) {
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

    public void setPlotBySlot(Plot plot, Slot slot) throws SQLException {
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
