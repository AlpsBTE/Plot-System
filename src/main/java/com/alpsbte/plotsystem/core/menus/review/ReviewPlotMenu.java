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

package com.alpsbte.plotsystem.core.menus.review;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.core.menus.PlotActionsMenu;
import com.alpsbte.plotsystem.core.system.review.ReviewRating;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.util.List;
import java.util.Objects;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class ReviewPlotMenu extends AbstractMenu {
    private final Plot plot;
    private final ReviewRating rating;

    boolean sentWarning = false;

    public ReviewPlotMenu(Player player, Plot plot, ReviewRating rating) {
        super(6, LangUtil.getInstance().get(player, LangPaths.MenuTitle.REVIEW_PLOT, Integer.toString(plot.getID())), player);
        this.plot = plot;
        this.rating = rating;
    }

    public ReviewPlotMenu(Player player, Plot plot) {
        this(player, plot,
                new ReviewRating(0, 0, List.of(), DataProvider.REVIEW.getBuildTeamToggleCriteria(plot.getCityProject().getBuildTeam().getID())));
    }

    @Override
    protected void setPreviewItems() {
        getMenu().getSlot(4).setItem(MenuItems.loadingItem(Material.MAP, getMenuPlayer()));

        getMenu().getSlot(19).setItem(getAccuracyItem());
        getMenu().getSlot(28).setItem(getBlockPaletteItem());

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plot information item
        getMenu().getSlot(4).setItem(ReviewItems.getPlotInfoItem(getMenuPlayer(), plot));

        // Set review information item
        getMenu().getSlot(7).setItem(ReviewItems.getReviewInfoItem(getMenuPlayer()));

        // Set back item
        getMenu().getSlot(48).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        // Set next item
        getMenu().getSlot(50).setItem(MenuItems.continueMenuItem(getMenuPlayer()));

        // Set point items
        for (int row = 0; row < 2; row++) {
            int selectedPoints = row == 0 ? rating.getAccuracyPoints() : rating.getBlockPalettePoints();
            for (int col = 0; col < 6; col++) {
                ItemStack item;
                switch (col) {
                    case 0 -> item = getZeroPointItem();
                    case 1 -> item = getOnePointItem();
                    case 2 -> item = getTwoPointItem();
                    case 3 -> item = getThreePointItem();
                    case 4 -> item = getFourPointItem();
                    default -> item = getFivePointItem();
                }

                if (selectedPoints == col) {
                    ItemMeta itemMeta = item.getItemMeta();
                    Objects.requireNonNull(itemMeta).addEnchant(Enchantment.POWER, 1, true);
                    item.setItemMeta(itemMeta);
                    item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                } else {
                    item.removeEnchantment(Enchantment.POWER);
                }

                getMenu().getSlot(20 + col + (9 * row)).setItem(item);
            }
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for back item
        getMenu().getSlot(48).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new ReviewMenu(clickPlayer);
        });

        // Set click event for plot info item
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new PlotActionsMenu(clickPlayer, plot);
        });

        // Set click event for submit item
        getMenu().getSlot(50).setClickHandler((clickPlayer, clickInformation)
                -> Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            int totalRating = rating.getAccuracyPoints() + rating.getBlockPalettePoints();
            if (totalRating == 0 && !sentWarning) {
                clickPlayer.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_WILL_GET_ABANDONED)));
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.CREATE_PLOT_SOUND, 1, 1);
                sentWarning = true;
                return;
            } else if (totalRating == 0) {
                plot.setStatus(Status.unfinished);
                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                    clickPlayer.closeInventory();
                    clickPlayer.performCommand("plot abandon " + plot.getID());
                });
                return;
            }
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                clickPlayer.closeInventory();
                new ReviewPlotTogglesMenu(clickPlayer, plot, rating);
            });
        }));

        // Set click event for point selection items
        for (int col = 0; col < 6; col++) {
            for (int row = 0; row < 2; row++) {
                int slot = 20 + col + (9 * row);
                int captureRow = row; // need to assign separate variables so that they can be captured by the lambda
                int points = col;
                ItemMeta meta = getMenu().getSlot(slot).getItem(getMenuPlayer()).getItemMeta();
                getMenu().getSlot(slot).setClickHandler((clickPlayer, clickInformation) -> {
                    for (int j = 0; j < 6; j++) {
                        ItemStack previousItem = getMenu().getSlot(20 + j + (9 * captureRow)).getItem(clickPlayer);
                        if (previousItem == null || !previousItem.getItemMeta().hasEnchant(Enchantment.POWER)) continue;

                        ItemMeta metaPrevious = previousItem.getItemMeta();
                        assert metaPrevious != null;
                        metaPrevious.removeEnchant(Enchantment.POWER);
                        previousItem.setItemMeta(metaPrevious);
                        getMenu().getSlot(20 + j + (9 * captureRow)).setItem(previousItem);
                    }

                    assert meta != null;
                    meta.addEnchant(Enchantment.POWER, 1, true);
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);

                    ItemStack newItem = getMenu().getSlot(slot).getItem(clickPlayer);
                    newItem.setItemMeta(meta);
                    newItem.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    getMenu().getSlot(slot).setItem(newItem);
                    sentWarning = false;

                    if (captureRow == 0) rating.setAccuracyPoints(points);
                    else rating.setBlockPalettePoints(points);
                });
            }
        }
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
                .pattern("111010111")
                .build();
    }

    // --- Category Items ---
    private ItemStack getAccuracyItem() {
        return new ItemBuilder(BaseItems.REVIEW_ACCURACY.getItem())
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.ACCURACY))
                        .color(GREEN)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLines(true, LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.ACCURACY_DESC))
                        .build())
                .build();
    }

    private ItemStack getBlockPaletteItem() {
        return new ItemBuilder(BaseItems.REVIEW_BLOCK_PALETTE.getItem())
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.BLOCK_PALETTE))
                        .color(GREEN)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLines(true, LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.BLOCK_PALETTE_DESC))
                        .build())
                .build();
    }

    // --- Point Items ---
    private ItemStack getZeroPointItem() {
        return new ItemBuilder(BaseItems.REVIEW_POINT_ZERO.getItem())
                .setName(text("0 " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS))
                        .color(GRAY)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS))
                        .build())
                .build();
    }

    private ItemStack getOnePointItem() {
        return new ItemBuilder(BaseItems.REVIEW_POINT_ONE.getItem())
                .setName(text("1 " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINT))
                        .color(RED)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS))
                        .build())
                .build();
    }

    private ItemStack getTwoPointItem() {
        ItemStack item = BaseItems.REVIEW_POINT_TWO.getItem();
        item.setAmount(2);

        return new ItemBuilder(item)
                .setName(text("2 " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS))
                        .color(GOLD)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS))
                        .build())
                .build();
    }

    private ItemStack getThreePointItem() {
        ItemStack item = BaseItems.REVIEW_POINT_THREE.getItem();
        item.setAmount(3);

        return new ItemBuilder(item)
                .setName(text("3 " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS))
                        .color(YELLOW)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS))
                        .build())
                .build();
    }

    private ItemStack getFourPointItem() {
        ItemStack item = BaseItems.REVIEW_POINT_FOUR.getItem();
        item.setAmount(4);

        return new ItemBuilder(item)
                .setName(text("4 " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS))
                        .color(DARK_GREEN)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS))
                        .build())
                .build();
    }

    private ItemStack getFivePointItem() {
        ItemStack item = BaseItems.REVIEW_POINT_FIVE.getItem();
        item.setAmount(5);

        return new ItemBuilder(item)
                .setName(text("5 " + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS))
                        .color(GREEN)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS))
                        .build())
                .build();
    }
}
