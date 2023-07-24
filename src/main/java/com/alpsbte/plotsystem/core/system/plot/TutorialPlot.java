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
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import com.sk89q.worldedit.BlockVector2D;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;

public class TutorialPlot extends AbstractPlot {
    private int tutorialId = -1;
    private final FileConfiguration tutorialConfig;

    public TutorialPlot(int id) throws SQLException {
        super(id);
        tutorialConfig = ConfigUtil.getInstance().configs[2 + getTutorialId()];
    }

    public int getTutorialId() throws SQLException {
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
    public Builder getPlotOwner() throws SQLException {
        if(plotOwner != null)
            return plotOwner;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT owner_uuid FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()){
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);

                plotOwner = Builder.byUUID(UUID.fromString(s));

                return plotOwner;
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PlotWorld> T getWorld() throws SQLException {
        try {
            if (onePlotWorld == null) onePlotWorld = new OnePlotWorld(this);
            return (T) onePlotWorld;
        } catch (SQLException ex) { Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex); }
        return null;
    }

    @Override
    public List<BlockVector2D> getOutline() throws SQLException, IOException {
        String plotOutlines = tutorialConfig.getString(TutorialPaths.PLOT_OUTLINES);
        return getOutlinePoints(plotOutlines);
    }

    @Override
    public Date getLastActivity() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT last_activity FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                Date d = rs.getDate(1);
                DatabaseConnection.closeResultSet(rs);
                return d;
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    @Override
    public void setLastActivity(boolean setNull) throws SQLException {
        if(setNull) {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots_tutorial SET last_activity = DEFAULT(last_activity) WHERE id = ?")
                    .setValue(this.ID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots_tutorial SET last_activity = ? WHERE id = ?")
                    .setValue(java.sql.Date.valueOf(LocalDate.now())).setValue(this.ID).executeUpdate();
        }
    }

    @Override
    public Status getStatus() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT is_completed FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                int b = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return b == 1 ? Status.completed : Status.unfinished;
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    @Override
    public void setStatus(@NotNull Status status) throws SQLException {
        if (status != Status.completed && status != Status.unfinished) return;
        DatabaseConnection.createStatement("UPDATE plotsystem_plots_tutorial SET is_completed = ? WHERE id = ?")
            .setValue(status == Status.completed ? 1 : 0).setValue(this.ID).executeUpdate();
    }

    @Override
    public PlotType getPlotType() {
        return PlotType.TUTORIAL;
    }

    @Override
    public double getVersion() {
        return 3;
    }

    public int getStage() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT stage FROM plotsystem_plots_tutorial WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                int stage = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return stage;
            }

            DatabaseConnection.closeResultSet(rs);
            return -1;
        }
    }

    @Override
    protected File getSchematicFile(String fileName) {
        File file = Paths.get(PlotUtils.getDefaultSchematicPath(), "tutorials", fileName + ".schematic").toFile();
        if (file.exists()) return file;

        try {
            FileUtils.copyInputStreamToFile(PlotSystem.getPlugin().getResource("tutorial/schematics/" + fileName + ".schematic.gz"), file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public File getOutlinesSchematic(int stageId) {
        try {
            return getSchematicFile(getTutorialId() + "-" + stageId);
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return null;
        }
    }

    @Override
    public File getOutlinesSchematic() {
        try {
            return getOutlinesSchematic(getStage());
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return null;
        }
    }

    @Override
    public File getEnvironmentSchematic() {
        try {
            return getSchematicFile(getTutorialId() + "-env");
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return null;
        }
    }

    public FileConfiguration getTutorialConfig() {
        return tutorialConfig;
    }




    public static TutorialPlot addTutorialPlot(String UUID, int tutorialId) throws SQLException {
        DatabaseConnection.createStatement("INSERT INTO plotsystem_plots_tutorial (owner_uuid, tutorial_id) VALUES (?, ?)")
                .setValue(UUID).setValue(tutorialId).executeUpdate();
        return getPlot(UUID, tutorialId);
    }

    public static TutorialPlot getPlot(String UUID, int tutorialId) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots_tutorial WHERE owner_uuid = ? AND tutorial_id = ?")
                .setValue(UUID).setValue(tutorialId).executeQuery()) {

            if (rs.next()){
                int id = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);

                return new TutorialPlot(id);
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return null;
    }

    public static List<TutorialPlot> getPlots() throws SQLException {
        return listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots_tutorial").executeQuery());
    }

    public static List<TutorialPlot> getPlots(Builder builder) throws SQLException {
        return listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots_tutorial WHERE owner_uuid = ?")
                .setValue(builder.getUUID().toString()).executeQuery());
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
