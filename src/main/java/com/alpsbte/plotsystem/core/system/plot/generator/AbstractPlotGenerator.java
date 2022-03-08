/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021-2022, Alps BTE <bte.atchli@gmail.com>
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
import com.alpsbte.plotsystem.utils.io.config.ConfigPaths;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

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

        if (init()) {
            Exception exception = null;
            try {
                generateWorld();
                generateOutlines(plot.getOutlinesSchematic(), plot.getEnvironmentSchematic());
                createMultiverseWorld();
                configureWorld();
                createProtection();
            } catch (Exception ex) {
                exception = ex;
            }

            try {
                this.onComplete(exception != null);
            } catch (SQLException ex) {
                exception = ex;
            }

            if (exception != null) {
                if (worldManager.isMVWorld(PlotWorld.getWorldName(plot, builder))) PlotHandler.abandonPlot(plot);
                onException(exception);
            }
        }
    }

    /**
     * Executed before plot generation
     * @return true if initialization was successful
     */
    protected abstract boolean init();

    /**
     * Generates plot world
     */
    protected void generateWorld() {
        if (getPlot().getWorld().isWorldGenerated()) {
            try {
                if(getPlot().getPlotOwner().playInVoid)
                    plot.getWorld().deleteWorld();
                else
                    return;
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An SQL error occurred!", ex);
                return;
            }
        }

        worldCreator = new WorldCreator(PlotWorld.getWorldName(getPlot(), builder));
        worldCreator.environment(org.bukkit.World.Environment.NORMAL);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generatorSettings("2;0;1;");
        worldCreator.createWorld();
    }

    /**
     * Creates Multiverse world
     */
    protected void createMultiverseWorld() {
        // Check if world creator is configured and add new world to multiverse world manager
        if (worldCreator != null) {
            String worldName = PlotWorld.getWorldName(getPlot(), getBuilder());

            if(!worldManager.isMVWorld(worldName))
                worldManager.addWorld(PlotWorld.getWorldName(getPlot(), getBuilder()), worldCreator.environment(), null, worldCreator.type(), false,
                    "VoidGen:{\"caves\":false,\"decoration\":false,\"mobs\":false,\"structures\":false}");
        } else {
            throw new RuntimeException("World Creator is not configured");
        }
    }

    /**
     * Generates plot schematic and outlines
     * @param plotSchematic - schematic file
     */
    protected void generateOutlines(File plotSchematic, File environmentSchematic) {
        try {

            Vector buildingOutlinesCoordinates = plot.getCenter();

            World plotBukkitWorld = PlotWorld.getBukkitWorld(PlotWorld.getWorldName(getPlot(), getBuilder()));
            com.sk89q.worldedit.world.World weWorld = new BukkitWorld(plotBukkitWorld);

            EditSession editSession = PlotSystem.DependencyManager.getWorldEdit().getEditSessionFactory().getEditSession(weWorld, -1);
            Mask oldMask = editSession.getMask();

            if(environmentSchematic != null){
                Clipboard clipboardPlot = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(environmentSchematic)).read(weWorld.getWorldData());
                ClipboardHolder clipboardHolder = new ClipboardHolder(clipboardPlot, weWorld.getWorldData());

                editSession.setMask(new OnlyAirMask(weWorld));

                Operation operation = clipboardHolder.createPaste(editSession, weWorld.getWorldData()).to(buildingOutlinesCoordinates).ignoreAirBlocks(true).build();
                Operations.complete(operation);
                editSession.flushQueue();
            }

            editSession.setMask(oldMask);
            Polygonal2DRegion polyRegion = new Polygonal2DRegion(weWorld, plot.getOutline(), 0, PlotWorld.MAX_WORLD_HEIGHT);
            editSession.replaceBlocks(polyRegion, null, new BaseBlock(0));
            editSession.flushQueue();

            if (plotSchematic != null) {
                Clipboard clipboardPlot = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(plotSchematic)).read(weWorld.getWorldData());
                ClipboardHolder clipboardHolder = new ClipboardHolder(clipboardPlot, weWorld.getWorldData());

                Operation operation = clipboardHolder.createPaste(editSession, weWorld.getWorldData()).to(buildingOutlinesCoordinates).ignoreAirBlocks(true).build();
                Operations.complete(operation);
                editSession.flushQueue();
            }

            Location spawnLocation = PlotWorld.getSpawnPoint(plotBukkitWorld, getPlot());
            if (spawnLocation != null && getBuilder().playInVoid)
                plotBukkitWorld.setSpawnLocation(spawnLocation);

        } catch (IOException | WorldEditException | SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while generating plot outlines!", ex);
            throw new RuntimeException("Plot outlines generation completed exceptionally");
        }
    }

    /**
     * Configures plot world
     */
    protected void configureWorld() {

        World bukkitWorld = PlotWorld.getBukkitWorld(PlotWorld.getWorldName(getPlot(),getBuilder()));
        MultiverseWorld mvWorld = worldManager.getMVWorld(bukkitWorld);

        // Set world time to midday
        bukkitWorld.setTime(6000);

        // Set Bukkit world game rules
        bukkitWorld.setGameRuleValue("randomTickSpeed", "0");
        bukkitWorld.setGameRuleValue("doDaylightCycle", "false");
        bukkitWorld.setGameRuleValue("doFireTick", "false");
        bukkitWorld.setGameRuleValue("doWeatherCycle", "false");
        bukkitWorld.setGameRuleValue("keepInventory", "true");
        bukkitWorld.setGameRuleValue("doMobSpawning", "false");
        bukkitWorld.setGameRuleValue("announceAdvancements", "false");

        // Configure multiverse world
        mvWorld.setAllowFlight(true);
        mvWorld.setGameMode(GameMode.CREATIVE);
        mvWorld.setEnableWeather(false);
        mvWorld.setDifficulty(Difficulty.PEACEFUL);
        mvWorld.setAllowAnimalSpawn(false);
        mvWorld.setAllowMonsterSpawn(false);
        mvWorld.setAutoLoad(false);
        mvWorld.setKeepSpawnInMemory(false);
        worldManager.saveWorldsConfig();
    }

    /**
     * Creates plot protection
     */
    protected void createProtection() {
        RegionContainer container = PlotSystem.DependencyManager.getWorldGuard().getRegionContainer();

        String worldName = PlotWorld.getWorldName(getPlot(),getBuilder());
        String regionName = PlotWorld.getRegionName(getPlot(),getBuilder());
        World bukkitWorld = PlotWorld.getBukkitWorld(worldName);
        RegionManager regionManager = container.get(bukkitWorld);

        // Create protected region for world
        GlobalProtectedRegion globalRegion = new GlobalProtectedRegion("__global__");
        globalRegion.setFlag(DefaultFlag.ENTRY, StateFlag.State.DENY);
        globalRegion.setFlag(DefaultFlag.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

        // Create protected region for plot from the outline of the plot
        ProtectedRegion protectedPlotRegion;
        try{
            List<BlockVector2D> points = plot.getOutline();
            protectedPlotRegion = new ProtectedPolygonalRegion(regionName, points, 0, 256);
            protectedPlotRegion.setPriority(100);

        }catch (Exception ex){
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while saving plot protection!", ex);
            throw new RuntimeException("Plot protection creation completed exceptionally");
        }


        // Add and save regions
        try {
            boolean globalRegionExists = regionManager.hasRegion("__global__");

            if (regionManager != null) {
                if(!globalRegionExists)
                    regionManager.addRegion(globalRegion);

                regionManager.addRegion(protectedPlotRegion);
                regionManager.saveChanges();
            } else {
                throw new RuntimeException("Region Manager is null");
            }
        } catch (StorageException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while saving plot protection!", ex);
            throw new RuntimeException("Plot protection creation completed exceptionally");
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
            PlotManager.clearCache();
        }
    }

    /**
     * Gets invoked when an exception has occurred
     * @param ex - caused exception
     */
    protected void onException(Throwable ex) {
        Bukkit.getLogger().log(Level.SEVERE, "An error occurred while generating plot!", ex);
        builder.getPlayer().sendMessage(Utils.getErrorMessageFormat(LangUtil.get(builder.getPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
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
