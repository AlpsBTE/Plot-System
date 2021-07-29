package github.BTEPlotSystem.utils.ftp;

import github.BTEPlotSystem.BTEPlotSystem;
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

    public static void initializeServers() {
        FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();
        Set<String> serverSection = config.getConfigurationSection("servers").getKeys(false);

        servers.clear();
        for (String serverKey : serverSection) {
            String configPath = "servers." + serverKey + "."; // Temporary till we add ConfigPath class
            if(servers.stream().anyMatch(s -> s.name.equals(serverKey))) {
                Bukkit.getLogger().log(Level.WARNING, "Server (" + serverKey + ") is already registered! Please check your config!");
                continue;
            }

            FTPConfiguration ftpConfiguration = null;
            try {
                String ftpPath = configPath + "ftp."; // Temporary till we add ConfigPath class
                if (config.getBoolean(ftpPath + "enabled")) {
                    ftpConfiguration = new FTPConfiguration(
                            config.getString(ftpPath + "address"),
                            config.getInt(ftpPath + "port"),
                            config.getString(ftpPath + "username"),
                            config.getString(ftpPath + "password")
                    );
                }
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.WARNING, "Could not initialize ftp for server (" + serverKey + ")! Please check your config!");
            }

            String schematicPath;
            try {
                String schemPath = configPath + "schematicPath"; // Temporary till we add ConfigPath class
                if (!config.getString(schemPath).equalsIgnoreCase("default")) {
                    schematicPath = config.getString(schemPath);
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
