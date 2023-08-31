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

package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Category;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
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
        super(6, LangUtil.getInstance().get(menuPlayer.getPlayer(), LangPaths.MenuTitle.PLAYER_PLOTS, builder.getName() + "'"), menuPlayer);
        this.builder = builder;
    }

    @Override
    protected void setPreviewItems() {
        // Set loading item for player head item
        getMenu().getSlot(4).setItem(MenuItems.loadingItem(Material.PLAYER_HEAD, getMenuPlayer()));

        // Set back item
        getMenu().getSlot(49).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Load player head
        ItemStack playerHead = AlpsUtils.getPlayerHead(builder.getUUID());

        // Set player stats item
        try {
            getMenu().getSlot(4)
                    .setItem(new ItemBuilder(playerHead)
                            .setName("§6§l" + builder.getName()).setLore(new LoreBuilder()
                                    .addLines(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.SCORE) + ": §f" + builder.getScore(),
                                            "§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.COMPLETED_PLOTS) + ": §f" + builder.getCompletedBuilds())
                                    .build())
                            .build());
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            getMenu().getSlot(4).setItem(MenuItems.errorItem(getMenuPlayer()));
        }

        // Set player plot items
        try {
            plots = PlotManager.getPlots(builder);

            plotDisplayCount = Math.min(plots.size(), 36);
            for (int i = 0; i < plotDisplayCount; i++) {
                Plot plot = plots.get(i);
                try {
                    ItemStack item = plot.getStatus() == Status.unfinished ? new ItemStack(Material.ORANGE_WOOL, 1) :
                            plot.getStatus() == Status.unreviewed ? new ItemStack(Material.FILLED_MAP) : new ItemStack(Material.GREEN_WOOL, 1);

                    getMenu().getSlot(9 + i)
                            .setItem(new ItemBuilder(item)
                                    .setName("§b§l" + plot.getCity().getName() + " | " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.PLOT_NAME) + " #" + plot.getID())
                                    .setLore(getDescription(plot, getMenuPlayer()))
                                    .build());
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    getMenu().getSlot(9 + i).setItem(MenuItems.errorItem(getMenuPlayer()));
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
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(" ").build())
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
     * @param p player instance for language system
     * @return Description lore for plot item
     * @throws SQLException When querying database
     */
    private List<String> getDescription(Plot plot, Player p) throws SQLException {
        List<String> lines = new ArrayList<>();
        if (plot.getPlotMembers().size() == 0) {
            // Plot is single player plot
            lines.add("§7" + LangUtil.getInstance().get(p, LangPaths.Plot.TOTAL_SCORE) + ": §6" + (plot.getTotalScore() == -1 ? 0 : plot.getTotalScore()));
        } else {
            // Plot is multiplayer plot
            lines.add("§7" + LangUtil.getInstance().get(p, LangPaths.Plot.OWNER) + ": §a" + plot.getPlotOwner().getName());
            lines.add("");

            int score = (plot.getTotalScore() == -1 ? 0 : plot.getTotalScore());
            lines.add("§7" + LangUtil.getInstance().get(p, LangPaths.Plot.TOTAL_SCORE) + ": §f" + score + " §8" + LangUtil.getInstance().get(p, LangPaths.Plot.GroupSystem.SHARED_BY_MEMBERS, Integer.toString(plot.getPlotMembers().size() + 1)));
            lines.add("§7" + LangUtil.getInstance().get(p, LangPaths.Plot.EFFECTIVE_SCORE) + ": §6" + (plot.getSharedScore() == -1 ? 0 : plot.getSharedScore()));
        }

        if (plot.isReviewed() || plot.isRejected()) {
            lines.add("");
            lines.add("§7" + LangUtil.getInstance().get(p, LangPaths.Review.Criteria.ACCURACY) + ": " + Utils.ChatUtils.getColorByPoints(plot.getReview().getRating(Category.ACCURACY)) + "§8/§a5");
            lines.add("§7" + LangUtil.getInstance().get(p, LangPaths.Review.Criteria.BLOCK_PALETTE) + ": " + Utils.ChatUtils.getColorByPoints(plot.getReview().getRating(Category.BLOCKPALETTE)) + "§8/§a5");
            lines.add("§7" + LangUtil.getInstance().get(p, LangPaths.Review.Criteria.DETAILING) + ": " + Utils.ChatUtils.getColorByPoints(plot.getReview().getRating(Category.DETAILING)) + "§8/§a5");
            lines.add("§7" + LangUtil.getInstance().get(p, LangPaths.Review.Criteria.TECHNIQUE) + ": " + Utils.ChatUtils.getColorByPoints(plot.getReview().getRating(Category.TECHNIQUE)) + "§8/§a5");
            lines.add("");
            lines.add("§7" + LangUtil.getInstance().get(p, LangPaths.Review.FEEDBACK) + ":");

            String[] splitText = plot.getReview().getFeedback().split("//");
            for(String line : splitText) {
                lines.add("§f" + line.replace("//", ""));
            }
        }
        lines.add("");
        if (plot.isReviewed() && plot.isRejected()) lines.add("§c§l" + LangUtil.getInstance().get(p, LangPaths.Review.REJECTED));
        lines.add("§6§l" + LangUtil.getInstance().get(p, LangPaths.Plot.STATUS) + ": §7§l" + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1));
        return lines;
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player p) {
        return new ItemBuilder(AlpsUtils.getItemHead(Utils.HeadUtils.WHITE_P_HEAD))
                .setName("§b§l" + LangUtil.getInstance().get(p, LangPaths.MenuTitle.SHOW_PLOTS))
                .setLore(new LoreBuilder()
                    .addLine(LangUtil.getInstance().get(p, LangPaths.MenuDescription.SHOW_PLOTS)).build())
                .build();
    }
}
