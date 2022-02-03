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

package com.alpsbte.plotsystem.core.database;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.config.ConfigPaths;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class DatabaseConnection {

    private final static HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;

    private static String URL;
    private static String name;
    private static String username;
    private static String password;

    private static int connectionClosed, connectionOpened;

    public static void InitializeDatabase() throws ClassNotFoundException, SQLException {
        Class.forName("org.mariadb.jdbc.Driver");

        FileConfiguration configFile = PlotSystem.getPlugin().getConfigManager().getConfig();
        URL = configFile.getString(ConfigPaths.DATABASE_URL);
        name = configFile.getString(ConfigPaths.DATABASE_NAME);
        username = configFile.getString(ConfigPaths.DATABASE_USERNAME);
        password = configFile.getString(ConfigPaths.DATABASE_PASSWORD);

        createDatabase();

        config.setJdbcUrl(URL + name);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);

        createTables();
}

    @Deprecated
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

    public static StatementBuilder createStatement(String sql) {
        return new StatementBuilder(sql);
    }

    public static void closeResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet.isClosed()
        && resultSet.getStatement().isClosed()
        && resultSet.getStatement().getConnection().isClosed())
            return;

        resultSet.close();
        resultSet.getStatement().close();
        resultSet.getStatement().getConnection().close();

        connectionClosed++;

        if(connectionOpened > connectionClosed + 5)
            Bukkit.getLogger().log(Level.SEVERE, "There are multiple database connections opened. Please report this issue.");
    }

    private static void createDatabase() throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, username, password)) {
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + name);
            }
        }
    }

    private static void createTables() {
        try (Connection con = dataSource.getConnection()) {
            for (String table : Tables.getTables()) {
                Objects.requireNonNull(con).prepareStatement(table).executeUpdate();
            }

            try (ResultSet rs = con.prepareStatement("SELECT COUNT(id) FROM plotsystem_difficulties").executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt(1) == 0) {
                        con.prepareStatement("INSERT INTO plotsystem_difficulties (id, name) VALUES (1, 'EASY')").executeUpdate();
                        con.prepareStatement("INSERT INTO plotsystem_difficulties (id, name, multiplier) VALUES (2, 'MEDIUM', 1.5)").executeUpdate();
                        con.prepareStatement("INSERT INTO plotsystem_difficulties (id, name, multiplier) VALUES (3, 'HARD', 2)").executeUpdate();
                    }
                }
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while creating database table!");
        }
    }

    /**
     * Returns a missing auto increment id
     * @param table in the database
     * @return smallest missing auto increment id in the table
     */
    public static int getTableID(String table) {
        try {
            String query ="SELECT id + 1 available_id FROM $table t WHERE NOT EXISTS (SELECT * FROM $table WHERE $table.id = t.id + 1) ORDER BY id LIMIT 1"
                    .replace("$table", table);
            try (ResultSet rs = DatabaseConnection.createStatement(query).executeQuery()) {
                if (rs.next()) {
                    int i = rs.getInt(1);
                    DatabaseConnection.closeResultSet(rs);
                    return i;
                }

                DatabaseConnection.closeResultSet(rs);
                return 1;
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return 1;
        }
    }

    public static class StatementBuilder {
        private final String sql;
        private final List<Object> values = new ArrayList<>();

        public StatementBuilder(String sql) {
            this.sql = sql;
        }

        public StatementBuilder setValue(Object value) {
            values.add(value instanceof Boolean ? ((boolean) value ? 1 : 0) : value);
            return this;
        }

        public ResultSet executeQuery() throws SQLException {
            Connection con = dataSource.getConnection();
            PreparedStatement ps = Objects.requireNonNull(con).prepareStatement(sql);
            ResultSet rs = iterateValues(ps).executeQuery();

            connectionOpened++;

            return rs;
        }

        public void executeUpdate() throws SQLException {
            try (Connection con = dataSource.getConnection()) {
                try (PreparedStatement ps = Objects.requireNonNull(con).prepareStatement(sql)) {
                    iterateValues(ps).executeUpdate();
                }
            }
        }

        private PreparedStatement iterateValues(PreparedStatement ps) throws SQLException {
            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
            }
            return ps;
        }
    }

    private static class Tables {
        private final static List<String> tables;

        public static List<String> getTables() {
            return tables;
        }

        static {
            tables = Arrays.asList(
                    // FTP Configurations
                    "CREATE TABLE IF NOT EXISTS `plotsystem_ftp_configurations`" +
                            "(" +
                            " `id`              int NOT NULL AUTO_INCREMENT ," +
                            " `address`         varchar(255) NOT NULL ," +
                            " `port`            int NOT NULL ," +
                            " `isSFTP`          tinyint NOT NULL DEFAULT 1 ," +
                            " `username`        varchar(255) NOT NULL ," +
                            " `password`        varchar(255) NOT NULL ," +
                            " `schematics_path` varchar(255) NULL ," +
                            "PRIMARY KEY (`id`)" +
                            ");",

                    // Servers
                    "CREATE TABLE IF NOT EXISTS `plotsystem_servers`" +
                            "(" +
                            " `id`                   int NOT NULL AUTO_INCREMENT ," +
                            " `ftp_configuration_id` int NULL ," +
                            " `name`                 varchar(45) NOT NULL ," +
                            "PRIMARY KEY (`id`)," +
                            "KEY `fkIdx_30` (`ftp_configuration_id`)," +
                            "CONSTRAINT `FK_29` FOREIGN KEY `fkIdx_30` (`ftp_configuration_id`) REFERENCES `plotsystem_ftp_configurations` (`id`)" +
                            ");",

                    // Countries
                    "CREATE TABLE IF NOT EXISTS `plotsystem_countries`" +
                            "(" +
                            " `id`        int NOT NULL AUTO_INCREMENT ," +
                            " `server_id` int NOT NULL ," +
                            " `name`      varchar(45) NOT NULL ," +
                            " `head_id`   varchar(10) NULL ," +
                            "PRIMARY KEY (`id`)," +
                            "KEY `fkIdx_38` (`server_id`)," +
                            "CONSTRAINT `FK_37` FOREIGN KEY `fkIdx_38` (`server_id`) REFERENCES `plotsystem_servers` (`id`)" +
                            ");",

                    // City Projects
                    "CREATE TABLE IF NOT EXISTS `plotsystem_city_projects`" +
                            "(" +
                            " `id`          int NOT NULL AUTO_INCREMENT ," +
                            " `country_id`  int NOT NULL ," +
                            " `name`        varchar(45) NOT NULL ," +
                            " `description` varchar(255) NOT NULL ," +
                            " `visible`     tinyint DEFAULT 0 ," +
                            "PRIMARY KEY (`id`)," +
                            "KEY `fkIdx_44` (`country_id`)," +
                            "CONSTRAINT `FK_43` FOREIGN KEY `fkIdx_44` (`country_id`) REFERENCES `plotsystem_countries` (`id`)" +
                            ");",

                    // Builders
                    "CREATE TABLE IF NOT EXISTS `plotsystem_builders`" +
                            "(" +
                            " `uuid`            varchar(36) NOT NULL ," +
                            " `name`            varchar(16) NOT NULL ," +
                            " `score`           int DEFAULt 0 ," +
                            " `completed_plots` int DEFAULT 0 ," +
                            " `first_slot`      int NULL ," +
                            " `second_slot`     int NULL ," +
                            " `third_slot`      int NULL ," +
                            "PRIMARY KEY (`uuid`)" +
                            ");",

                    // Reviews
                    "CREATE TABLE IF NOT EXISTS `plotsystem_reviews`" +
                            "(" +
                            " `id`            int NOT NULL AUTO_INCREMENT ," +
                            " `reviewer_uuid` varchar(36) NOT NULL ," +
                            " `rating`        varchar(45) NOT NULL ," +
                            " `feedback`      varchar(420) NOT NULL ," +
                            " `review_date`   datetime NOT NULL ," +
                            " `sent`          tinyint DEFAULT 0 ," +
                            "PRIMARY KEY (`id`)," +
                            "KEY `fkIdx_73` (`reviewer_uuid`)," +
                            "CONSTRAINT `FK_72` FOREIGN KEY `fkIdx_73` (`reviewer_uuid`) REFERENCES `plotsystem_builders` (`uuid`)" +
                            ");",

                    // Difficulties
                    "CREATE TABLE IF NOT EXISTS `plotsystem_difficulties`" +
                            "(" +
                            " `id`               int NOT NULL AUTO_INCREMENT ," +
                            " `name`             varchar(45) NOT NULL ," +
                            " `multiplier`       double DEFAULT 1 ," +
                            " `score_requirment` int DEFAULT 0 ," +
                            "PRIMARY KEY (`id`)" +
                            ");",

                    // Plots
                    "CREATE TABLE IF NOT EXISTS `plotsystem_plots`" +
                            "(" +
                            " `id`              int NOT NULL AUTO_INCREMENT ," +
                            " `city_project_id` int NOT NULL ," +
                            " `difficulty_id`   int NOT NULL ," +
                            " `review_id`       int NULL ," +
                            " `owner_uuid`     varchar(36) NULL ," +
                            " `member_uuids`    varchar(110) NULL ," +
                            " `status`          enum ('unclaimed', 'unfinished', 'unreviewed', 'completed') NOT NULL DEFAULT 'unclaimed' ," +
                            " `mc_coordinates`  varchar(255) NOT NULL ," +
                            " `score`           int NULL ," +
                            " `last_activity`   datetime NULL ," +
                            " `create_date`     datetime NOT NULL ," +
                            " `create_player`   varchar(36) NOT NULL ," +
                            " `pasted`          tinyint DEFAULT 0 ," +
                            "PRIMARY KEY (`id`)," +
                            "KEY `fkIdx_57` (`city_project_id`)," +
                            "CONSTRAINT `FK_56` FOREIGN KEY `fkIdx_57` (`city_project_id`) REFERENCES `plotsystem_city_projects` (`id`)," +
                            "KEY `fkIdx_60` (`owner_uuid`)," +
                            "CONSTRAINT `FK_59` FOREIGN KEY `fkIdx_60` (`owner_uuid`) REFERENCES `plotsystem_builders` (`uuid`)," +
                            "KEY `fkIdx_70` (`review_id`)," +
                            "CONSTRAINT `FK_69` FOREIGN KEY `fkIdx_70` (`review_id`) REFERENCES `plotsystem_reviews` (`id`)," +
                            "KEY `fkIdx_82` (`difficulty_id`)," +
                            "CONSTRAINT `FK_81` FOREIGN KEY `fkIdx_82` (`difficulty_id`) REFERENCES `plotsystem_difficulties` (`id`)" +
                            ");"
            );
        }
    }
}
