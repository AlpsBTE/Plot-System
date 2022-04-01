package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.utils.items.builder.LoreBuilder;
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
        getMenu().getSlot(10).setItem(
                new ItemBuilder(Utils.CustomHead.GLOBE.getAsItemStack())
                        .setName("§6§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_LANGUAGE))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_LANGUAGE))
                                .build())
                        .build());

        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEvents() {
        // Set click event for language item
        getMenu().getSlot(10).setClickHandler(((clickPlayer, clickInformation) -> {
            getMenuPlayer().closeInventory();
            new SelectLanguageMenu(clickPlayer);
        }));

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
