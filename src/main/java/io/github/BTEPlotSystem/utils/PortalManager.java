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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import github.BTEPlotSystem.BTEPlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.logging.Level;

public class PortalManager extends Thread {

    // Portals
    public Region Portal_Plot = new CuboidRegion(Vector.toBlockPoint(553, 79, 542), Vector.toBlockPoint(551, 79, 544));

    public Region Portal_Terra = new CuboidRegion(Vector.toBlockPoint(544, 79, 535), Vector.toBlockPoint(542, 79, 533));

    public Region Portal_Event = new CuboidRegion(Vector.toBlockPoint(540, 79, 544), Vector.toBlockPoint(542, 79, 546));

    public void run() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(BTEPlotSystem.getPlugin(), () -> {

            spawnPlotParticles(Portal_Plot.getMinimumPoint(), Portal_Plot.getMaximumPoint());
            spawnPlotParticles(Portal_Terra.getMinimumPoint(), Portal_Terra.getMaximumPoint());
            spawnPlotParticles(Portal_Event.getMinimumPoint(), Portal_Event.getMaximumPoint());

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

    public void spawnPlotParticles(Vector min, Vector max) {
        for (int i = min.getBlockX(); i <= max.getBlockX();i++) {
            for (int j = min.getBlockY(); j <= max.getBlockY(); j++) {
                for (int k = min.getBlockZ(); k <= max.getBlockZ();k++) {
                    new ParticleBuilder(ParticleEffect.CLOUD, new Location(Bukkit.getWorld(BTEPlotSystem.getPlugin().getConfig().getString("lobby-world")), i, j, k))
                            .setOffsetX(0.5f)
                            .setOffsetZ(0.5f)
                            .setSpeed(0.05f) // TODO: Improve Performance
                            .display();
                }
            }
        }
    }
}
