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
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.database.providers.CityProjectProvider;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class CityProject {

    private final int ID;
    private String countryCode;
    private String serverName;
    private boolean isVisible;

    public CityProject(int id, String countryCode, String serverName, boolean isVisible) {
        this.ID = id;
        this.countryCode = countryCode;
        this.serverName = serverName;
        this.isVisible = isVisible;
    }

    public int getID() {
        return ID;
    }

    public Country getCountry() {
        return DataProvider.COUNTRY.getCountryByCode(countryCode);
    }

    public String getName(Player player) {
        // TODO: implement (get from language file)
        return "CityProject WIP";
    }

    public String getDescription(Player player) {
        // TODO: implement (get from language file)
        return "Description WIP";
    }

    public ArrayList<Component> getDescriptionComponents(Player player) {
        ArrayList<Component> descriptionLines = new ArrayList<>();
        for (String line : getDescription(player).split("%newline%")) descriptionLines.add(text(line));
        return descriptionLines;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public ItemStack getItem(Player player, PlotDifficulty selectedPlotDifficulty) {
        ItemStack cpItem = getCountry().getCountryItem();
        try {
            PlotDifficulty cpPlotDifficulty = selectedPlotDifficulty != null ?
                    selectedPlotDifficulty : Plot.getPlotDifficultyForBuilder(getID(), Builder.byUUID(player.getUniqueId())).get();

            int plotsOpen = DataProvider.PLOT.getPlots(this, Status.unclaimed).size();
            int plotsInProgress = DataProvider.PLOT.getPlots(this, Status.unfinished, Status.unreviewed).size();
            int plotsCompleted = DataProvider.PLOT.getPlots(this, Status.completed).size();
            int plotsUnclaimed = cpPlotDifficulty != null ? Plot.getPlots(getID(), cpPlotDifficulty, Status.unclaimed).size() : 0;
            int plotsOpenForPlayer = cpPlotDifficulty != null && plotsUnclaimed != 0 ? getOpenPlotsForPlayer(getID(), cpPlotDifficulty) : 0;

            return new ItemBuilder(cpItem)
                    .setName(text(getName(player), AQUA).decoration(BOLD, true))
                    .setLore(new LoreBuilder()
                            .addLines(true, getDescription(player))
                            .emptyLine()
                            .addLine(text(plotsOpen, GOLD)
                                    .append(text(" " + LangUtil.getInstance().get(player, LangPaths.CityProject.PROJECT_OPEN) + " ", GRAY))
                                    .append(LangUtil.getInstance().getComponent(player.getUniqueId(), LangPaths.CityProject.FOR_YOUR_DIFFICULTY, DARK_GRAY,
                                            text(plotsOpenForPlayer + " ", plotsOpenForPlayer == 0 ? RED : GREEN))))
                            .addLine(text("---------------------", DARK_GRAY))
                            .addLine(text(plotsInProgress, GOLD)
                                    .append(text(" " + LangUtil.getInstance().get(player, LangPaths.CityProject.PROJECT_IN_PROGRESS), GRAY)))
                            .addLine(text(plotsCompleted, GOLD)
                                    .append(text(" " + LangUtil.getInstance().get(player, LangPaths.CityProject.PROJECT_COMPLETED), GRAY)))
                            .emptyLine()
                            .addLine(plotsUnclaimed != 0
                                    ? Utils.ItemUtils.getFormattedDifficulty(cpPlotDifficulty)
                                    : text(LangUtil.getInstance().get(player, LangPaths.CityProject.PROJECT_NO_PLOTS_AVAILABLE), WHITE).decoration(BOLD, true))
                            .build())
                    .build();
        } catch (SQLException | ExecutionException | InterruptedException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            return MenuItems.errorItem(player);
        }
    }

    private int getOpenPlotsForPlayer(int plotID, PlotDifficulty plotDifficulty) throws SQLException {
        return Plot.getPlots(plotID, plotDifficulty, Status.unclaimed).size();
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
