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

import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.MenuItems;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

public class BuilderUtilitiesMenu extends AbstractMenu {

    public BuilderUtilitiesMenu(Player player) {
        super(3, "Builder Utilities", player);

        if(PlotManager.isPlotWorld(player.getWorld())) {
            Mask mask = BinaryMask.builder(getMenu())
                    .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                    .pattern("111111111")
                    .pattern("000000000")
                    .pattern("111111111")
                    .build();
            mask.apply(getMenu());

            addMenuItems();
            setItemClickEvents();

            getMenu().open(getMenuPlayer());
        } else {
            player.sendMessage(Utils.getErrorMessageFormat("You need to be on a plot in order to use this!"));
        }
    }

    @Override
    protected void addMenuItems() {
        // Add custom heads item
        getMenu().getSlot(10)
                .setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                        .setName("§b§lCUSTOM HEADS")
                        .setLore(new LoreBuilder()
                                .addLine("Open the head menu to get a variety of custom heads.").build())
                        .build());

        // Add banner maker item
        getMenu().getSlot(13)
                .setItem(new ItemBuilder(Material.BANNER, 1, (byte) 14)
                        .setName("§b§lBANNER MAKER")
                        .setLore(new LoreBuilder()
                                .addLine("Open the banner maker menu to create your own custom banners.").build())
                        .build());

        // Add special blocks menu item
        getMenu().getSlot(16).setItem(SpecialBlocksMenu.getMenuItem());

        // Add back button item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem());
    }

    @Override
    protected void setItemClickEvents() {
        // Set click event for custom heads
        getMenu().getSlot(10).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("hdb"));

        // Set click event for banner maker
        getMenu().getSlot(13).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("bm"));

        // Set click event for special blocks
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> new SpecialBlocksMenu(clickPlayer));

        // Set click event for back button
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("companion"));
    }

    public static ItemStack getMenuItem() {
        return new ItemBuilder(Material.GOLD_AXE)
                .setName("§b§lBuilder Utilities")
                .setLore(new LoreBuilder()
                        .addLine("Get access to custom heads, banners and special blocks.").build())
                .build();
    }
}
