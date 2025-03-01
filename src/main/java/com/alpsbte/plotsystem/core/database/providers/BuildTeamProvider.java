package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.utils.Utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class BuildTeamProvider {
    public static final List<BuildTeam> BUILD_TEAMS = new ArrayList<>();
    private final CityProjectProvider cityProjectProvider;

    public BuildTeamProvider(BuilderProvider builderProvider, CityProjectProvider cityProjectProvider) {
        this.cityProjectProvider = cityProjectProvider;

        // cache all build teams
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT build_team_id, name FROM build_team").executeQuery()) {
            while (rs.next()) {
                int buildTeamId = rs.getInt(1);

                List<CityProject> cityProjects = cityProjectProvider.getCityProjectsByBuildTeam(buildTeamId);
                List<Builder> reviewers = builderProvider.getReviewersByBuildTeam(buildTeamId);
                BUILD_TEAMS.add(new BuildTeam(buildTeamId, rs.getString(2), cityProjects, reviewers));
            }
            DatabaseConnection.closeResultSet(rs);
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
    }

    public Optional<BuildTeam> getBuildTeam(int id) {
        return BUILD_TEAMS.stream().filter(b -> b.getID() == id).findFirst();
    }

    public List<BuildTeam> getBuildTeamsByReviewer(UUID reviewerUUID) {
        List<BuildTeam> buildTeams = new ArrayList<>();
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("SELECT build_team_id FROM build_team_has_reviewer WHERE uuid = ?")) {
            stmt.setString(1, reviewerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<BuildTeam> buildTeam = getBuildTeam(rs.getInt(1));
                    if (buildTeam.isEmpty()) continue;
                    buildTeams.add(buildTeam.get());
                }
                DatabaseConnection.closeResultSet(rs);
            }
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
        return buildTeams;
    }

    public List<BuildTeam> getBuildTeams() {
        return BUILD_TEAMS;
    }

    public boolean addBuildTeam(String name) {
        boolean result = false;
        if (BUILD_TEAMS.stream().anyMatch(b -> b.getName().equals(name))) return false;
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("INSERT INTO build_team (name) VALUES (?);")) {
            stmt.setString(1, name);
            result = stmt.executeUpdate() > 0;
        } catch (SQLException ex) {Utils.logSqlException(ex);}

        if (result) {
            try (PreparedStatement stmt = DatabaseConnection.getConnection()
                    .prepareStatement("SELECT build_team_id FROM build_team WHERE name = ?;")) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    BUILD_TEAMS.add(new BuildTeam(rs.getInt(1), name, List.of(), List.of()));
                }
            } catch (SQLException ex) {Utils.logSqlException(ex);}
        }
        return result;
    }

    public boolean removeBuildTeam(int id) {
        Optional<BuildTeam> cachedBuildTeam = getBuildTeam(id);
        if (cachedBuildTeam.isEmpty()) return false;

        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM build_team WHERE build_team_id = ?;")) {
            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            if (result) BUILD_TEAMS.remove(cachedBuildTeam.get());
            return result;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public boolean setBuildTeamName(int id, String newName) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("UPDATE build_team SET name = ? WHERE build_team_id = ?;")) {
            stmt.setString(1, newName);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) { Utils.logSqlException(ex); }
        return false;
    }

    public boolean addCityProject(int id, String cityId) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("INSERT INTO build_team_has_city_project (build_team_id, city_project_id) " +
                        "VALUES (?, ?);")) {
            stmt.setInt(1, id);
            stmt.setString(2, cityId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public boolean removeCityProject(int id, String cityId) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM build_team_has_city_project " +
                        "WHERE build_team_id = ? AND city_project_id = ?;")) {
            stmt.setInt(1, id);
            stmt.setString(2, cityId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public boolean addReviewer(int id, String reviewerUUID) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("INSERT INTO build_team_has_reviewer (build_team_id, uuid) " +
                        "VALUES (?, ?);")) {
            stmt.setInt(1, id);
            stmt.setString(2, reviewerUUID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public boolean removeReviewer(int id, String reviewerUUID) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM build_team_has_reviewer " +
                        "WHERE build_team_id = ? AND uuid = ?;")) {
            stmt.setInt(1, id);
            stmt.setString(2, reviewerUUID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public List<CityProject> getReviewerCities(Builder builder) {
        List<BuildTeam> buildTeams = BUILD_TEAMS.stream().filter(b
                -> b.getReviewers().stream().anyMatch(r -> r.getUUID() == builder.getUUID())).toList();
        List<CityProject> cities = new ArrayList<>();

        for (BuildTeam buildTeam : buildTeams) {
            cities.addAll(cityProjectProvider.getCityProjectsByBuildTeam(buildTeam.getID()));
        }

        return cities;
    }
}
