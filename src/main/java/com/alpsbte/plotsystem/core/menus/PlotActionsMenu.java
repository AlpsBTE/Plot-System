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
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.CustomHeads;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class PlotActionsMenu extends AbstractMenu {
    private final Plot plot;
    private final boolean hasFeedback;

    public PlotActionsMenu(Player menuPlayer, Plot plot) {
        super(3, LangUtil.getInstance().get(menuPlayer, LangPaths.Plot.PLOT_NAME) + " #" + plot.getID() + " | " + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1), menuPlayer);

        this.plot = plot;
        hasFeedback = plot.isReviewed() || plot.isRejected();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plot submit or undo submit item
        if (plot.getStatus().equals(Status.unreviewed)) {
            getMenu().getSlot(10)
                    .setItem(new ItemBuilder(Material.FIRE_CHARGE, 1)
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.UNDO_SUBMIT), RED).decoration(BOLD, true))
                            .setLore(new LoreBuilder()
                                    .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.UNDO_SUBMIT), true).build())
                            .build());
        } else {
            getMenu().getSlot(10)
                    .setItem(new ItemBuilder(Material.NAME_TAG, 1)
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SUBMIT), GREEN).decoration(BOLD, true))
                            .setLore(new LoreBuilder()
                                    .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SUBMIT_PLOT)).color(GRAY), true)
                                    .emptyLine()
                                    .addLine(Utils.ItemUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.WONT_BE_ABLE_CONTINUE_BUILDING)))
                                    .build())
                            .build());
        }

        // Set teleport to plot item
        getMenu().getSlot(hasFeedback ? 12 : 13)
                .setItem(new ItemBuilder(Material.COMPASS, 1)
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.TELEPORT), GOLD).decoration(BOLD, true))
                        .setLore(new LoreBuilder().addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.TELEPORT)).color(GRAY), true).build())
                        .build());

        // Set plot abandon item
        getMenu().getSlot(hasFeedback ? 14 : 16)
                .setItem(new ItemBuilder(Material.BARRIER, 1)
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.ABANDON), RED).decoration(BOLD, true))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.ABANDON), true)
                                .emptyLine()
                                .addLine(Utils.ItemUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.WONT_BE_ABLE_CONTINUE_BUILDING)))
                                .build())
                        .build());

        // Set plot feedback item
        if (hasFeedback) {
            getMenu().getSlot(16)
                    .setItem(new ItemBuilder(Material.WRITABLE_BOOK)
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.FEEDBACK), AQUA).decoration(BOLD, true))
                            .setLore(new LoreBuilder().addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.FEEDBACK), true).build())
                            .build());
        }

        // Set plot members item
        if (!plot.isReviewed()) {
            FileConfiguration config = PlotSystem.getPlugin().getConfig();
            if ((getMenuPlayer() == plot.getPlotOwner().getPlayer() || getMenuPlayer().hasPermission("plotsystem.admin")) && config.getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)) {
                getMenu().getSlot(22)
                        .setItem(new ItemBuilder(AlpsHeadUtils.getCustomHead(CustomHeads.ADD_BUTTON.getId()))
                                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.MANAGE_MEMBERS), AQUA).decoration(BOLD, true))
                                .setLore(new LoreBuilder()
                                        .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.MANAGE_MEMBERS), true)
                                        .emptyLine()
                                        .addLine(Utils.ItemUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.SCORE_WILL_BE_SPLIT)))
                                        .build())
                                .build());
            } else if (plot.getPlotMembers().stream().anyMatch(m -> m.getUUID().equals(getMenuPlayer().getUniqueId()))) {
                getMenu().getSlot(22)
                        .setItem(new ItemBuilder(AlpsHeadUtils.getCustomHead(CustomHeads.REMOVE_BUTTON.getId()))
                                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.LEAVE_PLOT), AQUA).decoration(BOLD, true))
                                .setLore(new LoreBuilder()
                                        .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.LEAVE_PLOT), true)
                                        .emptyLine()
                                        .addLine(Utils.ItemUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.WONT_BE_ABLE_CONTINUE_BUILDING)))
                                        .build())
                                .build());
            }
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for submit or undo submit plot item
        getMenu().getSlot(10).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("plot " + (plot.getStatus().equals(Status.unreviewed) ? "undosubmit " : "submit ") + plot.getID());
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
        if (hasFeedback) {
            getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
                clickPlayer.closeInventory();
                clickPlayer.performCommand("plot feedback " + plot.getID());
            });
        }

        // Set click event for plot members item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> {
            if (plot.isReviewed()) return;
            if (plot.getStatus() != Status.unfinished) {
                clickPlayer.closeInventory();
                clickPlayer.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.CAN_ONLY_MANAGE_MEMBERS_UNFINISHED)));
                return;
            }

            FileConfiguration config = PlotSystem.getPlugin().getConfig();
            if ((getMenuPlayer() == plot.getPlotOwner().getPlayer() || getMenuPlayer().hasPermission("plotsystem.admin")) && config.getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)) {
                new PlotMemberMenu(plot, clickPlayer);
            } else if (plot.getPlotMembers().stream().anyMatch(m -> m.getUUID().equals(getMenuPlayer().getUniqueId()))) {
                // Leave Plot
                clickPlayer.closeInventory();
                plot.removePlotMember(Builder.byUUID(clickPlayer.getUniqueId()));
                clickPlayer.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.LEFT_PLOT, Integer.toString(plot.getID()))));
            }
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(Component.empty()).build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
    }
}
