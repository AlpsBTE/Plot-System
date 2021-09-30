/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.plotsystem.core.config.ConfigPaths;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.ftp.FTPManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PlotManager {

    public static List<Plot> getPlots() throws SQLException {
        return listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots").executeQuery());
    }

    public static List<Plot> getPlots(Status... statuses) throws SQLException {
       return listPlots(DatabaseConnection.createStatement(getStatusQuery("", statuses)).executeQuery());
    }

    public static List<Plot> getPlots(Builder builder) throws SQLException {
        List<Plot> plots = listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE owner_uuid = '" + builder.getUUID() + "' ORDER BY CAST(status AS CHAR)").executeQuery());
        plots.addAll(listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE INSTR(member_uuids, '" + builder.getUUID() + "') > 0 ORDER BY CAST(status AS CHAR)").executeQuery()));
        return plots;
    }

    public static List<Plot> getPlots(Builder builder, Status... statuses) throws SQLException {
        List<Plot> plots = listPlots(DatabaseConnection.createStatement(getStatusQuery( "' AND owner_uuid = '" + builder.getUUID(), statuses)).executeQuery());
        //plots.addAll(listPlots(DatabaseConnection.createStatement(getStatusQuery("id", "' AND INSTR(member_uuids, '" + builder.getUUID() + "') > 0", statuses)).executeQuery()));
        // TODO: Add support for member plots
        return plots;
    }

    public static List<Plot> getPlots(int cityID, Status... statuses) throws SQLException {
        return listPlots(DatabaseConnection.createStatement(getStatusQuery("' AND city_project_id = '" + cityID, statuses)).executeQuery());
    }

    public static List<Plot> getPlots(int cityID, PlotDifficulty plotDifficulty, Status status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE city_project_id = ? AND difficulty_id = ? AND status = ?")
                .setValue(cityID)
                .setValue(plotDifficulty.ordinal() + 1)
                .setValue(status.name())
                .executeQuery());
    }

    private static String getStatusQuery(String additionalQuery, Status... statuses) {
        StringBuilder query = new StringBuilder("SELECT id FROM plotsystem_plots WHERE status = ");

        for(int i = 0; i < statuses.length; i++) {
            query.append("'").append(statuses[i].name()).append(additionalQuery).append("'");
            query.append((i != statuses.length - 1) ? " OR status = " : "");
        }
        return query.toString();
    }

    public static double getMultiplierByDifficulty(PlotDifficulty plotDifficulty) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement("SELECT multiplier FROM plotsystem_difficulties WHERE id = ?")
                .setValue(plotDifficulty.ordinal() + 1).executeQuery();

        if (rs.next()) {
            return rs.getDouble(1);
        }
        return 1;
    }

    public static int getScoreRequirementByDifficulty(PlotDifficulty plotDifficulty) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT score_requirment FROM plotsystem_difficulties WHERE id = ?")
                .setValue(plotDifficulty.ordinal() + 1).executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    private static List<Plot> listPlots(ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        while (rs.next()) {
           plots.add(new Plot(rs.getInt(1)));
        }

        rs.close();
        return plots;
    }

    public static CompletableFuture<Void> savePlotAsSchematic(Plot plot) throws IOException, SQLException, WorldEditException {
        // TODO: MOVE CONVERSION TO SEPERATE METHODS

        Vector terraOrigin, schematicOrigin, plotOrigin;
        Vector schematicMinPoint, schematicMaxPoint;
        Vector plotCenter;

        // Load plot outlines schematic as clipboard
        Clipboard outlinesClipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(plot.getOutlinesSchematic())).read(null);

        // Get player origin coordinates on terra
        terraOrigin = plot.getMinecraftCoordinates();


        // Get plot center
        plotCenter = PlotManager.getPlotCenter(plot);


        // Calculate min and max points of schematic
        int outlinesClipboardCenterX = (int) Math.floor(outlinesClipboard.getRegion().getWidth() / 2d);
        int outlinesClipboardCenterZ = (int) Math.floor(outlinesClipboard.getRegion().getLength() / 2d);

        schematicMinPoint = Vector.toBlockPoint(
                plotCenter.getX() - outlinesClipboardCenterX + ((outlinesClipboard.getRegion().getWidth() % 2 == 0 ? 1 : 0)),
                0,
                plotCenter.getZ() - outlinesClipboardCenterZ + ((outlinesClipboard.getRegion().getLength() % 2 == 0 ? 1 : 0))
        );

        schematicMaxPoint = Vector.toBlockPoint(
                plotCenter.getX() + outlinesClipboardCenterX,
                256,
                plotCenter.getZ() + outlinesClipboardCenterZ
        );


        // Convert terra schematic coordinates into relative plot schematic coordinates
        schematicOrigin = Vector.toBlockPoint(
                Math.floor(terraOrigin.getX()) - Math.floor(outlinesClipboard.getMinimumPoint().getX()),
                Math.floor(terraOrigin.getY()) - Math.floor(outlinesClipboard.getMinimumPoint().getY()),
                Math.floor(terraOrigin.getZ()) - Math.floor(outlinesClipboard.getMinimumPoint().getZ())
        );


        // Add additional plot sizes to relative plot schematic coordinates
        plotOrigin = Vector.toBlockPoint(
                schematicOrigin.getX() + schematicMinPoint.getX(),
                schematicOrigin.getY() + 15 - Math.floor(outlinesClipboard.getRegion().getHeight() / 2f) + (outlinesClipboard.getRegion().getHeight() % 2 == 0 ? 1 : 0),
                schematicOrigin.getZ() + schematicMinPoint.getZ()
        );


        // Load finished plot region as cuboid region
        PlotHandler.loadPlot(plot);
        CuboidRegion region = new CuboidRegion(new BukkitWorld(Bukkit.getWorld("P-" + plot.getID())), schematicMinPoint, schematicMaxPoint);


        // Copy finished plot region to clipboard
        Clipboard cb = new BlockArrayClipboard(region);
        cb.setOrigin(plotOrigin);
        EditSession editSession = PlotSystem.DependencyManager.getWorldEdit().getEditSessionFactory().getEditSession(region.getWorld(), -1);
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, cb, region.getMinimumPoint());
        Operations.complete(forwardExtentCopy);

        // Write finished plot clipboard to schematic file
        File finishedSchematicFile = plot.getFinishedSchematic();

        if (!finishedSchematicFile.exists()) {
            boolean createdDirs = finishedSchematicFile.getParentFile().mkdirs();
            boolean createdFile = finishedSchematicFile.createNewFile();
            if ((!finishedSchematicFile.getParentFile().exists() && !createdDirs) || (!finishedSchematicFile.exists() && !createdFile)) {
                return CompletableFuture.completedFuture(null);
            }
        }

        try(ClipboardWriter writer = ClipboardFormat.SCHEMATIC.getWriter(new FileOutputStream(finishedSchematicFile, false))) {
            writer.write(cb, Objects.requireNonNull(region.getWorld()).getWorldData());
        }

        // Upload to FTP server
        if (plot.getCity().getCountry().getServer().getFTPConfiguration() != null) {
            CompletableFuture.supplyAsync(() -> {
                try {
                    return FTPManager.uploadSchematic(FTPManager.getFTPUrl(plot.getCity().getCountry().getServer(), plot.getCity().getID()), finishedSchematicFile);
                } catch (SQLException | URISyntaxException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
                return null;
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    public static CompletableFuture<double[]> convertTerraToPlotXZ(Plot plot, double[] terraCoords) throws IOException {

        // Load plot outlines schematic as clipboard
        Clipboard outlinesClipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(plot.getOutlinesSchematic())).read(null);

        // Calculate min and max points of schematic
        int outlinesClipboardCenterX = (int) Math.floor(outlinesClipboard.getRegion().getWidth() / 2d);
        int outlinesClipboardCenterZ = (int) Math.floor(outlinesClipboard.getRegion().getLength() / 2d);

        Vector schematicMinPoint = Vector.toBlockPoint(
                PlotManager.getPlotCenter(plot).getX() - outlinesClipboardCenterX + ((outlinesClipboard.getRegion().getWidth() % 2 == 0 ? 1 : 0)),
                0,
                PlotManager.getPlotCenter(plot).getZ() - outlinesClipboardCenterZ + ((outlinesClipboard.getRegion().getLength() % 2 == 0 ? 1 : 0))
        );

        Vector schematicMaxPoint = Vector.toBlockPoint(
                PlotManager.getPlotCenter(plot).getX() + outlinesClipboardCenterX,
                256,
                PlotManager.getPlotCenter(plot).getZ() + outlinesClipboardCenterZ
        );

        // Convert terra schematic coordinates into relative plot schematic coordinates
        double[] schematicCoords = {
                terraCoords[0] - outlinesClipboard.getMinimumPoint().getX(),
                terraCoords[1] - outlinesClipboard.getMinimumPoint().getZ()
        };

        // Add additional plot sizes to relative plot schematic coordinates
        double[] plotCoords = {
                schematicCoords[0] + schematicMinPoint.getX(),
                schematicCoords[1] + schematicMinPoint.getZ()
        };

        // Return coordinates if they are in the schematic plot region
        if(new CuboidRegion(schematicMinPoint, schematicMaxPoint).contains(new Vector((int)plotCoords[0], 15, (int)plotCoords[1]))) {
            return CompletableFuture.completedFuture(plotCoords);
        }

       return null;
    }

    public static void checkPlotsForLastActivity() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), () -> {
            try {
                List<Plot> plots = getPlots(Status.unfinished);
                FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getConfig();
                long millisInDays = config.getLong(ConfigPaths.INACTIVITY_INTERVAL) * 24 * 60 * 60 * 1000; // Remove all plots which have no activity for the last x days

                for(Plot plot : plots) {
                    if(plot.getLastActivity() != null && plot.getLastActivity().getTime() < (new Date().getTime() - millisInDays)) {
                        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                            try {
                                PlotHandler.abandonPlot(plot);
                                Bukkit.getLogger().log(Level.INFO, "Abandoned plot #" + plot.getID() + " due to inactivity!");
                            } catch (Exception ex) {
                                Bukkit.getLogger().log(Level.SEVERE, "A unknown error occurred!", ex);
                            }
                        });
                    }
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }, 0L, 20 * 60 * 60); // Check every hour
    }

    public static void syncPlotSchematicFiles() {
        FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getConfig();
        if (config.getBoolean(ConfigPaths.SYNC_FTP_FILES_ENABLED)) {
            long interval = config.getLong(ConfigPaths.SYNC_FTP_FILES_INTERVAL);

            Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), () -> {
                CityProject.getCityProjects(false).forEach(c -> {
                    try {
                        if (c.getCountry().getServer().getFTPConfiguration() != null) {
                            List<Plot> plots = PlotManager.getPlots(c.getID(), Status.unclaimed);
                            plots.forEach(Plot::getOutlinesSchematic);
                        }
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.INFO, "A SQL error occurred!", ex);
                    }
                });
            }, 0L, 20 * interval);
        }
    }

    public static Plot getPlotByWorld(World plotWorld) throws SQLException {
        return new Plot(Integer.parseInt(plotWorld.getName().substring(2)));
    }

    public static boolean plotExists(int ID) {
        String worldName = "P-" + ID;
        return (PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().getMVWorld(worldName) != null) || PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().getUnloadedWorlds().contains(worldName);
    }

    public static boolean plotExists(int ID, boolean system) {
        if (system) {
            try (ResultSet rs = DatabaseConnection.createStatement("SELECT COUNT(id) FROM plotsystem_plots WHERE id = ?")
                    .setValue(ID).executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return true;
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        } else return plotExists(ID);
        return false;
    }

    // TODO: Move this function to Builder.java or rework in V3.0
    public static boolean hasPlotDifficultyScoreRequirement(Builder builder, PlotDifficulty plotDifficulty) throws SQLException {
        int playerScore = builder.getScore(), scoreRequirement = getScoreRequirementByDifficulty(plotDifficulty);
        return playerScore >= scoreRequirement;
    }

    public static CompletableFuture<PlotDifficulty> getPlotDifficultyForBuilder(int cityID, Builder builder) throws SQLException {
        // Check if plot difficulties are available
        boolean easyHasPlots = false, mediumHasPlots = false, hardHasPlots = false;
        if(PlotManager.getPlots(cityID, PlotDifficulty.EASY, Status.unclaimed).size() != 0) easyHasPlots = true;
        if(PlotManager.getPlots(cityID, PlotDifficulty.MEDIUM, Status.unclaimed).size() != 0) mediumHasPlots = true;
        if(PlotManager.getPlots(cityID, PlotDifficulty.HARD, Status.unclaimed).size() != 0) hardHasPlots = true;

        if(hardHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.HARD)) { // Return hard
            return CompletableFuture.completedFuture(PlotDifficulty.HARD);
        } else if (mediumHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.MEDIUM)) { // Return medium
            return CompletableFuture.completedFuture(PlotDifficulty.MEDIUM);
        } else if (easyHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.EASY)) { // Return easy
            return CompletableFuture.completedFuture(PlotDifficulty.EASY);
        } else if (mediumHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.HARD)) { // If hard has no plots return medium
            return CompletableFuture.completedFuture(PlotDifficulty.EASY);
        } else if (easyHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.MEDIUM)) { // If medium has no plots return easy
            return CompletableFuture.completedFuture(PlotDifficulty.MEDIUM);
        } else if (!PlotSystem.getPlugin().getConfigManager().getConfig().getBoolean(ConfigPaths.ENABLE_SCORE_REQUIREMENT)) { // If score requirement is disabled get plot from any available difficulty
            if (easyHasPlots) {
                return CompletableFuture.completedFuture(PlotDifficulty.EASY);
            } else if (mediumHasPlots) {
                return CompletableFuture.completedFuture(PlotDifficulty.MEDIUM);
            }
        }
        return CompletableFuture.completedFuture(PlotDifficulty.HARD); // If nothing is available return hard (plot availability will be checked later additionally)
    }

    public static boolean isPlotWorld(World world) {
        return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().isMVWorld(world) && world.getName().startsWith("P-");
    }

    public static int getPlotSize(Plot plot) {
       if(plot.getPlotRegion() != null) {
           if(plot.getPlotRegion().contains(150, 15, 150)) {
               return 150;
           } else {
               return 100;
           }
       } else {
        return 150;
       }
    }

    public static Vector getPlotCenter(Plot plot) {
        return Vector.toBlockPoint(
                getPlotSize(plot) / 2d + 0.5,
                5,
                getPlotSize(plot) / 2d + 0.5
        );
    }

    public static String getWorldGuardConfigPath(String worldName) {
        return Bukkit.getPluginManager().getPlugin("WorldGuard").getDataFolder() + "/worlds/" + worldName;
    }

    public static String getMultiverseInventoriesConfigPath(String worldName) {
        return Bukkit.getPluginManager().getPlugin("Multiverse-Inventories").getDataFolder() + "/worlds/" + worldName;
    }

    public static String getDefaultSchematicPath() {
        return Paths.get(PlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "schematics") + File.separator;
    }
}