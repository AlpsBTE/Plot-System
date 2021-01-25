package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.DatabaseConnection;

import java.sql.ResultSet;
import java.sql.SQLException;

public class City {

    private final int ID;
    private String name;
    private Country country;
    private String description;
    private String tags;
    private Difficulty difficulty;

    public City(int ID) throws SQLException {
        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM cityProjects WHERE idcityProject =" + ID);

        if(rs.next()) {
            // City Name
            this.name = rs.getString("name");

            // Country
            this.country = Country.valueOf(rs.getString("country"));

            // Description
            this.description = rs.getString("description");

            // Tags
            this.tags = rs.getString("tags");

            // Difficulty
            this.difficulty = Difficulty.valueOf(rs.getString("difficulty_iddifficulty"));
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
}
