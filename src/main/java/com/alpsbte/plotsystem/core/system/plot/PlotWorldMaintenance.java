package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.AbstractPlotLoader;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

/**
 * Admin maintenance for leftover finished {@code P-} worlds and rebuilding CIM {@code C-} worlds.
 */
public final class PlotWorldMaintenance {
    private static final long PAUSE_MILLIS = 2000L;
    private static final AtomicBoolean RUNNING = new AtomicBoolean();

    private PlotWorldMaintenance() {}

    public static boolean tryBegin() {
        return RUNNING.compareAndSet(false, true);
    }

    public static void end() {
        RUNNING.set(false);
    }

    /**
     * Deletes private {@code P-{id}} worlds whose plot is already finished.
     * Worlds that still have players in them are left in place.
     *
     * @param dryRun when true, worlds are counted and left on disk
     * @param limit  maximum number of matching worlds to process (success, skip, or fail), or null for all
     */
    public static FinishedWorldCleanup deleteFinishedOnePlotWorlds(boolean dryRun, @Nullable Integer limit) throws Exception {
        CleanupProgress progress = new CleanupProgress();
        for (MultiverseWorld multiverseWorld : listMultiverseWorlds()) {
            if (progress.interrupted || reachedLimit(limit, progress.processed())) break;
            cleanupFinishedWorld(multiverseWorld.getName(), dryRun, progress);
        }
        return progress.toResult();
    }

    /**
     * Deletes and recreates one or all city inspiration worlds, then re-pastes every completed plot.
     * Fails before any change when a city still has unfinished/unreviewed CIM plots — rebuilding
     * would wipe those in-progress builds from the shared city world.
     *
     * @param cityId city project id, or {@code null} for every city that has completed plots / a CIM world
     * @param dryRun when true, only report what would happen (still fails on in-progress CIM plots)
     */
    public static CimRebuildResult rebuildCityInspirationWorlds(@Nullable String cityId, boolean dryRun) throws Exception {
        List<CityProject> cities = resolveCitiesForCimRebuild(cityId);
        List<Plot> inProgress = findInProgressCimPlots(cities);
        if (!inProgress.isEmpty()) {
            String ids = inProgress.stream()
                    .map(plot -> "#" + plot.getId())
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException(
                    "Cannot rebuild CIM: in-progress CIM plot(s) would be wiped from the city world: "
                            + ids + ". Finish, abandon, or move them first."
            );
        }

        CimRebuildProgress progress = new CimRebuildProgress();
        boolean pauseBeforeNext = false;
        for (CityProject city : cities) {
            if (progress.interrupted) break;
            pauseBeforeNext = rebuildOneCity(city, dryRun, progress, pauseBeforeNext);
        }
        return progress.toResult();
    }

    private static List<CityProject> resolveCitiesForCimRebuild(@Nullable String cityId) {
        if (cityId != null) {
            CityProject city = DataProvider.CITY_PROJECT.getById(cityId).orElse(null);
            if (city == null) throw new IllegalArgumentException("Unknown city project id: " + cityId);
            return List.of(city);
        }
        List<CityProject> cities = new ArrayList<>();
        for (CityProject city : DataProvider.CITY_PROJECT.get(false)) {
            if (!DataProvider.PLOT.getPlots(city, Status.completed).isEmpty()
                    || new PlotWorld(cityWorldName(city), null).isWorldGenerated()) {
                cities.add(city);
            }
        }
        return cities;
    }

    private static List<Plot> findInProgressCimPlots(List<CityProject> cities) {
        List<Plot> inProgress = new ArrayList<>();
        for (CityProject city : cities) {
            for (Plot plot : DataProvider.PLOT.getPlots(city, Status.unfinished, Status.unreviewed)) {
                if (plot.getPlotType() == PlotType.CITY_INSPIRATION_MODE) {
                    inProgress.add(plot);
                }
            }
        }
        return inProgress;
    }

    private static boolean rebuildOneCity(
            CityProject city,
            boolean dryRun,
            CimRebuildProgress progress,
            boolean pauseBeforeNext
    ) throws InterruptedException {
        String worldName = cityWorldName(city);
        List<Plot> completedPlots = DataProvider.PLOT.getPlots(city, Status.completed);
        PlotWorld cityWorld = new PlotWorld(worldName, completedPlots.isEmpty() ? null : completedPlots.get(0));

        if (dryRun) {
            progress.citiesProcessed++;
            progress.plotsWouldPaste += completedPlots.size();
            if (cityWorld.isWorldGenerated()) progress.worldsWouldDelete++;
            PlotSystem.getPlugin().getComponentLogger().info(text(
                    "Would rebuild CIM world " + worldName + " with " + completedPlots.size() + " completed plot(s)."
            ));
            return pauseBeforeNext;
        }

        try {
            if (pauseBeforeNext) Thread.sleep(PAUSE_MILLIS);
            if (!deleteCityWorld(cityWorld, worldName, progress)) return true;
            progress.worldsDeleted++;

            if (completedPlots.isEmpty()) {
                progress.citiesProcessed++;
                return true;
            }

            // Recreate the empty city world, then paste builds, then environment into leftover void.
            CityPlotWorld recreated = new CityPlotWorld(completedPlots.get(0));
            AbstractPlotLoader.ensureWorldGenerated(recreated);

            boolean pastedAny = false;
            for (Plot plot : completedPlots) {
                if (pastedAny) Thread.sleep(PAUSE_MILLIS);
                if (PlotHandler.pasteCompletedIntoCity(plot, false)) {
                    progress.plotsPasted++;
                    pastedAny = true;
                    PlotSystem.getPlugin().getComponentLogger().info(text(
                            "Rebuilt completed plot #" + plot.getId() + " into CIM world " + worldName + "."
                    ));
                } else {
                    progress.plotsFailed++;
                }
            }
            for (Plot plot : completedPlots) {
                if (pastedAny) Thread.sleep(PAUSE_MILLIS);
                PlotHandler.pasteCityEnvironmentIntoVoid(plot);
                pastedAny = true;
            }
            progress.citiesProcessed++;
            return true;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            progress.interrupted = true;
            throw interrupted;
        } catch (Exception exception) {
            progress.citiesFailed++;
            PlotSystem.getPlugin().getComponentLogger().error(
                    text("Could not rebuild CIM world " + worldName + "!"),
                    exception
            );
            return true;
        }
    }

    private static boolean deleteCityWorld(PlotWorld cityWorld, String worldName, CimRebuildProgress progress) throws Exception {
        if (!cityWorld.isWorldGenerated()) return true;
        Boolean removed = Utils.supplySync(() -> {
            if (worldHasPlayers(cityWorld)) {
                for (Player player : List.copyOf(cityWorld.getBukkitWorld().getPlayers())) {
                    player.teleport(Utils.getSpawnLocation());
                }
            }
            return cityWorld.deleteWorld();
        }).get();
        if (Boolean.TRUE.equals(removed)) return true;
        progress.citiesFailed++;
        PlotSystem.getPlugin().getComponentLogger().warn(text(
                "Could not delete CIM world " + worldName + " for rebuild (deleteWorld returned false)."
        ));
        return false;
    }

    private static String cityWorldName(CityProject city) {
        return "C-" + city.getId();
    }

    private static Collection<MultiverseWorld> listMultiverseWorlds() throws Exception {
        return Utils.supplySync(() -> List.copyOf(DependencyManager.getMultiverseCore().getWorldManager().getWorlds())).get();
    }

    private static boolean reachedLimit(@Nullable Integer limit, int processed) {
        return limit != null && processed >= limit;
    }

    private static void cleanupFinishedWorld(String worldName, boolean dryRun, CleanupProgress progress) {
        Plot plot = finishedOnePlot(worldName);
        if (plot == null) return;

        try {
            if (!dryRun && progress.pauseBeforeNextDelete) Thread.sleep(PAUSE_MILLIS);
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

    private static Boolean removeFinishedWorld(String worldName, Plot plot, boolean dryRun) throws Exception {
        return Utils.supplySync(() -> deleteOrCountFinishedWorld(worldName, plot, dryRun)).get();
    }

    private static Boolean deleteOrCountFinishedWorld(String worldName, Plot plot, boolean dryRun) {
        PlotWorld world = new PlotWorld(worldName, plot);
        if (worldHasPlayers(world)) return null;
        if (dryRun) return true;
        if (!world.isWorldGenerated()) {
            PlotSystem.getPlugin().getComponentLogger().warn(text(
                    "Could not delete finished plot world " + worldName + " because Multiverse does not have it registered."
            ));
            return false;
        }
        if (!world.loadWorld()) {
            PlotSystem.getPlugin().getComponentLogger().warn(text(
                    "Could not delete finished plot world " + worldName + " because it failed to load."
            ));
            return false;
        }
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
            PlotSystem.getPlugin().getComponentLogger().warn(text(
                    "Failed to delete finished plot world " + worldName + " (deleteWorld returned false)."
            ));
            return;
        }

        progress.deleted++;
        progress.pauseBeforeNextDelete = !dryRun;
        PlotSystem.getPlugin().getComponentLogger().info(text(
                dryRun ? "Would delete finished plot world " + worldName + "."
                        : "Deleted finished plot world " + worldName + "."
        ));
    }

    private static final class CleanupProgress {
        private int deleted;
        private int skippedOccupied;
        private int failed;
        private boolean pauseBeforeNextDelete;
        private boolean interrupted;

        private int processed() {
            return deleted + skippedOccupied + failed;
        }

        private FinishedWorldCleanup toResult() {
            return new FinishedWorldCleanup(deleted, skippedOccupied, failed);
        }
    }

    private static final class CimRebuildProgress {
        private int citiesProcessed;
        private int citiesFailed;
        private int worldsDeleted;
        private int worldsWouldDelete;
        private int plotsPasted;
        private int plotsWouldPaste;
        private int plotsFailed;
        private boolean interrupted;

        private CimRebuildResult toResult() {
            return new CimRebuildResult(
                    citiesProcessed,
                    citiesFailed,
                    worldsDeleted,
                    worldsWouldDelete,
                    plotsPasted,
                    plotsWouldPaste,
                    plotsFailed
            );
        }
    }

    public record FinishedWorldCleanup(int deleted, int skippedOccupied, int failed) {}

    public record CimRebuildResult(
            int citiesProcessed,
            int citiesFailed,
            int worldsDeleted,
            int worldsWouldDelete,
            int plotsPasted,
            int plotsWouldPaste,
            int plotsFailed
    ) {}
}
