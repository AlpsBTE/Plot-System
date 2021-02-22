package github.BTEPlotSystem.core.holograms;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import github.BTEPlotSystem.BTEPlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Level;

public abstract class HolographicDisplay extends Thread {

    private final String hologramName;
    private Hologram hologram;
    private boolean isPlaced = false;

    public HolographicDisplay(String hologramName) {
        this.hologramName = hologramName;

        placeLeaderboard();
        updateLeaderboard();
    }

    public Location getLocation() {
        try {
            FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

            return new Location(Bukkit.getWorld("world"),
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

        config.set(getDefaultPath() + "x", newLocation.getX());
        config.set(getDefaultPath() + "y", newLocation.getY() + 4);
        config.set(getDefaultPath() + "z", newLocation.getZ());

        BTEPlotSystem.getPlugin().saveConfig();

        if(isPlaced) {
            hologram.delete();
            isPlaced = false;
        }
        placeLeaderboard();
        updateLeaderboard();
    }

    public void placeLeaderboard() {
        if(!isPlaced() && getLocation() != null) {
            hologram = HologramsAPI.createHologram(BTEPlotSystem.getPlugin(), getLocation());
            isPlaced = true;
        }
    }

    protected abstract String getTitle();

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

    public void updateLeaderboard() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(BTEPlotSystem.getPlugin(), () -> {
            if(isPlaced) {
                hologram.clearLines();
                insertLines();
            }
        },0, getInterval());
    }

    public String getHologramName() { return hologramName; }

    public Hologram getHologram() { return hologram; }

    public boolean isPlaced() { return isPlaced; }

    public int getInterval() { return 20*60; }

    public String getDefaultPath() {
        return "holograms." + getHologramName() + ".";
    }
}
