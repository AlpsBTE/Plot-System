package github.BTEPlotSystem.utils.ftp;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.config.ConfigPaths;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class Server {
    private final static List<Server> servers = new ArrayList<>();

    private final String name;
    private final String schematicPath;

    private final FTPConfiguration ftpConfiguration;

    public Server(String name, String schematicPath, FTPConfiguration ftpConfiguration) {
        this.name = name;
        this.schematicPath = schematicPath;
        this.ftpConfiguration = ftpConfiguration;
    }

    public String getName() {
        return name;
    }

    public String getSchematicPath() {
        return schematicPath;
    }

    public FTPConfiguration getFTPConfiguration() {
        return ftpConfiguration;
    }

    public static void init() {
        FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();
        Set<String> serverSection = config.getConfigurationSection(ConfigPaths.SERVERS).getKeys(false);

        servers.clear();
        for (String serverKey : serverSection) {
            String configPath = ConfigPaths.SERVERS + serverKey;
            if(servers.stream().anyMatch(s -> s.name.equals(serverKey))) {
                Bukkit.getLogger().log(Level.WARNING, "Server (" + serverKey + ") is already registered! Please check your config!");
                continue;
            }

            FTPConfiguration ftpConfiguration = null;
            try {
                if (config.getBoolean(configPath + ConfigPaths.SERVERS_FTP_ENABLED)) {
                    ftpConfiguration = new FTPConfiguration(
                            config.getString(configPath + ConfigPaths.SERVERS_FTP_ADDRESS),
                            config.getInt(configPath + ConfigPaths.SERVERS_FTP_PORT),
                            config.getString(configPath + ConfigPaths.SERVERS_FTP_USERNAME),
                            config.getString(configPath + ConfigPaths.SERVERS_FTP_PASSWORD)
                    );
                }
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.WARNING, "Could not initialize ftp for server (" + serverKey + ")! Please check your config!");
            }

            String schematicPath;
            try {
                if (!config.getString(configPath + ConfigPaths.SERVERS_SCHEMATIC_PATH).equalsIgnoreCase("default")) {
                    schematicPath = config.getString(configPath + ConfigPaths.SERVERS_SCHEMATIC_PATH);
                    schematicPath = !schematicPath.startsWith(File.separator) ? File.separator + schematicPath : schematicPath;
                    schematicPath = !schematicPath.endsWith(File.separator) ? schematicPath + File.separator : schematicPath;
                } else {
                    if (ftpConfiguration == null) {
                        Bukkit.getLogger().log(Level.WARNING, "Could not initialize server (" + serverKey + ")! Please check your config!");
                        Bukkit.getLogger().log(Level.WARNING, "If FTP is enabled, schematicPath cannot be default!");
                        continue;
                    }
                    schematicPath = PlotManager.getDefaultSchematicPath();
                }
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.WARNING, "Could not initialize server (" + serverKey + ")! Please check your config!");
                continue;
            }

            servers.add(new Server(serverKey, schematicPath, ftpConfiguration));
        }

        if(servers.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "No server could be registered. Please check your config!");
        }
    }

    public static List<Server> getServers() {
        return servers;
    }

    public static class FTPConfiguration {
        private final String address;
        private final int port;
        private final String username;
        private final String password;

        public FTPConfiguration(String address, int port, String username, String password) {
            this.address = address;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public String getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
