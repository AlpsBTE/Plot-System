/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.enums.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Builder {
    private final UUID uuid;
    private String name;
    private int score;
    private int firstSlot;
    private int secondSlot;
    private int thirdSlot;
    private int plotType;

    public Builder(UUID UUID, String name, int score, int first_slot, int second_slot, int third_slot, int plotType) {
        this.uuid = UUID;
        this.name = name;
        this.score = score;
        this.firstSlot = first_slot;
        this.secondSlot = second_slot;
        this.thirdSlot = third_slot;
        this.plotType = plotType;
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

    public boolean setSlot(Slot slot, int plotId) {
        if (DataProvider.BUILDER.setSlot(this.uuid, plotId, slot)) {
            switch (slot) {
                case FIRST -> firstSlot = plotId;
                case SECOND -> secondSlot = plotId;
                case THIRD -> thirdSlot = plotId;
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
