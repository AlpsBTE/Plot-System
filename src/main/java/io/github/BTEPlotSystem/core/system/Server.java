package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.core.database.DatabaseConnection;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Server {
    private final int ID;
    private int ftpConfigurationID;

    private String name;

    public Server(int ID) throws SQLException {
        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement("SELECT ftp_configuration_id, name FROM plotsystem_servers WHERE id = ?")
                .setValue(this.ID).executeQuery();

        if (rs.next()) {
            this.ftpConfigurationID = rs.getInt(1);
            if (rs.wasNull()) this.ftpConfigurationID = -1;

            this.name = rs.getString(2);
        }
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public FTPConfiguration getFTPConfiguration() throws SQLException {
        return ftpConfigurationID != -1 ? new FTPConfiguration(ftpConfigurationID) : null;
    }

    public static List<Server> getServers() {
        try {;
            ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_servers").executeQuery();

            List<Server> servers = new ArrayList<>();
            while (rs.next()) {
                servers.add(new Server(rs.getInt(1)));
            }
            return servers;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return new ArrayList<>();
    }

    public static class FTPConfiguration {
        private final int ID;

        private String schematicPath;
        private String address;
        private int port;
        private String username;
        private String password;

        public FTPConfiguration(int ID) throws SQLException {
            this.ID = ID;

            ResultSet rs = DatabaseConnection.createStatement("SELECT schematics_path, address, port, username, password FROM plotsystem_ftp_configurations WHERE id = ?")
                    .setValue(this.ID).executeQuery();

            if (rs.next()) {
                this.schematicPath = rs.getString(1);
                this.address = rs.getString(2);
                this.port = rs.getInt(3);
                this.username = rs.getString(4);
                this.password = rs.getString(5);
            }
        }

        public int getID() {
            return ID;
        }

        public String getSchematicPath() {
            if (schematicPath != null) {
                schematicPath = !schematicPath.startsWith("/") ? File.separator + schematicPath : schematicPath;
                schematicPath = schematicPath.endsWith("/") ? schematicPath.substring(0, schematicPath.length() - 1) : schematicPath;
            }
            return schematicPath;
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
