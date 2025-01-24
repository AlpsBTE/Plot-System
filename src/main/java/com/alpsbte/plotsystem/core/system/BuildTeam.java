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

import com.alpsbte.plotsystem.core.database.DataProvider;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BuildTeam {
    private final int ID;
    private final String name;

    public BuildTeam(int ID) throws SQLException {
        this.ID = ID;
        this.name = DataProvider.BUILD_TEAM.getName(ID);
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

    public List<Country> getCountries() {
        return DataProvider.BUILD_TEAM.getCountries(ID);
    }

    public List<Builder> getReviewers() {
        return DataProvider.BUILD_TEAM.getReviewers(ID);
    }

    public static void addBuildTeam(String name) throws SQLException {
        DataProvider.BUILD_TEAM.addBuildTeam(name);
    }

    public static void removeBuildTeam(int serverID) throws SQLException {
        DataProvider.BUILD_TEAM.removeBuildTeam(serverID);
    }

    public static void setBuildTeamName(int id, String newName) throws SQLException {
        DataProvider.BUILD_TEAM.setBuildTeamName(id, newName);
    }

    public static void addCountry(int id, int countryID) throws SQLException {
        DataProvider.BUILD_TEAM.addCountry(id, countryID);
    }

    public static void removeCountry(int id, int countryID) throws SQLException {
        DataProvider.BUILD_TEAM.removeCountry(id, countryID);
    }

    public static void removeReviewer(int id, String reviewerUUID) throws SQLException {
        DataProvider.BUILD_TEAM.removeReviewer(id, reviewerUUID);
    }

    public static void addReviewer(int id, String reviewerUUID) throws SQLException {
        DataProvider.BUILD_TEAM.addReviewer(id, reviewerUUID);
    }
}
