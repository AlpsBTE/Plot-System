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

package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.*;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.enums.Continent;
import com.alpsbte.plotsystem.utils.io.config.ConfigPaths;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.utils.items.builder.LoreBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;

public class CompanionMenu extends AbstractMenu {

    private Plot[] slots;
    private List<CityProject> cityProjects;
    private List<Country> countryProjects;

    private enum ActiveMenu {
        COUNTRY,
        CITY
    }

    private ActiveMenu activeMenu = ActiveMenu.COUNTRY;

    private final Continent selectedContinent;
    private Country selectedCountry = null;

    private PlotDifficulty selectedPlotDifficulty = null;

    CompanionMenu(Player player, Continent continent) {
        super(6, LangUtil.get(player, LangPaths.MenuTitle.COMPANION), player);
        selectedContinent = continent;
    }

    @Override
    protected void setPreviewItems() {
        // Set navigator item
        getMenu().getSlot(4)
                .setItem(new ItemBuilder(Material.valueOf(PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.NAVIGATOR_ITEM)), 1)
                        .setName("§6§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.NAVIGATOR)).setLore(new LoreBuilder()
                                .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.NAVIGATOR)).build())
                        .build());

        // Set loading item for plots difficulty item
        getMenu().getSlot(7).setItem(MenuItems.loadingItem(Material.SKULL_ITEM, (byte) 3, getMenuPlayer()));

        for (Map.Entry<Integer, FooterItem> entry : getFooterItems(45, getMenuPlayer(), player -> {
            player.closeInventory();
            new CompanionMenu(player, selectedContinent);
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setItem(entry.getValue().item);
        }

        super.setPreviewItems();
    }

    public static boolean hasContinentView() {
        return Arrays.stream(Continent.values()).map(continent -> Country.getCountries(continent).size()).filter(count -> count > 0).count() > 1;
    }

    /**
     * Determine what menu to open for the player
     * @param player player to open the menu for
     */
    public static void open(Player player) {
        if(hasContinentView()) {
            new ContinentMenu(player);
        } else {
            Optional<Continent> continent = Arrays.stream(Continent.values()).filter(c -> Country.getCountries(c).size()>0).findFirst();

            if(!continent.isPresent()) {
                player.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(player, LangPaths.Message.Error.NO_CONTINENTS_AVAILABLE)));
                return;
            }

            new CompanionMenu(player, continent.get());
        }
    }

    static class FooterItem {
        public ItemStack item;
        public org.ipvp.canvas.slot.Slot.ClickHandler clickHandler = null;

        FooterItem(ItemStack item, org.ipvp.canvas.slot.Slot.ClickHandler clickHandler) {
            this.item = item;
            this.clickHandler = clickHandler;
        }

        FooterItem(ItemStack item) {
            this.item = item;
        }
    }

    /**
     * Get common footer items between all companion menus
     *
     * @param startingSlot slot to start drawing items at
     * @param player player that is viewing this (translation purposes)
     * @param returnToMenu a lambda to call when needing to return to current menu
     * @return FooterItems indexed by slot number
     */
    public static HashMap<Integer, FooterItem> getFooterItems(int startingSlot, Player player, Consumer<Player> returnToMenu) {
        HashMap<Integer, FooterItem> items = new HashMap<>();
        // Set builder utilities menu item
        items.put(startingSlot + 5, new FooterItem(BuilderUtilitiesMenu.getMenuItem(player), (clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new BuilderUtilitiesMenu(clickPlayer);
        }));

        // Set player plots menu item
        items.put(startingSlot + 6, new FooterItem(PlayerPlotsMenu.getMenuItem(player), (clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("plots " + clickPlayer.getName());
        }));

        // Set player settings menu item
        items.put(startingSlot + 7, new FooterItem(new ItemBuilder(Material.REDSTONE_COMPARATOR)
                .setName("§b§l" + LangUtil.get(player, LangPaths.MenuTitle.SETTINGS))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.get(player, LangPaths.MenuDescription.SETTINGS)).build())
                .build(), (clickPlayer, clickInformation) -> new SettingsMenu(clickPlayer, returnToMenu)));

        for (int i = 0; i < 3; i++) {
            try {
                Builder builder = new Builder(player.getUniqueId());

                final int i_ = i;

                items.put(startingSlot + 1 + i, new FooterItem(builder.getPlotMenuItem(Slot.values()[i], player), (clickPlayer, clickInformation) -> {
                    clickPlayer.closeInventory();
                    try {
                        new PlotActionsMenu(clickPlayer, builder.getPlot(Slot.values()[i_]));
                    } catch (SQLException ex) {
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(clickPlayer, LangPaths.Message.Error.ERROR_OCCURRED)));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        Bukkit.getLogger().log(Level.SEVERE, "An error occurred while opening the plot actions menu!", ex);
                    }
                }));
            } catch (NullPointerException | SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while placing player slot items!", ex);
                items.put(startingSlot + 1 + i, new FooterItem(MenuItems.errorItem(player)));
            }
        }

        return items;
    }

    private void changeMenu(ActiveMenu activeMenu) {
        this.activeMenu = activeMenu;

        for (int i = 9; i < 44; i++) {
            getMenu().getSlot(i).setItem(null);
        }

        reloadMenuAsync();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plots difficulty item head
        getMenu().getSlot(7).setItem(getSelectedDifficultyItem());

        // Set city project items
        try {
            switch (activeMenu) {
                case COUNTRY:
                    countryProjects = Country.getCountries(selectedContinent);
                    setCountryItems();
                    break;
                case CITY:
                    cityProjects = CityProject.getCityProjects(selectedCountry, true);
                    setCityProjectItems();
                    break;
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    protected void setItemClickEvents() {
        // Set click event for navigator item
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand(PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.NAVIGATOR_COMMAND));
        });

        // Set click event for plots difficulty item
        getMenu().getSlot(7).setClickHandler(((clickPlayer, clickInformation) -> {
            selectedPlotDifficulty = (selectedPlotDifficulty == null ?
                    PlotDifficulty.values()[0] : selectedPlotDifficulty.ordinal() != PlotDifficulty.values().length - 1 ?
                    PlotDifficulty.values()[selectedPlotDifficulty.ordinal() + 1] : null);

            getMenu().getSlot(7).setItem(getSelectedDifficultyItem());
            clickPlayer.playSound(clickPlayer.getLocation(), Utils.Done, 1, 1);

            try {
//                setCityProjectItems();
                setCountryItems();
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }));

        switch (activeMenu) {
            case COUNTRY:
                int startingSlot = 9;
                if(hasContinentView()) {
                    startingSlot++;
                    getMenu().getSlot(9).setClickHandler((clickPlayer, clickInformation) -> {
                        clickPlayer.closeInventory();
                        new ContinentMenu(clickPlayer);
                    });
                }

                for (Country country : countryProjects) {
                    int i = countryProjects.indexOf(country);
                    getMenu().getSlot(startingSlot + i).setClickHandler((clickPlayer, clickInformation) -> {
//                        selectedCountry = country;
//                        changeMenu(ActiveMenu.CITY);
                        clickPlayer.closeInventory();
                        new CityProjectMenu(clickPlayer, country);
                    });
                }
                break;
            case CITY:
                getMenu().getSlot(9).setClickHandler((clickPlayer, clickInformation) -> {
                    selectedCountry = null;
                    changeMenu(ActiveMenu.COUNTRY);
                });

                // Set click event for city project items
                for (int i = 0; i < cityProjects.size(); i++) {
                    int itemSlot = i;
                    getMenu().getSlot(10 + i).setClickHandler((clickPlayer, clickInformation) -> {
                        if (!getMenu().getSlot(10 + itemSlot).getItem(clickPlayer).equals(MenuItems.errorItem(getMenuPlayer()))) {
                            try {
                                clickPlayer.closeInventory();
                                Builder builder = new Builder(clickPlayer.getUniqueId());
                                int cityID = cityProjects.get(itemSlot).getID();

                                PlotDifficulty plotDifficultyForCity = selectedPlotDifficulty != null ? selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(cityID, builder).get();
                                if (PlotManager.getPlots(cityID, plotDifficultyForCity, Status.unclaimed).size() != 0) {
                                    if (selectedPlotDifficulty != null && PlotSystem.getPlugin().getConfigManager().getConfig().getBoolean(ConfigPaths.ENABLE_SCORE_REQUIREMENT) && !PlotManager.hasPlotDifficultyScoreRequirement(builder, selectedPlotDifficulty)) {
                                        clickPlayer.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(clickPlayer, LangPaths.Message.Error.PLAYER_NEEDS_HIGHER_SCORE)));
                                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                                        return;
                                    }

                                    new DefaultPlotGenerator(cityID, plotDifficultyForCity, builder);
                                } else {
                                    clickPlayer.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(clickPlayer, LangPaths.Message.Error.NO_PLOTS_LEFT)));
                                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                                }
                            } catch (SQLException | ExecutionException | InterruptedException ex) {
                                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                                clickPlayer.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(clickPlayer, LangPaths.Message.Error.ERROR_OCCURRED)));
                                clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                            }
                        } else {
                            clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        }
                    });
                }
                break;
        }

        for (Map.Entry<Integer, FooterItem> entry : getFooterItems(45, getMenuPlayer(), player -> {
            player.closeInventory();
            new CompanionMenu(player, selectedContinent);
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setClickHandler(entry.getValue().clickHandler);
        }
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("100010001")
                .build();
    }

    private void setCountryItems() throws SQLException {
        int startingSlot = 9;
        if(hasContinentView()) {
            startingSlot++;
            getMenu().getSlot(9).setItem(new ItemBuilder(Utils.getItemHead(new Utils.CustomHead("9219")))
                    .setName("§b§lBack")
                    .build());
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
                                    "§6" + cities.size() + " §7" + LangUtil.get(getMenuPlayer(), LangPaths.CityProject.CITIES),
                                    "",
                                    "§6" + plotsOpen + " §7" + LangUtil.get(getMenuPlayer(), LangPaths.CityProject.PROJECT_OPEN),
                                    "§8---------------------",
                                    "§6" + plotsInProgress + " §7" + LangUtil.get(getMenuPlayer(), LangPaths.CityProject.PROJECT_IN_PROGRESS),
                                    "§6" + plotsCompleted + " §7" + LangUtil.get(getMenuPlayer(), LangPaths.CityProject.PROJECT_COMPLETED),
                                    "",
                                    (plotsUnclaimed > 0 ? "§a§lPlots Available!" : "§f§lNo Plots Available")
                            )
                            .build())
                    .build());
        }
    }

    /**
     * Sets city project items asynchronously in the menu
     *
     * @throws SQLException When querying database
     */
    private void setCityProjectItems() throws SQLException {
        getMenu().getSlot(9).setItem(new ItemBuilder(Utils.getItemHead(new Utils.CustomHead("9219")))
                .setName("§b§lBack")
                .build());

        for (int i = 0; i < cityProjects.size(); i++) {
            if (i <= 28) {
                CityProject cp = cityProjects.get(i);
                ItemStack cpItem = cp.getCountry().getHead();
                try {
                    PlotDifficulty cpPlotDifficulty = selectedPlotDifficulty != null ?
                            selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(cp.getID(), new Builder(getMenuPlayer().getUniqueId())).get();

                    int plotsOpen = PlotManager.getPlots(cp.getID(), Status.unclaimed).size();
                    int plotsInProgress = PlotManager.getPlots(cp.getID(), Status.unfinished, Status.unreviewed).size();
                    int plotsCompleted = PlotManager.getPlots(cp.getID(), Status.completed).size();
                    int plotsUnclaimed = PlotManager.getPlots(cp.getID(), cpPlotDifficulty, Status.unclaimed).size();

                    getMenu().getSlot(10 + cityProjects.indexOf(cp)).setItem(new ItemBuilder(cpItem)
                            .setName("§b§l" + cp.getName())
                            .setLore(new LoreBuilder()
                                    .addLines(cp.getDescription(),
                                            "",
                                            "§6" + plotsOpen + " §7" + LangUtil.get(getMenuPlayer(), LangPaths.CityProject.PROJECT_OPEN),
                                            "§8---------------------",
                                            "§6" + plotsInProgress + " §7" + LangUtil.get(getMenuPlayer(), LangPaths.CityProject.PROJECT_IN_PROGRESS),
                                            "§6" + plotsCompleted + " §7" + LangUtil.get(getMenuPlayer(), LangPaths.CityProject.PROJECT_COMPLETED),
                                            "",
                                            plotsUnclaimed != 0 ? Utils.getFormattedDifficulty(cpPlotDifficulty) : "§f§l" + LangUtil.get(getMenuPlayer(), LangPaths.CityProject.PROJECT_NO_PLOTS)
                                    ).build())
                            .build());

                } catch (SQLException | ExecutionException | InterruptedException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    getMenu().getSlot(9 + cityProjects.indexOf(cp)).setItem(MenuItems.errorItem(getMenuPlayer()));
                }
            }
        }
    }

    /**
     * Returns head for current selected plot difficulty
     *
     * @return plots difficulty head as ItemStack
     */
    private ItemStack getSelectedDifficultyItem() {
        ItemStack item = Utils.getItemHead(Utils.CustomHead.WHITE_CONCRETE);

        if (selectedPlotDifficulty != null) {
            if (selectedPlotDifficulty == PlotDifficulty.EASY) {
                item = Utils.getItemHead(Utils.CustomHead.GREEN_CONCRETE);
            } else if (selectedPlotDifficulty == PlotDifficulty.MEDIUM) {
                item = Utils.getItemHead(Utils.CustomHead.YELLOW_CONCRETE);
            } else if (selectedPlotDifficulty == PlotDifficulty.HARD) {
                item = Utils.getItemHead(Utils.CustomHead.RED_CONCRETE);
            }
        }

        try {
            return new ItemBuilder(item)
                    .setName("§b§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.PLOT_DIFFICULTY).toUpperCase())
                    .setLore(new LoreBuilder()
                            .addLines("",
                                    selectedPlotDifficulty != null ? Utils.getFormattedDifficulty(selectedPlotDifficulty) : "§f§l" + LangUtil.get(getMenuPlayer(), LangPaths.Difficulty.AUTOMATIC),
                                    selectedPlotDifficulty != null ? "§7" + LangUtil.get(getMenuPlayer(), LangPaths.Difficulty.SCORE_MULTIPLIER) + ": §fx" + PlotManager.getMultiplierByDifficulty(selectedPlotDifficulty) : "",
                                    "",
                                    "§7" + LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.PLOT_DIFFICULTY))
                            .build())
                    .build();
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return MenuItems.errorItem(getMenuPlayer());
        }
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(Material.NETHER_STAR, 1)
                .setName("§b§l" + LangUtil.get(player, LangPaths.MenuTitle.COMPANION) + " §7(" + LangUtil.get(player, LangPaths.Note.Action.RIGHT_CLICK) + ")")
                .setEnchantment(true)
                .build();
    }
}