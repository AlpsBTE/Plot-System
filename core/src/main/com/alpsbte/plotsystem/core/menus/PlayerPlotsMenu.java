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

import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.utils.enums.Status;
import com.alpsbte.plotsystem.core.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.core.utils.items.builder.LoreBuilder;
import com.alpsbte.plotsystem.core.utils.items.MenuItems;
import com.alpsbte.plotsystem.core.utils.Utils;
import com.alpsbte.plotsystem.core.utils.enums.Category;
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

public class PlayerPlotsMenu extends AbstractMenu {

    private final Builder builder;
    private List<Plot> plots;

    private int plotDisplayCount = 0;

    public PlayerPlotsMenu(Player menuPlayer, Builder builder) throws SQLException {
        super(6, builder.getName() + "'s Plots", menuPlayer);
        this.builder = builder;
    }

    @Override
    protected void setPreviewItems() {
        // Set loading item for player head item
        getMenu().getSlot(4).setItem(MenuItems.loadingItem(Material.SKULL_ITEM, (byte) 3));

        // Set back item
        getMenu().getSlot(49).setItem(MenuItems.backMenuItem());

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Load player head
        ItemStack playerHead = Utils.getPlayerHead(builder.getUUID());

        // Set player stats item
        try {
            getMenu().getSlot(4)
                    .setItem(new ItemBuilder(playerHead)
                            .setName("§6§l" + builder.getName()).setLore(new LoreBuilder()
                                    .addLines("Score: §f" + builder.getScore(),
                                            "§7Completed Plots: §f" + builder.getCompletedBuilds())
                                    .build())
                            .build());
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            getMenu().getSlot(4).setItem(MenuItems.errorItem());
        }

        // Set player plot items
        try {
            plots = PlotManager.getPlots(builder);

            plotDisplayCount = Math.min(plots.size(), 36);
            for (int i = 0; i < plotDisplayCount; i++) {
                Plot plot = plots.get(i);
                try {
                    ItemStack item = plot.getStatus() == Status.unfinished ? new ItemStack(Material.WOOL, 1, (byte) 1) :
                            plot.getStatus() == Status.unreviewed ? new ItemStack(Material.MAP) : new ItemStack(Material.WOOL, 1, (byte) 13);

                    getMenu().getSlot(9 + i)
                            .setItem(new ItemBuilder(item)
                                    .setName("§b§l" + plot.getCity().getName() + " | Plot #" + plot.getID())
                                    .setLore(getDescription(plot))
                                    .build());
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    getMenu().getSlot(9 + i).setItem(MenuItems.errorItem());
                }
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Add click event for player plot items
        for(int i = 0; i < plotDisplayCount; i++) {
            int itemSlot = i;
            getMenu().getSlot(9 + i).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    new PlotActionsMenu(clickPlayer, plots.get(itemSlot));
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            });
        }

        // Set click event for back item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("companion");
        });
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
                .pattern("111101111")
                .build();
    }

    /**
     * Returns description for plot item
     * @param plot Plot class
     * @return Description lore for plot item
     * @throws SQLException When querying database
     */
    private List<String> getDescription(Plot plot) throws SQLException {
        List<String> lines = new ArrayList<>();
        if (plot.getPlotMembers().size() == 0) {
            // Plot is single player plot
            lines.add("§7Total Score: §6" + (plot.getTotalScore() == -1 ? 0 : plot.getTotalScore()));
        } else {
            // Plot is multiplayer plot
            lines.add("§7Plot Owner: §a" + plot.getPlotOwner().getName());
            lines.add("");

            int score = (plot.getTotalScore() == -1 ? 0 : plot.getTotalScore());
            lines.add("§7Total Score: §f" + score + " §8(shared by " + (plot.getPlotMembers().size() + 1) + " members)");
            lines.add("§7Effective Score: §6" + (plot.getSharedScore() == -1 ? 0 : plot.getSharedScore()));
        }

        if (plot.isReviewed() || plot.isRejected()) {
            lines.add("");
            lines.add("§7Accuracy: " + Utils.getPointsByColor(plot.getReview().getRating(Category.ACCURACY)) + "§8/§a5");
            lines.add("§7Block Palette: " + Utils.getPointsByColor(plot.getReview().getRating(Category.BLOCKPALETTE)) + "§8/§a5");
            lines.add("§7Detailing: " + Utils.getPointsByColor(plot.getReview().getRating(Category.DETAILING)) + "§8/§a5");
            lines.add("§7Technique: " + Utils.getPointsByColor(plot.getReview().getRating(Category.TECHNIQUE)) + "§8/§a5");
            lines.add("");
            lines.add("§7Feedback:");

            String[] splitText = plot.getReview().getFeedback().split("//");
            for(String line : splitText) {
                lines.add("§f" + line.replace("//", ""));
            }
        }
        lines.add("");
        if (plot.isReviewed() && plot.isRejected()) lines.add("§c§lRejected");
        lines.add("§6§lStatus: §7§l" + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1));
        return lines;
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem() {
        return new ItemBuilder(Utils.getItemHead(Utils.CustomHead.WHITE_P))
                .setName("§b§lShow Plots")
                .setLore(new LoreBuilder()
                        .addLine("Show all your plots.").build())
                .build();
    }
}
