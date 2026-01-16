package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Continent;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
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

        Map<Integer, FooterItem> footerItems = CompanionMenu.getFooterItems(9 * 4, getMenuPlayer(), ContinentMenu::new);
        footerItems.forEach((index, footerItem) -> getMenu().getSlot(index).setItem(footerItem.item()));

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
                var successful = CountryMenu.generateRandomPlot(clickPlayer, DataProvider.COUNTRY.getCountriesByContinent(rndContinent), null);
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

        Map<Integer, FooterItem> footerItems = CompanionMenu.getFooterItems(9 * 4, getMenuPlayer(), ContinentMenu::new);
        footerItems.forEach((index, footerItem) -> getMenu().getSlot(index).setClickHandler(footerItem.clickHandler()));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(Utils.DEFAULT_ITEM)
                .pattern("011111111")
                .pattern("010101010")
                .pattern("111101111")
                .pattern(Utils.FULL_MASK)
                .pattern("100010001")
                .build();
    }
}
