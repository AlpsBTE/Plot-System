package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.leaderboards.ScoreLeaderboard;
import com.sk89q.worldedit.util.command.argument.ArgumentException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Payout {
    private final int id;
    private ScoreLeaderboard.LeaderboardTimeframe timeframe;
    private int position;
    private String payout_amount;

    private Payout(int id, ScoreLeaderboard.LeaderboardTimeframe timeframe, int position, String payout_amount) {
        this.id = id;
        this.timeframe = timeframe;
        this.position = position;
        this.payout_amount = payout_amount;
    }

    public static Payout getPayout(ScoreLeaderboard.LeaderboardTimeframe timeframe, int position) throws SQLException {
        if(timeframe == ScoreLeaderboard.LeaderboardTimeframe.LIFETIME) {
            throw new IllegalArgumentException("Invalid option LIFETIME");
        }
        if(position < 1 || position > 10){
            throw new IllegalArgumentException("Illegal position " + position);
        }

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id, timeframe, position, payout_amount FROM plotsystem_payouts WHERE timeframe = ? AND position = ?")
                .setValue(timeframe.name()).setValue(position).executeQuery()) {
            Payout instance = null;

            if (rs.next()) {
                instance = new Payout(rs.getInt(1), ScoreLeaderboard.LeaderboardTimeframe.valueOf(rs.getString(2)), rs.getInt(3), rs.getString(4));
            }

            DatabaseConnection.closeResultSet(rs);
            return instance;
        }
    }

    public int getId() {
        return id;
    }

    public ScoreLeaderboard.LeaderboardTimeframe getTimeframe() {
        return timeframe;
    }

    public int getPosition() {
        return position;
    }

    public String getPayoutAmount() {
        return payout_amount;
    }
}
