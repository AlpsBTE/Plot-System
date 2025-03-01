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
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.chat.ChatInput;
import com.alpsbte.plotsystem.utils.chat.PlayerFeedbackChatInput;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.CustomHeads;
import com.sk89q.worldedit.WorldEditException;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Review;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class ReviewPlotMenu extends AbstractMenu {
    private final Plot plot;

    boolean sentWarning = false;

    public ReviewPlotMenu(Player player, Plot plot) {
        super(6, LangUtil.getInstance().get(player, LangPaths.MenuTitle.REVIEW_PLOT, Integer.toString(plot.getID())), player);
        this.plot = plot;
    }

    @Override
    protected void setPreviewItems() {
        final ItemStack[] itemPointZero = new ItemStack[4];
        final ItemStack[] itemPointOne = new ItemStack[4];
        final ItemStack[] itemPointTwo = new ItemStack[4];
        final ItemStack[] itemPointThree = new ItemStack[4];
        final ItemStack[] itemPointFour = new ItemStack[4];
        final ItemStack[] itemPointFive = new ItemStack[4];

        for (int i = 0; i < 54; i++) {
            int column = (i % 9) + 1;
            int row = (i - (i % 9)) / 9 + 1;
            int position = ((i + 1) - (i + 1) % 9) / 54;
            if (column > 2 && column < 9 && row > 1 && row < 6) {
                if ((i + 1) % 9 == 3) {
                    itemPointZero[position] = getZeroPointItem();

                    //Add Enchantment
                    ItemMeta itemMeta = itemPointZero[position].getItemMeta();
                    Objects.requireNonNull(itemMeta).addEnchant(Enchantment.POWER, 1, true);
                    itemPointZero[position].setItemMeta(itemMeta);
                    getMenu().getSlot(i).setItem(itemPointZero[(i - (i + 1) % 9) / 54]);
                } else if ((i + 1) % 9 == 4) {
                    itemPointOne[position] = getOnePointItem();
                    getMenu().getSlot(i).setItem(itemPointOne[(i - (i + 1) % 9) / 54]);
                } else if ((i + 1) % 9 == 5) {
                    itemPointTwo[position] = getTwoPointItem();
                    getMenu().getSlot(i).setItem(itemPointTwo[(i - (i + 1) % 9) / 54]);
                } else if ((i + 1) % 9 == 6) {
                    itemPointThree[position] = getThreePointItem();
                    getMenu().getSlot(i).setItem(itemPointThree[(i - (i + 1) % 9) / 54]);
                } else if ((i + 1) % 9 == 7) {
                    itemPointFour[position] = getFourPointItem();
                    getMenu().getSlot(i).setItem(itemPointFour[(i - (i + 1) % 9) / 54]);
                } else if ((i + 1) % 9 == 8) {
                    itemPointFive[position] = getFivePointItem();
                    getMenu().getSlot(i).setItem(itemPointFive[(i - (i + 1) % 9) / 54]);
                }
            }
        }

        getMenu().getSlot(4).setItem(MenuItems.loadingItem(Material.MAP, getMenuPlayer()));

        getMenu().getSlot(10).setItem(getAccuracyItem());
        getMenu().getSlot(19).setItem(getBlockPaletteItem());
        getMenu().getSlot(28).setItem(getDetailingItem());
        getMenu().getSlot(37).setItem(getTechniqueItem());

        getMenu().getSlot(48).setItem(getSubmitItem());
        getMenu().getSlot(50).setItem(getCancelItem());

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set back item
        getMenu().getSlot(1).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        // Set plot information item
        getMenu().getSlot(4).setItem(getPlotInfoItem());

        // Set review information item
        getMenu().getSlot(7).setItem(getReviewInfoItem());
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for back item
        getMenu().getSlot(1).setClickHandler((clickPlayer, clickInformation)
                -> new ReviewMenu(getMenuPlayer()));

        // Set click event for close item
        getMenu().getSlot(50).setClickHandler((clickPlayer, clickInformation)
                -> clickPlayer.closeInventory());

        // Set click event for plot info item
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation)
                -> new PlotActionsMenu(clickPlayer, plot));

        /* Set click event for submit item */
        getMenu().getSlot(48).setClickHandler((clickPlayer, clickInformation)
                -> Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            try {
                StringBuilder score = new StringBuilder();

                int totalRating = 0;
                boolean isRejected = false;

                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 6; j++) {
                        if (Objects.requireNonNull(getMenu().getSlot(11 + (i * 9) + j).getItem(clickPlayer).getItemMeta()).hasEnchant(Enchantment.POWER)) {
                            if (i == 3) {
                                score.append(j);
                            } else {
                                score.append(j).append(",");
                            }
                            totalRating += j;
                            if (j == 0) isRejected = true;
                        }
                    }
                }
                if (totalRating <= 8) isRejected = true;

                if (totalRating == 0 && !sentWarning) {
                    clickPlayer.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_WILL_GET_ABANDONED)));
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.CREATE_PLOT_SOUND, 1, 1);
                    sentWarning = true;
                    return;
                } else if (isRejected && !sentWarning) {
                    clickPlayer.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_WILL_GET_REJECTED)));
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.CREATE_PLOT_SOUND, 1, 1);
                    sentWarning = true;
                    return;
                } else if (totalRating == 0) {
                    plot.setStatus(Status.unfinished);
                    Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> clickPlayer.performCommand("plot abandon " + plot.getID()));
                    return;
                }
                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> clickPlayer.closeInventory());

                if (plot.isReviewed()) {
                    plot.getReview().setRating(score.toString());
                    plot.getReview().setReviewer(clickPlayer.getUniqueId());
                } else new Review(plot.getID(), clickPlayer.getUniqueId(), score.toString());

                double totalRatingWithMultiplier = totalRating * Plot.getMultiplierByDifficulty(plot.getDifficulty());
                totalRating = (int) Math.floor(totalRatingWithMultiplier);
                plot.setTotalScore(totalRating);

                Component reviewerConfirmationMessage;

                if (!isRejected) {
                    clickPlayer.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.SAVING_PLOT)));
                    try {
                        if (!PlotUtils.savePlotAsSchematic(plot)) {
                            clickPlayer.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
                            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not save finished plot schematic (ID: " + plot.getID() + ")!"));
                            return;
                        }
                    } catch (IOException | WorldEditException ex) {
                        PlotSystem.getPlugin().getComponentLogger().error(text("Could not save finished plot schematic (ID: " + plot.getID() + ")!"), ex);
                    }

                plot.setStatus(Status.completed);
                plot.getReview().setFeedbackSent(false);
                plot.getReview().setFeedback("No Feedback");

                // Remove Plot from Owner
                plot.getPlotOwner().setSlot(plot.getSlot(), -1);

                    if (plot.getPlotMembers().isEmpty()) {
                        // Plot was made alone
                        reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_MARKED_REVIEWED, Integer.toString(plot.getID()), plot.getPlotOwner().getName()));

                        // Builder gets 100% of score
                        plot.getPlotOwner().addScore(totalRating);
                    } else {
                        // Plot was made in a group
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < plot.getPlotMembers().size(); i++) {
                            sb.append(i == plot.getPlotMembers().size() - 1 ?
                                    plot.getPlotMembers().get(i).getName() :
                                    plot.getPlotMembers().get(i).getName() + ", ");
                        }
                        reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_MARKED_REVIEWED, Integer.toString(plot.getID()), sb.toString()));

                        // Score gets split between all participants
                        plot.getPlotOwner().addScore(plot.getSharedScore());

                    for (Builder builder : plot.getPlotMembers()) {
                        // Score gets split between all participants
                        builder.addScore(plot.getSharedScore());

                        // Remove Slot from Member
                        builder.setSlot(plot.getSlot(), -1);
                    }
                }
            } else {
                if (!plot.getPlotMembers().isEmpty()) {
                    // Plot was made alone
                    reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_REJECTED, Integer.toString(plot.getID()), plot.getPlotOwner().getName()));
                } else {
                    // Plot was made in a group
                    StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < plot.getPlotMembers().size(); i++) {
                            sb.append(i == plot.getPlotMembers().size() - 1 ?
                                    plot.getPlotMembers().get(i).getName() :
                                    plot.getPlotMembers().get(i).getName() + ", ");
                        }
                        reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_REJECTED, Integer.toString(plot.getID()), sb.toString()));
                    }

                    PlotUtils.Actions.undoSubmit(plot);
                }

                boolean finalIsRejected = isRejected;
                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                    for (Player player : plot.getWorld().getBukkitWorld().getPlayers()) {
                        player.teleport(Utils.getSpawnLocation());
                    }

                    // Delete plot world after reviewing
                    if (!finalIsRejected && plot.getPlotType().hasOnePlotPerWorld())
                        plot.getWorld().deleteWorld();

                    clickPlayer.sendMessage(reviewerConfirmationMessage);
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.FINISH_PLOT_SOUND, 1f, 1f);

                    ChatInput.awaitChatInput.put(clickPlayer.getUniqueId(),
                            new PlayerFeedbackChatInput(clickPlayer.getUniqueId(), plot.getReview()));
                    PlayerFeedbackChatInput.sendChatInputMessage(clickPlayer);
                });

                for (Builder member : plot.getPlotMembers()) {
                    if (member.isOnline()) PlotUtils.ChatFormatting.sendFeedbackMessage(Collections.singletonList(plot), member.getPlayer());
                }

                if (plot.getPlotOwner().isOnline()) {
                    PlotUtils.ChatFormatting.sendFeedbackMessage(Collections.singletonList(plot), plot.getPlotOwner().getPlayer());
                    plot.getReview().setFeedbackSent(true);
                }
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        }));

        // Set click event for point selection items
        for (int i = 0; i < 54; i++) {
            int slot = i;

            int column = (slot % 9) + 1;
            int row = (slot - (slot % 9)) / 9 + 1;

            ItemMeta meta = getMenu().getSlot(slot).getItem(getMenuPlayer()).getItemMeta();

            if (column > 2 && column < 9 && row > 1 && row < 6) {
                //Go through the whole points row
                getMenu().getSlot(i).setClickHandler((clickPlayer, clickInformation) -> {
                    for (int j = 0; j < 6; j++) {
                        if (!Objects.requireNonNull(getMenu().getSlot(slot - (column - 1) + j + 2).getItem(clickPlayer).getItemMeta()).hasEnchant(Enchantment.POWER)) continue;

                        ItemStack itemPrevious = getMenu().getSlot(slot - (column - 1) + j + 2).getItem(clickPlayer);
                        ItemMeta metaPrevious = itemPrevious.getItemMeta();
                        assert metaPrevious != null;
                        metaPrevious.removeEnchant(Enchantment.POWER);
                        itemPrevious.setItemMeta(metaPrevious);
                        getMenu().getSlot(slot - (column - 1) + j + 2).setItem(itemPrevious);
                    }

                    assert meta != null;
                    meta.addEnchant(Enchantment.POWER, 1, true);
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);

                    ItemStack newItem = getMenu().getSlot(slot).getItem(clickPlayer);
                    newItem.setItemMeta(meta);
                    getMenu().getSlot(slot).setItem(newItem);
                    sentWarning = false;
                });
            }
        }
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(Component.empty()).build())
                .pattern("101101101")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("111010111")
                .build();
    }

    // --- Info Items ---
    private ItemStack getPlotInfoItem() {
        String plotOwner, city, country, difficulty;
        Player plotOwnerPlayer;

        plotOwner = plot.getPlotOwner().getName();
        city = plot.getCityProject().getName(getMenuPlayer());
        country = plot.getCityProject().getCountry().getName(getMenuPlayer());
        difficulty = plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase();

        plotOwnerPlayer = plot.getPlotOwner().getPlayer();


        return new ItemBuilder(BaseItems.REVIEW_INFO_PLOT.getItem())
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.REVIEW_PLOT))
                        .color(AQUA)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.ID) + ": ", GRAY).append(text(plot.getID(), WHITE)))
                        .emptyLine()
                        .addLines(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.OWNER) + ": ", GRAY).append(text(plotOwner, WHITE)),
                                text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.CITY) + ": ", GRAY).append(text(city, WHITE)),
                                text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.COUNTRY) + ": ", GRAY).append(text(country, WHITE)),
                                text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.DIFFICULTY) + ": ", GRAY).append(text(difficulty, WHITE)))
                        .emptyLine()
                        .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.PLAYER_LANGUAGE) + ": ", GRAY).append(text(LangUtil.getInstance().get(plotOwnerPlayer, "lang.name"), WHITE)))
                        .build())
                .build();
    }

    private ItemStack getReviewInfoItem() {
        String points = LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS);

        return new ItemBuilder(AlpsHeadUtils.getCustomHead(CustomHeads.INFO_BUTTON.getId()))
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.INFORMATION), AQUA).decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLines(true, LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.INFORMATION))
                        .emptyLine()
                        .addLines(text(points + " <= 0: ", WHITE).append(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.ABANDONED), RED)),
                                text(points + " <= 8: ", WHITE).append(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.REJECTED), YELLOW)),
                                text(points + " > 8: ", WHITE).append(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.ACCEPTED), GREEN)))
                        .build())
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

    private ItemStack getDetailingItem() {
        return new ItemBuilder(BaseItems.REVIEW_DETAILING.getItem())
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.DETAILING))
                        .color(GREEN)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLines(true, LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.DETAILING_DESC))
                        .build())
                .build();
    }

    private ItemStack getTechniqueItem() {
        return new ItemBuilder(BaseItems.REVIEW_TECHNIQUE.getItem())
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.TECHNIQUE))
                        .color(GREEN)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLines(true, LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.TECHNIQUE_DESC))
                        .build())
                .build();
    }

    // --- Button Items ---
    private ItemStack getSubmitItem() {
        return new ItemBuilder(BaseItems.REVIEW_SUBMIT.getItem())
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SUBMIT))
                        .color(GREEN)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SUBMIT_REVIEW), true)
                        .build())
                .build();
    }

    private ItemStack getCancelItem() {
        return new ItemBuilder(BaseItems.REVIEW_CANCEL.getItem())
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.CANCEL))
                        .color(RED)
                        .decoration(BOLD, true))
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
