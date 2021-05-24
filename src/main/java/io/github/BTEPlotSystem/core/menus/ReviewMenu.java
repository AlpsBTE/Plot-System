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

import github.BTEPlotSystem.core.system.Review;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotHandler;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.*;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class ReviewMenu implements Listener {
    private Inventory reviewMenu;
    private Inventory reviewPlotMenu;

    //Review Inventory
    private ItemStack itemArrowLeft;
    private ItemStack itemArrowRight;
    private ItemStack itemClose;

    //Review Plot Inventory
    private ItemStack itemMap;
    private ItemStack itemCancel;
    private ItemStack itemSubmit;

    private final ItemStack[] itemCategory = new ItemStack[4];
    private final ItemStack[] itemPointZero = new ItemStack[4];
    private final ItemStack[] itemPointOne = new ItemStack[4];
    private final ItemStack[] itemPointTwo = new ItemStack[4];
    private final ItemStack[] itemPointThree = new ItemStack[4];
    private final ItemStack[] itemPointFour = new ItemStack[4];
    private final ItemStack[] itemPointFive = new ItemStack[4];

    private final HashMap<Plot, ItemStack> plotItems = new HashMap<>();
    private Plot selectedPlot;
    private boolean sentWarning;

    private final Player player;

    public ReviewMenu(Player player) throws SQLException {
        //Opens Review Menu, showing all plots in the given round.
        reviewMenu = Bukkit.createInventory(player, 54, "Review & Manage Plots");
        this.player = player;

        ItemStack plotLoadError = new ItemBuilder(Material.BARRIER, 1)
                .setName("§cCould not load plot")
                .setLore(new LoreBuilder()
                        .addLine("Please contact a Manager or Developer!").build())
                .build();

        List<Plot> plots = PlotManager.getPlots(Status.unreviewed);
        plots.addAll(PlotManager.getPlots(Status.unfinished));

        int counter = 0;
        for (Plot plot : plots) {
            try {
                if(counter <= 45) {
                    switch (plot.getStatus()) {
                        case unfinished:
                            plotItems.put(plot, new ItemBuilder(Material.WOOL, 1, (byte) 1)
                                    .setName("§b§lManage Plot")
                                    .setLore(new LoreBuilder()
                                           .addLines("ID: §f" + plot.getID(),
                                                     "",
                                                     "§7Builder: §f" + plot.getBuilder().getName(),
                                                     "§7City: §f" + plot.getCity().getName(),
                                                     "§7Difficulty: §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase())
                                            .build())
                                    .build());
                            break;
                        case unreviewed:
                            plotItems.put(plot, new ItemBuilder(Material.MAP, 1)
                                    .setName("§b§lReview Plot")
                                    .setLore(new LoreBuilder()
                                            .addLines("ID: §f" + plot.getID(),
                                                    "",
                                                    "§7Builder: §f" + plot.getBuilder().getName(),
                                                    "§7City: §f" + plot.getCity().getName(),
                                                    "§7Difficulty: §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase())
                                            .build())
                                    .build());
                            break;
                        default:
                            reviewMenu.setItem(counter, plotLoadError);
                            return;
                    }
                    reviewMenu.setItem(counter, plotItems.get(plot));
                }
            } catch (Exception e) {
                reviewMenu.setItem(counter, plotLoadError);
                e.printStackTrace();
            }
            counter++;
        }

        for (int i = 45; i < 54; i++) {
            switch (i) {
                case 46:
                    itemArrowLeft = new ItemBuilder(Material.ARROW, 1)
                            .setName("§6§lPrevious Page")
                            .setLore(new LoreBuilder()
                                    .addLine("Show the previous page").build())
                            .build();
                    reviewMenu.setItem(i, itemArrowLeft);
                    break;
                case 49:
                    itemClose = new ItemBuilder(Material.BARRIER, 1)
                            .setName("§c§lCLOSE")
                            .setLore(new LoreBuilder()
                                    .addLine("Close the menu").build())
                            .build();
                    reviewMenu.setItem(i, itemClose);
                    break;
                case 52:
                    itemArrowRight = new ItemBuilder(Material.ARROW, 1)
                            .setName("§6§lNext Page")
                            .setLore(new LoreBuilder()
                                    .addLine("Show the next page").build())
                            .build();
                    reviewMenu.setItem(i, itemArrowRight);
                    break;
                default:
                    reviewMenu.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build());
            }
        }
        player.openInventory(reviewMenu);
    }

    public ReviewMenu(Player player, Plot plot) {
        this.player = player;
        selectedPlot = plot;

        try {
            if (selectedPlot.getBuilder().getUUID().equals(player.getUniqueId())){
                player.sendMessage(Utils.getErrorMessageFormat("You cannot review your own builds!"));
                return;
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }

        ReviewPlot(plot);
    }

    private void ReviewPlot(Plot plot) {
        reviewPlotMenu = Bukkit.createInventory(player, 54, "Review Plot #" + plot.getID());

        for (int i = 0; i < 54; i++) {
            reviewPlotMenu.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build());

            switch (i) {
                case 4:
                    try {
                        itemMap = new ItemBuilder(Material.MAP, 1)
                                .setName("§b§lReview Plot")
                                .setLore(new LoreBuilder()
                                        .addLines("ID: §f" + plot.getID(),
                                                  "",
                                                  "§7Builder: §f" + plot.getBuilder().getName(),
                                                  "§7City: §f" + plot.getCity().getName(),
                                                  "§7Difficulty: §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase())
                                        .build())
                                .build();
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        reviewPlotMenu.setItem(i, MenuItems.errorItem());
                    }
                    reviewPlotMenu.setItem(i, itemMap);
                    break;
                case 10:
                    itemCategory[0] = new ItemBuilder(Material.ARROW, 1)
                            .setName("§a§lACCURACY")
                            .setLore(new LoreBuilder()
                                    .addLines("How accurate is the building?",
                                              "",
                                              "- Looks like in RL",
                                              "- Correct outlines",
                                              "- Correct height",
                                              "- Is completed")
                                    .build())
                            .build();
                    reviewPlotMenu.setItem(i, itemCategory[0]);
                    break;
                case 19:
                    itemCategory[1] = new ItemBuilder(Material.PAINTING, 1)
                            .setName("§a§lBLOCK PALETTE")
                            .setLore(new LoreBuilder()
                                    .addLines("How many different blocks are used and how creative are they?",
                                              "",
                                              "- Choice of block colours/textures",
                                              "- Random blocks")
                                    .build())
                            .build();
                    reviewPlotMenu.setItem(i, itemCategory[1]);
                    break;
                case 28:
                    itemCategory[2] = new ItemBuilder(Material.EYE_OF_ENDER, 1)
                            .setName("§a§lDETAILING")
                            .setLore(new LoreBuilder()
                                    .addLines("How much detail does the building have?",
                                              "",
                                              "- Roof details",
                                              "- Details on the facades",
                                              "- Heads and Banners")
                                    .build())
                            .build();
                    reviewPlotMenu.setItem(i, itemCategory[2]);
                    break;
                case 37:
                    itemCategory[3] = new ItemBuilder(Material.WOOD_AXE, 1)
                            .setName("§a§lTECHNIQUE")
                            .setLore(new LoreBuilder()
                                    .addLines("What building techniques have been used and how creative are they?",
                                              "",
                                              "- WorldEdit",
                                              "- Used Special Blocks")
                                    .build())
                            .build();
                    reviewPlotMenu.setItem(i, itemCategory[3]);
                    break;
                case 48:
                    itemSubmit = new ItemBuilder(Material.CONCRETE, 1, (byte) 13)
                            .setName("§a§lSUBMIT")
                            .setLore(new LoreBuilder()
                                    .addLine("Submit selected points and mark plot as reviewed").build())
                            .build();
                    reviewPlotMenu.setItem(i, itemSubmit);
                    break;
                case 50:

                    itemCancel = new ItemBuilder(Material.CONCRETE, 1, (byte) 14)
                            .setName("§c§lCANCEL")
                            .setLore(new LoreBuilder()
                                    .addLine("Close the menu").build())
                            .build();
                    reviewPlotMenu.setItem(i, itemCancel);
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
                            reviewPlotMenu.setItem(i, itemPointZero[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 4) {
                            itemPointOne[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 1, (byte) 14)
                                    .setName("§l§c1 Point")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();

                            reviewPlotMenu.setItem(i, itemPointOne[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 5) {
                            itemPointTwo[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 2, (byte) 1)
                                    .setName("§l§62 Points")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();
                            reviewPlotMenu.setItem(i, itemPointTwo[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 6) {
                            itemPointThree[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 3, (byte) 4)
                                    .setName("§l§e3 Points")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();
                            reviewPlotMenu.setItem(i, itemPointThree[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 7) {
                            itemPointFour[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 4, (byte) 13)
                                    .setName("§l§24 Points")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();
                            reviewPlotMenu.setItem(i, itemPointFour[(i - (i + 1) % 9) / 54]);
                        } else if ((i + 1) % 9 == 8) {
                            itemPointFive[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 5, (byte) 5)
                                    .setName("§l§a5 Points")
                                    .setLore(new LoreBuilder()
                                            .addLine("Click to select").build())
                                    .build();
                            reviewPlotMenu.setItem(i, itemPointFive[(i - (i + 1) % 9) / 54]);
                        }
                    }
                    break;
            }
        }
        player.openInventory(reviewPlotMenu);
    }

    @EventHandler
    public void onPlayerInventoryClickEvent(InventoryClickEvent event) {
        try {
            if (event.getWhoClicked().equals(player)) {
                if (event.getClickedInventory() != null && event.getClickedInventory().equals(reviewMenu)) {

                    if (event.getCurrentItem().equals(itemClose)) {
                        event.getWhoClicked().closeInventory();

                    } else if (event.getCurrentItem().equals(itemArrowLeft)) {

                    } else if (event.getCurrentItem().equals(itemArrowRight)) {
                    }

                    for (ItemStack plotItem : plotItems.values()) {
                        if (event.getCurrentItem().equals(plotItem)) {
                            selectedPlot = getPlotByValue(plotItem);
                            if (selectedPlot.getStatus() == Status.unreviewed) {
                                event.getWhoClicked().closeInventory();
                                if (!selectedPlot.getBuilder().getUUID().toString().equals(event.getWhoClicked().getUniqueId().toString())){
                                    PlotHandler.teleportPlayer(selectedPlot, player);
                                } else {
                                    event.getWhoClicked().sendMessage(Utils.getErrorMessageFormat("You cannot review your own builds!"));
                                }
                            } else {
                                event.getWhoClicked().closeInventory();
                                new PlotActionsMenu(player, selectedPlot);
                            }
                        }
                    }
                    event.setCancelled(true);
                } else if (event.getClickedInventory().equals(reviewPlotMenu)) {
                    if (event.getCurrentItem().equals(itemCancel)) {
                        event.getWhoClicked().closeInventory();
                    } else if (event.getCurrentItem().equals(itemSubmit)) {
                        StringBuilder score = new StringBuilder();

                        int totalRating = 0;
                        boolean isRejected = false;

                        for (int i = 0; i < 4; i++) {
                            for (int j = 0; j < 6; j++) {
                                if (reviewPlotMenu.getItem(11 + (i * 9) + j).getItemMeta().hasEnchant(Enchantment.ARROW_DAMAGE)) {
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
                            player.sendMessage(Utils.getInfoMessageFormat("§c§lWARNING: §cThis plot will automatically get abandoned!"));
                            player.playSound(player.getLocation(), Utils.CreatePlotSound, 1, 1);
                            sentWarning = true;
                            event.setCancelled(true);
                            return;
                        } else if(isRejected && !sentWarning) {
                            player.sendMessage(Utils.getInfoMessageFormat("§c§lWARNING: §cThis plot will get rejected!"));
                            player.playSound(player.getLocation(), Utils.CreatePlotSound, 1, 1);
                            sentWarning = true;
                            event.setCancelled(true);
                            return;
                        } else if(totalRating == 0) {
                            PlotHandler.abandonPlot(selectedPlot, false);
                            return;
                        }

                        if(selectedPlot.isReviewed()) {
                            selectedPlot.getReview().setRating(score.toString());
                            selectedPlot.getReview().setReviewer(player.getUniqueId());
                        } else {
                            new Review(selectedPlot.getID(), player.getUniqueId(), score.toString());
                        }

                        double totalRatingWithMultiplier = totalRating * PlotManager.getMultiplierByDifficulty(selectedPlot.getDifficulty());
                        totalRating = (int) Math.floor(totalRatingWithMultiplier);
                        selectedPlot.setScore(totalRating);

                        if (!isRejected){
                            player.sendMessage("§7>> §aPlot §6#" + selectedPlot.getID() + " §aby §6" + selectedPlot.getBuilder().getName() + " §amarked as reviewed!");

                            selectedPlot.getReview().setFeedbackSent(false);
                            selectedPlot.getReview().setFeedback("No Feedback");
                            selectedPlot.setStatus(Status.complete);
                            selectedPlot.getBuilder().addScore(totalRating);
                            selectedPlot.getBuilder().addCompletedBuild(1);
                            try {
                                selectedPlot.getBuilder().removePlot(selectedPlot.getSlot());
                            } catch (Exception ex) {
                                Bukkit.getLogger().log(Level.SEVERE, "Could not remove Plot of builders slot!", ex);
                            }

                            PlotManager.savePlotAsSchematic(selectedPlot);
                        } else {
                            player.sendMessage("§7>> §aPlot §6#" + selectedPlot.getID() + " §aby §6" + selectedPlot.getBuilder().getName() + " §ahas been rejected! Send feedback using §6/sendFeedback <ID> <Text> §a!");

                            PlotHandler.undoSubmit(selectedPlot);
                        }

                        for(Player player : player.getWorld().getPlayers()) {
                            player.teleport(Utils.getSpawnPoint());
                        }

                        if(selectedPlot.getBuilder().isOnline()) {
                            PlotHandler.sendFeedbackMessage(Collections.singletonList(selectedPlot), selectedPlot.getBuilder().getPlayer());
                        }
                        player.playSound(player.getLocation(), Utils.FinishPlotSound, 1, 1);
                    } else if (event.getCurrentItem().equals(itemMap)) {
                        event.getWhoClicked().closeInventory();
                        new PlotActionsMenu(player, selectedPlot);
                    } else {
                        int slot = event.getSlot();
                        int column = (slot % 9) + 1;
                        int row = (slot - (slot % 9)) / 9 + 1;

                        ItemMeta meta = event.getCurrentItem().getItemMeta();

                        if (column > 2 && column < 9 && row > 1 && row < 6) {
                            //if (meta.hasEnchant(Enchantment.ARROW_DAMAGE)) {
                            //    meta.removeEnchant(Enchantment.ARROW_DAMAGE);
                            //} else {
                                //Go through the whole points row
                                for (int i = 0; i < 6; i++) {
                                    if (reviewPlotMenu.getItem(slot - (column - 1) + i + 2).getItemMeta().hasEnchant(Enchantment.ARROW_DAMAGE)) {
                                        ItemMeta metaPrevious = reviewPlotMenu.getItem(slot - (column - 1) + i + 2).getItemMeta();
                                        metaPrevious.removeEnchant(Enchantment.ARROW_DAMAGE);
                                        reviewPlotMenu.getItem(slot - (column - 1) + i + 2).setItemMeta(metaPrevious);
                                    }
                                }

                                meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
                            //}
                            event.getCurrentItem().setItemMeta(meta);
                            reviewPlotMenu.setItem(slot, event.getCurrentItem());
                            sentWarning = false;
                        }
                    }
                    event.setCancelled(true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Plot getPlotByValue(ItemStack item) {
        for (Map.Entry entry : plotItems.entrySet()) {
            if (entry.getValue().equals(item)) {
                return (Plot) entry.getKey();
            }
        }
        return null;
    }

    public static ItemStack getMenuItem(){
        return new ItemBuilder(Material.BOOK, 1)
                .setName("§b§lReview Plots §7(Right Click)")
                .setEnchantment(true)
                .build();
    }
}
