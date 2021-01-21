package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.BTEPlotSystem;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.nio.file.Paths;

public class Schematic {

    private static final FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

    private static final String path = config.getString("SchematicsPath");

    private final int ID;
    private final File file;
    private final String theme;
    private final String coordinates;
    private final String link;

    public Schematic(int ID) {
        Object[] schematics = config.getConfigurationSection("Schematics").getKeys(false).toArray();

        this.ID = ID;
        this.file = Paths.get(path, schematics[ID].toString() + ".schematic").toFile();
        this.theme = config.getString("Schematics." + schematics[ID] + ".Theme");
        this.coordinates = config.getString("Schematics." + schematics[ID] + ".Coordinates");
        this.link = config.getString("Schematics." + schematics[ID] + ".Link");
    }

    public int getID() {
        return ID;
    }

    public File getFile() {
        return file;
    }

    public String getTheme() {
        return theme;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getLink() {
        return link;
    }
}
