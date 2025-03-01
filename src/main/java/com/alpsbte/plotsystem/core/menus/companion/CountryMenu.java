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

package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.core.menus.tutorial.TutorialsMenu;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.enums.Continent;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class CountryMenu extends AbstractMenu {
    private List<Country> countryProjects;
    private final Continent selectedContinent;
    private PlotDifficulty selectedPlotDifficulty = null;

    CountryMenu(Player player, @NotNull Continent continent) {
        super(6, LangUtil.getInstance().get(player, continent.langPath) + " → " + LangUtil.getInstance().get(player, LangPaths.MenuTitle.COMPANION_SELECT_COUNTRY), player);
        selectedContinent = continent;
    }

    CountryMenu(Player player, Continent continent, PlotDifficulty plotDifficulty) {
        this(player, continent);
        this.selectedPlotDifficulty = plotDifficulty;
    }

    @Override
    protected void setPreviewItems() {
        getMenu().getSlot(0).setItem(MenuItems.getRandomItem(getMenuPlayer())); // Set random selection item

        // Set plots difficulty item head
        getMenu().getSlot(6).setItem(CompanionMenu.getDifficultyItem(getMenuPlayer(), selectedPlotDifficulty));

        // Set tutorial item
        getMenu().getSlot(7).setItem(PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE) ?
                TutorialsMenu.getTutorialItem(getMenuPlayer()) : new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(empty()).build());

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> new CountryMenu(player, selectedContinent)).entrySet()) {
            getMenu().getSlot(entry.getKey()).setItem(entry.getValue().item);
        }

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set city project items
        countryProjects = DataProvider.COUNTRY.getCountriesByContinent(selectedContinent);
        setCountryItems();
    }

    @Override
    protected void setItemClickEventsAsync() {
        getMenu().getSlot(0).setClickHandler((clickPlayer, clickInformation) ->  // Set click event for random selection item
                generateRandomPlot(clickPlayer, countryProjects, selectedPlotDifficulty));

        // Set click event for plots difficulty item
        getMenu().getSlot(6).setClickHandler(((clickPlayer, clickInformation) -> {
            selectedPlotDifficulty = (selectedPlotDifficulty == null ?
                    PlotDifficulty.values()[0] : selectedPlotDifficulty.ordinal() != PlotDifficulty.values().length - 1 ?
                    PlotDifficulty.values()[selectedPlotDifficulty.ordinal() + 1] : null);

            getMenu().getSlot(6).setItem(CompanionMenu.getDifficultyItem(getMenuPlayer(), selectedPlotDifficulty));
            clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.DONE_SOUND, 1, 1);

            setCountryItems();
        }));

        CompanionMenu.clickEventTutorialItem(getMenu());

        int startingSlot = 9;
        if (CompanionMenu.hasContinentView()) {
            getMenu().getSlot(0).setClickHandler((clickPlayer, clickInformation) -> new ContinentMenu(clickPlayer));
        }

        for (Country country : countryProjects) {
            int i = countryProjects.indexOf(country);
            getMenu().getSlot(startingSlot + i).setClickHandler((clickPlayer, clickInformation) -> new CityProjectMenu(clickPlayer, country, selectedPlotDifficulty));
        }

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> new CountryMenu(player, selectedContinent)).entrySet()) {
            getMenu().getSlot(entry.getKey()).setClickHandler(entry.getValue().clickHandler);
        }
    }

    public static boolean generateRandomPlot(Player clickPlayer, @NotNull List<Country> countryProjects, PlotDifficulty selectedPlotDifficulty) {
        List<CityProject> cityProjects = new ArrayList<>();
        for (Country curCountry : countryProjects) {
            cityProjects.addAll(CityProjectMenu.getValidCityProjects(selectedPlotDifficulty, clickPlayer, curCountry));
        }

        return CityProjectMenu.generateRandomPlot(clickPlayer, cityProjects, selectedPlotDifficulty);
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(empty()).build())
                .pattern("101111001")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("100010001")
                .build();
    }

    private void setCountryItems() {
        int startingSlot = 9;
        if (CompanionMenu.hasContinentView()) {
            getMenu().getSlot(1).setItem(MenuItems.backMenuItem(getMenuPlayer()));
        } else {
            getMenu().getSlot(1).setItem(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(empty()).build());
        }

        for (Country country : countryProjects) {
            ItemStack item = country.getCountryItem();

            List<CityProject> cities = country.getCityProjects();
            int plotsOpen = DataProvider.PLOT.getPlots(cities, Status.unclaimed).size();
            int plotsInProgress = DataProvider.PLOT.getPlots(cities, Status.unfinished, Status.unreviewed).size();
            int plotsCompleted = DataProvider.PLOT.getPlots(cities, Status.completed).size();
            int plotsUnclaimed = DataProvider.PLOT.getPlots(cities, Status.unclaimed).size();

            getMenu().getSlot(startingSlot + countryProjects.indexOf(country)).setItem(new ItemBuilder(item)
                    .setName(text(country.getName(getMenuPlayer()), AQUA).decoration(BOLD, true))
                    .setLore(new LoreBuilder()
                            .addLine(text(cities.size(), GOLD).append(text(" " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.CITIES), GRAY)))
                            .emptyLine()
                            .addLines(text(plotsOpen, GOLD).append(text(" " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.PROJECT_OPEN), GRAY)),
                                    text("---------------------", DARK_GRAY),
                                    text(plotsInProgress, GOLD).append(text(" " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.PROJECT_IN_PROGRESS), GRAY)),
                                    text(plotsCompleted, GOLD).append(text(" " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.PROJECT_COMPLETED), GRAY)))
                            .emptyLine()
                            .addLine(plotsUnclaimed > 0
                                    ? text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.PROJECT_PLOTS_AVAILABLE), GREEN).decoration(BOLD, true)
                                    : text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.PROJECT_NO_PLOTS_AVAILABLE), WHITE).decoration(BOLD, true))
                            .build())
                    .build());
        }
    }
}