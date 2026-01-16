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
