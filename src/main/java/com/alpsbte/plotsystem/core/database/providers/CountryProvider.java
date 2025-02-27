package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Continent;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CountryProvider {
    private static final List<Country> cachedCountries = new ArrayList<>();

    public CountryProvider() {
        // cache all countries
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("SELECT country_code, continent, material, custom_model_data FROM country;")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Continent continent = Continent.fromDatabase(rs.getString(2));
                    Country country = new Country(rs.getString(1), continent, rs.getString(3), rs.getString(4));
                    cachedCountries.add(country);
                }
            }
        } catch (SQLException ex) {Utils.logSqlException(ex);}
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
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("UPDATE country SET material = ?, custom_model_data = ? WHERE country_code = ?;")) {
            stmt.setString(1, material);
            stmt.setString(2, customModelData);
            stmt.setString(3, code);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public boolean addCountry(String code, Continent continent, String material, @Nullable String customModelData) {
        if (getCountryByCode(code).isPresent()) return true;
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("INSERT INTO country (country_code, continent, material, custom_model_data) " +
                        "VALUES (?, ?, ?, ?);")) {
            stmt.setString(1, code);
            stmt.setString(2, continent.databaseEnum);
            stmt.setString(3, material);
            stmt.setString(4, customModelData);
            boolean result = stmt.executeUpdate() > 0;
            if (result) cachedCountries.add(new Country(code, continent, material, customModelData));
            return result;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public boolean removeCountry(String code) {
        Optional<Country> cachedCountry = getCountryByCode(code);
        if (cachedCountry.isEmpty()) return false;

        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("DELETE FROM country WHERE country_code = ?;")) {
            stmt.setString(1, code);
            boolean result = stmt.executeUpdate() > 0;
            if (result) cachedCountries.remove(cachedCountry.get());
            return result;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }
}
