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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BuildTeam {
    private final int id;
    private String name;
    private final List<CityProject> cityProjects;
    private final List<Builder> reviewers;

    @Contract(pure = true)
    public BuildTeam(int id, String name, List<CityProject> cities, List<Builder> reviewers) {
        this.id = id;
        this.name = name;
        this.cityProjects = cities;
        this.reviewers = reviewers;
    }

    public BuildTeam(int id, String name) {
        this(id, name, new ArrayList<>(), new ArrayList<>());
    }

    public int getId() {
        return id;
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
        if (DataProvider.BUILD_TEAM.setBuildTeamName(id, newName)) {
            this.name = newName;
            return true;
        }
        return false;
    }

    public boolean removeReviewer(String reviewerUUID) {
        Optional<Builder> removeReviewer = reviewers.stream().filter(r -> r.getUUID().toString().equals(reviewerUUID)).findFirst();
        if (removeReviewer.isEmpty()) return false;
        if (DataProvider.BUILD_TEAM.removeReviewer(id, reviewerUUID)) {
            this.reviewers.remove(removeReviewer.get());
            return true;
        }
        return false;
    }

    public boolean addReviewer(@NotNull Builder reviewer) {
        if (DataProvider.BUILD_TEAM.addReviewer(id, reviewer.getUUID().toString())) {
            this.reviewers.add(reviewer);
            return true;
        }
        return false;
    }
}
