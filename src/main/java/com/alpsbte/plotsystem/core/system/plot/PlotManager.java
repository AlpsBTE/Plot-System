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

import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.fastasyncworldedit.core.FaweAPI;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import com.github.fierioziy.particlenativeapi.plugin.ParticleNativePlugin;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.FTPManager;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PlotManager {
    // TODO: Start -> Move to another class
    public static int CACHE_UPDATE_TICKS = 20*60;

    public static int time;

    public static HashMap<UUID, List<Plot>> cachedInProgressPlots = new HashMap<>();

    private static boolean ParticleAPIEnabled = false;
    private static Particles_1_8 particles;



    public static void startTimer(){
        if(PlotSystem.DependencyManager.isParticleNativeAPIEnabled())
            loadParticleNativeAPI();

        Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
            if(PlotSystem.DependencyManager.isParticleNativeAPIEnabled())
                loadParticleNativeAPI();
        }, 20*10L);


        Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), PlotManager::tick, 0L, 0L);
    }

    public static void tick(){
        time++;

        if(time%CACHE_UPDATE_TICKS == 0)
            clearCache();


        if(time%10 == 0)
            showOutlines();
    }

    public static void clearCache(){
        cachedInProgressPlots.clear();
    }

    public static void clearCache(UUID builderUUID) {
        cachedInProgressPlots.remove(builderUUID);
    }

    public static void loadParticleNativeAPI(){
        ParticleAPIEnabled = PlotSystem.DependencyManager.isParticleNativeAPIEnabled();

        // get API
        ParticleNativeAPI api = ParticleNativePlugin.getAPI();

        // choose particles lists you want to use
        particles = api.getParticles_1_8();
    }



    public static List<Plot> getCachedInProgressPlots(Builder builder){
        if(!cachedInProgressPlots.containsKey(builder.getUUID())) {
            try {
                cachedInProgressPlots.put(builder.getUUID(), getPlots(builder, Status.unfinished));
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                return new ArrayList<>();
            }
        }

        return cachedInProgressPlots.get(builder.getUUID());
    }
    // TODO: End -> Move to another class

    public static List<Plot> getPlots() throws SQLException {
        return listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots").executeQuery());
    }

    public static List<Plot> getPlots(Status... statuses) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement(getStatusQuery("", statuses)).executeQuery();
        return listPlots(rs);
    }

    public static List<Plot> getPlots(Builder builder) throws SQLException {
        List<Plot> plots = listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE owner_uuid = '" + builder.getUUID() + "' ORDER BY CAST(status AS CHAR)").executeQuery());
        plots.addAll(listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE INSTR(member_uuids, '" + builder.getUUID() + "') > 0 ORDER BY CAST(status AS CHAR)").executeQuery()));
        return plots;
    }

    public static List<Plot> getPlots(Builder builder, Status... statuses) throws SQLException {
        List<Plot> plots = listPlots(DatabaseConnection.createStatement(getStatusQuery(" AND owner_uuid = '" + builder.getUUID().toString() + "'", statuses)).executeQuery());
        plots.addAll(getPlotsAsMember(builder, statuses));
        return plots;
    }

    public static List<Plot> getPlots(List<Country> countries, Status status) throws SQLException {
        List<CityProject> cities = new ArrayList<>();
        countries.forEach(c -> cities.addAll(c.getCityProjects()));
        return getPlots(cities, status);
    }

    // Temporary fix to receive plots of builder as member
    private static List<Plot> getPlotsAsMember(Builder builder, Status... status) throws SQLException {
        List<Plot> plots = new ArrayList<>();
        for (Status stat : status) {
            plots.addAll(listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE status = '" + stat.name() + "' AND INSTR(member_uuids, '" + builder.getUUID() + "') > 0 ORDER BY CAST(status AS CHAR)").executeQuery()));
        }
        return plots;
    }

    public static List<Plot> getPlots(int cityID, Status... statuses) throws SQLException {
        return listPlots(DatabaseConnection.createStatement(getStatusQuery(" AND city_project_id = '" + cityID + "'", statuses)).executeQuery());
    }

    public static List<Plot> getPlots(List<CityProject> cities, Status... statuses) throws SQLException {
        if(cities.size() == 0) {
            return new ArrayList<>();
        }
        StringBuilder query = new StringBuilder(" AND (city_project_id = ");

        for (int i = 0; i < cities.size(); i++) {
            query.append(cities.get(i).getID());
            query.append((i != cities.size() - 1) ? " OR city_project_id = " : ")");
        }

        return listPlots(DatabaseConnection.createStatement(getStatusQuery(query.toString(), statuses)).executeQuery());
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

        for (int i = 0; i < statuses.length; i++) {
            query.append("'").append(statuses[i].name()).append("'").append(additionalQuery);
            query.append((i != statuses.length - 1) ? " OR status = " : "");
        }
        return query.toString();
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

    private static List<Plot> listPlots(ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        while (rs.next()) {
            plots.add(new Plot(rs.getInt(1)));
        }

        DatabaseConnection.closeResultSet(rs);
        return plots;
    }

    public static CuboidRegion getPlotAsRegion(Plot plot) throws IOException {
        Clipboard clipboard = FaweAPI.load(plot.getOutlinesSchematic());
        if (clipboard != null) {
            if (plot.getVersion() >= 3) {
                return new CuboidRegion(
                        clipboard.getDimensions().getMinimum(BlockVector3.at(clipboard.getMinimumPoint().getX(),plot.getWorld().getPlotHeight(),clipboard.getMinimumPoint().getZ())),
                        clipboard.getDimensions().getMaximum(BlockVector3.at(clipboard.getMaximumPoint().getX(),PlotWorld.MAX_WORLD_HEIGHT,clipboard.getMaximumPoint().getZ())));
            } else {
                BlockVector3 plotCenter = plot.getCenter();

                // Calculate min and max points of schematic
                int regionCenterModX = clipboard.getRegion().getWidth() % 2 == 0 ? 1 : 0;
                int regionCenterModZ = clipboard.getRegion().getLength() % 2 == 0 ? 1 : 0;
                int outlinesClipboardCenterX = (int) Math.floor(clipboard.getRegion().getWidth() / 2d);
                int outlinesClipboardCenterZ = (int) Math.floor(clipboard.getRegion().getLength() / 2d);

                BlockVector3 schematicMinPoint = BlockVector3.at(
                        plotCenter.getX() - (outlinesClipboardCenterX - regionCenterModX),
                        PlotWorld.MIN_WORLD_HEIGHT,
                        plotCenter.getZ() - (outlinesClipboardCenterZ - regionCenterModZ)
                );

                BlockVector3 schematicMaxPoint = BlockVector3.at(
                        plotCenter.getX() + outlinesClipboardCenterX,
                        PlotWorld.MAX_WORLD_HEIGHT,
                        plotCenter.getZ() + outlinesClipboardCenterZ
                );

                return new CuboidRegion(schematicMinPoint, schematicMaxPoint);
            }
        }
        return null;
    }

    // TODO: MOVE CONVERSION TO SEPARATE METHODS
    public static boolean savePlotAsSchematic(Plot plot) throws IOException, SQLException, WorldEditException {
        Clipboard clipboard = FaweAPI.load(plot.getOutlinesSchematic());
        if (clipboard != null) {
            CuboidRegion cuboidRegion = getPlotAsRegion(plot);

            if (cuboidRegion != null) {
                BlockVector3 plotCenter = plot.getCenter();

                // Get plot outline
                List<BlockVector2> plotOutlines = plot.getOutline();

                // Load finished plot region as cuboid region
                if (plot.getWorld().loadWorld()) {
                    com.sk89q.worldedit.world.World world = new BukkitWorld(plot.getWorld().getBukkitWorld());
                    Polygonal2DRegion region = new Polygonal2DRegion(world, plotOutlines, cuboidRegion.getMinimumPoint().getBlockY(), cuboidRegion.getMaximumPoint().getBlockY());

                    // Copy and write finished plot clipboard to schematic file
                    File finishedSchematicFile = plot.getFinishedSchematic();

                    if (!finishedSchematicFile.exists()) {
                        boolean createdDirs = finishedSchematicFile.getParentFile().mkdirs();
                        boolean createdFile = finishedSchematicFile.createNewFile();
                        if ((!finishedSchematicFile.getParentFile().exists() && !createdDirs) || (!finishedSchematicFile.exists() && !createdFile)) {
                            return false;
                        }
                    }

                    Clipboard cb = new BlockArrayClipboard(region);
                    if (plot.getVersion() >= 3) {
                        cb.setOrigin(BlockVector3.at(Math.floor(plotCenter.getX()), cuboidRegion.getMinimumY(), Math.floor(plotCenter.getZ())));
                    } else {
                        BlockVector3 terraCenter = plot.getMinecraftCoordinates();
                        plotCenter = BlockVector3.at(
                            Math.floor(terraCenter.getX()) - Math.floor(clipboard.getMinimumPoint().getX()) + cuboidRegion.getMinimumPoint().getX(),
                            Math.floor(terraCenter.getY()) - Math.floor(clipboard.getMinimumPoint().getY()) + cuboidRegion.getMinimumPoint().getY(),
                            Math.floor(terraCenter.getZ()) - Math.floor(clipboard.getMinimumPoint().getZ()) + cuboidRegion.getMinimumPoint().getZ()
                        );
                        cb.setOrigin(plotCenter);
                    }

                    EditSession editSession = PlotSystem.DependencyManager.getWorldEdit().getEditSessionFactory().getEditSession(region.getWorld(), -1);
                    ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, cb, region.getMinimumPoint());
                    Operations.complete(forwardExtentCopy);

                    try(ClipboardWriter writer = ClipboardFormats.findByFile(finishedSchematicFile).getWriter(new FileOutputStream(finishedSchematicFile, false))) {
                        writer.write(cb);
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

                    // If plot was created in a void world, copy the result to the city world
                    if (plot.getPlotType() != PlotType.CITY_INSPIRATION_MODE && plot.getVersion() >= 3) {
                        AbstractPlotGenerator.pasteSchematic(null, plot.getFinishedSchematic(), new CityPlotWorld(plot), false);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static CompletableFuture<double[]> convertTerraToPlotXZ(Plot plot, double[] terraCoords) throws IOException {
        // Load plot outlines schematic as clipboard
        Clipboard clipboard = FaweAPI.load(plot.getOutlinesSchematic());

        if (clipboard != null) {
            // Calculate min and max points of schematic
            CuboidRegion plotRegion = getPlotAsRegion(plot);

            if (plotRegion != null) {
                // Convert terra schematic coordinates into relative plot schematic coordinates
                double[] schematicCoords = {
                        terraCoords[0] - clipboard.getMinimumPoint().getX(),
                        terraCoords[1] - clipboard.getMinimumPoint().getZ()
                };

                // Add additional plot sizes to relative plot schematic coordinates
                double[] plotCoords = {
                        schematicCoords[0] + plotRegion.getMinimumPoint().getX(),
                        schematicCoords[1] + plotRegion.getMinimumPoint().getZ()
                };

                // Return coordinates if they are in the schematic plot region
                ProtectedRegion protectedPlotRegion = plot.getWorld().getProtectedRegion() != null ? plot.getWorld().getProtectedRegion() : plot.getWorld().getProtectedBuildRegion();
                if (protectedPlotRegion.contains(BlockVector3.at((int) plotCoords[0], plot.getWorld().getPlotHeightCentered(), (int) plotCoords[1]))) {
                    return CompletableFuture.completedFuture(plotCoords);
                }
            }
        }

        return null;
    }

    public static void checkPlotsForLastActivity() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), () -> {
            try {
                List<Plot> plots = getPlots(Status.unfinished);
                FileConfiguration config = PlotSystem.getPlugin().getConfig();
                long millisInDays = config.getLong(ConfigPaths.INACTIVITY_INTERVAL) * 24 * 60 * 60 * 1000; // Remove all plots which have no activity for the last x days

                for (Plot plot : plots) {
                    if (plot.getLastActivity() != null && plot.getLastActivity().getTime() < (new Date().getTime() - millisInDays)) {
                        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                            if (PlotHandler.abandonPlot(plot)) {
                                Bukkit.getLogger().log(Level.INFO, "Abandoned plot #" + plot.getID() + " due to inactivity!");
                            } else
                                Bukkit.getLogger().log(Level.WARNING, "An error occurred while abandoning plot #" + plot.getID() + " due to inactivity!");
                        });
                    }
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }, 0L, 20 * 60 * 60); // Check every hour
    }

    public static void syncPlotSchematicFiles() {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        if (config.getBoolean(ConfigPaths.SYNC_FTP_FILES_ENABLED)) {
            long interval = config.getLong(ConfigPaths.SYNC_FTP_FILES_INTERVAL);

            Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), () -> CityProject.getCityProjects(false).forEach(c -> {
                try {
                    if (c.getCountry().getServer().getFTPConfiguration() != null) {
                        List<Plot> plots = PlotManager.getPlots(c.getID(), Status.unclaimed);
                        plots.forEach(Plot::getOutlinesSchematic);
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.INFO, "A SQL error occurred!", ex);
                }
            }), 0L, 20 * interval);
        }
    }

    /** Returns the plot that the player is currently standing on or next to.
     *  If he is standing in a single plot world it returns the plot of this world.
     *  If he is standing in a multi plot world it returns the closest plot of all unfinished plots of this city
     *
     * @return the current plot of the player
     */
    public static Plot getCurrentPlot(Builder builder, Status... statuses) throws SQLException {
        if (builder.isOnline()) {
            String worldName = builder.getPlayer().getWorld().getName();

            if(PlotWorld.isOnePlotWorld(worldName))
                return new Plot(Integer.parseInt(worldName.substring(2)));
            else if (CityPlotWorld.isCityPlotWorld(worldName)) {
                int cityID = Integer.parseInt(worldName.substring(2));
                List<Plot> plots = getPlots(cityID, statuses);

                if(plots.size() == 0)
                    return getPlots(builder).get(0);
                if(plots.size() == 1)
                    return plots.get(0);

                // Find the plot in the city world that is closest to the player
                Location playerLoc = builder.getPlayer().getLocation().clone();
                BlockVector3 playerVector = BlockVector3.at(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ());

                double distance = 100000000;
                Plot chosenPlot = plots.get(0);
                for(Plot plot : plots)
                    if (plot.getPlotType() == PlotType.CITY_INSPIRATION_MODE && plot.getCenter().add(0,playerVector.getY(),0).distance(playerVector) < distance) {
                        distance = plot.getCenter().distance(playerVector);
                        chosenPlot = plot;
                    }

                return chosenPlot;
            }
        }
        return null;
    }

    public static boolean isPlayerOnPlot(Plot plot, Player player) {
        if (plot.getWorld().isWorldLoaded() && plot.getWorld().getBukkitWorld().getPlayers().contains(player)) {
            Location playerLoc = player.getLocation();
            return plot.getWorld().getProtectedRegion().contains(BlockVector3.at(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()));
        }
        return false;
    }

    public static boolean plotExists(int ID) {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT COUNT(id) FROM plotsystem_plots WHERE id = ?")
                .setValue(ID).executeQuery()) {

            if (rs.next() && rs.getInt(1) > 0) {
                DatabaseConnection.closeResultSet(rs);
                return true;
            }

            DatabaseConnection.closeResultSet(rs);
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
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
        if (PlotManager.getPlots(cityID, PlotDifficulty.EASY, Status.unclaimed).size() != 0) easyHasPlots = true;
        if (PlotManager.getPlots(cityID, PlotDifficulty.MEDIUM, Status.unclaimed).size() != 0) mediumHasPlots = true;
        if (PlotManager.getPlots(cityID, PlotDifficulty.HARD, Status.unclaimed).size() != 0) hardHasPlots = true;

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

    public static boolean isPlotWorld(World world) {
        return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().isMVWorld(world) && (OnePlotWorld.isOnePlotWorld(world.getName()) || CityPlotWorld.isCityPlotWorld(world.getName()));
    }


    public static String getDefaultSchematicPath() {
        return Paths.get(PlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "schematics") + File.separator;
    }

    public static void showOutlines(){
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Builder builder = Builder.byUUID(player.getUniqueId());

                List<Plot> plots = getCachedInProgressPlots(builder);
                BlockVector2 playerPos2D = BlockVector2.at(player.getLocation().getX(), player.getLocation().getZ());

                if(plots.size() == 0)
                    continue;

                for(Plot plot : plots){
                    if(!plot.getWorld().getWorldName().equals(player.getWorld().getName()))
                        continue;

                    if(!plot.getPlotOwner().getPlotTypeSetting().hasEnvironment() || plot.getVersion() <= 2)
                        continue;

                    List<BlockVector2> points = plot.getBlockOutline();

                    for(BlockVector2 point : points) if(point.distanceSq(playerPos2D) < 50*50)
                        if(!ParticleAPIEnabled)
                            player.spawnParticle(Particle.FLAME, point.getX(), player.getLocation().getY() + 1, point.getZ(), 1, 0.0 ,0.0,0.0, 0);
                        else{
                            Location loc = new Location(player.getWorld(), point.getX(), player.getLocation().getY() + 1, point.getZ());
                            // create a particle packet
                            Object packet = particles.FLAME().packet(true, loc);

                            // send this packet to player
                            particles.sendPacket(player, packet);
                        }
                }
            }

        } catch (SQLException | IOException ex) {
            Bukkit.getLogger().log(Level.INFO, "A SQL error occurred!", ex);
        }
    }

}