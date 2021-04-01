package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.BTEPlotSystem;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Utils {

    // Head Database API
    public static HeadDatabaseAPI headDatabaseAPI;

    public static ItemStack getItemHead(String headID) {
        return headDatabaseAPI != null ? headDatabaseAPI.getItemHead(headID) : new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3).build();
    }

    // Sounds
    public static Sound TeleportSound = Sound.ENTITY_ENDERMEN_TELEPORT;
    public static Sound ErrorSound = Sound.ENTITY_ITEM_BREAK;
    public static Sound CreatePlotSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    public static Sound FinishPlotSound = Sound.ENTITY_PLAYER_LEVELUP;
    public static Sound AbandonPlotSound = Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE;
    public static Sound Done = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

    // Spawn Location
    public static Location getSpawnPoint() {
        FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

        return new Location(Bukkit.getWorld("Hub"),
                config.getDouble("spawn-point.x"),
                config.getDouble("spawn-point.y"),
                config.getDouble("spawn-point.z"),
                (float) config.getDouble("spawn-point.yaw"),
                (float) config.getDouble("spawn-point.pitch")
        );
    }

    // Player Messages
    private static final String messagePrefix = "§7§l>> ";

    public static String getInfoMessageFormat(String info) {
        return messagePrefix + "§a" + info;
    }

    public static String getErrorMessageFormat(String error) {
        return messagePrefix + "§c" + error;
    }

    // Servers
    public final static String PLOT_SERVER = "ALPS-1";

    public final static String TERRA_SERVER = "ALPS-2";

    public final static String EVENT_SERVER = "ALPS-3";

    public final static String TEST_SERVER = "ALPS-4";

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
}
