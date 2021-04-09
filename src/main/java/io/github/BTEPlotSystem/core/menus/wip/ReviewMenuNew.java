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

package github.BTEPlotSystem.core.menus.wip;

import github.BTEPlotSystem.core.menus.AbstractMenu;
import github.BTEPlotSystem.core.menus.PlotActionsMenu;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotHandler;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class ReviewMenuNew extends AbstractMenu {

    private final List<Plot> plots = PlotManager.getPlots(Status.unreviewed, Status.unfinished);

    private int plotDisplayCount = 0;

    public ReviewMenuNew(Player player) throws SQLException {
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
        // Add plot items
        plotDisplayCount = Math.min(plots.size(), 40);
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
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.WOOL, 1, (byte) 1)
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
                getMenu().getSlot(i).setItem(errorItem());
            }
        }

        // Add previous page button
        getMenu().getSlot(42).setItem(previousPageItem());

        // Add close menu button
        getMenu().getSlot(45).setItem(closeMenuItem());

        // Add next page button
        getMenu().getSlot(48).setItem(nextPageItem());
    }

    @Override
    protected void setItemClickEvents() {
        // Set click event for unreviewed & unfinished plots
        for(int i = 0; i < plotDisplayCount; i++) {
            try {
                Plot plot = plots.get(i);
                if(plot.getStatus() == Status.unreviewed) {
                    if (!plot.getBuilder().getUUID().toString().equals(getMenuPlayer().getUniqueId().toString())){
                        PlotHandler.teleportPlayer(plot, getMenuPlayer());
                    } else {
                        getMenuPlayer().sendMessage(Utils.getErrorMessageFormat("You cannot review your own builds!"));
                    }
                } else {
                    new PlotActionsMenu(getMenuPlayer(), plot);
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }

        // Set click event for previous page button
        getMenu().getSlot(42).setClickHandler((clickPlayer, clickInformation) -> {
            // Not implemented yet
        });

        // Set click event for close menu button
        getMenu().getSlot(45).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.closeInventory());

        // Set click event for next page button
        getMenu().getSlot(48).setClickHandler((clickPlayer, clickInformation) -> {
            // Not implemented yet
        });
    }
}
