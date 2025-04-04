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

import com.alpsbte.alpslib.utils.head.AlpsHeadUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class FeedbackMenu extends AbstractMenu {

    private PlotReview review = null;
    private final Plot plot;

    public FeedbackMenu(Player player, int plotID) {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.FEEDBACK, String.valueOf(plotID)), player);
        this.plot = DataProvider.PLOT.getPlotById(plotID);
    }

    @Override
    protected void setPreviewItems() {
        getMenu().getSlot(16).setItem(MenuItems.loadingItem(Material.PLAYER_HEAD, getMenuPlayer()));
        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Get review id from plot
        DataProvider.PLOT.getReview(plot.getID()).ifPresent(value -> this.review = value);

        // Set score item
        getMenu().getSlot(10).setItem(new ItemBuilder(Material.NETHER_STAR)
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.SCORE), AQUA, BOLD))
                .setLore(new LoreBuilder()
                        .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.TOTAL_SCORE) + ": ", GRAY).append(text(plot.getTotalScore(), WHITE)))
                        .emptyLine()
                        .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.ACCURACY) + ": ", GRAY)
                                .append(Utils.ItemUtils.getColoredPointsComponent(review.getRating().getAccuracyPoints())
                                        .append(text("/", DARK_GRAY))
                                        .append(text("5", GREEN))))
                        .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.BLOCK_PALETTE) + ": ", GRAY)
                                .append(Utils.ItemUtils.getColoredPointsComponent(review.getRating().getBlockPalettePoints()))
                                .append(text("/", DARK_GRAY))
                                .append(text("5", GREEN)))
                        .emptyLine()
                        .addLine(plot.isRejected()
                                ? text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.REJECTED), RED, BOLD)
                                : text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.ACCEPTED), GREEN, BOLD))
                        .build())
                .build());

        // Set feedback text item
        String feedbackText = review.getFeedback() == null ? "No Feedback" : review.getFeedback(); // TODO: translate no feedback text
        getMenu().getSlot(13).setItem(new ItemBuilder(Material.WRITABLE_BOOK)
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.FEEDBACK), AQUA, BOLD))
                .setLore(new LoreBuilder()
                        .addLine(feedbackText.replaceAll("//", " "), true)
                        .build())
                .build());

        // Set reviewer item
        getMenu().getSlot(16).setItem(new ItemBuilder(AlpsHeadUtils.getPlayerHead(review.getReviewerUUID()))
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.REVIEWER), AQUA, BOLD))
                .setLore(new LoreBuilder().addLine(review.getReviewer().getName()).build())
                .build());
    }

    @Override
    protected void setItemClickEventsAsync() {}

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(empty()).build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
    }
}
