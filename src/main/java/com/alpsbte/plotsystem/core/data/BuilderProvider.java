package com.alpsbte.plotsystem.core.data;

import com.alpsbte.plotsystem.core.holograms.ScoreLeaderboard;
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.enums.Slot;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public interface BuilderProvider {
    String getName(UUID uuid) throws DataException;
    int getScore(UUID uuid) throws DataException;
    int getCompletedBuildsCount(UUID uuid) throws DataException;
    Slot getFreeSlot(UUID uuid) throws DataException;
    Plot getPlot(UUID uuid, Slot slot) throws DataException;
    void addScore(UUID uuid, int score) throws DataException;
    void addCompletedBuild(UUID uuid, int amount) throws DataException;
    void setPlot(UUID uuid, int plotID, Slot slot) throws DataException;
    void removePlot(UUID uuid, Slot slot) throws DataException;
    Builder getBuilderByName(String name) throws DataException;
    PlotType getPlotTypeSetting(UUID uuid);
    void setPlotTypeSetting(UUID uuid, PlotType plotType);
    boolean isReviewer(UUID uuid) throws DataException;
    List<Country> getReviewerCountries(List<BuildTeam> buildTeams);
    int getLeaderboardScore(UUID uuid, ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException;
    int getLeaderboardPosition(UUID uuid, ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException;
    int getLeaderboardEntryCount(ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException;
    HashMap<String, Integer> getLeaderboardEntriesByScore(ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException;
    HashMap<String, Integer> getLeaderboardEntriesByCompletedBuilds(int limit) throws DataException;
}
