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

package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.enums.Continent;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CountryMenu extends AbstractMenu {
    private List<Country> countryProjects;
    private final Continent selectedContinent;
    private PlotDifficulty selectedPlotDifficulty = null;

    CountryMenu(Player player, Continent continent) {
        super(6, LangUtil.getInstance().get(player, continent.langPath) + " -> " + LangUtil.getInstance().get(player, LangPaths.MenuTitle.COMPANION_SELECT_COUNTRY) , player);
        selectedContinent = continent;
    }

    CountryMenu(Player player, Continent continent, PlotDifficulty plotDifficulty) {
        this(player, continent);
        this.selectedPlotDifficulty = plotDifficulty;
    }

    @Override
    protected void setPreviewItems() {
        // Set navigator item
        getMenu().getSlot(4)
                .setItem(new ItemBuilder(Material.valueOf(PlotSystem.getPlugin().getConfig().getString(ConfigPaths.NAVIGATOR_ITEM)), 1)
                        .setName("§6§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.NAVIGATOR)).setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.NAVIGATOR)).build())
                        .build());

        // Set loading item for plots difficulty item
        getMenu().getSlot(7).setItem(MenuItems.loadingItem(Material.PLAYER_HEAD, getMenuPlayer()));

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> {
            player.closeInventory();
            new CountryMenu(player, selectedContinent);
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setItem(entry.getValue().item);
        }

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plots difficulty item head
        getMenu().getSlot(7).setItem(CompanionMenu.getDifficultyItem(getMenuPlayer(), selectedPlotDifficulty));

        // Set city project items
        try {
            countryProjects = Country.getCountries(selectedContinent);
            setCountryItems();
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for navigator item
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand(PlotSystem.getPlugin().getConfig().getString(ConfigPaths.NAVIGATOR_COMMAND));
        });

        // Set click event for plots difficulty item
        getMenu().getSlot(7).setClickHandler(((clickPlayer, clickInformation) -> {
            selectedPlotDifficulty = (selectedPlotDifficulty == null ?
                    PlotDifficulty.values()[0] : selectedPlotDifficulty.ordinal() != PlotDifficulty.values().length - 1 ?
                    PlotDifficulty.values()[selectedPlotDifficulty.ordinal() + 1] : null);

            getMenu().getSlot(7).setItem(CompanionMenu.getDifficultyItem(getMenuPlayer(), selectedPlotDifficulty));
            clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.DONE_SOUND, 1, 1);

            try {
                setCountryItems();
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }));

        int startingSlot = 9;
        if (CompanionMenu.hasContinentView()) {
            getMenu().getSlot(0).setClickHandler((clickPlayer, clickInformation) -> {
                clickPlayer.closeInventory();
                new ContinentMenu(clickPlayer);
            });
        }

        for (Country country : countryProjects) {
            int i = countryProjects.indexOf(country);
            getMenu().getSlot(startingSlot + i).setClickHandler((clickPlayer, clickInformation) -> {
                clickPlayer.closeInventory();
                new CityProjectMenu(clickPlayer, country, selectedPlotDifficulty);
            });
        }

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> {
            player.closeInventory();
            new CountryMenu(player, selectedContinent);
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setClickHandler(entry.getValue().clickHandler);
        }
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(" ").build())
                .pattern("011101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("100010001")
                .build();
    }

    private void setCountryItems() throws SQLException {
        int startingSlot = 9;
        if (CompanionMenu.hasContinentView()) {
            getMenu().getSlot(0).setItem(new ItemBuilder(AlpsUtils.getItemHead(Utils.HeadUtils.BACK_BUTTON_HEAD))
                    .setName("§b§lBack")
                    .build());
        } else {
            getMenu().getSlot(0).setItem(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(" ").build());
        }

        for (Country country : countryProjects) {
            ItemStack item = country.getHead();

            List<CityProject> cities = country.getCityProjects();
            int plotsOpen = PlotManager.getPlots(cities, Status.unclaimed).size();
            int plotsInProgress = PlotManager.getPlots(cities, Status.unfinished, Status.unreviewed).size();
            int plotsCompleted = PlotManager.getPlots(cities, Status.completed).size();
            int plotsUnclaimed = PlotManager.getPlots(cities, Status.unclaimed).size();

            getMenu().getSlot(startingSlot + countryProjects.indexOf(country)).setItem(new ItemBuilder(item)
                    .setName("§b§l" + country.getName())
                    .setLore(new LoreBuilder()
                            .addLines(
                                    "§6" + cities.size() + " §7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.CITIES),
                                    "",
                                    "§6" + plotsOpen + " §7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.PROJECT_OPEN),
                                    "§8---------------------",
                                    "§6" + plotsInProgress + " §7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.PROJECT_IN_PROGRESS),
                                    "§6" + plotsCompleted + " §7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.PROJECT_COMPLETED),
                                    "",
                                    (plotsUnclaimed > 0 ? "§a§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.PROJECT_PLOTS_AVAILABLE) : "§f§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.CityProject.PROJECT_NO_PLOTS_AVAILABLE))
                            )
                            .build())
                    .build());
        }
    }

}