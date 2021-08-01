package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.config.ConfigPaths;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.Set;

public class Country {
    public Country (int ID){
        this.ID = ID;
        this.name = (String)configFile.getKeys(false).toArray()[ID];
    }

    public Country (String name) {
        this.name = name;
        Set<String> keys = configFile.getKeys(false);
        this.ID = Arrays.binarySearch(keys.toArray(new String[0]), name);
    }

    public FileConfiguration configFile =  BTEPlotSystem.getPlugin().getConfig();
    public int ID;
    public String name;

    public int getHeadID() {
        return configFile.getInt(ConfigPaths.COUNTRIES + name + ConfigPaths.COUNTRIES_HEAD_ID);
    }

    public String getServer() {
        return configFile.getString(ConfigPaths.COUNTRIES + name + ConfigPaths.COUNTRIES_SERVER);
    }

    public String getFinishedSchematicPath() {
        return configFile.getString(ConfigPaths.SERVERS + getServer() + ConfigPaths.SERVERS_SCHEMATIC_PATH + "finished-plots/");
    }
}