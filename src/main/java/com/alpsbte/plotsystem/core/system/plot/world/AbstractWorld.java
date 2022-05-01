package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.generator.PlotWorldGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.boydti.fawe.util.EditSessionBuilder;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;

public abstract class AbstractWorld implements IWorld {
    public static final int MAX_WORLD_HEIGHT = 256;

    private final MultiverseCore mvCore = PlotSystem.DependencyManager.getMultiverseCore();
    private final String worldName;
    private final Plot plot;

    protected AbstractWorld(@NotNull String worldName, @NotNull Plot plot) {
        this.worldName = worldName;
        this.plot = plot;
    }

    @Override
    public <T extends PlotWorldGenerator> boolean generateWorld(@NotNull Class<T> generator) {
        throw new UnsupportedOperationException("No world generator set for world " + getWorldName());
    }

    @Override
    public <T extends PlotWorldGenerator> boolean regenWorld(@NotNull Class<T> generator) {
        return deleteWorld() && generateWorld(generator);
    }

    @Override
    public boolean deleteWorld() {
        if (isWorldGenerated() && loadWorld()) {
            if (mvCore.getMVWorldManager().deleteWorld(getWorldName(), true, true) && mvCore.saveWorldConfig()) {
                try {
                    File multiverseInventoriesConfig = new File(PlotSystem.DependencyManager.getMultiverseInventoriesConfigPath(getWorldName()));
                    File worldGuardConfig = new File(PlotSystem.DependencyManager.getWorldGuardConfigPath(getWorldName()));
                    if (multiverseInventoriesConfig.exists()) FileUtils.deleteDirectory(multiverseInventoriesConfig);
                    if (worldGuardConfig.exists()) FileUtils.deleteDirectory(worldGuardConfig);
                } catch (IOException ex) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not delete config files for world " + getWorldName() + "!");
                    return false;
                }
                return true;
            } else Bukkit.getLogger().log(Level.WARNING, "Could not delete world " + getWorldName() + "!");
        }
        return false;
    }

    @Override
    public boolean loadWorld() {
        if(isWorldGenerated()) {
            if (isWorldLoaded()) {
                return true;
            } else return mvCore.getMVWorldManager().loadWorld(getWorldName()) || isWorldLoaded();
        } else Bukkit.getLogger().log(Level.WARNING, "Could not load world " + worldName + " because it is not generated!");
        return false;
    }

    @Override
    public boolean unloadWorld(boolean movePlayers) {
        if (isWorldGenerated()) {
            if(isWorldLoaded()) {
                if (movePlayers && !getBukkitWorld().getPlayers().isEmpty()) {
                    for (Player player : getBukkitWorld().getPlayers()) {
                        player.teleport(Utils.getSpawnLocation());
                    }
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), (() ->
                        Bukkit.unloadWorld(getBukkitWorld(), true)), 60L);
                return !isWorldLoaded();
            }
            return true;
        } else Bukkit.getLogger().log(Level.WARNING, "Could not unload world " + worldName + " because it is not generated!");
        return false;
    }

    @Override
    public boolean teleportPlayer(@NotNull Player player) {
        Location spawnLocation = getSpawnPoint(plot.getCenter());
        if (isWorldGenerated() && loadWorld()) {
            // Set spawn point 1 block above the highest block at the spawn location
            spawnLocation.setY(getBukkitWorld().getHighestBlockYAt((int) spawnLocation.getX(), (int) spawnLocation.getZ()) + 1);

            player.teleport(spawnLocation);
            return true;
        } else Bukkit.getLogger().log(Level.WARNING, "Could not teleport player " + player.getName() + " to world " + worldName + "!");
        return false;
    }

    @Override
    public Location getSpawnPoint(Vector plotVector) {
        if (isWorldGenerated() && loadWorld()) {
            Location spawnLocation = plotVector == null ? getBukkitWorld().getSpawnLocation() :
                    new Location(getBukkitWorld(), plotVector.getX(), plotVector.getY(), plotVector.getZ());
            unloadWorld(false);
            return spawnLocation;
        }
        return null;
    }

    @Override
    public World getBukkitWorld() {
        return Bukkit.getWorld(worldName);
    }

    @Override
    public String getWorldName() {
        return worldName;
    }

    @Override
    public String getRegionName() {
       return worldName.toLowerCase(Locale.ROOT);
    }

    @Override
    public ProtectedRegion getProtectedRegion() {
        return getRegion(getRegionName() + "-1");
    }

    @Override
    public ProtectedRegion getProtectedBuildRegion() {
        return getRegion(getRegionName());
    }

    @Override
    public boolean isWorldLoaded() {
        return getBukkitWorld() != null;
    }

    @Override
    public boolean isWorldGenerated() {
        return mvCore.getMVWorldManager().getMVWorld(worldName) != null || mvCore.getMVWorldManager().getUnloadedWorlds().contains(worldName);
    }

    private ProtectedRegion getRegion(String regionName) {
        RegionContainer container = PlotSystem.DependencyManager.getWorldGuard().getRegionContainer();
        if (loadWorld()) {
            RegionManager regionManager = container.get(getBukkitWorld());
            if (regionManager != null) {
                return regionManager.getRegion(regionName);
            } else Bukkit.getLogger().log(Level.WARNING, "Region manager is null");
        }
        return null;
    }

    public Plot getPlot() {
        return plot;
    }




    public static boolean isPlotWorld(String worldName) {
        return worldName.toLowerCase(Locale.ROOT).startsWith("p-");
    }

    public static boolean isCityPlotWorld(String worldName) {
        return worldName.toLowerCase(Locale.ROOT).startsWith("c-");
    }

    /**
     * @param plot - the plot to reset the region
     * @return - true if the region was reset successfully
     * @throws SQLException - database error
     * @throws MaxChangedBlocksException - max blocks changed error
     * @throws IOException - file error
     * @throws DataException - data error
     */
    public static boolean resetPlotRegion(Plot plot, AbstractWorld world) throws SQLException, MaxChangedBlocksException, IOException, DataException {
        if (world.loadWorld()) {
            com.sk89q.worldedit.world.World weWorld = new BukkitWorld(world.getBukkitWorld());
            EditSession editSession = new EditSessionBuilder(weWorld).fastmode(true).build();
            Polygonal2DRegion polyRegion = new Polygonal2DRegion(weWorld, plot.getOutline(), 0, PlotWorld.MAX_WORLD_HEIGHT);
            editSession.replaceBlocks(polyRegion, null, new BaseBlock(0));
            editSession.flushQueue();

            Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(plot.getOutlinesSchematic())).read(weWorld.getWorldData());
            SchematicFormat.getFormat(plot.getOutlinesSchematic()).load(plot.getOutlinesSchematic()).place(editSession, clipboard.getMinimumPoint().setY(5), true);
            editSession.flushQueue();
            return true;
        }
        return false;
    }
}
