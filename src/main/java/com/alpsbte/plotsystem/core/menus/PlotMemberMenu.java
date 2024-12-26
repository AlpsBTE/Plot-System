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
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.chat.ChatInput;
import com.alpsbte.plotsystem.utils.chat.PlayerInviteeChatInput;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.CustomHeads;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class PlotMemberMenu extends AbstractMenu {
    private final Plot plot;

    private final ItemStack emptyMemberSlotItem = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE, 1).setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.GroupSystem.EMPTY_MEMBER_SLOTS), DARK_GREEN).decoration(BOLD, true)).build();
    private List<Builder> builders;

    public PlotMemberMenu(Plot plot, Player menuPlayer) {
        super(3, LangUtil.getInstance().get(menuPlayer, LangPaths.MenuTitle.MANAGE_MEMBERS) + " | " + LangUtil.getInstance().get(menuPlayer, LangPaths.Plot.PLOT_NAME) + " #" + plot.getID(), menuPlayer);
        this.plot = plot;
    }

    @Override
    protected void setPreviewItems() {
        // Set loading item for plot owner item
        getMenu().getSlot(10).setItem(MenuItems.loadingItem(Material.PLAYER_HEAD, getMenuPlayer()));

        // Set loading item for plot member items
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            try {
                List<Builder> plotMembers = plot.getPlotMembers();
                for (int i = 1; i <= 3; i++) {
                    getMenu().getSlot(11 + i).setItem(plotMembers.size() >= i
                            ? MenuItems.loadingItem(Material.PLAYER_HEAD, getMenuPlayer())
                            : emptyMemberSlotItem);
                }
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        });

        // Set add plot member item
        ItemStack whitePlus = AlpsHeadUtils.getCustomHead(CustomHeads.ADD_BUTTON.getId());
        getMenu().getSlot(16)
                .setItem(new ItemBuilder(whitePlus)
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.ADD_MEMBER_TO_PLOT), GOLD, BOLD))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.ADD_MEMBER_TO_PLOT), true)
                                .emptyLine()
                                .addLine(Utils.ItemUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.PLAYER_HAS_TO_BE_ONLINE)))
                                .build())
                        .build());

        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plot owner item
        try {
            getMenu().getSlot(10)
                    .setItem(new ItemBuilder(AlpsHeadUtils.getPlayerHead(plot.getPlotOwner().getUUID()))
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.OWNER), GOLD, BOLD))
                            .setLore(new LoreBuilder()
                                    .addLine(plot.getPlotOwner().getName()).build())
                            .build());
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }

        // Set plot member items
        try {
            builders = plot.getPlotMembers();
            for (int i = 12; i < 15; i++) {
                if (builders.size() < (i - 11)) return;

                Builder builder = builders.get(i - 12);
                getMenu().getSlot(i)
                        .setItem(new ItemBuilder(AlpsHeadUtils.getPlayerHead(builder.getUUID()))
                                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.MEMBER), AQUA, BOLD))
                                .setLore(new LoreBuilder()
                                        .addLines(text(builder.getName()),
                                                empty(),
                                                text(Utils.ItemUtils.getActionFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.Action.CLICK_TO_REMOVE_PLOT_MEMBER))))
                                        .build())
                                .build());
            }
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for member slots
        for (int i = 12; i < 15; i++) {
            int itemSlot = i;
            getMenu().getSlot(i).setClickHandler((clickPlayer, clickInformation) -> {
                if (getMenu().getSlot(itemSlot).getItem(clickPlayer).equals(emptyMemberSlotItem)) return;
                Builder builder = builders.get(itemSlot - 12);
                try {
                    plot.removePlotMember(builder);
                    clickPlayer.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(),
                            LangPaths.Message.Info.REMOVED_PLOT_MEMBER, builder.getName(), Integer.toString(plot.getID()))));
                } catch (SQLException ex) {
                    PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
                }
                reloadMenuAsync();
            });
        }

        // Set click event for add plot member item
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            ChatInput.awaitChatInput.put(clickPlayer.getUniqueId(), new PlayerInviteeChatInput(clickPlayer.getUniqueId(), plot));
            PlayerInviteeChatInput.sendChatInputMessage(clickPlayer);
        });

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> {
            try {
                new PlotActionsMenu(clickPlayer, plot);
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(empty()).build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }
}