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
