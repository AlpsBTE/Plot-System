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

import com.alpsbte.plotsystem.core.PlotSystem;
import com.alpsbte.plotsystem.core.config.ConfigPaths;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.utils.Utils;
import com.alpsbte.plotsystem.core.utils.enums.Status;
import com.alpsbte.plotsystem.core.utils.items.MenuItems;
import com.alpsbte.plotsystem.core.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.core.utils.items.builder.LoreBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.logging.Level;

public class PlotActionsMenu extends AbstractMenu {

    private final Plot plot;
    private final boolean hasFeedback;

    public PlotActionsMenu(Player menuPlayer, Plot plot) throws SQLException {
        super(3, "Plot #" + plot.getID() + " | " + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1), menuPlayer);

        this.plot = plot;
        hasFeedback = plot.isReviewed() || plot.isRejected();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plot submit or undo submit item
        try {
            if (plot.getStatus().equals(Status.unreviewed)) {
                getMenu().getSlot(10)
                        .setItem(new ItemBuilder(Material.FIREBALL, 1)
                                .setName("§c§lUndo Submit").setLore(new LoreBuilder()
                                        .addLine("Click to undo your submission.").build())
                                .build());
            } else {
                getMenu().getSlot(10)
                        .setItem(new ItemBuilder(Material.NAME_TAG, 1)
                                .setName("§a§lSubmit").setLore(new LoreBuilder()
                                        .addLines("Click to complete this plot and submit it to be reviewed.",
                                                "",
                                                Utils.getNoteFormat("You won't be able to continue building on this plot!"))
                                        .build())
                                .build());
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            getMenu().getSlot(10).setItem(MenuItems.errorItem());
        }

        // Set teleport to plot item
        getMenu().getSlot(hasFeedback ? 12 : 13)
                .setItem(new ItemBuilder(Material.COMPASS, 1)
                        .setName("§6§lTeleport").setLore(new LoreBuilder()
                                .addLine("Click to teleport to the plot.").build())
                        .build());

        // Set plot abandon item
        getMenu().getSlot(hasFeedback ? 14 : 16)
                .setItem(new ItemBuilder(Material.BARRIER, 1)
                        .setName("§c§lAbandon").setLore(new LoreBuilder()
                                .addLines("Click to reset your plot and to give it to someone else.",
                                        "",
                                        Utils.getNoteFormat("You won't be able to continue building on your plot!"))
                                .build())
                        .build());

        // Set plot feedback item
        if (hasFeedback) {
            getMenu().getSlot(16)
                    .setItem(new ItemBuilder(Material.BOOK_AND_QUILL)
                            .setName("§b§lFeedback").setLore(new LoreBuilder()
                                    .addLine("Click to view your plot review feedback.").build())
                            .build());
        }

        // Set plot members item
        try {
            if (!plot.isReviewed()) {
                FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getConfig();
                if ((getMenuPlayer() == plot.getPlotOwner().getPlayer() || getMenuPlayer().hasPermission("plotsystem.admin")) && config.getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)) {
                    getMenu().getSlot(22)
                            .setItem(new ItemBuilder(Utils.getItemHead(Utils.CustomHead.ADD_BUTTON))
                                    .setName("§b§lManage Members").setLore(new LoreBuilder()
                                            .addLines("Click to open your Plot Member menu, where you can add",
                                                    "and remove other players on your plot.",
                                                    "",
                                                    Utils.getNoteFormat("Score will be split between all members when reviewed!"))
                                            .build())
                                    .build());
                } else if (plot.getPlotMembers().stream().anyMatch(m -> m.getUUID().equals(getMenuPlayer().getUniqueId()))) {
                    getMenu().getSlot(22)
                            .setItem(new ItemBuilder(Utils.getItemHead(Utils.CustomHead.REMOVE_BUTTON))
                                    .setName("§b§lLeave Plot").setLore(new LoreBuilder()
                                            .addLines("Click to leave this plot.",
                                                    "",
                                                    Utils.getNoteFormat("You will no longer be able to continue build or get any score on it!"))
                                            .build())
                                    .build());
                }
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for submit or undo submit plot item
        getMenu().getSlot(10).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            try {
                clickPlayer.performCommand("plot " + (plot.getStatus().equals(Status.unreviewed) ? "undosubmit " : "submit ") + plot.getID());
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });

        // Set click event for teleport to plot item
        getMenu().getSlot(hasFeedback ? 12 : 13).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            plot.getWorld().teleportPlayer(clickPlayer);
        });

        // Set click event for abandon plot item
        getMenu().getSlot(hasFeedback ? 14 : 16).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("plot abandon " + plot.getID());
        });

        // Set click event for feedback menu button
        if(hasFeedback) {
            getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
                clickPlayer.closeInventory();
                clickPlayer.performCommand("plot feedback " + plot.getID());
            });
        }

        // Set click event for plot members item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> {
            try {
                if (!plot.isReviewed()) {
                    if (plot.getStatus() == Status.unfinished) {
                        FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getConfig();
                        if ((getMenuPlayer() == plot.getPlotOwner().getPlayer() || getMenuPlayer().hasPermission("plotsystem.admin")) && config.getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)) {
                            clickPlayer.closeInventory();
                            new PlotMemberMenu(plot,clickPlayer);
                        } else if (plot.getPlotMembers().stream().anyMatch(m -> m.getUUID().equals(getMenuPlayer().getUniqueId()))) {
                            // Leave Plot
                            plot.removePlotMember(new Builder(clickPlayer.getUniqueId()));
                            clickPlayer.sendMessage(Utils.getInfoMessageFormat("Left plot #" + plot.getID() + "!"));
                            clickPlayer.closeInventory();
                        }
                    } else {
                        clickPlayer.closeInventory();
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat("You can only manage plot members if plot is unfinished!"));
                    }
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
    }
}
