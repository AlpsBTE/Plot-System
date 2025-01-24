package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.Review;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.enums.Status;

import java.util.ArrayList;
import java.util.List;

public class PlotProvider {
    public Plot getPlotById(int plotId) {
        // TODO: implement

        // set status
        // set owner

        return null;
    }

    public List<Plot> getPlots(CityProject city, Status... statuses) {
        // TODO: implement
        return List.of();
    }

    public List<Plot> getPlots(List<CityProject> cities, Status... statuses) {
        // TODO: implement
        return List.of();
    }

    public List<Plot> getPlots(List<Country> countries, Status status) {
        List<CityProject> cities = new ArrayList<>();
        countries.forEach(c -> cities.addAll(c.getCityProjects()));
        return getPlots(cities, status);
    }

    public List<Plot> getPlots(Builder builder) {
        // TODO: get plots where builder is either owner or member
        return List.of();
    }

    public List<Plot> getPlots(Builder builder, Status... statuses) {
        // TODO: get plots where builder is either owner or member and filter by status
        return List.of();
    }

    public List<Builder> getPlotMembers(int plotId) {
        // TODO: implement
        return null;
    }

    public Review getReview(int plotId) {
        // TODO: implement
        return null;
    }
}
