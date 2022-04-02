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
                new ItemBuilder(Material.STONE)
                        .setName("§6§l" + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_PLOT_TYPE))
                        .build());


        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for language items
        for (int i = 0; i < LangUtil.languages.length; i++) {
            LangUtil.LanguageFile langFile = LangUtil.languages[i];
            getMenu().getSlot(i).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    Builder builder = new Builder(getMenuPlayer().getUniqueId());
                    builder.setLanguageTag(langFile.getTag());
                    getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.Done, 1f, 1f);
                    getMenuPlayer().sendMessage(Utils.getInfoMessageFormat(LangUtil.get(getMenuPlayer(), LangPaths.Message.Info.CHANGED_LANGUAGE, langFile.getLangName())));
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                    getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.ErrorSound, 1f, 1f);
                }

                getMenuPlayer().closeInventory();
            });
        }

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> new SettingsMenu(clickPlayer));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }
}
