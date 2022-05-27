/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021-2022, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.companion.CompanionMenu;
import com.alpsbte.plotsystem.core.menus.ReviewMenu;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.alpsbte.plotsystem.utils.io.config.ConfigPaths;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import org.bukkit.*;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;
import java.util.logging.Level;

public class Utils {

    // Get custom head
    public static ItemStack getItemHead(CustomHead head) {
        return head != null ? head.getAsItemStack() : new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3).build();
    }

    // Get player head by UUID
    public static ItemStack getPlayerHead(UUID playerUUID) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
        skull.setItemMeta(meta);

        return skull;
    }

    // Sounds
    public static Sound TeleportSound = Sound.ENTITY_ENDERMEN_TELEPORT;
    public static Sound ErrorSound = Sound.ENTITY_ITEM_BREAK;
    public static Sound CreatePlotSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    public static Sound FinishPlotSound = Sound.ENTITY_PLAYER_LEVELUP;
    public static Sound AbandonPlotSound = Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE;
    public static Sound Done = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    public static Sound INVENTORY_CLICK = Sound.ENTITY_ITEMFRAME_ADD_ITEM;

    // Spawn Location
    public static Location getSpawnLocation() {
        FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getConfig();

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

    // Player Messages
    private static final String messagePrefix =  PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.MESSAGE_PREFIX) + " ";

    public static String getInfoMessageFormat(String info) {
        return messagePrefix + PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.MESSAGE_INFO_COLOUR) + info;
    }

    public static String getErrorMessageFormat(String error) {
        return messagePrefix + PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.MESSAGE_ERROR_COLOUR) + error;
    }

    // Item Formatting
    public static String getNoteFormat(String note) {
        return "§c§lNote: §8" + note;
    }

    public static String getActionFormat(String action) { return "§8§l> §c" + action; }

    // Integer Try Parser
    public static Integer TryParseInt(String someText) {
        try {
            return Integer.parseInt(someText);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // Double Try Parser
    public static Double tryParseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
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

    public static String getPointsByColor(int points) {
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

    public static class CustomHead {
        private final ItemStack headItem;

        public CustomHead(String headID) {
            this.headItem = headDatabaseAPI != null && headID != null && Utils.TryParseInt(headID) != null
                    ? headDatabaseAPI.getItemHead(headID) : new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3).build();
        }

        public ItemStack getAsItemStack() {
            return headItem;
        }

        private static HeadDatabaseAPI headDatabaseAPI;

        public static CustomHead WHITE_CONCRETE;
        public static CustomHead GREEN_CONCRETE;
        public static CustomHead YELLOW_CONCRETE;
        public static CustomHead RED_CONCRETE;
        public static CustomHead WHITE_P;

        public static CustomHead ADD_BUTTON;
        public static CustomHead REMOVE_BUTTON;
        public static CustomHead BACK_BUTTON;
        public static CustomHead NEXT_BUTTON;
        public static CustomHead PREVIOUS_BUTTON;

        public static CustomHead GLOBE;

        public static void loadHeadsAsync(HeadDatabaseAPI api) {
            headDatabaseAPI = api;
            Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
                WHITE_CONCRETE = new CustomHead("8614");
                GREEN_CONCRETE = new CustomHead("8621");
                YELLOW_CONCRETE = new CustomHead("8613");
                RED_CONCRETE = new CustomHead("8616");
                WHITE_P = new CustomHead("9282");

                ADD_BUTTON = new CustomHead("9237");
                REMOVE_BUTTON = new CustomHead("9243");
                BACK_BUTTON = new CustomHead("9226");
                NEXT_BUTTON = new CustomHead("9223");
                PREVIOUS_BUTTON = new CustomHead("9226");

                GLOBE = new CustomHead("49973");
            });
        }
    }
}
