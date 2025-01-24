package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.PlotSystem;
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

    public BuildTeam getBuildTeam(int id) {
        Optional<BuildTeam> buildTeam = BUILD_TEAMS.stream().filter(b -> b.getID() == id).findFirst();
        if (buildTeam.isPresent()) return buildTeam.get();
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("SELECT bt.name, bthc.country_code, bthcp.city_project_id, bthr.uuid FROM build_team bt " +
                        "INNER JOIN build_team_has_country bthc ON bthc.build_team_id = bt.build_team_id " +
                        "INNER JOIN build_team_has_city_project bthcp ON bthcp.build_team_id = bt.build_team_id " +
                        "INNER JOIN build_team_has_reviewer bthr ON bthr.build_team_id = bt.build_team_id " +
                        "WHERE bt.build_team_id = ?")) {

        } catch (SQLException ex) { Utils.logSqlException(ex); }
        return null;
    }

    public String getName(int id) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name FROM plotsystem_buildteams WHERE id = ?")
                .setValue(id).executeQuery()) {

            if (rs.next()) return rs.getString(1);
            DatabaseConnection.closeResultSet(rs);
        }
        return null;
    }

    public List<Country> getCountries(int id) {
        // TODO: implement
        return List.of();
    }

    public List<Builder> getReviewers(int id) {
        // TODO: implement
        return List.of();
    }

    public List<BuildTeam> getBuildTeamsByReviewer(UUID reviewerUUID) {
        // TODO: implement
        return List.of();
    }

    public List<BuildTeam> getBuildTeams() {
        List<BuildTeam> buildTeams = new ArrayList<>();
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id, name FROM plotsystem_buildteams").executeQuery()) {
            while (rs.next()) buildTeams.add(new BuildTeam(rs.getInt(1), rs.getString(2)));
            DatabaseConnection.closeResultSet(rs);
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
        return buildTeams;
    }

    public void addBuildTeam(String name) throws SQLException {
        int id = DatabaseConnection.getTableID("plotsystem_buildteams");
        DatabaseConnection.createStatement("INSERT INTO plotsystem_buildteams (id, name) VALUES (?, ?)")
                .setValue(id)
                .setValue(name).executeUpdate();
    }

    public void removeBuildTeam(int serverId) throws SQLException {
        DatabaseConnection.createStatement("DELETE FROM plotsystem_buildteams WHERE id = ?")
                .setValue(serverId).executeUpdate();
    }

    public void setBuildTeamName(int id, String newName) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_buildteams SET name = ? WHERE id = ?")
                .setValue(newName)
                .setValue(id).executeUpdate();
    }

    public void addCountry(int id, int countryId) throws SQLException {
        DatabaseConnection.createStatement("INSERT plotsystem_buildteam_has_countries SET country_id = ?, buildteam_id = ?")
                .setValue(countryId)
                .setValue(id).executeUpdate();
    }

    public void removeCountry(int id, int countryId) throws SQLException {
        DatabaseConnection.createStatement("DELETE FROM plotsystem_buildteam_has_countries WHERE country_id = ? AND buildteam_id = ?")
                .setValue(countryId)
                .setValue(id).executeUpdate();
    }

    public void addReviewer(int id, String reviewerUUID) throws SQLException {
        DatabaseConnection.createStatement("INSERT plotsystem_builder_is_reviewer SET builder_uuid = ?, buildteam_id = ?")
                .setValue(reviewerUUID)
                .setValue(id).executeUpdate();
    }

    public void removeReviewer(int id, String reviewerUUID) throws SQLException {
        DatabaseConnection.createStatement("DELETE FROM plotsystem_builder_is_reviewer WHERE builder_uuid = ? AND buildteam_id = ?")
                .setValue(reviewerUUID)
                .setValue(id).executeUpdate();
    }
}
