/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.heads.CustomHead;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.utils.enums.Continent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Country {

    private final int ID;
    private int serverID;

    private String name;
    private String headID;

    private String continent;

    public Country(int ID) throws SQLException {
        this.ID = ID;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT server_id, name, head_id, continent FROM plotsystem_countries WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                this.serverID = rs.getInt(1);
                this.name = rs.getString(2);
                this.headID = rs.getString(3);
                this.continent = rs.getString(4);
            }

            DatabaseConnection.closeResultSet(rs);
        }
    }

    public int getID() {
        return ID;
    }

    public Server getServer() throws SQLException {
        return new Server(serverID);
    }

    public String getName() {
        return name;
    }

    public ItemStack getHead() {
        return AlpsUtils.getItemHead(new CustomHead(headID));
    }

    /**
     * Get city projects that are inside of this country
     * <p>
     * Might be a good idea to put this in CityProject but could work in both classes
     *
     * @return CityProjects inside this country
     */
    public List<CityProject> getCityProjects() {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_city_projects WHERE country_id = ?").setValue(getID()).executeQuery()) {
            List<CityProject> cityProjects = new ArrayList<>();
            while (rs.next()) {
                cityProjects.add(new CityProject(rs.getInt(1)));
            }

            DatabaseConnection.closeResultSet(rs);
            return cityProjects;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return new ArrayList<>();
    }

    public Continent getContinent() {
        return Continent.fromDatabase(continent);
    }

    public static List<Country> getCountries() {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_countries ORDER BY server_id").executeQuery()) {
            List<Country> countries = new ArrayList<>();
            while (rs.next()) {
                countries.add(new Country(rs.getInt(1)));
            }

            DatabaseConnection.closeResultSet(rs);
            return countries;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return new ArrayList<>();
    }

    public static List<Country> getCountries(Continent continent) {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_countries WHERE continent = ? ORDER BY server_id").setValue(continent.databaseEnum).executeQuery()) {
            List<Country> countries = new ArrayList<>();
            while (rs.next()) {
                countries.add(new Country(rs.getInt(1)));
            }

            DatabaseConnection.closeResultSet(rs);
            return countries;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return new ArrayList<>();
    }

    public static void addCountry(int serverID, String name, Continent continent) throws SQLException {
        DatabaseConnection.createStatement("INSERT INTO plotsystem_countries (id, name, server_id, continent) VALUES (?, ?, ?, ?)")
                .setValue(DatabaseConnection.getTableID("plotsystem_countries"))
                .setValue(name)
                .setValue(serverID).setValue(continent.databaseEnum).executeUpdate();
    }

    public static void removeCountry(int countryID) throws SQLException {
        DatabaseConnection.createStatement("DELETE FROM plotsystem_countries WHERE id = ?")
                .setValue(countryID).executeUpdate();
    }

    public static void setHeadID(int countryID, int headID) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_countries SET head_id = ? WHERE id = ?")
                .setValue(headID)
                .setValue(countryID).executeUpdate();
    }
}