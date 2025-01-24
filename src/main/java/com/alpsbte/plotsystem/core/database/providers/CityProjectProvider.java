package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;

import java.util.List;

public class CityProjectProvider {
    public CityProject getCityProjectById(String id) {
        // TODO: implement
        return null;
    }

    public List<CityProject> getCityProjectsByCountryCode(String countryCode) {
        // TODO: implement
        return List.of();
    }

    public List<CityProject> getCityProjects(Country country, boolean onlyVisible) {
        // TODO: implement
        return List.of();
    }

    public List<CityProject> getCityProjects(boolean onlyVisible) {
        // TODO: implement
        return List.of();
    }

    public boolean addCityProject(String id, String countryCode, String serverName) {
        // TODO: implement (isVisible can be default)
        // return false if error occurred
        return false;
    }

    public boolean removeCityProject(String id) {
        // TODO: implement
        // return false if error occurred
        return false;
    }

    public boolean setCityProjectVisibility(String id, boolean isVisible) {
        // TODO: implement
        return false;
    }

    public boolean setCityProjectServer(String id, String serverName) {
        // TODO: implement
        return false;
    }
}
