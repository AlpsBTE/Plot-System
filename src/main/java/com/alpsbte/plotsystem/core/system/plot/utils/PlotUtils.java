package com.alpsbte.plotsystem.core.system.plot.utils;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.core.system.review.ReviewNotification;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.alpsbte.plotsystem.utils.ShortLink;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.Particles_1_13;
import com.github.fierioziy.particlenativeapi.plugin.ParticleNativePlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

public final class PlotUtils {
    private PlotUtils() {}

    private static final String MSG_LINE = "--------------------------";
    public static final Map<UUID, BukkitTask> plotReminder = new HashMap<>();

    /**
     * Returns the plot that the player is currently standing on or next to.
     * If he is standing in a single-plot world, it returns the plot of this world.
     * If he is standing in a multi-plot world, it returns the closest plot of all unfinished plots in this city
     *
     * @return the current plot of the player
     */
    @Nullable
    public static AbstractPlot getCurrentPlot(@NotNull Builder builder, Status... statuses) {
        if (builder.isOnline()) {
            String worldName = builder.getPlayer().getWorld().getName();

            if (PlotWorld.isOnePlotWorld(worldName)) {
                int id = Integer.parseInt(worldName.substring(2));
                AbstractPlot plot = worldName.toLowerCase(Locale.ROOT).startsWith("p-")
                        ? DataProvider.PLOT.getPlotById(id)
                        : DataProvider.TUTORIAL_PLOT.getById(id).orElseThrow();
                if (statuses == null) return plot;
                for (Status status : statuses) if (status == plot.getStatus()) return plot;
                return null;
            } else if (PlotWorld.isCityPlotWorld(worldName)) {
                String cityID = worldName.substring(2);
                Optional<CityProject> city = DataProvider.CITY_PROJECT.getById(cityID);
                if (city.isEmpty()) return null;

                List<Plot> plots = DataProvider.PLOT.getPlots(city.get(), statuses);
                if (plots.isEmpty()) return null;
                if (plots.size() == 1) return plots.getFirst();

                // Find the plot in the city world that is closest to the player
                Location playerLoc = builder.getPlayer().getLocation().clone();
                Vector3 playerVector = Vector3.at(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ());

                double distance = 100000000;
                Plot chosenPlot = plots.getFirst();
                for (Plot plot : plots) {
                    if (plot.getPlotType() != PlotType.CITY_INSPIRATION_MODE) continue;
                    BlockVector3 plotCenter = plot.getCenter();
                    if (plotCenter.withY((int) playerVector.y()).distance(playerVector.toBlockPoint()) < distance) {
                        distance = plotCenter.distance(playerVector.toBlockPoint());
                        chosenPlot = plot;
                    }
                }

                return chosenPlot;
            }
        }
        return null;
    }

    public static boolean isPlayerOnPlot(@NotNull AbstractPlot plot, Player player) {
        PlotWorld world = plot.getWorld();
        if (world.isWorldLoaded() && world.getBukkitWorld().getPlayers().contains(player)) {
            Location playerLoc = player.getLocation();
            ProtectedRegion protectedRegion = world.getProtectedRegion();
            return protectedRegion == null || protectedRegion.contains(Vector3.toBlockPoint(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()));
        }
        return false;
    }

    public static @Nullable CuboidRegion getPlotAsRegion(@NotNull AbstractPlot plot) throws IOException {
        Clipboard clipboard;
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(new ByteArrayInputStream(plot.getInitialSchematicBytes()))) {
            clipboard = reader.read();
        }
        if (clipboard == null) return null;

        // No longer supported!
        if (plot.getVersion() < 4) return null;

        return new CuboidRegion(
                clipboard.getMinimumPoint().withY(plot.getWorld().getPlotHeight()),
                clipboard.getMaximumPoint().withY(PlotWorld.MAX_WORLD_HEIGHT));
    }

    public static boolean isPlotWorld(@NotNull World world) {
        return DependencyManager.getMultiverseCore().getWorldManager().isLoadedWorld(world) && (PlotWorld.isOnePlotWorld(world.getName()) || PlotWorld.isCityPlotWorld(world.getName()));
    }

    public static byte @Nullable [] getOutlinesSchematicBytes(@NotNull AbstractPlot plot, byte[] initialSchematic, World world) throws IOException {
        Clipboard clipboard;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(initialSchematic);
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(inputStream)) {
            clipboard = reader.read();
        }

        Polygonal2DRegion region = new Polygonal2DRegion(
                BukkitAdapter.adapt(world),
                plot.getOutline(),
                clipboard.getMinimumPoint().y(),
                clipboard.getMaximumPoint().y()
        );

        BlockArrayClipboard newClipboard = new BlockArrayClipboard(region);
        newClipboard.setOrigin(BlockVector3.at(region.getCenter().x(), region.getMinimumPoint().y(), region.getCenter().z()));
        ForwardExtentCopy copy = new ForwardExtentCopy(
                clipboard,
                region,
                newClipboard,
                region.getMinimumPoint()
        );

        Operations.complete(copy);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ClipboardWriter writer = AbstractPlot.CLIPBOARD_FORMAT.getWriter(outputStream)) {
            writer.write(newClipboard);
        }
        return outputStream.toByteArray();
    }

    public static @NotNull String getDefaultSchematicPath() {
        return Paths.get(PlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "schematics") + File.separator;
    }

    public static @Nullable CompletableFuture<double[]> convertTerraToPlotXZ(@NotNull AbstractPlot plot, double[] terraCoords) throws IOException {
        // Load plot outlines schematic as clipboard
        Clipboard clipboard;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(plot.getInitialSchematicBytes());
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(inputStream)) {
            clipboard = reader.read();
        }
        if (clipboard == null) return null;

        // Calculate min and max points of schematic
        CuboidRegion plotRegion = getPlotAsRegion(plot);
        if (plotRegion == null) return null;

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
        PlotWorld world = plot.getWorld();
        ProtectedRegion protectedPlotRegion = world.getProtectedRegion() != null
                ? world.getProtectedRegion()
                : world.getProtectedBuildRegion();
        if (protectedPlotRegion.contains(BlockVector3.at((int) plotCoords[0], world.getPlotHeightCentered(), (int) plotCoords[1]))) {
            return CompletableFuture.completedFuture(plotCoords);
        }

        return null;
    }

    public static void informPlayerAboutUnfinishedPlots(@NotNull Player player, Builder builder) {
        try {
            List<Plot> plots = Cache.getCachedInProgressPlots(builder);
            if (plots.isEmpty()) return;
            ChatFormatting.sendUnfinishedPlotReminderMessage(plots, player);
        } catch (Exception ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while trying to inform the player about his unfinished plots!"), ex);
        }
    }

    public static void startUnfinishedPlotReminderTimer(Player player) {
        int interval = PlotSystem.getPlugin().getConfig().getInt(ConfigPaths.UNFINISHED_REMINDER_INTERVAL);
        if (interval == -1) return;
        plotReminder.put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(),
                () -> informPlayerAboutUnfinishedPlots(player, Builder.byUUID(player.getUniqueId())),
                20L * 60 * interval,
                20L * 60 * interval));
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
                cachedInProgressPlots.put(builder.getUUID(), DataProvider.PLOT.getPlots(builder, Status.unfinished));
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
        private static Particles_1_13 particles;

        public static void loadParticleNativeAPI() {
            particleAPIEnabled = DependencyManager.isParticleNativeAPIEnabled();

            // get API
            ParticleNativeAPI api = ParticleNativePlugin.getAPI();

            // choose particle list you want to use
            particles = api.getParticles_1_13();
        }

        public static void startTimer() {
            if (DependencyManager.isParticleNativeAPIEnabled())
                loadParticleNativeAPI();

            Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
                if (DependencyManager.isParticleNativeAPIEnabled())
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
            for (Player player : Bukkit.getOnlinePlayers()) {
                Builder builder = Builder.byUUID(player.getUniqueId());

                List<Plot> plots = Cache.getCachedInProgressPlots(builder);
                BlockVector2 playerPos2D = BlockVector2.at(player.getLocation().getX(), player.getLocation().getZ());

                if (plots.isEmpty()) continue;

                for (Plot plot : plots) {
                    if ((!plot.getWorld().getWorldName().equals(player.getWorld().getName())) ||
                            (!plot.getPlotOwner().getPlotType().hasEnvironment() || plot.getVersion() <= 2)) {
                        continue;
                    }

                    List<BlockVector2> points = plot.getBlockOutline();

                    for (BlockVector2 point : points) {
                        if (point.distanceSq(playerPos2D) >= 50 * 50) continue;
                        if (!particleAPIEnabled) {
                            player.spawnParticle(Particle.FLAME, point.x(), player.getLocation().getY() + 1, point.z(), 1, 0.0, 0.0, 0.0, 0);
                            continue;
                        }
                        Location loc = new Location(player.getWorld(), point.x(), player.getLocation().getY() + 1, point.z());
                        // create a particle packet
                        Object packet = particles.FLAME().packet(true, loc);

                        // send this packet to player
                        particles.sendPacket(player, packet);
                    }
                }
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
            if (plot.getPlotMembers().isEmpty() && PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)) {
                Component tc = text("» ", DARK_GRAY)
                        .append(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_PLAY_WITH_FRIENDS), GRAY))
                        .clickEvent(ClickEvent.runCommand("/plot members " + plot.getID()))
                        .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.Plot.MEMBERS)));

                player.sendMessage(tc);
                player.sendMessage(text(MSG_LINE, DARK_GRAY));
            }
        }

        public static void sendFeedbackMessage(@NotNull List<ReviewNotification> notifications, @NotNull Player player) {
            player.sendMessage(text(MSG_LINE, DARK_GRAY));
            for (ReviewNotification notification : notifications) {
                PlotReview review = DataProvider.REVIEW.getReview(notification.reviewId()).orElseThrow();
                player.sendMessage(text("» ", DARK_GRAY).append(text(LangUtil.getInstance().get(player, LangPaths.Message.Info.REVIEWED_PLOT, String.valueOf(review.getPlot().getID())), GREEN)));

                Component tc = text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_FEEDBACK), GOLD)
                        .clickEvent(ClickEvent.runCommand("/plot feedback " + review.getPlot().getID()))
                        .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.Plot.PLOT_NAME) + " " + LangUtil.getInstance().get(player, LangPaths.Review.FEEDBACK)));
                player.sendMessage(tc);

                DataProvider.REVIEW.removeReviewNotification(notification.reviewId(), notification.uuid());

                if (notifications.size() != notifications.indexOf(notification) + 1) {
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
                    .hoverEvent(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SHOW_PLOTS)))
                    .appendNewline();
            player.sendMessage(tc);
        }
    }
}
