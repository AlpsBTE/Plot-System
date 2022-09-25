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

package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.plotsystem.PlotSystem;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.alpsbte.plotsystem.utils.io.config.ConfigPaths;
import com.alpsbte.plotsystem.utils.Utils;
import com.gmail.filoghost.holographicdisplays.api.line.HologramLine;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Level;

public abstract class HolographicDisplay {
    private final String hologramName;
    private Hologram hologram;
    private boolean isPlaced = false;

    public HolographicDisplay(String hologramName) {
        this.hologramName = hologramName;
    }

    public void show() {
        reloadHologram();
        updateHologram();
    }

    public void hide() {
        if (isPlaced()) {
            getHologram().delete();
            isPlaced = false;
        }
    }

    public Location getLocation() {
        try {
            FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getConfig();

            return new Location(Utils.getSpawnLocation().getWorld(),
                    config.getDouble(getDefaultPath() + ConfigPaths.HOLOGRAMS_X),
                    config.getDouble(getDefaultPath() + ConfigPaths.HOLOGRAMS_Y),
                    config.getDouble(getDefaultPath() + ConfigPaths.HOLOGRAMS_Z)
            );
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not read hologram location of " + getHologramName() + "!", ex);
            return null;
        }
    }

    public void setLocation(Location newLocation) {
        FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getConfig();

        config.set(getDefaultPath() + ConfigPaths.HOLOGRAMS_ENABLED, true);
        config.set(getDefaultPath() + ConfigPaths.HOLOGRAMS_X, newLocation.getX());
        config.set(getDefaultPath() + ConfigPaths.HOLOGRAMS_Y, newLocation.getY() + 4);
        config.set(getDefaultPath() + ConfigPaths.HOLOGRAMS_Z, newLocation.getZ());

        PlotSystem.getPlugin().getConfigManager().saveFiles();

        reloadHologram();
    }

    public void placeHologram() {
        if (!isPlaced() && getLocation() != null) {
            hologram = HologramsAPI.createHologram(PlotSystem.getPlugin(), getLocation());
            isPlaced = true;
        }
    }

    protected String getHeader() {
        return "§7---------------";
    }

    protected String getFooter() {
        return "§7---------------";
    }

    protected void insertLines() {
        replaceLine(0, getItem());

        replaceLine(1, getTitle());
        replaceLine(2, getHeader());

        List<DataLine> data = getDataLines();
        for (int i = 2; i < data.size() + 2; i++) {
            replaceLine(i + 1, data.get(i - 2).getLine());
        }

        replaceLine(data.size() + 3, getFooter());
    }

    protected void replaceLine(int line, ItemStack item) {
        if (getHologram().size() < line + 1) {
            getHologram().insertItemLine(line, item);
        } else {
            HologramLine hologramLine = getHologram().getLine(line);
            if (hologramLine instanceof TextLine) {
                // we're replacing the line with a different type, so we will have to destroy old line to replace with new type
                getHologram().insertItemLine(line, item);
                getHologram().removeLine(line + 1);
            } else {
                ((ItemLine) hologramLine).setItemStack(item);
            }
        }
    }

    protected void replaceLine(int line, String text) {
        if (getHologram().size() < line + 1) {
            getHologram().insertTextLine(line, text);
        } else {
            HologramLine hologramLine = getHologram().getLine(line);
            if (hologramLine instanceof ItemLine) {
                // we're replacing the line with a different type, so we will have to destroy old line to replace with new type
                getHologram().insertTextLine(line, text);
                getHologram().removeLine(line + 1);
            } else {
                ((TextLine) hologramLine).setText(text);
            }
        }
    }

    protected abstract List<DataLine> getDataLines();

    protected abstract ItemStack getItem();

    public abstract void onShutdown();

    public void updateHologram() {
        if (isPlaced()) {
            insertLines();
        }
    }

    public void reloadHologram() {
        if (isPlaced) {
            hologram.delete();
            isPlaced = false;
        }
        placeHologram();
    }

    public String getHologramName() {
        return hologramName;
    }

    protected abstract String getTitle();

    public Hologram getHologram() {
        return hologram;
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public long getInterval() {
        return PlotSystem.getPlugin().getConfigManager().getConfig().getInt(ConfigPaths.DISPLAY_OPTIONS_INTERVAL) * 20L;
    }

    public String getDefaultPath() {
        return ConfigPaths.HOLOGRAMS + getHologramName();
    }


    public interface DataLine {
        String getLine();
    }

    public static class LeaderboardPositionLine implements DataLine {
        protected int position;
        protected int score;
        protected String username;

        public LeaderboardPositionLine(int position, String username, int score) {
            this.position = position;
            this.username = username;
            this.score = score;
        }

        @Override
        public String getLine() {
            return "§e#" + position + " " + (username != null ? "§a" + username : "§8No one, yet") + " §7- §b" + score;
        }
    }
}