package github.BTEPlotSystem;
import github.BTEPlotSystem.core.EventListener;
import github.BTEPlotSystem.utils.conversion.CoordinateConversion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.logging.Level;

public class BTEPlotSystem extends JavaPlugin {
    private static BTEPlotSystem plugin;
    private FileConfiguration config;
    private File configFile;

    @Override
    public void onEnable() {
        plugin = this;

        reloadConfig();

        // Add Listener
        this.getServer().getPluginManager().registerEvents(new EventListener(), plugin);
        this.getServer().getPluginManager().registerEvents(new MenuFunctionListener(), plugin);

        // Add Commands

        getLogger().log(Level.INFO, "Successfully enabled BTEPlotSystem plugin.");
        getLogger().log(Level.INFO, "MC Cords: 3662176 -4651104 IRL Cords: " + Arrays.toString(CoordinateConversion.convertToGeo(-42.39012107174608, 19.198042385454855)));
    }

    public static BTEPlotSystem getPlugin() {
        return plugin;
    }

    @Override
    public void reloadConfig() {
        configFile = new File(getDataFolder(), "defaultConfig.yml");
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            // Look for default configuration file
            try {
                Reader defConfigStream = new InputStreamReader(this.getResource("defaultConfig.yml"), "UTF8");

                config = YamlConfiguration.loadConfiguration(defConfigStream);
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Could not load default configuration file", ex);
            }
        }
        saveConfig();
    }

    @Override
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    @Override
    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }

        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }
}
