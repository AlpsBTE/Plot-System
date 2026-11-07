package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.api.event.PlotAbandonedEvent;
import com.alpsbte.plotsystem.api.event.PlotArchivedEvent;
import com.alpsbte.plotsystem.api.event.PlotCreatedEvent;
import com.alpsbte.plotsystem.api.event.PlotSubmissionUndoneEvent;
import com.alpsbte.plotsystem.api.event.PlotSubmittedEvent;
import com.alpsbte.plotsystem.api.model.PlotAbandonReason;
import com.alpsbte.plotsystem.core.integration.PlotEventSnapshots;
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
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
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
        if (loader.isSuccessful()) {
            publishEvent(new PlotCreatedEvent(PlotEventSnapshots.plot(plot)));
        }
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
        return abandonPlot(plot, PlotAbandonReason.MANUAL, null);
    }

    public static boolean abandonPlot(AbstractPlot plot, PlotAbandonReason reason) {
        return abandonPlot(plot, reason, null);
    }

    public static boolean abandonPlot(AbstractPlot plot, PlotAbandonReason reason, UUID actor) {
        // Capture before world cleanup so coordinates and ownership stay intact.
        var snapshot = plot.getPlotType() == PlotType.TUTORIAL
                ? null
                : PlotEventSnapshots.plot(plot);
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

        // Tutorials only tear down a temporary world; they are not archived in the DB
        // and are excluded from PlotSnapshotProvider, so do not publish lifecycle events.
        if (snapshot == null) {
            return true;
        }

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
        if (successful) {
            publishEvent(new PlotArchivedEvent(snapshot, reason, actor));
            publishEvent(new PlotAbandonedEvent(snapshot, reason));
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
     * When a finished plot's city world does not exist yet, it is created and the stored completed schematic is pasted.
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
            }
            return cityWorld.ensureVisitorAccess();
        } catch (Exception exception) {
            PlotSystem.getPlugin().getComponentLogger().error(text(
                    "Could not prepare the city world for finished plot " + plot.getId() + "!"
            ), exception);
            return false;
        }
    }

    private static boolean pasteCompletedSchematic(Plot plot, CityPlotWorld cityWorld) throws Exception {
        byte[] schematic = plot.getCompletedSchematic();
        if (schematic == null) {
            PlotSystem.getPlugin().getComponentLogger().warn(text(
                    "Finished plot " + plot.getId() + " has no completed schematic to paste into the city world!"
            ));
            return false;
        }
        if (!cityWorld.isWorldGenerated()) AbstractPlotLoader.ensureWorldGenerated(cityWorld);
        AbstractPlotLoader.runFaweBlocking(() ->
                AbstractPlotLoader.pasteSchematic(true, schematic, cityWorld, false, true));
        return true;
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
        return deletePlot(plot, null);
    }

    public static boolean deletePlot(Plot plot, UUID actor) {
        if (!abandonPlot(plot, PlotAbandonReason.COMMAND, actor)) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Failed to delete plot with the ID " + plot.getId() + "!"));
            return false;
        }
        if (DataProvider.PLOT.deletePlot(plot.getId())) return true;
        PlotSystem.getPlugin().getComponentLogger().warn(text("Failed to delete plot with the ID " + plot.getId() + " from the database!"));
        return false;
    }

    public static void abandonInactivePlots() {
        for (Plot plot : DataProvider.PLOT.getPlots(Status.unfinished)) {
            LocalDate deadline = plot.getAbandonmentDate();
            if (deadline == null || deadline.isAfter(LocalDate.now())) continue;
            CompletableFuture.runAsync(() -> {
                Plot currentPlot = DataProvider.PLOT.getPlotById(plot.getId());
                if (currentPlot == null) return;
                LocalDate currentDeadline = currentPlot.getAbandonmentDate();
                if (currentDeadline == null || currentDeadline.isAfter(LocalDate.now())) return;
                if (!abandonPlot(currentPlot, PlotAbandonReason.INACTIVITY)) {
                    PlotSystem.getPlugin().getComponentLogger().warn(text("An error occurred while abandoning plot #" + currentPlot.getId() + " due to inactivity!"));
                    return;
                }
                PlotSystem.getPlugin().getComponentLogger().info(text("Abandoned plot #" + currentPlot.getId() + " due to inactivity!"));
            });
        }
    }

    public static void publishEvent(org.bukkit.event.Event event) {
        Utils.runSync(() -> {
            Bukkit.getPluginManager().callEvent(event);
            return null;
        });
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
        publishEvent(new PlotSubmittedEvent(PlotEventSnapshots.plot(plot)));
    }

    public static void undoSubmit(@NotNull Plot plot) {
        plot.setStatus(Status.unfinished);

        plot.getPermissions().addBuilderPerms(plot.getPlotOwner().getUUID()).save();
        if (!plot.getPlotMembers().isEmpty()) {
            for (Builder builder : plot.getPlotMembers()) {
                plot.getPermissions().addBuilderPerms(builder.getUUID());
            }
        }
        publishEvent(new PlotSubmissionUndoneEvent(PlotEventSnapshots.plot(plot)));
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
        try (Clipboard clipboard = new BlockArrayClipboard(region)) {
            clipboard.setOrigin(BlockVector3.at(plot.getCenter().x(), cuboidRegion.getMinimumY(), (double) plot.getCenter().z()));
            ForwardExtentCopy copy = new ForwardExtentCopy(
                    new BukkitWorld(plot.getWorld().getBukkitWorld()),
                    region,
                    clipboard,
                    region.getMinimumPoint()
            );
            Operations.complete(copy);

            try (ClipboardWriter writer = AbstractPlot.CLIPBOARD_FORMAT.getWriter(outputStream)) {
                double offset = initialSchematic.getRegion().getMinimumY() - cuboidRegion.getMinimumY();
                writer.write(clipboard.transform(new AffineTransform().translate(Vector3.at(0, offset, 0))));
            }
        }
    }

    private static void copySchematicToCityWorld(Plot plot, byte[] schematic) throws Exception {
        if (plot.getPlotType() == PlotType.CITY_INSPIRATION_MODE) return;

        CityPlotWorld cityPlotWorld = new CityPlotWorld(plot);
        if (!cityPlotWorld.isWorldGenerated()) AbstractPlotLoader.ensureWorldGenerated(cityPlotWorld);
        AbstractPlotLoader.runFaweBlocking(() ->
                AbstractPlotLoader.pasteSchematic(true, schematic, cityPlotWorld, false, true));
    }
}
