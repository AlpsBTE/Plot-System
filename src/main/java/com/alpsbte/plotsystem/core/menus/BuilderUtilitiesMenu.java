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

import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.utils.items.builder.LoreBuilder;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

public class BuilderUtilitiesMenu extends AbstractMenu {

    public BuilderUtilitiesMenu(Player player) {
        super(3, LangUtil.get(player, LangPaths.MenuTitle.BUILDER_UTILITIES), player);

        if(!PlotManager.isPlotWorld(player.getWorld())) {
            player.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(player, LangPaths.Message.Error.PLAYER_NEEDS_TO_BE_ON_PLOT)));
            player.closeInventory();
        }
    }

    @Override
    protected void setPreviewItems() {
        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem());
        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set custom-heads menu item
        getMenu().getSlot(10)
                .setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                        .setName("§b§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.CUSTOM_HEADS).toUpperCase())
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.CUSTOM_HEADS)).build())
                        .build());

        // Set banner-maker menu item
        getMenu().getSlot(13)
                .setItem(new ItemBuilder(Material.BANNER, 1, (byte) 14)
                        .setName("§b§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.BANNER_MAKER).toUpperCase())
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.BANNER_MAKER)).build())
                        .build());

        // Set special-blocks menu item
        getMenu().getSlot(16).setItem(SpecialBlocksMenu.getMenuItem());
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for custom-heads menu item
        getMenu().getSlot(10).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("hdb"));

        // Set click event for banner-maker menu item
        getMenu().getSlot(13).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("bm"));

        // Set click event for special-blocks menu item
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> new SpecialBlocksMenu(clickPlayer));

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("companion"));
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

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(Material.GOLD_AXE)
                .setName("§b§lBuilder Utilities")
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.get(player, LangPaths.MenuDescription.BUILDER_UTILITIES)).build()) //TODO:
                .build();
    }
}
