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

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
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
        super(3, LangUtil.getInstance().get(menuPlayer, LangPaths.Plot.PLOT_NAME) + " #" + plot.getID() + " | " + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1), menuPlayer);

        this.plot = plot;
        hasFeedback = plot.isReviewed() || plot.isRejected();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plot submit or undo submit item
        try {
            if (plot.getStatus().equals(Status.unreviewed)) {
                getMenu().getSlot(10)
                        .setItem(new ItemBuilder(Material.FIRE_CHARGE, 1)
                                .setName("§c§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.UNDO_SUBMIT)).setLore(new LoreBuilder()
                                        .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.UNDO_SUBMIT)).build())
                                .build());
            } else {
                getMenu().getSlot(10)
                        .setItem(new ItemBuilder(Material.NAME_TAG, 1)
                                .setName("§a§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SUBMIT)).setLore(new LoreBuilder()
                                        .addLines(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SUBMIT_PLOT),
                                                "",
                                                Utils.ChatUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.WONT_BE_ABLE_CONTINUE_BUILDING)))
                                        .build())
                                .build());
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            getMenu().getSlot(10).setItem(MenuItems.errorItem(getMenuPlayer()));
        }

        // Set teleport to plot item
        getMenu().getSlot(hasFeedback ? 12 : 13)
                .setItem(new ItemBuilder(Material.COMPASS, 1)
                        .setName("§6§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.TELEPORT)).setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.TELEPORT)).build())
                        .build());

        // Set plot abandon item
        getMenu().getSlot(hasFeedback ? 14 : 16)
                .setItem(new ItemBuilder(Material.BARRIER, 1)
                        .setName("§c§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.ABANDON)).setLore(new LoreBuilder()
                                .addLines(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.ABANDON),
                                        "",
                                        Utils.ChatUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.WONT_BE_ABLE_CONTINUE_BUILDING)))
                                .build())
                        .build());

        // Set plot feedback item
        if (hasFeedback) {
            getMenu().getSlot(16)
                    .setItem(new ItemBuilder(Material.WRITABLE_BOOK)
                            .setName("§b§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.FEEDBACK)).setLore(new LoreBuilder()
                                    .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.FEEDBACK)).build())
                            .build());
        }

        // Set plot members item
        try {
            if (!plot.isReviewed()) {
                FileConfiguration config = PlotSystem.getPlugin().getConfig();
                if ((getMenuPlayer() == plot.getPlotOwner().getPlayer() || getMenuPlayer().hasPermission("plotsystem.admin")) && config.getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)) {
                    getMenu().getSlot(22)
                            .setItem(new ItemBuilder(AlpsUtils.getItemHead(Utils.HeadUtils.ADD_BUTTON_HEAD))
                                    .setName("§b§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.MANAGE_MEMBERS)).setLore(new LoreBuilder()
                                            .addLines(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.MANAGE_MEMBERS),
                                                    "",
                                                    Utils.ChatUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.SCORE_WILL_BE_SPLIT)))
                                            .build())
                                    .build());
                } else if (plot.getPlotMembers().stream().anyMatch(m -> m.getUUID().equals(getMenuPlayer().getUniqueId()))) {
                    getMenu().getSlot(22)
                            .setItem(new ItemBuilder(AlpsUtils.getItemHead(Utils.HeadUtils.REMOVE_BUTTON_HEAD))
                                    .setName("§b§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.LEAVE_PLOT)).setLore(new LoreBuilder()
                                            .addLines(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.LEAVE_PLOT),
                                                    "",
                                                    Utils.ChatUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.WONT_BE_ABLE_CONTINUE_BUILDING)))
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
                        FileConfiguration config = PlotSystem.getPlugin().getConfig();
                        if ((getMenuPlayer() == plot.getPlotOwner().getPlayer() || getMenuPlayer().hasPermission("plotsystem.admin")) && config.getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)) {
                            clickPlayer.closeInventory();
                            new PlotMemberMenu(plot,clickPlayer);
                        } else if (plot.getPlotMembers().stream().anyMatch(m -> m.getUUID().equals(getMenuPlayer().getUniqueId()))) {
                            // Leave Plot
                            plot.removePlotMember(Builder.byUUID(clickPlayer.getUniqueId()));
                            clickPlayer.sendMessage(Utils.ChatUtils.getInfoMessageFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.LEFT_PLOT, Integer.toString(plot.getID()))));
                            clickPlayer.closeInventory();
                        }
                    } else {
                        clickPlayer.closeInventory();
                        clickPlayer.sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.CAN_ONLY_MANAGE_MEMBERS_UNFINISHED)));
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
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
    }
}
