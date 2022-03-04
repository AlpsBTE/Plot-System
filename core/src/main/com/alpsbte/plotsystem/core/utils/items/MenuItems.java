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

package com.alpsbte.plotsystem.core.utils.items;

import com.alpsbte.plotsystem.core.utils.Utils;
import com.alpsbte.plotsystem.core.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.core.utils.items.builder.LoreBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MenuItems {

    public static ItemStack closeMenuItem() {
        return new ItemBuilder(Material.BARRIER)
                .setName("§c§lClose")
                .setLore(new LoreBuilder()
                    .addLine("Close the menu.").build())
                .build();
    }

    public static ItemStack backMenuItem() {
        return new ItemBuilder(Utils.getItemHead(Utils.CustomHead.BACK_BUTTON))
                .setName("§6§lBack")
                .setLore(new LoreBuilder()
                    .addLine("Go back to the last menu.").build())
                .build();
    }

    public static ItemStack nextPageItem() {
        return new ItemBuilder(Utils.getItemHead(Utils.CustomHead.NEXT_BUTTON))
                .setName("§6§lNext Page")
                .setLore(new LoreBuilder()
                    .addLine("Show the next page.").build())
                .build();
    }

    public static ItemStack previousPageItem() {
        return new ItemBuilder(Utils.getItemHead(Utils.CustomHead.PREVIOUS_BUTTON))
                .setName("§6§lPrevious Page")
                .setLore(new LoreBuilder()
                        .addLine("Show the previous page.").build())
                .build();
    }

    public static ItemStack errorItem() {
        return new ItemBuilder(Material.BARRIER)
                .setName("§c§lError")
                .setLore(new LoreBuilder()
                    .addLine("An internal error occurred! Please contact a staff member!").build())
                .build();
    }

    public static ItemStack loadingItem(Material material) {
        return new ItemBuilder(material)
                .setName("§6§lLoading...")
                .build();
    }

    public static ItemStack loadingItem(Material material, byte subId) {
        return new ItemBuilder(material, 1, subId)
                .setName("§6§lLoading...")
                .build();
    }
}
