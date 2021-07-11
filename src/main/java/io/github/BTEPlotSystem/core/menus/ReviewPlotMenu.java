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

package github.BTEPlotSystem.core.menus;

import com.sk89q.worldedit.WorldEditException;
import github.BTEPlotSystem.core.menus.AbstractMenu;
import github.BTEPlotSystem.core.menus.PlotActionsMenu;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.Review;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotHandler;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.MenuItems;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Slot;
import github.BTEPlotSystem.utils.enums.Status;
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
import java.util.logging.Level;

public class ReviewPlotMenu extends AbstractMenu {

    private Plot plot;

    boolean sentWarning = false;

    private final ItemStack[] itemPointZero = new ItemStack[4];
    private final ItemStack[] itemPointOne = new ItemStack[4];
    private final ItemStack[] itemPointTwo = new ItemStack[4];
    private final ItemStack[] itemPointThree = new ItemStack[4];
    private final ItemStack[] itemPointFour = new ItemStack[4];
    private final ItemStack[] itemPointFive = new ItemStack[4];

    public ReviewPlotMenu(Player player, Plot plot) {
        super(6, "Review Plot #" + plot.getID(), player);

        this.plot = plot;

        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("111010111")
                .build();
        mask.apply(getMenu());

        // Check if plot is from player
        try {
            if (plot.getBuilder().getUUID().equals(player.getUniqueId())){
                player.sendMessage(Utils.getErrorMessageFormat("You cannot review your own builds!"));
                return;
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }

        addMenuItems();
        setItemClickEvents();

        getMenu().open(getMenuPlayer());
    }

    @Override
    protected void addMenuItems() {
        for(int i = 0; i < 54; i++) {
            switch (i) {
                case 4:
                    try {
                        // Map Item
                        getMenu().getSlot(i).setItem(new ItemBuilder(Material.MAP, 1)
                                .setName("§b§lReview Plot")
                                .setLore(new LoreBuilder()
                                        .addLines("ID: §f" + plot.getID(),
                                                "",
                                                "§7Builder: §f" + plot.getBuilder().getName(),
                                                "§7City: §f" + plot.getCity().getName(),
                                                "§7Difficulty: §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase())
                                        .build())
                                .build());
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        getMenu().getSlot(i).setItem(MenuItems.errorItem());
                    }
                    break;
                case 10:
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.ARROW, 1)
                            .setName("§a§lACCURACY")
                            .setLore(new LoreBuilder()
                                    .addLines("How accurate is the building?",
                                            "",
                                            "- Looks like in RL",
                                            "- Correct outlines",
                                            "- Correct height",
                                            "- Is completed")
                                    .build())
                            .build());
                    break;
                case 19:
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.PAINTING, 1)
                            .setName("§a§lBLOCK PALETTE")
                            .setLore(new LoreBuilder()
                                    .addLines("How many different blocks are used and how creative are they?",
                                            "",
                                            "- Choice of block colours/textures",
                                            "- Random blocks")
                                    .build())
                            .build());
                    break;
                case 28:
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.EYE_OF_ENDER, 1)
                            .setName("§a§lDETAILING")
                            .setLore(new LoreBuilder()
                                    .addLines("How much detail does the building have?",
                                            "",
                                            "- Roof details",
                                            "- Details on the facades",
                                            "- Heads and Banners")
                                    .build())
                            .build());
                    break;
                case 37:
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.WOOD_AXE, 1)
                            .setName("§a§lTECHNIQUE")
                            .setLore(new LoreBuilder()
                                    .addLines("What building techniques have been used and how creative are they?",
                                            "",
                                            "- WorldEdit",
                                            "- Used Special Blocks")
                                    .build())
                            .build());
                    break;
                case 48:
                    // Submit Item
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.CONCRETE, 1, (byte) 13)
                            .setName("§a§lSUBMIT")
                            .setLore(new LoreBuilder()
                                    .addLine("Submit selected points and mark plot as reviewed").build())
                            .build());
                    break;
                case 50:
                    // Cancel Item
                    getMenu().getSlot(i).setItem(new ItemBuilder(Material.CONCRETE, 1, (byte) 14)
                            .setName("§c§lCANCEL")
                            .setLore(new LoreBuilder()
                                    .addLine("Close the menu").build())
                            .build());
                    break;
                default:
                    int column = (i % 9) + 1;
                    int row = (i - (i % 9)) / 9 + 1;
                    if (column > 2 && column < 9 && row > 1 && row < 6) {
                        if ((i + 1) % 9 == 3) {
                            itemPointZero[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 1, (byte) 8)
                                    .setName("§l§70 Points")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();

                            //Add Enchantment
                            ItemMeta itemMeta = itemPointZero[((i + 1) - (i + 1) % 9) / 54].getItemMeta();
                            itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
                            itemPointZero[((i + 1) - (i + 1) % 9) / 54].setItemMeta(itemMeta);
                            getMenu().getSlot(i).setItem(itemPointZero[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 4) {
                            itemPointOne[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 1, (byte) 14)
                                    .setName("§l§c1 Point")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();

                            getMenu().getSlot(i).setItem(itemPointOne[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 5) {
                            itemPointTwo[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 2, (byte) 1)
                                    .setName("§l§62 Points")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();
                            getMenu().getSlot(i).setItem(itemPointTwo[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 6) {
                            itemPointThree[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 3, (byte) 4)
                                    .setName("§l§e3 Points")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();
                            getMenu().getSlot(i).setItem(itemPointThree[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 7) {
                            itemPointFour[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 4, (byte) 13)
                                    .setName("§l§24 Points")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();
                            getMenu().getSlot(i).setItem(itemPointFour[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 8) {
                            itemPointFive[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 5, (byte) 5)
                                    .setName("§l§a5 Points")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();
                            getMenu().getSlot(i).setItem(itemPointFive[(i - (i + 1) % 9) / 54]);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    protected void setItemClickEvents() {
        // Cancel Item
        getMenu().getSlot(50).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
        });

        // Map Item
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            try {
                new PlotActionsMenu(clickPlayer,plot);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Submit Item
        getMenu().getSlot(48).setClickHandler((clickPlayer, clickInformation) -> {
            StringBuilder score = new StringBuilder();

            int totalRating = 0;
            boolean isRejected = false;

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 6; j++) {
                    if (getMenu().getSlot(11 + (i * 9) + j).getItem(clickPlayer).getItemMeta().hasEnchant(Enchantment.ARROW_DAMAGE)) {
                        if(i == 3) {
                            score.append(j);
                        } else {
                            score.append(j).append(",");
                        }
                        totalRating+=j;
                        if (j <= 0){ isRejected = true; }
                    }
                }
            }
            if (totalRating <= 8){ isRejected = true; }

            if(totalRating == 0 && !sentWarning) {
                clickPlayer.sendMessage(Utils.getInfoMessageFormat("§c§lWARNING: §cThis plot will automatically get abandoned!"));
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.CreatePlotSound, 1, 1);
                sentWarning = true;
                return;
            } else if(isRejected && !sentWarning) {
                clickPlayer.sendMessage(Utils.getInfoMessageFormat("§c§lWARNING: §cThis plot will get rejected!"));
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.CreatePlotSound, 1, 1);
                sentWarning = true;
                return;
            } else if(totalRating == 0) {
                try {
                    PlotHandler.abandonPlot(plot);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }

            try {
                if(plot.isReviewed()) {
                    plot.getReview().setRating(score.toString());
                    plot.getReview().setReviewer(clickPlayer.getUniqueId());
                } else {
                    new Review(plot.getID(), clickPlayer.getUniqueId(), score.toString());
                }

                double totalRatingWithMultiplier = totalRating * PlotManager.getMultiplierByDifficulty(plot.getDifficulty());
                totalRating = (int) Math.floor(totalRatingWithMultiplier);
                plot.setScore(totalRating);

                if (!isRejected){
                    plot.getReview().setFeedbackSent(false);
                    plot.getReview().setFeedback("No Feedback");
                    plot.setStatus(Status.complete);
                    plot.getBuilder().addCompletedBuild(1);

                    // Remove Plot from Owner
                    try {
                        plot.getBuilder().removePlot(plot.getSlot());
                    } catch (Exception ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "Could not remove Plot of builders slot!", ex);
                    }

                    if (plot.getPlotMembers().isEmpty()) {
                        // Plot was made alone
                        clickPlayer.sendMessage("§7>> §aPlot §6#" + plot.getID() + " §aby §6" + plot.getBuilder().getName() + " §amarked as reviewed!");

                        // Builder gets 100% of score
                        plot.getBuilder().addScore(totalRating);
                    } else {
                        // Plot was made in a group
                        StringBuilder sb = new StringBuilder("§7>> §aPlot §6#" + plot.getID() + " §aby §6" + plot.getBuilder().getName() + ", ");

                        for (int i = 0; i < plot.getPlotMembers().size(); i++) {
                            sb.append(i == plot.getPlotMembers().size() - 1 ?
                                    plot.getPlotMembers().get(i).getName() + " §amarked as reviewed!" :
                                    plot.getPlotMembers().get(i).getName() + ", ");
                        }

                        // Score gets split between all participants
                        plot.getBuilder().addScore((int) Math.floor((double) (totalRating/(plot.getPlotMembers().size() + 1))));

                        for (Builder builder : plot.getPlotMembers()) {
                            // Score gets split between all participants
                            builder.addScore((int) Math.floor((double) (totalRating/(plot.getPlotMembers().size() + 1))));
                            builder.addCompletedBuild(1);

                            // Remove Slot from Member
                            try {
                                builder.removePlot(builder.getSlot(plot));
                            } catch (Exception ex) {
                                Bukkit.getLogger().log(Level.SEVERE, "Could not remove Plot of builders slot!", ex);
                            }
                        }

                        clickPlayer.sendMessage(sb.toString());
                    }

                    PlotManager.savePlotAsSchematic(plot);
                } else {
                    if (plot.getPlotMembers().size() != 0) {
                        // Plot was made alone
                        clickPlayer.sendMessage("§7>> §aPlot §6#" + plot.getID() + " §aby §6" + plot.getBuilder().getName() + " §ahas been rejected! Send feedback using §6/sendFeedback <ID> <Text> §a!");
                    } else {
                        // Plot was made in a group
                        StringBuilder sb = new StringBuilder("§7>> §aPlot §6#" + plot.getID() + " §aby §6" + plot.getBuilder().getName() + ", ");

                        for (int i = 0; i < plot.getPlotMembers().size(); i++) {
                            sb.append(i == plot.getPlotMembers().size() - 1 ?
                                    plot.getPlotMembers().get(i).getName() + " §ahas been rejected! Send feedback using §6/sendFeedback <ID> <Text> §a!" :
                                    plot.getPlotMembers().get(i).getName() + ", ");
                        }
                        clickPlayer.sendMessage(sb.toString());
                    }

                    PlotHandler.undoSubmit(plot);
                }

                for(Player player : clickPlayer.getWorld().getPlayers()) {
                    player.teleport(Utils.getSpawnPoint());
                }

                if(plot.getBuilder().isOnline()) {
                    PlotHandler.sendFeedbackMessage(Collections.singletonList(plot), plot.getBuilder().getPlayer());
                }
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.FinishPlotSound, 1, 1);
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                ex.printStackTrace();
            } catch (WorldEditException | IOException e) {
                e.printStackTrace();
            }
        });

        // Point Selection
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
}
