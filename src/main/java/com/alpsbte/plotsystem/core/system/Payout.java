/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021-2022, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.holograms.ScoreLeaderboard;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Payout {
    private final int id;
    private final ScoreLeaderboard.LeaderboardTimeframe timeframe;
    private final int position;
    private final String payoutAmount;

    private Payout(int id, ScoreLeaderboard.LeaderboardTimeframe timeframe, int position, String payoutAmount) {
        this.id = id;
        this.timeframe = timeframe;
        this.position = position;
        this.payoutAmount = payoutAmount;
    }

    public static Payout getPayout(ScoreLeaderboard.LeaderboardTimeframe timeframe, int position) throws SQLException {
        if (timeframe == ScoreLeaderboard.LeaderboardTimeframe.LIFETIME) {
            throw new IllegalArgumentException("Invalid option LIFETIME");
        }
        if (position < 1 || position > 10) {
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
        return payoutAmount;
    }
}