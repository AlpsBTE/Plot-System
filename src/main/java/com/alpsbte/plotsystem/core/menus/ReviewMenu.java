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

import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
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
import java.util.stream.Collectors;

public class ReviewMenu extends AbstractMenu {

    private final List<Plot> plots = new ArrayList<>();
    private int plotDisplayCount = 0;

    public ReviewMenu(Player player) throws SQLException {
        super(6, "Manage & Review Plots", player);
    }

    @Override
    protected void setPreviewItems() {
        // Set previous page item
        getMenu().getSlot(46).setItem(MenuItems.previousPageItem());

        // Set close item
        getMenu().getSlot(49).setItem(MenuItems.closeMenuItem());

        // Set next page item
        getMenu().getSlot(52).setItem(MenuItems.nextPageItem());

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Get all unreviewed and unfinished plots
        try {
            plots.addAll(PlotManager.getPlots(Status.unreviewed));
            plots.addAll(PlotManager.getPlots(Status.unfinished));
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }

        // Set unreviewed and unfinished plot items
        plotDisplayCount = Math.min(plots.size(), 45);
        for(int i = 0; i < plotDisplayCount; i++) {
            try {
                Plot plot = plots.get(i);

                List<String> lines = new ArrayList<>();
                lines.add("§7ID: §f" + plot.getID());
                lines.add("");
                lines.add("§7Plot Owner: §f" + plot.getPlotOwner().getName());
                if (!plot.getPlotMembers().isEmpty()) lines.add("§7Members: §f" + plot.getPlotMembers().stream().map(m -> {
                            try {
                                return m.getName();
                            } catch (SQLException ex) {
                                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                            }
                            return "";
                        }).collect(Collectors.joining(", "))
                );
                lines.add("§7City: §f" + plot.getCity().getName());
                lines.add("§7Difficulty: §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase());

                getMenu().getSlot(i).setItem(new ItemBuilder(plot.getStatus() == Status.unfinished ? Material.EMPTY_MAP : Material.MAP, 1)
                        .setName(plot.getStatus() == Status.unfinished ? "§b§lManage Plot" : "§b§lReview Plot")
                        .setLore(lines)
                        .build());
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                getMenu().getSlot(i).setItem(MenuItems.errorItem());
            }
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for unreviewed and unfinished plot items
        for(int i = 0; i < plotDisplayCount; i++) {
            int currentIteration = i;
            getMenu().getSlot(i).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    Plot plot = plots.get(currentIteration);
                    if(plot.getStatus() == Status.unreviewed) {
                        if (!plot.getPlotOwner().getUUID().toString().equals(getMenuPlayer().getUniqueId().toString())){
                            getMenuPlayer().closeInventory();
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

        // Set click event for previous page item
        //getMenu().getSlot(46).setClickHandler((clickPlayer, clickInformation) -> {
            // Not implemented yet
        //});

        // Set click event for close item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.closeInventory());

        // Set click event for next page item
        //getMenu().getSlot(52).setClickHandler((clickPlayer, clickInformation) -> {
            // Not implemented yet
        //});
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("101101101")
                .build();
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(){
        return new ItemBuilder(Material.BOOK, 1)
                .setName("§b§lReview Plots §7(Right Click)")
                .setEnchantment(true)
                .build();
    }
}
