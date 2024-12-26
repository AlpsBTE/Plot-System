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
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.CustomHeads;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class SettingsMenu extends AbstractMenu {
    private Consumer<Player> onBack = (player) -> player.performCommand("companion");

    public SettingsMenu(Player player) {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.SETTINGS), player);
    }

    public SettingsMenu(Player player, Consumer<Player> onBack) {
        this(player);
        this.onBack = onBack;
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set language item
        getMenu().getSlot(11).setItem(
                new ItemBuilder(AlpsHeadUtils.getCustomHead(CustomHeads.GLOBE_HEAD.getId()))
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_LANGUAGE), GOLD, BOLD))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_LANGUAGE))
                                .build())
                        .build());

        // Set Plot type item
        getMenu().getSlot(15).setItem(
                new ItemBuilder(AlpsHeadUtils.getCustomHead(CustomHeads.PLOT_TYPE_BUTTON.getId()))
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_PLOT_TYPE), GOLD, BOLD))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_PLOT_TYPE))
                                .build())
                        .build());

        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for language item
        getMenu().getSlot(11).setClickHandler(((clickPlayer, clickInformation) -> clickPlayer.performCommand("language")));

        // Set click event for plot type item
        getMenu().getSlot(15).setClickHandler(((clickPlayer, clickInformation) -> new PlotTypeMenu(clickPlayer)));

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> onBack.accept(clickPlayer));
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
