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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.logging.Level;

public class DatabaseConnection {

    private final static HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;

    public static void InitializeDatabase() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Bukkit.getLogger().log(Level.INFO, "Successfully registered MariaDB JDBC Driver!");

            FileConfiguration configFile = github.BTEPlotSystem.BTEPlotSystem.getPlugin().getConfig();

            config.setJdbcUrl(configFile.getString("database.url") + configFile.getString("database.name"));
            config.setUsername(configFile.getString("database.username"));
            config.setPassword(configFile.getString("database.password"));
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            CreateDefaultTables();
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while initializing database!", ex);
        }
    }

    public static Connection getConnection() {
        int retries = 3;
        while (retries > 0) {
            try {
                return dataSource.getConnection();
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Database connection failed!\n\n" + ex.getMessage());
            }
            retries--;
        }
        return null;
    }

    private static void CreateDefaultTables() {
        try (Connection con = DatabaseConnection.getConnection()) {
            // Create Players table if not exists
            PreparedStatement ps1 = con.prepareStatement("create table if not exists players" +
                    "(" +
                    "    uuid            varchar(36)   not null  primary key," +
                    "    name            varchar(45)   not null," +
                    "    score           int default 0 not null," +
                    "    completedBuilds int default 0 not null," +
                    "    firstSlot       int           null," +
                    "    secondSlot      int           null," +
                    "    thirdSlot       int           null" +
                    ");");
            ps1.executeUpdate();

            // Create Plots table if not exists
            PreparedStatement ps2 = con.prepareStatement("create table if not exists plots" +
                    "(" +
                    "    idplot        int auto_increment" +
                    "        primary key," +
                    "    idcity        int                                                                            not null," +
                    "    mcCoordinates varchar(100)                                                                   not null," +
                    "    status        enum ('unclaimed', 'unfinished', 'unreviewed', 'complete') default 'unclaimed' not null," +
                    "    score         varchar(100)                                                                   null," +
                    "    uuidplayer    varchar(36)                                                                    null," +
                    "    uuidMembers   varchar(110)                                                                   null," +
                    "    iddifficulty  int                                                        default 1           not null," +
                    "    lastActivity  date                                                                           null," +
                    "    idreview      int                                                                            null," +
                    "    isPasted      tinyint                                                    default 0           not null" +
                    ");");
            ps2.executeUpdate();

            // Create City Projects table if not exists
            PreparedStatement ps3 = con.prepareStatement("create table if not exists cityProjects" +
                    "(" +
                    "    idcityProject int auto_increment primary key," +
                    "    name          varchar(45)                   not null," +
                    "    country       varchar(45)                   null," +
                    "    description   varchar(255)                  null," +
                    "    tags          varchar(45)                   null," +
                    "    visible       tinyint default 1             not null" +
                    ");");
            ps3.executeUpdate();

            // Create Review table if not exists
            PreparedStatement ps4 = con.prepareStatement("create table if not exists reviews" +
                    "(" +
                    "    id_review     int auto_increment primary key," +
                    "    uuid_reviewer varchar(36)                        not null," +
                    "    rating        varchar(36)                        not null," +
                    "    feedbackText  varchar(420) default 'No Feedback' null," +
                    "    isSent        tinyint      default 0             null" +
                    ");");
            ps4.executeUpdate();

            // Create Review table if not exists
            PreparedStatement ps5 = con.prepareStatement("create table if not exists difficulties" +
                    "(" +
                    "    iddifficulty     int auto_increment primary key," +
                    "    name             varchar(45)      null," +
                    "    multiplier       double default 1 not null," +
                    "    scoreRequirement int    default 0 not null" +
                    ");");
            ps5.executeUpdate();
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE,"An error occurred while creating database");
        }
    }
}
