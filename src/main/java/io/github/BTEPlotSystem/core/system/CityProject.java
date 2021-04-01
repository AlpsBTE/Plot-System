package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.enums.Country;
import org.bukkit.Bukkit;

import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class CityProject {

    private final int ID;
    private String name;
    private Country country;
    private String description;
    private String tags;
    private boolean visible;

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

            // Visible
            this.visible = rs.getInt("visible") == 1;
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

    public boolean isVisible() { return visible; }

    public static List<CityProject> getCityProjects() {
        try {
            ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idcityProject FROM cityProjects ORDER BY CAST(country AS CHAR)");
            List<CityProject> cityProjects = new ArrayList<>();

            while (rs.next()) {
                CityProject city = new CityProject(rs.getInt(1));
                if(city.isVisible()) {
                    cityProjects.add(city);
                }
            }

            return cityProjects;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return null;
    }
}
