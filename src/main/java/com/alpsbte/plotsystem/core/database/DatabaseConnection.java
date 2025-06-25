/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
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
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

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

        FileConfiguration configFile = PlotSystem.getPlugin().getConfig();
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

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static StatementBuilder createStatement(String sql) {
        return new StatementBuilder(sql);
    }

    public static void closeResultSet(ResultSet resultSet) throws SQLException {
        if (resultSet.isClosed()
                && resultSet.getStatement().isClosed()
                && resultSet.getStatement().getConnection().isClosed())
            return;

        resultSet.close();
        resultSet.getStatement().close();
        resultSet.getStatement().getConnection().close();

        connectionClosed++;

        if (connectionOpened > connectionClosed + 5) {
            PlotSystem.getPlugin().getComponentLogger().error(text("There are multiple database connections opened. Please report this issue."));
            PlotSystem.getPlugin().getComponentLogger().error(text("Connections Open: " + (connectionOpened - connectionClosed)));
        }
    }

    private static void createDatabase() throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, username, password)) {
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + name + "`");
            }
        }
    }

    private static void createTables() {
        try (Connection con = dataSource.getConnection()) {
            for (String table : Tables.getTables()) {
                Objects.requireNonNull(con).prepareStatement(table).executeUpdate();
            }

            try (ResultSet rs = con.prepareStatement("SELECT COUNT(difficulty_id) FROM plot_difficulty").executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt(1) == 0) {
                        con.prepareStatement("INSERT INTO plot_difficulty (difficulty_id, multiplier)" +
                                "VALUES ('EASY', 1.0)," +
                                "       ('MEDIUM', 1.5)," +
                                "       ('HARD', 2);").executeUpdate();
                    }
                }
            }


        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while creating á database table"), ex);
        }
    }

    /**
     * Returns a missing auto increment id
     *
     * @param table in the database
     * @return smallest missing auto increment id in the table
     */
    public static int getTableID(String table) {
        try {
            String query = "SELECT id + 1 available_id FROM $table t WHERE NOT EXISTS (SELECT * FROM $table WHERE $table.id = t.id + 1) ORDER BY id LIMIT 1"
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
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
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
                    // System Info
                    "CREATE TABLE IF NOT EXISTS system_info" +
                            "(" +
                            "    system_id            INT    NOT NULL AUTO_INCREMENT," +
                            "    db_version           DOUBLE NOT NULL," +
                            "    current_plot_version DOUBLE NOT NULL," +
                            "    last_update          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                            "    description          TEXT   NULL," +
                            "    PRIMARY KEY (system_id)" +
                            ");",

                    // Build Team
                    "CREATE TABLE IF NOT EXISTS build_team" +
                            "(" +
                            "    build_team_id   INT          NOT NULL AUTO_INCREMENT," +
                            "    name            VARCHAR(255) NOT NULL," +
                            "    api_key         VARCHAR(255) NULL UNIQUE," +
                            "    api_create_date DATETIME     NULL," +
                            "    PRIMARY KEY (build_team_id)" +
                            ");",

                    // Server
                    "CREATE TABLE IF NOT EXISTS server" +
                            "(" +
                            "    build_team_id INT          NOT NULL," +
                            "    server_name   VARCHAR(255) NOT NULL UNIQUE," +
                            "    PRIMARY KEY (build_team_id, server_name)," +
                            "    FOREIGN KEY (build_team_id) REFERENCES build_team (build_team_id)" +
                            "        ON DELETE RESTRICT ON UPDATE CASCADE" +
                            ");",

                    // Countries
                    "CREATE TABLE IF NOT EXISTS country" +
                            "(" +
                            "    country_code      VARCHAR(2)                           NOT NULL," +
                            "    continent         ENUM ('EU','AS','AF','OC','SA','NA') NOT NULL," +
                            "    material          VARCHAR(255)                         NOT NULL," +
                            "    custom_model_data VARCHAR(255)                         NULL," +
                            "    PRIMARY KEY (country_code)" +
                            ");",

                    // City Projects
                    "CREATE TABLE IF NOT EXISTS city_project" +
                            "(" +
                            "    city_project_id VARCHAR(255) NOT NULL," +
                            "    build_team_id   INT          NOT NULL," +
                            "    country_code    VARCHAR(2)   NOT NULL," +
                            "    server_name     VARCHAR(255) NOT NULL," +
                            "    is_visible      BOOLEAN      NOT NULL DEFAULT 1," +
                            "    PRIMARY KEY (city_project_id)," +
                            "    FOREIGN KEY (build_team_id) REFERENCES build_team (build_team_id)" +
                            "        ON DELETE RESTRICT ON UPDATE CASCADE," +
                            "    FOREIGN KEY (country_code) REFERENCES country (country_code)" +
                            "        ON DELETE RESTRICT ON UPDATE CASCADE," +
                            "    FOREIGN KEY (server_name) REFERENCES server (server_name)" +
                            "        ON DELETE RESTRICT ON UPDATE CASCADE" +
                            ");",

                    // Builders
                    "CREATE TABLE IF NOT EXISTS builder" +
                            "(" +
                            "    uuid        VARCHAR(36)  NOT NULL," +
                            "    name        VARCHAR(255) NOT NULL UNIQUE," +
                            "    score       INT          NOT NULL DEFAULT 0," +
                            "    first_slot  INT          NULL," +
                            "    second_slot INT          NULL," +
                            "    third_slot  INT          NULL," +
                            "    plot_type   INT          NOT NULL," +
                            "    PRIMARY KEY (uuid)" +
                            ");",

                    // Difficulty
                    "CREATE TABLE IF NOT EXISTS plot_difficulty" +
                            "(" +
                            "    difficulty_id     VARCHAR(255) NOT NULL," +
                            "    multiplier        DECIMAL(4, 2)         DEFAULT 1.00," +
                            "    score_requirement INT          NOT NULL DEFAULT 0," +
                            "    PRIMARY KEY (difficulty_id)," +
                            "    CHECK ( multiplier > 0 )," +
                            "    CHECK ( score_requirement >= 0 )" +
                            ");",

                    // Plot
                    "CREATE TABLE IF NOT EXISTS plot" +
                            "(" +
                            "    plot_id            INT                                                      NOT NULL AUTO_INCREMENT," +
                            "    city_project_id    VARCHAR(255)                                             NOT NULL," +
                            "    difficulty_id      VARCHAR(255)                                             NOT NULL," +
                            "    owner_uuid         VARCHAR(36)                                              NULL," +
                            "    status             ENUM ('unclaimed','unfinished','unreviewed','completed') NOT NULL DEFAULT 'unclaimed'," +
                            "    outline_bounds     TEXT                                                     NOT NULL," +
                            "    initial_schematic  MEDIUMBLOB                                               NOT NULL," +
                            "    complete_schematic MEDIUMBLOB                                               NULL," +
                            "    last_activity_date DATETIME                                                 NULL," +
                            "    is_pasted          BOOLEAN                                                  NOT NULL DEFAULT 0," +
                            "    mc_version         VARCHAR(8)                                               NULL," +
                            "    plot_version       DOUBLE                                                   NOT NULL," +
                            "    plot_type          INT                                                      NULL," +
                            "    created_by         VARCHAR(36)                                              NOT NULL," +
                            "    create_date        DATETIME                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "    PRIMARY KEY (plot_id)," +
                            "    FOREIGN KEY (city_project_id) REFERENCES city_project (city_project_id)" +
                            "        ON DELETE RESTRICT ON UPDATE CASCADE," +
                            "    FOREIGN KEY (difficulty_id) REFERENCES plot_difficulty (difficulty_id)" +
                            "        ON DELETE RESTRICT ON UPDATE CASCADE," +
                            "    FOREIGN KEY (owner_uuid) REFERENCES builder (uuid)" +
                            "        ON DELETE RESTRICT ON UPDATE CASCADE" +
                            ");",

                    // Tutorial
                    "CREATE TABLE IF NOT EXISTS tutorial" +
                            "(" +
                            "    tutorial_id              INT         NOT NULL," +
                            "    uuid                     VARCHAR(36) NOT NULL," +
                            "    stage_id                 INT         NOT NULL DEFAULT 0," +
                            "    is_complete              BOOLEAN     NOT NULL DEFAULT 0," +
                            "    first_stage_start_date   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "    last_stage_complete_date DATETIME    NULL," +
                            "    PRIMARY KEY (tutorial_id, uuid)" +
                            ");",

                    // Review
                    "CREATE TABLE IF NOT EXISTS plot_review" +
                            "(" +
                            "    review_id   INT          NOT NULL AUTO_INCREMENT," +
                            "    plot_id     INT          NOT NULL," +
                            "    rating      VARCHAR(7)   NOT NULL," +
                            "    score       INT          NOT NULL DEFAULT 0," +
                            "    feedback    VARCHAR(256) NULL," +
                            "    reviewed_by VARCHAR(36)  NOT NULL," +
                            "    review_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "    PRIMARY KEY (review_id)," +
                            "    FOREIGN KEY (plot_id) REFERENCES plot (plot_id)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE" +
                            ");",

                    // Toggle Criteria
                    "CREATE TABLE IF NOT EXISTS review_toggle_criteria" +
                            "(" +
                            "    criteria_name VARCHAR(255) NOT NULL," +
                            "    is_optional   BOOLEAN      NOT NULL," +
                            "    PRIMARY KEY (criteria_name)" +
                            ");",

                    // Build team uses toggle criteria
                    "CREATE TABLE IF NOT EXISTS build_team_uses_toggle_criteria" +
                            "(" +
                            "    build_team_id INT          NOT NULL," +
                            "    criteria_name VARCHAR(255) NOT NULL," +
                            "    PRIMARY KEY (build_team_id, criteria_name)," +
                            "    FOREIGN KEY (build_team_id) REFERENCES build_team (build_team_id)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE," +
                            "    FOREIGN KEY (criteria_name) REFERENCES review_toggle_criteria (criteria_name)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE" +
                            ");",

                    // Review contains toggle criteria
                    "CREATE TABLE IF NOT EXISTS review_contains_toggle_criteria" +
                            "(" +
                            "    review_id     INT          NOT NULL," +
                            "    criteria_name VARCHAR(255) NOT NULL," +
                            "    is_checked    BOOLEAN      NOT NULL," +
                            "    PRIMARY KEY (review_id, criteria_name)," +
                            "    FOREIGN KEY (review_id) REFERENCES plot_review (review_id)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE," +
                            "    FOREIGN KEY (criteria_name) REFERENCES review_toggle_criteria (criteria_name)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE" +
                            ");",

                    // Build team has reviewer
                    "CREATE TABLE IF NOT EXISTS build_team_has_reviewer" +
                            "(" +
                            "    build_team_id INT         NOT NULL," +
                            "    uuid          VARCHAR(36) NOT NULL," +
                            "    PRIMARY KEY (build_team_id, uuid)," +
                            "    FOREIGN KEY (build_team_id) REFERENCES build_team (build_team_id)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE," +
                            "    FOREIGN KEY (uuid) REFERENCES builder (uuid)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE" +
                            ");",

                    // Builder is plot member
                    "CREATE TABLE IF NOT EXISTS builder_is_plot_member" +
                            "(" +
                            "    plot_id INT         NOT NULL," +
                            "    uuid    VARCHAR(36) NOT NULL," +
                            "    PRIMARY KEY (plot_id, uuid)," +
                            "    FOREIGN KEY (plot_id) REFERENCES plot (plot_id)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE," +
                            "    FOREIGN KEY (uuid) REFERENCES builder (uuid)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE" +
                            ");",

                    // Builder has review notification
                    "CREATE TABLE IF NOT EXISTS builder_has_review_notification" +
                            "(" +
                            "    review_id INT         NOT NULL," +
                            "    uuid      VARCHAR(36) NOT NULL," +
                            "    PRIMARY KEY (review_id, uuid)," +
                            "    FOREIGN KEY (review_id) REFERENCES plot_review (review_id)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE," +
                            "    FOREIGN KEY (uuid) REFERENCES builder (uuid)" +
                            "        ON DELETE CASCADE ON UPDATE CASCADE" +
                            ");"
            );
        }
    }
}
