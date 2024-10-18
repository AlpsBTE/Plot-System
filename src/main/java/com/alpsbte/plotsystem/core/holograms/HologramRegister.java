package com.alpsbte.plotsystem.core.holograms;

// import com.aseanbte.aseanlib.hologram.DecentHologramDisplay;
import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;

public final class HologramRegister extends HologramManager {

    public static void init() {
        activeDisplays.add(new ScoreLeaderboard());
        activeDisplays.add(new PlotsLeaderboard());
    }

    public static class LeaderboardPositionLine extends DecentHologramDisplay.TextLine {
        public LeaderboardPositionLine(int position, String username, int score) {
            super("§e#" + position + " " + (username != null ? "§a" + username : "§8No one, yet") + " §7- §b" + score);
        }
    }
}