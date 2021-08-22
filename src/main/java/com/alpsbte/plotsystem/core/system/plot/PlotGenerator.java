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

package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.config.ConfigPaths;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
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
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static com.alpsbte.plotsystem.core.system.plot.PlotManager.getPlots;

public final class PlotGenerator {

    private final Builder builder;
    private final Plot plot;

    private final static Random random = new Random();
    private World weWorld;
    private RegionManager regionManager;

    private static final MVWorldManager worldManager = PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager();

    public final static Map<UUID, LocalDateTime> playerPlotGenerationHistory = new HashMap<>();

    public PlotGenerator(int cityID, PlotDifficulty plotDifficulty, Builder builder) throws SQLException {
        this(getPlots(cityID, plotDifficulty, Status.unclaimed).get(random.nextInt(getPlots(cityID, plotDifficulty, Status.unclaimed).size())), builder);
    }

    public PlotGenerator(Plot plot, Builder builder) {
        this.plot = plot;
        this.builder = builder;

        try {
            CompletableFuture.allOf(generateWorld(), generateOutlines(), createPlotProtection())
                    .thenRun(() -> {
                        try {
                            builder.setPlot(plot.getID(), builder.getFreeSlot());
                            plot.setStatus(Status.unfinished);
                            plot.setPlotOwner(builder.getPlayer().getUniqueId().toString());
                            plot.setLastActivity(false);
                        } catch (SQLException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                    }).whenComplete((result, failed) -> {
                        try {
                            if (failed == null) {
                                PlotHandler.teleportPlayer(plot, builder.getPlayer());
                                Bukkit.broadcastMessage(Utils.getInfoMessageFormat("Created new plot §afor §6" + plot.getPlotOwner().getName() + "§a!"));
                            }
                        } catch (SQLException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                    });
        } catch (StorageException | IOException | WorldEditException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while generating plot world!", ex);
            builder.getPlayer().sendMessage(Utils.getErrorMessageFormat("An error occurred while generating a new plot!"));
            builder.getPlayer().playSound(builder.getPlayer().getLocation(), Utils.ErrorSound,1,1);
        }
    }

    private CompletableFuture<Void> generateWorld() throws StorageException {
        WorldCreator wc = new WorldCreator(plot.getWorldName());
        wc.environment(org.bukkit.World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.generatorSettings("2;0;1;");
        wc.createWorld();

        worldManager.addWorld(plot.getWorldName(), wc.environment(), null, wc.type(), false, "VoidGen");

        plot.getPlotWorld().setGameRuleValue("randomTickSpeed", "0");
        plot.getPlotWorld().setGameRuleValue("doDaylightCycle", "false");
        plot.getPlotWorld().setGameRuleValue("doFireTick", "false");
        plot.getPlotWorld().setGameRuleValue("doWeatherCycle", "false");
        plot.getPlotWorld().setGameRuleValue("keepInventory", "true");
        plot.getPlotWorld().setGameRuleValue("announceAdvancements", "false");

        plot.getPlotWorld().setTime(6000);

        MultiverseWorld mvWorld = worldManager.getMVWorld(plot.getPlotWorld());
        mvWorld.setAllowFlight(true);
        mvWorld.setGameMode(GameMode.CREATIVE);
        mvWorld.setEnableWeather(false);
        mvWorld.setDifficulty(Difficulty.PEACEFUL);
        mvWorld.setAllowAnimalSpawn(false);
        mvWorld.setAllowMonsterSpawn(false);
        mvWorld.setAutoLoad(false);
        mvWorld.setKeepSpawnInMemory(false);

        RegionContainer container = PlotSystem.DependencyManager.getWorldGuard().getRegionContainer();
        this.regionManager = container.get(plot.getPlotWorld());
        this.weWorld = new BukkitWorld(plot.getPlotWorld());

        GlobalProtectedRegion globalRegion = new GlobalProtectedRegion("__global__");

        globalRegion.setFlag(DefaultFlag.ENTRY, StateFlag.State.DENY);
        globalRegion.setFlag(DefaultFlag.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

        regionManager.addRegion(globalRegion);
        regionManager.saveChanges();
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> generateOutlines() throws IOException, WorldEditException {
        Vector buildingOutlinesCoordinates = PlotManager.getPlotCenter(plot); // TODO: Set Plot to the bottom of the schematic

        Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(plot.getOutlinesSchematic())).read(weWorld.getWorldData());
        ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard, weWorld.getWorldData());
        EditSession editSession = PlotSystem.DependencyManager.getWorldEdit().getEditSessionFactory().getEditSession(weWorld, -1);

        Operation operation = clipboardHolder.createPaste(editSession, weWorld.getWorldData()).to(buildingOutlinesCoordinates).ignoreAirBlocks(false).build();
        Operations.complete(operation);
        editSession.flushQueue();
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> createPlotProtection() throws StorageException {
        BlockVector min = BlockVector.toBlockPoint(
                0,
                1,
                0
        );
        BlockVector max = BlockVector.toBlockPoint(
                PlotManager.getPlotSize(plot),
                256,
                PlotManager.getPlotSize(plot)
        );

        ProtectedRegion protectedPlotRegion = new ProtectedCuboidRegion(
                "P-" + plot.getID(),
                min,
                max
        );

        protectedPlotRegion.setPriority(100);

        DefaultDomain owner = protectedPlotRegion.getOwners();
        owner.addPlayer(builder.getUUID());
        protectedPlotRegion.setOwners(owner);

        protectedPlotRegion.setFlag(DefaultFlag.PASSTHROUGH, StateFlag.State.ALLOW);
        protectedPlotRegion.setFlag(DefaultFlag.PASSTHROUGH.getRegionGroupFlag(), RegionGroup.OWNERS);

        protectedPlotRegion.setFlag(DefaultFlag.ENTRY, StateFlag.State.ALLOW);
        protectedPlotRegion.setFlag(DefaultFlag.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

        // Permissions
        List<String> blockedCommandsBuilders = new ArrayList<>(), allowedCommandsNonBuilder = new ArrayList<>();
        FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getCommandsConfig();

        try {
            allowedCommandsNonBuilder = config.getStringList(ConfigPaths.ALLOWED_COMMANDS_NON_BUILDERS);
            allowedCommandsNonBuilder.removeIf(c -> c.equals("/cmd1"));
            for (BaseCommand baseCommand : PlotSystem.getPlugin().getCommandManager().getBaseCommands()) {
                allowedCommandsNonBuilder.addAll(Arrays.asList(baseCommand.getNames()));
            }

            blockedCommandsBuilders = config.getStringList(ConfigPaths.BLOCKED_COMMANDS_BUILDERS);
            blockedCommandsBuilders.removeIf(c -> c.equals("/cmd1"));
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "Could not set allowed and blocked commands for plot #" + plot.getID() + "!");
        }

        protectedPlotRegion.setFlag(DefaultFlag.BLOCKED_CMDS, new HashSet<>(blockedCommandsBuilders));
        protectedPlotRegion.setFlag(DefaultFlag.BLOCKED_CMDS.getRegionGroupFlag(), RegionGroup.OWNERS);

        protectedPlotRegion.setFlag(DefaultFlag.ALLOWED_CMDS, new HashSet<>(allowedCommandsNonBuilder));
        protectedPlotRegion.setFlag(DefaultFlag.ALLOWED_CMDS.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        regionManager.addRegion(protectedPlotRegion);
        regionManager.saveChanges();
        return CompletableFuture.completedFuture(null);
    }
}
