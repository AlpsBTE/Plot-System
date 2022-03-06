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

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.sk89q.worldedit.WorldEditException;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Review;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.utils.items.builder.LoreBuilder;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
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
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ReviewPlotMenu extends AbstractMenu {

    private final Plot plot;

    boolean sentWarning = false;

    public ReviewPlotMenu(Player player, Plot plot) {
        super(6, LangUtil.get(player, LangPaths.MenuTitle.REVIEW_PLOT, Integer.toString(plot.getID())), player);
        this.plot = plot;

        // Check if plot is from player
        try {
            if (plot.getPlotOwner().getUUID().equals(player.getUniqueId())){
                player.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Error.CANNOT_REVIEW_OWN_PLOT)));
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    protected void setPreviewItems() {
        final ItemStack[] itemPointZero = new ItemStack[4];
        final ItemStack[] itemPointOne = new ItemStack[4];
        final ItemStack[] itemPointTwo = new ItemStack[4];
        final ItemStack[] itemPointThree = new ItemStack[4];
        final ItemStack[] itemPointFour = new ItemStack[4];
        final ItemStack[] itemPointFive = new ItemStack[4];

        for(int i = 0; i < 54; i++) {
            switch (i) {
                case 4:
                    getMenu().getSlot(i).setItem(MenuItems.loadingItem(Material.MAP, getMenuPlayer()));
                    break;
                case 10:
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.ARROW, 1)
                            .setName("§a§l" + LangUtil.get(getMenuPlayer(), LangPaths.Review.Criteria.ACCURACY.toUpperCase()))
                            .setLore(new LoreBuilder()
                                    .addLines(LangUtil.get(getMenuPlayer(), LangPaths.Review.Criteria.ACCURACY_DESC))
                                    .build())
                            .build());
                    break;
                case 19:
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.PAINTING, 1)
                            .setName("§a§l" + LangUtil.get(getMenuPlayer(), LangPaths.Review.Criteria.BLOCK_PALETTE.toLowerCase()))
                            .setLore(new LoreBuilder()
                                    .addLines(LangUtil.get(getMenuPlayer(), LangPaths.Review.Criteria.BLOCK_PALETTE_DESC))
                                    .build())
                            .build());
                    break;
                case 28:
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.EYE_OF_ENDER, 1)
                            .setName(LangUtil.get(getMenuPlayer(), LangPaths.Review.Criteria.DETAILING.toLowerCase()))
                            .setLore(new LoreBuilder()
                                    .addLines(LangUtil.get(getMenuPlayer(), LangPaths.Review.Criteria.DETAILING_DESC))
                                    .build())
                            .build());
                    break;
                case 37:
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.WOOD_AXE, 1)
                            .setName("§a§l" + LangUtil.get(getMenuPlayer(), LangPaths.Review.Criteria.TECHNIQUE.toLowerCase()))
                            .setLore(new LoreBuilder()
                                    .addLines(LangUtil.get(getMenuPlayer(), LangPaths.Review.Criteria.TECHNIQUE_DESC))
                                    .build())
                            .build());
                    break;
                case 48:
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.CONCRETE, 1, (byte) 13)
                            .setName("§a§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.SUBMIT.toUpperCase()))
                            .setLore(new LoreBuilder()
                                    .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.SUBMIT_REVIEW)).build())
                            .build());
                    break;
                case 50:
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.CONCRETE, 1, (byte) 14)
                            .setName("§c§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.CANCEL.toUpperCase()))
                            .build());
                    break;
                default:
                    int column = (i % 9) + 1;
                    int row = (i - (i % 9)) / 9 + 1;
                    int position = ((i + 1) - (i + 1) % 9) / 54;
                    if (column > 2 && column < 9 && row > 1 && row < 6) {
                        if ((i + 1) % 9 == 3) {
                            itemPointZero[position] = new ItemBuilder(Material.WOOL, 1, (byte) 8)
                                    .setName("§l§70 " + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS))
                                    .setLore(new LoreBuilder()
                                            .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS)).build())
                                    .build();

                            //Add Enchantment
                            ItemMeta itemMeta = itemPointZero[position].getItemMeta();
                            itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
                            itemPointZero[position].setItemMeta(itemMeta);
                            getMenu().getSlot(i).setItem(itemPointZero[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 4) {
                            itemPointOne[position] = new ItemBuilder(Material.WOOL, 1, (byte) 14)
                                    .setName("§l§c1 " + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINT))
                                    .setLore(new LoreBuilder()
                                            .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS)).build())
                                    .build();

                            getMenu().getSlot(i).setItem(itemPointOne[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 5) {
                            itemPointTwo[position] = new ItemBuilder(Material.WOOL, 2, (byte) 1)
                                    .setName("§l§62 " + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS))
                                    .setLore(new LoreBuilder()
                                            .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS)).build())
                                    .build();
                            getMenu().getSlot(i).setItem(itemPointTwo[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 6) {
                            itemPointThree[position] = new ItemBuilder(Material.WOOL, 3, (byte) 4)
                                    .setName("§l§e3 " + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS))
                                    .setLore(new LoreBuilder()
                                            .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS)).build())
                                    .build();
                            getMenu().getSlot(i).setItem(itemPointThree[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 7) {
                            itemPointFour[position] = new ItemBuilder(Material.WOOL, 4, (byte) 13)
                                    .setName("§l§24 " + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS))
                                    .setLore(new LoreBuilder()
                                            .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS)).build())
                                    .build();
                            getMenu().getSlot(i).setItem(itemPointFour[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 8) {
                            itemPointFive[position] = new ItemBuilder(Material.WOOL, 5, (byte) 5)
                                    .setName("§l§a5 " + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.REVIEW_POINTS))
                                    .setLore(new LoreBuilder()
                                            .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.REVIEW_POINTS)).build())
                                    .build();
                            getMenu().getSlot(i).setItem(itemPointFive[(i - (i + 1) % 9) / 54]);
                        }
                    }
                    break;
            }
        }

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        try {
            getMenu().getSlot(4).setItem(new ItemBuilder(Material.MAP, 1)
                    .setName("§b§l" + LangUtil.get(getMenuPlayer(), LangPaths.Review.REVIEW_PLOT))
                    .setLore(new LoreBuilder()
                            .addLines(LangUtil.get(getMenuPlayer(), LangPaths.Plot.ID) + ": §f" + plot.getID(),
                                    "",
                                    "§7" + LangUtil.get(getMenuPlayer(), LangPaths.Plot.OWNER) + ": §f" + plot.getPlotOwner().getName(),
                                    "§7" + LangUtil.get(getMenuPlayer(), LangPaths.Plot.CITY) + ": §f" + plot.getCity().getName(),
                                    "§7" + LangUtil.get(getMenuPlayer(), LangPaths.Plot.DIFFICULTY) + ": §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase())
                            .build())
                    .build());
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            getMenu().getSlot(4).setItem(MenuItems.errorItem(getMenuPlayer()));
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for close item
        getMenu().getSlot(50).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.closeInventory());

        // Set click event for plot info item
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            try {
                new PlotActionsMenu(clickPlayer,plot);
            } catch (SQLException ex) {
               Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });

        // Set click event for submit item
        getMenu().getSlot(48).setClickHandler((clickPlayer, clickInformation) -> {
            CompletableFuture.runAsync(() -> {
                try {
                    StringBuilder score = new StringBuilder();

                    int totalRating = 0;
                    boolean isRejected = false;

                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 6; j++) {
                            if (getMenu().getSlot(11 + (i * 9) + j).getItem(clickPlayer).getItemMeta().hasEnchant(Enchantment.ARROW_DAMAGE)) {
                                if (i == 3) {
                                    score.append(j);
                                } else {
                                    score.append(j).append(",");
                                }
                                totalRating += j;
                                if (j <= 0) {
                                    isRejected = true;
                                }
                            }
                        }
                    }
                    if (totalRating <= 8) isRejected = true;

                    if (totalRating == 0 && !sentWarning) {
                        clickPlayer.sendMessage(Utils.getInfoMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Info.PLOT_WILL_GET_ABANDONED)));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.CreatePlotSound, 1, 1);
                        sentWarning = true;
                        return;
                    } else if (isRejected && !sentWarning) {
                        clickPlayer.sendMessage(Utils.getInfoMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Info.PLOT_WILL_GET_REJECTED)));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.CreatePlotSound, 1, 1);
                        sentWarning = true;
                        return;
                    } else if (totalRating == 0) {
                        plot.setStatus(Status.unfinished);
                        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> clickPlayer.performCommand("plot abandon " + plot.getID()));
                        return;
                    }

                    if (plot.isReviewed()) {
                        plot.getReview().setRating(score.toString());
                        plot.getReview().setReviewer(clickPlayer.getUniqueId());
                    } else {
                        new Review(plot.getID(), clickPlayer.getUniqueId(), score.toString());
                    }

                    double totalRatingWithMultiplier = totalRating * PlotManager.getMultiplierByDifficulty(plot.getDifficulty());
                    totalRating = (int) Math.floor(totalRatingWithMultiplier);
                    plot.setTotalScore(totalRating);

                    String reviewerConfirmationMessage;
                    clickPlayer.closeInventory();
                    if (!isRejected) {
                        clickPlayer.sendMessage(Utils.getInfoMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Info.SAVING_PLOT)));
                        try {
                            if (!PlotManager.savePlotAsSchematic(plot)) {
                                clickPlayer.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
                                Bukkit.getLogger().log(Level.WARNING, "Could not save finished plot schematic (ID: " + plot.getID() + ")!");
                                return;
                            }
                        } catch (IOException | SQLException | WorldEditException ex) {
                            Bukkit.getLogger().log(Level.WARNING, "Could not save finished plot schematic (ID: " + plot.getID() + ")!", ex);
                        }

                        plot.setStatus(Status.completed);
                        plot.getReview().setFeedbackSent(false);
                        plot.getReview().setFeedback("No Feedback");
                        plot.getPlotOwner().addCompletedBuild(1);

                        // Remove Plot from Owner
                        plot.getPlotOwner().removePlot(plot.getSlot());

                        if (plot.getPlotMembers().isEmpty()) {
                            // Plot was made alone
                            reviewerConfirmationMessage = Utils.getInfoMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Info.PLOT_MARKED_REVIEWED, Integer.toString(plot.getID()), plot.getPlotOwner().getName()));

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
                            reviewerConfirmationMessage = Utils.getInfoMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Info.PLOT_MARKED_REVIEWED, Integer.toString(plot.getID()), sb.toString()));

                            // Score gets split between all participants
                            plot.getPlotOwner().addScore(plot.getSharedScore());

                            for (Builder builder : plot.getPlotMembers()) {
                                // Score gets split between all participants
                                builder.addScore(plot.getSharedScore());
                                builder.addCompletedBuild(1);

                                // Remove Slot from Member
                                builder.removePlot(builder.getSlot(plot));
                            }
                        }
                    } else {
                        if (plot.getPlotMembers().size() != 0) {
                            // Plot was made alone
                            reviewerConfirmationMessage = Utils.getInfoMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Info.PLOT_REJECTED, Integer.toString(plot.getID()), plot.getPlotOwner().getName()));
                        } else {
                            // Plot was made in a group
                            StringBuilder sb = new StringBuilder();

                            for (int i = 0; i < plot.getPlotMembers().size(); i++) {
                                sb.append(i == plot.getPlotMembers().size() - 1 ?
                                        plot.getPlotMembers().get(i).getName() :
                                        plot.getPlotMembers().get(i).getName() + ", ");
                            }
                            reviewerConfirmationMessage = Utils.getInfoMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Info.PLOT_REJECTED, Integer.toString(plot.getID()), sb.toString()));
                        }

                        PlotHandler.undoSubmit(plot);
                    }

                    boolean finalIsRejected = isRejected;
                    Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                        for(Player player : plot.getWorld().getBukkitWorld().getPlayers()) {
                            player.teleport(Utils.getSpawnLocation());
                        }

                        // Delete plot world after reviewing
                        if (!finalIsRejected) plot.getWorld().deleteWorld();

                        clickPlayer.sendMessage(reviewerConfirmationMessage);
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.FinishPlotSound, 1f, 1f);
                    });

                    for (Builder member : plot.getPlotMembers()) {
                        if (member.isOnline()) {
                            PlotHandler.sendFeedbackMessage(Collections.singletonList(plot), member.getPlayer());
                        }
                    }

                    if(plot.getPlotOwner().isOnline()) {
                        PlotHandler.sendFeedbackMessage(Collections.singletonList(plot), plot.getPlotOwner().getPlayer());
                        plot.getReview().setFeedbackSent(true);
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            });
        });

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
                        if (getMenu().getSlot(slot - (column - 1) + j + 2).getItem(clickPlayer).getItemMeta().hasEnchant(Enchantment.ARROW_DAMAGE)) {
                            ItemStack itemPrevious = getMenu().getSlot(slot - (column - 1) + j + 2).getItem(clickPlayer);
                            ItemMeta metaPrevious = itemPrevious.getItemMeta();
                            metaPrevious.removeEnchant(Enchantment.ARROW_DAMAGE);
                            itemPrevious.setItemMeta(metaPrevious);
                            getMenu().getSlot(slot - (column - 1) + j + 2).setItem(itemPrevious);
                        }
                    }

                    meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);

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
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("111010111")
                .build();
    }
}
