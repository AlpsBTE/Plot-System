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

import net.kyori.adventure.util.TriState;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;

public class SkeletonWorldGenerator {
    public static final String WORLD_NAME = "Skeleton";
    private static final World.Environment ENVIRONMENT = World.Environment.NORMAL;
    private static final WorldType WORLD_TYPE = WorldType.FLAT;
    private static final String GENERATOR_SETTINGS = "{\"features\": false,\"layers\": [{\"block\": \"air\", \"height\": 1}],\"biome\":\"plains\"}";

    private World world;

    public SkeletonWorldGenerator() {
        generateWorld();
        configureWorld();
        saveWorld();
    }

    protected void generateWorld() {
        WorldCreator worldCreator = new WorldCreator(WORLD_NAME)
                .environment(ENVIRONMENT)
                .type(WORLD_TYPE)
                .generator(new SkeletonWorldGenerator.EmptyChunkGenerator())
                .generatorSettings(GENERATOR_SETTINGS)
                .keepSpawnLoaded(TriState.FALSE);
        this.world = worldCreator.createWorld();
    }

    protected void configureWorld() {
        World bukkitWorld = this.world;
        assert bukkitWorld != null;

        // Set game rules
        bukkitWorld.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        bukkitWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        bukkitWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
        bukkitWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        bukkitWorld.setGameRule(GameRule.KEEP_INVENTORY, true);
        bukkitWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        bukkitWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        bukkitWorld.setGameRule(GameRule.DO_TILE_DROPS, false);
        bukkitWorld.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 0);

        // Set time to noon
        bukkitWorld.setTime(6000);
    }

    protected void saveWorld() {
        assert this.world != null;
        this.world.save();
    }

    public static class EmptyChunkGenerator extends ChunkGenerator {
        // It should just do nothing
    }
}
