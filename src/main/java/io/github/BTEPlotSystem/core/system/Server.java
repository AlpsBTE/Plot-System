package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.core.database.DatabaseConnection;
import github.BTEPlotSystem.core.database.builder.StatementBuilder;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Server {
    private final int ID;
    private int ftpConfigurationID;

    private String name;
    private String schematicPath;

    public Server(int ID) throws SQLException {
        this.ID = ID;

        String sql = "SELECT * FROM plotsystem_servers WHERE id = ?";
        ResultSet rs = DatabaseConnection.query(new StatementBuilder(sql)
                .setInt(this.ID).build());

        if (!rs.wasNull()) {
            this.ftpConfigurationID = rs.getInt("ftp_configuration_id");
            if (rs.wasNull()) this.ftpConfigurationID = -1;

            this.name = rs.getString("name");
            this.schematicPath = rs.getString("schematic_path");
        }
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getSchematicPath() {
        return schematicPath;
    }

    public FTPConfiguration getFTPConfiguration() throws SQLException {
        return ftpConfigurationID != -1 ? new FTPConfiguration(ftpConfigurationID) : null;
    }

    public static List<Server> getServers() {
        try {
            String sql = "SELECT id FROM plotsystem_servers";
            ResultSet rs = DatabaseConnection.query(new StatementBuilder(sql).build());

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

        private String address;
        private int port;
        private String username;
        private String password;

        public FTPConfiguration(int ID) throws SQLException {
            this.ID = ID;

            String sql = "SELECT * FROM plotsystem_ftp_configurations WHERE id = ?";
            ResultSet rs = DatabaseConnection.query(new StatementBuilder(sql)
                    .setInt(this.ID).build());

            if (!rs.wasNull()) {
                this.address = rs.getString("address");
                this.port = rs.getInt("port");
                this.username = rs.getString("username");
                this.password = rs.getString("password");
            }
        }

        public int getID() {
            return ID;
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
