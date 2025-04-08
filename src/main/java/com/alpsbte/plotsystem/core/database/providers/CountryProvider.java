/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Continent;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CountryProvider {
    private static final List<Country> cachedCountries = new ArrayList<>();

    public CountryProvider() {
        String query = "SELECT country_code, continent, material, custom_model_data FROM country;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Continent continent = Continent.fromDatabase(rs.getString(2));
                    Country country = new Country(rs.getString(1), continent, rs.getString(3),
                            rs.getString(4));
                    cachedCountries.add(country); // cache all countries
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
    }

    public List<Country> getCountries() {
        return cachedCountries;
    }

    public List<Country> getCountriesByContinent(Continent continent) {
        return cachedCountries.stream().filter(c -> c.getContinent() == continent).toList();
    }

    public Optional<Country> getCountryByCode(String code) {
        return cachedCountries.stream().filter(c -> c.getCode().equals(code)).findFirst();
    }

    public boolean setMaterialAndCustomModelData(String code, String material, @Nullable String customModelData) {
        String query = "UPDATE country SET material = ?, custom_model_data = ? WHERE country_code = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, material);
            stmt.setString(2, customModelData);
            stmt.setString(3, code);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean addCountry(String code, Continent continent, String material, @Nullable String customModelData) {
        if (getCountryByCode(code).isPresent()) return true;

        String query = "INSERT INTO country (country_code, continent, material, custom_model_data) VALUES (?, ?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, code);
            stmt.setString(2, continent.databaseEnum);
            stmt.setString(3, material);
            stmt.setString(4, customModelData);
            boolean result = stmt.executeUpdate() > 0;
            if (result) cachedCountries.add(new Country(code, continent, material, customModelData));
            return result;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean removeCountry(String code) {
        Optional<Country> cachedCountry = getCountryByCode(code);
        if (cachedCountry.isEmpty()) return false;

        String query = "DELETE FROM country WHERE country_code = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, code);
            boolean result = stmt.executeUpdate() > 0;
            if (result) cachedCountries.remove(cachedCountry.get());
            return result;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }
}
