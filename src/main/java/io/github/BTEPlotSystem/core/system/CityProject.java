package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Country;
import github.BTEPlotSystem.utils.enums.Difficulty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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

    public static List<CityProject> getCityProjects() {
        try {
            ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idcityProject FROM cityProjects ORDER BY CAST(country AS CHAR)");
            List<CityProject> cityProjects = new ArrayList<>();

            while (rs.next()) {
                cityProjects.add(new CityProject(rs.getInt(1)));
            }

            return cityProjects;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return null;
    }
}
