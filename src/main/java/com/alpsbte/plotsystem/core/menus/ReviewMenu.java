/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021-2022, Alps BTE <bte.atchli@gmail.com>
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
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
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

public class ReviewMenu extends AbstractPaginatedMenu {

    public ReviewMenu(Player player) throws SQLException {
        super(6, 5, LangUtil.get(player, LangPaths.Review.MANAGE_AND_REVIEW_PLOTS), player);
    }

    @Override
    protected List<?> getSource() {
        List<Plot> plots = new ArrayList<>();
        try {
            plots.addAll(PlotManager.getPlots(Status.unreviewed));
            plots.addAll(PlotManager.getPlots(Status.unfinished));
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
        return plots;
    }

    @Override
    protected void setPreviewItems() {
        // Set close item
        getMenu().getSlot(49).setItem(MenuItems.closeMenuItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setPaginatedMenuItemsAsync(List<?> source) {
        // Set unreviewed and unfinished plot items
        List<Plot> plots = source.stream().map(p -> (Plot) p).collect(Collectors.toList());
        int index = 0;
        for(Plot plot : plots) {
            try {
                List<String> lines = new ArrayList<>();
                lines.add("§7" + LangUtil.get(getMenuPlayer(), LangPaths.Plot.ID) + ": §f" + plot.getID());
                lines.add("");
                lines.add("§7" + LangUtil.get(getMenuPlayer(), LangPaths.Plot.OWNER) + ": §f" + plot.getPlotOwner().getName());
                if (!plot.getPlotMembers().isEmpty()) lines.add("§7" + LangUtil.get(getMenuPlayer(), LangPaths.Plot.MEMBERS) + ": §f" + plot.getPlotMembers().stream().map(m -> {
                            try { return m.getName(); } catch (SQLException ex) { Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex); }
                            return "";
                        }).collect(Collectors.joining(", "))
                );
                lines.add("§7" + LangUtil.get(getMenuPlayer(), LangPaths.Plot.CITY) + ": §f" + plot.getCity().getName());
                lines.add("§7" + LangUtil.get(getMenuPlayer(), LangPaths.Plot.DIFFICULTY) + ": §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase());

                getMenu().getSlot(index).setItem(new ItemBuilder(plot.getStatus() == Status.unfinished ? Material.EMPTY_MAP : Material.MAP, 1)
                        .setName(LangUtil.get(getMenuPlayer(), plot.getStatus() == Status.unfinished ? LangPaths.Review.MANAGE_PLOT : LangPaths.Review.REVIEW_PLOT))
                        .setLore(lines)
                        .build());
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                getMenu().getSlot(index).setItem(MenuItems.errorItem(getMenuPlayer()));
            }
            index++;
        }
    }

    @Override
    protected void setPaginatedItemClickEventsAsync(List<?> source) {
        // Set click event for unreviewed and unfinished plot items
        List<Plot> plots = source.stream().map(p -> (Plot) p).collect(Collectors.toList());
        int index = 0;
        for (Plot plot : plots) {
            getMenu().getSlot(index).setClickHandler((player, info) -> {
                try {
                    getMenuPlayer().closeInventory();
                    if (plot.getStatus() == Status.unreviewed) {
                        if (!plot.getPlotOwner().getUUID().toString().equals(getMenuPlayer().getUniqueId().toString())) {
                            plot.getWorld().teleportPlayer(getMenuPlayer());
                        } else {
                            getMenuPlayer().sendMessage(Utils.getErrorMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Error.CANNOT_REVIEW_OWN_PLOT)));
                        }
                    } else {
                        new PlotActionsMenu(getMenuPlayer(), plot);
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                }
            });
            index++;
        }
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set previous page item
        if (hasPreviousPage()) getMenu().getSlot(46).setItem(MenuItems.previousPageItem(getMenuPlayer()));

        // Set next page item
        if (hasNextPage()) getMenu().getSlot(52).setItem(MenuItems.nextPageItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for previous page item
        getMenu().getSlot(46).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasPreviousPage()) {
                previousPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.INVENTORY_CLICK, 1, 1);
            }
        });

        // Set click event for close item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.closeInventory());

        // Set click event for next page item
        getMenu().getSlot(52).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasNextPage()) {
                nextPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.INVENTORY_CLICK, 1, 1);
            }
        });
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
                .pattern("111101111")
                .build();
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(Material.BOOK, 1)
                .setName("§b§l" + LangUtil.get(player, LangPaths.MenuTitle.REVIEW_PLOTS) + " §7(" + LangUtil.get(player, LangPaths.Note.Action.RIGHT_CLICK) + ")")
                .setEnchantment(true)
                .build();
    }
}
