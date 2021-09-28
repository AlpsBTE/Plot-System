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

package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.config.ConfigPaths;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Stream;

public abstract class AbstractPlotGenerator {

    private static final MVWorldManager worldManager = PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager();
    private WorldCreator worldCreator;

    private final Plot plot;
    private final Builder builder;

    /**
     * @param plot - plot which should be generated
     * @param builder - builder of the plot
     */
    public AbstractPlotGenerator(@NotNull Plot plot, @NotNull Builder builder) {
        this.plot = plot;
        this.builder = builder;

        try {
            if (init().get()) {
                 CompletableFuture<Boolean> a = configureWorldGeneration(), b = generateWorld(), c = generateOutlines(plot.getOutlinesSchematic()),
                         d = configureWorld(worldManager.getMVWorld(plot.getPlotWorld())), e = createProtection();

                CompletableFuture<Void> plotGen = CompletableFuture.allOf(a, b, c, d, e);
                plotGen.thenRun(() -> {
                    AtomicBoolean completedExceptionally = new AtomicBoolean(false);
                    Stream.of(a, b, c, d, e).forEach(f -> f.handle((value, ex) -> {
                        if (!value || ex != null) {
                            completedExceptionally.set(true);
                            onException(ex != null ? ex : new RuntimeException("Generator completed exceptionally"));
                            if (worldManager.isMVWorld(plot.getPlotWorld())) worldManager.deleteWorld(plot.getWorldName(), true, true);
                            plotGen.completeExceptionally(ex);
                        }
                        return value;
                    }));

                    try {
                        this.onComplete(completedExceptionally.get());
                    } catch (SQLException ex) { PlotHandler.abandonPlot(plot); onException(ex); }
                });
            }
        } catch (InterruptedException | ExecutionException ex) { onException(ex); }
    }

    /**
     * Executed before plot generation
     * @return - true if plot should be generated
     */
    protected abstract CompletableFuture<Boolean> init();

    /**
     * Configures plot world generation
     * @return - true if configuration was successful
     */
    protected CompletableFuture<Boolean> configureWorldGeneration() {
        worldCreator = new WorldCreator(plot.getWorldName());
        worldCreator.environment(org.bukkit.World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generatorSettings("2;0;1;");
        worldCreator.createWorld();

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Generates plot world
     * @return - true if generation was successful
     */
    protected CompletableFuture<Boolean> generateWorld() {
        // Check if world creator is configured and add new world to multiverse world manager
        if (worldCreator != null) {
            if (worldManager.isMVWorld(plot.getPlotWorld())) worldManager.deleteWorld(plot.getWorldName(), true, true);
            worldManager.addWorld(plot.getWorldName(), worldCreator.environment(), null, worldCreator.type(), false, "VoidGen");
        } else {
            Bukkit.getLogger().log(Level.SEVERE, "World Creator is not configured!");
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Generates plot schematic and outlines
     * @param plotSchematic - schematic file
     * @return - true if generation was successful
     */
    protected CompletableFuture<Boolean> generateOutlines(File plotSchematic) {
        try {
            if (plotSchematic != null) {
                Vector buildingOutlinesCoordinates = PlotManager.getPlotCenter(plot);

                com.sk89q.worldedit.world.World weWorld = new BukkitWorld(plot.getPlotWorld());
                Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(plotSchematic)).read(weWorld.getWorldData());
                clipboard.setOrigin(clipboard.getOrigin().setY(clipboard.getMinimumPoint().getY())); // Set origin point to the center bottom of the schematic

                ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard, weWorld.getWorldData());
                EditSession editSession = PlotSystem.DependencyManager.getWorldEdit().getEditSessionFactory().getEditSession(weWorld, -1);

                Operation operation = clipboardHolder.createPaste(editSession, weWorld.getWorldData()).to(buildingOutlinesCoordinates).ignoreAirBlocks(false).build();
                Operations.complete(operation);
                editSession.flushQueue();

                return CompletableFuture.completedFuture(true);
            }
        } catch (IOException | WorldEditException ex) {
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Configures plot world
     * @param mvWorld - plot world
     * @return - true if configuration was successful
     */
    protected CompletableFuture<Boolean> configureWorld(@NotNull MultiverseWorld mvWorld) {
        // Set Bukkit world game rules
        plot.getPlotWorld().setGameRuleValue("randomTickSpeed", "0");
        plot.getPlotWorld().setGameRuleValue("doDaylightCycle", "false");
        plot.getPlotWorld().setGameRuleValue("doFireTick", "false");
        plot.getPlotWorld().setGameRuleValue("doWeatherCycle", "false");
        plot.getPlotWorld().setGameRuleValue("keepInventory", "true");
        plot.getPlotWorld().setGameRuleValue("announceAdvancements", "false");

        // Set world time to midday
        plot.getPlotWorld().setTime(6000);

        // Configure multiverse world
        mvWorld.setSpawnLocation(PlotHandler.getPlotSpawnPoint(getPlot()));
        mvWorld.setAdjustSpawn(false);
        mvWorld.setAllowFlight(true);
        mvWorld.setGameMode(GameMode.CREATIVE);
        mvWorld.setEnableWeather(false);
        mvWorld.setDifficulty(Difficulty.PEACEFUL);
        mvWorld.setAllowAnimalSpawn(false);
        mvWorld.setAllowMonsterSpawn(false);
        mvWorld.setAutoLoad(false);
        mvWorld.setKeepSpawnInMemory(false);

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Creates plot protection
     * @return - true if protection was created successful
     */
    protected CompletableFuture<Boolean> createProtection() {
        BlockVector min = BlockVector.toBlockPoint(0, 1, 0);
        BlockVector max = BlockVector.toBlockPoint(PlotManager.getPlotSize(plot), 256, PlotManager.getPlotSize(plot));

        RegionContainer container = PlotSystem.DependencyManager.getWorldGuard().getRegionContainer();
        RegionManager regionManager = container.get(plot.getPlotWorld());

        // Create protected region for world
        GlobalProtectedRegion globalRegion = new GlobalProtectedRegion("__global__");
        globalRegion.setFlag(DefaultFlag.ENTRY, StateFlag.State.DENY);
        globalRegion.setFlag(DefaultFlag.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

        // Create protected region for plot
        ProtectedRegion protectedPlotRegion = new ProtectedCuboidRegion(plot.getWorldName(), min, max);
        protectedPlotRegion.setPriority(100);

        // Add and save regions
        try {
            if (regionManager != null) {
                regionManager.addRegion(globalRegion);
                regionManager.addRegion(protectedPlotRegion);
                regionManager.saveChanges();
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "Region Manager is null!");
                return CompletableFuture.completedFuture(false);
            }
        } catch (StorageException ex) {
            return CompletableFuture.completedFuture(false);
        }


        // Add plot owner
        DefaultDomain owner = protectedPlotRegion.getOwners();
        owner.addPlayer(builder.getUUID());
        protectedPlotRegion.setOwners(owner);


        // Set permissions
        protectedPlotRegion.setFlag(DefaultFlag.PASSTHROUGH, StateFlag.State.ALLOW);
        protectedPlotRegion.setFlag(DefaultFlag.PASSTHROUGH.getRegionGroupFlag(), RegionGroup.OWNERS);

        protectedPlotRegion.setFlag(DefaultFlag.ENTRY, StateFlag.State.ALLOW);
        protectedPlotRegion.setFlag(DefaultFlag.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

        FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getCommandsConfig();

        List<String> allowedCommandsNonBuilder = config.getStringList(ConfigPaths.ALLOWED_COMMANDS_NON_BUILDERS);
        allowedCommandsNonBuilder.removeIf(c -> c.equals("/cmd1"));
        for (BaseCommand baseCommand : PlotSystem.getPlugin().getCommandManager().getBaseCommands()) {
            allowedCommandsNonBuilder.addAll(Arrays.asList(baseCommand.getNames()));
            for (String command : baseCommand.getNames()) {
                allowedCommandsNonBuilder.add("/" + command);
            }
        }

        List<String> blockedCommandsBuilders = config.getStringList(ConfigPaths.BLOCKED_COMMANDS_BUILDERS);
        blockedCommandsBuilders.removeIf(c -> c.equals("/cmd1"));

        protectedPlotRegion.setFlag(DefaultFlag.BLOCKED_CMDS, new HashSet<>(blockedCommandsBuilders));
        protectedPlotRegion.setFlag(DefaultFlag.BLOCKED_CMDS.getRegionGroupFlag(), RegionGroup.OWNERS);

        protectedPlotRegion.setFlag(DefaultFlag.ALLOWED_CMDS, new HashSet<>(allowedCommandsNonBuilder));
        protectedPlotRegion.setFlag(DefaultFlag.ALLOWED_CMDS.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Gets invoked when generation is completed
     * @param failed - true if generation has failed
     * @throws SQLException - caused by a database exception
     */
    protected void onComplete(boolean failed) throws SQLException {
        if (!failed) {
            builder.setPlot(plot.getID(), builder.getFreeSlot());
            plot.setStatus(Status.unfinished);
            plot.setPlotOwner(builder.getPlayer().getUniqueId().toString());
        }
    }

    /**
     * Gets invoked when an exception has occurred
     * @param ex - caused exception
     */
    protected void onException(Throwable ex) {
        Bukkit.getLogger().log(Level.SEVERE, "An error occurred while generating plot!", ex);
        builder.getPlayer().sendMessage(Utils.getErrorMessageFormat("An error occurred while generating plot! Please try again!"));
        builder.getPlayer().playSound(builder.getPlayer().getLocation(), Utils.ErrorSound,1,1);
    }

    /**
     * @return - plot object
     */
    public Plot getPlot() {
        return plot;
    }

    /**
     * @return - builder object
     */
    public Builder getBuilder() {
        return builder;
    }
}
