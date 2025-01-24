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
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.Particles_1_8;
import com.github.fierioziy.particlenativeapi.plugin.ParticleNativePlugin;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
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

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class PlotUtils {

    public static final String SCHEM_ENDING = ".schem";
    public static final String SCHEMATIC_ENDING = ".schematic";

    private PlotUtils() {}

    private static final String MSG_LINE = "--------------------------";

    /**
     * Returns the plot that the player is currently standing on or next to.
     * If he is standing in a single-plot world, it returns the plot of this world.
     * If he is standing in a multi-plot world, it returns the closest plot of all unfinished plots in this city
     *
     * @return the current plot of the player
     */
    @Nullable
    public static AbstractPlot getCurrentPlot(@NotNull Builder builder, Status... statuses) throws SQLException {
        if (builder.isOnline()) {
            String worldName = builder.getPlayer().getWorld().getName();

            if (PlotWorld.isOnePlotWorld(worldName)) {
                int id = Integer.parseInt(worldName.substring(2));
                AbstractPlot plot = worldName.toLowerCase(Locale.ROOT).startsWith("p-") ? new Plot(id) : new TutorialPlot(id);
                if (statuses == null) return plot;
                for (Status status : statuses) if (status == plot.getStatus()) return plot;
                return null;
            } else if (PlotWorld.isCityPlotWorld(worldName)) {
                int cityID = Integer.parseInt(worldName.substring(2));
                List<Plot> plots = Plot.getPlots(cityID, statuses);

                if (plots.isEmpty()) return null;
                if (plots.size() == 1) return plots.getFirst();

                // Find the plot in the city world that is closest to the player
                Location playerLoc = builder.getPlayer().getLocation().clone();
                Vector3 playerVector = Vector3.at(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ());

                double distance = 100000000;
                Plot chosenPlot = plots.getFirst();
                for (Plot plot : plots)
                    if (plot.getPlotType() == PlotType.CITY_INSPIRATION_MODE && plot.getCenter().withY((int) playerVector.y()).distance(playerVector.toBlockPoint()) < distance) {
                        distance = plot.getCenter().distance(playerVector.toBlockPoint());
                        chosenPlot = plot;
                    }

                return chosenPlot;
            }
        }
        return null;
    }

    public static boolean isPlayerOnPlot(@NotNull AbstractPlot plot, Player player) throws SQLException {
        if (plot.getWorld().isWorldLoaded() && plot.getWorld().getBukkitWorld().getPlayers().contains(player)) {
            Location playerLoc = player.getLocation();
            return plot.getWorld().getProtectedRegion().contains(Vector3.toBlockPoint(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()));
        }
        return false;
    }

    public static @Nullable CuboidRegion getPlotAsRegion(@NotNull AbstractPlot plot) throws IOException, SQLException {
        try (Clipboard clipboard = Objects.requireNonNull(ClipboardFormats.findByFile(plot.getOutlinesSchematic())).load(plot.getOutlinesSchematic())) {

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
                            plotCenter.x() - (outlinesClipboardCenterX - regionCenterModX),
                            PlotWorld.MIN_WORLD_HEIGHT,
                            plotCenter.z() - (outlinesClipboardCenterZ - regionCenterModZ)
                    );

                    BlockVector3 schematicMaxPoint = BlockVector3.at(
                            plotCenter.x() + outlinesClipboardCenterX,
                            PlotWorld.MAX_WORLD_HEIGHT,
                            plotCenter.z() + outlinesClipboardCenterZ
                    );

                    return new CuboidRegion(schematicMinPoint, schematicMaxPoint);
                }
            }
        }
        return null;
    }

    public static boolean isPlotWorld(World world) {
        return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().isMVWorld(world) && (PlotWorld.isOnePlotWorld(world.getName()) || PlotWorld.isCityPlotWorld(world.getName()));
    }


    public static @NotNull String getDefaultSchematicPath() {
        return Paths.get(PlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "schematics") + File.separator;
    }

    public static boolean savePlotAsSchematic(@NotNull Plot plot) throws IOException, SQLException, WorldEditException {
        Clipboard clipboard = Objects.requireNonNull(ClipboardFormats.findByFile(plot.getOutlinesSchematic())).load(plot.getOutlinesSchematic());
        if (clipboard != null) {
            CuboidRegion cuboidRegion = getPlotAsRegion(plot);

            if (cuboidRegion != null) {
                // Get plot outline
                List<BlockVector2> plotOutlines = plot.getOutline();

                // Load finished plot region as cuboid region
                if (plot.getWorld().loadWorld()) {
                    com.sk89q.worldedit.world.World world = new BukkitWorld(plot.getWorld().getBukkitWorld());
                    Polygonal2DRegion region = new Polygonal2DRegion(world, plotOutlines, cuboidRegion.getMinimumPoint().y(), cuboidRegion.getMaximumPoint().y());

                    File finishedSchematicFile = getFinalFile(plot, region, cuboidRegion, clipboard);
                    if (finishedSchematicFile == null) return false;

                    // Upload to FTP server
                    if (plot.getCity().getCountry().getServer().getFTPConfiguration() != null) {
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                return FTPManager.uploadSchematic(FTPManager.getFTPUrl(plot.getCity().getCountry().getServer(), plot.getCity().getID()), finishedSchematicFile);
                            } catch (SQLException | URISyntaxException ex) {
                                Utils.logSqlException(ex);
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

    private static @Nullable File getFinalFile(@NotNull Plot plot, Polygonal2DRegion region, CuboidRegion cuboidRegion, Clipboard clipboard) throws SQLException, IOException {
        // Copy and write finished plot clipboard to schematic file
        File finishedSchematicFile = Paths.get(PlotUtils.getDefaultSchematicPath(),
                String.valueOf(plot.getCity().getCountry().getServer().getID()),
                "finishedSchematics", String.valueOf(plot.getCity().getID()), plot.getID() + SCHEM_ENDING).toFile();

        if (!finishedSchematicFile.exists()) {
            boolean createdDirs = finishedSchematicFile.getParentFile().mkdirs();
            boolean createdFile = finishedSchematicFile.createNewFile();
            if ((!finishedSchematicFile.getParentFile().exists() && !createdDirs) || (!finishedSchematicFile.exists() && !createdFile)) {
                return null;
            }
        }

        try (Clipboard cb = new BlockArrayClipboard(region)) {
            if (plot.getVersion() >= 3) {
                cb.setOrigin(BlockVector3.at(plot.getCenter().x(), cuboidRegion.getMinimumY(), (double) plot.getCenter().z()));
            } else {
                BlockVector3 terraCenter = plot.getCoordinates();
                cb.setOrigin(BlockVector3.at(
                        (double) terraCenter.x() - (double) clipboard.getMinimumPoint().x() + cuboidRegion.getMinimumPoint().x(),
                        (double) terraCenter.y() - (double) clipboard.getMinimumPoint().y() + cuboidRegion.getMinimumPoint().y(),
                        (double) terraCenter.z() - (double) clipboard.getMinimumPoint().z() + cuboidRegion.getMinimumPoint().z()
                ));
            }
        }

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(Objects.requireNonNull(region.getWorld()), region, clipboard, region.getMinimumPoint());
        Operations.complete(forwardExtentCopy);

        try (ClipboardWriter writer = BuiltInClipboardFormat.FAST_V2.getWriter(new FileOutputStream(finishedSchematicFile, false))) {
            writer.write(clipboard);
        }
        return finishedSchematicFile;
    }

    public static @Nullable CompletableFuture<double[]> convertTerraToPlotXZ(@NotNull AbstractPlot plot, double[] terraCoords) throws IOException, SQLException {
        // Load plot outlines schematic as clipboard
        try (Clipboard clipboard = Objects.requireNonNull(ClipboardFormats.findByFile(plot.getOutlinesSchematic())).load(plot.getOutlinesSchematic())) {

            if (clipboard != null) {
                // Calculate min and max points of schematic
                CuboidRegion plotRegion = getPlotAsRegion(plot);

                if (plotRegion != null) {
                    // Convert terra schematic coordinates into relative plot schematic coordinates
                    double[] schematicCoords = {
                            terraCoords[0] - clipboard.getMinimumPoint().x(),
                            terraCoords[1] - clipboard.getMinimumPoint().z()
                    };

                    // Add additional plot sizes to relative plot schematic coordinates
                    double[] plotCoords = {
                            schematicCoords[0] + plotRegion.getMinimumPoint().x(),
                            schematicCoords[1] + plotRegion.getMinimumPoint().z()
                    };

                    // Return coordinates if they are in the schematic plot region
                    ProtectedRegion protectedPlotRegion = plot.getWorld().getProtectedRegion() != null ? plot.getWorld().getProtectedRegion() : plot.getWorld().getProtectedBuildRegion();
                    if (protectedPlotRegion.contains(BlockVector3.at((int) plotCoords[0], plot.getWorld().getPlotHeightCentered(), (int) plotCoords[1]))) {
                        return CompletableFuture.completedFuture(plotCoords);
                    }
                }
            }
        }

        return null;
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
                                PlotSystem.getPlugin().getComponentLogger().info(text("Abandoned plot #" + plot.getID() + " due to inactivity!"));
                            } else {
                                PlotSystem.getPlugin().getComponentLogger().warn(text("An error occurred while abandoning plot #" + plot.getID() + " due to inactivity!"));
                            }
                        });
                    }
                }
            } catch (SQLException ex) {
                Utils.logSqlException(ex);
            }
        }, 0L, 20 * 60 * 60L); // Check every hour
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
                    Utils.logSqlException(ex);
                }
            }), 0L, 20 * interval);
        }
    }

    public static boolean plotExists(int id) {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT COUNT(id) FROM plotsystem_plots WHERE id = ?")
                .setValue(id).executeQuery()) {

            if (rs.next() && rs.getInt(1) > 0) {
                DatabaseConnection.closeResultSet(rs);
                return true;
            }

            DatabaseConnection.closeResultSet(rs);
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public static final class Actions {
        private Actions() {}

        public static void submitPlot(@NotNull Plot plot) throws SQLException {
            plot.setStatus(Status.unreviewed);

            if (plot.getWorld().isWorldLoaded()) {
                for (Player player : plot.getWorld() instanceof OnePlotWorld ? plot.getWorld().getBukkitWorld().getPlayers() : ((CityPlotWorld) plot.getWorld()).getPlayersOnPlot()) {
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

        public static void undoSubmit(@NotNull Plot plot) throws SQLException {
            plot.setStatus(Status.unfinished);

            plot.getPermissions().addBuilderPerms(plot.getPlotOwner().getUUID()).save();
            if (!plot.getPlotMembers().isEmpty()) {
                for (Builder builder : plot.getPlotMembers()) {
                    plot.getPermissions().addBuilderPerms(builder.getUUID());
                }
            }
        }

        public static boolean abandonPlot(@NotNull AbstractPlot plot) {
            try {
                if (plot.getWorld() instanceof OnePlotWorld) {
                    if (plot.getWorld().isWorldGenerated()) {
                        if (plot.getWorld().isWorldLoaded()) {
                            for (Player player : plot.getWorld().getBukkitWorld().getPlayers()) {
                                player.teleport(Utils.getSpawnLocation());
                            }
                        }
                        if (!plot.getWorld().deleteWorld()) PlotSystem.getPlugin().getComponentLogger().warn(text("Could not delete plot world " + plot.getWorld().getWorldName() + "!"));
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
                        } else PlotSystem.getPlugin().getComponentLogger().warn(text("Region Manager is null!"));

                        playersToTeleport.forEach(p -> p.teleport(Utils.getSpawnLocation()));
                        if (plot.getWorld().isWorldLoaded()) plot.getWorld().unloadWorld(false);
                    }
                }
            } catch (SQLException | IOException | WorldEditException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("Failed to abandon plot with the ID " + plot.getID() + "!"), ex);
                return false;
            }
            return updateDatabase(plot);
        }

        private static boolean updateDatabase(@NotNull AbstractPlot plot) {
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
                        Utils.logSqlException(ex);
                        throw new CompletionException(ex);
                    }
                }).join();
            } catch (CompletionException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("Failed to abandon plot with the ID " + plot.getID() + "!"), ex);
                return false;
            }
            return true;
        }

        public static boolean deletePlot(Plot plot) {
            if (abandonPlot(plot)) {
                try {
                    CompletableFuture.runAsync(() -> {
                        try {
                            Server plotServer = plot.getCity().getCountry().getServer();

                            Files.deleteIfExists(Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), "finishedSchematics", String.valueOf(plot.getCity().getID()), plot.getID() + SCHEMATIC_ENDING));
                            Files.deleteIfExists(Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), String.valueOf(plot.getCity().getID()), plot.getID() + SCHEMATIC_ENDING));
                            Files.deleteIfExists(Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), String.valueOf(plot.getCity().getID()), plot.getID() + "-env" + SCHEMATIC_ENDING));

                            if (plotServer.getFTPConfiguration() != null) {
                                FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()), plot.getID() + SCHEMATIC_ENDING);
                                FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()).replaceFirst("finishedSchematics/", ""), plot.getID() + SCHEMATIC_ENDING);
                                FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()).replaceFirst("finishedSchematics/", ""), plot.getID() + "-env" + SCHEMATIC_ENDING);
                            }

                            DatabaseConnection.createStatement("DELETE FROM plotsystem_plots WHERE id = ?")
                                    .setValue(plot.getID()).executeUpdate();
                        } catch (IOException | SQLException | URISyntaxException ex) {
                            PlotSystem.getPlugin().getComponentLogger().error(text(ex.getMessage()), ex);
                            throw new CompletionException(ex);
                        }
                    });
                } catch (CompletionException ex) {
                    return false;
                }
                return true;
            }
            PlotSystem.getPlugin().getComponentLogger().warn(text("Failed to delete plot with the ID " + plot.getID() + "!"));
            return false;
        }
    }

    public static final class Cache {
        private Cache() {}
        private static final HashMap<UUID, List<Plot>> cachedInProgressPlots = new HashMap<>();

        public static void clearCache() {
            cachedInProgressPlots.clear();
        }

        public static void clearCache(UUID builderUUID) {
            cachedInProgressPlots.remove(builderUUID);
        }

        public static List<Plot> getCachedInProgressPlots(@NotNull Builder builder) {
            if (!cachedInProgressPlots.containsKey(builder.getUUID())) {
                try {
                    cachedInProgressPlots.put(builder.getUUID(), Plot.getPlots(builder, Status.unfinished));
                } catch (SQLException ex) {
                    Utils.logSqlException(ex);
                    return new ArrayList<>();
                }
            }

            return cachedInProgressPlots.get(builder.getUUID());
        }

        public static Map<UUID, List<Plot>> getCachedInProgressPlots() {
            return cachedInProgressPlots;
        }
    }

    public static final class Effects {
        private Effects() {}
        public static final int CACHE_UPDATE_TICKS = 20 * 60;
        private static int time;

        private static boolean particleAPIEnabled = false;
        private static Particles_1_8 particles;

        public static void loadParticleNativeAPI() {
            particleAPIEnabled = PlotSystem.DependencyManager.isParticleNativeAPIEnabled();

            // get API
            ParticleNativeAPI api = ParticleNativePlugin.getAPI();

            // choose particle list you want to use
            particles = api.getParticles_1_8();
        }

        public static void startTimer() {
            if (PlotSystem.DependencyManager.isParticleNativeAPIEnabled())
                loadParticleNativeAPI();

            Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
                if (PlotSystem.DependencyManager.isParticleNativeAPIEnabled())
                    loadParticleNativeAPI();
            }, 20 * 10L);


            Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), Effects::tick, 0L, 0L);
        }

        public static void tick() {
            time++;
            if (time % CACHE_UPDATE_TICKS == 0) Cache.clearCache();
            if (time % 10 == 0) showOutlines();
        }

        public static void showOutlines() {
            try {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Builder builder = Builder.byUUID(player.getUniqueId());

                    List<Plot> plots = Cache.getCachedInProgressPlots(builder);
                    BlockVector2 playerPos2D = BlockVector2.at(player.getLocation().getX(), player.getLocation().getZ());

                    if (plots.isEmpty()) continue;

                    for (Plot plot : plots) {
                        if ((!plot.getWorld().getWorldName().equals(player.getWorld().getName())) ||
                                (!plot.getPlotOwner().getPlotTypeSetting().hasEnvironment() || plot.getVersion() <= 2)) {
                            continue;
                        }

                        List<BlockVector2> points = plot.getBlockOutline();

                        for (BlockVector2 point : points)
                            if (point.distanceSq(playerPos2D) < 50 * 50) {
                                if (!particleAPIEnabled) {
                                    player.spawnParticle(Particle.FLAME, point.x(), player.getLocation().getY() + 1, point.z(), 1, 0.0, 0.0, 0.0, 0);
                                } else {
                                    Location loc = new Location(player.getWorld(), point.x(), player.getLocation().getY() + 1, point.z());
                                    // create a particle packet
                                    Object packet = particles.FLAME().packet(true, loc);

                                    // send this packet to player
                                    particles.sendPacket(player, packet);
                                }
                            }
                    }
                }
            } catch (SQLException | IOException ex) {
                Utils.logSqlException(ex);
            }
        }
    }

    public static final class ChatFormatting {
        private ChatFormatting() {}
        public static void sendLinkMessages(AbstractPlot plot, Player player) {
            Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
                Component[] tc = new Component[3];

                String shortLinkGoogleMaps = null;
                String shortLinkGoogleEarth = null;
                String shortLinkOSM = null;
                String googleMaps = " Google Maps ";
                String googleEarthWeb = " Google Earth Web ";
                String openStreetMap = " Open Street Map ";
                try {
                    if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.SHORTLINK_ENABLE)) {
                        shortLinkGoogleMaps = ShortLink.generateShortLink(plot.getGoogleMapsLink());
                        tc[0] = text("» ", DARK_GRAY).append(LangUtil.getInstance().getComponent(player.getUniqueId(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK_WITH_SHORTLINK, GRAY, text(googleMaps, GREEN), text(shortLinkGoogleMaps, GREEN)));
                        shortLinkGoogleEarth = ShortLink.generateShortLink(plot.getGoogleEarthLink());
                        tc[1] = text("» ", DARK_GRAY).append(LangUtil.getInstance().getComponent(player.getUniqueId(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK_WITH_SHORTLINK, GRAY, text(googleEarthWeb, GREEN), text(shortLinkGoogleEarth, GREEN)));
                        shortLinkOSM = ShortLink.generateShortLink(plot.getOSMMapsLink());
                        tc[2] = text("» ", DARK_GRAY).append(LangUtil.getInstance().getComponent(player.getUniqueId(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK_WITH_SHORTLINK, GRAY, text(openStreetMap, GREEN), text(shortLinkOSM, GREEN)));
                    } else {
                        tc[0] = text("» ", DARK_GRAY).append(LangUtil.getInstance().getComponent(player.getUniqueId(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GRAY, text(googleMaps, GREEN)));
                        tc[1] = text("» ", DARK_GRAY).append(LangUtil.getInstance().getComponent(player.getUniqueId(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GRAY, text(googleEarthWeb, GREEN)));
                        tc[2] = text("» ", DARK_GRAY).append(LangUtil.getInstance().getComponent(player.getUniqueId(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GRAY, text(openStreetMap, GREEN)));
                    }

                    tc[0] = tc[0].clickEvent(ClickEvent.openUrl((shortLinkGoogleMaps != null) ? shortLinkGoogleMaps : plot.getGoogleMapsLink()));
                    tc[1] = tc[1].clickEvent(ClickEvent.openUrl((shortLinkGoogleEarth != null) ? shortLinkGoogleEarth : plot.getGoogleEarthLink()));
                    tc[2] = tc[2].clickEvent(ClickEvent.openUrl((shortLinkOSM != null) ? shortLinkOSM : plot.getOSMMapsLink()));
                } catch (IOException | URISyntaxException ex) {
                    PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while creating short link!"), ex);
                }

                tc[0] = tc[0].hoverEvent(text(googleMaps));
                tc[1] = tc[1].hoverEvent(text(googleEarthWeb));
                tc[2] = tc[2].hoverEvent(text(openStreetMap));

                // Temporary fix for bedrock players
                Component coords = null;
                try {
                    String[] coordsSplit = plot.getGeoCoordinates().split(",");
                    double lat = Double.parseDouble(coordsSplit[0]);
                    double lon = Double.parseDouble(coordsSplit[1]);
                    DecimalFormat df = new DecimalFormat("##.#####");
                    df.setRoundingMode(RoundingMode.FLOOR);
                    coords = text(df.format(lat), GREEN).append(text(", ", GRAY)).append(text(df.format(lon), GREEN));
                } catch (IOException ex) {
                    PlotSystem.getPlugin().getComponentLogger().error(text(ex.getMessage()), ex);
                }

                player.sendMessage(text(MSG_LINE, DARK_GRAY));
                if (coords != null) player.sendMessage(text("Coords: ", GRAY).append(coords));
                player.sendMessage(tc[0]);
                player.sendMessage(tc[1]);
                player.sendMessage(tc[2]);
                player.sendMessage(text(MSG_LINE, DARK_GRAY));

                if (plot instanceof Plot p) sendGroupTipMessage(p, player);
            });
        }

        public static void sendGroupTipMessage(@NotNull Plot plot, Player player) {
            try {
                if (plot.getPlotMembers().isEmpty()) {
                    Component tc = text("» ", DARK_GRAY)
                            .append(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_PLAY_WITH_FRIENDS), GRAY))
                            .clickEvent(ClickEvent.runCommand("/plot members " + plot.getID()))
                            .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.Plot.MEMBERS)));

                    player.sendMessage(tc);
                    player.sendMessage(text(MSG_LINE, DARK_GRAY));
                }
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text(ex.getMessage()), ex);
            }
        }

        public static void sendFeedbackMessage(@NotNull List<Plot> plots, @NotNull Player player) {
            player.sendMessage(text(MSG_LINE, DARK_GRAY));
            for (Plot plot : plots) {
                player.sendMessage(text("» ", DARK_GRAY).append(text(LangUtil.getInstance().get(player, LangPaths.Message.Info.REVIEWED_PLOT, String.valueOf(plot.getID())), GREEN)));

                Component tc = text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_FEEDBACK), GOLD)
                        .clickEvent(ClickEvent.runCommand("/plot feedback " + plot.getID()))
                        .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.Plot.PLOT_NAME) + " " + LangUtil.getInstance().get(player, LangPaths.Review.FEEDBACK)));
                player.sendMessage(tc);

                if (plots.size() != plots.indexOf(plot) + 1) {
                    player.sendMessage(empty());
                }
            }
            player.sendMessage(text(MSG_LINE, DARK_GRAY));
            player.playSound(player.getLocation(), Utils.SoundUtils.FINISH_PLOT_SOUND, 1, 1);
        }

        public static void sendUnfinishedPlotReminderMessage(@NotNull List<Plot> plots, @NotNull Player player) {
            player.sendMessage(text("» ", DARK_GRAY).append(text(LangUtil.getInstance().get(player, plots.size() <= 1 ? LangPaths.Message.Info.UNFINISHED_PLOT : LangPaths.Message.Info.UNFINISHED_PLOTS, String.valueOf(plots.size())), GREEN)));

            Component tc = text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_PLOTS), GOLD)
                    .clickEvent(ClickEvent.runCommand("/plots"))
                    .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SHOW_PLOTS)));
            player.sendMessage(tc);
        }

        public static void sendUnreviewedPlotsReminderMessage(@NotNull List<Plot> plots, @NotNull Player player) {
            player.sendMessage(text("» ", DARK_GRAY).append(text(LangUtil.getInstance().get(player, plots.size() <= 1 ?
                    LangPaths.Message.Info.UNREVIEWED_PLOT :
                    LangPaths.Message.Info.UNREVIEWED_PLOTS, String.valueOf(plots.size())), GREEN)));

            Component tc = text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_OPEN_REVIEWS), GOLD)
                    .clickEvent(ClickEvent.runCommand("/review"))
                    .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SHOW_PLOTS)));
            player.sendMessage(tc);
        }
    }
}
