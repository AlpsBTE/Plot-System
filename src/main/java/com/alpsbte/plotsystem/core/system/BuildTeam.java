/*
 * The MIT License (MIT)
 *
 *  Copyright © 2022, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.data.DataException;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BuildTeam {
    private final int ID;
    private final String name;

    public BuildTeam(int ID) throws SQLException {
        this.ID = ID;
        this.name = PlotSystem.getDataProvider().getBuildTeamProvider().getName(ID);
    }

    public BuildTeam(int ID, String name) {
        this.ID = ID;
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public List<Country> getCountries() throws DataException {
        return PlotSystem.getDataProvider().getBuildTeamProvider().getCountries(ID);
    }

    public List<Builder> getReviewers() throws DataException {
        return PlotSystem.getDataProvider().getBuildTeamProvider().getReviewers(ID);
    }

    public static List<BuildTeam> getBuildTeamsByReviewer(UUID reviewerUUID) throws DataException {
        return PlotSystem.getDataProvider().getBuildTeamProvider().getBuildTeamsByReviewer(reviewerUUID);
    }

    public static List<BuildTeam> getBuildTeams() {
        return PlotSystem.getDataProvider().getBuildTeamProvider().getBuildTeams();
    }

    public static void addBuildTeam(String name) throws DataException {
        PlotSystem.getDataProvider().getBuildTeamProvider().addBuildTeam(name);
    }

    public static void removeBuildTeam(int serverID) throws DataException {
        PlotSystem.getDataProvider().getBuildTeamProvider().removeBuildTeam(serverID);
    }

    public static void setBuildTeamName(int id, String newName) throws DataException {
        PlotSystem.getDataProvider().getBuildTeamProvider().setBuildTeamName(id, newName);
    }

    public static void addCountry(int id, int countryID) throws DataException {
        PlotSystem.getDataProvider().getBuildTeamProvider().addCountry(id, countryID);
    }

    public static void removeCountry(int id, int countryID) throws DataException {
        PlotSystem.getDataProvider().getBuildTeamProvider().removeCountry(id, countryID);
    }

    public static void removeReviewer(int id, String reviewerUUID) throws SQLException {
        PlotSystem.getDataProvider().getBuildTeamProvider().removeReviewer(id, reviewerUUID);
    }

    public static void addReviewer(int id, String reviewerUUID) throws DataException {
        PlotSystem.getDataProvider().getBuildTeamProvider().addReviewer(id, reviewerUUID);
    }
}
