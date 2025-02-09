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

package com.alpsbte.plotsystem.core.system;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.database.DataProvider;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class CityProject {
    private final String ID;
    private final String countryCode;
    private String serverName;
    private boolean isVisible;

    public CityProject(String id, String countryCode, String serverName, boolean isVisible) {
        this.ID = id;
        this.countryCode = countryCode;
        this.serverName = serverName;
        this.isVisible = isVisible;
        CityProjectProvider.cityProjects.add(this);
    }

    public String getID() {
        return ID;
    }

    public Country getCountry() {
        return DataProvider.COUNTRY.getCountryByCode(countryCode);
    }

    public String getServerName() {
        return serverName;
    }

    public boolean setServer(String serverName) {
        if (DataProvider.CITY_PROJECT.setServer(ID, serverName)) {
            this.serverName = serverName;
            return true;
        }
        return false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean setVisible(boolean isVisible) {
        if (DataProvider.CITY_PROJECT.setVisibility(ID, isVisible)) {
            this.isVisible = isVisible;
            return true;
        }
        return false;
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

    public ItemStack getItem(Player player, PlotDifficulty selectedPlotDifficulty) {
        ItemStack cpItem = getCountry().getCountryItem();
        try {
            PlotDifficulty plotDifficulty = selectedPlotDifficulty != null ?
                    selectedPlotDifficulty : Plot.getPlotDifficultyForBuilder(this, Builder.byUUID(player.getUniqueId())).get();

            int plotsOpen = DataProvider.PLOT.getPlots(this, Status.unclaimed).size();
            int plotsInProgress = DataProvider.PLOT.getPlots(this, Status.unfinished, Status.unreviewed).size();
            int plotsCompleted = DataProvider.PLOT.getPlots(this, Status.completed).size();
            int plotsUnclaimed = plotDifficulty != null
                    ? DataProvider.PLOT.getPlots(this, plotDifficulty, Status.unclaimed).size()
                    : 0;
            int plotsOpenForPlayer = plotDifficulty != null && plotsUnclaimed != 0
                    ? DataProvider.PLOT.getPlots(this, plotDifficulty, Status.unclaimed).size()
                    : 0;

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
                                    ? Utils.ItemUtils.getFormattedDifficulty(plotDifficulty)
                                    : text(LangUtil.getInstance().get(player, LangPaths.CityProject.PROJECT_NO_PLOTS_AVAILABLE), WHITE).decoration(BOLD, true))
                            .build())
                    .build();
        } catch (SQLException | ExecutionException | InterruptedException ex) {
            Utils.logSqlException(ex);
            Thread.currentThread().interrupt();
            return MenuItems.errorItem(player);
        }
    }
}
