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

package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Plot extends AbstractPlot {
    private final CityProject cityProject;
    private final PlotDifficulty difficulty;
    private Status status;
    private final String outlineBounds;
    private LocalDate lastActivity;
    private final List<Builder> members;

    private CityPlotWorld cityPlotWorld;

    public Plot(
            int id, CityProject cityProject, PlotDifficulty difficulty, UUID ownerUUID, Status status,
            String outlineBounds, LocalDate lastActivity, double version, PlotType type, List<Builder> members) {
        super(id, ownerUUID);
        this.cityProject = cityProject;
        this.difficulty = difficulty;
        this.status = status;
        this.outlineBounds = outlineBounds;
        this.lastActivity = lastActivity;
        this.plotVersion = version;
        this.plotType = type;
        this.members = members;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean setStatus(@NotNull Status status) {
        if (DataProvider.PLOT.setStatus(getID(), status)) {
            this.status = status;
            return true;
        }
        return false;
    }

    public CityProject getCityProject() {
        return cityProject;
    }

    public PlotDifficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public boolean setPlotOwner(@Nullable Builder plotOwner) {
        if (DataProvider.PLOT.setPlotOwner(getID(), plotOwner == null ? null : plotOwner.getUUID())) {
            this.plotOwner = plotOwner;
            return true;
        }
        return false;
    }

    @Override
    public double getVersion() {
        return plotVersion;
    }

    @Override
    public List<BlockVector2> getOutline() {
        if (outline != null)
            return this.outline;

        List<BlockVector2> pointVectors;
        pointVectors = getOutlinePoints(outlineBounds.isEmpty() ? "" : outlineBounds);
        return pointVectors;
    }

    @Override
    public LocalDate getLastActivity() {
        return lastActivity;
    }

    @Override
    public boolean setLastActivity(boolean setNull) {
        LocalDate activityDate = setNull ? null : LocalDate.now();
        if (DataProvider.PLOT.setLastActivity(getID(), activityDate)) {
            this.lastActivity = activityDate;
            return true;
        }
        return false;
    }

    public List<Builder> getPlotMembers() {
        return members;
    }

    @Override
    public PlotType getPlotType() {
        return plotType;
    }

    public boolean setPlotType(PlotType type) {
        if (DataProvider.PLOT.setPlotType(getID(), type)) {
            this.plotType = type;
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends PlotWorld> T getWorld() {
        if (getVersion() <= 2 || getPlotType().hasOnePlotPerWorld()) {
            if (onePlotWorld == null) onePlotWorld = new OnePlotWorld(this);
            return (T) onePlotWorld;
        } else {
            if (cityPlotWorld == null) cityPlotWorld = new CityPlotWorld(this);
            return (T) cityPlotWorld;
        }
    }

    @Override
    public byte[] getInitialSchematicBytes() {
        return DataProvider.PLOT.getInitialSchematic(getID());
    }

    public byte[] getCompletedSchematic() {
        return DataProvider.PLOT.getCompletedSchematic(getID());
    }

    @Override
    public BlockVector3 getCenter() {
        return super.getCenter();
    }

    public List<PlotReview> getReviewHistory() {
        return DataProvider.REVIEW.getPlotReviewHistory(getID());
    }

    public Optional<PlotReview> getLatestReview() {
        return DataProvider.REVIEW.getLatestReview(getID());
    }

    public boolean isReviewed() {
        return getLatestReview().isPresent();
    }

    public boolean isRejected() {
        return (getStatus() == Status.unfinished || getStatus() == Status.unreviewed) && isReviewed();
    }

    public boolean setPasted(boolean pasted) {
        return DataProvider.PLOT.setPasted(getID(), pasted);
    }

    public boolean addPlotMember(Builder member) {
        List<Builder> members = getPlotMembers();
        if (members.size() < 3 && members.stream().noneMatch(m -> m.getUUID().equals(member.getUUID()))) {
            Slot slot = member.getFreeSlot();
            if (slot != null) {
                members.add(member);
                if (DataProvider.PLOT.setPlotMembers(getID(), members)) {
                    if (!member.setSlot(slot, getID())) return false;
                    getPermissions().addBuilderPerms(member.getUUID());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removePlotMember(Builder member) {
        List<Builder> members = getPlotMembers();
        if (!members.isEmpty() && members.contains(member)) {
            members.remove(member);
            if (DataProvider.PLOT.setPlotMembers(getID(), members)) {
                Slot slot = member.getSlotByPlotId(getID());
                if (slot != null && !member.setSlot(slot, -1)) return false;
                if (getWorld().isWorldGenerated()) getPermissions().removeBuilderPerms(member.getUUID());
                return true;
            }
        }
        return false;
    }

    public static boolean meetsPlotDifficultyScoreRequirement(@NotNull Builder builder, PlotDifficulty plotDifficulty) {
        int playerScore = builder.getScore();
        int scoreRequirement = DataProvider.DIFFICULTY.getDifficultyByEnum(plotDifficulty).orElseThrow().getScoreRequirement();
        return playerScore >= scoreRequirement;
    }

    public static CompletableFuture<PlotDifficulty> getPlotDifficultyForBuilder(CityProject city, Builder builder) {
        // Check if plot difficulties are available
        boolean easyHasPlots = false, mediumHasPlots = false, hardHasPlots = false;
        if (!DataProvider.PLOT.getPlots(city, PlotDifficulty.EASY, Status.unclaimed).isEmpty()) easyHasPlots = true;
        if (!DataProvider.PLOT.getPlots(city, PlotDifficulty.MEDIUM, Status.unclaimed).isEmpty()) mediumHasPlots = true;
        if (!DataProvider.PLOT.getPlots(city, PlotDifficulty.HARD, Status.unclaimed).isEmpty()) hardHasPlots = true;

        if (hardHasPlots && meetsPlotDifficultyScoreRequirement(builder, PlotDifficulty.HARD)) { // Return hard
            return CompletableFuture.completedFuture(PlotDifficulty.HARD);
        } else if (mediumHasPlots && meetsPlotDifficultyScoreRequirement(builder, PlotDifficulty.MEDIUM)) { // Return medium
            return CompletableFuture.completedFuture(PlotDifficulty.MEDIUM);
        } else if (easyHasPlots && meetsPlotDifficultyScoreRequirement(builder, PlotDifficulty.EASY)) { // Return easy
            return CompletableFuture.completedFuture(PlotDifficulty.EASY);
        } else if (mediumHasPlots && meetsPlotDifficultyScoreRequirement(builder, PlotDifficulty.HARD)) { // If hard has no plots return medium
            return CompletableFuture.completedFuture(PlotDifficulty.EASY);
        } else if (easyHasPlots && meetsPlotDifficultyScoreRequirement(builder, PlotDifficulty.MEDIUM)) { // If medium has no plots return easy
            return CompletableFuture.completedFuture(PlotDifficulty.MEDIUM);
        } else if (!PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.ENABLE_SCORE_REQUIREMENT)) { // If score requirement is disabled get plot from any available difficulty
            if (easyHasPlots) {
                return CompletableFuture.completedFuture(PlotDifficulty.EASY);
            } else if (mediumHasPlots) {
                return CompletableFuture.completedFuture(PlotDifficulty.MEDIUM);
            }
        }
        return CompletableFuture.completedFuture(null); // If nothing is available return null
    }
}
