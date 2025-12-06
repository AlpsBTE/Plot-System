package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
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
import com.sk89q.worldguard.protection.managers.storage.StorageException;
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

import static net.kyori.adventure.text.Component.text;

public abstract class AbstractPlotGenerator {
    protected final AbstractPlot plot;
    private final Builder builder;
    protected final PlotWorld world;
    protected final double plotVersion;
    protected final PlotType plotType;

    /**
     * Generates a new plot in the plot world
     *
     * @param plot    - plot which should be generated
     * @param builder - builder of the plot
     */
    protected AbstractPlotGenerator(@NotNull AbstractPlot plot, @NotNull Builder builder) {
        this(plot, builder, builder.getPlotType());
    }

    /**
     * Generates a new plot in the given world
     *
     * @param plot     - plot which should be generated
     * @param builder  - builder of the plot
     * @param plotType - type of the plot
     */
    protected AbstractPlotGenerator(@NotNull AbstractPlot plot, @NotNull Builder builder, @NotNull PlotType plotType) {
        this(plot, builder, plotType, plot.getVersion() <= 2 || plotType.hasOnePlotPerWorld() ? new OnePlotWorld(plot) : new CityPlotWorld((Plot) plot));
    }

    /**
     * Generates a new plot in the given world
     *
     * @param plot    - plot which should be generated
     * @param builder - builder of the plot
     * @param world   - world of the plot
     */
    private AbstractPlotGenerator(@NotNull AbstractPlot plot, @NotNull Builder builder, @NotNull PlotType plotType, @NotNull PlotWorld world) {
        this.plot = plot;
        this.builder = builder;
        this.world = world;
        this.plotVersion = plot.getVersion();
        this.plotType = plotType;

        if (init()) {
            Exception exception = null;
            try {
                if (plotType.hasOnePlotPerWorld() || !world.isWorldGenerated()) {
                    new PlotWorldGenerator(world.getWorldName());
                } else if (!world.isWorldLoaded() && !world.loadWorld()) throw new Exception("Could not load world");
                generateOutlines();
                createPlotProtection();
            } catch (Exception ex) {
                exception = ex;
            }
            this.onComplete(exception != null, false);

            if (exception != null) {
                PlotUtils.Actions.abandonPlot(plot);
                onException(exception);
            }
        }
    }


    /**
     * Executed before plot generation
     *
     * @return true if initialization was successful
     */
    protected abstract boolean init();


    /**
     * Generates plot schematic and outlines
     */
    protected void generateOutlines() throws IOException {
        if (plotVersion >= 3 && plotType.hasEnvironment()) {
            pasteSchematic(null, plot.getInitialSchematicBytes(), world, false, false);
        } else {
            Mask airMask = new BlockTypeMask(BukkitAdapter.adapt(world.getBukkitWorld()), BlockTypes.AIR);
            pasteSchematic(airMask, PlotUtils.getOutlinesSchematicBytes(plot, world.getBukkitWorld()), world, true, false);
        }
    }


    /**
     * Creates plot protection
     */
    protected void createPlotProtection() throws StorageException {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world.getBukkitWorld()));

        if (regionManager != null) {
            // Create build region for plot from the outline of the plot
            ProtectedRegion protectedBuildRegion = new ProtectedPolygonalRegion(world.getRegionName(), plot.getOutline(), PlotWorld.MIN_WORLD_HEIGHT, PlotWorld.MAX_WORLD_HEIGHT);
            protectedBuildRegion.setPriority(100);

            // Create protected plot region for plot
            World weWorld = new BukkitWorld(world.getBukkitWorld());
            CylinderRegion cylinderRegion = new CylinderRegion(weWorld, plot.getCenter(), Vector2.at(PlotWorld.PLOT_SIZE, PlotWorld.PLOT_SIZE), PlotWorld.MIN_WORLD_HEIGHT, PlotWorld.MAX_WORLD_HEIGHT);
            ProtectedRegion protectedRegion = new ProtectedPolygonalRegion(world.getRegionName() + "-1", cylinderRegion.polygonize(-1), PlotWorld.MIN_WORLD_HEIGHT, PlotWorld.MAX_WORLD_HEIGHT);
            protectedRegion.setPriority(50);

            // Add plot owner
            DefaultDomain owner = protectedBuildRegion.getOwners();
            owner.addPlayer(builder.getUUID());
            protectedBuildRegion.setOwners(owner);

            // Set protected build region permissions
            setBuildRegionPermissions(protectedBuildRegion);

            // Set protected region permissions
            setRegionPermissions(protectedRegion);

            // Add regions and save changes
            if (regionManager.hasRegion(world.getRegionName())) regionManager.removeRegion(world.getRegionName());
            if (regionManager.hasRegion(world.getRegionName() + "-1")) regionManager.removeRegion(world.getRegionName() + "-1");
            regionManager.addRegion(protectedBuildRegion);
            regionManager.addRegion(protectedRegion);
            regionManager.saveChanges();
        } else PlotSystem.getPlugin().getComponentLogger().warn(text("Region Manager is null!"));
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

    /**
     * Gets invoked when generation is completed
     *
     * @param failed      - true if generation has failed
     * @param unloadWorld - try to unload world after generation
     */
    protected void onComplete(boolean failed, boolean unloadWorld) {
        // Unload plot world if it is not needed anymore
        if (unloadWorld) world.unloadWorld(false);
    }


    /**
     * Gets invoked when an exception has occurred
     *
     * @param ex - caused exception
     */
    protected void onException(Throwable ex) {
        PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while generating plot!"), ex);
        builder.getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(builder.getPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
        builder.getPlayer().playSound(builder.getPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
    }


    /**
     * @return - plot object
     */
    public AbstractPlot getPlot() {
        return plot;
    }


    /**
     * @return - builder object
     */
    public Builder getBuilder() {
        return builder;
    }


    /**
     * Pastes the schematic to the plot center in the given world
     *
     * @param pasteMask     - sets a mask for the paste operation, can be null
     * @param schematicFile - plot/environment schematic file
     * @param world         - world to paste in
     * @param clearArea     - clears the plot area with air before pasting
     * @param offset        - offset for the paste operation
     */
    public static void pasteSchematic(@Nullable Mask pasteMask, byte[] schematicFile, @NotNull PlotWorld world, boolean clearArea, boolean offset) throws IOException {
        // load world
        if (!world.loadWorld()) return;
        World weWorld = new BukkitWorld(world.getBukkitWorld());

        // set outline region with air
        if (clearArea) {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world.getBukkitWorld()))) {
                Polygonal2DRegion polyRegion = new Polygonal2DRegion(weWorld, world.getPlot().getOutline(), 0, PlotWorld.MAX_WORLD_HEIGHT);
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
                    .build();
            Operations.complete(clipboardHolder);
        }
    }
}
