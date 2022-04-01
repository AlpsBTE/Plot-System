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

public class SelectLanguageMenu extends AbstractMenu {
    private Builder builder;
    private boolean isAutoDetectEnabled;

    public SelectLanguageMenu(Player player) {
        super(3, LangUtil.get(player, LangPaths.MenuTitle.SELECT_LANGUAGE), player);
    }

    @Override
    protected void setPreviewItems() {
        super.setPreviewItems();

        builder = new Builder(getMenuPlayer().getUniqueId());
        isAutoDetectEnabled = builder.getLanguageTag() == null;
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set language items
        for (int i = 0; i < LangUtil.languages.length; i++) {
            LangUtil.LanguageFile langFile = LangUtil.languages[i];
            getMenu().getSlot(i).setItem(
                    new ItemBuilder(langFile.getHead())
                            .setName("§6§l" + langFile.getLangName())
                            .build());
        }

        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        // Set auto-detect language item
        getMenu().getSlot(25).setItem(
                new ItemBuilder(new ItemStack(isAutoDetectEnabled ? Material.SLIME_BALL : Material.MAGMA_CREAM))
                        .setName((isAutoDetectEnabled ? "§a§l" : "§c§l") + LangUtil.get(getMenuPlayer(), LangPaths.MenuTitle.AUTO_DETECT_LANGUAGE))
                        .setLore(new LoreBuilder()
                                .addLines(LangUtil.get(getMenuPlayer(), LangPaths.MenuDescription.AUTO_DETECT_LANGUAGE),
                                        "",
                                        "§6" + LangUtil.get(getMenuPlayer(), isAutoDetectEnabled ? LangPaths.Note.Action.CLICK_TO_DISABLE : LangPaths.Note.Action.CLICK_TO_ENABLE))
                                .build())
                        .build());
    }

    @Override
    protected void setItemClickEvents() {
        // Set click event for language items
        for (int i = 0; i < LangUtil.languages.length; i++) {
            LangUtil.LanguageFile langFile = LangUtil.languages[i];
            getMenu().getSlot(i).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    Builder builder = new Builder(getMenuPlayer().getUniqueId());
                    builder.setLanguageTag(langFile.getTag());
                    Utils.updatePlayerInventorySlots(clickPlayer);

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

        // Set click event for auto-detect language item
        getMenu().getSlot(25).setClickHandler((clickPlayer, clickInformation) -> {
            try {
                builder.setLanguageTag(isAutoDetectEnabled ? LangUtil.languages[0].getTag() : null);
                Utils.updatePlayerInventorySlots(clickPlayer);
                reloadMenuAsync();

                getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.Done, 1f, 1f);
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.ErrorSound, 1f, 1f);
            }
        });
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
