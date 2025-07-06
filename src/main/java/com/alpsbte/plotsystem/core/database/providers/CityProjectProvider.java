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

package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.alpslib.io.database.SqlHelper;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.utils.Utils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CityProjectProvider {
    protected static final List<CityProject> CITY_PROJECTS = new ArrayList<>();

    public CityProjectProvider() {
        String qCityProjects = "SELECT city_project_id, country_code, server_name, is_visible, build_team_id FROM city_project;";
        Utils.handleSqlException(() -> SqlHelper.runQuery(qCityProjects, ps -> {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CITY_PROJECTS.add(new CityProject(rs.getString(1), // cache all city projects
                        rs.getString(2), rs.getString(3), rs.getBoolean(4), rs.getInt(5)));
            }
        }));
    }

    public Optional<CityProject> getById(String id) {
        return CITY_PROJECTS.stream().filter(c -> c.getID().equals(id)).findFirst();
    }

    public List<CityProject> getByCountryCode(String countryCode, boolean onlyVisible) {
        return CITY_PROJECTS.stream().filter(c -> (!onlyVisible || c.isVisible()) &&
                c.getCountry().getCode().equals(countryCode)).toList();
    }

    public List<CityProject> get(boolean onlyVisible) {
        return CITY_PROJECTS.stream().filter(c -> !onlyVisible || c.isVisible()).toList();
    }

    public List<CityProject> getCityProjectsByBuildTeam(int buildTeamId) {
        String qIdByBtId = "SELECT city_project_id FROM city_project WHERE build_team_id = ?;";
        return Utils.handleSqlException(new ArrayList<>(), () -> SqlHelper.runQuery(qIdByBtId, ps -> {
            ps.setInt(1, buildTeamId);
            ResultSet rs = ps.executeQuery();
            List<CityProject> cityProjects = new ArrayList<>();
            while (rs.next()) {
                Optional<CityProject> city = getById(rs.getString(1));
                city.ifPresent(cityProjects::add);
            }
            return cityProjects;
        }));
    }

    public boolean add(String id, int buildTeamId, String countryCode, String serverName) {
        if (getById(id).isPresent()) return true;

        String qInsert = "INSERT INTO city_project (city_project_id, build_team_id, country_code, server_name) VALUES (?, ?, ?, ?);";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qInsert, ps -> {
            ps.setString(1, id);
            ps.setInt(2, buildTeamId);
            ps.setString(3, countryCode);
            ps.setString(4, serverName);
            boolean result = ps.executeUpdate() > 0;
            if (result) CITY_PROJECTS.add(new CityProject(id, countryCode, serverName, true, buildTeamId));
            return result;
        })));
    }

    public boolean remove(String id) {
        Optional<CityProject> cityProject = getById(id);
        String qDelete = "DELETE FROM city_project WHERE city_project_id = ?;";
        return cityProject.filter(project -> Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qDelete, ps -> {
            ps.setString(1, id);
            boolean result = ps.executeUpdate() > 0;
            if (result) CITY_PROJECTS.remove(project);
            return result;
        })))).isPresent();
    }

    public boolean setVisibility(String id, boolean isVisible) {
        String qUpdateVisible = "UPDATE city_project SET is_visible = ? WHERE city_project_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qUpdateVisible, ps -> {
            ps.setBoolean(1, isVisible);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setServer(String id, String serverName) {
        String qUpdateServerName = "UPDATE city_project SET server_name = ? WHERE city_project_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qUpdateServerName, ps -> {
            ps.setString(1, serverName);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setBuildTeam(String id, int buildTeamId) {
        String qUpdateBuildTeamId = "UPDATE city_project SET build_team_id = ? WHERE city_project_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qUpdateBuildTeamId, ps -> {
            ps.setInt(1, buildTeamId);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        })));
    }
}
