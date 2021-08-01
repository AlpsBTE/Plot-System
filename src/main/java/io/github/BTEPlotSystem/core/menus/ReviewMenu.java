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

package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotHandler;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.items.builder.ItemBuilder;
import github.BTEPlotSystem.utils.items.builder.LoreBuilder;
import github.BTEPlotSystem.utils.items.MenuItems;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ReviewMenu extends AbstractMenu {

    private final List<Plot> plots = new ArrayList<>();

    private int plotDisplayCount = 0;

    public ReviewMenu(Player player) throws SQLException {
        // Opens Review Menu, showing all plots in the given round.
        super(6, "Review & Manage Plots", player);

        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("101101101")
                .build();
        mask.apply(getMenu());

        addMenuItems();
        setItemClickEvents();

        getMenu().open(getMenuPlayer());
    }

    @Override
    protected void addMenuItems() {
        try {
            plots.addAll(PlotManager.getPlots(Status.unreviewed));
            plots.addAll(PlotManager.getPlots(Status.unfinished));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        // Add plot items
        plotDisplayCount = Math.min(plots.size(), 45);
        for(int i = 0; i < plotDisplayCount; i++) {
            try {
                Plot plot = plots.get(i);
                if(plot.getStatus() == Status.unreviewed) {
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.MAP, 1)
                            .setName("§b§lReview Plot")
                            .setLore(new LoreBuilder()
                                    .addLines("ID: §f" + plot.getID(),
                                            "",
                                            "§7Builder: §f" + plot.getBuilder().getName(),
                                            "§7City: §f" + plot.getCity().getName(),
                                            "§7Difficulty: §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase())
                                    .build())
                    .build());
                } else {
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.EMPTY_MAP, 1)
                            .setName("§b§lManage Plot")
                            .setLore(new LoreBuilder()
                                    .addLines("ID: §f" + plot.getID(),
                                              "",
                                              "§7Builder: §f" + plot.getBuilder().getName(),
                                              "§7City: §f" + plot.getCity().getName(),
                                              "§7Difficulty: §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase())
                                    .build())
                    .build());
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                getMenu().getSlot(i).setItem(MenuItems.errorItem());
            }
        }

        // Add previous page button
        getMenu().getSlot(46).setItem(MenuItems.previousPageItem());

        // Add close menu button
        getMenu().getSlot(49).setItem(MenuItems.closeMenuItem());

        // Add next page button
        getMenu().getSlot(52).setItem(MenuItems.nextPageItem());
    }

    @Override
    protected void setItemClickEvents() {
        // Set click event for unreviewed & unfinished plots
        for(int i = 0; i < plotDisplayCount; i++) {
            int currentIteration = i;
            getMenu().getSlot(i).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    Plot plot = plots.get(currentIteration);
                    if(plot.getStatus() == Status.unreviewed) {
                        if (!plot.getBuilder().getUUID().toString().equals(getMenuPlayer().getUniqueId().toString())){
                            PlotHandler.teleportPlayer(plot, getMenuPlayer());
                        } else {
                            getMenuPlayer().sendMessage(Utils.getErrorMessageFormat("You cannot review your own builds!"));
                        }
                    } else {
                        getMenuPlayer().closeInventory();
                        new PlotActionsMenu(getMenuPlayer(), plot);
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            });
        }

        // Set click event for previous page button
        getMenu().getSlot(46).setClickHandler((clickPlayer, clickInformation) -> {
            // Not implemented yet
        });

        // Set click event for close menu button
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.closeInventory());

        // Set click event for next page button
        getMenu().getSlot(52).setClickHandler((clickPlayer, clickInformation) -> {
            // Not implemented yet
        });
    }

    public static ItemStack getMenuItem(){
        return new ItemBuilder(Material.BOOK, 1)
                .setName("§b§lReview Plots §7(Right Click)")
                .setEnchantment(true)
                .build();
    }
}
