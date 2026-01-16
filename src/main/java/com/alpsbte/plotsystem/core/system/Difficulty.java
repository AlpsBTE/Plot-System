package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.entity.Player;

public class Difficulty {
    private final String ID;
    private final PlotDifficulty difficulty;

    private double multiplier;
    private int scoreRequirement;

    public Difficulty(PlotDifficulty difficulty, String id, double multiplier, int scoreRequirement) {
        this.difficulty = difficulty;
        this.ID = id;
        this.multiplier = multiplier;
        this.scoreRequirement = scoreRequirement;
    }

    public String getID() {
        return ID;
    }

    public String getName(Player player) {
        return LangUtil.getInstance().get(player, LangPaths.Database.DIFFICULTY + "." + ID + ".name");
    }

    public PlotDifficulty getDifficulty() {
        return difficulty;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getScoreRequirement() {
        return scoreRequirement;
    }

    public boolean setMultiplier(double multiplier) {
        if (DataProvider.DIFFICULTY.setMultiplier(ID, multiplier)) {
            this.multiplier = multiplier;
            return true;
        }
        return false;
    }

    public boolean setScoreRequirement(int scoreRequirement) {
        if (DataProvider.DIFFICULTY.setScoreRequirement(ID, scoreRequirement)) {
            this.scoreRequirement = scoreRequirement;
            return true;
        }
        return false;
    }
}
