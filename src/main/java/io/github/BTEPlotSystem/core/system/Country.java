package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.BTEPlotSystem;
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
        return configFile.getInt("countries." + name + ".head-id");
    }

    public String getFinishedSchematicPath() {
        return configFile.getString("countries." + name + ".finished-schematic-path");
    }
}