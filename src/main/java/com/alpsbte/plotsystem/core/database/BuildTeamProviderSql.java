package com.alpsbte.plotsystem.core.database;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Country;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class BuildTeamProviderSql {
    public String getName(int id) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name FROM plotsystem_buildteams WHERE id = ?")
                .setValue(id).executeQuery()) {

            if (rs.next()) return rs.getString(1);
            DatabaseConnection.closeResultSet(rs);
        }
        return null;
    }

    public List<Country> getCountries(int id) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT country_id FROM plotsystem_buildteam_has_countries WHERE buildteam_id = ?")
                .setValue(id).executeQuery()) {

            List<Country> countries = new ArrayList<>();
            while (rs.next()) countries.add(new Country(rs.getInt(1)));
            DatabaseConnection.closeResultSet(rs);
            return countries;
        }
    }

    public List<Builder> getReviewers(int id) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT builder_uuid FROM plotsystem_builder_is_reviewer WHERE buildteam_id = ?")
                .setValue(id).executeQuery()) {

            List<Builder> builders = new ArrayList<>();
            while (rs.next()) builders.add(Builder.byUUID(UUID.fromString(rs.getString(1))));
            DatabaseConnection.closeResultSet(rs);
            return builders;
        }
    }

    public List<BuildTeam> getBuildTeamsByReviewer(UUID reviewerUUID) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT buildteam_id FROM plotsystem_builder_is_reviewer WHERE builder_uuid = ?")
                .setValue(reviewerUUID.toString()).executeQuery()) {

            List<BuildTeam> buildTeams = new ArrayList<>();
            while (rs.next()) buildTeams.add(new BuildTeam(rs.getInt(1)));
            DatabaseConnection.closeResultSet(rs);
            return buildTeams;
        }
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
