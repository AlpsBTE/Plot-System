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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CityProjectProvider {
    public static final List<CityProject> cachedCityProjects = new ArrayList<>();

    public CityProjectProvider() {
        String query = "SELECT city_project_id, country_code, server_name, is_visible FROM city_project;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cachedCityProjects.add(new CityProject(rs.getString(1), // cache all city projects
                            rs.getString(2), rs.getString(3), rs.getBoolean(4)));
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
    }

    public Optional<CityProject> getById(String id) {
        return cachedCityProjects.stream().filter(c -> c.getID().equals(id)).findFirst();
    }

    public List<CityProject> getByCountryCode(String countryCode, boolean onlyVisible) {
        return cachedCityProjects.stream().filter(c -> (!onlyVisible || c.isVisible()) &&
                c.getCountry().getCode().equals(countryCode)).toList();
    }

    public List<CityProject> get(boolean onlyVisible) {
        return cachedCityProjects.stream().filter(c -> !onlyVisible || c.isVisible()).toList();
    }

    public List<CityProject> getCityProjectsByBuildTeam(int buildTeamId) {
        List<CityProject> cityProjects = new ArrayList<>();

        String query = "SELECT city_project_id FROM city_project WHERE build_team_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, buildTeamId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<CityProject> city = getById(rs.getString(1));
                    city.ifPresent(cityProjects::add);
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return cityProjects;
    }

    public boolean add(String id, int buildTeamId, String countryCode, String serverName) {
        if (getById(id).isPresent()) return true;

        String query = "INSERT INTO city_project (city_project_id, build_team_id, country_code, server_name) " +
                "VALUES (?, ?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            stmt.setInt(2, buildTeamId);
            stmt.setString(3, countryCode);
            stmt.setString(4, serverName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean remove(String id) {
        Optional<CityProject> cityProject = getById(id);
        if (cityProject.isEmpty()) return false;

        String query = "DELETE FROM city_project WHERE city_project_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, id);
            boolean result = stmt.executeUpdate() > 0;
            if (result) cachedCityProjects.remove(cityProject.get());
            return result;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setVisibility(String id, boolean isVisible) {
        String query = "UPDATE city_project SET is_visible = ? WHERE city_project_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, isVisible);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setServer(String id, String serverName) {
        String query = "UPDATE city_project SET server_name = ? WHERE city_project_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, serverName);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }
}
