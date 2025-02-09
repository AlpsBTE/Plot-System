/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.utils.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CityProjectProvider {
    public static final List<CityProject> cityProjects = new ArrayList<>();

    public CityProject getById(String id) {
        return cityProjects.stream().filter(c -> c.getID().equals(id)).findFirst()
                .orElseGet(() -> {
                    try (PreparedStatement stmt = DatabaseConnection.getConnection()
                            .prepareStatement("SELECT country_code, server_name, is_visible FROM city_project " +
                                    "WHERE city_project_id = ?;")) {
                        stmt.setString(1, id);

                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                return new CityProject(id, rs.getString(1), rs.getString(2),
                                        rs.getBoolean(3));
                            }
                        }
                    } catch (SQLException ex) { Utils.logSqlException(ex); }
                    return null;
                });
    }

    public List<CityProject> getByCountryCode(String countryCode, boolean onlyVisible) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("SELECT city_project_id FROM city_project WHERE country_code = ? " +
                        "AND is_visible = ?;")) {
            stmt.setString(1, countryCode);
            stmt.setBoolean(2, onlyVisible);

            try (ResultSet rs = stmt.executeQuery()) {
                List<CityProject> cityProjects = new ArrayList<>();
                while (rs.next()) cityProjects.add(getById(rs.getString(1)));
                return cityProjects;
            }
        } catch (SQLException ex) { Utils.logSqlException(ex); }
        return null;
    }

    public List<CityProject> get(boolean onlyVisible) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("SELECT city_project_id FROM city_project WHERE is_visible = ?;")) {
            stmt.setBoolean(1, onlyVisible);

            try (ResultSet rs = stmt.executeQuery()) {
                List<CityProject> cityProjects = new ArrayList<>();
                while (rs.next()) cityProjects.add(getById(rs.getString(1)));
                return cityProjects;
            }
        } catch (SQLException ex) { Utils.logSqlException(ex); }
        return null;
    }

    public boolean add(String id, String countryCode, String serverName) {
        if (getById(id) != null) return true;
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("INSERT INTO city_project (city_project_id, country_code, server_name) " +
                        "VALUES (?, ?, ?);")) {
            stmt.setString(1, id);
            stmt.setString(2, countryCode);
            stmt.setString(3, serverName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) { Utils.logSqlException(ex); }
        return false;
    }

    public boolean remove(String id) {
        CityProject cityProject = getById(id);
        if (cityProject == null) return false;

        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM city_project WHERE city_project_id = ?;")) {
            stmt.setString(1, id);
            boolean result = stmt.executeUpdate() > 0;
            if (result) cityProjects.remove(cityProject);
            return result;
        } catch (SQLException ex) { Utils.logSqlException(ex); }
        return false;
    }

    public boolean setVisibility(String id, boolean isVisible) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("UPDATE city_project SET is_visible = ? WHERE city_project_id = ?;")) {
            stmt.setBoolean(1, isVisible);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) { Utils.logSqlException(ex); }
        return false;
    }

    public boolean setServer(String id, String serverName) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("UPDATE city_project SET server_name = ? WHERE city_project_id = ?;")) {
            stmt.setString(1, serverName);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) { Utils.logSqlException(ex); }
        return false;
    }
}
