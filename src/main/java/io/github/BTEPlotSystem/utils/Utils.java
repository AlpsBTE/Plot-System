package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.BTEPlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

public class Utils {

    public static Sound TeleportSound = Sound.ENTITY_ENDERMEN_TELEPORT;
    public static Sound ErrorSound = Sound.ENTITY_ITEM_BREAK;
    public static Sound CreatePlotSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    public static Sound FinishPlotSound = Sound.ENTITY_PLAYER_LEVELUP;
    public static Sound AbandonPlotSound = Sound.ENTITY_ENDERDRAGON_FIREBALL_EXPLODE;
    public static Sound Done = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

    public static Location getSpawnPoint() {
        FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

        return new Location(Bukkit.getWorld("world"),
                config.getDouble("spawn-point.x"),
                config.getDouble("spawn-point.y"),
                config.getDouble("spawn-point.z"),
                (float) config.getDouble("spawn-point.yaw"),
                (float) config.getDouble("spawn-point.pitch")
        );
    }
}
