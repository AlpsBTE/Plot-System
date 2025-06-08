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
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BuildTeamProvider {
    private static final List<BuildTeam> BUILD_TEAMS = new ArrayList<>();
    private final CityProjectProvider cityProjectProvider;

    public BuildTeamProvider(BuilderProvider builderProvider, CityProjectProvider cityProjectProvider) {
        this.cityProjectProvider = cityProjectProvider;

        // cache all build teams
        String query = "SELECT build_team_id, name FROM build_team;";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int buildTeamId = rs.getInt(1);

                    List<CityProject> cityProjects = cityProjectProvider.getCityProjectsByBuildTeam(buildTeamId);
                    List<Builder> reviewers = builderProvider.getReviewersByBuildTeam(buildTeamId);
                    BUILD_TEAMS.add(new BuildTeam(buildTeamId, rs.getString(2), cityProjects, reviewers));
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
    }

    public Optional<BuildTeam> getBuildTeam(int id) {
        return BUILD_TEAMS.stream().filter(b -> b.getID() == id).findFirst();
    }

    public List<BuildTeam> getBuildTeamsByReviewer(UUID reviewerUUID) {
        List<BuildTeam> buildTeams = new ArrayList<>();

        String query = "SELECT build_team_id FROM build_team_has_reviewer WHERE uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, reviewerUUID.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<BuildTeam> buildTeam = getBuildTeam(rs.getInt(1));
                    if (buildTeam.isEmpty()) continue;
                    buildTeams.add(buildTeam.get());
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return buildTeams;
    }

    public List<BuildTeam> getBuildTeams() {
        return BUILD_TEAMS;
    }

    public boolean addBuildTeam(String name) {
        boolean result = false;
        if (BUILD_TEAMS.stream().anyMatch(b -> b.getName().equals(name))) return false;

        String query = "INSERT INTO build_team (name) VALUES (?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            result = stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }

        if (result) {
            String resultQuery = "SELECT build_team_id FROM build_team WHERE name = ?;";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(resultQuery)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next(); // get the last inserted build team id
                    BUILD_TEAMS.add(new BuildTeam(rs.getInt(1), name, List.of(), List.of()));
                }
            } catch (SQLException ex) {
                Utils.logSqlException(ex);
            }
        }
        return result;
    }

    public boolean removeBuildTeam(int id) {
        Optional<BuildTeam> cachedBuildTeam = getBuildTeam(id);
        if (cachedBuildTeam.isEmpty()) return false;

        String query = "DELETE FROM build_team WHERE build_team_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            if (result) BUILD_TEAMS.remove(cachedBuildTeam.get());
            return result;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setBuildTeamName(int id, String newName) {
        String query = "UPDATE build_team SET name = ? WHERE build_team_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newName);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean addReviewer(int id, String reviewerUUID) {
        String query = "INSERT INTO build_team_has_reviewer (build_team_id, uuid) VALUES (?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setString(2, reviewerUUID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean removeReviewer(int id, String reviewerUUID) {
        String query = "DELETE FROM build_team_has_reviewer WHERE build_team_id = ? AND uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.setString(2, reviewerUUID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public List<CityProject> getReviewerCities(Builder builder) {
        List<BuildTeam> buildTeams = BUILD_TEAMS.stream().filter(b
                -> b.getReviewers().stream().anyMatch(r -> r.getUUID().equals(builder.getUUID()))).toList();
        List<CityProject> cities = new ArrayList<>();

        for (BuildTeam buildTeam : buildTeams) {
            cities.addAll(cityProjectProvider.getCityProjectsByBuildTeam(buildTeam.getID()));
        }
        return cities;
    }
}
