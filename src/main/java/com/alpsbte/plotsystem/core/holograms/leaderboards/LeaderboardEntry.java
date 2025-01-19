package com.alpsbte.plotsystem.core.holograms.leaderboards;

public class LeaderboardEntry {
    private final int score;
    private final int position;
    private final int totalPositions;

    public LeaderboardEntry(int score, int position, int totalPositions) {
        this.score = score;
        this.position = position;
        this.totalPositions = totalPositions;
    }

    public int getScore() {
        return score;
    }

    public int getPosition() {
        return position;
    }

    public int getTotalPosition() {
        return totalPositions;
    }
}