package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.utils.enums.Continent;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.util.HashMap;
import java.util.Map;

public class ContinentMenu extends AbstractMenu {
    private final HashMap<Integer, Continent> layout = new HashMap<>();

    ContinentMenu(Player menuPlayer) {
        super(5, "§n" + LangUtil.get(menuPlayer, LangPaths.MenuTitle.COMPANION_SELECT_CONTINENT), menuPlayer);

        layout.put(9, Continent.NORTH_AMERICA);
        layout.put(11, Continent.SOUTH_AMERICA);
        layout.put(13, Continent.EUROPE);
        layout.put(15, Continent.AFRICA);
        layout.put(17, Continent.ASIA);
        layout.put(22, Continent.OCEANIA);
    }

    @Override
    protected void setPreviewItems() {
        for(Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(9 * 4,getMenuPlayer(), player -> {
            player.closeInventory();
            new ContinentMenu(player);
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setItem(entry.getValue().item);
        }

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        for(Map.Entry<Integer, Continent> continent : layout.entrySet()) {
            getMenu().getSlot(continent.getKey()).setItem(continent.getValue().getItem(getMenuPlayer()));
        }
    }

    @Override
    protected void setItemClickEvents() {
        for(Map.Entry<Integer, Continent> continent : layout.entrySet()) {
            getMenu().getSlot(continent.getKey()).setClickHandler((clickPlayer, clickInfo) -> {
                clickPlayer.closeInventory();
                new CountryMenu(clickPlayer, continent.getValue());
            });
        }

        for(Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(9 * 4,getMenuPlayer(), player -> {
            player.closeInventory();
            new ContinentMenu(player);
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setClickHandler(entry.getValue().clickHandler);
        }
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("010101010")
                .pattern("111101111")
                .pattern("111111111")
                .pattern("100010001")
                .build();
    }
}
