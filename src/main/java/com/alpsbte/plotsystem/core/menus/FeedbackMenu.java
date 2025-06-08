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
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.core.system.review.ToggleCriteria;
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
        plot.getLatestReview().ifPresent(value -> this.review = value);

        // Set score item
        getMenu().getSlot(10).setItem(new ItemBuilder(BaseItems.REVIEW_SCORE.getItem())
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.SCORE), AQUA, BOLD))
                .setLore(new LoreBuilder()
                        .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.TOTAL_SCORE) + ": ", GRAY).append(text(review.getScore(), WHITE)))
                        .emptyLine()
                        .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.ACCURACY) + ": ", GRAY)
                                .append(Utils.ItemUtils.getColoredPointsComponent(review.getRating().getAccuracyPoints(), 5)
                                        .append(text("/", DARK_GRAY))
                                        .append(text("5", GREEN))))
                        .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.BLOCK_PALETTE) + ": ", GRAY)
                                .append(Utils.ItemUtils.getColoredPointsComponent(review.getRating().getBlockPalettePoints(), 5))
                                .append(text("/", DARK_GRAY))
                                .append(text("5", GREEN)))
                        .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.TOGGLE_POINTS) + ": ", GRAY)
                                .append(Utils.ItemUtils.getColoredPointsComponent(review.getRating().getTogglePoints(), 10))
                                .append(text("/", DARK_GRAY))
                                .append(text("10", GREEN)))
                        .emptyLine()
                        .addLine(plot.isRejected()
                                ? text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.REJECTED), RED, BOLD)
                                : text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.ACCEPTED), GREEN, BOLD))
                        .build())
                .build());

        // Set toggles item
        getMenu().getSlot(12).setItem(getTogglesItem());

        // Set feedback text item
        String feedbackText = review.getFeedback() == null ? LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.NO_FEEDBACK) : review.getFeedback();
        getMenu().getSlot(14).setItem(new ItemBuilder(BaseItems.REVIEW_FEEDBACK.getItem())
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

    private ItemStack getTogglesItem() {
        ItemBuilder itemBuilder = new ItemBuilder(BaseItems.REVIEW_TOGGLE_CHECKED.getItem())
                .setName(text("Toggle Criteria", AQUA, BOLD));


        int totalCheckCriteriaCount = review.getRating().getAllToggles().size();
        int checkedCount = review.getRating().getCheckedToggles().size();
        LoreBuilder loreBuilder = new LoreBuilder()
                .addLine(text(checkedCount + "/" + totalCheckCriteriaCount, GRAY));

        if (!review.getRating().getUncheckedToggles().isEmpty()) {
            loreBuilder.emptyLine();
            loreBuilder.addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.CRITERIA_NOT_FULFILLED) + ":", RED, BOLD));
            for (ToggleCriteria unchecked : review.getRating().getUncheckedToggles()) {
                loreBuilder.addLine(text("• ", DARK_GRAY).append(text(unchecked.getDisplayName(getMenuPlayer()), GRAY)
                        .append(unchecked.isOptional()
                                ? empty()
                                : text(" - ").append(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.REQUIRED), GOLD)))));
            }
        }

        if (!review.getRating().getCheckedToggles().isEmpty()) {
            loreBuilder.emptyLine();
            loreBuilder.addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.CRITERIA_FULFILLED) + ":", GREEN, BOLD));
            for (ToggleCriteria unchecked : review.getRating().getCheckedToggles()) {
                loreBuilder.addLine(text("• ", DARK_GRAY).append(text(unchecked.getDisplayName(getMenuPlayer()), GRAY)));
            }
        }

        return itemBuilder.setLore(loreBuilder.build()).build();
    }
}