package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.enums.Country;
import github.BTEPlotSystem.utils.enums.Difficulty;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CityProject {

    private final int ID;
    private String name;
    private Country country;
    private String description;
    private String tags;

    public CityProject(int ID) throws SQLException {
        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM cityProjects WHERE idcityProject = '" + ID + "'");

        if(rs.next()) {
            // Name
            this.name = rs.getString("name");

            // Country
            this.country = Country.valueOf(rs.getString("country"));

            // Description
            this.description = rs.getString("description");

            // Tags
            this.tags = rs.getString("tags");
        }
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public Country getCountry() {
        return country;
    }

    public String getDescription() { return description; }

    public String getTags() {
        return tags;
    }

    public Difficulty getAverageDifficulty() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT iddifficulty FROM plots WHERE idcity = '" + getID() + "'");

        int diff_easy = 0, diff_medium = 0, diff_hard = 0;

        while (rs.next()) {
            switch (rs.getInt(1)) {
                case 1:
                    diff_easy++;
                    break;
                case 2:
                    diff_medium++;
                    break;
                case 3:
                    diff_hard++;
                    break;
            }
        }

        int i = Math.max(diff_easy, diff_medium);
        int max = Math.max(i, diff_hard);

        if(max == diff_easy) {
            return Difficulty.EASY;
        } else if(max == diff_medium) {
            return Difficulty.MEDIUM;
        } else {
            return Difficulty.HARD;
        }
    }

    public static List<CityProject> getCityProjects() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idcityProject FROM cityProjects");
        List<CityProject> cityProjects = new ArrayList<>();

        while (rs.next()) {
            cityProjects.add(new CityProject(rs.getInt(1)));
        }

        return cityProjects;
    }
}
