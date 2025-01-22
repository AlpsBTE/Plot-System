package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;

public class DifficultyProvider {
    public double getMultiplier(PlotDifficulty difficulty) {
        // TODO: implement
        return 0;
    }

    public int getScoreRequirement(PlotDifficulty difficulty) {
        // TODO: implement
        return 0;
    }

    public boolean builderMeetsRequirements(Builder builder, PlotDifficulty plotDifficulty) {
        int playerScore = builder.getScore();
        int scoreRequirement = getScoreRequirement(plotDifficulty);
        return playerScore >= scoreRequirement;
    }
}
