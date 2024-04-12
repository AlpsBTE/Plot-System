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

package com.alpsbte.plotsystem.core.system.plot.utils;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Server;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.ShortLink;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.FTPManager;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.util.collection.BlockVector3Set;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import com.github.fierioziy.particlenativeapi.plugin.ParticleNativePlugin;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

import static com.alpsbte.plotsystem.core.system.plot.world.PlotWorld.MAX_WORLD_HEIGHT;
import static com.alpsbte.plotsystem.core.system.plot.world.PlotWorld.MIN_WORLD_HEIGHT;
import static net.md_5.bungee.api.ChatColor.*;

public final class PlotUtils {

    /** Returns the plot that the player is currently standing on or next to.
     *  If he is standing in a single plot world it returns the plot of this world.
     *  If he is standing in a multi plot world it returns the closest plot of all unfinished plots of this city
     *
     * @return the current plot of the player
     */
    @Nullable
    public static AbstractPlot getCurrentPlot(Builder builder, Status... statuses) throws SQLException {
        if (builder.isOnline()) {
            String worldName = builder.getPlayer().getWorld().getName();

            if(PlotWorld.isOnePlotWorld(worldName)) {
                int id = Integer.parseInt(worldName.substring(2));
                AbstractPlot plot = worldName.toLowerCase(Locale.ROOT).startsWith("p-") ? new Plot(id) : new TutorialPlot(id);
                if (statuses == null) return plot;
                for (Status status : statuses) if (status == plot.getStatus()) return plot;
                return null;
            } else if (CityPlotWorld.isCityPlotWorld(worldName)) {
                int cityID = Integer.parseInt(worldName.substring(2));
                List<Plot> plots = Plot.getPlots(cityID, statuses);

                if(plots.isEmpty()) return null;
                if(plots.size() == 1) return plots.get(0);

                // Find the plot in the city world that is closest to the player
                Location playerLoc = builder.getPlayer().getLocation().clone();
                Vector3 playerVector = Vector3.at(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ());

                double distance = 100000000;
                Plot chosenPlot = plots.get(0);
                for(Plot plot : plots)
                    if (plot.getPlotType() == PlotType.CITY_INSPIRATION_MODE && plot.getCenter().withY((int) playerVector.getY()).distance(playerVector.toBlockPoint()) < distance) {
                        distance = plot.getCenter().distance(playerVector.toBlockPoint());
                        chosenPlot = plot;
                    }

                return chosenPlot;
            }
        }
        return null;
    }

    public static boolean isPlayerOnPlot(AbstractPlot plot, Player player) throws SQLException {
        if (plot.getWorld().isWorldLoaded() && plot.getWorld().getBukkitWorld().getPlayers().contains(player)) {
            Location playerLoc = player.getLocation();
            return plot.getWorld().getProtectedRegion().contains(Vector3.toBlockPoint(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()));
        }
        return false;
    }

    public static CuboidRegion getPlotAsRegion(AbstractPlot plot) throws IOException, SQLException {
        Clipboard clipboard = FaweAPI.load(plot.getOutlinesSchematic());
        if (clipboard != null) {
            if (plot.getVersion() >= 3) {
                return new CuboidRegion(
                        clipboard.getMinimumPoint().withY(plot.getWorld().getPlotHeight()),
                        clipboard.getMaximumPoint().withY(PlotWorld.MAX_WORLD_HEIGHT));
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

    public static boolean isPlotWorld(World world) {
        return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().isMVWorld(world) && (OnePlotWorld.isOnePlotWorld(world.getName()) || CityPlotWorld.isCityPlotWorld(world.getName()));
    }


    public static String getDefaultSchematicPath() {
        return Paths.get(PlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "schematics") + File.separator;
    }

    public static boolean savePlotAsSchematic(Plot plot) throws IOException, SQLException, WorldEditException {
        Clipboard clipboard = FaweAPI.load(plot.getOutlinesSchematic());
        if (clipboard != null) {
            CuboidRegion cuboidRegion = getPlotAsRegion(plot);
            Bukkit.getLogger().log(Level.INFO, "Getting Plot region for saving from: " + cuboidRegion);

            if (cuboidRegion != null) {
                BlockVector3 plotCenter = plot.getCenter();

                // Get plot outline
                List<BlockVector2> plotOutlines = plot.getShiftedOutline();

                // Shift schematic region to the force (0, 0) paste
                cuboidRegion.shift(BlockVector3.at(-plotCenter.getX(), 0, -plotCenter.getZ()));

                Bukkit.getLogger().log(Level.INFO, "Shifted Plot region for saving to: " + cuboidRegion);

                // Load finished plot region as cuboid region
                if (plot.getWorld().loadWorld()) {
                    com.sk89q.worldedit.world.World world = new BukkitWorld(plot.getWorld().getBukkitWorld());
                    Polygonal2DRegion region = new Polygonal2DRegion(world, plotOutlines, cuboidRegion.getMinimumPoint().getBlockY(), cuboidRegion.getMaximumPoint().getBlockY());

                    Bukkit.getLogger().log(Level.INFO, "Saving Schematic at region: " + region);

                    // Copy and write finished plot clipboard to schematic file
                    File finishedSchematicFile = Paths.get(PlotUtils.getDefaultSchematicPath(),
                            String.valueOf(plot.getCity().getCountry().getServer().getID()),
                            "finishedSchematics", String.valueOf(plot.getCity().getID()), plot.getID() + ".schem").toFile();

                    if (!finishedSchematicFile.exists()) {
                        boolean createdDirs = finishedSchematicFile.getParentFile().mkdirs();
                        boolean createdFile = finishedSchematicFile.createNewFile();
                        if ((!finishedSchematicFile.getParentFile().exists() && !createdDirs) || (!finishedSchematicFile.exists() && !createdFile)) {
                            return false;
                        }
                    }

                    Clipboard cb = new BlockArrayClipboard(region);
                    if (plot.getVersion() >= 3) {
                        Bukkit.getLogger().log(Level.INFO, "Setting origin schems at: " + BlockVector3.at(0, cuboidRegion.getMinimumY(), (double) 0));
                        cb.setOrigin(BlockVector3.at(0, cuboidRegion.getMinimumY(), (double) 0));
                    } else {
                        Bukkit.getLogger().log(Level.SEVERE, "This should not happen, whyyy");
                        BlockVector3 terraCenter = plot.getMinecraftCoordinates();
                        plotCenter = BlockVector3.at(
                                (double) terraCenter.getX() - (double) clipboard.getMinimumPoint().getX() + cuboidRegion.getMinimumPoint().getX(),
                                (double) terraCenter.getY() - (double) clipboard.getMinimumPoint().getY() + cuboidRegion.getMinimumPoint().getY(),
                                (double) terraCenter.getZ() - (double) clipboard.getMinimumPoint().getZ() + cuboidRegion.getMinimumPoint().getZ()
                        );
                        cb.setOrigin(plotCenter);
                    }

                    EditSession editSession = PlotSystem.DependencyManager.getWorldEdit().getEditSessionFactory().getEditSession(region.getWorld(), -1);
                    ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, cb, region.getMinimumPoint());
                    Operations.complete(forwardExtentCopy);

                    try(ClipboardWriter writer = Objects.requireNonNull(ClipboardFormats.findByFile(finishedSchematicFile)).getWriter(new FileOutputStream(finishedSchematicFile, false))) {
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
                        AbstractPlotGenerator.pasteSchematic(null, plot.getCompletedSchematic(), new CityPlotWorld(plot), false);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static CompletableFuture<double[]> convertTerraToPlotXZ(AbstractPlot plot, double[] terraCoords) throws IOException, SQLException {
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
                        schematicCoords[0] + plotRegion.getMinimumPoint().getX() - plot.getCenter().getX(),
                        schematicCoords[1] + plotRegion.getMinimumPoint().getZ() - plot.getCenter().getZ()
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

    public static BlockVector2 getCenterFromOutline(List<BlockVector2> points) {
        int minX = points.get(0).getX();
        int minZ = points.get(0).getZ();
        int maxX = points.get(0).getX();
        int maxZ = points.get(0).getZ();

        for (BlockVector2 v : points) {
            int x = v.getX();
            int z = v.getZ();
            if (x < minX) minX = x;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (z > maxZ) maxZ = z;
        }
        Vector3 center = BlockVector2.at(minX, minZ).add(BlockVector2.at(maxX, maxZ)).toVector3().divide(2);
        return BlockVector2.at(center.getX(), center.getZ());
    }

    public static void checkPlotsForLastActivity() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), () -> {
            try {
                List<Plot> plots = Plot.getPlots(Status.unfinished);
                FileConfiguration config = PlotSystem.getPlugin().getConfig();
                long millisInDays = config.getLong(ConfigPaths.INACTIVITY_INTERVAL) * 24 * 60 * 60 * 1000; // Remove all plots which have no activity for the last x days

                for (Plot plot : plots) {
                    if (plot.getLastActivity() != null && plot.getLastActivity().getTime() < (new Date().getTime() - millisInDays)) {
                        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                            if (Actions.abandonPlot(plot)) {
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
        if (config.getBoolean(ConfigPaths.SYNC_FTP_FILES_ENABLE)) {
            long interval = config.getLong(ConfigPaths.SYNC_FTP_FILES_INTERVAL);

            Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), () -> CityProject.getCityProjects(false).forEach(c -> {
                try {
                    if (c.getCountry().getServer().getFTPConfiguration() != null) {
                        List<Plot> plots = Plot.getPlots(c.getID(), Status.unclaimed);
                        plots.forEach(Plot::getOutlinesSchematic);
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.INFO, "A SQL error occurred!", ex);
                }
            }), 0L, 20 * interval);
        }
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

    public static final class Actions {
        public static void submitPlot(Plot plot) throws SQLException {
            plot.setStatus(Status.unreviewed);

            if(plot.getWorld().isWorldLoaded()) {
                for(Player player : plot.getWorld() instanceof OnePlotWorld ? plot.getWorld().getBukkitWorld().getPlayers() : ((CityPlotWorld) plot.getWorld()).getPlayersOnPlot()) {
                    player.teleport(Utils.getSpawnLocation());
                }
            }

            plot.getPermissions().removeBuilderPerms(plot.getPlotOwner().getUUID()).save();
            if (!plot.getPlotMembers().isEmpty()) {
                for (Builder builder : plot.getPlotMembers()) {
                    plot.getPermissions().removeBuilderPerms(builder.getUUID());
                }
            }
        }

        public static void undoSubmit(Plot plot) throws SQLException {
            plot.setStatus(Status.unfinished);

            plot.getPermissions().addBuilderPerms(plot.getPlotOwner().getUUID()).save();
            if (!plot.getPlotMembers().isEmpty()) {
                for (Builder builder : plot.getPlotMembers()) {
                    plot.getPermissions().addBuilderPerms(builder.getUUID());
                }
            }
        }

        public static boolean abandonPlot(AbstractPlot plot) {
            try {
                if (plot.getWorld() instanceof OnePlotWorld) {
                    if (plot.getWorld().isWorldGenerated()) {
                        if (plot.getWorld().isWorldLoaded()) {
                            for(Player player : plot.getWorld().getBukkitWorld().getPlayers()) {
                                player.teleport(Utils.getSpawnLocation());
                            }
                        }
                        if (!plot.getWorld().deleteWorld()) Bukkit.getLogger().log(Level.WARNING, "Could not delete plot world " + plot.getWorld().getWorldName() + "!");
                    }
                } else if (!(plot instanceof TutorialPlot)) {
                    RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();

                    if (plot.getWorld().loadWorld()) {
                        CityPlotWorld world = plot.getWorld();
                        List<Player> playersToTeleport = new ArrayList<>(world.getPlayersOnPlot());

                        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world.getBukkitWorld()));
                        if (regionManager != null) {
                            for (Builder builder : ((Plot) plot).getPlotMembers()) {
                                ((Plot) plot).removePlotMember(builder);
                            }

                            if (regionManager.hasRegion(world.getRegionName())) regionManager.removeRegion(world.getRegionName());
                            if (regionManager.hasRegion(world.getRegionName() + "-1")) regionManager.removeRegion(world.getRegionName() + "-1");

                            AbstractPlotGenerator.pasteSchematic(null, plot.getOutlinesSchematic(), world, true);
                        } else Bukkit.getLogger().log(Level.WARNING, "Region Manager is null!");

                        playersToTeleport.forEach(p -> p.teleport(Utils.getSpawnLocation()));
                        if (plot.getWorld().isWorldLoaded()) plot.getWorld().unloadWorld(false);
                    }
                }
            } catch (SQLException | IOException | WorldEditException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to abandon plot with the ID " + plot.getID() + "!", ex);
                return false;
            }

            try {
                CompletableFuture.runAsync(() -> {
                    try {
                        if (plot.getPlotType() != PlotType.TUTORIAL) {
                            Plot dPlot = (Plot) plot;
                            if (dPlot.isReviewed()) {
                                DatabaseConnection.createStatement("UPDATE plotsystem_plots SET review_id = DEFAULT(review_id) WHERE id = ?")
                                        .setValue(plot.getID()).executeUpdate();

                                DatabaseConnection.createStatement("DELETE FROM plotsystem_reviews WHERE id = ?")
                                        .setValue(dPlot.getReview().getReviewID()).executeUpdate();
                            }

                            for (Builder builder : dPlot.getPlotMembers()) dPlot.removePlotMember(builder);

                            if (plot.getPlotOwner() != null) {
                                Cache.clearCache(plot.getPlotOwner().getUUID());
                                plot.getPlotOwner().removePlot(dPlot.getSlot());
                            }

                            dPlot.setPlotOwner(null);
                            dPlot.setLastActivity(true);
                            dPlot.setTotalScore(-1);
                            dPlot.setStatus(Status.unclaimed);
                            dPlot.setPlotType(PlotType.LOCAL_INSPIRATION_MODE);
                        }
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        throw new CompletionException(ex);
                    }
                }).join();
            } catch (CompletionException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to abandon plot with the ID " + plot.getID() + "!", ex);
                return false;
            }
            return true;
        }

        public static boolean deletePlot(Plot plot) throws SQLException {
            if (abandonPlot(plot)) {
                try {
                    CompletableFuture.runAsync(() -> {
                        try {
                            Server plotServer = plot.getCity().getCountry().getServer();

                            Files.deleteIfExists(Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), "finishedSchematics", String.valueOf(plot.getCity().getID()), plot.getID() + ".schematic"));
                            Files.deleteIfExists(Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), String.valueOf(plot.getCity().getID()), plot.getID() + ".schematic"));
                            Files.deleteIfExists(Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), String.valueOf(plot.getCity().getID()), plot.getID() + "-env.schematic"));

                            if (plotServer.getFTPConfiguration() != null) {
                                FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()), plot.getID() + ".schematic");
                                FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()).replaceFirst("finishedSchematics/",""), plot.getID() + ".schematic");
                                FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()).replaceFirst("finishedSchematics/",""), plot.getID() + "-env.schematic");
                            }

                            DatabaseConnection.createStatement("DELETE FROM plotsystem_plots WHERE id = ?")
                                    .setValue(plot.getID()).executeUpdate();
                        } catch (IOException | SQLException | URISyntaxException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                            throw new CompletionException(ex);
                        }
                    });
                } catch (CompletionException ex) {
                    return false;
                }
                return true;
            }
            Bukkit.getLogger().log(Level.WARNING, "Failed to delete plot with the ID " + plot.getID() + "!");
            return false;
        }
    }

    public static final class Cache {
        private static final HashMap<UUID, List<Plot>> cachedInProgressPlots = new HashMap<>();

        public static void clearCache(){
            cachedInProgressPlots.clear();
        }

        public static void clearCache(UUID builderUUID) {
            cachedInProgressPlots.remove(builderUUID);
        }

        public static List<Plot> getCachedInProgressPlots(Builder builder){
            if(!cachedInProgressPlots.containsKey(builder.getUUID())) {
                try {
                    cachedInProgressPlots.put(builder.getUUID(), Plot.getPlots(builder, Status.unfinished));
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    return new ArrayList<>();
                }
            }

            return cachedInProgressPlots.get(builder.getUUID());
        }

        public static HashMap<UUID, List<Plot>> getCachedInProgressPlots() {
            return cachedInProgressPlots;
        }
    }

    public static final class Effects {
        public static final int CACHE_UPDATE_TICKS = 20*60;
        private static int time;

        private static boolean ParticleAPIEnabled = false;
        private static Particles_1_8 particles;

        public static void loadParticleNativeAPI(){
            ParticleAPIEnabled = PlotSystem.DependencyManager.isParticleNativeAPIEnabled();

            // get API
            ParticleNativeAPI api = ParticleNativePlugin.getAPI();

            // choose particles lists you want to use
            particles = api.getParticles_1_8();
        }

        public static void startTimer(){
            if(PlotSystem.DependencyManager.isParticleNativeAPIEnabled())
                loadParticleNativeAPI();

            Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
                if(PlotSystem.DependencyManager.isParticleNativeAPIEnabled())
                    loadParticleNativeAPI();
            }, 20*10L);


            Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), Effects::tick, 0L, 0L);
        }

        public static void tick(){
            time++;
            if(time%CACHE_UPDATE_TICKS == 0) Cache.clearCache();
            if(time%10 == 0) showOutlines();
        }

        public static void showOutlines(){
            try {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Builder builder = Builder.byUUID(player.getUniqueId());

                    List<Plot> plots = Cache.getCachedInProgressPlots(builder);
                    BlockVector2 playerPos2D = BlockVector2.at(player.getLocation().getX(), player.getLocation().getZ());

                    if(plots.isEmpty()) continue;

                    for(Plot plot : plots) {
                        if (!plot.getWorld().getWorldName().equals(player.getWorld().getName()))
                            continue;

                        if (!plot.getPlotOwner().getPlotTypeSetting().hasEnvironment() || plot.getVersion() <= 2)
                            continue;

                        List<BlockVector2> points = plot.getBlockOutline();
                        BlockVector2 center = getCenterFromOutline(points);

                        for (BlockVector2 point : points) {
                            BlockVector2 shiftedPoint = BlockVector2.at(point.getX() - center.getX(), point.getZ() - center.getZ());

                            if (shiftedPoint.distanceSq(playerPos2D) < 50 * 50)
                                if (!ParticleAPIEnabled)
                                    player.spawnParticle(Particle.FLAME, shiftedPoint.getX(), player.getLocation().getY() + 1, shiftedPoint.getZ(), 1, 0.0, 0.0, 0.0, 0);
                                else {
                                    Location loc = new Location(player.getWorld(), shiftedPoint.getX(), player.getLocation().getY() + 1, shiftedPoint.getZ());
                                    // create a particle packet
                                    Object packet = particles.FLAME().packet(true, loc);

                                    // send this packet to player
                                    particles.sendPacket(player, packet);
                                }
                        }
                    }
                }
            } catch (SQLException | IOException ex) {
                Bukkit.getLogger().log(Level.INFO, "A SQL error occurred!", ex);
            }
        }
    }

    public static final class ChatFormatting {
        public static void sendLinkMessages(AbstractPlot plot, Player player){
            Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
                TextComponent[] tc = new TextComponent[3];
                tc[0] = new TextComponent();
                tc[1] = new TextComponent();
                tc[2] = new TextComponent();

                try {
                    if(PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.SHORTLINK_ENABLE)) {
                        tc[0].setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK_WITH_SHORTLINK, "Google Maps", ShortLink.generateShortLink(
                                plot.getGoogleMapsLink(),
                                PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_APIKEY),
                                PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_HOST))));

                        tc[1].setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK_WITH_SHORTLINK, "Google Earth Web", ShortLink.generateShortLink(
                                plot.getGoogleEarthLink(),
                                PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_APIKEY),
                                PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_HOST))));

                        tc[2].setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK_WITH_SHORTLINK, "Open Street Map", ShortLink.generateShortLink(
                                plot.getOSMMapsLink(),
                                PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_APIKEY),
                                PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_HOST))));
                    } else {
                        tc[0].setText("§8§l> " + GRAY + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GREEN + BOLD.toString() + "Google Maps" + GRAY));
                        tc[1].setText("§8§l> " + GRAY + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GREEN + BOLD.toString() + "Google Earth Web" + GRAY));
                        tc[2].setText("§8§l> " + GRAY + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GREEN + BOLD.toString() + "Open Street Map" + GRAY));
                    }

                    tc[0].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleMapsLink()));
                    tc[1].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleEarthLink()));
                    tc[2].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getOSMMapsLink()));
                } catch (IOException | URISyntaxException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "An error occurred while creating short link!", ex);
                }

                tc[0].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Google Maps")));
                tc[1].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Google Earth Web")));
                tc[2].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Open Street Map")));

                // Temporary fix for bedrock players
                String coords = null;
                try {
                    String[] coordsSplit = plot.getGeoCoordinates().split(",");
                    double lat = Double.parseDouble(coordsSplit[0]);
                    double lon = Double.parseDouble(coordsSplit[1]);
                    DecimalFormat df = new DecimalFormat("##.#####");
                    df.setRoundingMode(RoundingMode.FLOOR);
                    coords = "§a" + df.format(lat) + "§7, §a" + df.format(lon);
                } catch (IOException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                }

                player.sendMessage("§8--------------------------");
                if (coords != null) player.sendMessage("§7Coords: " + coords);
                player.spigot().sendMessage(tc[0]);
                player.spigot().sendMessage(tc[1]);
                player.spigot().sendMessage(tc[2]);
                player.sendMessage("§8--------------------------");

                if (plot instanceof Plot) sendGroupTipMessage((Plot) plot, player);
            });
        }

        public static void sendGroupTipMessage(Plot plot, Player player) {
            try {
                if (plot.getPlotMembers().isEmpty()) {
                    TextComponent tc = new TextComponent();
                    tc.setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_PLAY_WITH_FRIENDS));
                    tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/plot members " + plot.getID()));
                    tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LangUtil.getInstance().get(player, LangPaths.Plot.MEMBERS))));

                    player.spigot().sendMessage(tc);
                    player.sendMessage("§8--------------------------");
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        public static void sendFeedbackMessage(List<Plot> plots, Player player) throws SQLException {
            player.sendMessage("§8--------------------------");
            for(Plot plot : plots) {
                player.sendMessage("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Message.Info.REVIEWED_PLOT, String.valueOf(plot.getID())));
                TextComponent tc = new TextComponent();
                tc.setText(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_FEEDBACK));
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot feedback " + plot.getID()));
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(
                        LangUtil.getInstance().get(player, LangPaths.Plot.PLOT_NAME) + " " + LangUtil.getInstance().get(player, LangPaths.Review.FEEDBACK))));
                player.spigot().sendMessage(tc);

                if(plots.size() != plots.indexOf(plot) + 1) {
                    player.sendMessage("");
                }
            }
            player.sendMessage("§8--------------------------");
            player.playSound(player.getLocation(), Utils.SoundUtils.FINISH_PLOT_SOUND, 1, 1);
        }

        public static void sendUnfinishedPlotReminderMessage(List<Plot> plots, Player player) {
            player.sendMessage("§7§l> " + LangUtil.getInstance().get(player, plots.size() <= 1 ? LangPaths.Message.Info.UNFINISHED_PLOT : LangPaths.Message.Info.UNFINISHED_PLOTS, String.valueOf(plots.size())));
            TextComponent tc = new TextComponent();
            tc.setText(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_PLOTS));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plots"));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SHOW_PLOTS))));
            player.spigot().sendMessage(tc);
        }

        public static void sendUnreviewedPlotsReminderMessage(List<Plot> plots, Player player) {
            player.sendMessage("§7§l> " + LangUtil.getInstance().get(player, plots.size() <= 1 ?
                    LangPaths.Message.Info.UNREVIEWED_PLOT :
                    LangPaths.Message.Info.UNREVIEWED_PLOTS, String.valueOf(plots.size())));

            TextComponent tc = new TextComponent();
            tc.setText(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_OPEN_REVIEWS));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/review"));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SHOW_PLOTS))));
            player.spigot().sendMessage(tc);
        }
    }
}
