package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.PlotSystem;
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
import org.bukkit.*;

import java.util.logging.Level;

public class PlotWorldGenerator {
    private final MVWorldManager worldManager = PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager();
    private WorldCreator worldCreator;

    private final String worldName;
    private static final World.Environment environment = World.Environment.NORMAL;
    private static final WorldType worldType = WorldType.FLAT;
    private static final String generatorSettings = "{\"layers\": [{\"block\": \"air\", \"height\": 1}],\"biome\":\"plains\"}";

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
        worldCreator.generatorSettings(generatorSettings);
        worldCreator.createWorld();
    }

    protected void createMultiverseWorld() throws Exception {
        // Check if world creator is configured and add new world to multiverse world manager
        if (worldCreator != null) {
            if(!worldManager.isMVWorld(worldName))
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

    protected void createGlobalProtection() throws StorageException {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Bukkit.getWorld(worldName)));

        if (regionManager != null) {
            // Create protected region for world
            String regionName = "__global__";
            GlobalProtectedRegion globalRegion = new GlobalProtectedRegion(regionName);
            globalRegion.setFlag(Flags.ENTRY, StateFlag.State.DENY);
            globalRegion.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);
            globalRegion.setFlag(Flags.PASSTHROUGH, StateFlag.State.DENY);
            globalRegion.setFlag(Flags.PASSTHROUGH.getRegionGroupFlag(), RegionGroup.ALL);
            globalRegion.setFlag(Flags.TNT, StateFlag.State.DENY);
            globalRegion.setFlag(Flags.TNT.getRegionGroupFlag(), RegionGroup.ALL);

            if (regionManager.hasRegion(regionName)) regionManager.removeRegion(regionName);
            regionManager.addRegion(globalRegion);
            regionManager.saveChanges();
        } else Bukkit.getLogger().log(Level.WARNING, "Region Manager is null!");
    }
}
