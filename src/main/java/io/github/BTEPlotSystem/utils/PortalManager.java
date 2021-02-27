package github.BTEPlotSystem.utils;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import github.BTEPlotSystem.BTEPlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class PortalManager extends Thread {

    // Portals
    public Region Portal_Plot = new CuboidRegion(Vector.toBlockPoint(553, 79, 542), Vector.toBlockPoint(551, 79, 544));

    public Region Portal_Terra = new CuboidRegion(Vector.toBlockPoint(544, 79, 535), Vector.toBlockPoint(542, 79, 533));

    public Region Portal_Event = new CuboidRegion(Vector.toBlockPoint(540, 79, 544), Vector.toBlockPoint(542, 79, 546));

    public void run() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(BTEPlotSystem.getPlugin(), () -> {

            for(Player player : Bukkit.getOnlinePlayers()) {
                try {
                    Vector playerLocation = Vector.toBlockPoint(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                    if(Portal_Plot.contains(playerLocation)) {
                        player.teleport(Utils.getSpawnPoint());
                        player.performCommand("companion");
                    } else if(Portal_Terra.contains(playerLocation)) {
                        BTEPlotSystem.getPlugin().connectPlayer(player, Utils.TERRA_SERVER);
                    } else if(Portal_Event.contains(playerLocation)) {
                        FileConfiguration config = BTEPlotSystem.getPlugin().getNavigatorConfig();

                        if(config.getBoolean("servers.event.joinable") || player.hasPermission("alpsbte.joinEventStaff")) {
                            if(player.hasPermission("alpsbte.joinEvent")) {
                                BTEPlotSystem.getPlugin().connectPlayer(player, Utils.EVENT_SERVER);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "An error occurred while handling player in portal.");
                }
            }
        }, 1, 15);
    }
}
