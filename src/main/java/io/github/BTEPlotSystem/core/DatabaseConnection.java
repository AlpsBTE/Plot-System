/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package github.BTEPlotSystem.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.logging.Level;

public class DatabaseConnection {

    //private static Connection connection;

    public static void InitializeDatabase() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Bukkit.getLogger().log(Level.INFO, "Successfully registered MariaDB JDBC Driver!");

        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "MySQL JDBC Driver not found!", ex);
        }
    }

    public static Connection getConnection() {
        int retries = 3;
        while (retries > 0) {
            try {
                FileConfiguration config = github.BTEPlotSystem.BTEPlotSystem.getPlugin().getConfig();

                return DriverManager.getConnection(config.getString("database.url") + config.getString("database.name"),
                        config.getString("database.username"),
                        config.getString("database.password"));
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Database connection failed!\n\n" + ex.getMessage());
            }
            retries--;
        }
        return null;
    }

/*    public static Statement createStatement() throws SQLException {
        if (getConnection().isClosed()) {
            ConnectToDatabase();
        }
        return getConnection().createStatement();
    }

    public static PreparedStatement prepareStatement(String query) throws SQLException {
        return getConnection().prepareStatement(query);
    }*/
}
