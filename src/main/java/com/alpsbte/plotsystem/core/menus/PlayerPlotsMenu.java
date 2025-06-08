/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.alpslib.utils.head.AlpsHeadUtils;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.core.system.review.ReviewRating;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class PlayerPlotsMenu extends AbstractMenu {
    private final Builder builder;
    private List<Plot> plots;

    private int plotDisplayCount = 0;

    public PlayerPlotsMenu(Player menuPlayer, Builder builder) {
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
        // Set player stats item
        getMenu().getSlot(4)
                .setItem(new ItemBuilder(AlpsHeadUtils.getPlayerHead(builder.getUUID()))
                        .setName(text(builder.getName(), GOLD).decoration(BOLD, true))
                        .setLore(new LoreBuilder()
                                .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.SCORE) + ": ", GRAY)
                                        .append(text(builder.getScore(), WHITE)))
                                .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.COMPLETED_PLOTS) + ": ", GRAY)
                                        .append(text(builder.getCompletedBuildsCount(), WHITE)))
                                .build())
                        .build());

        // Set player plot items
        plots = DataProvider.PLOT.getPlots(builder);

        plotDisplayCount = Math.min(plots.size(), 36);
        for (int i = 0; i < plotDisplayCount; i++) {
            Plot plot = plots.get(i);
            ItemStack item = switch (plot.getStatus()) {
                case unfinished -> BaseItems.PLOT_UNFINISHED.getItem();
                case unreviewed -> BaseItems.PLOT_UNREVIEWED.getItem();
                default -> BaseItems.PLOT_COMPLETED.getItem();
            };

            getMenu().getSlot(9 + i)
                    .setItem(new ItemBuilder(item)
                            .setName(text(plot.getCityProject().getName(getMenuPlayer()) + " | " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.PLOT_NAME) + " #" + plot.getID(), AQUA).decoration(BOLD, true))
                            .setLore(getLore(plot, getMenuPlayer()).build())
                            .build());
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Add click event for player plot items
        for (int i = 0; i < plotDisplayCount; i++) {
            int itemSlot = i;
            getMenu().getSlot(9 + i).setClickHandler((clickPlayer, clickInformation) -> {
                if (plots.get(itemSlot).getVersion() <= AbstractPlot.LEGACY_VERSION_THRESHOLD) return;
                new PlotActionsMenu(clickPlayer, plots.get(itemSlot));
            });
        }

        // Set click event for back item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("companion"));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(empty()).build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }

    /**
     * Returns description lore for plot item
     *
     * @param plot Plot class
     * @param p    player instance for language system
     * @return description lore for plot item
     */
    private LoreBuilder getLore(Plot plot, Player p) {
        LoreBuilder builder = new LoreBuilder();

        Optional<PlotReview> review = plot.getLatestReview();
        int score = (review.isEmpty() || plot.isRejected()) ? 0 : review.get().getScore();
        if (plot.getPlotMembers().isEmpty()) {
            // Plot is single player plot
            builder.addLine(text(LangUtil.getInstance().get(p, LangPaths.Plot.TOTAL_SCORE) + ": ", GRAY)
                    .append(text(score, WHITE)));
        } else {
            // Plot is multiplayer plot
            builder.addLine(text(LangUtil.getInstance().get(p, LangPaths.Plot.OWNER) + ": ", GRAY)
                    .append(text(plot.getPlotOwner().getName(), WHITE)));
            builder.emptyLine();

            builder.addLine(text(LangUtil.getInstance().get(p, LangPaths.Plot.TOTAL_SCORE) + ": ", GRAY)
                    .append(text(score + " ", WHITE))
                    .append(text(LangUtil.getInstance().get(p, LangPaths.Plot.GroupSystem.SHARED_BY_MEMBERS,
                            Integer.toString(plot.getPlotMembers().size() + 1)), DARK_GRAY)));
        }

        if (review.isPresent()) {
            ReviewRating rating = review.get().getRating();
            builder.emptyLine();
            builder.addLines(
                    text(LangUtil.getInstance().get(p, LangPaths.Review.Criteria.ACCURACY) + ": ", GRAY)
                            .append(Utils.ItemUtils.getColoredPointsComponent(rating.getAccuracyPoints(), 5))
                            .append(text("/", DARK_GRAY)).append(text("5", GREEN)),
                    text(LangUtil.getInstance().get(p, LangPaths.Review.Criteria.BLOCK_PALETTE) + ": ", GRAY)
                            .append(Utils.ItemUtils.getColoredPointsComponent(rating.getBlockPalettePoints(), 5))
                            .append(text("/", DARK_GRAY)).append(text("5", GREEN))
            );
            if (plot.getVersion() > AbstractPlot.LEGACY_VERSION_THRESHOLD) {
                builder.addLine(text(LangUtil.getInstance().get(p, LangPaths.Review.TOGGLE_POINTS) + ": ", GRAY)
                        .append(Utils.ItemUtils.getColoredPointsComponent(rating.getTogglePoints(), 10))
                        .append(text("/", DARK_GRAY)).append(text("10", GREEN)));
            }
            builder.emptyLine();
            builder.addLine(text(LangUtil.getInstance().get(p, LangPaths.Review.FEEDBACK) + ":", GRAY));
            String feedback = review.get().getFeedback() == null
                    ? LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.NO_FEEDBACK)
                    : review.get().getFeedback().replaceAll("//", " ");
            builder.addLine(text(feedback, WHITE), true);
        }

        builder.emptyLine();
        if (plot.isRejected()) builder.addLine(text(LangUtil.getInstance().get(p, LangPaths.Review.REJECTED), RED, BOLD));
        builder.addLine(text(LangUtil.getInstance().get(p, LangPaths.Plot.STATUS) + ": ", GRAY)
                .append(text(plot.getStatus().name().substring(0, 1).toUpperCase() +
                        plot.getStatus().name().substring(1), WHITE)));

        if (plot.getVersion() <= AbstractPlot.LEGACY_VERSION_THRESHOLD) {
            builder.addLine(text(LangUtil.getInstance().get(p, LangPaths.Note.LEGACY), RED).decoration(BOLD, true));
        }
        return builder;
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player p) {
        return new ItemBuilder(AlpsHeadUtils.getPlayerHead(p.getUniqueId()))
                .setName(text(LangUtil.getInstance().get(p, LangPaths.MenuTitle.SHOW_PLOTS), AQUA, BOLD))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(p, LangPaths.MenuDescription.SHOW_PLOTS), true).build())
                .build();
    }
}
