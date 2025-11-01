/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static net.kyori.adventure.text.Component.text;

public class PlotWorld implements IWorld {
    public static final int PLOT_SIZE = 150;
    public static final int MAX_WORLD_HEIGHT = 256;
    public static final int MIN_WORLD_HEIGHT = 5;

    private final MultiverseCoreApi mvCore = DependencyManager.getMultiverseCore();
    private final String worldName;
    protected final AbstractPlot plot;

    public PlotWorld(@NotNull String worldName, @Nullable AbstractPlot plot) {
        this.worldName = worldName;
        this.plot = plot;
    }

    @Override
    public <T extends AbstractPlotGenerator> boolean generateWorld(@NotNull Class<T> generator) {
        throw new UnsupportedOperationException("No world generator set for world " + getWorldName());
    }

    @Override
    public <T extends AbstractPlotGenerator> boolean regenWorld(@NotNull Class<T> generator) {
        return deleteWorld() && generateWorld(generator);
    }

    @Override
    public boolean deleteWorld() {
        if (!isWorldGenerated() || !loadWorld()) return false;
        if (!Boolean.TRUE.equals(mvCore.getWorldManager().getWorld(getWorldName())
                .map(world -> mvCore.getWorldManager().deleteWorld(DeleteWorldOptions.world(world)).isSuccess())
                .getOrElse(false)) || !mvCore.getWorldManager().saveWorldsConfig().isSuccess()) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not delete world " + getWorldName() + "!"));
            return false;
        }
        try {
            File multiverseInventoriesConfig = new File(DependencyManager.getMultiverseInventoriesConfigPath(getWorldName()));
            File worldGuardConfig = new File(DependencyManager.getWorldGuardConfigPath(getWorldName()));
            if (multiverseInventoriesConfig.exists()) FileUtils.deleteDirectory(multiverseInventoriesConfig);
            if (worldGuardConfig.exists()) FileUtils.deleteDirectory(worldGuardConfig);
        } catch (IOException ex) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not delete config files for world " + getWorldName() + "!"));
            return false;
        }
        return true;
    }

    @Override
    public boolean loadWorld() {
        if (!isWorldGenerated()) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not load world " + worldName + " because it is not generated!"));
            return false;
        }

        return isWorldLoaded() || mvCore.getWorldManager().loadWorld(getWorldName()).isSuccess();
    }

    @Override
    public boolean unloadWorld(boolean movePlayers) {
        if (!isWorldGenerated()) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not unload world " + worldName + " because it is not generated!"));
            return false;
        }
        if (!isWorldLoaded()) return true;

        if (movePlayers && !getBukkitWorld().getPlayers().isEmpty()) {
            for (Player player : getBukkitWorld().getPlayers()) {
                player.teleport(Utils.getSpawnLocation());
            }
        }

        return Bukkit.unloadWorld(getBukkitWorld(), true);
    }

    @Override
    public boolean teleportPlayer(@NotNull Player player) {
        if (loadWorld() && plot != null) {
            player.teleport(getSpawnPoint(plot instanceof TutorialPlot ? null : plot.getCenter()));
            return true;
        }
        PlotSystem.getPlugin().getComponentLogger().warn(text("Could not teleport player " + player.getName() + " to world " + worldName + "!"));
        return false;
    }

    @Override
    public Location getSpawnPoint(BlockVector3 plotVector) {
        if (!isWorldGenerated() || !loadWorld()) return null;
        Location spawnLocation = plotVector == null
                ? getBukkitWorld().getSpawnLocation()
                : new Location(getBukkitWorld(), plotVector.x(), plotVector.y(), plotVector.z());

        // Set spawn point 1 block above the highest block at the spawn location
        spawnLocation.setY(getBukkitWorld().getHighestBlockYAt((int) spawnLocation.getX(), (int) spawnLocation.getZ()) + 1d);
        return spawnLocation;
    }

    @Override
    public int getPlotHeight() throws IOException {
        throw new UnsupportedOperationException("No plot height set for " + getWorldName());
    }

    @Override
    public int getPlotHeightCentered() throws IOException {
        if (plot == null) return 0;

        Clipboard clipboard;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(plot.getInitialSchematicBytes());
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(inputStream)) {
            clipboard = reader.read();
        }
        if (clipboard == null) return 0;
        return (int) clipboard.getRegion().getCenter().y() - clipboard.getMinimumPoint().y();
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
        return mvCore.getWorldManager().getWorld(worldName).isDefined();
    }

    @Override
    public AbstractPlot getPlot() {
        return plot;
    }

    @Override
    public boolean onAbandon() {
        return true;
    }

    private @Nullable ProtectedRegion getRegion(String regionName) {
        if (!loadWorld()) return null;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(getBukkitWorld()));
        if (regionManager != null) return regionManager.getRegion(regionName);

        PlotSystem.getPlugin().getComponentLogger().warn(text("Region manager is null!"));
        return null;
    }

    /**
     * @param worldName - the name of the world
     * @return - true if the world is a plot world
     */
    public static boolean isOnePlotWorld(@NotNull String worldName) {
        return worldName.toLowerCase(Locale.ROOT).startsWith("p-") || worldName.toLowerCase(Locale.ROOT).startsWith("t-");
    }

    /**
     * @param worldName - the name of the world
     * @return - true if the world is a city plot world
     */
    public static boolean isCityPlotWorld(@NotNull String worldName) {
        return worldName.toLowerCase(Locale.ROOT).startsWith("c-");
    }

    /**
     * Returns OnePlotWorld or PlotWorld (CityPlotWorld) class depending on the world name.
     * It won't return the CityPlotWorld class because there is no use case without a plot.
     *
     * @param worldName - name of the world
     * @return - plot world
     */
    public static @Nullable PlotWorld getPlotWorldByName(String worldName) {
        // TODO: rework
        if (!isOnePlotWorld(worldName) && !isCityPlotWorld(worldName)) return null;

        Integer id = AlpsUtils.tryParseInt(worldName.substring(2));
        if (id == null) return new PlotWorld(worldName, null);

        AbstractPlot plot = worldName.toLowerCase().startsWith("t-") ? DataProvider.TUTORIAL_PLOT.getById(id).orElse(null) : DataProvider.PLOT.getPlotById(id);
        return plot == null ? null : plot.getWorld();
    }

    public static PlotWorld getByType(PlotType type, Plot plot) {
        // TODO: rework
        boolean disableCIM = PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DISABLE_CITY_INSPIRATION_MODE);
        return disableCIM || type.hasOnePlotPerWorld() ? new OnePlotWorld(plot) : new CityPlotWorld(plot);
    }
}
