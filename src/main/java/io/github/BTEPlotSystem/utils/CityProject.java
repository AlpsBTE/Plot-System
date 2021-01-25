package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.DatabaseConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CityProject {

    private final int ID;
    private String name;
    private Country country;
    private String description;
    private String tags;
    private Difficulty difficulty;

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

            // Difficulty
            this.difficulty = Difficulty.values()[rs.getInt("difficulty_iddifficulty") - 1];
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

    public String getDescription() {
        return description;
    }

    public String getTags() {
        return tags;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public static List<CityProject> getCityProjects() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idcityProject FROM cityProjects");
        List<CityProject> cityProjects = new ArrayList<>();

        while (rs.next()) {
            cityProjects.add(new CityProject(rs.getInt("idcityProject")));
        }

        return cityProjects;
    }
}
