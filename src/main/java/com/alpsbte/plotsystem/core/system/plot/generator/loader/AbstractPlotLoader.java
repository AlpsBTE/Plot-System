package com.alpsbte.plotsystem.core.system.plot.generator.loader;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.plot.generator.world.PlotWorldGenerator;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.text.Component.text;

public abstract class AbstractPlotLoader {
    private static final ConcurrentHashMap<String, Object> WORLD_GENERATION_LOCKS = new ConcurrentHashMap<>();

    protected final AbstractPlot plot;
    protected final PlotType plotType;
    protected final PlotWorld plotWorld;

    protected final Builder builder;
    protected final boolean completionActionsEnabled;

    protected byte[] schematicBytes = null;
    private boolean successful;

    protected AbstractPlotLoader(@NotNull AbstractPlot plot, Builder builder, PlotType plotType, PlotWorld plotWorld) {
        this(plot, builder, plotType, plotWorld, true);
    }

    protected AbstractPlotLoader(
            @NotNull AbstractPlot plot,
            Builder builder,
            PlotType plotType,
            PlotWorld plotWorld,
            boolean completionActionsEnabled
    ) {
        this.plot = plot;
        this.plotType = plotType;
        this.plotWorld = plotWorld;
        this.builder = builder;
        this.completionActionsEnabled = completionActionsEnabled;

        PlotSystem.getPlugin().getComponentLogger().info("Loading plot #{}...", plot.getId());
        PlotSystem.getPlugin().getComponentLogger().info("Plot Type: {}", plotType.name());

        successful = true;
        try {
            generateWorld();
            loadWorld();
            fetchSchematicData();
            createPlotProtection();
            generateStructure();
        } catch (Exception e) {
            successful = false;
            onException(e);
        }

        if (successful) onCompletion();
    }

    protected void generateWorld() throws Exception {
        ensureWorldGenerated(plotWorld);
    }

    public static void ensureWorldGenerated(@NotNull PlotWorld world) throws Exception {
        if (Utils.supplySync(world::isWorldGenerated).get()) return;

        Object lock = WORLD_GENERATION_LOCKS.computeIfAbsent(world.getWorldName(), ignored -> new Object());
        try {
            synchronized (lock) {
                if (!Utils.supplySync(world::isWorldGenerated).get()) {
                    new PlotWorldGenerator(world.getWorldName());
                }
            }
        } finally {
            WORLD_GENERATION_LOCKS.remove(world.getWorldName(), lock);
        }
    }

    protected void loadWorld() throws Exception {
        Utils.runSync(() -> {
            if (plotWorld.isWorldLoaded()) return null;

            boolean successful = plotWorld.loadWorld();
            if (!successful) throw new Exception("Could not load world!");
            return null;
        }).get();
    }

    protected void fetchSchematicData() {
        this.schematicBytes = plot.getInitialSchematicBytes();
    }

    protected void createPlotProtection() throws Exception {
        Utils.runSync(() -> {
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(plotWorld.getBukkitWorld()));
            if (regionManager == null) {
                PlotSystem.getPlugin().getComponentLogger().warn(text("Region Manager is null!"));
                return null;
            }

            int maxWorldHeight = plotWorld.getBukkitWorld().getMaxHeight() - 1;
            // Create build region for plot from the outline of the plot
            ProtectedRegion protectedBuildRegion = new ProtectedPolygonalRegion(plotWorld.getRegionName(), plot.getOutline(), PlotWorld.MIN_WORLD_HEIGHT, maxWorldHeight);
            protectedBuildRegion.setPriority(100);

            // Create protected plot region for plot
            ProtectedRegion protectedRegion = getProtectedRegion();

            // Add plot owner
            DefaultDomain owner = protectedBuildRegion.getOwners();
            owner.addPlayer(builder.getUUID());
            protectedBuildRegion.setOwners(owner);

            // Set protected build region permissions
            setBuildRegionPermissions(protectedBuildRegion);

            // Set protected region permissions
            setRegionPermissions(protectedRegion);

            // Add regions and save changes
            if (regionManager.hasRegion(plotWorld.getRegionName())) regionManager.removeRegion(plotWorld.getRegionName());
            if (regionManager.hasRegion(plotWorld.getRegionName() + "-1")) regionManager.removeRegion(plotWorld.getRegionName() + "-1");
            regionManager.addRegion(protectedBuildRegion);
            regionManager.addRegion(protectedRegion);
            regionManager.saveChanges();
            return null;
        }).get();
    }

    private @NotNull ProtectedRegion getProtectedRegion() {
        World weWorld = new BukkitWorld(plotWorld.getBukkitWorld());
        int maxWorldHeight = plotWorld.getBukkitWorld().getMaxHeight() - 1;
        CylinderRegion cylinderRegion = new CylinderRegion(weWorld, plot.getCenter(), Vector2.at(PlotWorld.PLOT_SIZE, PlotWorld.PLOT_SIZE), PlotWorld.MIN_WORLD_HEIGHT, maxWorldHeight);
        ProtectedRegion protectedRegion = new ProtectedPolygonalRegion(plotWorld.getRegionName() + "-1", cylinderRegion.polygonize(-1), PlotWorld.MIN_WORLD_HEIGHT, maxWorldHeight);
        protectedRegion.setPriority(50);
        return protectedRegion;
    }

    /**
     * Sets the permissions for the plot build region only
     *
     * @param region build region
     */
    protected void setBuildRegionPermissions(@NotNull ProtectedRegion region) {
        region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
        region.setFlag(Flags.BUILD.getRegionGroupFlag(), RegionGroup.OWNERS);
        if (DependencyManager.isWorldGuardExtraFlagsEnabled())
            region.setFlag(new StateFlag("worldedit", true, RegionGroup.OWNERS), StateFlag.State.ALLOW);
    }

    /**
     * Sets the permissions for the whole plot region
     *
     * @param region plot region
     */
    protected void setRegionPermissions(@NotNull ProtectedRegion region) {
        region.setFlag(Flags.ENTRY, StateFlag.State.ALLOW);
        region.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

        FileConfiguration config = ConfigUtil.getInstance().configs[1];
        region.setFlag(Flags.BLOCKED_CMDS, new HashSet<>(getBlockedCommands(config)));
        region.setFlag(Flags.BLOCKED_CMDS.getRegionGroupFlag(), RegionGroup.ALL);
    }

    /**
     * Reads the blocked commands for the plot region from the config
     *
     * @param config commands.yml config
     * @return list of blocked commands
     */
    protected List<String> getBlockedCommands(@NotNull FileConfiguration config) {
        List<String> blockedCommands = config.getStringList(ConfigPaths.BLOCKED_COMMANDS_BUILDERS);
        blockedCommands.removeIf(c -> c.equals("/cmd1"));
        return blockedCommands;
    }

    protected void generateStructure() throws Exception {
        runFaweAsync(() -> {
            if (plotType.hasEnvironment()) {
                pasteSchematic(null, this.schematicBytes, this.plotWorld, false, false);
            } else {
                Mask airMask = new BlockTypeMask(BukkitAdapter.adapt(this.plotWorld.getBukkitWorld()), BlockTypes.AIR);
                pasteSchematic(airMask, PlotUtils.getOutlinesSchematicBytes(plot, this.schematicBytes), this.plotWorld, true, false);
            }
        }).get();
    }

    /**
     * Runs a WorldEdit operation on FAWE's asynchronous task executor.
     */
    public static CompletableFuture<Void> runFaweAsync(@NotNull FaweTask task) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        FaweAPI.getTaskManager().async(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    /**
     * Pastes the schematic to the plot center in the given world
     *
     * @param pasteMask     - sets a mask for the paste operation, can be null - if the mast is not null, the paste operation ignores air blocks
     * @param schematicFile - plot/environment schematic file
     * @param world         - world to paste in
     * @param clearArea     - clears the plot area with air before pasting
     * @param offset        - offset for the paste operation
     */
    public static void pasteSchematic(@Nullable Mask pasteMask, byte[] schematicFile, @NotNull PlotWorld world, boolean clearArea, boolean offset) throws IOException {
        // load world if not loaded already
        if (!world.loadWorld()) return;
        World weWorld = new BukkitWorld(world.getBukkitWorld());

        // set outline region with air
        if (clearArea) {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world.getBukkitWorld()))) {
                Polygonal2DRegion polyRegion = new Polygonal2DRegion(
                        weWorld,
                        world.getPlot().getOutline(),
                        world.getBukkitWorld().getMinHeight(),
                        world.getBukkitWorld().getMaxHeight() - 1
                );
                editSession.setMask(new RegionMask(polyRegion));
                editSession.setBlocks((Region) polyRegion, Objects.requireNonNull(BlockTypes.AIR).getDefaultState());
            }
        }

        // load schematic
        Clipboard clipboard;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(schematicFile);
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(inputStream)) {
            clipboard = reader.read();
        }

        int pasteY = world.getPlotHeight();

        if (offset) {
            BlockVector3 clipboardOrigin = clipboard.getOrigin();
            if (clipboardOrigin == null) clipboardOrigin = clipboard.getMinimumPoint();
            pasteY = world.getPlotHeight() - clipboard.getMinimumPoint().y() + clipboardOrigin.y();
        }

        // paste schematic
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world.getBukkitWorld()))) {
            if (pasteMask != null) editSession.setMask(pasteMask);
            Operation clipboardHolder = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(world.getPlot().getCenter().x(), pasteY, world.getPlot().getCenter().z()))
                    .ignoreAirBlocks(pasteMask != null)
                    .build();
            Operations.complete(clipboardHolder);
        }
    }

    protected void onException(Exception e) {
        try {
            if (!PlotHandler.abandonPlot(this.plot)) {
                PlotSystem.getPlugin().getComponentLogger().error("Failed to clean up plot #{} after generation error!", plot.getId());
            }
        } catch (Exception ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Failed to clean up plot after generation error!"), ex);
        }

        PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while generating plot!"), e);
        Utils.runSync(() -> {
            if (builder != null && builder.getPlayer() != null) {
                builder.getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(builder.getPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
                builder.getPlayer().playSound(builder.getPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
            }
            return null;
        });
    }

    public boolean isSuccessful() {
        return successful;
    }

    @FunctionalInterface
    public interface FaweTask {
        void run() throws Exception;
    }

    protected abstract void onCompletion();
}
