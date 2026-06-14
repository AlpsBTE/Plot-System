package com.alpsbte.plotsystem.utils.items;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class MenuItems {
    private MenuItems() {}

    public static ItemStack closeMenuItem(Player player) {
        return new ItemBuilder(BaseItems.MENU_CLOSE.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.CLOSE), RED, BOLD))
                .build();
    }

    public static ItemStack backMenuItem(Player player) {
        return new ItemBuilder(BaseItems.MENU_BACK.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.BACK), GOLD, BOLD))
                .build();
    }

    public static ItemStack continueMenuItem(Player player) {
        return new ItemBuilder(BaseItems.MENU_NEXT.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.CONTINUE), GOLD, BOLD))
                .build();
    }

    public static ItemStack nextPageItem(Player player) {
        return new ItemBuilder(BaseItems.MENU_NEXT.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.NEXT_PAGE), GOLD, BOLD))
                .build();
    }

    public static ItemStack previousPageItem(Player player) {
        return new ItemBuilder(BaseItems.MENU_BACK.getItem())
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
        return new ItemBuilder(BaseItems.FILTER_ITEM.getItem())
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
