/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

package github.BTEPlotSystem.utils;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import github.BTEPlotSystem.core.config.ConfigPaths;
import github.BTEPlotSystem.utils.items.builder.ItemBuilder;
import org.bukkit.*;
import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.utils.enums.PlotDifficulty;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;
import java.util.logging.Level;

public class Utils {

    // Head Database API
    public static HeadDatabaseAPI headDatabaseAPI;

    public static ItemStack getItemHead(String headID) {
        return headDatabaseAPI != null && headID != null ? headDatabaseAPI.getItemHead(headID) : new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3).build();
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

    // Spawn Location
    public static Location getSpawnLocation() {
        FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

        if (!config.getString(ConfigPaths.SPAWN_WORLD).equalsIgnoreCase("default")) {
            try {
                MultiverseWorld spawnWorld = BTEPlotSystem.getMultiverseCore().getMVWorldManager().getMVWorld(config.getString(ConfigPaths.SPAWN_WORLD));
                return spawnWorld.getSpawnLocation();
            } catch (Exception ignore) {
                Bukkit.getLogger().log(Level.WARNING, String.format("Could not find %s in multiverse config!", ConfigPaths.SPAWN_WORLD));
            }
        }

        return BTEPlotSystem.getMultiverseCore().getMVWorldManager().getSpawnWorld().getSpawnLocation();
    }

    // Player Messages
    private static final String messagePrefix =  BTEPlotSystem.getPlugin().getConfig().getString(ConfigPaths.MESSAGE_PREFIX) + " ";

    public static String getInfoMessageFormat(String info) {
        return messagePrefix + BTEPlotSystem.getPlugin().getConfig().getString(ConfigPaths.MESSAGE_INFO_COLOUR) + info;
    }

    public static String getErrorMessageFormat(String error) {
        return messagePrefix + BTEPlotSystem.getPlugin().getConfig().getString(ConfigPaths.MESSAGE_ERROR_COLOUR) + error;
    }

    // Item Formatting
    public static String getNoteFormat(String note) {
        return "§c§lNote: §8" + note;
    }

    // Integer Try Parser
    public static Integer TryParseInt(String someText) {
        try {
            return Integer.parseInt(someText);
        } catch (NumberFormatException ex) {
            return null;
        }
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
}
