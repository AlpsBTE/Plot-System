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

package com.alpsbte.plotsystem.core.system.tutorial.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class TutorialUtils {
    public static String TEXT_HIGHLIGHT_START = "<gold>", TEXT_HIGHLIGHT_END = "</gold>";
    public static String TEXT_CLICK_HIGHLIGHT = "<underlined>";

    public static Component CHAT_PREFIX_COMPONENT = text("»", DARK_GRAY)
            .append(text(" ", GRAY));
    public static Component CHAT_TASK_PREFIX_COMPONENT = text("[", DARK_GRAY)
            .append(text("Tutorial", GOLD).append(text("] ", DARK_GRAY)));

    /**
     * Set a block at a specific location
     *
     * @param world    The world to set the block in
     * @param vector   The vector of the location
     * @param material The material of the block
     */
    public static void setBlockAt(World world, Vector vector, Material material) {
        Location loc = new Location(world, vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        loc.getBlock().setType(material);
    }

    /**
     * Get a list of 3D vectors for the tip holograms
     *
     * @param tutorialConfig the tutorial config file
     * @return A list of vector points
     */
    public static List<Vector> getTipPoints(FileConfiguration tutorialConfig) {
        List<String> tipPointsAsString = tutorialConfig.getStringList(Path.TIP_HOLOGRAM_COORDINATES);

        List<Vector> tipPoints = new ArrayList<>();
        tipPointsAsString.forEach(point -> {
            String[] split = point.trim().split(",");
            tipPoints.add(new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2])));
        });
        return tipPoints;
    }

    /**
     * Get a list of documentation links which can be opened by clicking on the hologram
     *
     * @param tutorialConfig the tutorial config file
     * @return A list of documentation links
     */
    public static List<String> getDocumentationLinks(FileConfiguration tutorialConfig) {
        return tutorialConfig.getStringList(Path.DOCUMENTATION_LINKS);
    }

    public static class Sound {
        public static final org.bukkit.Sound STAGE_COMPLETED = org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
        public static final org.bukkit.Sound TUTORIAL_COMPLETED = org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE;
        public static final org.bukkit.Sound NPC_TALK = org.bukkit.Sound.ENTITY_VILLAGER_AMBIENT;
        public static final org.bukkit.Sound ASSIGNMENT_START = org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        public static final org.bukkit.Sound ASSIGNMENT_WRONG = org.bukkit.Sound.ENTITY_VILLAGER_NO;
        public static final org.bukkit.Sound ASSIGNMENT_COMPLETED = org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING;
    }

    public static class Delay {
        public static final int TIMELINE_START = 3;
        public static final int TASK_START = 2;
        public static final int TASK_END = 2;
    }

    public static class Path {
        public static final String TUTORIAL_ID = "tutorial-id";
        public static final String TUTORIAL_ITEM_NAME = "tutorial-item-name";
        public static final String TUTORIAL_STAGES = "tutorial-stages";
        public static final String TUTORIAL_WORLDS = "tutorial-worlds";
        public static final String TUTORIAL_WORLDS_SPAWN_PLAYER = "spawn-player";
        public static final String TUTORIAL_WORLDS_SPAWN_NPC = "spawn-npc";
        public static final String TIP_HOLOGRAM_COORDINATES = "tip-hologram-coordinates";
        public static final String DOCUMENTATION_LINKS = "documentation-links";
    }
}
