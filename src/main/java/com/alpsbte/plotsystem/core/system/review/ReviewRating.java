package com.alpsbte.plotsystem.core.system.review;

import java.util.List;
import java.util.Optional;

public class ReviewRating {
    private int accuracyPoints;
    private int blockPalettePoints;
    private List<ToggleCriteria> checkedToggles;

    public ReviewRating(int accuracyPoints, int blockPalettePoints, List<ToggleCriteria> checkedToggles) {
        this.accuracyPoints = accuracyPoints;
        this.blockPalettePoints = blockPalettePoints;
        this.checkedToggles = checkedToggles;
    }

    public int getAccuracyPoints() {
        return accuracyPoints;
    }

    public void setAccuracyPoints(int points) {
        accuracyPoints = points;
    }

    public int getBlockPalettePoints() {
        return blockPalettePoints;
    }

    public void setBlockPalettePoints(int points) {
        blockPalettePoints = points;
    }

    public List<ToggleCriteria> getCheckedToggles() {
        return checkedToggles;
    }

    public void setCheckedToggles(List<ToggleCriteria> checkedToggles) {
        this.checkedToggles = checkedToggles;
    }
}
