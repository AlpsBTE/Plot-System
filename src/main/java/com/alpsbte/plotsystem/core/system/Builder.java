/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Builder {
    private final UUID uuid;
    private String name;
    private int score;
    private int firstSlot;
    private int secondSlot;
    private int thirdSlot;
    private int plotType;

    public Builder(UUID UUID, String name, int score, int first_slot, int second_slot, int third_slot, int plot_type) {
        this.uuid = UUID;
        this.name = name;
        this.score = score;
        this.firstSlot = first_slot;
        this.secondSlot = second_slot;
        this.thirdSlot = third_slot;
        this.plotType = plot_type;
    }

    public java.util.UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public CompletableFuture<Boolean> setName(String name) {
        this.name = name;
        return DataProvider.BUILDER.setName(uuid, name);
    }

    public int getScore() {
        return score;
    }

    public CompletableFuture<Boolean> addScore(int score) {
        return DataProvider.BUILDER.addScore(uuid, score)
                .thenCompose(success-> {
                    if (success) this.score = this.score + score;
                    return CompletableFuture.completedFuture(success);
                });
    }

    public Plot getSlot(Slot slot) {
        if (slot == Slot.first_slot && firstSlot != -1) {
            return new Plot(firstSlot);
        } else if (slot == Slot.second_slot && secondSlot != -1) {
            return new Plot(secondSlot);
        } else if (slot == Slot.third_slot && thirdSlot != -1) {
            return new Plot(thirdSlot);
        }
        return null;
    }

    public CompletableFuture<Boolean> setSlot(Slot slot, int plotId) {
        return DataProvider.BUILDER.setSlot(uuid, plotId, slot)
                .thenCompose(success -> {
                    if (success) {
                        switch (slot) {
                            case first_slot: firstSlot = plotId; break;
                            case second_slot: secondSlot = plotId; break;
                            case third_slot: thirdSlot = plotId; break;
                        }
                    }
                    return CompletableFuture.completedFuture(success);
                });
    }

    public PlotType getPlotType() {
        return PlotType.byId(plotType);
    }

    public CompletableFuture<Boolean> setPlotType(int plotType) {
        return DataProvider.BUILDER.setPlotType(uuid, plotType)
                .thenCompose(success -> {
                    if (success) this.plotType = plotType;
                    return CompletableFuture.completedFuture(success);
                });
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public CompletableFuture<Integer> getCompletedBuildsCount() {
        return DataProvider.BUILDER.getCompletedBuildsCount(uuid);
    }

    public CompletableFuture<Slot> getFreeSlot() {
        return DataProvider.BUILDER.getFreeSlot(uuid);
    }

    public static CompletableFuture<Builder> byUUID(UUID uuid) {
        return DataProvider.BUILDER.getBuilderByUUID(uuid);
    }

    public static CompletableFuture<Builder> byName(String name) {
        return DataProvider.BUILDER.getBuilderByName(name);
    }

    public Reviewer getAsReviewer() throws SQLException {
        return new Reviewer(getUUID());
    }

    public static class Reviewer {
        private final List<BuildTeam> buildTeams;

        public Reviewer(UUID reviewerUUID) throws SQLException {
            this.buildTeams = BuildTeam.getBuildTeamsByReviewer(reviewerUUID);
        }

        public List<Country> getCountries() {
            return DataProvider.BUILDER.getReviewerCountries(buildTeams);
        }
    }
}
