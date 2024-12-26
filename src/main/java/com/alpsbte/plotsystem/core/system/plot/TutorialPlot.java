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

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialDataModel;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import com.sk89q.worldedit.math.BlockVector2;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static net.kyori.adventure.text.Component.text;

public class TutorialPlot extends AbstractPlot implements TutorialDataModel {
    private int tutorialId = -1;
    private final FileConfiguration tutorialConfig;

    public TutorialPlot(int id) throws SQLException {
        super(id);
        tutorialConfig = ConfigUtil.getTutorialInstance().configs[getTutorialID()];
    }

    @Override
    public UUID getPlayerUUID() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT player_uuid FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                String uuid = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return UUID.fromString(uuid);
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return null;
    }

    @Override
    public int getTutorialID() throws SQLException {
        if (tutorialId != -1) return tutorialId;
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT tutorial_id FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                int id = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                tutorialId = id;
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return tutorialId;
    }

    @Override
    public int getStageID() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT stage_id FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                int stage = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return stage;
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return -1;
    }

    /**
     * Sets the stage of the tutorial and updates the last stage completion date.
     * Check if the stage is valid before setting it!
     *
     * @param stageID stage id, 0 is the first stage
     */
    public void setStageID(int stageID) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_plots_tutorial SET stage_id = ? WHERE id = ?")
                .setValue(stageID).setValue(this.ID).executeUpdate();
        setLastStageCompletionDate(Timestamp.valueOf(LocalDateTime.now()));
    }

    @Override
    public boolean isCompleted() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT is_completed FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                int isCompleted = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return isCompleted == 1;
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return false;
    }

    /**
     * Sets the completed status of the tutorial and updates the completion date.
     */
    public void setCompleted() throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_plots_tutorial SET is_completed = ? WHERE id = ?")
                .setValue(1).setValue(this.ID).executeUpdate();
        setCompletionDate(Timestamp.valueOf(LocalDateTime.now()));
    }

    @Override
    public Date getCreationDate() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT create_date FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                Date d = rs.getDate(1);
                DatabaseConnection.closeResultSet(rs);
                return d;
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return null;
    }

    @Override
    public Date getLastStageCompletionDate() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT last_stage_complete_date FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                Date d = rs.getDate(1);
                DatabaseConnection.closeResultSet(rs);
                return d;
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return null;
    }

    /**
     * Sets the date when the last stage was completed.
     *
     * @param date date of the last stage completion
     */
    private void setLastStageCompletionDate(Date date) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_plots_tutorial SET last_stage_complete_date = ? WHERE id = ?")
                .setValue(date).setValue(this.ID).executeUpdate();
    }

    @Override
    public Date getCompletionDate() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT complete_date FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                Date d = rs.getDate(1);
                DatabaseConnection.closeResultSet(rs);
                return d;
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return null;
    }

    /**
     * Sets the date when the tutorial was completed.
     *
     * @param date date of the completion
     */
    private void setCompletionDate(Date date) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_plots_tutorial SET complete_date = ? WHERE id = ?")
                .setValue(date).setValue(this.ID).executeUpdate();
    }

    @Override
    public Builder getPlotOwner() throws SQLException {
        if (plotOwner != null) return plotOwner;
        plotOwner = Builder.byUUID(getPlayerUUID());
        return plotOwner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PlotWorld> T getWorld() throws SQLException {
        try {
            if (onePlotWorld == null) onePlotWorld = new OnePlotWorld(this);
            return (T) onePlotWorld;
        } catch (SQLException ex) {PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);}
        return null;
    }

    @Override
    public List<BlockVector2> getOutline() throws SQLException, IOException {
        String plotOutlines = tutorialConfig.getString(TutorialPaths.Beginner.PLOT_OUTLINES);
        return getOutlinePoints(plotOutlines);
    }

    /**
     * @deprecated Use {@link TutorialDataModel#getLastStageCompletionDate()} instead.
     */
    @Override
    public Date getLastActivity() throws SQLException {
        return getLastStageCompletionDate(); // Temporary to fix backwards compatibility
    }

    /**
     * @deprecated Use {@link TutorialPlot#setLastStageCompletionDate(Date)} instead.
     */
    @Override
    public void setLastActivity(boolean setNull) throws SQLException {
        setLastStageCompletionDate(java.sql.Date.valueOf(LocalDate.now())); // Temporary to fix backwards compatibility
    }

    /**
     * @deprecated Use {@link TutorialDataModel#isCompleted()} instead.
     */
    @Override
    public Status getStatus() throws SQLException {
        return isCompleted() ? Status.completed : Status.unfinished; // Temporary to fix backwards compatibility
    }

    /**
     * @deprecated Use {@link TutorialPlot#setCompleted()} instead.
     */
    @Override
    public void setStatus(@NotNull Status status) throws SQLException {
        if (status == Status.completed) setCompleted(); // Temporary to fix backwards compatibility
    }

    @Override
    public PlotType getPlotType() {
        return PlotType.TUTORIAL;
    }

    @Override
    public double getVersion() {
        return 3;
    }

    @Override
    protected File getSchematicFile(String fileName) {
        File newSchem = Paths.get(PlotUtils.getDefaultSchematicPath(), "tutorials", fileName + ".schem").toFile();
        if (newSchem.exists()) return newSchem;
        File oldSchem = Paths.get(PlotUtils.getDefaultSchematicPath(), "tutorials", fileName + ".schematic").toFile();
        if (oldSchem.exists()) return oldSchem;

        try {
            FileUtils.copyInputStreamToFile(Objects.requireNonNull(PlotSystem.getPlugin().getResource("tutorial/schematics/" + fileName + ".schematic.gz")), oldSchem);
        } catch (IOException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while copying the schematic file!"), ex);
        }
        return oldSchem;
    }

    public File getOutlinesSchematic(int schematicId) {
        try {
            return getSchematicFile(getTutorialID() + "-" + schematicId);
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            return null;
        }
    }

    @Override
    public File getOutlinesSchematic() {
        return getOutlinesSchematic(0);
    }

    @Override
    public File getEnvironmentSchematic() {
        try {
            return getSchematicFile(getTutorialID() + "-env");
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            return null;
        }
    }


    /**
     * Adds a new tutorial plot to the database.
     *
     * @param UUID       uuid of the player
     * @param tutorialId id of the tutorial
     * @return the new tutorial plot
     */
    public static TutorialPlot addTutorialPlot(String UUID, int tutorialId) throws SQLException {
        DatabaseConnection.createStatement("INSERT INTO plotsystem_plots_tutorial (player_uuid, tutorial_id) VALUES (?, ?)")
                .setValue(UUID).setValue(tutorialId).executeUpdate();
        return getPlot(UUID, tutorialId);
    }

    /**
     * Gets a tutorial plot from the database.
     *
     * @param UUID       uuid of the player
     * @param tutorialId id of the tutorial
     * @return the tutorial plot
     */
    public static TutorialPlot getPlot(String UUID, int tutorialId) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots_tutorial WHERE player_uuid = ? AND tutorial_id = ?")
                .setValue(UUID).setValue(tutorialId).executeQuery()) {

            if (rs.next()) {
                int id = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);

                return new TutorialPlot(id);
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return null;
    }

    /**
     * Gets all tutorials from the player.
     *
     * @param builderUUID uuid of the player
     * @return list of tutorials
     */
    public static List<TutorialPlot> getPlots(UUID builderUUID) throws SQLException {
        return listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots_tutorial WHERE player_uuid = ?")
                .setValue(builderUUID.toString()).executeQuery());
    }

    public static boolean isPlotCompleted(Player player, int tutorialId) throws SQLException {
        TutorialPlot plot = TutorialPlot.getPlot(player.getUniqueId().toString(), tutorialId);
        if (plot == null) return false;
        return plot.isCompleted();
    }

    private static List<TutorialPlot> listPlots(ResultSet rs) throws SQLException {
        List<TutorialPlot> plots = new ArrayList<>();

        while (rs.next()) {
            plots.add(new TutorialPlot(rs.getInt(1)));
        }

        DatabaseConnection.closeResultSet(rs);
        return plots;
    }
}
