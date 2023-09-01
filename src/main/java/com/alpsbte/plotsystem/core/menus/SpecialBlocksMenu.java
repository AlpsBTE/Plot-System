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
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.items.SpecialBlocks;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

public class SpecialBlocksMenu extends AbstractMenu {
    public SpecialBlocksMenu(Player player) {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.SPECIAL_BLOCKS), player);
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set special block items
        for(int i = 0; i <= 14; i++) {
            if (getSpecialBlock(i) != null) {
                getMenu().getSlot(i).setItem(getSpecialBlock(i));
            }
        }

        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for special block items
        for(int i = 0; i <= 14; i++) {
            int specialBlockID = i;
            if (getSpecialBlock(i) != null) {
                getMenu().getSlot(i).setClickHandler((clickPlayer, clickInformation) -> {
                    if(!clickPlayer.getInventory().contains(getSpecialBlock(specialBlockID))) {
                        clickPlayer.getInventory().addItem(getSpecialBlock(specialBlockID));
                        clickPlayer.playSound(clickPlayer.getLocation(), Sound.ENTITY_ITEM_PICKUP, 5.0f, 1.0f);
                    }
                });
            }
        }

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> new BuilderUtilitiesMenu(clickPlayer));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(" ").build())
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }

    /**
     * @param ID menu slot
     * @return Special block as ItemStack
     */
    private ItemStack getSpecialBlock(int ID) {
        switch (ID) {
            // First Row
            // Seamless Sandstone
            case 0:
                return SpecialBlocks.SeamlessSandstone;
            // Seamless Red Sandstone
            case 1:
                return SpecialBlocks.SeamlessRedSandstone;
            // Seamless Stone
            case 2:
                return SpecialBlocks.SeamlessStone;
            // Red Mushroom
            case 3:
                return SpecialBlocks.RedMushroom;
            // Seamless Mushroom Stem
            case 4:
                return SpecialBlocks.SeamlessMushroomStem;
            // Brown Mushroom
            case 5:
                return SpecialBlocks.BrownMushroom;
            // Light Brown Mushroom
            case 6:
                return SpecialBlocks.LightBrownMushroom;
            // Barrier
            case 7:
                return SpecialBlocks.Barrier;
            // Structure Void
            case 8:
                return SpecialBlocks.StructureVoid;

            // Second Row
            // Bark Oak Log
            case 9:
                return SpecialBlocks.BarkOakLog;
            // Bark Spruce Log
            case 10:
               return SpecialBlocks.BarkSpruceLog;
            // Bark Birch Log
            case 11:
                return SpecialBlocks.BarkBirchLog;
            // Bark Jungle Log
            case 12:
                return SpecialBlocks.BarkJungleLog;
            // Bark Acacia Log
            case 13:
                return SpecialBlocks.BarkAcaciaLog;
            // Bark Dark Oak Log
            case 14:
                return SpecialBlocks.BarkDarkOakLog;
            default:
                return null;
        }
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(Material.GOLD_BLOCK ,1)
                .setName("§b§l" + LangUtil.getInstance().get(player, LangPaths.MenuTitle.SPECIAL_BLOCKS).toUpperCase())
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(player, LangPaths.MenuDescription.SPECIAL_BLOCKS)).build())
                .build();
    }
}
