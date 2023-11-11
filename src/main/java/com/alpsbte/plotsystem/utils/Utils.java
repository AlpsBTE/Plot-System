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

package com.alpsbte.plotsystem.utils;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.head.AlpsHeadUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.ReviewMenu;
import com.alpsbte.plotsystem.core.menus.companion.CompanionMenu;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.items.CustomHeads;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.math.BlockVector2;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.logging.Level;

import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class Utils {

    // Spawn Location
    public static Location getSpawnLocation() {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();

        if (!config.getString(ConfigPaths.SPAWN_WORLD).equalsIgnoreCase("default")) {
            try {
                MultiverseWorld spawnWorld = PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().getMVWorld(config.getString(ConfigPaths.SPAWN_WORLD));
                return spawnWorld.getSpawnLocation();
            } catch (Exception ignore) {
                Bukkit.getLogger().log(Level.WARNING, String.format("Could not find %s in multiverse config!", ConfigPaths.SPAWN_WORLD));
            }
        }

        return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().getSpawnWorld().getSpawnLocation();
    }

    public static void updatePlayerInventorySlots(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            // Add Items
            if (!player.getInventory().contains(CompanionMenu.getMenuItem(player))) {
                player.getInventory().setItem(8, CompanionMenu.getMenuItem(player));
            }
            if (player.hasPermission("plotsystem.review") && !player.getInventory().contains(ReviewMenu.getMenuItem(player))) {
                player.getInventory().setItem(7, ReviewMenu.getMenuItem(player));
            }
        });
    }


    public static class SoundUtils {
        public final static Sound TELEPORT_SOUND = Sound.ENTITY_ENDERMAN_TELEPORT;
        public final static Sound ERROR_SOUND = Sound.ENTITY_ITEM_BREAK;
        public final static Sound CREATE_PLOT_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        public final static Sound FINISH_PLOT_SOUND = Sound.ENTITY_PLAYER_LEVELUP;
        public final static Sound ABANDON_PLOT_SOUND = Sound.ENTITY_DRAGON_FIREBALL_EXPLODE;
        public final static Sound DONE_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        public final static Sound INVENTORY_CLICK_SOUND = Sound.ENTITY_ITEM_FRAME_ADD_ITEM;
        public final static Sound NOTIFICATION_SOUND = Sound.BLOCK_NOTE_BLOCK_PLING;
    }


    public static class ChatUtils {
        public static void setChatFormat(String infoPrefix, String alertPrefix) {
            ChatUtils.infoPrefix = AlpsUtils.deserialize(infoPrefix);
            ChatUtils.alertPrefix = AlpsUtils.deserialize(alertPrefix);
        }

        private static Component infoPrefix;
        private static Component alertPrefix;

        public static Component getInfoFormat(String info) {
            return infoPrefix.append(LegacyComponentSerializer.legacySection().deserialize(info).color(GREEN));
        }

        public static Component getAlertFormat(String alert) {
            return alertPrefix.append(LegacyComponentSerializer.legacySection().deserialize(alert).color(RED));
        }
    }


    public static class ItemUtils {
        public static String getNoteFormat(String note) {
            return "§c§lNote: §8" + note;
        }

        public static String getActionFormat(String action) { return "§8§l> §c" + action; }

        public static String getColorByPoints(int points) {
            switch (points) {
                case 0:
                    return "§7" + points;
                case 1:
                    return "§4" + points;
                case 2:
                    return "§6" + points;
                case 3:
                    return "§e" + points;
                case 4:
                    return "§2" + points;
                default:
                    return "§a" + points;
            }
        }

        public static String getFormattedDifficulty(PlotDifficulty plotDifficulty) {
            switch (plotDifficulty) {
                case EASY:
                    return "§a§lEasy";
                case MEDIUM:
                    return "§6§lMedium";
                case HARD:
                    return "§c§lHard";
                default:
                    return "";
            }
        }
    }



    public static void registerCustomHeads() {
        for (CustomHeads head : CustomHeads.values()) AlpsHeadUtils.registerCustomHead(head.getId());
    }

    public static HashSet<Vector> getLineBetweenPoints(Vector point1, Vector point2, int pointsInLine){
        double p1X = point1.getX();
        double p1Y = point1.getY();
        double p1Z = point1.getZ();
        double p2X = point2.getX();
        double p2Y = point2.getY();
        double p2Z = point2.getZ();

        double lineAveX = (p2X-p1X)/pointsInLine;
        double lineAveY = (p2Y-p1Y)/pointsInLine;
        double lineAveZ = (p2Z-p1Z)/pointsInLine;

        HashSet<Vector> line = new HashSet<>();
        for(int i = 0; i <= pointsInLine; i++){
            Vector vector = new Vector(p1X + lineAveX * i, p1Y + lineAveY * i, p1Z + lineAveZ * i);
            line.add(vector);
        }
        return line;
    }

    public static HashSet<BlockVector2> getLineBetweenPoints(BlockVector2 point1, BlockVector2 point2, int pointsInLine){
        double p1X = point1.getX();
        double p1Z = point1.getZ();
        double p2X = point2.getX();
        double p2Z = point2.getZ();

        double lineAveX = (p2X-p1X)/pointsInLine;
        double lineAveZ = (p2Z-p1Z)/pointsInLine;

        HashSet<BlockVector2> line = new HashSet<>();
        for(int i = 0; i <= pointsInLine; i++){
            BlockVector2 vector = BlockVector2.at(p1X + lineAveX * i, p1Z + lineAveZ * i);
            line.add(vector);
        }
        return line;
    }
}