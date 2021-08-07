/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.core.database.DatabaseConnection;
import org.bukkit.Bukkit;

import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class CityProject {

    private final int ID;
    private int countryID;

    private String name;
    private String description;
    private boolean visible;


    public CityProject(int ID) throws SQLException {
        this.ID = ID;

        ResultSet rs = DatabaseConnection.createStatement("SELECT country_id, name, description, visible FROM plotsystem_city_projects WHERE id = ?")
                .setValue(this.ID).executeQuery();

        if (rs.next()) {
            this.countryID = rs.getInt(1);
            this.name = rs.getString(2);
            this.description = rs.getString(3);
            this.visible = rs.getInt(4) == 1;
        }
    }

    public int getID() {
        return ID;
    }

    public Country getCountry() throws SQLException {
        return new Country(countryID);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVisible() {
        return visible;
    }


    public static List<CityProject> getCityProjects() {
        try {
            ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_city_projects ORDER BY country_id").executeQuery();

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
        return new ArrayList<>();
    }
}
