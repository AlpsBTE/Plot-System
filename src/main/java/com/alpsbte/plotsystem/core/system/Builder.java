package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Builder {
    private final UUID uuid;
    private String name;
    private int score;
    private int firstSlot;
    private int secondSlot;
    private int thirdSlot;
    private int plotType;

    public Builder(UUID uniqueId, String name, int score, int firstSlot, int secondSlot, int thirdSlot, int plotType) {
        this.uuid = uniqueId;
        this.name = name;
        this.score = score;
        this.firstSlot = firstSlot;
        this.secondSlot = secondSlot;
        this.thirdSlot = thirdSlot;
        this.plotType = plotType;

        boolean inspirationModeDisabled = PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DISABLE_CITY_INSPIRATION_MODE); // TODO remove or improve when CIM is working again
        if (inspirationModeDisabled && plotType == 2) this.plotType = 1;
    }

    public java.util.UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean setName(String name) {
        if (DataProvider.BUILDER.setName(this.uuid, name)) {
            this.name = name;
            return true;
        }
        return false;
    }

    public int getScore() {
        return score;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean addScore(int score) {
        if (DataProvider.BUILDER.addScore(this.uuid, score)) {
            this.score = this.score + score;
            return true;
        }
        return false;
    }

    public Plot getSlot(Slot slot) {
        if (slot == Slot.FIRST && firstSlot != -1) {
            return DataProvider.PLOT.getPlotById(firstSlot);
        } else if (slot == Slot.SECOND && secondSlot != -1) {
            return DataProvider.PLOT.getPlotById(secondSlot);
        } else if (slot == Slot.THIRD && thirdSlot != -1) {
            return DataProvider.PLOT.getPlotById(thirdSlot);
        }
        return null;
    }

    public Slot getSlot(Plot plot) {
        return DataProvider.BUILDER.getSlot(this.uuid, plot.getId());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean setSlot(Slot slot, int plotId) {
        if (DataProvider.BUILDER.setSlot(this.uuid, plotId, slot)) {
            switch (slot) {
                case FIRST -> firstSlot = plotId;
                case SECOND -> secondSlot = plotId;
                default -> thirdSlot = plotId;
            }
            return true;
        }
        return false;
    }

    public PlotType getPlotType() {
        return PlotType.byId(plotType);
    }

    public boolean setPlotType(PlotType plotType) {
        if (DataProvider.BUILDER.setPlotType(this.uuid, plotType.getId())) {
            this.plotType = plotType.getId();
            return true;
        }
        return false;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public int getCompletedBuildsCount() {
        return DataProvider.BUILDER.getCompletedBuildsCount(uuid);
    }

    public Slot getFreeSlot() {
        return DataProvider.BUILDER.getFreeSlot(uuid);
    }

    public Slot getSlotByPlotId(int plotId) {
        if (firstSlot == plotId) {
            return Slot.FIRST;
        } else if (secondSlot == plotId) {
            return Slot.SECOND;
        } else if (thirdSlot == plotId) {
            return Slot.THIRD;
        }
        return null;
    }

    public static Builder byUUID(UUID uuid) {
        return DataProvider.BUILDER.getBuilderByUUID(uuid);
    }

    public static Builder byName(String name) {
        return DataProvider.BUILDER.getBuilderByName(name);
    }
}
