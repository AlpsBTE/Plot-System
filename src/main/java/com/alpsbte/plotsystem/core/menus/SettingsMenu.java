package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

public class SettingsMenu extends AbstractMenu {
    public SettingsMenu(Player player) {
        super(3, LangUtil.get(player, LangPaths.MenuTitle.SETTINGS), player);
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set language item
        getMenu().getSlot(13).setItem(MenuItems.errorItem(getMenuPlayer())); // TODO: set language item

        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for language item
        getMenu().getSlot(13).setClickHandler(((clickPlayer, clickInformation) -> new SelectLanguageMenu(clickPlayer)));

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("companion"));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }
}
