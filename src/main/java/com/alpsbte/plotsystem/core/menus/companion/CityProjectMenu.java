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

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.AbstractPaginatedMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CityProjectMenu extends AbstractPaginatedMenu {
    Country country;
    List<CityProject> projects;
    private PlotDifficulty selectedPlotDifficulty;

    CityProjectMenu(Player player, Country country, PlotDifficulty selectedPlotDifficulty) {
        super(6, 4, country.getName() + " -> " + LangUtil.getInstance().get(player, LangPaths.MenuTitle.COMPANION_SELECT_CITY), player);
        this.country = country;
        this.selectedPlotDifficulty = selectedPlotDifficulty;
    }

    @Override
    protected void setPreviewItems() {
        getMenu().getSlot(1).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> {
            player.closeInventory();
            new CountryMenu(player, country.getContinent());
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setItem(entry.getValue().item);
        }

        getMenu().getSlot(4)
                .setItem(new ItemBuilder(Material.valueOf(PlotSystem.getPlugin().getConfig().getString(ConfigPaths.NAVIGATOR_ITEM)), 1)
                        .setName("§6§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.NAVIGATOR)).setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.NAVIGATOR)).build())
                        .build());

        // Set loading item for plots difficulty item
        getMenu().getSlot(7).setItem(MenuItems.loadingItem(Material.PLAYER_HEAD, getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set previous page item
        if (hasPreviousPage()) {
            getMenu().getSlot(45).setItem(MenuItems.previousPageItem(getMenuPlayer()));
        }

        // Set next page item
        if (hasNextPage()) {
            getMenu().getSlot(53).setItem(MenuItems.nextPageItem(getMenuPlayer()));
        }

        // difficulty selector
        getMenu().getSlot(7).setItem(CompanionMenu.getDifficultyItem(getMenuPlayer(), selectedPlotDifficulty));
    }

    @Override
    protected void setItemClickEventsAsync() {
        getMenu().getSlot(1).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new CountryMenu(clickPlayer, country.getContinent(), selectedPlotDifficulty);
        });

        // Set click event for navigator item
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand(PlotSystem.getPlugin().getConfig().getString(ConfigPaths.NAVIGATOR_COMMAND));
        });

        // Set click event for previous page item
        getMenu().getSlot(45).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasPreviousPage()) {
                previousPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            }
        });

        // Set click event for next page item
        getMenu().getSlot(53).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasNextPage()) {
                nextPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            }
        });

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> {
            player.closeInventory();
            new CountryMenu(player, country.getContinent());
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setClickHandler(entry.getValue().clickHandler);
        }

        // Set click event for plots difficulty item
        getMenu().getSlot(7).setClickHandler(((clickPlayer, clickInformation) -> {
            selectedPlotDifficulty = (selectedPlotDifficulty == null ?
                    PlotDifficulty.values()[0] : selectedPlotDifficulty.ordinal() != PlotDifficulty.values().length - 1 ?
                    PlotDifficulty.values()[selectedPlotDifficulty.ordinal() + 1] : null);

            getMenu().getSlot(7).setItem(CompanionMenu.getDifficultyItem(getMenuPlayer(), selectedPlotDifficulty));
            clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.DONE_SOUND, 1, 1);

            // reload displayed cities
            reloadMenuAsync();
        }));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(" ").build())
                .pattern("101101101")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("100010001")
                .build();
    }

    @Override
    protected List<?> getSource() {
        if(projects == null) {
            projects = CityProject.getCityProjects(country, true);
        }
        return projects;
    }

    @Override
    protected void setPaginatedMenuItemsAsync(List<?> source) {
        List<CityProject> cities = source.stream().map(l -> (CityProject) l).collect(Collectors.toList());
        int slot = 9;
        for (CityProject city : cities) {
            try {
                getMenu().getSlot(slot).setItem(city.getItem(getMenuPlayer(), selectedPlotDifficulty));
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", e);
                getMenu().getSlot(slot).setItem(MenuItems.errorItem(getMenuPlayer()));
            }
            slot++;
        }
    }

    @Override
    protected void setPaginatedItemClickEventsAsync(List<?> source) {
        List<CityProject> cities = source.stream().map(l -> (CityProject) l).collect(Collectors.toList());
        int slot = 9;
        for(CityProject city : cities) {
            final int _slot = slot;
            getMenu().getSlot(_slot).setClickHandler((clickPlayer, clickInformation) -> {
                if (!getMenu().getSlot(_slot).getItem(clickPlayer).equals(MenuItems.errorItem(getMenuPlayer()))) {
                    try {
                        clickPlayer.closeInventory();
                        Builder builder = Builder.byUUID(clickPlayer.getUniqueId());
                        int cityID = city.getID();

                        PlotDifficulty plotDifficultyForCity = selectedPlotDifficulty != null ? selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(cityID, builder).get();
                        if (plotDifficultyForCity != null && PlotManager.getPlots(cityID, plotDifficultyForCity, Status.unclaimed).size() != 0) {
                            if (selectedPlotDifficulty != null && PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.ENABLE_SCORE_REQUIREMENT) && !PlotManager.hasPlotDifficultyScoreRequirement(builder, selectedPlotDifficulty)) {
                                clickPlayer.sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(clickPlayer, LangPaths.Message.Error.PLAYER_NEEDS_HIGHER_SCORE)));
                                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                                return;
                            }

                            new DefaultPlotGenerator(cityID, plotDifficultyForCity, builder);
                        } else {
                            clickPlayer.sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(clickPlayer, LangPaths.Message.Error.NO_PLOTS_LEFT)));
                            clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                        }
                    } catch (SQLException | ExecutionException | InterruptedException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        clickPlayer.sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(clickPlayer, LangPaths.Message.Error.ERROR_OCCURRED)));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                    }
                } else {
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                }
            });
            slot++;
        }
    }
}
