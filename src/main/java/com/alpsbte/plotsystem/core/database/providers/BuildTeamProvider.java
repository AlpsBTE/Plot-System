package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Country;
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

    public BuildTeamProvider(BuilderProvider builderProvider, CountryProvider countryProvider) {
        // cache all build teams
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT build_team_id, name FROM build_team").executeQuery()) {
            while (rs.next()) {
                int buildTeamId = rs.getInt(1);

                List<Country> countries = countryProvider.getCountriesByBuildTeam(buildTeamId);
                List<Builder> reviewers = builderProvider.getReviewersByBuildTeam(buildTeamId);
                BUILD_TEAMS.add(new BuildTeam(buildTeamId, rs.getString(2), countries, reviewers));
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

    public boolean addCountry(int id, String countryCode) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("INSERT INTO build_team_has_country (build_team_id, country_code) " +
                        "VALUES (?, ?);")) {
            stmt.setInt(1, id);
            stmt.setString(2, countryCode);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public boolean removeCountry(int id, String countryCode) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM build_team_has_country " +
                        "WHERE build_team_id = ? AND country_code = ?;")) {
            stmt.setInt(1, id);
            stmt.setString(2, countryCode);
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

    public List<Country> getReviewerCountries(Builder builder) {
        // TODO: implement
        return List.of();
    }
}
