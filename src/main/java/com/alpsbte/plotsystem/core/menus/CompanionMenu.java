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

package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.config.ConfigPaths;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.enums.Status;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class CompanionMenu extends AbstractMenu {

    private Plot[] slots;
    private List<CityProject> cityProjects;

    private PlotDifficulty selectedPlotDifficulty = null;

    public CompanionMenu(Player player) {
        super(6, "Companion", player);
    }

    @Override
    protected void setPreviewItems() {
        // Set navigator item
        getMenu().getSlot(4)
                .setItem(new ItemBuilder(Material.valueOf(PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.NAVIGATOR_ITEM)), 1)
                        .setName("§6§l"+ PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.NAVIGATOR_NAME)).setLore(new LoreBuilder()
                                .addLine(PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.NAVIGATOR_DESCRIPTION)).build())
                        .build());

        // Set loading item for plots difficulty item
        getMenu().getSlot(7).setItem(MenuItems.loadingItem(Material.SKULL_ITEM, (byte) 3));

        // Set builder utilities menu item
        getMenu().getSlot(50).setItem(BuilderUtilitiesMenu.getMenuItem());

        // Set player plots menu item
        getMenu().getSlot(51).setItem(PlayerPlotsMenu.getMenuItem());

        // Set player settings menu item
        getMenu().getSlot(52)
                .setItem(new ItemBuilder(Material.REDSTONE_COMPARATOR)
                        .setName("§b§lSettings")
                        .setLore(new LoreBuilder()
                                .addLine("Modify your user settings.").build())
                        .build());

        // Set players slot items
        slots = new Plot[3];
        for (int i = 0; i < 3; i++) {
            try {
                Builder builder = new Builder(getMenuPlayer().getUniqueId());
                slots[i] = builder.getPlot(Slot.values()[i]);

                if (slots[i] != null) {
                    getMenu().getSlot(46 + i).setItem(new ItemBuilder(Material.MAP, 1 + i)
                            .setName("§b§lSLOT " + (i + 1))
                            .setLore(new LoreBuilder()
                                    .addLines("§7ID: §f" + slots[i].getID(),
                                            "§7City: §f" + slots[i].getCity().getName(),
                                            "§7Difficulty: §f" + slots[i].getDifficulty().name().charAt(0) + slots[i].getDifficulty().name().substring(1).toLowerCase(),
                                            "",
                                            "§6§lStatus: §7§l" + slots[i].getStatus().name().substring(0, 1).toUpperCase() + slots[i].getStatus().name().substring(1)
                                    ).build())
                            .build());
                } else {
                    getMenu().getSlot(46 + i).setItem(new ItemBuilder(Material.EMPTY_MAP, 1 + i)
                            .setName("§b§lSLOT " + (i + 1))
                            .setLore(new LoreBuilder()
                                    .addLines("§7Click on a city project to create a new plot.",
                                            "",
                                            "§6§lStatus: §7§lUnassigned")
                                    .build())
                            .build());
                }
            } catch (NullPointerException | SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while placing player slot items!", ex);
                getMenu().getSlot(46 + i).setItem(MenuItems.errorItem());
            }
        }

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plots difficulty item head
        getMenu().getSlot(7).setItem(getSelectedDifficultyItem());

        // Set city project items
        try {
            cityProjects = CityProject.getCityProjects(true);
            setCityProjectItems();
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
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
                setCityProjectItems();
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }));

        // Set click event for city project items
        for(int i = 0; i < cityProjects.size(); i++) {
            int itemSlot = i;
            getMenu().getSlot(9 + i).setClickHandler((clickPlayer, clickInformation) -> {
                if (!getMenu().getSlot(9 + itemSlot).getItem(clickPlayer).equals(MenuItems.errorItem())) {
                    try {
                        clickPlayer.closeInventory();
                        Builder builder = new Builder(clickPlayer.getUniqueId());
                        int cityID = cityProjects.get(itemSlot).getID();

                        PlotDifficulty plotDifficultyForCity = selectedPlotDifficulty != null ? selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(cityID, builder).get();
                        if (PlotManager.getPlots(cityID, plotDifficultyForCity, Status.unclaimed).size() != 0) {
                            if (selectedPlotDifficulty != null && PlotSystem.getPlugin().getConfigManager().getConfig().getBoolean(ConfigPaths.ENABLE_SCORE_REQUIREMENT) && !PlotManager.hasPlotDifficultyScoreRequirement(builder, selectedPlotDifficulty)) {
                                clickPlayer.sendMessage(Utils.getErrorMessageFormat("You need a higher score to build in this difficulty level."));
                                clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                                return;
                            }

                            new DefaultPlotGenerator(cityID, plotDifficultyForCity, builder);
                        } else {
                            clickPlayer.sendMessage(Utils.getErrorMessageFormat("This city project doesn't have any more plots left. Please select another project."));
                            clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        }
                    } catch (SQLException | ExecutionException | InterruptedException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                    }
                } else {
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                }
            });
        }

        // Set click event for player slot items
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            for(int i = 0; i < 3; i++) {
                if (slots[i] != null) {
                    int itemSlot = i;
                    getMenu().getSlot(46 + i).setClickHandler((clickPlayer, clickInformation) -> {
                        clickPlayer.closeInventory();
                        try {
                            new PlotActionsMenu(clickPlayer, slots[itemSlot]);
                        } catch (SQLException ex) {
                            clickPlayer.sendMessage(Utils.getErrorMessageFormat("An internal error occurred! Please try again or contact a developer!"));
                            clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while opening the plot actions menu!", ex);
                        }
                    });
                }
            }
        });

        // Set click event for builder utilities menu item
        getMenu().getSlot(50).setClickHandler(((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new BuilderUtilitiesMenu(clickPlayer);
        }));

        // Set click event for player plots menu item
        getMenu().getSlot(51).setClickHandler(((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("plots " + clickPlayer.getName());
        }));

        // Set click event for player settings menu item
        getMenu().getSlot(52).setClickHandler(((clickPlayer, clickInformation) -> clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1)));
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

    /**
     * Sets city project items asynchronously in the menu
     * @throws SQLException When querying database
     */
    private void setCityProjectItems() throws SQLException {
        for(int i = 0; i < cityProjects.size(); i++) {
            if(i <= 28) {
                CityProject cp = cityProjects.get(i);
                ItemStack cpItem = cp.getCountry().getHead();
                try {
                    PlotDifficulty cpPlotDifficulty = selectedPlotDifficulty != null ?
                            selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(cp.getID(), new Builder(getMenuPlayer().getUniqueId())).get();

                    int plotsOpen = PlotManager.getPlots(cp.getID(), Status.unclaimed).size();
                    int plotsInProgress = PlotManager.getPlots(cp.getID(), Status.unfinished, Status.unreviewed).size();
                    int plotsCompleted = PlotManager.getPlots(cp.getID(), Status.completed).size();
                    int plotsUnclaimed = PlotManager.getPlots(cp.getID(), cpPlotDifficulty, Status.unclaimed).size();

                    getMenu().getSlot(9 + cityProjects.indexOf(cp)).setItem(new ItemBuilder(cpItem)
                            .setName("§b§l" + cp.getName())
                            .setLore(new LoreBuilder()
                                    .addLines(cp.getDescription(),
                                            "",
                                            "§6" + plotsOpen + " §7Plots Open",
                                            "§8---------------------",
                                            "§6" + plotsInProgress + " §7Plots In Progress",
                                            "§6" + plotsCompleted + " §7Plots Completed",
                                            "",
                                            plotsUnclaimed != 0 ? Utils.getFormattedDifficulty(cpPlotDifficulty) : "§f§lNo Plots Available"
                                    ).build())
                            .build());

                } catch (SQLException | ExecutionException | InterruptedException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    getMenu().getSlot(9 + cityProjects.indexOf(cp)).setItem(MenuItems.errorItem());
                }
            }
        }
    }

    /**
     * Returns head for current selected plot difficulty
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
                    .setName("§b§lPLOT DIFFICULTY")
                    .setLore(new LoreBuilder()
                            .addLines("",
                                    selectedPlotDifficulty != null ? Utils.getFormattedDifficulty(selectedPlotDifficulty) : "§f§lAutomatic",
                                    selectedPlotDifficulty != null ? "§7Score Multiplier: §fx" + PlotManager.getMultiplierByDifficulty(selectedPlotDifficulty) : "",
                                    "",
                                    "§7Click to Switch...")
                            .build())
                    .build();
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return MenuItems.errorItem();
        }
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem() {
        return new ItemBuilder(Material.NETHER_STAR, 1)
                .setName("§b§lCompanion §7(Right Click)")
                .setEnchantment(true)
                .build();
    }
}