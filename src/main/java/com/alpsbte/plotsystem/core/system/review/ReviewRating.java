package com.alpsbte.plotsystem.core.system.review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReviewRating {
    private int accuracyPoints;
    private int blockPalettePoints;
    private final List<ToggleCriteria> checkedToggles;
    private final List<ToggleCriteria> uncheckedToggles;

    public ReviewRating(int accuracyPoints, int blockPalettePoints, List<ToggleCriteria> checkedToggles, List<ToggleCriteria> uncheckedToggles) {
        this.accuracyPoints = accuracyPoints;
        this.blockPalettePoints = blockPalettePoints;
        this.checkedToggles = checkedToggles;
        this.uncheckedToggles = uncheckedToggles;
    }

    public ReviewRating(int accuracyPoints, int blockPalettePoints, HashMap<ToggleCriteria, Boolean> toggles) {
        this.accuracyPoints = accuracyPoints;
        this.blockPalettePoints = blockPalettePoints;

        List<ToggleCriteria> checkedToggles = new ArrayList<>();
        List<ToggleCriteria> uncheckedToggles = new ArrayList<>();
        for (ToggleCriteria criteria : toggles.keySet()) {
            if (toggles.get(criteria)) checkedToggles.add(criteria);
            else uncheckedToggles.add(criteria);
        }

        this.checkedToggles = checkedToggles;
        this.uncheckedToggles = uncheckedToggles;
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
    public HashMap<ToggleCriteria, Boolean> getAllToggles() {
        HashMap<ToggleCriteria, Boolean> allToggles = new HashMap<>();
        for (ToggleCriteria checked : checkedToggles) allToggles.put(checked, true);
        for (ToggleCriteria unchecked : uncheckedToggles) allToggles.put(unchecked, false);

        return allToggles;
    }

    public void setToggleCriteria(ToggleCriteria criteria, boolean isToggled) {
        if (isToggled) {
            checkedToggles.add(criteria);
            uncheckedToggles.remove(criteria);
        } else {
            uncheckedToggles.add(criteria);
            checkedToggles.remove(criteria);
        }
    }

    public int getTogglePoints() {
        return (int) Math.floor(((double) checkedToggles.size() / (checkedToggles.size() + uncheckedToggles.size())) * 10);
    }

    public int getTotalRating() {
        return accuracyPoints + blockPalettePoints + getTogglePoints();
    }

    public boolean isRejected() {
        // a plot is rejected if either of the point sliders are 0
        if (accuracyPoints == 0 || blockPalettePoints == 0) return true;

        for (ToggleCriteria unchecked : uncheckedToggles) {
            // a plot is also rejected if any of the required toggles are not checked
            if (!unchecked.isOptional()) return true;
        }

        // a plot is also rejected if the total rating is less than or equal to 8
        return getTotalRating() <= 8;
    }

    public String getRatingDatabaseString() {
        return accuracyPoints + "," + blockPalettePoints;
    }
}
