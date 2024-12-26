/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system.tutorial.stage;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

import static net.kyori.adventure.text.Component.text;

public class TutorialWorld {
    private String worldName;
    private String playerSpawnConfigPath;
    private String npcSpawnConfigPath;

    public TutorialWorld(int tutorialId, int tutorialWorldIndex) {
        this(tutorialId, tutorialWorldIndex, null);
    }

    public TutorialWorld(int tutorialId, int tutorialWorldIndex, String worldName) {
        FileConfiguration config = ConfigUtil.getTutorialInstance().configs[tutorialId];

        // Read tutorial spawn section
        ConfigurationSection tutorialSpawnsSection = config.getConfigurationSection(TutorialUtils.Path.TUTORIAL_WORLDS);
        if (tutorialSpawnsSection == null) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not find tutorial spawns section in the tutorial config!"));
            return;
        }

        Set<String> tutorialSpawn = tutorialSpawnsSection.getKeys(false);
        if (tutorialSpawn.size() < tutorialWorldIndex) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not find spawn point " + tutorialWorldIndex + " in the tutorial config!"));
            return;
        }

        ConfigurationSection spawnSection = tutorialSpawnsSection.getConfigurationSection(tutorialSpawn.toArray()[tutorialWorldIndex].toString());
        if (spawnSection == null) return;
        this.worldName = worldName == null ? spawnSection.getName() : worldName;
        this.playerSpawnConfigPath = spawnSection.getString(TutorialUtils.Path.TUTORIAL_WORLDS_SPAWN_PLAYER);
        this.npcSpawnConfigPath = spawnSection.getString(TutorialUtils.Path.TUTORIAL_WORLDS_SPAWN_NPC);
    }

    public Location getPlayerSpawnLocation() {
        return getSpawnLocation(worldName, playerSpawnConfigPath);
    }

    public Location getNpcSpawnLocation() {
        return getSpawnLocation(worldName, npcSpawnConfigPath);
    }

    public String getWorldName() {
        return worldName;
    }

    public static Location getSpawnLocation(String worldName, String configPath) {
        String[] spawnPointData = configPath.trim().split(",");
        return new Location(Bukkit.getWorld(worldName),
                Double.parseDouble(spawnPointData[0]),
                Double.parseDouble(spawnPointData[1]),
                Double.parseDouble(spawnPointData[2]),
                Float.parseFloat(spawnPointData[3]),
                Float.parseFloat(spawnPointData[4]));
    }
}
