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

    public List<BuildTeam> getBuildTeamsByReviewer(UUID reviewerUUID) {
        // TODO: implement
        return List.of();
    }

    public List<BuildTeam> getBuildTeams() {
        List<BuildTeam> buildTeams = new ArrayList<>();
        List<Country> countries = new ArrayList<>();
        List<Builder> reviewers = new ArrayList<>();
        // TODO: also get countries and reviewers (currently empty lists)
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT build_team_id, name FROM build_team").executeQuery()) {
            while (rs.next()) buildTeams.add(new BuildTeam(rs.getInt(1), rs.getString(2), countries, reviewers));
            DatabaseConnection.closeResultSet(rs);
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
        return buildTeams;
    }

    public boolean addBuildTeam(String name) {
        // TODO: implement
        return false;
    }

    public boolean removeBuildTeam(int id) {
        // TODO: implement
        return false;
    }

    public boolean setBuildTeamName(int id, String newName) {
        // TODO: implement
        return false;
    }

    public boolean addCountry(int id, String countryCode) {
        // TODO: implement
        return false;
    }

    public boolean removeCountry(int id, String countryCode) {
        // TODO: implement
        return false;
    }

    public boolean addReviewer(int id, String reviewerUUID) {
        // TODO: implement
        return false;
    }

    public boolean removeReviewer(int id, String reviewerUUID) {
        // TODO: implement
        return false;
    }

    public List<Country> getReviewerCountries(Builder builder) {
        // TODO: implement
        return List.of();
    }
}
