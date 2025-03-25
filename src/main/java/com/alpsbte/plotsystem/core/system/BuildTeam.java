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

import java.util.List;
import java.util.Optional;

public class BuildTeam {
    private final int ID;
    private String name;
    private final List<CityProject> cityProjects;
    private final List<Builder> reviewers;

    public BuildTeam(int ID, String name, List<CityProject> cities, List<Builder> reviewers) {
        this.ID = ID;
        this.name = name;
        this.cityProjects = cities;
        this.reviewers = reviewers;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public List<CityProject> getCityProjects() {
        return cityProjects;
    }

    public List<Builder> getReviewers() {
        return reviewers;
    }

    public boolean setName(String newName) {
        if (DataProvider.BUILD_TEAM.setBuildTeamName(ID, newName)) {
            this.name = newName;
            return true;
        }
        return false;
    }

    public boolean addCityProject(CityProject city) {
        /*if (DataProvider.CITY_PROJECT.addCityProject(ID, city.getID())) {
            this.cityProjects.add(city);
            return true;
        }*/
        // TODO: Implement
        return false;
    }

    public boolean removeCityProject(String cityId) {
        Optional<CityProject> removeCity = cityProjects.stream().filter(c -> c.getID().equalsIgnoreCase(cityId)).findFirst();
        if (removeCity.isEmpty()) return false;
        /*if (DataProvider.BUILD_TEAM.removeCityProject(ID, cityId)) {
            this.cityProjects.remove(removeCity.get());
            return true;
        }*/
        // TODO: Implement
        return false;
    }

    public boolean removeReviewer(String reviewerUUID) {
        Optional<Builder> removeReviewer = reviewers.stream().filter(r -> r.getUUID().toString().equals(reviewerUUID)).findFirst();
        if (removeReviewer.isEmpty()) return false;
        if (DataProvider.BUILD_TEAM.removeReviewer(ID, reviewerUUID)) {
            this.reviewers.remove(removeReviewer.get());
            return true;
        }
        return false;
    }

    public boolean addReviewer(Builder reviewer) {
        if (DataProvider.BUILD_TEAM.addReviewer(ID, reviewer.getUUID().toString())) {
            this.reviewers.add(reviewer);
            return true;
        }
        return false;
    }
}
