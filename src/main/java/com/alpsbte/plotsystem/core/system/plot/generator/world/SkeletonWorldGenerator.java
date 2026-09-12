package com.alpsbte.plotsystem.core.system.plot.generator.world;

import com.alpsbte.plotsystem.utils.DependencyManager;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class SkeletonWorldGenerator {
    public static final NamespacedKey WORLD_KEY = NamespacedKey.minecraft("skeleton");
    private static final World.Environment ENVIRONMENT = World.Environment.NORMAL;
    private static final WorldType WORLD_TYPE = WorldType.FLAT;
    private static final String GENERATOR_SETTINGS = "{\"features\": false,\"layers\": [{\"block\": \"air\", \"height\": 1}],\"biome\":\"plains\"}";

    private World world;

    public SkeletonWorldGenerator() {
        generateWorld();
        PlotWorldGenerator.createMultiverseWorld(DependencyManager.getMultiverseCore().getWorldManager(), this.world.getName());
        configureWorld();
        PlotWorldGenerator.configureWorld(DependencyManager.getMultiverseCore().getWorldManager(), this.world.getName());
        saveWorld();
    }

    protected void generateWorld() {
        WorldCreator worldCreator = WorldCreator.ofKey(WORLD_KEY)
                .environment(ENVIRONMENT)
                .type(WORLD_TYPE)
                .generator(new SkeletonWorldGenerator.EmptyChunkGenerator())
                .generatorSettings(GENERATOR_SETTINGS);
        this.world = worldCreator.createWorld();
    }

    protected void configureWorld() {
        World bukkitWorld = this.world;
        assert bukkitWorld != null;

        // Set game rules
        bukkitWorld.setGameRule(GameRules.RANDOM_TICK_SPEED, 0);
        bukkitWorld.setGameRule(GameRules.ADVANCE_TIME, false);
        bukkitWorld.setGameRule(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);
        bukkitWorld.setGameRule(GameRules.ADVANCE_WEATHER, false);
        bukkitWorld.setGameRule(GameRules.KEEP_INVENTORY, true);
        bukkitWorld.setGameRule(GameRules.SPAWN_MOBS, false);
        bukkitWorld.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
        bukkitWorld.setGameRule(GameRules.BLOCK_DROPS, false);

        // Set time to noon
        bukkitWorld.setTime(6000);
    }

    protected void saveWorld() {
        assert this.world != null;
        this.world.save();
    }

    public static class EmptyChunkGenerator extends ChunkGenerator {
        // It should just do nothing

        @Override
        public @Nullable Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
            return new Location(world, 0, 0, 0);
        }
    }
}
