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

package github.BTEPlotSystem.core.holograms;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import github.BTEPlotSystem.BTEPlotSystem;
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
        placeHologram();
        updateHologram();
    }

    public void hide() {
        if(isPlaced()){
            getHologram().delete();
            isPlaced = false;
        }
    }

    public Location getLocation() {
        try {
            FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

            return new Location(Bukkit.getWorld(config.getString("lobby-world")),
                    config.getDouble(getDefaultPath() + "x"),
                    config.getDouble(getDefaultPath() + "y"),
                    config.getDouble(getDefaultPath() + "z")
            );
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not read hologram location of " + getHologramName() + "!", ex);
            return null;
        }
    }

    public void setLocation(Location newLocation) {
        FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

        config.set(getDefaultPath() + "enabled", true);
        config.set(getDefaultPath() + "x", newLocation.getX());
        config.set(getDefaultPath() + "y", newLocation.getY() + 4);
        config.set(getDefaultPath() + "z", newLocation.getZ());

        BTEPlotSystem.getPlugin().saveConfig();

        if(isPlaced) {
            hologram.delete();
            isPlaced = false;
        }

        placeHologram();
    }

    public void placeHologram() {
        if(!isPlaced() && getLocation() != null) {
            hologram = HologramsAPI.createHologram(BTEPlotSystem.getPlugin(), getLocation());
            isPlaced = true;
        }
    }

    protected void insertLines() {
        getHologram().insertItemLine(0, getItem());

        getHologram().insertTextLine(1, getTitle());
        getHologram().insertTextLine(2, "§7---------------");

        List<String> data = getDataLines();
        for(int i = 2; i < data.size() + 2; i++) {
            getHologram().insertTextLine(i + 1,"§e#" + (i - 1) + " §a" +data.get(i - 2).split(",")[0] + " §7- §b" + data.get(i - 2).split(",")[1]);
        }

        getHologram().insertTextLine(data.size() + 3, "§7---------------");
    }

    protected abstract List<String> getDataLines();

    protected abstract ItemStack getItem();

    public void updateHologram() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(BTEPlotSystem.getPlugin(), () -> {
            if(isPlaced()) {
                hologram.clearLines();
                insertLines();
            }
        },0, getInterval());
    }

    public String getHologramName() { return hologramName; }

    protected abstract String getTitle();

    public Hologram getHologram() { return hologram; }

    public boolean isPlaced() { return isPlaced; }

    public int getInterval() { return 20*60; }

    public String getDefaultPath() {
        return "holograms." + getHologramName() + ".";
    }
}
