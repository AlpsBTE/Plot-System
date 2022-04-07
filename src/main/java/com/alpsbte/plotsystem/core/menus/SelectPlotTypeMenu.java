package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.utils.items.builder.LoreBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;

public class SelectPlotTypeMenu extends AbstractMenu {
    private Builder builder;

    public SelectPlotTypeMenu(Player player) {
        super(3, LangUtil.get(player, LangPaths.MenuTitle.SELECT_LANGUAGE), player);
    }

    @Override
    protected void setPreviewItems() {
        super.setPreviewItems();

        builder = new Builder(getMenuPlayer().getUniqueId());
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plot type items
        getMenu().getSlot(11).setItem(
                new ItemBuilder(Utils.CustomHead.FOCUS_MODE.getAsItemStack())
                        .setName("§6§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_FOCUS_MODE))
                        .setLore(new LoreBuilder()
                                .addLines(Utils.createMultilineFromString(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_FOCUS_MODE), AbstractMenu.MAX_CHARS_PER_LINE, AbstractMenu.LINE_BAKER))
                                .build())
                        .build());

        getMenu().getSlot(13).setItem(
                new ItemBuilder(Material.SAPLING, 1, (byte) 5)
                        .setName("§6§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_INSPIRATION_MODE))
                        .setLore(new LoreBuilder()
                                .addLines(Utils.createMultilineFromString(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_INSPIRATION_MODE), AbstractMenu.MAX_CHARS_PER_LINE, AbstractMenu.LINE_BAKER))
                                .build())
                        .build());

        getMenu().getSlot(15).setItem(
                new ItemBuilder(Utils.CustomHead.CITY_INSPIRATION_MODE.getAsItemStack())
                        .setName("§6§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_CITY_INSPIRATION_MODE))
                        .setLore(new LoreBuilder()
                                .addLines(Utils.createMultilineFromString(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_CITY_INSPIRATION_MODE), AbstractMenu.MAX_CHARS_PER_LINE, AbstractMenu.LINE_BAKER))
                                .build())
                        .build());


        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> new SettingsMenu(clickPlayer));
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
