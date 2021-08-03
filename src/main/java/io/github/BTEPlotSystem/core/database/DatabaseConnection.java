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

package github.BTEPlotSystem.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import github.BTEPlotSystem.core.config.ConfigPaths;
import github.BTEPlotSystem.core.database.builder.PreparedStatementBuilder;
import github.BTEPlotSystem.core.database.builder.TableBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class DatabaseConnection {

    private final static HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;

    public static void InitializeDatabase() {
        try {
            Class.forName("org.mariadb.jdbc.Driver"); // TODO: Add Support MySQL Driver
            Bukkit.getLogger().log(Level.INFO, "Successfully registered MariaDB JDBC Driver!");

            FileConfiguration configFile = github.BTEPlotSystem.BTEPlotSystem.getPlugin().getConfig();

            config.setJdbcUrl(configFile.getString(ConfigPaths.DATABASE_URL) + configFile.getString(ConfigPaths.DATABASE_NAME));
            config.setUsername(configFile.getString(ConfigPaths.DATABASE_USERNAME));
            config.setPassword(configFile.getString(ConfigPaths.DATABASE_PASSWORD));
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            // Create tables and default values if not exists
            Tables.createTables();
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

    public static ResultSet query(PreparedStatementBuilder builder) throws SQLException {
        ResultSet rs = builder.getPreparedStatement().executeQuery();
        rs.next();
        return rs;
    }

    public static void update(PreparedStatementBuilder builder) throws SQLException {
        builder.getPreparedStatement().executeUpdate();
    }

    public static class Tables {

        private final static List<TableBuilder> tables;

        public static void createTables() {
            for (TableBuilder table : tables) {
                try (Connection con = getConnection()) {
                    Objects.requireNonNull(con).prepareStatement(table.toString()).executeUpdate();

                    if (table.getTableName().equals("plotsystem_difficulties")) {
                        ResultSet rs = con.prepareStatement("SELECT COUNT(id) FROM plotsystem_difficulties").executeQuery();
                        rs.next();
                        if (rs.getInt(1) != 3) {
                            con.prepareStatement("INSERT INTO plotsystem_difficulties (id, name) VALUES (1, 'EASY')").executeUpdate();
                            con.prepareStatement("INSERT INTO plotsystem_difficulties (id, name, multiplier) VALUES (2, 'MEDIUM', 1.5)").executeUpdate();
                            con.prepareStatement("INSERT INTO plotsystem_difficulties (id, name, multiplier) VALUES (3, 'HARD', 2)").executeUpdate();
                        }
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "An error occurred while creating database table!");
                }
            }
        }

        static {
            tables = Arrays.asList(
                    // FTP Configurations
                    new TableBuilder("plotsystem_ftp_configurations")
                            .column("id", SQL.INT).primaryKey(true)
                            .column("address", SQL.varchar(255)).notNull()
                            .column("port", SQL.INT).notNull()
                            .column("username", SQL.varchar(255)).notNull()
                            .column("password", SQL.varchar(255)).notNull()
                            .build(),

                    // Servers
                    new TableBuilder("plotsystem_servers")
                            .column("id", SQL.INT).primaryKey(true)
                            .column("ftp_configuration_id", SQL.INT).Null()
                            .column("name", SQL.varchar(45)).notNull()
                            .column("schematic_path", SQL.varchar(255)).Null()
                            .foreignKey("ftp_configuration_id", "plotsystem_ftp_configurations", "id")
                            .build(),

                    // Countries
                    new TableBuilder("plotsystem_countries")
                            .column("id", SQL.INT).primaryKey(true)
                            .column("server_id", SQL.INT).notNull()
                            .column("name", SQL.varchar(45)).notNull()
                            .column("head_id", SQL.varchar(10)).Null()
                            .foreignKey("server_id", "plotsystem_servers", "id")
                            .build(),

                    // City Projects
                    new TableBuilder("plotsystem_city_projects")
                            .column("id", SQL.INT).primaryKey(true)
                            .column("country_id", SQL.INT).notNull()
                            .column("name", SQL.varchar(45)).notNull()
                            .column("description", SQL.varchar(255)).notNull()
                            .column("visible", SQL.BOOL).defaultValue("0")
                            .foreignKey("country_id", "plotsystem_countries", "id")
                            .build(),

                    // Builders
                    new TableBuilder("plotsystem_builders")
                            .column("uuid", SQL.varchar(36)).primaryKey(false)
                            .column("name", SQL.varchar(16)).notNull()
                            .column("score", SQL.INT).defaultValue("0")
                            .column("completed_plots", SQL.INT).defaultValue("0")
                            .column("first_slot", SQL.INT).Null()
                            .column("second_slot", SQL.INT).Null()
                            .column("third_slot", SQL.INT).Null()
                            .build(),

                    // Reviews
                    new TableBuilder("plotsystem_reviews")
                            .column("id", SQL.INT).primaryKey(true)
                            .column("reviewer_uuid", SQL.varchar(36)).notNull()
                            .column("rating", SQL.varchar(45)).notNull()
                            .column("feedback", SQL.varchar(420)).notNull()
                            .column("review_date", SQL.DATETIME).notNull()
                            .column("sent", SQL.BOOL).defaultValue("0")
                            .foreignKey("reviewer_uuid", "plotsystem_builders", "uuid")
                            .build(),

                    // Difficulties
                    new TableBuilder("plotsystem_difficulties")
                            .column("id", SQL.INT).primaryKey(true)
                            .column("name", SQL.varchar(45)).notNull()
                            .column("multiplier", SQL.DOUBLE).defaultValue("1")
                            .column("score_requirement", SQL.INT).defaultValue("0")
                            .build(),

                    // Plots
                    new TableBuilder("plotsystem_plots")
                            .column("id", SQL.INT).primaryKey(true)
                            .column("city_project_id", SQL.INT).notNull()
                            .column("difficulty_id", SQL.INT).notNull()
                            .column("review_id", SQL.INT).Null()
                            .column("player_uuid", SQL.varchar(36)).notNull()
                            .column("member_uuids", SQL.varchar(110)).Null()
                            .column("mc_coordinates", SQL.varchar(255)).notNull()
                            .column("score", SQL.INT).Null()
                            .column("last_activity", SQL.DATETIME).notNull()
                            .column("create_date", SQL.DATETIME).notNull()
                            .column("pasted", SQL.BOOL).defaultValue(false)
                            .foreignKey("city_project_id", "plotsystem_city_projects", "id")
                            .foreignKey("difficulty_id", "plotsystem_difficulties", "id")
                            .foreignKey("review_id", "plotsystem_reviews", "id")
                            .foreignKey("player_uuid", "plotsystem_builders", "uuid")
                            .build()
            );
        }
    }
}
