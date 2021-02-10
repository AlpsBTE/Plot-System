package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.BTEPlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class Utils {
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
