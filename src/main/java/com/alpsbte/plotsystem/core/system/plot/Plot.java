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

package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Review;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;

public class Plot extends AbstractPlot {
    private final CityProject city;
    private final PlotDifficulty difficulty;
    private final Builder plotCreator;
    private final Date createdDate;
    private final String outlineString;

    private CityPlotWorld cityPlotWorld;
    private Status status;
    private LocalDate lastActivity;
    private List<Builder> plotMembers;

    // missing:
    // outlineBounds
    // initialSchem
    // is_pasted
    public Plot(
            int id,
            Status status,
            CityProject city,
            PlotDifficulty difficulty,
            Builder createdBy,
            Date createdDate,
            double plotVersion,
            String outline,
            LocalDate lastActivity,
            List<Builder> members,
            PlotType type
    ) {
        super(id);
        this.status = status;
        this.city = city;
        this.difficulty = difficulty;
        this.plotCreator = createdBy;
        this.createdDate = createdDate;
        this.plotVersion = plotVersion;
        this.outlineString = outline;
        this.lastActivity = lastActivity;
        this.plotMembers = members;
        this.plotType = type;
    }

    public CityProject getCity() {
        return city;
    }

    public PlotDifficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public Builder getPlotOwner() {
        return plotOwner;
    }

    public boolean setPlotOwner(@Nullable Builder newPlotOwner) {
        if (DataProvider.PLOT.setPlotOwner(ID, newPlotOwner == null ? null : newPlotOwner.getUUID())) {
            plotOwner = newPlotOwner;
            return true;
        }
        return false;
    }

    public List<Builder> getPlotMembers() {
        return plotMembers;
    }

    public boolean setPlotMembers(@NotNull List<Builder> plotMembers) {
        if (DataProvider.PLOT.setPlotMembers(ID, plotMembers)) {
            this.plotMembers = plotMembers;
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends PlotWorld> T getWorld() {
        try {
            if (getVersion() <= 2 || getPlotType().hasOnePlotPerWorld()) {
                if (onePlotWorld == null) onePlotWorld = new OnePlotWorld(this);
                return (T) onePlotWorld;
            } else {
                if (cityPlotWorld == null) cityPlotWorld = new CityPlotWorld(this);
                return (T) cityPlotWorld;
            }
        } catch (SQLException ex) {PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);}
        return null;
    }

    @Override
    public List<BlockVector2> getOutline() throws IOException {
        if (outline != null)
            return this.outline;

        List<BlockVector2> pointVectors;
        pointVectors = getOutlinePoints((outlineString.isEmpty() || getVersion() <= 2) ? null : outlineString);
        return pointVectors;
    }

    @Override
    public LocalDate getLastActivity() {
        return lastActivity;
    }

    @Override
    public boolean setLastActivity(boolean setNull) {
        LocalDate activityDate = setNull ? null : LocalDate.now();
        if (DataProvider.PLOT.setLastActivity(ID, activityDate)) {
            this.lastActivity = activityDate;
            return true;
        }
        return false;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean setStatus(@NotNull Status status) {
        if (DataProvider.PLOT.setStatus(ID, status)) {
            this.status = status;
            return true;
        }
        return false;
    }

    public int getTotalScore() {
        // TODO: implement
        return 0;
    }

    public void setTotalScore(int score) throws SQLException {
        if (score == -1) {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET score = DEFAULT(score) WHERE id = ?")
                    .setValue(this.ID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET score = ? WHERE id = ?")
                    .setValue(score).setValue(this.ID).executeUpdate();
        }
    }

    public int getSharedScore() throws SQLException {
        int score = getTotalScore();
        if (score != -1 && !getPlotMembers().isEmpty()) {
            return (int) Math.floor(score / (getPlotMembers().size() + 1d));
        }
        return score;
    }

    @Override
    public PlotType getPlotType() {
        return plotType;
    }

    public boolean setPlotType(PlotType type) {
        if (DataProvider.PLOT.setPlotType(ID, type)) {
            this.plotType = type;
            return true;
        }
        return false;
    }

    @Override
    public double getVersion() {
        return plotVersion;
    }

    @Override
    public byte[] getInitialSchematicBytes() {return DataProvider.PLOT.getInitialSchematic(ID);}

    public byte[] getCompletedSchematic() {
        return DataProvider.PLOT.getCompletedSchematic(ID);
    }

    @Deprecated
    public BlockVector3 getMinecraftCoordinates() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT mc_coordinates FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                String[] mcLocation = rs.getString(1).split(",");
                DatabaseConnection.closeResultSet(rs);
                return BlockVector3.at(Double.parseDouble(mcLocation[0]), Double.parseDouble(mcLocation[1]), Double.parseDouble(mcLocation[2]));
            }

            DatabaseConnection.closeResultSet(rs);

            return null;
        }
    }

    public BlockVector3 getCenter() {
        try {
            if (getVersion() >= 3) {
                return super.getCenter();
            } else return BlockVector3.at(PlotWorld.PLOT_SIZE / 2d, this.getWorld().getPlotHeightCentered(), PlotWorld.PLOT_SIZE / 2d);
        } catch (IOException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Failed to load schematic file to clipboard!"), ex);
        }
        return null;
    }

    public Slot getSlot() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT first_slot, second_slot, third_slot FROM plotsystem_builders WHERE uuid = ?")
                .setValue(this.getPlotOwner().getUUID().toString()).executeQuery()) {

            if (rs.next()) {
                for (int i = 1; i <= 3; i++) {
                    int slot = rs.getInt(i);
                    if (!rs.wasNull() && slot == getID()) {
                        DatabaseConnection.closeResultSet(rs);
                        return Slot.values()[i - 1];
                    }
                }
            }

            DatabaseConnection.closeResultSet(rs);

            return null;
        }
    }

    public Review getReview() {
        return DataProvider.PLOT.getReview(ID);
    }

    public void setPasted(boolean pasted) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_plots SET pasted = ? WHERE id = ?")
                .setValue(pasted).setValue(this.ID).executeUpdate();
    }

    public void addPlotMember(Builder member) {
        List<Builder> members = getPlotMembers();
        if (members.size() < 3 && members.stream().noneMatch(m -> m.getUUID().equals(member.getUUID()))) {
            Slot slot = member.getFreeSlot();
            if (slot != null) {
                members.add(member);
                setPlotMembers(members);

                member.setSlot(slot, ID);
                getPermissions().addBuilderPerms(member.getUUID());
            }
        }
    }

    public void removePlotMember(Builder member) throws SQLException {
        List<Builder> members = getPlotMembers();
        if (!members.isEmpty() && members.stream().anyMatch(m -> m.getUUID().equals(member.getUUID()))) {
            members.remove(members.stream().filter(m -> m.getUUID().equals(member.getUUID())).findFirst().orElse(null));
            setPlotMembers(members);

            Slot slot = member.getSlotByPlotId(ID);
            if (slot != null) member.setSlot(slot, -1);
            if (getWorld().isWorldGenerated()) getPermissions().removeBuilderPerms(member.getUUID());
        }
    }

    public boolean isReviewed() {
        return getReview() != null;
    }

    public boolean isRejected() {
        return (getStatus() == Status.unfinished || getStatus() == Status.unreviewed) && getTotalScore() != -1; // -1 == null
    }

    public static double getMultiplierByDifficulty(PlotDifficulty plotDifficulty) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement("SELECT multiplier FROM plotsystem_difficulties WHERE id = ?")
                .setValue(plotDifficulty.ordinal() + 1).executeQuery();

        if (rs.next()) {
            double d = rs.getDouble(1);
            DatabaseConnection.closeResultSet(rs);
            return d;
        }

        DatabaseConnection.closeResultSet(rs);
        return 1;
    }

    public static int getScoreRequirementByDifficulty(PlotDifficulty plotDifficulty) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT score_requirment FROM plotsystem_difficulties WHERE id = ?")
                .setValue(plotDifficulty.ordinal() + 1).executeQuery()) {

            if (rs.next()) {
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return i;
            }

            DatabaseConnection.closeResultSet(rs);
            return 0;
        }
    }

    public static boolean hasPlotDifficultyScoreRequirement(Builder builder, PlotDifficulty plotDifficulty) throws SQLException {
        int playerScore = builder.getScore();
        int scoreRequirement = Plot.getScoreRequirementByDifficulty(plotDifficulty);
        return playerScore >= scoreRequirement;
    }

    public static CompletableFuture<PlotDifficulty> getPlotDifficultyForBuilder(CityProject city, Builder builder) throws SQLException {
        // Check if plot difficulties are available
        boolean easyHasPlots = false, mediumHasPlots = false, hardHasPlots = false;
        if (!DataProvider.PLOT.getPlots(city, PlotDifficulty.EASY, Status.unclaimed).isEmpty()) easyHasPlots = true;
        if (!DataProvider.PLOT.getPlots(city, PlotDifficulty.MEDIUM, Status.unclaimed).isEmpty()) mediumHasPlots = true;
        if (!DataProvider.PLOT.getPlots(city, PlotDifficulty.HARD, Status.unclaimed).isEmpty()) hardHasPlots = true;

        if (hardHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.HARD)) { // Return hard
            return CompletableFuture.completedFuture(PlotDifficulty.HARD);
        } else if (mediumHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.MEDIUM)) { // Return medium
            return CompletableFuture.completedFuture(PlotDifficulty.MEDIUM);
        } else if (easyHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.EASY)) { // Return easy
            return CompletableFuture.completedFuture(PlotDifficulty.EASY);
        } else if (mediumHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.HARD)) { // If hard has no plots return medium
            return CompletableFuture.completedFuture(PlotDifficulty.EASY);
        } else if (easyHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.MEDIUM)) { // If medium has no plots return easy
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
