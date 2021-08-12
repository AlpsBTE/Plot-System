package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.core.database.DatabaseConnection;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class FTPConfiguration {
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

    public static List<FTPConfiguration> getFTPConfigurations() {
        try {
            ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_ftp_configurations").executeQuery();

            List<FTPConfiguration> ftpConfigurations = new ArrayList<>();
            while (rs.next()) {
                ftpConfigurations.add(new FTPConfiguration(rs.getInt(1)));
            }
            return ftpConfigurations;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return new ArrayList<>();
    }

    public static boolean add(String address, int port, String username, String password) throws SQLException {
        if (getFTPConfigurations().stream().noneMatch(ftp -> ftp.getAddress().equals(address))) {
            DatabaseConnection.createStatement("INSERT INTO plotsystem_ftp_configurations (address, port, username, password) VALUES (?, ?, ?, ?)")
                    .setValue(address).setValue(port).setValue(username).setValue(password).executeUpdate();
            return true;
        }
        return false;
    }

    public static boolean remove(int ID) throws SQLException {
        if (getFTPConfigurations().stream().anyMatch(ftp -> ftp.getID() == ID)) {
            DatabaseConnection.createStatement("DELETE FROM plotsystem_ftp_configurations WHERE id = ?")
                    .setValue(ID).executeUpdate();
            return true;
        }
        return false;
    }
}
