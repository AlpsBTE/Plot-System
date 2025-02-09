package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.enums.Continent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CountryProvider {
    public List<Country> getCountries() {
        // TODO: implement
        return List.of();
    }

    public List<Country> getCountriesByContinent(Continent continent) {
        // TODO: implement
        return List.of();
    }

    public Country getCountryByCode(String code) {
        // TODO: implement
        //Continent continent = Continent.fromDatabase()

        return null;
    }

    public boolean setMaterialAndCustomModelData(String material, @Nullable String customModelData) {
        // TODO: implement
        return false;
    }

    public boolean addCountry(String code, Continent continent, String material, @Nullable String customModelData) {
        // TODO: implement
        return false;
    }

    public boolean removeCountry(String code) {
        // TODO: implement
        return false;
    }
}
