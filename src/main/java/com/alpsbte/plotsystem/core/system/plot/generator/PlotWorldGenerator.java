package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Random;

import static net.kyori.adventure.text.Component.text;

public class PlotWorldGenerator {
    private final MVWorldManager worldManager = DependencyManager.getMultiverseCore().getMVWorldManager();
    private WorldCreator worldCreator;

    private final String worldName;
    private static final World.Environment environment = World.Environment.NORMAL;
    private static final WorldType worldType = WorldType.FLAT;
    private static final String generatorSettings = "{\"features\": false,\"layers\": [{\"block\": \"air\", \"height\": 1}],\"biome\":\"plains\"}";

    public PlotWorldGenerator(String worldName) throws Exception {
        this.worldName = worldName;
        generateWorld();
        createMultiverseWorld();
        configureWorld();
        createGlobalProtection();
    }

    protected void generateWorld() {
        worldCreator = new WorldCreator(worldName);
        worldCreator.environment(environment);
        worldCreator.type(worldType);
        worldCreator.generator(new EmptyChunkGenerator());
        worldCreator.generatorSettings(generatorSettings);
        worldCreator.createWorld();
    }

    protected void createMultiverseWorld() throws Exception {
        // Check if world creator is configured and add new world to multiverse world manager
        if (worldCreator != null) {
            if (!worldManager.isMVWorld(worldName))
                worldManager.addWorld(worldName, environment, null, worldType, false,
                        "VoidGen:{\"caves\":false,\"decoration\":false,\"mobs\":false,\"structures\":false}", false);
        } else {
            throw new Exception("World Creator is not configured");
        }
    }

    protected void configureWorld() {
        World bukkitWorld = Bukkit.getWorld(worldName);
        MultiverseWorld mvWorld = worldManager.getMVWorld(bukkitWorld);

        // Set world time to midday
        assert bukkitWorld != null;
        bukkitWorld.setTime(6000);

        // Set Bukkit world game rules
        bukkitWorld.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        bukkitWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        bukkitWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
        bukkitWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        bukkitWorld.setGameRule(GameRule.KEEP_INVENTORY, true);
        bukkitWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        bukkitWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        bukkitWorld.setGameRule(GameRule.DO_TILE_DROPS, false);
        bukkitWorld.setGameRule(GameRule.DO_MOB_LOOT, false);

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

    public static class EmptyChunkGenerator extends ChunkGenerator {
        @Override
        @Nonnull
        public ChunkData generateChunkData(@Nonnull World world, @Nonnull Random random, int x, int z, @Nonnull BiomeGrid biome) {
            return createChunkData(world);
        }
    }

}


