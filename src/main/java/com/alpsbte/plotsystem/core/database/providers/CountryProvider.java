/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.alpslib.io.database.SqlHelper;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Continent;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CountryProvider {
    protected static final List<Country> COUNTRIES = new ArrayList<>();

    public CountryProvider() {
        String qAll = "SELECT country_code, continent, material, custom_model_data FROM country;";
        Utils.handleSqlException(() -> SqlHelper.runQuery(qAll, ps -> {
            ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Continent continent = Continent.fromDatabase(rs.getString(2));
                    Country country = new Country(rs.getString(1), continent, rs.getString(3),
                            rs.getString(4));
                    COUNTRIES.add(country); // cache all countries
                }
        }));
    }

    public List<Country> getCountries() {
        return COUNTRIES;
    }

    public List<Country> getCountriesByContinent(Continent continent) {
        return COUNTRIES.stream().filter(c -> c.getContinent() == continent).toList();
    }

    public Optional<Country> getCountryByCode(String code) {
        return COUNTRIES.stream().filter(c -> c.getCode().equals(code)).findFirst();
    }

    public boolean setMaterialAndCustomModelData(String code, String material, @Nullable String customModelData) {
        String qSetItem = "UPDATE country SET material = ?, custom_model_data = ? WHERE country_code = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetItem, ps -> {
            ps.setString(1, material);
            ps.setString(2, customModelData);
            ps.setString(3, code);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean addCountry(String code, Continent continent, String material, @Nullable String customModelData) {
        if (getCountryByCode(code).isPresent()) return true;

        String qInsert = "INSERT INTO country (country_code, continent, material, custom_model_data) VALUES (?, ?, ?, ?);";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qInsert, ps -> {
            ps.setString(1, code);
            ps.setString(2, continent.databaseEnum);
            ps.setString(3, material);
            ps.setString(4, customModelData);
            boolean result = ps.executeUpdate() > 0;
            if (result) COUNTRIES.add(new Country(code, continent, material, customModelData));
            return result;
        })));
    }

    public boolean removeCountry(String code) {
        Optional<Country> cachedCountry = getCountryByCode(code);
        if (cachedCountry.isEmpty()) return false;

        String qDelete = "DELETE FROM country WHERE country_code = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qDelete, ps -> {
            ps.setString(1, code);
            boolean result = ps.executeUpdate() > 0;
            if (result) COUNTRIES.remove(cachedCountry.get());
            return result;
        })));
    }
}
