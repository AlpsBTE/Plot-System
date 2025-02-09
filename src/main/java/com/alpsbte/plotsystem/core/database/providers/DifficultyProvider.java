package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Difficulty;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;

import java.util.List;

public class DifficultyProvider {
    public List<Difficulty> getDifficulties() {
        // TODO: implement
        return List.of();
    }

    public Difficulty getDifficultyById(String id) {
        // TODO: implement
        return null;
    }

    public double getMultiplier(PlotDifficulty difficulty) {
        // TODO: implement (get from cached list)
        return 0;
    }

    public boolean setMultiplier(String id, double multiplier) {
        // TODO: implement
        return false;
    }

    public int getScoreRequirement(PlotDifficulty difficulty) {
        // TODO: implement (get from cached list)
        return 0;
    }

    public boolean setScoreRequirement(String id, int scoreRequirement) {
        // TODO: implement
        return false;
    }

    public boolean builderMeetsRequirements(Builder builder, PlotDifficulty plotDifficulty) {
        int playerScore = builder.getScore();
        int scoreRequirement = getScoreRequirement(plotDifficulty);
        return playerScore >= scoreRequirement;
    }
}
