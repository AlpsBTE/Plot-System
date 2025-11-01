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

package com.alpsbte.plotsystem.core.system.plot.generator.world;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.SpawnCategory;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.ImportWorldOptions;
import org.mvplugins.multiverse.external.vavr.control.Option;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

public class PlotWorldGenerator {
    private final WorldManager worldManager = DependencyManager.getMultiverseCore().getWorldManager();
    private final String worldName;
    private static final World.Environment environment = World.Environment.NORMAL;
    private static final WorldType worldType = WorldType.FLAT;

    public PlotWorldGenerator(String worldName) throws Exception {
        long startTime = System.nanoTime();
        this.worldName = worldName;

        // Async Part
        generateWorld();

        // Sync Part
        long mainThreadStart = System.nanoTime();
        Utils.runSync(() -> {
            createMultiverseWorld();
            configureWorld();
            createGlobalProtection();
            return null;
        }).get();

        PlotSystem.getPlugin().getComponentLogger().info("(PWG) Total time to generate world: {}ms", (System.nanoTime() - startTime) / 1_000_000);
        PlotSystem.getPlugin().getComponentLogger().info("(PWG) Total time on main thread: {}ms", (System.nanoTime() - mainThreadStart) / 1_000_000);
    }

    protected void generateWorld() throws IOException {
        // copy skeleton world with correct world name
        Path skeletonPath = Bukkit.getWorldContainer().toPath().resolve("Skeleton");
        Path worldPath = Bukkit.getWorldContainer().toPath().resolve(worldName);
        FileUtils.copyDirectory(skeletonPath.toFile(), worldPath.toFile());

        // delete uid.dat
        Files.delete(worldPath.resolve("uid.dat"));

        // rename world name in level.dat
        Path levelDat = worldPath.resolve("level.dat");
        NamedTag level = NBTUtil.read(levelDat.toFile());
        CompoundTag tag = (CompoundTag) level.getTag();
        tag.remove("LevelName");
        tag.putString("LevelName", worldName);
        level.setTag(tag);
        NBTUtil.write(level, levelDat.toFile());

        // rename world in paper-world.yml
        Path paperWorld = worldPath.resolve("paper-world.yml");
        String paperWorldContents = Files.readString(paperWorld);
        String updatedContents = paperWorldContents
                .replaceAll(SkeletonWorldGenerator.WORLD_NAME, worldName)
                .replaceAll(SkeletonWorldGenerator.WORLD_NAME.toLowerCase(), worldName.toLowerCase());
        Files.writeString(paperWorld, updatedContents);
    }

    protected void createMultiverseWorld() {
        if (!worldManager.isLoadedWorld(worldName)) {
            worldManager.importWorld(ImportWorldOptions.worldName(worldName)
                    .environment(environment)
                    .generator("VoidGen:{\"caves\":false,\"decoration\":false,\"mobs\":false,\"structures\":false}")
                    .useSpawnAdjust(false)
            );
        }
    }

    protected void configureWorld() {
        World world = Bukkit.getWorld(worldName);
        assert world != null;
        Option<LoadedMultiverseWorld> mvWorld = worldManager.getLoadedWorld(worldName);

        // Configure multiverse world
        mvWorld.get().setAllowFlight(true);
        mvWorld.get().setGameMode(GameMode.CREATIVE);
        mvWorld.get().setAllowWeather(false);
        mvWorld.get().setDifficulty(Difficulty.PEACEFUL);
        mvWorld.get().getEntitySpawnConfig().getSpawnCategoryConfig(SpawnCategory.ANIMAL).setSpawn(false);
        mvWorld.get().getEntitySpawnConfig().getSpawnCategoryConfig(SpawnCategory.MONSTER).setSpawn(false);
        mvWorld.get().setAutoLoad(false);
        mvWorld.get().setKeepSpawnInMemory(false);
        worldManager.saveWorldsConfig();
    }

    protected void createGlobalProtection() throws StorageException {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(worldName))));

        if (regionManager != null) {
            // Create a protected region for the plot world
            String regionName = "__global__";
            GlobalProtectedRegion globalRegion = new GlobalProtectedRegion(regionName);
            globalRegion.setFlag(Flags.ENTRY, StateFlag.State.DENY);
            globalRegion.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);
            globalRegion.setFlag(Flags.PASSTHROUGH, StateFlag.State.DENY);
            globalRegion.setFlag(Flags.PASSTHROUGH.getRegionGroupFlag(), RegionGroup.ALL);
            globalRegion.setFlag(Flags.TNT, StateFlag.State.DENY);
            globalRegion.setFlag(Flags.TNT.getRegionGroupFlag(), RegionGroup.ALL);
            if (DependencyManager.isWorldGuardExtraFlagsEnabled())
                globalRegion.setFlag(new StateFlag("worldedit", true, RegionGroup.ALL), StateFlag.State.DENY);

            if (regionManager.hasRegion(regionName)) regionManager.removeRegion(regionName);
            regionManager.addRegion(globalRegion);
            regionManager.saveChanges();
        } else PlotSystem.getPlugin().getComponentLogger().warn(text("Region Manager is null!"));
    }
}


