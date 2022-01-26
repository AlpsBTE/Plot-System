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

package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.CompanionMenu;
import com.alpsbte.plotsystem.core.menus.ReviewMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.generator.RawPlotGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.onarandombox.MultiverseCore.MultiverseCore;
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
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;

public class PlotWorld implements IPlotWorld {

    private final Plot plot;
    private final MultiverseCore mvCore;

    public PlotWorld(Plot plot) {
        this.plot = plot;
        this.mvCore = PlotSystem.DependencyManager.getMultiverseCore();
    }

    @Override
    public <T extends AbstractPlotGenerator> boolean generateWorld(@NotNull Builder plotOwner, @NotNull Class<T> generator) {
        if (!isWorldGenerated()) {
            if (generator.isAssignableFrom(DefaultPlotGenerator.class)) {
               new DefaultPlotGenerator(plot, plotOwner);
            } else if (generator.isAssignableFrom(RawPlotGenerator.class)) {
                new RawPlotGenerator(plot, plotOwner);
            } else return false;
            return true;
        }
        return false;
    }

    @Override
    public <T extends AbstractPlotGenerator> boolean regenWorld(@NotNull Class<T> generator) {
        try {
            if (deleteWorld() && generateWorld(plot.getPlotOwner(), generator))
                return true;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public boolean deleteWorld() {
        if (isWorldGenerated() && loadWorld()) {
            if (mvCore.getMVWorldManager().deleteWorld(getWorldName(), true, true) && mvCore.saveWorldConfig()) {
                try {
                    File multiverseInventoriesConfig = new File(PlotManager.getMultiverseInventoriesConfigPath(plot.getWorld().getWorldName()));
                    File worldGuardConfig = new File(PlotManager.getWorldGuardConfigPath(plot.getWorld().getWorldName()));
                    if (multiverseInventoriesConfig.exists()) FileUtils.deleteDirectory(multiverseInventoriesConfig);
                    if (worldGuardConfig.exists()) FileUtils.deleteDirectory(worldGuardConfig);
                } catch (IOException ex) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not delete world configs of plot #" + plot.getID() + "!");
                    return false;
                }
                return true;
            } else Bukkit.getLogger().log(Level.WARNING, "Could not delete world of plot #" + plot.getID() + "!");
        }
        return false;
    }

    @Override
    public boolean loadWorld() {
        try {
            // Generate plot if it doesn't exist
            if (!isWorldGenerated() && plot.getFinishedSchematic().exists()) {
                new DefaultPlotGenerator(plot, plot.getPlotOwner()) {
                    @Override
                    protected void generateOutlines(File plotSchematic) {
                        super.generateOutlines(plot.getFinishedSchematic());
                    }

                    @Override
                    protected boolean init() {
                        return true;
                    }

                    @Override
                    protected void onComplete(boolean failed) throws SQLException {
                        plot.getPermissions().clearAllPerms();
                        super.onComplete(true); // This code sucks
                    }
                };
                if (!isWorldGenerated() || !isWorldLoaded()) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not regenerate world of plot #" + plot.getID() + "!");
                    return false;
                }
            } else if (isWorldGenerated())
                return mvCore.getMVWorldManager().loadWorld(getWorldName()) || isWorldLoaded();
            return true;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public boolean unloadWorld(boolean movePlayers) {
        if(isWorldLoaded()) {
            if (movePlayers && !getBukkitWorld().getPlayers().isEmpty()) {
                for (Player player : getBukkitWorld().getPlayers()) {
                    player.teleport(Utils.getSpawnLocation());
                }
            }

            try {
                if (plot.getStatus() == Status.completed && getBukkitWorld().getPlayers().isEmpty()) {
                    deleteWorld();
                    return true;
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), (() ->
                    Bukkit.unloadWorld(getBukkitWorld(), true)), 60L);
            return !isWorldLoaded();
        }
        return true;
    }

    @Override
    public boolean teleportPlayer(Player player) {
        if (loadWorld()) {
            try {
                player.sendMessage(Utils.getInfoMessageFormat("Teleporting to plot §6#" + plot.getID()));

                player.teleport(getSpawnPoint());
                player.playSound(player.getLocation(), Utils.TeleportSound, 1, 1);
                player.setAllowFlight(true);
                player.setFlying(true);

                player.getInventory().setItem(8, CompanionMenu.getMenuItem());
                if(player.hasPermission("plotsystem.review")) {
                    player.getInventory().setItem(7, ReviewMenu.getMenuItem());
                }

                PlotHandler.sendLinkMessages(plot, player);
                PlotHandler.sendGroupTipMessage(plot, player);

                if(plot.getPlotOwner().getUUID().equals(player.getUniqueId())) {
                    plot.setLastActivity(false);
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
            return true;
        } else player.sendMessage(Utils.getErrorMessageFormat("Could not load plot world. Please try again!"));
        return false;
    }

    @Override
    public Location getSpawnPoint() {
        Location spawnLocation = new Location(plot.getWorld().getBukkitWorld(),
                PlotManager.getPlotCenter().getX() + 0.5,
                30,
                PlotManager.getPlotCenter().getZ() + 0.5,
                -90,
                90);
        // Set spawn point 1 block above the highest center point
        spawnLocation.setY(plot.getWorld().getBukkitWorld().getHighestBlockYAt((int) spawnLocation.getX(), (int) spawnLocation.getZ()) + 1);
        return spawnLocation;
    }

    @Override
    public World getBukkitWorld() {
        return Bukkit.getWorld(getWorldName());
    }

    @Override
    public String getWorldName() {
        return "P-" + plot.getID();
    }

    @Override
    public ProtectedRegion getProtectedRegion() {
        RegionContainer container = PlotSystem.DependencyManager.getWorldGuard().getRegionContainer();
        if (loadWorld()) {
            RegionManager regionManager = container.get(getBukkitWorld());
            if (regionManager != null) {
                return regionManager.getRegion(getWorldName().toLowerCase(Locale.ROOT));
            } else Bukkit.getLogger().log(Level.WARNING, "Region manager is null");
        }
        return null;
    }

    @Override
    public boolean isWorldLoaded() {
        return getBukkitWorld() != null;
    }

    @Override
    public boolean isWorldGenerated() {
        return mvCore.getMVWorldManager().getMVWorld(getWorldName()) != null || mvCore.getMVWorldManager().getUnloadedWorlds().contains(getWorldName());
    }
}
