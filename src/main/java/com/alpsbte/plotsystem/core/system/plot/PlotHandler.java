package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.AbstractPlotLoader;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.DefaultPlotLoader;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.text.Component.text;

public class PlotHandler {
    private PlotHandler() {}

    private static final Map<UUID, LocalDateTime> playerPlotGenerationHistory = new ConcurrentHashMap<>();

    private static boolean assignPlot(Builder builder, Plot plot, PlotType plotType) {
        Player player = builder.getPlayer();

        // Score Requirement met?
        if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.ENABLE_SCORE_REQUIREMENT) && !DataProvider.DIFFICULTY.builderMeetsRequirements(builder, plot.getDifficulty())) {
            if (player != null) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.PLAYER_NEEDS_HIGHER_SCORE)));
                player.playSound(player.getLocation(), Utils.SoundUtils.ERROR_SOUND, SoundCategory.MASTER, 1, 1, 0);
            }
            return false;
        }

        // Slot available?
        Slot freeSlot = builder.getFreeSlot();
        if (freeSlot == null) {
            if (player != null) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.ALL_SLOTS_OCCUPIED)));
                player.playSound(player.getLocation(), Utils.SoundUtils.ERROR_SOUND, SoundCategory.MASTER, 1, 1, 0);
            }
            return false;
        }

        // Assign
        Status previousStatus = plot.getStatus();
        PlotType previousType = plot.getPlotType();
        if (!builder.setSlot(freeSlot, plot.getId())) return false;
        if (!plot.setStatus(Status.unfinished)) {
            builder.setSlot(freeSlot, -1);
            return false;
        }
        if (!plot.setPlotType(plotType)) {
            plot.setStatus(previousStatus);
            builder.setSlot(freeSlot, -1);
            return false;
        }
        if (!plot.setPlotOwner(builder)) {
            plot.setPlotType(previousType);
            plot.setStatus(previousStatus);
            builder.setSlot(freeSlot, -1);
            return false;
        }
        return true;
    }

    public static boolean assignAndGeneratePlot(Builder builder, Plot plot) {
        PlotType type = builder.getPlotType();
        if (type.equals(PlotType.CITY_INSPIRATION_MODE) && ConfigUtil.getInstance().configs[0].getBoolean(ConfigPaths.DISABLE_CITY_INSPIRATION_MODE))
            type = PlotType.LOCAL_INSPIRATION_MODE;

        if (!tryStartGeneration(builder)) return false;

        boolean assigned = assignPlot(builder, plot, type);
        if (!assigned) {
            removePlayerFromGenerationHistory(builder.getUUID());
            return false;
        }

        return generatePlot(builder, plot, type);
    }

    public static boolean assignAndGenerateRandomPlot(Builder builder, CityProject city, PlotDifficulty difficulty) {
        Plot randomPlot = DataProvider.PLOT.getPlots(city, difficulty, Status.unclaimed)
                .get(Utils.getRandom().nextInt(DataProvider.PLOT.getPlots(city, difficulty, Status.unclaimed).size()));
        return assignAndGeneratePlot(builder, randomPlot);
    }

    public static boolean generatePlot(Builder builder, Plot plot, PlotType type) {
        Player player = builder.getPlayer();

        if (player != null) {
            Utils.runSync(() -> {
                player.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(player, LangPaths.Message.Info.CREATING_PLOT)));
                player.playSound(player.getLocation(), Utils.SoundUtils.CREATE_PLOT_SOUND, SoundCategory.MASTER, 1, 1, 0);
                return null;
            });
        }

        DefaultPlotLoader loader = new DefaultPlotLoader(plot, builder, type, PlotWorld.getByType(type, plot));
        return loader.isSuccessful();
    }

    private static boolean tryStartGeneration(Builder builder) {
        LocalDateTime now = LocalDateTime.now();
        synchronized (playerPlotGenerationHistory) {
            LocalDateTime lastGeneration = playerPlotGenerationHistory.get(builder.getUUID());
            if (lastGeneration != null && !lastGeneration.isBefore(now.minusSeconds(10))) {
                Player player = builder.getPlayer();
                if (player == null) return false;
                Utils.runSync(() -> {
                    player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.PLEASE_WAIT)));
                    player.playSound(player.getLocation(), Utils.SoundUtils.ERROR_SOUND, SoundCategory.MASTER, 1, 1, 0);
                    return null;
                });
                return false;
            }
            playerPlotGenerationHistory.put(builder.getUUID(), now);
            return true;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean abandonPlot(AbstractPlot plot) {
        try {
            boolean successfullyAbandoned = plot.getWorld().onAbandon();
            if (!successfullyAbandoned) {
                PlotSystem.getPlugin().getComponentLogger().error(text("Failed to abandon plot with the ID " + plot.getId() + "!"));
                return false;
            }
        } catch (Exception exception) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Failed to clean up the world for plot " + plot.getId() + "!"), exception);
            return false;
        }

        if (plot.getPlotType() == PlotType.TUTORIAL) return true;

        Plot dPlot = (Plot) plot;
        boolean successful = DataProvider.REVIEW.removeAllReviewsOfPlot(dPlot.getId());

        for (Builder builder : List.copyOf(dPlot.getPlotMembers())) {
            successful &= dPlot.removePlotMember(builder);
        }

        Builder owner = plot.getPlotOwner();
        if (owner != null) {
            PlotUtils.Cache.clearCache(owner.getUUID());
            Slot slot = owner.getSlot(dPlot);
            successful &= slot != null && owner.setSlot(slot, -1);
        }

        successful &= dPlot.setPlotOwner(null);
        successful &= dPlot.setLastActivity(true);
        successful &= dPlot.setStatus(Status.unclaimed);
        successful &= dPlot.setPlotType(PlotType.LOCAL_INSPIRATION_MODE);
        successful &= DataProvider.PLOT.setCompletedSchematic(plot.getId(), null);
        if (!successful) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Failed to abandon plot with the ID " + plot.getId() + "!"));
        }
        return successful;
    }

    /**
     * World a player should be sent to when opening this plot.
     * Finished builds are pasted into the city world on accept, so visits go there instead of
     * recreating the private {@code P-{id}} world.
     */
    public static PlotWorld visitWorld(Plot plot) {
        if (plot.getStatus() == Status.completed) return new CityPlotWorld(plot);
        return plot.getWorld();
    }

    /**
     * Loads the world returned by {@link #visitWorld(Plot)}.
     * When a finished plot's city world does not exist yet, it is created and the stored completed
     * schematic is pasted. If the city world already exists but the plot center column is empty
     * (failed/corrupt paste), the completed schematic is pasted once to recover it.
     * Must be called off the main thread when that city world still has to be generated.
     *
     * @return true when the player can be teleported
     */
    public static boolean prepareVisit(Plot plot) {
        if (plot.getStatus() != Status.completed) return plot.getWorld().prepareWorld();

        CityPlotWorld cityWorld = new CityPlotWorld(plot);
        try {
            if (!cityWorld.isWorldGenerated()) {
                if (!pasteCompletedSchematic(plot, cityWorld)) return false;
            } else if (!cityWorld.loadWorld()) {
                return false;
            } else if (isPlotColumnEmpty(plot, cityWorld) && !pasteCompletedSchematic(plot, cityWorld)) {
                return false;
            }
            return cityWorld.ensureVisitorAccess();
        } catch (Exception exception) {
            PlotSystem.getPlugin().getComponentLogger().error(text(
                    "Could not prepare the city world for finished plot " + plot.getId() + "!"
            ), exception);
            return false;
        }
    }

    /**
     * Pastes the plot's completed schematic into its city world (creating the world if needed).
     * Normalizes legacy/corrupt Y data first. Safe to call when the city world already exists.
     *
     * @return false when the plot has no completed schematic
     */
    public static boolean pasteCompletedSchematic(@NotNull Plot plot) throws Exception {
        return pasteCompletedSchematic(plot, new CityPlotWorld(plot));
    }

    private static boolean pasteCompletedSchematic(Plot plot, CityPlotWorld cityWorld) throws Exception {
        byte[] schematic = getNormalizedCompletedSchematic(plot);
        if (schematic == null) {
            PlotSystem.getPlugin().getComponentLogger().warn(text(
                    "Finished plot " + plot.getId() + " has no completed schematic to paste into the city world!"
            ));
            return false;
        }
        if (!cityWorld.isWorldGenerated()) AbstractPlotLoader.ensureWorldGenerated(cityWorld);
        else if (!cityWorld.loadWorld()) return false;

        // Completed build first, then environment only into fully empty columns (full Y void).
        // That keeps basements / indoor air empty and still surrounds a brand-new CIM plot with terrain.
        AbstractPlotLoader.runFaweBlocking(() ->
                AbstractPlotLoader.pasteSchematic(true, schematic, cityWorld, false, true));
        pasteCityEnvironmentIntoVoid(plot, cityWorld);
        return true;
    }

    /**
     * Pastes the plot's initial environment into a city world, only into XZ columns that are air
     * for the full world height so existing builds and basements are never filled.
     */
    private static void pasteCityEnvironmentIntoVoid(@NotNull Plot plot, @NotNull CityPlotWorld cityWorld) throws Exception {
        byte[] environment = plot.getInitialSchematicBytes();
        if (environment == null || environment.length == 0) return;
        AbstractPlotLoader.runFaweBlocking(() ->
                AbstractPlotLoader.pasteSchematic(true, environment, cityWorld, false, true, true));
    }

    /**
     * True when the plot center column has no solid blocks (void / failed paste).
     */
    private static boolean isPlotColumnEmpty(@NotNull Plot plot, @NotNull CityPlotWorld cityWorld) throws Exception {
        BlockVector3 center = plot.getCenter();
        if (center == null) return true;
        return Utils.supplySync(() -> {
            var world = cityWorld.getBukkitWorld();
            if (world == null) return true;
            return world.getHighestBlockYAt(center.x(), center.z()) < world.getMinHeight();
        }).get();
    }

    /**
     * Pause between world deletions so loading and removing many worlds does not stall the server.
     */
    private static final long FINISHED_WORLD_CLEANUP_PAUSE_MILLIS = 2000L;

    /**
     * Deletes private {@code P-{id}} worlds whose plot is already finished.
     * Worlds that still have players in them are left in place.
     *
     * @param dryRun when true, worlds are counted and left on disk
     * @param limit  maximum number of worlds to delete, or null to scan every match
     */
    public static FinishedWorldCleanup deleteFinishedOnePlotWorlds(boolean dryRun, Integer limit) throws Exception {
        CleanupProgress progress = new CleanupProgress();
        for (MultiverseWorld multiverseWorld : listMultiverseWorlds()) {
            if (progress.interrupted || reachedCleanupLimit(limit, progress.deleted)) break;
            cleanupFinishedWorld(multiverseWorld.getName(), dryRun, progress);
        }
        return progress.toResult();
    }

    private static Collection<MultiverseWorld> listMultiverseWorlds() throws Exception {
        return Utils.supplySync(() -> List.copyOf(DependencyManager.getMultiverseCore().getWorldManager().getWorlds())).get();
    }

    private static boolean reachedCleanupLimit(Integer limit, int deleted) {
        return limit != null && deleted >= limit;
    }

    private static void cleanupFinishedWorld(String worldName, boolean dryRun, CleanupProgress progress) {
        Plot plot = finishedOnePlot(worldName);
        if (plot == null) return;

        try {
            pauseBeforeNextDeletion(dryRun, progress);
            recordCleanupOutcome(worldName, dryRun, progress, removeFinishedWorld(worldName, plot, dryRun));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            progress.interrupted = true;
        } catch (Exception exception) {
            progress.failed++;
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not delete finished plot world " + worldName + "!"), exception);
        }
    }

    private static @Nullable Plot finishedOnePlot(String worldName) {
        if (!worldName.toLowerCase(Locale.ROOT).startsWith("p-")) return null;

        Integer plotId = AlpsUtils.tryParseInt(worldName.substring(2));
        if (plotId == null) return null;

        Plot plot = DataProvider.PLOT.getPlotById(plotId);
        if (plot == null || plot.getStatus() != Status.completed) return null;
        return plot;
    }

    private static void pauseBeforeNextDeletion(boolean dryRun, CleanupProgress progress) throws InterruptedException {
        if (dryRun || !progress.pauseBeforeNextDelete) return;
        Thread.sleep(FINISHED_WORLD_CLEANUP_PAUSE_MILLIS);
    }

    private static Boolean removeFinishedWorld(String worldName, Plot plot, boolean dryRun) throws Exception {
        return Utils.supplySync(() -> deleteOrCountFinishedWorld(worldName, plot, dryRun)).get();
    }

    private static Boolean deleteOrCountFinishedWorld(String worldName, Plot plot, boolean dryRun) {
        PlotWorld world = new PlotWorld(worldName, plot);
        if (worldHasPlayers(world)) return null;
        if (dryRun) return true;
        return world.deleteWorld();
    }

    private static boolean worldHasPlayers(PlotWorld world) {
        return world.isWorldLoaded() && !world.getBukkitWorld().getPlayers().isEmpty();
    }

    private static void recordCleanupOutcome(String worldName, boolean dryRun, CleanupProgress progress, Boolean removed) {
        if (removed == null) {
            progress.skippedOccupied++;
            PlotSystem.getPlugin().getComponentLogger().info(text(
                    "Skipped finished plot world " + worldName + " because players are still in it."
            ));
            return;
        }
        if (!removed) {
            progress.failed++;
            return;
        }

        progress.deleted++;
        progress.pauseBeforeNextDelete = !dryRun;
        PlotSystem.getPlugin().getComponentLogger().info(text(cleanupOutcomeMessage(dryRun, worldName)));
    }

    private static String cleanupOutcomeMessage(boolean dryRun, String worldName) {
        if (dryRun) return "Would delete finished plot world " + worldName + ".";
        return "Deleted finished plot world " + worldName + ".";
    }

    private static final class CleanupProgress {
        private int deleted;
        private int skippedOccupied;
        private int failed;
        private boolean pauseBeforeNextDelete;
        private boolean interrupted;

        private FinishedWorldCleanup toResult() {
            return new FinishedWorldCleanup(deleted, skippedOccupied, failed);
        }
    }

    public record FinishedWorldCleanup(int deleted, int skippedOccupied, int failed) {}

    public static boolean deletePlot(Plot plot) {
        if (!abandonPlot(plot)) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Failed to delete plot with the ID " + plot.getId() + "!"));
            return false;
        }
        if (DataProvider.PLOT.deletePlot(plot.getId())) return true;
        PlotSystem.getPlugin().getComponentLogger().warn(text("Failed to delete plot with the ID " + plot.getId() + " from the database!"));
        return false;
    }

    public static void abandonInactivePlots() {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        long inactivityIntervalDays = config.getLong(ConfigPaths.INACTIVITY_INTERVAL);
        long rejectedInactivityIntervalDays = (config.getLong(ConfigPaths.REJECTED_INACTIVITY_INTERVAL) != -1) ? config.getLong(ConfigPaths.REJECTED_INACTIVITY_INTERVAL) : inactivityIntervalDays;
        if (inactivityIntervalDays == -2 && rejectedInactivityIntervalDays == -2) return;

        for (Plot plot : DataProvider.PLOT.getPlots(Status.unfinished)) {
            LocalDate lastActivity = plot.getLastActivity();
            long interval = plot.isRejected() ? rejectedInactivityIntervalDays : inactivityIntervalDays;
            if (interval == -2 || lastActivity == null || lastActivity.plusDays(interval).isAfter(LocalDate.now())) continue;

            CompletableFuture.runAsync(() -> {
                if (!abandonPlot(plot)) {
                    PlotSystem.getPlugin().getComponentLogger().warn(text("An error occurred while abandoning plot #" + plot.getId() + " due to inactivity!"));
                    return;
                }
                PlotSystem.getPlugin().getComponentLogger().info(text("Abandoned plot #" + plot.getId() + " due to inactivity!"));
            });
        }
    }

    public static void submitPlot(@NotNull Plot plot) {
        plot.setStatus(Status.unreviewed);

        if (plot.getWorld().isWorldLoaded()) {
            for (Player player : plot.getWorld() instanceof OnePlotWorld ? plot.getWorld().getBukkitWorld().getPlayers() : ((CityPlotWorld) plot.getWorld()).getPlayersOnPlot(plot)) {
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

    public static void undoSubmit(@NotNull Plot plot) {
        plot.setStatus(Status.unfinished);

        plot.getPermissions().addBuilderPerms(plot.getPlotOwner().getUUID()).save();
        if (!plot.getPlotMembers().isEmpty()) {
            for (Builder builder : plot.getPlotMembers()) {
                plot.getPermissions().addBuilderPerms(builder.getUUID());
            }
        }
    }

    public static void removePlayerFromGenerationHistory(UUID playerUuid) {
        playerPlotGenerationHistory.remove(playerUuid);
    }

    public static boolean savePlotAsSchematic(@NotNull Plot plot) throws Exception {
        if (!isSchematicSaveSupported(plot)) return false;

        Clipboard initialSchematic = readInitialSchematic(plot);
        if (initialSchematic == null) return false;

        byte[] completedSchematic = createCompletedSchematic(plot, initialSchematic);
        if (!DataProvider.PLOT.setCompletedSchematic(plot.getId(), completedSchematic)) return false;

        copySchematicToCityWorld(plot, completedSchematic);
        return true;
    }

    private static boolean isSchematicSaveSupported(Plot plot) {
        if (plot.getVersion() >= 4) return true;
        PlotSystem.getPlugin().getComponentLogger().error(text("Saving schematics of legacy plots is no longer allowed!"));
        return false;
    }

    private static Clipboard readInitialSchematic(Plot plot) throws IOException {
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(
                new ByteArrayInputStream(plot.getInitialSchematicBytes()))) {
            return reader.read();
        }
    }

    private static byte[] createCompletedSchematic(Plot plot, Clipboard initialSchematic) throws Exception {
        CuboidRegion cuboidRegion = PlotUtils.getPlotAsRegion(plot);
        if (cuboidRegion == null || !plot.getWorld().loadWorld()) return null;

        Polygonal2DRegion region = new Polygonal2DRegion(
                null,
                plot.getOutline(),
                cuboidRegion.getMinimumPoint().y(),
                cuboidRegion.getMaximumPoint().y()
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AbstractPlotLoader.runFaweBlocking(() -> writeCompletedSchematic(plot, initialSchematic, cuboidRegion, region, outputStream));
        return outputStream.toByteArray();
    }

    private static void writeCompletedSchematic(
            Plot plot,
            Clipboard initialSchematic,
            CuboidRegion cuboidRegion,
            Polygonal2DRegion region,
            ByteArrayOutputStream outputStream
    ) throws Exception {
        int cityFloorY = cuboidRegion.getMinimumY();
        int terraMinY = initialSchematic.getRegion().getMinimumY();
        BlockVector3 terraOrigin = BlockVector3.at(plot.getCenter().x(), terraMinY, plot.getCenter().z());

        try (Clipboard clipboard = new BlockArrayClipboard(region)) {
            // Copy from the city/one-plot floor, then restore absolute terra Y. FAWE's transform moves
            // blocks but leaves the origin behind — reset it to the terra min so completed schems match
            // creator format (WEOffsetY = 0) and city re-paste uses the same offset math as initials.
            clipboard.setOrigin(BlockVector3.at(plot.getCenter().x(), cityFloorY, plot.getCenter().z()));
            ForwardExtentCopy copy = new ForwardExtentCopy(
                    new BukkitWorld(plot.getWorld().getBukkitWorld()),
                    region,
                    clipboard,
                    region.getMinimumPoint()
            );
            Operations.complete(copy);

            Clipboard restored = clipboard.transform(new AffineTransform().translate(0, terraMinY - cityFloorY, 0));
            restored.setOrigin(terraOrigin);

            try (ClipboardWriter writer = AbstractPlot.CLIPBOARD_FORMAT.getWriter(outputStream)) {
                writer.write(restored);
            }
        }
    }

    /**
     * Result of aligning a completed schematic to its initial Y frame and creator-style origin.
     *
     * @param changed     whether bytes were rewritten
     * @param fromMinY    completed schematic min Y before repair
     * @param toMinY      completed schematic min Y after repair (initial min Y)
     * @param originFixed whether the paste origin Y was moved onto the schematic min
     */
    public record CompletedSchematicNormalization(boolean changed, int fromMinY, int toMinY, boolean originFixed) {
        public static CompletedSchematicNormalization unchanged(int minY) {
            return new CompletedSchematicNormalization(false, minY, minY, false);
        }
    }

    /**
     * Repairs a completed schematic whose absolute Y or origin drifted from the initial schematic.
     * <p>
     * Legacy saves left the origin on the city floor ({@code WEOffsetY ≠ 0}). Corrupt saves can have
     * block data at the wrong absolute Y. Both are rewritten to creator format (min Y = initial min Y,
     * origin on that min) and persisted when changed.
     *
     * @return normalization details, or {@code null} when the plot has no completed schematic
     */
    public static @Nullable CompletedSchematicNormalization normalizeCompletedSchematic(@NotNull Plot plot) throws Exception {
        byte[] completedBytes = plot.getCompletedSchematic();
        if (completedBytes == null) return null;

        Clipboard initialSchematic = readInitialSchematic(plot);
        if (initialSchematic == null) {
            throw new IOException("Could not read initial schematic for plot #" + plot.getId());
        }

        int initialMinY = initialSchematic.getRegion().getMinimumY();
        Clipboard completed;
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(new ByteArrayInputStream(completedBytes))) {
            completed = reader.read();
        }
        if (completed == null) {
            throw new IOException("Could not read completed schematic for plot #" + plot.getId());
        }

        int completedMinY = completed.getMinimumPoint().y();
        BlockVector3 origin = completed.getOrigin() != null ? completed.getOrigin() : completed.getMinimumPoint();
        boolean minWrong = completedMinY != initialMinY;
        boolean originWrong = origin.y() != completedMinY;
        if (!minWrong && !originWrong) {
            return CompletedSchematicNormalization.unchanged(completedMinY);
        }

        Clipboard fixed = completed;
        if (minWrong) {
            fixed = completed.transform(new AffineTransform().translate(0, initialMinY - completedMinY, 0));
        }
        fixed.setOrigin(BlockVector3.at(origin.x(), initialMinY, origin.z()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ClipboardWriter writer = AbstractPlot.CLIPBOARD_FORMAT.getWriter(outputStream)) {
            writer.write(fixed);
        }
        byte[] repaired = outputStream.toByteArray();
        if (!DataProvider.PLOT.setCompletedSchematic(plot.getId(), repaired)) {
            throw new IOException("Could not persist repaired completed schematic for plot #" + plot.getId());
        }

        PlotSystem.getPlugin().getComponentLogger().info(text(
                "Repaired completed schematic for plot #" + plot.getId()
                        + " (minY " + completedMinY + " -> " + initialMinY
                        + (originWrong ? ", origin reset onto min Y" : "") + ")"
        ));
        return new CompletedSchematicNormalization(true, completedMinY, initialMinY, originWrong || minWrong);
    }

    /**
     * Returns completed schematic bytes after normalizing legacy/corrupt Y data when needed.
     */
    public static byte @Nullable [] getNormalizedCompletedSchematic(@NotNull Plot plot) throws Exception {
        CompletedSchematicNormalization result = normalizeCompletedSchematic(plot);
        if (result == null) return null;
        return plot.getCompletedSchematic();
    }

    private static void copySchematicToCityWorld(Plot plot, byte[] schematic) throws Exception {
        if (plot.getPlotType() == PlotType.CITY_INSPIRATION_MODE) return;

        CityPlotWorld cityPlotWorld = new CityPlotWorld(plot);
        if (!cityPlotWorld.isWorldGenerated()) AbstractPlotLoader.ensureWorldGenerated(cityPlotWorld);
        AbstractPlotLoader.runFaweBlocking(() ->
                AbstractPlotLoader.pasteSchematic(true, schematic, cityPlotWorld, false, true));
        pasteCityEnvironmentIntoVoid(plot, cityPlotWorld);
    }
}
