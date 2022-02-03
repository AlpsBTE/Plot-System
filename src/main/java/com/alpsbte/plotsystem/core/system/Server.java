package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import org.bukkit.Bukkit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT ftp_configuration_id, name FROM plotsystem_servers WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                this.ftpConfigurationID = rs.getInt(1);
                if (rs.wasNull()) this.ftpConfigurationID = -1;

                this.name = rs.getString(2);
            }

            DatabaseConnection.closeResultSet(rs);
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
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_servers").executeQuery()) {
            List<Server> servers = new ArrayList<>();
            while (rs.next()) {
                servers.add(new Server(rs.getInt(1)));
            }

            DatabaseConnection.closeResultSet(rs);

            return servers;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return new ArrayList<>();
    }

    public static Server addServer(String name) throws SQLException {
        int id = DatabaseConnection.getTableID("plotsystem_servers");
        DatabaseConnection.createStatement("INSERT INTO plotsystem_servers (id, name) VALUES (?, ?)")
                .setValue(id)
                .setValue(name).executeUpdate();
        return new Server(id);
    }

    public static void removeServer(int serverID) throws SQLException {
        DatabaseConnection.createStatement("DELETE FROM plotsystem_servers WHERE id = ?")
                .setValue(serverID).executeUpdate();
    }

    public static void setFTP(int serverID, int ftpID) throws SQLException {
        if (ftpID != -1) {
            DatabaseConnection.createStatement("UPDATE plotsystem_servers SET ftp_configuration_id = ? WHERE id = ?")
                    .setValue(ftpID)
                    .setValue(serverID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_servers SET ftp_configuration_id = DEFAULT WHERE id = ?")
                    .setValue(serverID).executeUpdate();
        }
    }
}
