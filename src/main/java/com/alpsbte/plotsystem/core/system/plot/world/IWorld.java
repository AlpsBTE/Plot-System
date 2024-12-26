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

import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;

public interface IWorld {
    /**
     * Generates the plot world with the required configurations and schematic
     *
     * @param generator generator type as class
     * @return true if world was generated successfully
     */
    <T extends AbstractPlotGenerator> boolean generateWorld(@NotNull Class<T> generator);

    /**
     * Regenerates the current plot with an optional new generator type
     *
     * @param generator generator type as class
     * @return true if world was regenerated successfully
     */
    <T extends AbstractPlotGenerator> boolean regenWorld(@NotNull Class<T> generator);

    /**
     * Deletes the world file and entry in the config file
     *
     * @return true if world was deleted successfully
     */
    boolean deleteWorld();

    /**
     * Loads the plot world to memory to be used. Plot has to be generated.
     *
     * @return true if world was loaded successfully
     */
    boolean loadWorld();

    /**
     * Unloads the plot world from memory. Plot cannot be used anymore. Plot has to be generated.
     *
     * @param movePlayers if true, players will get teleported to the spawn location. Otherwise, plot will not get unloaded.
     * @return true if world was loaded successfully
     */
    boolean unloadWorld(boolean movePlayers);

    /**
     * Teleports a player to the spawn point of the plot
     *
     * @param player bukkit player
     * @return true if player was teleported successfully
     */
    boolean teleportPlayer(@NotNull Player player);

    /**
     * Returns the spawn point of the plot
     *
     * @param plotVector plot vector
     * @return center coordinates of the plot
     */
    Location getSpawnPoint(BlockVector3 plotVector);

    /**
     * Calculates the origin Y value in the plot world used for schematic pasting
     *
     * @return the origin Y value
     * @throws IOException if the outline schematic fails to load
     */
    int getPlotHeight() throws IOException, SQLException;

    /**
     * Calculates the centered Y value in the plot world
     *
     * @return the centered Y value
     * @throws IOException if the outline schematic fails to load
     */
    int getPlotHeightCentered() throws IOException;

    /**
     * @return Bukkit plot world
     */
    World getBukkitWorld();

    /**
     * @return world name of the plot
     */
    String getWorldName();

    /**
     * @return region world name of the plot
     */
    String getRegionName();

    /**
     * Loads the protected plot world region from WorldGuard config
     *
     * @return protected WorldGuard region
     */
    ProtectedRegion getProtectedRegion();

    /**
     * Loads the protected plot world build region from WorldGuard config
     *
     * @return protected WorldGuard build region
     */
    ProtectedRegion getProtectedBuildRegion();

    /**
     * Checks if the plot world is loaded to memory
     *
     * @return true if world is loaded
     */
    boolean isWorldLoaded();

    /**
     * Checks if the plot world is generated
     *
     * @return true if world is generated
     */
    boolean isWorldGenerated();
}
