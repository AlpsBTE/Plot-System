package github.BTEPlotSystem.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseConnection {

    private static Connection connection;

    public DatabaseConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("MySQL JDBC Driver Registered!");

            try {
                FileConfiguration config = github.BTEPlotSystem.BTEPlotSystem.getPlugin().getConfig();
                connection = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/alpsbte",
                        config.getString("review-system.database.username"),
                        config.getString("review-system.database.password"));

                Bukkit.getLogger().log(Level.INFO, "SQL Connection to database established!");
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Connection Failed!", ex);
            }
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "MySQL JDBC Driver not found!", ex);
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static Statement createStatement() throws SQLException {
        return getConnection().createStatement();
    }
}
