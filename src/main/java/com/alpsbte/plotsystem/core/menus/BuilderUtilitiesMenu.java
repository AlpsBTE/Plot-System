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

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class BuilderUtilitiesMenu extends AbstractMenu {

    public BuilderUtilitiesMenu(Player player) {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.BUILDER_UTILITIES), player);

        if (!PlotUtils.isPlotWorld(player.getWorld())) {
            player.closeInventory();
            player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.PLAYER_NEEDS_TO_BE_ON_PLOT)));
        }
    }

    @Override
    protected void setPreviewItems() {
        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set custom-heads menu item
        getMenu().getSlot(10)
                .setItem(new ItemBuilder(Material.PLAYER_HEAD, 1)
                        .setName(Component.text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.CUSTOM_HEADS), GOLD, BOLD))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.CUSTOM_HEADS), true).build())
                        .build());

        // Set banner-maker menu item
        getMenu().getSlot(13)
                .setItem(new ItemBuilder(Material.RED_BANNER, 1)
                        .setName(Component.text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.BANNER_MAKER), GOLD, BOLD))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.BANNER_MAKER), true).build())
                        .build());

        // Set special-blocks menu item
        getMenu().getSlot(16).setItem(SpecialToolsMenu.getMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for custom-heads menu item
        getMenu().getSlot(10).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("hdb"));

        // Set click event for banner-maker menu item
        getMenu().getSlot(13).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("bm"));

        // Set click event for special-blocks menu item
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> new SpecialToolsMenu(clickPlayer));

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("companion"));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(Component.empty()).build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(Material.GOLDEN_AXE)
                .setName(Component.text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.BUILDER_UTILITIES), AQUA, BOLD))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(player, LangPaths.MenuDescription.BUILDER_UTILITIES), true).build())
                .build();
    }
}
