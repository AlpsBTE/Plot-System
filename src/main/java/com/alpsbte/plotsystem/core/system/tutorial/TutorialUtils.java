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

package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.UNDERLINE;

public class TutorialUtils {
    public static final String CHAT_HIGHLIGHT_COLOR = GOLD.toString();
    public static final String CHAT_CLICK_HIGHLIGHT = UNDERLINE.toString();

    /**
     * Set a block at a specific location
     * @param world The world to set the block in
     * @param vector The vector of the location
     * @param material The material of the block
     */
    public static void setBlockAt(World world, BlockVector3 vector, Material material) {
        Location loc = new Location(world, vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        loc.getBlock().setType(material);
    }

    /**
     * Get a list of 3D vectors of the tip holograms
     * @param tutorialId The id of the tutorial to get the config file
     * @return A list of vector points
     */
    public static List<Vector3> getTipPoints(int tutorialId) {
        // Read coordinates from config
        FileConfiguration config = ConfigUtil.getTutorialInstance().configs[tutorialId];
        List<String> tipPointsAsString = config.getStringList(TutorialPaths.TIP_HOLOGRAM_COORDINATES);

        List<Vector3> tipPoints = new ArrayList<>();
        tipPointsAsString.forEach(point -> {
            String[] split = point.trim().split(",");
            tipPoints.add(Vector3.at(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2])));
        });
        return tipPoints;
    }

    /**
     * Get a list of documentation links which can be opened by clicking on the hologram
     * @param tutorialId The id of the tutorial to get the config file
     * @return A list of documentation links
     */
    public static List<String> getDocumentationLinks(int tutorialId) {
        // Read coordinates from config
        FileConfiguration config = ConfigUtil.getTutorialInstance().configs[tutorialId];
        return config.getStringList(TutorialPaths.DOCUMENTATION_LINKS);
    }

    public static class Sound {
        public static final org.bukkit.Sound STAGE_COMPLETED = org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
        public static final org.bukkit.Sound TUTORIAL_COMPLETED = org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE;
        public static final org.bukkit.Sound NPC_TALK = org.bukkit.Sound.ENTITY_VILLAGER_AMBIENT;
        public static final org.bukkit.Sound ASSIGNMENT_WRONG = org.bukkit.Sound.ENTITY_VILLAGER_NO;
        public static final org.bukkit.Sound ASSIGNMENT_COMPLETED = org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING;
    }

    public static class Delay {
        public static final int TIMELINE_START = 3;
        public static final int TASK_START = 2;
        public static final int TASK_END = 2;
    }
}
