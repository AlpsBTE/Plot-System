package com.alpsbte.plotsystem.core.data;

import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Country;

import java.util.List;
import java.util.UUID;

public interface BuildTeamProvider {
    String getName(int id) throws DataException;
    List<Country> getCountries(int id) throws DataException;
    List<Builder> getReviewers(int id) throws DataException;
    List<BuildTeam> getBuildTeamsByReviewer(UUID reviewerUUID) throws DataException;
    List<BuildTeam> getBuildTeams();
    void addBuildTeam(String name) throws DataException;
    void removeBuildTeam(int serverId) throws DataException;
    void setBuildTeamName(int id, String newName) throws DataException;
    void addCountry(int id, int countryId) throws DataException;
    void removeCountry(int id, int countryId) throws DataException;
    void addReviewer(int id, String reviewerUUID) throws DataException;
    void removeReviewer(int id, String reviewerUUID) throws DataException;
}
