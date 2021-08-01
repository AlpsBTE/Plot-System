package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.config.ConfigPaths;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.ftp.Server;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class Country {
    private final static List<Country> countries = new ArrayList<>();

    private final String name;
    private final ItemStack head;
    private final Server server;

    public Country(String name, String headID, Server server) {
        this.name = name;
        this.head = Utils.getItemHead(headID);
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public ItemStack getHead() {
        return head;
    }

    public Server getServer() {
        return server;
    }

    public static void init() {
        if (!Server.getServers().isEmpty()) {
            FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();
            Set<String> countrySection = config.getConfigurationSection(ConfigPaths.COUNTRIES).getKeys(false);

            countries.clear();
            for (String countryKey : countrySection) {
                String configPath = ConfigPaths.COUNTRIES + countryKey;
                if(countries.stream().anyMatch(s -> s.name.equals(countryKey))) {
                    Bukkit.getLogger().log(Level.WARNING, "Country (" + countryKey + ") is already registered! Please check your config!");
                    continue;
                }

                String headID;
                Server server;
                try {
                    headID = config.getString(configPath + ConfigPaths.COUNTRIES_HEAD_ID);

                    String serverName = config.getString(configPath + ConfigPaths.COUNTRIES_SERVER);
                    server = Server.getServers().stream().filter(c -> c.getName().equals(serverName)).findFirst().orElse(null);
                    if (server == null) {
                        Bukkit.getLogger().log(Level.WARNING, "Could not initialize country (" + countryKey + ")! Please check your config!");
                        Bukkit.getLogger().log(Level.WARNING, "Server (" + serverName + ") in country (" + countryKey + ") is not registered!");
                        continue;
                    }
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not initialize country (" + countryKey + ")! Please check your config!");
                    continue;
                }

                countries.add(new Country(countryKey, headID, server));
            }

            if (countries.isEmpty()) {
                Bukkit.getLogger().log(Level.WARNING, "No country could be registered! Please check your config!");
            }
        }
    }

    public static List<Country> getCountries() {
        return countries;
    }
}