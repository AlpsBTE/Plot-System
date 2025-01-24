/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.utils.items;

import com.alpsbte.alpslib.utils.head.AlpsHeadUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class MenuItems {
    private MenuItems() {}

    public static ItemStack closeMenuItem(Player player) {
        return new ItemBuilder(BaseItems.MENU_CLOSE.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.CLOSE), RED, BOLD))
                .build();
    }

    public static ItemStack backMenuItem(Player player) {
        return new ItemBuilder(AlpsHeadUtils.getCustomHead(CustomHeads.BACK_BUTTON.getId()))
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.BACK), GOLD, BOLD))
                .build();
    }

    public static ItemStack nextPageItem(Player player) {
        return new ItemBuilder(AlpsHeadUtils.getCustomHead(CustomHeads.NEXT_BUTTON.getId()))
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.NEXT_PAGE), GOLD, BOLD))
                .build();
    }

    public static ItemStack previousPageItem(Player player) {
        return new ItemBuilder(AlpsHeadUtils.getCustomHead(CustomHeads.PREVIOUS_BUTTON.getId()))
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.PREVIOUS_PAGE), GOLD, BOLD))
                .build();
    }

    public static ItemStack errorItem(Player player) {
        return new ItemBuilder(BaseItems.MENU_ERROR.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.ERROR), RED, BOLD))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(player, LangPaths.MenuDescription.ERROR)).build())
                .build();
    }

    public static ItemStack loadingItem(Material material, Player player) {
        return new ItemBuilder(material)
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.LOADING), GOLD, BOLD))
                .build();
    }

    @SuppressWarnings("unused")
    public static ItemStack loadingItem(ItemStack itemStack, Player player) {
        return new ItemBuilder(itemStack)
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.LOADING), GOLD, BOLD))
                .build();
    }

    public static ItemStack filterItem(Player langPlayer) {
        return new ItemBuilder(Material.HOPPER, 1)
                .setName(text(LangUtil.getInstance().get(langPlayer, LangPaths.MenuTitle.FILTER_BY_COUNTRY), GOLD, BOLD))
                .build();
    }

    public static ItemStack getRandomItem(Player player) {
        return new ItemBuilder(BaseItems.RANDOM_PLOT_ITEM.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.COMPANION_RANDOM), AQUA).decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .emptyLine()
                        .addLine(text(LangUtil.getInstance().get(player, LangPaths.MenuDescription.COMPANION_RANDOM), GRAY))
                        .build())
                .build();
    }
}
