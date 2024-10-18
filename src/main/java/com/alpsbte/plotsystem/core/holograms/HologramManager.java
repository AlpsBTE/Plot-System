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

package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class HologramManager {
    public static List<DecentHologramDisplay> activeDisplays = new ArrayList<>();

    public static void reload() {
        for (DecentHologramDisplay display : activeDisplays) {
            // Register and create holograms
            if (PlotSystem.getPlugin().getConfig().getBoolean(((HologramConfiguration) display).getEnablePath())) {
                display.setEnabled(true);
                for (Player player : Objects.requireNonNull(Bukkit.getWorld(display.getLocation().getWorld().getName())).getPlayers())
                    display.create(player);
            }
            else {
                display.setEnabled(false);
                display.removeAll();
            }
        }
    }

    public static Location getLocation(HologramConfiguration configPaths) {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();

        return new Location(Objects.requireNonNull(Utils.getSpawnLocation().getWorld()),
                config.getDouble(configPaths.getXPath()),
                config.getDouble(configPaths.getYPath()),
                config.getDouble(configPaths.getZPath())
        );
    }

    public static void saveLocation(String id, HologramConfiguration configPaths, Location newLocation) {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        config.set(configPaths.getEnablePath(), true);
        config.set(configPaths.getXPath(), newLocation.getX());
        config.set(configPaths.getYPath(), newLocation.getY());
        config.set(configPaths.getZPath(), newLocation.getZ());
        ConfigUtil.getInstance().saveFiles();

        HologramRegister.getActiveDisplays().stream().filter(leaderboard -> leaderboard.getId().equals(id)).findFirst()
                .ifPresent(holo -> holo.setLocation(newLocation));
    }

    public static List<DecentHologramDisplay> getActiveDisplays() {
        return activeDisplays;
    }
}