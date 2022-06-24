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

package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.api.database.repositories.CityProjectRepositoryMySQL;
import com.alpsbte.plotsystem.api.entities.CityProjectDTO;
import com.alpsbte.plotsystem.api.entities.mapper.EntityMapper;
import com.alpsbte.plotsystem.api.http.repositories.CityProjectRepositoryHTTP;
import com.alpsbte.plotsystem.api.repositories.ICityProjectRepository;
import com.alpsbte.plotsystem.core.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import org.bukkit.Bukkit;

import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CityProject {
    private static final ICityProjectRepository repository = PlotSystem.getStorageMethod() == PlotSystem.StorageMethod.API ? new CityProjectRepositoryHTTP() : new CityProjectRepositoryMySQL();
    private final CityProjectDTO dto;

    public CityProject(CityProjectDTO dto) {
        this.dto = dto;
    }

    public CityProject(int ID) throws SQLException {
        dto = repository.getCityProject(ID);
    }

    public int getID() {
        return dto.getCityId();
    }

    public Country getCountry() throws SQLException {
        return new Country(dto.getCountryId());
    }

    public String getName() {
        return dto.getName();
    }

    public String getDescription() {
        return dto.getDescription();
    }

    public boolean isVisible() {
        // Waiting for https://github.com/AlpsBTE/Plot-System-API/issues/33 to be fixed; this should be a Boolean returned from dto#getVisible
        return dto.getVisible() == 1;
    }

    public static List<CityProject> getCityProjects(boolean onlyVisible) {
        return Arrays.stream(repository.getCityProjects()).map(CityProject::new).collect(Collectors.toList());
    }

    public static void addCityProject(Country country, String name) throws SQLException {
        // Waiting for https://github.com/AlpsBTE/Plot-System-API/issues/33 to be fixed; this should be a Boolean for visible
        CityProjectDTO dto = new CityProjectDTO((int) Math.floor(Math.random() * 99999), country.getID(), name, "", 1);
        repository.addCityProject(dto);
    }

    public static void removeCityProject(int id) throws SQLException {
        repository.deleteCityProject(id);
    }

    public static void setCityProjectName(int id, String newName) throws SQLException {
        repository.updateCityProjectName(id, newName);
    }

    public static void setCityProjectDescription(int id, String description) throws SQLException {
        repository.updateCityProjectDescription(id, description);
    }

    public static void setCityProjectVisibility(int id, boolean isEnabled) throws SQLException {
        repository.updateCityProjectVisibility(id, isEnabled);
    }

    @Override
    public String toString() {
        return "CityProject{" + "dto=" + dto + '}';
    }
}
