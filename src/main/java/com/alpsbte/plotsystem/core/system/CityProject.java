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

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class CityProject {

    private final int ID;
    private int countryID;

    private String name;
    private String description;
    private boolean visible;

    public CityProject(int ID) throws SQLException {
        this.ID = ID;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT country_id, name, description, visible FROM plotsystem_city_projects WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                this.countryID = rs.getInt(1);
                this.name = rs.getString(2);
                this.description = rs.getString(3);
                this.visible = rs.getInt(4) == 1;
            }

            DatabaseConnection.closeResultSet(rs);
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

    public ItemStack getItem(Player player, PlotDifficulty selectedPlotDifficulty) throws SQLException {
        ItemStack cpItem = getCountry().getHead();
        try {
            PlotDifficulty cpPlotDifficulty = selectedPlotDifficulty != null ?
                    selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(getID(), Builder.byUUID(player.getUniqueId())).get();

            int plotsOpen = PlotManager.getPlots(getID(), Status.unclaimed).size();
            int plotsInProgress = PlotManager.getPlots(getID(), Status.unfinished, Status.unreviewed).size();
            int plotsCompleted = PlotManager.getPlots(getID(), Status.completed).size();
            int plotsUnclaimed = cpPlotDifficulty != null ? PlotManager.getPlots(getID(), cpPlotDifficulty, Status.unclaimed).size() : 0;
            int plotsOpenForPlayer = cpPlotDifficulty != null && plotsUnclaimed != 0 ? getOpenPlotsForPlayer(getID(), cpPlotDifficulty) : 0;

            return new ItemBuilder(cpItem)
                    .setName("§b§l" + getName())
                    .setLore(new LoreBuilder()
                            .addLines(getDescription(),
                                    "",
                                    "§6" + plotsOpen + " §7" + LangUtil.getInstance().get(player, LangPaths.CityProject.PROJECT_OPEN) + " §8" + LangUtil.getInstance().get(player, LangPaths.CityProject.FOR_YOUR_DIFFICULTY, (plotsOpenForPlayer == 0 ? "§c" : "§a") + plotsOpenForPlayer + "§8"),
                                    "§8---------------------",
                                    "§6" + plotsInProgress + " §7" + LangUtil.getInstance().get(player, LangPaths.CityProject.PROJECT_IN_PROGRESS),
                                    "§6" + plotsCompleted + " §7" + LangUtil.getInstance().get(player, LangPaths.CityProject.PROJECT_COMPLETED),
                                    "",
                                    plotsUnclaimed != 0 ? Utils.ChatUtils.getFormattedDifficulty(cpPlotDifficulty) : "§f§l" + LangUtil.getInstance().get(player, LangPaths.CityProject.PROJECT_NO_PLOTS_AVAILABLE)
                            ).build())
                    .build();

        } catch (SQLException | ExecutionException | InterruptedException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return MenuItems.errorItem(player);
        }
    }

    public static List<CityProject> getCityProjects(Country country, boolean onlyVisible) {
        // if country is not null, only get country's city projects, otherwise load all
        DatabaseConnection.StatementBuilder statement = DatabaseConnection.createStatement("SELECT id FROM plotsystem_city_projects " + (country == null ? "" : "WHERE country_id = ?") + " ORDER BY country_id");
        if (country != null) {
            statement.setValue(country.getID());
        }

        try (ResultSet rs = statement.executeQuery()) {
            List<CityProject> cityProjects = new ArrayList<>();
            while (rs.next()) {
                CityProject city = new CityProject(rs.getInt(1));
                if (city.isVisible() || !onlyVisible) {
                    cityProjects.add(city);
                }
            }

            DatabaseConnection.closeResultSet(rs);
            return cityProjects;
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return new ArrayList<>();
    }

    private int getOpenPlotsForPlayer(int plotID, PlotDifficulty plotDifficulty) throws SQLException {
        return PlotManager.getPlots(plotID, plotDifficulty, Status.unclaimed).size();
    }


    public static List<CityProject> getCityProjects(boolean onlyVisible) {
        return getCityProjects(null, onlyVisible);
    }

    public static void addCityProject(Country country, String name) throws SQLException {
        DatabaseConnection.createStatement("INSERT INTO plotsystem_city_projects (id, name, country_id, description, visible) VALUES (?, ?, ?, ?, ?)")
                .setValue(DatabaseConnection.getTableID("plotsystem_city_projects"))
                .setValue(name)
                .setValue(country.getID())
                .setValue("")
                .setValue(true).executeUpdate();
    }

    public static void removeCityProject(int id) throws SQLException {
        DatabaseConnection.createStatement("DELETE FROM plotsystem_city_projects WHERE id = ?")
                .setValue(id).executeUpdate();
    }

    public static void setCityProjectName(int id, String newName) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_city_projects SET name = ? WHERE id = ?")
                .setValue(newName)
                .setValue(id).executeUpdate();
    }

    public static void setCityProjectDescription(int id, String description) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_city_projects SET description = ? WHERE id = ?")
                .setValue(description)
                .setValue(id).executeUpdate();
    }

    public static void setCityProjectVisibility(int id, boolean isEnabled) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_city_projects SET visible = ? WHERE id = ?")
                .setValue(isEnabled ? 1 : 0)
                .setValue(id).executeUpdate();
    }
}
