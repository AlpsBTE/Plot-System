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

package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Continent;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContinentMenu extends AbstractMenu {
    private final HashMap<Integer, Continent> layout = new HashMap<>();

    ContinentMenu(Player menuPlayer) {
        super(5, LangUtil.getInstance().get(menuPlayer, LangPaths.MenuTitle.COMPANION_SELECT_CONTINENT), menuPlayer);

        layout.put(9, Continent.NORTH_AMERICA);
        layout.put(11, Continent.SOUTH_AMERICA);
        layout.put(13, Continent.EUROPE);
        layout.put(15, Continent.AFRICA);
        layout.put(17, Continent.ASIA);
        layout.put(22, Continent.OCEANIA);
    }

    @Override
    protected void setPreviewItems() {
        getMenu().getSlot(0).setItem(MenuItems.getRandomItem(getMenuPlayer())); // Set random selection item

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(9 * 4, getMenuPlayer(), ContinentMenu::new).entrySet()) {
            getMenu().getSlot(entry.getKey()).setItem(entry.getValue().item);
        }

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        for (Map.Entry<Integer, Continent> continent : layout.entrySet())
            getMenu().getSlot(continent.getKey()).setItem(continent.getValue().getItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        getMenu().getSlot(0).setClickHandler((clickPlayer, clickInformation) -> { // Set click event for random selection item
            List<Continent> layout2 = new java.util.ArrayList<>(layout.values().stream().toList());
            while (!layout2.isEmpty()) {
                var rndContinent = layout2.get(Utils.getRandom().nextInt(layout2.size()));
                var successful = CountryMenu.generateRandomPlot(clickPlayer, Country.getCountries(rndContinent), null);
                if (successful) {
                    return;
                } else {
                    layout2.remove(rndContinent);
                }
            }
        });

        for (Map.Entry<Integer, Continent> continent : layout.entrySet()) {
            getMenu().getSlot(continent.getKey()).setClickHandler((clickPlayer, clickInfo) -> new CountryMenu(clickPlayer, continent.getValue()));
        }

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(9 * 4, getMenuPlayer(), ContinentMenu::new).entrySet()) {
            getMenu().getSlot(entry.getKey()).setClickHandler(entry.getValue().clickHandler);
        }
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(Component.empty()).build())
                .pattern("011111111")
                .pattern("010101010")
                .pattern("111101111")
                .pattern("111111111")
                .pattern("100010001")
                .build();
    }
}
