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
import com.alpsbte.plotsystem.core.system.plot.PlotType;
import com.alpsbte.plotsystem.core.system.plot.world.AbstractWorld;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
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
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

public abstract class AbstractPlotGenerator {
    private final Plot plot;
    private final Builder builder;
    private final AbstractWorld world;

    private final PlotType builderPlotType;

    /**
     * Generates a new plot in the plot world
     * @param plot - plot which should be generated
     * @param builder - builder of the plot
     */
    public AbstractPlotGenerator(@NotNull Plot plot, @NotNull Builder builder) {
        this(plot, builder, plot.getWorld());
    }


    /**
     * Generates a new plot in the given world
     * @param plot - plot which should be generated
     * @param builder - builder of the plot
     * @param world - world of the plot
     */
    private AbstractPlotGenerator(@NotNull Plot plot, @NotNull Builder builder, @NotNull AbstractWorld world) {
        this.plot = plot;
        this.builder = builder;
        this.world = world;
        this.builderPlotType = builder.getPlotTypeSetting();

        if (init()) {
            Exception exception = null;
            try {
                if (builderPlotType.hasOnePlotPerWorld() || !world.isWorldGenerated()) {
                    new PlotWorldGenerator(world.getWorldName());
                }
                generateOutlines(plot.getOutlinesSchematic(), plot.getEnvironmentSchematic());
                createPlotProtection();
            } catch (Exception ex) {
                exception = ex;
            }

            try {
                this.onComplete(exception != null);
            } catch (SQLException ex) {
                exception = ex;
            }

            if (exception != null) {
                PlotHandler.abandonPlot(plot);
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
     * Generates plot schematic and outlines
     * @param plotSchematic - plot schematic file
     * @param environmentSchematic - environment schematic file
     */
    protected void generateOutlines(@NotNull File plotSchematic, @Nullable File environmentSchematic) {
        try {
            final class OnlyAirMask extends ExistingBlockMask {
                public OnlyAirMask(Extent extent) {
                    super(extent);
                }

                @Override
                public boolean test(Vector vector) {
                    return this.getExtent().getLazyBlock(vector).getType() == 0;
                }
            }

            Vector outlineCoords = plot.getCenter();
            World plotBukkitWorld = world.getBukkitWorld();

            com.sk89q.worldedit.world.World weWorld = new BukkitWorld(plotBukkitWorld);
            EditSession editSession = new EditSessionBuilder(weWorld).fastmode(true).build();
            Mask oldMask = editSession.getMask();

            if(builderPlotType.hasEnvironment() && environmentSchematic != null){
                editSession.setMask(new OnlyAirMask(weWorld));
                SchematicFormat.getFormat(environmentSchematic).load(environmentSchematic).place(editSession, outlineCoords, true);
                editSession.flushQueue();
            }

            editSession.setMask(oldMask);
            Polygonal2DRegion polyRegion = new Polygonal2DRegion(weWorld, plot.getOutline(), 0, PlotWorld.MAX_WORLD_HEIGHT);
            editSession.replaceBlocks(polyRegion, null, new BaseBlock(0));
            editSession.flushQueue();

            SchematicFormat.getFormat(plotSchematic).load(plotSchematic).place(editSession, outlineCoords, true);
            editSession.flushQueue();
        } catch (IOException | WorldEditException | SQLException | DataException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while generating plot outlines", ex);
            throw new RuntimeException("Plot outlines generation completed exceptionally");
        }

        // If the player is playing in his own world, then additionally generate the plot in the city world
        try {
            if (PlotWorld.isPlotWorld(world.getWorldName())) {
                // Generate city plot world if it doesn't exist
                new AbstractPlotGenerator(plot, builder, new CityPlotWorld(plot)) {
                    @Override
                    protected boolean init() {
                        return true;
                    }

                    @Override
                    protected void createPlotProtection() {}

                    @Override
                    protected void onComplete(boolean failed) throws SQLException {
                        super.onComplete(true);
                    }

                    @Override
                    protected void onException(Throwable ex) {
                        Bukkit.getLogger().log(Level.WARNING, "Could not generate plot in city world " + world.getWorldName() + "!", ex);
                    }
                };
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }


    /**
     * Creates plot protection
     */
    protected void createPlotProtection() {
        RegionContainer regionContainer = PlotSystem.DependencyManager.getWorldGuard().getRegionContainer();
        RegionManager regionManager = regionContainer.get(world.getBukkitWorld());

        try {
            if (regionManager != null) {
                // Create protected region for plot from the outline of the plot
                List<BlockVector2D> points = plot.getOutline();
                ProtectedRegion protectedPlotRegion = new ProtectedPolygonalRegion(world.getRegionName(), points, 0, 256);
                protectedPlotRegion.setPriority(100);


                // Add and save protected region
                regionManager.addRegion(protectedPlotRegion);
                regionManager.saveChanges();


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

            } else Bukkit.getLogger().log(Level.WARNING, "Region Manager is null!");
        } catch (StorageException | SQLException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Could not save protected plot region in world " + world.getWorldName() + "!", ex);
            throw new RuntimeException("Plot protection creation completed exceptionally");
        }
    }


    /**
     * Gets invoked when generation is completed
     * @param failed - true if generation has failed
     * @throws SQLException - caused by a database exception
     */
    protected void onComplete(boolean failed) throws SQLException {
        if (!failed) {
            builder.setPlot(plot.getID(), builder.getFreeSlot());
            plot.setPlotType(builderPlotType);
            plot.setStatus(Status.unfinished);
            plot.setPlotOwner(builder.getPlayer().getUniqueId().toString());
            PlotManager.clearCache();
        }

        // Unload city plot world if it is not needed anymore
        CityPlotWorld cityPlotWorld = new CityPlotWorld(plot);
        if (cityPlotWorld.isWorldLoaded()) cityPlotWorld.unloadWorld(false);
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
